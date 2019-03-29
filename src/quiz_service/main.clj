(ns quiz-service.main
  "Entry-point for running server"
  (:require
   [quiz-service.server :refer [run-server]]))

(defn -main [& args]
  (let [server-port
        (if-let [env-port (System/getenv "API_PORT")]
          (Integer/parseInt env-port)
          3666)]
    (println (format "Starting server on %s" server-port))
    (run-server server-port)))
