(ns server.methods.stage-status
  (:require [clojure.data.json :as json])
  (:require [clojure.string :as string])
  (:require [clojure.java.io :as io])
  (:require [server.config :as config])
  (:require [org.httpkit.client :as http]))

(defn api-call 
  [id]
  (println "hmm")
  (pr (config/get-config :call-timeout)) (println "")
  (pr (config/get-config :jgi-username)) (println "")
  (pr (config/get-config :jgi-password)) (println "")
  (pr (config/get-config :jgi-host)) (println "")
  (println "yeah")
  (let [params  (json/write-str {"id" id})
        options {:timeout (config/get-config :call-timeout)
                 :basic-auth [(config/get-config :jgi-username) (config/get-config :jgi-password)]
                 ;; :headers {"Content-Type" "application/json"}
                 :query-params params
                 :as :text}
        url (str (config/get-config :jgi-host) "/status")
        start (inst-ms (java.time.Instant/now))
        {:keys [status headers body error] :as resp} @(http/get url options)]
    ;; TODO: use slinghsot
    (let [elapsed (- (inst-ms (java.time.Instant/now)) start)]
      (if error (throw (Exception. "Error running call"))
        body))))

;; call implements the method call.
;; (call json-body context)
;; Note that the arguments are destructured for the defined parameter structure.
;; For more complex input objects this may not be feasible or possible.
(defn call 
  [[{:keys [id]}] _]
  (let [[result elapsed] (api-call id)]
    result))
