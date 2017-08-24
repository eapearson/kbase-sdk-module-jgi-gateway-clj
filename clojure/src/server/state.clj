(ns server.state
    (:require [clojure.core.async :refer [<! go-loop timeout chan]])
    (:require [server.api.jgi-gateway :as jgi-gateway]))

(def jobs (atom {}))

(defn get-job
    [key]
    (@jobs key))

(defn add-job
    [key state]
    (swap! jobs assoc key state))
    ; (start-monitoring-jobs))

(defn update-job
    [key state]
    (swap! jobs assoc key state))

(defn remove-job
    [key]
    (swap! jobs dissoc key))

(defn get-jobs
    []
    @jobs)

;; receives timeout events
(def job-tickler (chan))

(defn get-job-status
    [id config]
    (let [[result _] (jgi-gateway/status id config)]
        result))

(defn update-jobs
    [config]
    (let [update-count (atom)]
      (doseq [[id status] @jobs]
        (when (not (= status "completed"))
          (swap! update-count + 1)
          (let [job-status (get-job-status id config)]
            (swap! jobs assoc key job-status))))
      update-count))
    

(defn start-monitoring-jobs
    [config]
    ;; set up 
    (go-loop []
        (<! (timeout 1000))
        (when (update-jobs config)
            (recur))))
            
(defn stop-monitoring-jobs
    []
)