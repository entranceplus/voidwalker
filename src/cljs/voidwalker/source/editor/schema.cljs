(ns voidwalker.source.editor.schema
  (:require [reagent.core :as r]))

(defn *p* [whatever]
  (println whatever)
  whatever)

;; (def merge-js [& rest]
;;   (.assign js/Object))

;; (.assign js/Object
;;          #js {}
;;          (.-attributes props)
;;          #js {:href (.get (.. props -node -data) "href")})

(defn element [e & inline?]
  (fn [props]
    (let [element-props (if inline?
                          (clj->js (merge (js->clj (.-attributes props))
                                          {:href (.get (.. props -node -data)
                                                       "href")}))
                          (.-attributes props))]
      (.createElement js/React
                      e
                      element-props
                      (.-children props)))))

;; note :body is only being used in function transform-node
;; which is being used once to serialize state from json
;; just there ??? why is it so abstracted then ..
;; clean it..!!
(defn app []
  {:nodes {:list-item {:body (element "li")}
           :numbered-list {:body (element "ol")
                           :icon-name "format_list_numbered"}
           :bulleted-list {:body (element "ul")
                           :icon-name "format_list_bulleted"}
           :heading-one {:body (element "h1")
                         :icon-name "looks_one"}
           :heading-two {:body (element "h2")
                         :icon-name "looks_two"}
           :block-quote {:body (element "blockquote")
                         :icon-name "format_quote"}
           :link {:body (element "a" true)
                  :icon-name "http"}
           :table {:body (element "tbody")
                   :icon-name "view_module"}
           :table_row {:body (element "tr")}
           :table_cell {:body (element "td")}}
   :marks {:bold {:fontWeight "bold"
                  :icon-name "format_bold"}
           :italic {:fontStyle "italic"
                    :icon-name "format_italic"}
           :underlined {:textDecoration "underline"
                        :icon-name "format_underlined"}}
   :context-transforms {:remove-table {:button? true}
                        :remove-row {:button? true}
                        :remove-column {:button? true}
                        :add-column {:button? true}
                        :add-row {:button? true}}})

(defn- transform-node [app-nodes]
  (reduce (fn [agg node]
            (conj agg {(key node) (:body (val node))}))
          {}
          app-nodes))

;; (filter #() ())

(defn app->slate [{:keys [nodes] :as schema}]
  (assoc schema :nodes (transform-node nodes)))

(defn slate []
  (app->slate (app)))
