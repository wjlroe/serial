(ns serial.core
  (:require [leipzig.melody :refer [bpm is phrase then times where with]]
            [overtone
             [core :as overtone]]
            ;; [overtone.sc
            ;;  [ugens :as scu]
            ;;  [envelope :as sce]
            ;;  [defcgen :as scdcgen]]
            ;; [overtone.inst.synth :as instsy]
            [leipzig.live :as live]
            [leipzig.scale :as scale]
            ;; [overtone.orchestra
            ;;  [oboe :refer [oboe]]
            ;;  [cello :refer [cello]]]
            ))

(overtone/connect-external-server)

(overtone/definst beep [freq 440 dur 1.0]
  (-> freq
      overtone/saw
      (* (overtone/env-gen (overtone/perc 0.05 dur) :action overtone/FREE))))

(defmethod live/play-note :default [{midi :pitch seconds :duration}]
  (-> midi (beep)))

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
