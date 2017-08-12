(ns deploy.core
  (:gen-class)
  (:require [clojure.java.io :as io])
  (:require [clojure.string :as string])
  (:require [clojure-ini.core :as ini])
  (:require [stencil.core :as stencil])
  (:require [clojure.data.json :as json]))


;; config-from-file returns the data from a give ini file,
;; if the file exists and is an in file, otherwise nil.
;; Note that the ini file has a top level "global" section. 
;; We don't care about that, it is only there because the python
;; version of this functionality uses a library which requires the
;; file to be in ini-format, and requires that a section be present.
(defn config-from-file
  [filename]
  (if (.exists (io/as-file filename))
    (:global (ini/read-ini filename :keywordize? true :comment-char \#))))

;; add-env-variable adds a given environment variable to the configuration map
;; under a given property name. If a default value is provided and the value is not
;; found the default value is used, otherwise the original map is returned.
(defn add-env-variable
  [config variable-name property default-value]
    (if-let [value (System/getenv variable-name)]
      (assoc config property value)
      (if default-value
        (assoc config property default-value)
        config)))


;; default-config-from-env will build a configuration map from a base endpoint
;; (an origin + path) and hard-coded service paths. It also builds in auth service
;; properties -- not sure if those are still required or were just for the auth2
;; transition.
(defn config-from-env
  []
  (if-let [endpoint (System/getenv "KBASE_ENDPOINT")]
    (let [base-config {:kbase_endpoint endpoint  
                       :job_service_url (str endpoint "/userandjobstate")
                       :workspace_url (str endpoint "/ws")
                       :shock_url (str endpoint "/shock-api")
                       :handle_url (str endpoint "/handle_service")
                       :srv_wiz_url (str endpoint "/service_wizard")
                       :njsw_url (str endpoint "/njs_wrapper")}]
    (-> base-config
        (add-env-variable "AUTH_SERVICE_URL" :auth_service_url)
        (add-env-variable "AUTH_SERVICE_URL_ALLOW_INSECURE" :auth_service_url_allow_insecure "false")))))
          

;; overlay-secure-env will add any environment variables prefixed with 
;; KBASE_SECuRE_CONFIG_PARAM_ to the given config map.
;; Variable names are created by keywordizing the lowercase of the suffix
;; of the above prefix.
(defn overlay-secure-env
  [config]
  (let [secure-params (for [[key value] (seq (System/getenv))
                            [_ variable-name] (re-find #"^KBASE_SECURE_CONFIG_PARAM_(.*)$" key)
                            :when variable-name]
                          [(keyword (string/lower-case variable-name)) value])]
    (reduce (fn [cfg [k v]] (assoc cfg k v)) config secure-params)))

;; Read a template in mustache format but putatively a ini file, 
;; a data file in ini/cfg format, perform the subsitutions,
;; and output the result to the given file name.
;; compile-config <template-filename> <data-filename> <output-filename>
(defn -main
  "Process a template with a data file and output the result."
  [template-filename data-filename output-filename & args]

  (println (str template-filename ", " data-filename ", " output-filename))

  (when (not (and template-filename data-filename output-filename))
    (throw (Exception. "Invalid arguments")))

  (let [base-config (or (config-from-file data-filename)
                        (config-from-env)
                        (throw (ex-info "No configuration available"
                                {:explanation "Module configuration must be provided in either a file in ini format or in the KBASE_ENDPOINT environment variable"})))
        config (overlay-secure-env base-config)
        template (slurp template-filename)
        completed (stencil/render-string template config)]
    (spit output-filename completed)))


