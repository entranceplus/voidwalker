(ns voidwalker.source.editor.schema)

(defn element [e]
  (println "element outer " e)
  (fn [props]
    (println "element inner " (type props))
    (.createElement js/React
                    e
                    (.-attributes props)
                    (.-children props))))

(defn app []
  {:nodes {:list-item {:body (element "li")}
           :numbered-list {:body (element "ol")
                           :icon-name "format_list_numbered"}
           :heading-one {:body (element "h1")
                         :icon-name "looks_one"}
           }
   :marks {:bold {:fontWeight "bold"
                  :icon-name "format_bold"}
           :italic {:fontStyle "italic"
                    :icon-name "format_italic"}
           :underlined {:textDecoration "underline"
                        :icon-name "format_underlined"}}})

(defn- transform-node [app-nodes]
  (reduce (fn [agg node]
            (conj agg {(key node) (:body (val node))}))
          {}
          app-nodes))

(defn app->slate [{:keys [nodes] :as schema}]
  (assoc schema :nodes (transform-node nodes)))

(defn slate []
  (app->slate (app)))
