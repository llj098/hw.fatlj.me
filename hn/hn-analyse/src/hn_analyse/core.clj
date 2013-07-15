(ns hn-analyse.core
  (:require [net.cgrand.enlive-html :as html]
            [clojure.data.json :as json])
  (:import (de.jetwick.snacktory HtmlFetcher))
  (:gen-class))

(defn parse-int [s]
  (let [ss (re-find #"[0-9]*" s)]
    (if (empty? ss) 0
      (Integer. ss))))

(defn select-contents [res]
  (html/select res
               #{[:td.title :a]
                 [:td.subtext html/first-child]
                 [:td.subtext html/last-child]}))

(defn gen-result [c]
  (reduce #(assoc %1 (:comments-link %2) %2)  {}
          (for [[title points comments] (partition 3 c)]
            {:link (:href (:attrs title))
             :title (html/text title)
             :points (parse-int (html/text points))
             :comments (parse-int (html/text comments))
             :comments-link  (:href (:attrs comments))})))

(defn load-files
  ( [d]
      (sort #(< (.lastModified %) (.lastModified %2))
            (remove #(.isDirectory %)
                    (file-seq (clojure.java.io/file d)))))
  ([d hours]
     (let [files (load-files d)]
       (filter #(< (clj-time.core/in-hours
                    (clj-time.core/interval
                     (clj-time.coerce/from-long (.lastModified %))
                     (clj-time.core/now)))
                   hours)
               files))))

(defn files-to-map [d]
  (map #(gen-result (select-contents %))
       (map html/html-resource (load-files d))))

(defn map-to-results [m]
    (sort-by :points > (vals (apply merge m))))

(defn to-markdown [m]
  (for [{:keys [points title link comments-link]} m]
    (format "[%s](%s) (_%s_) &nbsp; &nbsp; [_hn comments_](https://news.ycombinator.com/%s)"
            title link points comments-link)))

(defn -main [d limit]
  (do
    (json/write
     (take (read-string limit)
           (map-to-results (files-to-map d)))
     *out*)
    (flush)))
