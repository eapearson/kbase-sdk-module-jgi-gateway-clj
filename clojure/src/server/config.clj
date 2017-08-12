(ns server.config
  (:require [clojure-ini.core :as ini])
  (:require [clojure.string :as string]))

(defn kbd-required
  []
  (throw (ex-info "The KB_DEPLOYMENT_CONFIG environment variable was not found"
                  {:explanation "This environment variable is required in order to bootstrap the service by pointing to the configuration file."})))

(defn kbs-required
  []
  (throw (ex-info "The KB_SERVICE_NAME environment variable was not found"
                  {:explanation "This environment variable is required in order to provide the name this service is known by in configuration files as well as to the service wizard. This is not necessarily the same as the rpc module name which is hard coded in the application and also must be known by clients."})))

(defn svcc-required
  [service-name config-file]
  (throw (ex-info "A stanza for this service was not found in the configuration file"
                  {:explanation ""
                   :service-name service-name
                   :config-file config-file})))

;; SHOEHORN env vars in here for now...
(defn load-config
  []
  (let [config-file (or (System/getenv "KB_DEPLOYMENT_CONFIG") (kbd-required))
        service-name (keyword (or (System/getenv "KB_SERVICE_NAME") (kbs-required)))
        raw-config (ini/read-ini config-file :keywordize? true :comment-char \#)
        service-config (or (get raw-config service-name) (svcc-required service-name config-file))
        authKey "auth-service-url"
        jgi-token (get service-config :jgi-token)
        [username password] (string/split jgi-token #":")]

    ;; Just the configs we are interested in, coerced to proper values.
    ;; TODO: wtf, why not use a modern config format, for gods' sake?
    ;; TODO: with the new deploy main function, we can create an edn file
    ;; and simply slurp it up here.
    {:auth2-service-url (:auth2-service-url service-config)
     :service-name service-name
     :jgi-username username
     :jgi-password password
     :jgi-host (:jgi-host service-config)
     :call-timeout (Integer/parseInt (:call-timeout service-config))
     :allow-insecure (= (:allow-insecure service-config) "true")
     :auth-service-url-allow-insecure (= (:allow-insecure service-config) "true")}))

(defn get-config
    [config config-key]
    (get config config-key))
