(ns server.jobs
    (:require [clojure.core.async :refer [go <! >! go-loop timeout chan pipe]])
    (:require [server.api.jgi-gateway :as jgi-gateway])
    (:require [clojure.core.match :refer [match]]))

;; Jobs Manager
;; Has three basic tasks:
;; 1. api for managing a set of jobs, basic crud operations
;; 2. job status monitoring - maintain the set of jobs based on 
;;    a job query api (i.e. network endpoint)
;; 3. provide snapshot of the job state

;; This supports the following use cases:
;; A client can add or remove a job for monitoring
;; A client can receive the state of all jobs
;; All jobs are kept up-to-date with the canonical source of truth
;; for job status.

;; Jobs creator
;; It is assumed that the jobs may be modified from different threads,
;; since jobs may be the results, e.g., of a json-rpc call.
(defn make-jobs
    []
    (atom {}))

;; Job operations
(defn get-job
    [jobs key]
    (get @jobs key))

(defn add-job
    [jobs key state]
    (swap! jobs assoc key state))
    ; (start-monitoring-jobs))

(defn update-job
    [jobs key state]
    (swap! jobs assoc key state))

(defn remove-job
    [jobs key]
    (swap! jobs dissoc key))

(defn get-jobs
    [jobs]
    @jobs)

;; receives timeout events
; (def job-tickler (chan))

; (defn get-jobs-status
;     [jobs id config]
;     (let [[result _] (jgi-gateway/status id config)]
;         result))

;; For each job we know about which is not in a completed 
;; state, query it's current status. We utilize a deref'ed
;; job table which we update and then restore into the global
;; atom.

(defmacro cond-let
  [binding-test expr & rest]
  (if (= binding-test :else) expr
    (when-let [[binding test] binding-test]
      `(if-let [~binding ~test]
        ~expr
        (cond-let ~@rest)))))

;   (let [binding (first bindings)]
;     (when-let [[test expr & more] clauses]
;       (if (= test :else)
;         expr
;         `(if-let [~binding ~test]
;            ~expr
;            (cond-let ~bindings ~@more))))))

