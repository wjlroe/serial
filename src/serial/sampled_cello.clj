(ns serial.sampled-cello
  (:require [overtone.core :refer :all]))

(defn- registered-pizzicato-cello-samples
  []
  (registered-assets ::CelloPizzicato))
(defn- registered-non-vibrato-cello-samples
  []
  (registered-assets ::CelloTenutoNonVibrato))
(defn- registered-vibrato-cello-samples
  []
  (registered-assets ::CelloTenutoVibrato))

;; Pizzicato Cello

(def FREESOUND-PIZZICATO-CELLO-SAMPLES
  {153753 :B3 153754 :C4 153752 :A#3 153756 :D4 153755 :C#4 153751 :A3 153750 :G#3
   153746 :E3 153749 :G3 153748 :F#3 153747 :F3 153745 :D#3 153744 :D3})

(def FREESOUND-NON-VIBRATO-CELLO-SAMPLES
  {153741 :C4 153736 :G3 153740 :B3 153731 :D3 153737 :G#3 153734 :F3 153738 :A3
   153735 :F#3 153742 :C#4 153732 :D#3 153733 :E3 153739 :A#3 153743 :A3})

(def FREESOUND-VIBRATO-CELLO-SAMPLES
  {153766 :B3 153765 :A#3 153767 :C4 153761 :F#3 153762 :G3 153757 :D3 153768 :C#4
   153763 :G#3 153759 :E3 153758 :D#3 153764 :A3 153760 :F3 153769 :D4})

(def PIZZICATO-CELLO-SAMPLE-IDS
  (keys FREESOUND-PIZZICATO-CELLO-SAMPLES))

(def NON-VIBRATO-CELLO-SAMPLE-IDS
  (keys FREESOUND-NON-VIBRATO-CELLO-SAMPLES))

(def VIBRATO-CELLO-SAMPLE-IDS
  (keys FREESOUND-VIBRATO-CELLO-SAMPLES))

(def pizzicato-cello-samples
  (doall (map freesound-sample PIZZICATO-CELLO-SAMPLE-IDS)))

(def non-vibrato-cello-samples
  (doall (map freesound-sample NON-VIBRATO-CELLO-SAMPLE-IDS)))

(def vibrato-cello-samples
  (doall (map freesound-sample VIBRATO-CELLO-SAMPLE-IDS)))

(defn- buffer->midi-note [buf note-map] (-> buf :freesound-id note-map name note))

(defn- note-index [buffers note-map]
  (reduce (fn [index buf]
            (let [id (:id buf)
                  note (buffer->midi-note buf note-map)]
              (assoc index note id)))
          {}
          buffers))

(defonce ^:private silent-buffer (buffer 0))

(defonce pizzicato-index-buffer
  (let [tab (note-index pizzicato-cello-samples FREESOUND-PIZZICATO-CELLO-SAMPLES)
        buf (buffer 128)]
    (buffer-fill! buf (:id silent-buffer))
    (doseq [[idx val] tab]
      (buffer-set! buf idx val))
    buf))

(defonce non-vibrato-index-buffer
  (let [tab (note-index non-vibrato-cello-samples FREESOUND-NON-VIBRATO-CELLO-SAMPLES)
        buf (buffer 128)]
    (buffer-fill! buf (:id silent-buffer))
    (doseq [[idx val] tab]
      (buffer-set! buf idx val))
    buf))

(defonce vibrato-index-buffer
  (let [tab (note-index vibrato-cello-samples FREESOUND-VIBRATO-CELLO-SAMPLES)
        buf (buffer 128)]
    (buffer-fill! buf (:id silent-buffer))
    (doseq [[idx val] tab]
      (buffer-set! buf idx val))
    buf))

(definst sampled-pizzicato-cello
  [note 60 level 1 rate 1 loop? 0
   attack 0 decay 1 sustain 1 release 0.1
   curve -4 gate 1]
  (let [buf (index:kr (:id pizzicato-index-buffer) note)
        env (env-gen (adsr attack decay sustain release level curve)
                     :gate gate
                     :action FREE)]
    (* env
       (scaled-play-buf 2 buf :level level :loop loop? :action FREE))))

(definst sampled-non-vibrato-cello
  [note 60 level 1 rate 1 loop? 0
   attack 0 decay 1 sustain 1 release 0.1
   curve -4 gate 1]
  (let [buf (index:kr (:id non-vibrato-index-buffer) note)
        env (env-gen (adsr attack decay sustain release level curve)
                     :gate gate
                     :action FREE)]
    (* env
       (scaled-play-buf 2 buf :level level :loop loop? :action FREE))))

(definst sampled-vibrato-cello
  [note 60 level 1 rate 1 loop? 0
   attack 0 decay 1 sustain 1 release 0.1
   curve -4 gate 1]
  (let [buf (index:kr (:id vibrato-index-buffer) note)
        env (env-gen (adsr attack decay sustain release level curve)
                     :gate gate
                     :action FREE)]
    (* env
       (scaled-play-buf 2 buf :level level :loop loop? :action FREE))))
