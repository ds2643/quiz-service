(ns quiz-service.server-test
  (:require
   [quiz-service.server :refer :all]
   [quiz-service.fixtures :refer :all]
   [clj-http.client :as client]
   [cheshire.core :as json]
   [clojure.test :refer :all]))

(use-fixtures :once (compose-fixtures create-server-context
                                      create-db-context))

(defn url-builder [path]
  (format "http://localhost:%s/%s" *local-port* path))

(defn create-test-session []
  (let [url     (url-builder "create-session")
        my-body {:user-config test-user-config}
        req     {:content-type  :application/json
                 :body          (json/generate-string my-body)
                 :body-encoding "UTF-8"
                 :accept        :json}]
    (-> url
        (client/post req)
        :body
        (json/parse-string true)
        :session-id)))

(defn get-answer-ids [session-id]
  (let [url     (url-builder "answer-ids")
        my-body {:session-id session-id}
        req     {:content-type  :application/json
                 :body          (json/generate-string my-body)
                 :body-encoding "UTF-8"
                 :accept        :json}]
    (-> url
        (client/get req)
        :body
        (json/parse-string true)
        :answers)))

(defn get-question-content [answer-id]
  (let [url     (url-builder "get-question")
        my-body {:answer-id answer-id}
        req     {:content-type  :application/json
                 :body          (json/generate-string my-body)
                 :body-encoding "UTF-8"
                 :accept        :json}]
    (-> url
        (client/get req)
        :body
        (json/parse-string true)
        :content)))

(defn submit-answer [submission]
  (let [url     (url-builder "submit-answer")
        my-body {:submission submission}
        req     {:content-type  :application/json
                 :body          (json/generate-string my-body)
                 :body-encoding "UTF-8"
                 :accept        :json}]
    (-> url
        (client/put req)
        :body
        (json/parse-string true)
        :status)))

(defn confirm-submission [session-id]
  (let [url     (url-builder "confirm-submission")
        my-body {:session-id session-id}
        req     {:content-type  :application/json
                 :body          (json/generate-string my-body)
                 :body-encoding "UTF-8"
                 :accept        :json}]
    (-> url
        (client/post req)
        :body
        (json/parse-string true)
        :status)))

(deftest simulate-user-exchange-through-server-interface
  (let [session-id (create-test-session)
        answer-ids (get-answer-ids session-id)]

    (testing "Create session through /create-session"
      (is (some? session-id)))

    (testing "Generating session adds associated responses"
      (is (seq answer-ids)))

    (testing "Get question associated with answer id via /get-questions"
      (is (string? (get-question-content (first answer-ids)))))

    (testing "Get question associated with answer id via /get-questions"
      (let [submission {:response   (rand-int 10)
                        :session-id session-id
                        :id         (first answer-ids)}]
        (is (= "ok" (submit-answer submission)))))

    (testing "End session with /confirm-submission"
      (is (= "ok" (confirm-submission session-id))))))
