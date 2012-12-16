(ns hn-analyse.core)

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
