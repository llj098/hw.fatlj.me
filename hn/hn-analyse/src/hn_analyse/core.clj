(ns hn-analyse.core
  (:require [net.cgrand.enlive-html :as html]))

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

(defn files-to-results [d]
  (map #(gen-result (select-contents %))
       (map html/html-resource (load-files d))))

(defn load-files [d]
  (sort #(< (.lastModified %) (.lastModified %2))
        (remove #(.isDirectory %)
                (file-seq (clojure.java.io/file d)))))

(defn -main [d]
  (apply merge (files-to-results d)))
