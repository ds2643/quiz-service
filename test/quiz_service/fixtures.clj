(ns quiz-service.fixtures
  (:require
   [quiz-service.migration :as m]
   [quiz-service.server :as s]
   [clojure.test :as t])
  (:import
   (java.net ServerSocket)))

(defn- find-free-local-port []
  (let [socket (ServerSocket. 0)]
    (let [port (.getLocalPort socket)]
      (.close socket)
      port)))

(def ^:dynamic *local-port* nil)

(defn create-db-context [f]
  (m/rollback)
  (m/migrate)
  (f)
  (m/rollback))

(defn create-server-context [f]
  (binding [*local-port* (find-free-local-port)]
    (let [test-server (s/run-server *local-port*)]
      (f)
      (.stop test-server))))

(def test-user-config {:firstname "Creed"
                       :lastname  "Bratton"
                       :email     "cbratton@creedthoughts.gov"})

