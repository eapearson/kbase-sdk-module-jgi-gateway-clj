(defproject server "0.1.0-SNAPSHOT"
  :description "A bridge to the jgi_gateway search service"
  :url "https://github.com/eapearson/jgi_gateway_clj"
  :license {:name "KBase Open Source License"
            :url "https://kbase.us/open-source/license"}
  ;; :plugins [[cider/cider-nrepl "0.11.0"]]
  :plugins [[lein-ring "0.9.7"]
            [lein-ancient "0.6.10"]]
  ;; This defines the entry point to the server
  ; :ring {:handler server.core/app}
  ;; now with profiles!  
  ;; Profiles are now used
  ;; The service profile provides the main standalone server entry point
  ;; The deploy profile provides the deployment interface
  ;; The dev profile provides the develop-time rin server integration
  ;; The uberjar provides uberjar building configuration 
  :profiles {:service {:main server.core}
             :deploy {:main deploy.core}
             :dev {:ring {:handler server.core/app
                          :init server.core/init}}
             :uberjar {:aot :all}
  }
  :main nil
  ; :aot [server.core]
  :dependencies [[org.clojure/clojure "1.9.0-alpha17"]
                 [org.clojure/tools.cli "0.3.5"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/core.cache "0.6.5"]
                 [org.clojure/core.async "0.3.443"]
                 [me.raynes/fs "1.4.6"]
                 [clojure-ini "0.0.2"]
                 [stencil "0.5.0"]
                 ;; for easy properties file access
                 [clojurewerkz/propertied "1.3.0"]
                 [ring/ring "1.6.2"]
                 [ring/ring-jetty-adapter "1.6.2"]
                 [http-kit "2.2.0"]
                 [compojure "1.6.0"]])
