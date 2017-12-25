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

(defn create-react-element [e attrs children]
  (.createElement js/React
                  e
                  attrs
                  children))

(defn element
  ([e]  (fn [props]
          (create-react-element e
                                (.-attributes props)
                                (.-children props))))
  ([e attr-fn] (fn [props]
                 (create-react-element e
                                       (clj->js (merge (js->clj (.-attributes props))
                                                       (attr-fn props)))
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
           :link {:body (element "a" (fn [props]
                                       {:href (.get (.. props -node -data)
                                                    "href")}))
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
                        :icon-name "format_underlined"}
           :font-size {:body (element "span"
                                      (fn [props]
                                        {:style {:fontSize (.get (.. props -mark -data)
                                                                 "font-size")}}))
                       :element :number-input
                       :icon-name "build"}}
   :context-transforms {:remove-table {:button? true
                                       :element :button}
                        :remove-row {:button? true
                                     :element :button}
                        :remove-column {:button? true
                                        :element :button}
                        :add-column {:button? true
                                     :elment :button}
                        :add-row {:button? true
                                  :element :button}}})

(defn- transform-node [app-nodes]
  "take nodes from schema and make their key equal to the :body val"
  (reduce (fn [agg node]
            (conj agg {(key node) (if-let [body (:body  (val node))]
                                    body
                                    (val node))}))
          {}
          app-nodes))

(defn app->slate [{:keys [nodes marks] :as schema}]
  "prepare schema for slate consumption. Consumption schema format requires
   the nodes key to have body equal to body. Think
   bold: props => <strong>{props.children}</strong>
  --------------------------------------------------"
  (assoc schema
         :nodes (transform-node nodes)
         :marks (transform-node marks)))

(defn slate []
  (app->slate (app)))
