(ns server.methods.search
  (:require [server.api.jgi-gateway :as jgi-gateway]))

;; call implements the method call.
;; Note that the arguments are destructured for the defined parameter structure.
;; For more complex input objects this may not be feasible or possible.
(defn call
  [[{:keys [query filter fields page limit include_private]}] {:keys [user-id]} config]
  (cond
    (not (map? query))
      [nil
      {:message "parameter 'query' must be an object"
       :type "input"
       :key "query"
       :code "missing"}
      nil]
    (not (or (nil? filter) (map? filter)))
      [nil
       {:message "parameter 'filter' must be an object"
        :type "input"
        :key "filter"
        :code "wrong-type"}
      nil]
    ;; etc, rest of validation here.
    :else
      (let [username (if (= 1 include_private) user-id nil)
            page (and page (- page 1))
            [result err [pre req post]] (jgi-gateway/query query filter fields username page limit config)
            stats {:request_elapsed_time req :pre_elapsed pre :post_elapsed post}]
        (if result
            (let [fixed {
                  :hits (map (fn [h] {"id" (get h "_id")
                                      "score" (get h "_score")
                                      "index" (get h "_index")
                                      "source" (get h "_source")})
                            (get result "hits"))
                  :total (get result "total")
                }]
              [fixed nil stats])
            [nil err stats]))))
