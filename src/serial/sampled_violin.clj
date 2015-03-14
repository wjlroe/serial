(ns serial.sampled-violin
  (:require [overtone.core :refer :all]))

(defn- registered-pizzicato-violin-samples
  []
  (registered-assets ::ViolinPizzicato))
(defn- registered-non-vibrato-violin-samples
  []
  (registered-assets ::ViolinTenutoNonVibrato))

;; Pizzicato Violin

(def FREESOUND-PIZZICATO-VIOLIN-SAMPLES
  {153622 :G5 153620 :F5 153612 :G#4 153618 :D#5 153614 :A#4 153615 :B4 153611 :G4
   153617 :D5 153616 :C5 153613 :A4 153621 :F#5 153619 :E5})

(def PIZZICATO-VIOLIN-SAMPLE-IDS (keys FREESOUND-PIZZICATO-VIOLIN-SAMPLES))

(def pizzicato-violin-samples
  (doall (map freesound-sample PIZZICATO-VIOLIN-SAMPLE-IDS)))

;; Non-vibrato violin

(def FREESOUND-NON-VIBRATO-VIOLIN-SAMPLES
  {153588 :A#4 153586 :G#4 153596 :F#5 153590 :C5 153593 :D#5 153591 :C#5 153595 :F5
   153594 :E5 153587 :A4 153592 :D5 153597 :G5 153585 :G4 153589 :B4})

(def NON-VIBRATO-VIOLIN-SAMPLE-IDS
  (keys FREESOUND-NON-VIBRATO-VIOLIN-SAMPLES))

(def non-vibrato-violin-samples
  (doall (map freesound-sample NON-VIBRATO-VIOLIN-SAMPLE-IDS)))

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
  (let [tab (note-index pizzicato-violin-samples FREESOUND-PIZZICATO-VIOLIN-SAMPLES)
        buf (buffer 128)]
    (buffer-fill! buf (:id silent-buffer))
    (doseq [[idx val] tab]
      (buffer-set! buf idx val))
    buf))

(definst sampled-pizzicato-violin
  [note 60 level 1 rate 1 loop? 0
   attack 0 decay 1 sustain 1 release 0.1
   curve -4 gate 1 position 0]
  (let [buf (index:kr (:id pizzicato-index-buffer) note)
        env (env-gen (adsr attack decay sustain release level curve)
                     :gate gate
                     :action FREE)]
    (* env
       (pan2 (scaled-play-buf 1 buf :level level :loop loop? :action FREE) position))))

(defonce non-vibrato-index-buffer
  (let [tab (note-index non-vibrato-violin-samples FREESOUND-NON-VIBRATO-VIOLIN-SAMPLES)
        buf (buffer 128)]
    (buffer-fill! buf (:id silent-buffer))
    (doseq [[idx val] tab]
      (buffer-set! buf idx val))
    buf))

(definst sampled-non-vibrato-violin
  [note 60 level 1 rate 1 loop? 0
   attack 0 decay 1 sustain 1 release 0.1
   curve -4 gate 1 position 0]
  (let [buf (index:kr (:id non-vibrato-index-buffer) note)
        env (env-gen (adsr attack decay sustain release level curve)
                     :gate gate
                     :action FREE)]
    (* env
       (pan2 (scaled-play-buf 1 buf :level level :loop loop? :action FREE) position))))
