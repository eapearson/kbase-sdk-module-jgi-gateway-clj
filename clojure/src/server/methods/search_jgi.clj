(ns server.methods.search-jgi
  (:require [server.api.jgi-gateway :as jgi-gateway]))

;; call implements the method call.
;; Note that the arguments are destructured for the defined parameter structure.
;; For more complex input objects this may not be feasible or possible.
(defn call 
  [[{:keys [search_string page limit]}] _ config]
  (let [[result elapsed] (jgi-gateway/query search_string page limit config)]
    [{"search_result" result}
     {"request_elapsed_time" elapsed}]))