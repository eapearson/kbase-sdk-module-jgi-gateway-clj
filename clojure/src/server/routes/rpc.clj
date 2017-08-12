(ns server.routes.rpc
  (:require [server.rpc.json :as json-rpc]))

(defn handle [request]
    (-> request 
        json-rpc/validate-header 
        json-rpc/validate-auth-token
        json-rpc/extract-json 
        json-rpc/validate 
        json-rpc/dispatch))
