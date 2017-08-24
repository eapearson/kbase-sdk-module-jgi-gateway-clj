(ns server.middleware.config
    (:require [server.config :as config])
    (:require [clojure.pprint :refer [pprint]]))

;; wrap-config is a simple ring middleware wrapper which adds the 
;; module runtime config to the request map as the key :config.
;; See server.config for config details.

(def config (atom nil))

(defn load-config
    []
    (reset! config (config/load-config)))

(defn wrap-config 
  [handler]
  (println "wrapping config...")
  (let [config (config/load-config)]
    (fn [request]
        (when (nil? config) (throw (Exception. "Config not defined -- call load-config before wrap-config")))
        (handler (assoc request :config config)))))