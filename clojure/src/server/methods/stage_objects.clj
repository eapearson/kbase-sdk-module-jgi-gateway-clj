(ns server.methods.stage-objects
  (:require [clojure.data.json :as json])
  (:require [clojure.string :as string])
  (:require [clojure.java.io :as io])
  (:require [server.config :as config])
  (:require [org.httpkit.client :as http]))

(defn api-call 
  [ids user-id]
  (println "hmm")
  (pr (config/get-config :call-timeout)) (println "")
  (pr (config/get-config :jgi-username)) (println "")
  (pr (config/get-config :jgi-password)) (println "")
  (pr (config/get-config :jgi-host)) (println "")
  (println "yeah")
  (let [data  (json/write-str {"ids" (string/join "," ids)
                               "path" (string/join "/" ["" "data" user-id])})
        options {:timeout (config/get-config :call-timeout)
                 :basic-auth [(config/get-config :jgi-username) (config/get-config :jgi-password)]
                 :headers {"Content-Type" "application/json"}
                 :body data
                 :as :stream}
        url (str (config/get-config :jgi-host) "/fetch")
        start (inst-ms (java.time.Instant/now))
        {:keys [status headers body error] :as resp} @(http/post url options)]
    ;; TODO: use slinghsot
    (let [elapsed (- (inst-ms (java.time.Instant/now)) start)]
      (if error (throw (Exception. "Error running call"))
        [(json/read (io/reader body)) elapsed]))))

;; call implements the method call.
;; (call json-body context)
;; Note that the arguments are destructured for the defined parameter structure.
;; For more complex input objects this may not be feasible or possible.
(defn call 
  [[{:keys [ids]}] {:keys [user-id]}]
  (let [[result elapsed] (api-call ids user-id)]
    result))
