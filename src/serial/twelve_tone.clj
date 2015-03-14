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

(def ranges
  {:violin (range 67 80)
   :cello (range 50 63)
   :piano (range 21 109)})

(defn mid-range
  [r]
  (let [inst-range (get ranges r (:piano ranges))
        lowest (first inst-range)
        highest (last inst-range)]
    (int (+ lowest
            (/ (- highest lowest) 2)))))

;; (sampled-pizzicato-cello :note 59)
;; (sampled-vibrato-cello :note 59 :level 10)
(defmethod live/play-note :violin [{midi :pitch seconds :duration}]
  (sampled-pizzicato-violin :note midi
                            :level 8))
(defmethod live/play-note :cello [{midi :pitch seconds :duration}]
  (sampled-pizzicato-cello :note midi
                           :level 8))
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

(defn play-in-range
  [r row]
  (->> row
       (where :pitch (comp (scale/from (mid-range r))
                           scale/chromatic))))

(defn play-on
  [instrument row]
  (->> row
       (where :part (is instrument))
       (play-in-range instrument)))

(defn play-tone-row
  [row bloops-per-minute]
  (->> row
       (where :time (bpm bloops-per-minute))
       (where :duration (bpm bloops-per-minute))
       ;;(where :pitch (comp (scale/from 45) scale/chromatic))
       ;;(where :pitch (comp (scale/from (mid-range piano-range)) scale/chromatic))
       live/play))

(comment
  (play-tone-row tone-row 90))

(defn total-tone-row []
  (phrase (random-rhythms)
          (shuffle (range 0 12))))

(def my-tone-row (total-tone-row))

(comment
  (play-tone-row my-tone-row 60))

(defn piano-trio
  []
  (let [my-row (random-pitches)
        bpm 60]
    (play-tone-row
     (with
      (->> (phrase (random-rhythms) my-row)
           (play-on :cello))
      (->> (phrase (random-rhythms)
                   (retrograde-inversion my-row))
           (play-on :violin))
      (->> (phrase (random-rhythms)
                   (retrograde my-row))
           (play-on :piano)))
     bpm)))

(comment (piano-trio))
