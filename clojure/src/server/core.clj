(ns server.core
  ;; deps
  (:require [ring.adapter.jetty :as jetty])
  (:require [clojure.tools.cli :refer [parse-opts]])
  (:require clojure.pprint)
  (:require [clojure.string :as string])
  (:require [clojure.data.json :as json])
  (:require [ring.middleware.stacktrace :as stacktrace])
  (:require [compojure.core :refer :all])
  (:require [compojure.route :as route])
  ;; routes
  (:require [server.routes.rpc :as rpc])
  (:require [server.async-app :as async-app])
  ;; middlewares
  (:require [server.middleware.config :as config])
  (:require [server.middleware.spy :as spy])
  ;; clojure boilerplate
  (:gen-class))

(defn just-404
  [request] 
  {:status 404
   :headers {"Content-Type" "text/plain"}
   :body "Route not found"})

;; We just have one top-level route, rpc.
(defroutes app-routes
  (POST "/rpc" request (rpc/handle request))
  (GET "/test" [] "hi")
  (route/not-found "Page not found"))

;; app is the main handler for the service web app. 
;; it can be used directly fom the project.clj ring plugin,
;; or in the direct call to jetty as in the main function below.
(def app
  (-> app-routes 
      config/wrap-config))
      ;; spy/wrap-spy
      ;; stacktrace/wrap-stacktrace))

(defn init
  []
  (config/load-config))

;; -main is the primary entrypoint to the service, both for the server and 
;; the "async" command line version.
(defn -main [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args [])]

    (cond
      ;; Service interface
      (= 1 (count arguments))
       (let [port (Integer/parseInt (first arguments))]
        (jetty/run-jetty app {:port port}))
      ;; "Async" or command-line interface
      ;; context-file  - is actually the json-rpc payload in a file
      ;; output-file   - name of file in which to provide response
      ;; token         - the auth token
      (= 3 (count arguments))
        (try
          (let [result (apply async-app/run arguments)]
            (print result)
            result)
          (catch Exception e
            (println (str "caught exception: " (.getMessage e)))))
      :else (throw (ex-info "Usage: invalid # of arguments"
                   {:explanation "The 'main' server call expects either 1 argument for server mode or 3 for command line mode"
                    :provided (count arguments)}))
      )))