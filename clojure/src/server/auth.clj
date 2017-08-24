(ns server.auth
    (:require [org.httpkit.client :as http])
    (:require [clojure.java.io :as io])
    (:require [clojure.data.json :as json])
    (:require [clojure.core.cache :as cache]))

(def token-cache (atom (-> {}
                           (cache/fifo-cache-factory :threshold 1000)
                           (cache/ttl-cache-factory :ttl 300000))))

(defn fetch-token-info
    [token]
    ;; TODO: use the config
    (println "fetching token info")
    (let [url (str "https://ci.kbase.us/services/auth" "/api/V2/token")
          header {"Authorization" token}
          options {:timeout 5000
                   :headers header
                   :insecure? true
                   :as :stream}
          {:keys [status headers body error] :as resp} @(http/get url options)]
        (if error (throw (Exception. "Error getting token"))
            (json/read (io/reader body) :key-fn keyword))))

(defn hit-or-miss
    [cache token]
    (if (cache/has? cache token)
        (cache/hit cache token)
        (cache/miss cache token (fetch-token-info token))))

(defn token-info
    [token]
    ;; Ensure that the token info is in the cache.
    (swap! token-cache hit-or-miss token)
    ;; Get the token info from the cache
    (cache/lookup @token-cache token))

;; TODO: remove token from cache if it has expired? Maybe, but then we'll just fetch it again.
(defn ensure-token
  [token]
  (println "ensuring token...")
  (let [{:keys [expires user name cachefor] :as token-info} (token-info token)
        now (inst-ms (java.time.Instant/now))]
    (if (and expires (> expires now))
        token-info
        nil)))
