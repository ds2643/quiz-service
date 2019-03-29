(ns quiz-service.server
  "Implements routing defining server API."
  (:require
   [compojure.core :refer [defroutes POST GET PUT]]
   [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
   [compojure.route :as route]
   [ring.adapter.jetty :as ring]
   [clojure.string :as str]
   [quiz-service.db :as db]
   [quiz-service.session :as session]))

(defroutes routes
  (POST "/create-session" req
        (let [user-config (get-in req [:body :user-config])
              new-session
              (session/create-new-session user-config db/test-db)]
          {:body {:session-id new-session}}))

  (GET "/answer-ids" req
       (let [session-id (get-in req [:body :session-id])
             answer-ids (session/get-answer-ids session-id db/test-db)]
         {:body {:answers answer-ids}}))

  (GET "/get-question" req
       (let [answer-id (get-in req [:body :answer-id])
             question-content
             (session/get-question-content answer-id db/test-db)]
         {:body {:content question-content}}))

  (PUT "/submit-answer" req
       (let [submission   (get-in req [:body :submission])
             confirmation (session/submit-answer submission db/test-db)]
         {:body {:status confirmation}}))

  (POST "/confirm-submission" req
        (let [session-id (get-in req [:body :session-id])
              confirmation
              (session/mark-session-as-complete session-id db/test-db)]
          {:body {:status confirmation}}))

  ;; TODO: Consider adding a route for assessment status.

  (route/not-found "Invalid route"))

(defn run-server
  "Run an instance of the quiz server on the local machine
  at the port indicated in the argument."
  [port]
  (let [config {:port  port :join? false}
        app (-> routes
                (wrap-json-body {:keywords? true})
                wrap-json-response)]
    (ring/run-jetty app config)))
