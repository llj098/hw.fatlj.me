(ns hn-analyse.core
  (:require [net.cgrand.enlive-html :as html]))


(defn -main[]
  (let [result (redis/zrange "hn-page" 0 -1)
        all-news (map #(html/html-resourse (java.io.StringReader. (b64/decode (.getBytes %)))) result)]
   (apply hn-result  (mqp #(hn-one-page (hn-headlines %) (hn-points %) (hn-comments %)) all-news))))

(defn hn-headlines [h]
  (let [t (html/select h [:td.title :a])]
    map #({:name (html/text %) :link (-> % :attrs :href)}) t))

(defn hn-points [h]
  (map #(hash-map :points (.replaceAll (html/text %) "\\D" ""))  (html/select h [:td.subtext html/first-child])))

(defn hn-comments [h]
  (map #(hash-map :comments (.replaceAll (html/text %) "\\D" ""))  (html/select h [:td.subtext html/last-child])))

(defn hn-one-page[hl hp hc]
  (let [l (map-indexed (fn [x y] (merge y (nth hp x) (nth hc x))) hl)]
    (sort-by :points > l)))

(defn hn-result[ & pages]
  (sort-by :points > (first (vals (group-by :name (concat pages))))))


(defn parse-int [s]
  (let [ss (re-find #"[0-9]*" s)]
    (if (empty? ss) 0
      (Integer. ss))))

(defn contents [res]
  (html/select res
               #{[:td.title :a]
                 [:td.subtext html/first-child]
                 [:td.subtext html/last-child]}))

(defn parse [c]
  (for [[title points comments] (partition 3 c)]
    {:link (:href (:attrs title))
     :title (html/text title)
     :points (parse-int (html/text points))
     :comments (parse-int (html/text comments))
     :comments-link  (:href (:attrs comments))}))
