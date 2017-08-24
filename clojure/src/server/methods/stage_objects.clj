(ns server.methods.stage-objects
  (:require [server.state :as state])
  (:require [server.api.jgi-gateway :as jgi-gateway]))

;; call implements the method call.
;; (call json-body context)
;; Note that the arguments are destructured for the defined parameter structure.
;; For more complex input objects this may not be feasible or possible.
(defn call 
  [[{:keys [ids]}] {:keys [user-id]} config]
  (let [[result elapsed] (jgi-gateway/fetch ids user-id config)
        job-id (get result "id")]
    (state/add-job job-id nil)
    [{"job_id" job-id}
     {"request_elapsed_time" elapsed}]))
