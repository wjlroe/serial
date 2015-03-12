(ns serial.twelve-tone
  (:require [overtone.live :refer :all]
            [overtone.inst.sampled-piano :refer :all]
            [leipzig.live :as live]
            [leipzig.scale :as scale]
            [leipzig.melody :refer [bpm is phrase then times where with]]
            [serial.sampled-violin :refer
             [sampled-pizzicato-violin
              sampled-non-vibrato-violin]]
            [serial.sampled-cello :refer
             [sampled-pizzicato-cello
              sampled-non-vibrato-cello
              sampled-vibrato-cello]]))

(comment
  (on-event
   [:midi :note-on]
   (fn [m]
     (let [note (:note m)]
       (sampled-piano :note note
                      :level (:velocity-f m))))
   ::nanokey-midi))

(comment
  (on-event [:midi :note-on]
            (fn [m]
              (println "note received:" m))
            ::midi-debug))

(def violin-range (range 67 80))
(def cello-range (range 50 63))
(def piano-range (range 21 109))

(defmethod live/play-note :violin [{midi :pitch seconds :duration}]
  (-> midi (sampled-pizzicato-violin)))
(defmethod live/play-note :default [{midi :pitch seconds :duration}]
  (-> midi (sampled-piano)))

(defn random-rhythms
  []
  (shuffle (map #(/ % 12) (range 1 13))))

(defn random-pitches
  []
  (shuffle (range 0 12)))

(defn random-tone-row
  []
  (shuffle (range 0 12)))

(defn retrograde
  [row]
  (reverse row))

(defn inversion
  [row]
  (map - row))

(def retrograde-inversion (comp retrograde inversion))

(def row-of-notes
  (phrase (repeat 3/3)
          (range 0 12)))

(def tone-row
  (phrase (repeat 3/3)
          (random-pitches)))

(defn play-tone-row
  [row bloops-per-minute]
  (->> row
       (where :time (bpm bloops-per-minute))
       (where :duration (bpm bloops-per-minute))
       (where :pitch (comp (scale/from 60) scale/chromatic))
       live/play))

(comment
  (play-tone-row tone-row 90))

(defn total-tone-row []
  (phrase (random-rhythms)
          (shuffle (range 0 12))))

(def my-tone-row (total-tone-row))

(comment
  (play-tone-row my-tone-row 60))

(comment
  (let [my-row (random-pitches)]
    (play-tone-row (with (phrase (random-rhythms)
                                 my-row)
                         (->> (phrase (random-rhythms)
                                      (retrograde-inversion my-row))
                              (where :part (is :piano))))
                   60)))
