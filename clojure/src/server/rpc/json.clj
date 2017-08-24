(ns server.rpc.json
  (:require [clojure.data.json :as json])
  (:require [server.methods.status :as status])
  (:require [server.methods.search-jgi :as search-jgi])
  (:require [server.methods.stage-objects :as stage-objects])
  (:require [server.methods.stage-status :as stage-status])
  (:require [server.methods.list-jobs :as list-jobs])
  (:require [server.config :as config])
  (:require [server.auth :as auth]))

; (println "jgi token is")
; (println (config/get-config :jgi-token))

;; JSON RPC IMPLEMENTATION, ala KBase

(defn validate-header [request]
  (let [content-type (get (get request :headers) "content-type")]
    (when-not (= content-type "application/json")
            (throw (Exception. (str "Unexpected content type:" content-type))))
   request))

(defn validate-auth-token [request]
  (let [authorization (get (get request :headers) "authorization")]
    (if-let [{:keys [user] :as token-info} (auth/token-info authorization)]
      (-> request
          (assoc :auth/token authorization)
          (assoc :auth/token-info token-info)
          (assoc :auth/user-id user))
      request)))

(defn extract-json [request]
  (let [json (json/read (clojure.java.io/reader (:body request)) :key-fn clojure.core/keyword)]
    (assoc request :json-rpc-message json)))

(def method-map {"jgi_gateway_eap.status" {:call status/call
                                           :auth-required? false}
                 "jgi_gateway_eap.search_jgi" {:call search-jgi/call
                                               :auth-required? true}
                 "jgi_gateway_eap.stage_objects" {:call stage-objects/call
                                                  :auth-required? true}
                 "jgi_gateway_eap.list_jobs" {:call list-jobs/call
                                                  :auth-required? true}
                 "jgi_gateway_eap.stage_status" {:call stage-status/call
                                                  :auth-required? true}})

(defn dispatch [request]
  (let [message (:json-rpc-message request)
        config (:config request)]
    (if-let [method-spec (get method-map (:method message))]
        ;; TODO: we should really do a roles-based check here.
        (if (and (:auth-required? method-spec)
                  (not (:auth/user-id request)))
            ;; No authorization
            {:status 401
             :headers {"Content-type" "application/json"}
             :body (json/write-str {"version" "1.1"
                                    "id" (:id message)
                                    "error" "No authorization"})}
            ;; Authorization not required, or required and available.
            (let [params (:params message)
                  ;; the user-id will be deposited if the token validates.
                  context {:user-id (:auth/user-id request "eapearson")}
                  result (apply (:call method-spec) [params context config])]
              {:status 200
              :headers {"Content-type" "application/json"}
              :body (json/write-str {"version" "1.1"
                                      "id" (:id message)
                                      "result" result})}))
            "Sorry, not found")))

(defn validate [request]
  (let [json (:json-rpc-message request)]
    (let [version (:version json)
          method (:method json)
          params (:params json)
          id (:id json)]
      (if (not= version "1.1")
        (throw (Exception. (str "Incorrect json rpc version " version))))
      request)))
