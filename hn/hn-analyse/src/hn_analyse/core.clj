(ns hn-analyse.core
  (:require [net.cgrand.enlive-html :as html]
            [clojure.data.json :as json]
            [clj-rss.core :as rss])
  (:use [clojure.tools.cli :only [cli]])
  (:import (de.jetwick.snacktory HtmlFetcher))
  (:gen-class))

(defn parse-int [s]
  (let [ss (re-find #"[0-9]*" s)]
    (if (empty? ss) 0
      (Integer. ss))))

(defn select-contents [res]
  (html/select res
               #{ [:td.title html/first-child]
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

(defn load-files [d]
  (sort #(< (.lastModified %) (.lastModified %2))
        (remove #(.isDirectory %)
                (file-seq (clojure.java.io/file d)))))

(defn files-to-map [d]
  (map #(gen-result (select-contents %))
       (map html/html-resource (load-files d))))

(defn map-to-results [m]
    (sort-by :points > (vals (apply merge m))))

(defn to-markdown [m]
  (for [{:keys [points title link comments-link]} m]
    (format "- [%s](%s) (_%s_) &nbsp; &nbsp; [_comments_](https://news.ycombinator.com/%s)  \r\n"
            title link points comments-link)))

(defn run [{:keys [source count format]}]
  (let [data (take count (map-to-results (files-to-map source)))]
    (cond
     (= format "edn") (clojure.pprint/pprint data)
     (= format "md") (doseq [item (to-markdown data)] (println item))
     (= format "rss")(println
                      (apply rss/channel-xml {:title "Hacker Weekly"
                                              :link "http://llj098.github.io/hw"
                                              :description "weekly hacker news"}
                             (map #(select-keys % [:title :link]) data))))))

(defn -main [& args]
  (let [pa (cli args
                ["-s" "--source" "source" :default nil]
                ["-c" "--count" "item count want to get,optional, default 30"
                 :parse-fn #(Integer. %) :default 30]
                ["-f" "--format" "output format, optional, default edn":default "edn"])]
    (if (> (count (filter nil? (vals (first pa)))) 0)
      (println (last pa))
      (run (first pa)))))
