(ns quiz-service.db
  "Utilities for interacting with MySQL db"
  (:require
   [clojure.java.jdbc :as jdbc]
   [re-rand :refer [re-rand]]))

(def test-db
  (let [host (or (System/getenv "DB_HOST") "localhost")
        port (or (System/getenv "DB_PORT") "3006")]
    {:dbtype      "mysql"
     :classname   "com.mysql.jdbc.Driver"
     :subprotocol "mysql"
     :subname     (format "//%s:%s/db" host port)
     :dbname      "db"
     :user        "user"
     :password    "password"}))

(defn create-random-id [table db-connection]
  {:post [(string? %)]}
  (let [existing-ids
        (->> [(format "select id from %s" (name table))]
             (jdbc/query test-db) (map :id) set)
        find-unique-id
        (partial some #(when-not (contains? existing-ids %) %))
        generate-id #(re-rand #"\w{5}\-\w{5}-\w{5}")]
    (find-unique-id (repeatedly generate-id))))
