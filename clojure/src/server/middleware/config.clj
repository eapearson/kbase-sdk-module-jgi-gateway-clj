(ns server.middleware.config
    (:require [server.config :as config]))

;; wrap-config is a simple ring middleware wrapper which adds the 
;; module runtime config to the request map as the key :config.
;; See server.config for config details.
(defn wrap-config 
  [handler]
  (let [config (config/load-config)]
    (fn
      ([request]
        (handler (assoc request :config config))))))