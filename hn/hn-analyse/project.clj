(defproject hn-analyse "1.0.0-SNAPSHOT"
  :description "FIXME: write description"
  :repositories {"releases" "https://github.com/karussell/mvnrepo/raw/master/releases/"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [clj-redis "0.0.12"]
                 [org.clojure/data.codec "0.1.0"]
                 [enlive "1.1.1"]
                 [de.jetwick/snacktory "1.2"]
                 [org.clojure/data.json "0.2.2"]
                 [clj-time "0.5.1"]
                 [org.clojure/tools.cli "0.2.2"]
                 [clj-rss "0.1.3"]
                 [clj-http "0.7.6"]]
   :main hn-analyse.core)
