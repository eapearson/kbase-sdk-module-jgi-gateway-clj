(ns server.methods.stage-status
  (:require [server.api.jgi-gateway :as jgi-gateway]))

;; call implements the method call.
;; (call json-body context)
;; Note that the arguments are destructured for the defined parameter structure.
;; For more complex input objects this may not be feasible or possible.
(defn call
  [[{:keys [job_id]}] _ config]
  (let [[result err elapsed] (jgi-gateway/status job_id config)]
    [result err {:request_elapsed_time elapsed}]))
