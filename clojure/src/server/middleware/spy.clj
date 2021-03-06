(ns server.middleware.spy)

;; spy will simply log the request and response, handy for debugging.
;; use it like (def app (-> spy/wrap-spy))
(defn wrap-spy 
  [handler]
  (fn [request]
    (clojure.pprint/pprint "request:")
    (clojure.pprint/pprint request)
    (clojure.pprint/pprint handler)
    (let [response (handler request)]
      (clojure.pprint/pprint "response:")
      (clojure.pprint/pprint response)
      response)))