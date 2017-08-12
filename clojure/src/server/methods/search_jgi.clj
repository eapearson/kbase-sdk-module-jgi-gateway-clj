(ns server.methods.search-jgi
  (:require [clojure.data.json :as json])
  (:require [clojure.string :as string])
  (:require [clojure.java.io :as io])
  (:require [server.config :as config])
  (:require [org.httpkit.client :as http]))

(defn api-call 
  [search page limit]
  (let [data  (json/write-str {"query" search
                               "page" page
                               "size" limit})
        options {:timeout (config/get-config :call-timeout)
                 :basic-auth [(config/get-config :jgi-username) (config/get-config :jgi-password)]
                 :headers {"Content-Type" "application/json"}
                 :body data
                 :as :stream}
        url (str (config/get-config :jgi-host) "/query")
        start (inst-ms (java.time.Instant/now))
        {:keys [status headers body error] :as resp} @(http/post url options)]
    ;; TODO: use slinghsot
    (let [elapsed (- (inst-ms (java.time.Instant/now)) start)]
      (if error (throw (Exception. "Error running query"))
        [(json/read (io/reader body)) elapsed]))))

;; call implements the method call.
;; Note that the arguments are destructured for the defined parameter structure.
;; For more complex input objects this may not be feasible or possible.
(defn call 
  [[{:keys [search_string page limit]}] _]
  (let [url (config/get-config :jgi-host)]
    (let [[result elapsed] (api-call search_string page limit)]
  {"search_result" result
   "search_elapsed_time" elapsed})))
