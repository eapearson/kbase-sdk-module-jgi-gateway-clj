(ns server.api.jgi-gateway
  (:require [clojure.data.json :as json])
  (:require [clojure.string :as string])
  (:require [clojure.java.io :as io])
  (:require [clojure.core.async :as async])
  (:require [org.httpkit.client :as http]))

;; Note that the body is just read as a string for now, since the 
;; status is returned as a string sentence.
;; We want this to be returned as json with structure!
(defn status
  [id config]
  (let [options {:timeout (:call-timeout config)
                 :basic-auth [(:jgi-username config) (:jgi-password config)]
                 ;; :headers {"Content-Type" "application/json"}
                 :query-params {:id id}
                 :as :text}
        url (str (:jgi-host config) "/status")
        start (inst-ms (java.time.Instant/now))
        {:keys [status headers body error] :as resp} @(http/get url options)]
    ;; TODO: use slinghsot
    (let [elapsed (- (inst-ms (java.time.Instant/now)) start)]
      (if error (throw (Exception. "Error running call"))
        [:message body elapsed]))))

;; The async version
(defn <status
  [id config]
  (let [options {:timeout (:call-timeout config)
                 :basic-auth [(:jgi-username config) (:jgi-password config)]
                 ;; :headers {"Content-Type" "application/json"}
                 :query-params {:id id}
                 :as :text}
        url (str (:jgi-host config) "/status")
        start (inst-ms (java.time.Instant/now))
        out (async/chan)]

    (http/get url options (fn [{:keys [status headers body error]}]
                            (let [elapsed (- (inst-ms (java.time.Instant/now)) start)]
                              (if error (throw (Exception. "Error running call"))
                                  (async/go (async/>! out [:ok body elapsed])))))
                          (fn [exception]
                            (let [elapsed (- (inst-ms (java.time.Instant/now)) start)]
                              (async/go (async/>! out [:er "Error getting status" exception elapsed])))))
    out))
      


(defn fetch
  [ids user-id config]
  (let [data  (json/write-str {"ids" (string/join "," ids)
                               "path" (string/join "/" ["" "data" user-id])})
        options {:timeout (:call-timeout config)
                 :basic-auth [(:jgi-username config) (:jgi-password config)]
                 :headers {"Content-Type" "application/json"}
                 :body data
                 :as :stream}
        url (str (:jgi-host config) "/fetch")
        start (inst-ms (java.time.Instant/now))
        {:keys [status headers body error] :as resp} @(http/post url options)]
    (let [elapsed (- (inst-ms (java.time.Instant/now)) start)]
      (if error (throw (Exception. "Error running call"))
        (let [response (json/read (io/reader body))]
          [response elapsed])))))

(defn query
  [search page limit config]
  ; (println "in search_jgi")
  (let [data  (json/write-str {"query" search
                               "page" page
                               "size" limit})
        options {:timeout (:call-timeout config)
                 :basic-auth [(:jgi-username config) (:jgi-password config)]
                 :headers {"Content-Type" "application/json"}
                 :body data
                 :as :stream}
        url (str (:jgi-host config) "/query")
        start (inst-ms (java.time.Instant/now))
        {:keys [status headers body error] :as resp} @(http/post url options)]
    ;; TODO: use slinghsot
    (let [elapsed (- (inst-ms (java.time.Instant/now)) start)]
      (if error (throw (Exception. "Error running query"))
        [(json/read (io/reader body)) elapsed]))))          

