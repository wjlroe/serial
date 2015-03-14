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

(def styles
  {:violin [::pizzicato
            ::non-vibrato]
   :cello [::pizzicato
           ::non-vibrato
           ::vibrato]})

(defn mid-range
  [r]
  (let [inst-range (get ranges r (:piano ranges))
        lowest (first inst-range)
        highest (last inst-range)]
    (int (+ lowest
            (/ (- highest lowest) 2)))))

;; (sampled-pizzicato-cello :note 59)
;; (sampled-vibrato-cello :note 59 :level 10)
(defmethod live/play-note :violin [{midi :pitch seconds :duration
                                    style :style}]
  (condp = style
    ::pizzicato (sampled-pizzicato-violin :note midi
                                          :level 8)
    ::non-vibrato (sampled-non-vibrato-violin :note midi
                                              :level 8)))
(defmethod live/play-note :cello [{midi :pitch seconds :duration
                                   style :style}]
  (condp = style
    ::pizzicato (sampled-pizzicato-cello :note midi
                                         :level 8)
    ::non-vibrato (sampled-non-vibrato-cello :note midi
                                             :level 8)
    ::vibrato (sampled-vibrato-cello :note midi
                                     :level 8)))
(defmethod live/play-note :default [{midi :pitch seconds :duration}]
  (-> midi (sampled-piano)))

(defn random-rhythms
  []
  (shuffle (map #(/ % 12) (range 1 13))))

(comment (sum (random-rhythms)))

(defn split-into
  [n coll]
  (let [l-size (/ (count coll) n)]
    (partition-all l-size coll)))

(comment (split-into 5 (random-pitches)))

(def sum' (partial apply +))

(defn squash-durations
  [durations row]
  (map sum'
       (split-into (count row) durations)))

(comment (squash-durations (random-rhythms)
                           (hexachords (random-pitches))))

(defn total-serial
  [row]
  (phrase (squash-durations (random-rhythms)
                            row)
          row))

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

(defn chord-key
  [k]
  (keyword (str "k" k)))

(defn chordify
  [row]
  (zipmap (map chord-key
               (range 1 (inc (count row)))) row))

(defn hexachords
  [row]
  (map chordify (partition 6 row)))

(def row-of-notes
  (phrase (repeat 3/3)
          (range 0 12)))

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

(defn serial-style
  [instrument row]
  (let [available (cycle (shuffle (get styles instrument [::default])))]
    (map-indexed (fn [i note]
                   (assoc note :style (nth available i)))
                 row)))

(defn in-tempo
  [bloops-per-minute row]
  (->> row
       (where :time (bpm bloops-per-minute))
       (where :duration (bpm bloops-per-minute))))

(defn total-tone-row []
  (phrase (random-rhythms)
          (shuffle (range 0 12))))

(def my-tone-row (total-tone-row))

(comment
  (in-tempo 60 my-tone-row))

(defn piano-trio
  [bpm tone-row]
  (in-tempo
   bpm
   (with
    (->> (total-serial tone-row)
         (play-on :cello)
         (serial-style :cello))
    (->> (total-serial (retrograde-inversion tone-row))
         (play-on :violin)
         (serial-style :violin))
    (->> (total-serial (hexachords (retrograde tone-row)))
         (play-on :piano)
         (serial-style :piano)))))

(def tone-row1 (random-pitches))
(comment (piano-trio 60 tone-row1))
(comment
  (live/play (piano-trio 60 tone-row1)))
