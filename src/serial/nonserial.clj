(ns serial.nonserial
  (:require [overtone.live :refer :all]
            [overtone.inst.sampled-piano :refer :all]
            [leipzig.live :as live]
            [leipzig.scale :as scale]
            [leipzig.chord :as chord]
            [leipzig.temperament :as temperament]
            [leipzig.melody :refer
             [bpm is phrase then times where with wherever mapthen]]
            [serial.sampled-violin :refer
             [sampled-pizzicato-violin
              sampled-non-vibrato-violin
              sampled-vibrato-violin]]
            [serial.sampled-cello :refer
             [sampled-pizzicato-cello
              sampled-non-vibrato-cello
              sampled-vibrato-cello]]))

(def salamander-path "/Users/will/Downloads/salamanderDrumkit/OH/")

(def kick1          (sample (str salamander-path "kick_OH_FF_9.wav")))
(def ride2crash (sample (str salamander-path "ride2Crash_OH_FF_6.wav")))
(def snare1 (sample (str salamander-path "snare_OH_FF_9.wav")))
(def bellchime (sample (str salamander-path "bellchime_F_3.wav")))

(comment
  (kick1)
  (ride2crash)
  (snare1)
  (bellchime))

(definst fakekick [freq 110]
  (-> (line:kr freq (* freq 1/2) 0.5)
      sin-osc
      (+ (sin-osc freq))
      (* (env-gen (perc 0.01 0.3) :action FREE))
      (* 1/1)))

;; (fakekick)

(definst bass [freq 110]
  (-> freq
      saw
      (rlpf (line:kr (* freq 10) freq 1))
      (* (env-gen (perc 0.1 0.4) :action FREE))))

(definst organ [freq 440 dur 1]
  (-> freq
      saw
      (rlpf (mul-add (sin-osc 3) 300 (* freq 4)))
      (rlpf (mul-add (sin-osc 2) 400 (* freq 3)))
      (rlpf (mul-add (sin-osc 2) 200 (* freq 5)))
      (* (env-gen (adsr) (line:kr 1 0 dur) :action FREE))
      (* 1/4)))

;; (organ 440 2)

(defmethod live/play-note :piano [{midi :pitch}]
  (sampled-piano :note midi :level 0.6))

(defmethod live/play-note :fakekick [{freq :frequency}]
  (kick1))

(defmethod live/play-note :fakeorgan [{freq :frequency seconds :duration}]
  (organ freq seconds :level 1.2))

(defmethod live/play-note :bells [{midi :pitch seconds :duration}]
  (bellchime :level 0.1))

(defn organstuff [root]
  (->> (phrase (repeat 8) [(-> chord/triad (chord/root root))])
       (where :part (is :fakeorgan))))

(def beat
  (->>
   (phrase (cycle [1/2 1/4 1/4 1/2 1/2]) (repeat -14))
   (take 20)
   (times 1)
   (where :part (is :fakekick))))

(def offbeat
  (->>
   (phrase (cycle [1/2 1/4 1/4 1/2 1/2]) (repeat -14))
   (take 20)
   (times 6)
   (where :part (is :offbeat))))

(def put-bells-on-it
  (->>
   (phrase (cycle [1 1/2 1/4 1/4]) (repeat -14))
   (take 20)
   (times 6)
   (where :part (is :bells))))

(def first-tune
  (phrase (cycle [1 1/2 1/2 1 1]) [0 -3 -1 0 2 0 2 3 2 0]))

(def dramatic-motif
  (phrase (cycle [3 1/3 1/3 1/3 1/3 1/3 1/3 3]) [0 0 -1 -3 -8 -7 -5 -3]))

(def motiff
  ;; [0 0 -1 -3 -8 -7 -5]
  [0 0 -1 -2 -5 -4 -3])

(def inverse-motif
  (map - motiff))

(def retrograde-motif
  (reverse motiff))

(def retrograde-inversion
  (reverse inverse-motif))

(defn down-third
  [n]
  (- n 2))

(defn starting-notes
  [f n]
  (loop [prev-note n notes [n]]
    (let [next-note (f prev-note)]
      (if (zero? (mod next-note 7))
        notes
        (recur next-note (conj notes next-note))))))

;; (starting-notes down-third 0)

(defn join-motifs
  [notes transform]
  (reduce (fn [acc starting-note]
            (concat acc (map #(+ starting-note %) notes)))
          []
          (starting-notes transform 0)))

(def cycling-motiff
  (phrase (cycle [3 1/3 1/3 1/3 1/3 1/3 1/3])
          (join-motifs motiff down-third)
          ;;(concat motiff (map down-third motiff))
          ))

;; (count cycling-motiff)

(defn basspiano [root]
  (->>
   ;;first-tune
   ;;  dramatic-motif
   cycling-motiff
       (where :pitch (scale/from root))
       ;; (where :pitch (comp scale/lower scale/lower
       ;;                     ;; scale/lower
       ;;                     ))
       (where :part (is :piano))))

;; (basspiano 0)
(def progression
  ;; [0 0 3 0 4 0]
  (repeat 1 0)
  )

(defn also
  [k f notes]
  (map (fn [n] (assoc-in n [k] (f n)))
       notes))

(comment
  (def track2
    (->>
     (mapthen basspiano progression)
     (with (mapthen (repeat 49 organstuff) progression))
     (with beat)
     (where :pitch (comp scale/C scale/major))
     (also :frequency (comp temperament/equal :pitch))
     (where :time (bpm 60))
     (where :duration (bpm 60)))))

(def track
  (->>
   (mapthen basspiano progression)
   (with (mapthen organstuff progression))
   (with beat)
   (with put-bells-on-it)
   (where :pitch (comp scale/C scale/major))
   (also :frequency (comp temperament/equal :pitch))
   (where :time (bpm 60))
   (where :duration (bpm 60))))

(defn val=
  [k v]
  (fn [x]
    (= (get x k) v)))

(defn and-reduce
  [val tests]
  (reduce (fn [acc tfun] (and acc (tfun val)))
          true
          tests))

(defn filter-and
  [val & tests]
  (filter #(and-reduce % tests)
          val))

(comment
  (filter-and track2
              (val= :part :fakeorgan)
              (val= :time 0))

  (take 8 (filter-and track
               (val= :part :piano))))


;; (filter (fn [x] (= (:part x) :fakeorgan)) track2)
;; (organ 440)
;; (filter (fn [x] (= (:part x) :fakekick)) track2)
;; (fakekick 75)
;; (filter (fn [x] (= (:part x) :piano)) track2)
;; (live/stop)
;; (live/jam (var track))
;; (live/play track)

(comment
  (do
    (recording-start "soundtrack.wav")
    (deref (live/play track))
    (Thread/sleep 4000)
    (recording-stop)))


(comment
  (->> (phrase [1/2 2/2 3/4]
               [nil chord/triad
                nil (-> chord/seventh (chord/root 4) (chord/inversion 1) (dissoc :v))
                nil chord/triad
                nil chord/triad])
       (where :part (is :piano))
       (wherever :pitch, :pitch (comp scale/C scale/major))
       (in-tempo 60)
       (play-on :piano)
       live/play
       ))
