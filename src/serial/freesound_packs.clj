(ns serial.freesound-packs
  (:require [clojure.string :as string]
            [net.cgrand.enlive-html :as html]))

(defn fetch-url [url]
  (html/html-resource (java.net.URL. url)))

(defn url->sound-id
  [url]
  (-> (re-seq #"sounds\/(\d+)\/"
              url)
      first
      second
      Integer/parseInt))

(defn tap
  [val msg]
  (println msg val)
  val)

(defn title->pitch
  [title]
  (->> (map #(some-> (re-seq #"([A-G]#?-\d)"
                             %)
                     first
                     second
                     (string/replace #"-" "")
                     keyword)
            title)
       (filter identity)
       first))

(defn pitch-and-sound-id
  [node]
  (let [href (first (html/attr-values node :href))
        title (html/attr-values node :title)]
    {:pitch (title->pitch title)
     :sound-id (url->sound-id href)}))

(defn pack-sound-urls
  [url]
  (map pitch-and-sound-id
       (html/select (fetch-url url)
                    [:.sound_filename :a])))

(defonce example-sound-url
  "https://www.freesound.org/people/Carlos_Vaquero/sounds/153622/")
(defonce example-pack-url
  "https://www.freesound.org/people/Carlos_Vaquero/packs/9513/")

(defn pack-url->notes-map
  "Example: https://www.freesound.org/people/Carlos_Vaquero/packs/9513/"
  [url]
  (into {}
        (map (fn [x] [(:sound-id x)
                     (:pitch x)])
             (pack-sound-urls url))))

(defn -main
  [& args]
  (if (seq args)
    (let [pack-url (first args)]
      (pack-url->notes-map pack-url))
    (println "Usage: lein run ... [pack-url]")))
