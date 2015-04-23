(ns maxdome-parser.core
  (:require [clojure.data.json :as json]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io]))
(use 'jsoup.soup)

(def maxdome-baseurl "https://store.maxdome.de/spielfilm/all")

(def maxdome-ajax "https://store.maxdome.de/_ajax/asset/")

(defn film-url [film-id]
  (str maxdome-ajax film-id))

(defn get-page! [start & [size]]
  (get! (str maxdome-baseurl "?start=" start "&size=" (or size 72))))

(defn films-list [doc]
  (map
   #(.attr % "data-asset-id")
   ($ doc "div.content")))

(defn get-film! [id]
  (Thread/sleep 100)
  (slurp (film-url id)))

(defn parse-film [json-text]
  (let [json (json/read-str json-text)]
    (assoc nil
      :id (get json "id")
      :title (get json "title")
      :year (get json "productionYear"))))

(defn parse-page! [doc]
  (map #(parse-film (get-film! %)) (films-list doc)))

(defn write-page! [films]
  (with-open [out-file (io/writer "out-file.csv" :append true)]
    (csv/write-csv out-file (map vals films) :separator \;)))

(defn main [& args]
  (loop [page 1]
    (let [parsed (parse-page! (getpage! page))]
      (when-not (empty? parsed)
        (write-page! parsed)
        (println "page" page "processed")
        (recur (inc page))))))