;; parse-statys-response
;; For a given string, attempt to parse a sensible job status 
;; out of it. The problem is that the jgi-gateway presently
;; (it will surely be fixed at some point soonish) response with
;; a string containing the embedded information.
(def completed-re #"^\"Transfer Complete\. Transfered ([\d]+) files\.\"$")
(def progress-re #"^\"In Progress\. Total files = ([\d]+)\. Copy complete = ([\d]+)\. Restore in progress = ([\d]+)\. Copy in progress = ([\d]+)\"$")

(defn parse-status-message
    [msg]
    (cond-let
      [_  (= msg "\"In Queue\"")] "queued"
      [_  (= msg "\"Error: No such Id\"")] "notfound"
      [[_ count] (re-matches completed-re msg)] "completed"
      [[_ total completed restoring copying] (re-matches progress-re msg)]
        ;; TODO convert to and test the count?
        (cond
          (= completed "1") "completed"
          (= restoring "1") "restoring"
          (= copying "1") "copying"
          :else "unknown2")
      :else "unknown1"))

; (defn parse-status-response
;     [status-response]
;     (case status-response
;         "\"In Queue\"" "queued"
;         "\"Error: No such Id\"" "notfound"
;         (let [completed-re #"^\"Transfer Complete\. Transfered ([\d]+) files\.\"$"]
;             (if-let [[_ count] (re-matches completed-re status-response)]
;             "completed"
;             ;; Note no terminal . in the regular expression.
;             (let [progress-re #"^\"In Progress\. Total files = ([\d]+)\. Copy complete = ([\d]+)\. Restore in progress = ([\d]+)\. Copy in progress = ([\d]+)\"$"]
;                 (if-let [[_ total completed restoring copying] (re-matches progress-re status-response)]
;                     (cond
;                     (= completed "1") "completed"
;                     (= restoring "1") "restoring"
;                     (= copying "1") "copying"
;                     :else "unknown2")
;                     "unknown1"))))))                    



;; <sync-job
;; Given a jobs object, a job id, and configuration, will fetch the
;; job info from the jgi-gateway, update the jobs object with it,
;; and return that status in a channel.
;; 
;; Sync the given job by fetching the current state from the jgi gateway
;; and setting the current job entry in the jobs table to this new value.
;; Returns a channel with the new value as well, which should be used to
;; synchronize one or more job sync requests.
(defn <sync-job
    [jobs job-id config]
    (let [out (chan)]
      (go
        (match (<! (jgi-gateway/<status job-id config))
          [:ok status _] 
            (let [status-code (parse-status-message status)]
              (update-job jobs job-id {:message status :status status-code})
              (>! out [:ok status-code]))
          [:er message ex _] 
            ;; TODO: log the error
            (>! out [:er "ERROR"])))
      out))

    ;     (let [[status _] (<! (jgi-gateway/<status job-id config))
    ;           status-code (parse-status-response status)]
    ;       (update-job jobs job-id {:message status :status status-code})
    ;       (>! out status)))
    ;   out))

;; <sync-all-jobs
;; Performs a job sync, as described above, for all jobs in the jobs object
;; not in a "completed" state.
;;
;; It collects all response into a channel, which is returned.
(defn <sync-all-jobs
    [jobs config]
    (let [jobs-to-update (reduce (fn [j [id state]]
                                   (if (not (= (:status state) "completed"))
                                     (conj j id) j)) #{} @jobs)
         ;; out (chan (count jobs-to-update))]
         out (chan)
         jobs-chan (chan)]
      (go
        ;; Queue up all the sync jobs.
        (doseq [job-id jobs-to-update]
            (pipe (<sync-job jobs job-id config) jobs-chan false))
        ;; Ensure they all complete before we continue with 
        ;; final success message.
        ;; Throw a timeout into the mix to issue a slow message
        ;; into the log, but keep going.
        (for [iter (range 1 (count jobs-to-update))]
          (match (<! jobs-chan)
            [:ok _] (println "ok!")
            [:er _] (println ":(")))
        ;; Throw a bone.
        (>! out [:ok]))
    out))

(def job-loop (chan))
(def job-control (chan))

(defn reconfigure-jobs-monitor
  [new-config]
  (let [{interval :interval} new-config]
    (when interval
      (>! jobs-monitoring [:new-interval interval]))))

(defn start-monitoring-jobs
  [interval]
  (go (>! jobs-monitoring [:restart interval])))

(defn stop-monitoring-jobs
  []
  (go (>! jobs-monitoring [:exit])))
    

;; monitor-jobs
;; Establishes two loops. One loops infinitely on the job loop control
;; channel and for each turn of the loop will sync the job state. 
;; Another channel loop listens for job control commands and which are used to 
;; either reconfigure the job loop interval or to tell the main job 
;; loop to run once more.
(defn monitor-jobs
  [jobs interval config]
    (go-loop [iters 0]
       (match (<! job-loop)
         :exit (println "exiting the main job loop")
         :continue (match (<! (<sync-all-jobs jobs config))
                        ;; TODO should have returned :continue, to keep going
                        ;; :restart interfal to coninue but with a different poll interval
                        ;; :exit stop monitoring.
                        :ok (do (println (str "got OK"))
                                (>! job-control :continue)
                                (recur (inc iters)))
                        :exit (do (println "ok, exiting")
                                  (>! job-control :exit)
                                  (recur)))))

    (go-loop [iters 0
              polling-interval interval]
      (println (str "job control loop " iters))
      ;; Controls the job monitoring loop, including 
      ;; pause, continue, exit, restart
      (match (<! job-control)
        :start (do (>! job-loop [:continue])
                     (recur (inc iters) interval))
        :pause (recur (inc iters) interval) ;; simply wait on the channel; note, someone will need to 
                         ;; send another message on this channel!
        :continue (do (println (str "continuing in " interval))
                        (<! (timeout interval))
                        (>! job-loop [:continue])
                        (recur (inc iters) interval))
        :exit (do (println "ok, exiting")
                    (>! job-loop :exit))
        [:restart new-interval] (do (println (str "okay, restarting with " new-interval))
                                    (<! (timeout new-interval))
                                    (>! job-control [:continue])
                                    (recur (inc iters) new-interval)))))

     
        

; (defn update-jobs
;     [config](.)
;     (let [update-count (atom)]
;       (seq [[id status] jobs]
;         (when (not (= status "completed"))
;           (swap! update-count + 1)
;           (let [job-status (get-job-status id config)]
;             (update-job jobs key job-status))))
;       update-count))

; (defn start-monitoring-jobs
;     [config]
;     ;; set up 
;     (go-loop []
;         (<! (timeout 1000))
;         (when (update-jobs config)
;             (recur))))
            
; (defn stop-monitoring-jobs
;     []
; )