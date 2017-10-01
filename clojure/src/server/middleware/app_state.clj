(ns server.middleware.app-state
    (:require [clojure.pprint :refer [pprint]]))

;; wrap-config is a simple ring middleware wrapper which adds the 
;; module runtime config to the request map as the key :config.
;; See server.config for config details.

;; Note the fn within a fn. This is a wrapper factory.
(defn wrap 
  [app-state]
  (println "creating app-state wrapper...")
  (fn [handler]
    (println "wrapping app-state...")
    (fn [request]
        (handler (assoc request :app-state app-state)))))