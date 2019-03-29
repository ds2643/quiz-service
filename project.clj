(defproject quiz-service "0.1.0"
  :description "REST server supporting Correlation-One quiz service."
  :dependencies
  [[cheshire "5.8.1"]
   [clj-http "3.9.1"]
   [clj-time "0.15.0"]
   [compojure "1.6.1"]
   [honeysql "0.9.4"]
   [metosin/compojure-api "2.0.0-alpha29"]
   [mysql/mysql-connector-java "5.1.6"]
   [org.clojure/clojure "1.8.0"]
   [org.clojure/java.jdbc "0.7.9"]
   [prismatic/schema "1.1.10"]
   [ragtime "0.8.0"]
   [re-rand "0.1.0"]
   [ring/ring-jetty-adapter "1.4.0"]
   [ring/ring-json "0.4.0"]]
  :aliases
  {"migrate"  ["run" "-m" "quiz-service.migration/migrate"]
   "rollback" ["run" "-m" "quiz-service.migration/rollback"]}
  :main quiz-service.main
  :profiles
  {:dev
   {:plugins
    [[jonase/eastwood "0.3.5"]]}})
