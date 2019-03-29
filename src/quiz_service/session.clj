(ns quiz-service.session
  "Interactions relating to maintainance of session information."
  (:require
   [honeysql.core :as sql]
   [honeysql.helpers :as helpers]
   [clj-time.local :as local]
   [clojure.java.jdbc :as jdbc]
   [clj-time.jdbc]
   [clj-time.core :as t]
   [quiz-service.db :as db]))

(defn get-time-allowed [ruleset db-connection]
  (let [query (sql/format {:select [:minutesallowed]
                           :from   [:rules]
                           :where  [:= :ruleset (name ruleset)]})]
    (->> (jdbc/query db-connection query)
         (map :minutesallowed)
         first t/minutes)))

(defn get-answer-ids
  "Finds the list of required answers for the supplied session-id.

  Throws exception indicating no session exists if query returns
  empty result."
  [session-id db-connection]
  (let [query (sql/format {:select [:id]
                           :from   [:answers]
                           :where  [:= :sid session-id]})]
    (if-let [ids (seq (jdbc/query db-connection query))]
      (mapv :id (jdbc/query db-connection query))
      (throw (Exception. (format "No such session: %s" session-id))))))

(defn get-question-content
  [id db-connection]
  (let [qid-query
        (sql/format {:select [:qid]
                     :from   [:answers]
                     :where  [:= :id id]})
        qid (-> db-connection
                (jdbc/query qid-query)
                first :qid)
        content-query
        (sql/format
         {:select [:content]
          :from   [:questions]
          :where  [:= :id qid]})]
    (if-let [result (-> db-connection
                        (jdbc/query content-query)
                        first :content)]
      result
      (throw (Exception.
              (format "Failed to fetch content associated with %s" id))))))

(defn get-session-starttime [session-id db-connection]
  (let [query        (sql/format {:select [:starttime]
                                  :from   [:sessions]
                                  :where  [:= :id session-id]})]
    ((comp :starttime first) (jdbc/query db-connection query))))

;; TODO: Add date to times! The `clj-time` solution doesn't
;;       seem to include dates in the timestamp. Therefore,
;;       comparisons of times (e.g., when `time-expired?` is
;;       run) doesn't take days being different into account,
;;       rendering this predicate incorrect.
(defn time-expired? [session-id db-connection]
  (let [time-allowed (get-time-allowed :default db-connection)]
    (t/after? (t/plus (get-session-starttime session-id db-connection)
                      time-allowed)
              (local/local-now))))

(defn session-complete? [session-id db-connection]
  (let [query        (sql/format {:select [:iscomplete]
                                  :from   [:sessions]
                                  :where  [:= :id session-id]})
        query-result (jdbc/query db-connection query)]
    (if-not (empty? query-result)
      (let [marked-as-complete ((comp :iscomplete first) query-result)
            out-of-time?       (time-expired? session-id db-connection)]
        (or marked-as-complete out-of-time?))
      :no-such-session)))

;; TODO: Consider making submission of session-id unnecessary.
;;       Such information could be inferred from the response id.
(defn submit-answer
  [{:keys [response id session-id]
    :as   answer} db-connection]
  (let [statement (-> (helpers/update :answers)
                      (helpers/sset {:response response})
                      (helpers/where [:= :id id])
                      sql/format)
        expired?  (session-complete? session-id db-connection)
        [result]  (jdbc/execute! db-connection statement)]
    (when-not (integer? response)
      (throw (Exception. "Invalid response type: Expected integer for response")))
    (cond
      expired?     (throw (Exception. (format "Session %s expired" session-id)))
      (= result 1) :ok
      :else        (throw (Exception. (format "No such answer id %s" id))))))

(defn mark-session-as-complete
  [session-id db-connection]
  (let [statement (-> (helpers/update :sessions)
                      (helpers/sset {:iscomplete true
                                     :endtime    (local/local-now)})
                      (helpers/where [:= :id session-id])
                      sql/format)
        [result]    (jdbc/execute! db-connection statement)]
    (if (= result 1)
      :ok
      (throw (Exception. (format "No such session: %s"
                                 session-id))))))

(defn get-question-ids
  "Returns collection of ids associated with all existing questions."
  [db-connection]
  (let [query (sql/format {:select [:id]
                           :from   [:questions]})]
    (map :id (jdbc/query db-connection query))))

(defn init-answers
  "Populates answer table with a set of required questions associated
  with session-id.

  Returns count of required questions."
  [session-id db-connection]
  (let [qids  (get-question-ids db-connection)
        table :answers
        make-question-row
        (fn [qid] {:sid session-id
                   :qid qid
                   :id  (db/create-random-id table db-connection)})
        statement (-> (helpers/insert-into table)
                      (helpers/values (mapv make-question-row qids))
                      sql/format)]
    (jdbc/execute! db-connection statement)
    (count qids)))

(defn create-new-session
  "Create session associated with user information supplied in user-config.
  Returns associated session-id.

  Assumes successful schema migration."
  [{:keys [firstname lastname email] :as user-config}
   db-connection]
  (when-not (and firstname lastname email)
    (throw (Exception. "Incomplete user information supplied")))

  (let [table          :sessions
        new-session-id (db/create-random-id table db-connection)
        statement      (-> (helpers/insert-into table)
                           (helpers/values [(merge user-config
                                                   {:id         new-session-id
                                                    :starttime  (local/local-now)
                                                    :iscomplete false})])
                           sql/format)]
    (jdbc/execute! db-connection statement)
    (init-answers new-session-id db-connection)
    new-session-id))
