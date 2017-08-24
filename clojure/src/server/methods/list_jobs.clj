(ns server.methods.list-jobs
    (:require [server.state :as state]))

(defn api-call
    [user-id]
    ;; just literally get the jobs and return them.
    ;; They are already in a json friendly format, a
    ;; clojure map.
    ;; They are stored in a namespace global (server.state).
    (let [start (inst-ms (java.time.Instant/now))
          jobs (state/get-jobs)]
      (let [elapsed (- (inst-ms (java.time.Instant/now)) start)]
        [jobs elapsed])))

(defn call
    [_ {:keys [user-id]} config]
  (let [[result elapsed] (api-call user-id)]
    [result
     {"request_elapsed_time" elapsed}]))