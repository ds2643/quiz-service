(ns quiz-service.session-test
  (:require
   [quiz-service.fixtures :refer :all]
   [clojure.test :refer :all]
   [quiz-service.db :as db]
   [quiz-service.session :refer :all]))

(use-fixtures :each create-db-context)

(deftest simulating-user-exchange
  (let [session-id (create-new-session test-user-config db/test-db)
        answer-ids (get-answer-ids session-id db/test-db)]
    (testing "Creating new session yields session-id"
      (is (string? session-id)))

    (testing "Session initially marked as incomplete"
      (is (not (session-complete? session-id db/test-db))))

    (testing "A set of responses is allocated to the user"
      (is (seq answer-ids)))

    (testing "Requesting the question associated with a response yields content"
      (is (string? (get-question-content (first answer-ids) db/test-db))))

    (testing "Confirming submission marks session as complete"
      (mark-session-as-complete session-id db/test-db)
      (is (session-complete? session-id db/test-db)))))

(deftest simulating-answer-submission
  (let [session-id (create-new-session test-user-config db/test-db)
        answer-ids (get-answer-ids session-id db/test-db)]
    (testing "Session initially marked as incomplete"
      (is (not (session-complete? session-id db/test-db))))

    ;; submit randomly generated answers
    (doseq [id answer-ids]
      (submit-answer {:response   (rand-int 10)
                      :session-id session-id
                      :id         id} db/test-db))

    (testing "Submitting all answers leaves session incomplete"
      (is (not (session-complete? session-id db/test-db))))

    (testing "Confirming submission marks session as complete"
      (mark-session-as-complete session-id db/test-db)
      (is (session-complete? session-id db/test-db)))))


;; TODO: write tests for checking time bounding behavior.
;;       e.g., if time expires, no further edits of answers
;;       should be allowed.
