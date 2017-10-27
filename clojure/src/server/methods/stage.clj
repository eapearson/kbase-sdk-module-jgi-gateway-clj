(ns server.methods.stage
  (:require [server.state :as state])
  (:require [server.api.jgi-gateway :as jgi-gateway]))

;; call implements the method call.
;; (call json-body context)
;; Note that the arguments are destructured for the defined parameter structure.
;; For more complex input objects this may not be feasible or possible.
(defn call
  [[{:keys [ids]}] {:keys [user-id]} config]
  (let [[result err elapsed] (jgi-gateway/fetch ids user-id config)
        stats {"request_elapsed_time" elapsed}]

    (if result
        [{"job_id" (get result "id")} nil stats]
        [nil err stats])))
