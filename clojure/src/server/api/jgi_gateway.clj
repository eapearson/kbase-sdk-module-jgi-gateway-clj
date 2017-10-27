(ns server.api.jgi-gateway
  (:require [clojure.data.json :as json])
  (:require [clojure.string :as string])
  (:require [clojure.java.io :as io])
  (:require [clojure.core.async :as async])
  (:require [org.httpkit.client :as http]))


;; JGi api calls
;; All of the calls accept as a final argument a "ctx". This is a context map which includes
;; all of the information necessary to conduct the api call - host name, authentication, timeout
;; All of the api call parameter values are passed in explicitly as positional arguments.
;; All of the calls also report the time for the call as the second return value. Without this
;; this minimal code would be even shorter :) But this information is quite useful for diagnostics
;; Also note that most of the calls return the body as a stream since it can be fed straight to the json parser
;; in the case of the status call, though, a string is returned since it is never json and is just
;; returned as-is for grokking by the caller.
;; Another note -- if a non-json response is returned, an error structure is returned with the
;; body as a string. This should not be necesary, but handles the cases in which the back end
;; is misbehaving.

(defn get-content-type
    [headers]
    (let [[_ content-type charset] (re-matches #"^(.*?)(?:;(.*)){0,1}$" (:content-type headers))]
        content-type))

;; status id
;; Implements the /status jgi gateway api call.
(defn status
  [id ctx]
  (let [options {:idle-timeout (:call-timeout ctx)
                 :connect-timeout (:connect-timeout ctx)
                 :basic-auth [(:jgi-username ctx) (:jgi-password ctx)]
                 ;; :headers {"Content-Type" "application/json"}
                 :query-params {:id id}
                 :as :stream}
        url (str (:jgi-host ctx) "/status")
        start (inst-ms (java.time.Instant/now))
        {:keys [status headers body error] :as resp} @(http/get url options)]
    (let [content-type (get-content-type headers)
          elapsed (- (inst-ms (java.time.Instant/now)) start)]
      (if error ;; (throw (Exception. "Error running call"))
        [nil {:message (.getMessage error) :code "exception" :info {:status status}} elapsed]
        [(json/read (io/reader body)) nil elapsed]))))


;; The async version
(defn <status
  [id ctx]
  (let [options {:idle-timeout (:call-timeout ctx)
                 :connect-timeout (:connect-timeout ctx)
                 :basic-auth [(:jgi-username ctx) (:jgi-password ctx)]
                 ;; :headers {"Content-Type" "application/json"}
                 :query-params {:id id}
                 :as :text}
        url (str (:jgi-host ctx) "/status")
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
  [ids user-id ctx]
  (let [data  (json/write-str {"ids" (string/join "," ids)
                               "path" (string/join "/" ["" "data" user-id])})
        options {:idle-timeout (:call-timeout ctx)
                 :connect-timeout (:connect-timeout ctx)
                 :basic-auth [(:jgi-username ctx) (:jgi-password ctx)]
                 :headers {"Content-Type" "application/json"}
                 :body data
                 :as :stream}
        url (str (:jgi-host ctx) "/fetch")
        start (inst-ms (java.time.Instant/now))
        {:keys [status headers body error] :as resp} @(http/post url options)]
    (let [content-type (get-content-type headers)
          elapsed (- (inst-ms (java.time.Instant/now)) start)]
       (if error ;; (throw (Exception. "Error running query"))
        [nil {:message (.getMessage error) :code "exception" :info {:status status}} elapsed]
        (if (= content-type "application/json")
          [(json/read (io/reader body)) nil elapsed]
          [nil {:message (slurp (io/reader body)) :code "not-json" :info {:status status}} elapsed])))))

(defn query
  [search filter fields username page limit ctx]
  (println "in search:" search filter username )
  (let [start (inst-ms (java.time.Instant/now))
        query {"query" search
                "page" page
                "size" limit}
        ;; TODO: building up our query, just be a better way... i think threading
        ;;       macro + anon funcs would be do the trick.
        q2 (if filter (assoc query "filter" filter) query)
        q3 (if username (assoc q2 "userid" username) q2)
        q4 (if fields (assoc q3 "fields" fields) q3)
        data  (json/write-str q4)
        options {:idle-timeout (:call-timeout ctx)
                 :connect-timeout (:connect-timeout ctx)
                 :basic-auth [(:jgi-username ctx) (:jgi-password ctx)]
                 :headers {"Content-Type" "application/json"
                           "Accept" "application/json"}
                 :body data
                 :as :stream}
        url (str (:jgi-host ctx) "/query")
        start-req (inst-ms (java.time.Instant/now))
        pre-elapsed (- start-req start)
        {:keys [status headers body error] :as resp} @(http/post url options)]
      (println "sent> " data headers)
    ;   (println "recvd< " status body error)
    ; (def ^{:doc "Pattern for pulling the charset out of the content-type header"
    ;        :added "1.6"}
    ;   re-charset
    ;   (re-pattern (str ";(?:.*\\s)?(?i:charset)=(" re-value ")\\s*(?:;|$)")))

      (let [content-type (get-content-type headers)
            done-req (inst-ms (java.time.Instant/now))
            req-elapsed (- done-req start-req)]
        (if error ;; (throw (Exception. "Error running query"))
          (try
            (throw error)
            (catch java.net.UnknownHostException ex
                [nil {:message "the configured jgi host could not be found"
                      :type "network"
                      :code "unknown-host"
                      :info {:message (.getMessage error)
                            :exception (Throwable->map error)}} [pre-elapsed req-elapsed nil]])
            (catch java.net.SocketTimeoutException ex
                [nil {:message (.getMessage error)
                      :type "network"
                      :code "timeout"
                      :info {:message (.getMessage error)
                            :exception (Throwable->map error)}} [pre-elapsed req-elapsed nil]])
            (catch Exception ex
                [nil {:message (.getMessage error)
                      :type "exception"
                      :code "unknown"
                      :info {:exception (Throwable->map error)}} [pre-elapsed req-elapsed nil]]))
          (case status
            200 (case content-type
                  ;; only valid response
                  "application/json" [(json/read (io/reader body)) nil [pre-elapsed req-elapsed nil]]
                  ;; success response, but not json??
                  ("text/html" "text/plain")
                    [nil {:message "received a non-json text response"
                          :type "result"
                          :code "non-json-result"
                          :info {:body (slurp (io/reader body))}} [pre-elapsed req-elapsed nil]]
                  ;; not even a valid text conteXnt type
                  [nil {:message "received a non-json text response"
                          :type "result"
                          :code "unsupported-result-type"
                          :info {:content-type content-type
                                 :body (slurp (io/reader body))}} [pre-elapsed req-elapsed nil]])
            500 (case content-type
                  ;; only valid response
                  "application/json" [nil {:message "the jgi search service experienced an internal error"
                                           :type "upstream-service"
                                           :code "internal-error"
                                           :info {:data (json/read (io/reader body))}}  [pre-elapsed req-elapsed nil]]
                  ;; success response, but not json??
                  ("text/html" "text/plain")
                    [nil {:message "the jgi search service experienced an internal error"
                          :type "upstream-service-error"
                          :code "internal-error"
                          :info {:body (slurp (io/reader body))}} [pre-elapsed req-elapsed nil]]
                  ;; not even a valid text content type
                  [nil {:message "the jgi search service experienced an internal error"
                          :type "upstream-service"
                          :code "internal-error"
                          :info {:content-type content-type}} [pre-elapsed req-elapsed nil]])
            ;; handle relatively common http service errors
            502
                ;; we don't even try for 503.
                [nil {:message "jgi search service unavailable due to gateway error"
                      :type "upstream-service"
                      :code "gateway-error"}]
            503
                ;; we don't even try for 503.
                [nil {:message "jgi search service unavailable"
                      :type "upstream-service-error"
                      :code "service-unavailable"}]
            (case content-type
                  ;; only valid response
                  "application/json" [nil {:message "the jgi search service experienced an unknown error"
                                           :type "upstream-service"
                                           :code "unknown-error"
                                           :info {:status status
                                                  :data (json/read (io/reader body))}}  [pre-elapsed req-elapsed nil]]
                  ;; success response, but not json??
                  ("text/html" "text/plain")
                    [nil {:message "the jgi search service experienced an unknown error"
                          :type "upstream-service"
                          :code "unknown-error"
                          :info {:status status
                                 :body (slurp (io/reader body))}} [pre-elapsed req-elapsed nil]]
                  ;; not even a valid text content type
                  [nil {:message "the jgi search service experienced an unknown error"
                          :type "upstream-service"
                          :code "unknown-error"
                          :info {:status status
                                 :content-type content-type}} [pre-elapsed req-elapsed nil]]))))))
