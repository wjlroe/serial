(ns serial.twelve-tone
  (:require [overtone.live :refer :all]
            [overtone.inst.sampled-piano :refer :all]
            [leipzig.live :as live]
            [leipzig.scale :as scale]))

(defmethod live/play-note :default [{midi :pitch seconds :duration}]
  (-> midi (sampled-piano)))

(def melody
  ;; Row,  row,  row   your  boat
  (phrase [3/3   3/3   2/3   1/3   3/3]
          [  0     0     0     1     2]))

(comment
  (->> melody
       (where :time (bpm 90))
       (where :duration (bpm 90))
       (where :pitch (comp scale/C scale/major))
       live/play))
