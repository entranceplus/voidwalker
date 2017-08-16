(ns voidwalker.source.editor.core
  (:require [webpack.bundle]
            [reagent.core :as r]
            [voidwalker.source.editor.initial-state :as st]
            [voidwalker.source.editor.schema :as schema]))

;; ;;;;;;;;;;;;;;;;;;;
;; ;; useful macros ;;
;; ;;;;;;;;;;;;;;;;;;;

;; (defmacro if-let*
;;   ([bindings then]
;;    `(if-let* ~bindings ~then nil))
;;   ([bindings then else]
;;    (if (seq bindings)
;;      `(if-let [~(first bindings) ~(second bindings)]
;;         (if-let* ~(drop 2 bindings) ~then ~else)
;;         ~else)
;;      then)))

;;;;;;;;;;;;;;;;;;
;; dependencies ;;
;;;;;;;;;;;;;;;;;;

(def slate (.. js/window -deps -slate))

(def edit-table-plugin  ((.. js/window -deps -edittable)))

;; (def state (r/atom (.deserialize (.-Raw slate)
;;                                     (st/initial)
;;                                     #js {:terse true})))

(def default-node "paragraph")

;;;;;;;;;;;;;;;;;;;
;; serialization ;;
;;;;;;;;;;;;;;;;;;;

(defn json? [string]
  (try (do  (.parse js/JSON string)
            true)
       (catch js/Error e false)))

(defn get-serialized-state [state]
  (.stringify js/JSON (.serialize (.-Raw slate)
                                  state)))

(defn empty-state []
  (.deserialize (.-Raw slate)
                (st/initial)
                #js {:terse true}))

(defn deserialize-state [raw-state]
  (println "deserializing " raw-state)
  (.deserialize (.-Raw slate)
                (.parse js/JSON raw-state)
                #js {:terse true}))

;; quick and dirty fix for initialization of state,
;; state cab be obtained from a reframe subscription ?
(defn set-state! [raw-state]
  (deserialize-state raw-state))

;;;;;;;;;;;;;;;;;;;;;;;;
;; edit table helpers ;;
;;;;;;;;;;;;;;;;;;;;;;;;

(defn insert-table [transform]
  (.insertTable (.-transforms edit-table-plugin)
                transform))

(defn remove-table [transform]
  (.removeTable (.-transforms edit-table-plugin)
                transform))

(defn remove-row [transform]
  (.removeRow (.-transforms edit-table-plugin)
                transform))

(defn remove-column [transform]
  (.removeColumn (.-transforms edit-table-plugin)
                 transform))

(defn add-row [transform]
  (.insertRow (.-transforms edit-table-plugin)
           transform))

(defn add-column [transform]
  (.insertColumn (.-transforms edit-table-plugin)
              transform))

;;;;;;;;;;;;;;;;;;;
;; slate helpers ;;
;;;;;;;;;;;;;;;;;;;

(defn slate->clj [state]
  (js->clj (.toJS state)
           :keywordize-keys
           true))

;; check if a type exists in attrs of state
(defn some-slate [coll type some-fn]
  (some some-fn (slate->clj coll)))

(defn check-type [slate-coll type]
  (some-slate slate-coll type #(= type (:type %))))

(defn type? [state type]
  (some-slate
   (.-blocks state)
   type
   (fn [block]
     (.getClosest (.-document state)
                  (:key block)
                  (fn [parent]
                    (= (.-type parent) type))))))


;;;;;;;;;;;;;;;;;;
;; block helper ;;
;;;;;;;;;;;;;;;;;;

(defn unwrap-block [transform & block-types]
  (reduce
   (fn [transform type]
     (.unwrapBlock transform type)) transform block-types))

(defn wrap-block [transform & block-types]
  (reduce
   (fn [transform type]
     (.wrapBlock transform type)) transform block-types))

(defn unwrap-list-blocks [transform]
  (unwrap-block transform "bulleted-list" "numbered-list"))

(defn set-block [transform block]
  (.setBlock transform block))

(defn has-block? [state type]
  (check-type (.-blocks @state) type))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; transform-state driver ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn transform-state [state transform-fn]
  (swap! state (fn [cur-state]
                 (clj->js (-> cur-state
                              .transform
                              transform-fn
                              .apply)))))


;;;;;;;;;;;;;;;
;; for marks ;;
;;;;;;;;;;;;;;;

(defn on-click-mark [state e mark-type]
  (.preventDefault e)
  (transform-state state #(.toggleMark %  mark-type)))


(defn has-mark? [state type]
  (check-type (.-marks @state) type))

;;;;;;;;;;;;;;;;;
;; for inlines ;;
;;;;;;;;;;;;;;;;;

(defn has-inline? [state type]
  (check-type (.-inlines @state) type))


;;;;;;;;;;;;;;;;
;; for blocks ;;
;;;;;;;;;;;;;;;;

(defn transform-block-elements [state type list?]
  (transform-state state
                   (fn [transform]
                     (let [tf (set-block transform
                                         (if  (has-block? state type)
                                           default-node
                                           type))]
                       (if list? (unwrap-list-blocks tf) tf)))))


(defn transform-lists [state type list?]
  (transform-state state
                       (fn [transform]
                         (cond
                           (and list? (type? @state type))
                           (-> transform
                               (set-block default-node)
                               unwrap-list-blocks)
                           list?
                           (-> transform
                               (unwrap-block
                                (if (= "bulleted-list")
                                  "numbered-list" "bulleted-list"))
                               (wrap-block type))
                           :else
                           (-> transform
                               (set-block "list-item")
                               (wrap-block type))))))

(defn prompt! [message]
  (.prompt js/window message))

;;; this function is supposed to be used after partial application
;;; see if this can be replaced by a macro. I feel most of the 
;;; glue code can be
(defn transform-inline [state type transform]
  (cond
    (has-inline? state type) (do (println "Is inline")
                                 (.unwrapInline transform type))
    (.-isExpanded @state) (do (println "It is expanded")
                             (-> transform
                                 (.wrapInline (clj->js {:type type
                                                        :data (when (= type "link")
                                                                {:href (prompt! "Enter the url of the link")})}))
                                 .collapseToEnd)) 
    :else (let [text (prompt! "Enter text for link")
                href (prompt! "Enter url for the link")]
            (println "href is " href)
            (-> transform
                (.insertText text)
                (.extend (- 0 (count text)))
                (.wrapInline (clj->js 
                              {:type type
                               :data {:href href}}))
                .collapseToEnd))))


(defn on-click-block [state e type]
  (println "block clicked " type)
  (let [list? (has-block? state "list-item")]
    (cond
      (or (= type "bulleted-list")
           (= type "numbered-list")) (transform-lists state type list?)
      (= type "table") (transform-state state insert-table)
      (= type "link") (transform-state state (partial transform-inline state type))
      :else (transform-block-elements state type list?))))



(defn on-click-context [state e type]
  (println "handling click of type " type)
  (cond
    (= type "remove-table") (transform-state state remove-table)
    (= type "remove-row") (transform-state state remove-row)
    (= type "remove-column") (transform-state state remove-column)
    (= type "add-column") (transform-state state add-column)
    (= type "add-row") (transform-state state add-row)))


;;;;;;;;;;;;;
;; toolbar ;;
;;;;;;;;;;;;;

(defn icon-buttons [{:keys [on-click active? type icon]}]
  [:span.col-md-1
   {:key type}  [:a.bottom
                 {:on-click (fn [e]
                              (on-click e type))
                  :data-active #(active? type)
                  :data-placement "bottom"
                  :data-toggle "tooltip"
                  :data-original-title type}
                 [:span.material-icons icon]]])

;; TODO hide in context menu when not needed

(defn context-buttons [{:keys [on-click active? type icon]}]
  [:span.col-md-2
   {:key type} [:button.btn.btn-info {:on-click (fn [e]
                                                  (.preventDefault e)
                                                  (on-click e type))} type]])


(defn toolbar-render [{:keys [btn-list-md
                              on-mouse-down
                              active?]}]
  [:div (doall (for [btn-md btn-list-md]
                 (let [attrs {:on-click on-mouse-down
                              :active? active?
                              :type (name (key btn-md))
                              :icon (:icon-name (val btn-md))}
                       button? (:button? (val btn-md))]
                   (if button?
                     (context-buttons  attrs)
                     (icon-buttons attrs)))))])

(defn toolbar-buttons [{:keys [btn-list-md on-mouse-down active?]}]
  (r/create-class {:component-did-mount
                   #(.tooltip ((.-$ js/window) "a"))

                   :display-name "Toolbar-Buttons"

                   :reagent-render toolbar-render}))

;; (println "Does this even work")

(defn toolbar [state]
  (let [{:keys [marks nodes context-transforms]}  (schema/app)
        table? (fn [state]
                 (.isSelectionInTable (.-utils edit-table-plugin)
                                      @state))]
    [:div.menu.toolbar-menu

     ;; mark-toggling toolbar buttons
     [:div.row.some-padding [toolbar-buttons
                             {:btn-list-md marks
                              :on-mouse-down (partial on-click-mark state)
                              :active? (partial has-mark? state)}]]

     ;; block-toggling toolbar buttons
     [:div.row.some-padding [toolbar-buttons
                             {:btn-list-md nodes
                              :on-mouse-down (partial on-click-block state)
                              :active? (partial has-block? state)}]]

     ;; context buttons
     [:div.row.some-padding
      {:class (when-not (table? state) "hidden")}
      [toolbar-buttons
       {:btn-list-md context-transforms
        :on-mouse-down (partial on-click-context state)
        :active? false}]]]
    ))

;;;;;;;;;;;;
;; editor ;;
;;;;;;;;;;;;

(defn jsx [e attrs & children]
  (.createElement js/React
                  e
                  (clj->js attrs)
                  children))


(defn slate-editor [state]
  (jsx "div"
       {:className "editor"}
       (jsx (.-Editor slate)
            {:state @state
             :schema (schema/slate)
             :plugins [ edit-table-plugin ]
             :placeholder "Compose an epic"
             :onChange #(reset! state %)})))

(defn sp-button [text]
  [:button.space.btn.btn-default "Remove table"])

;;;;;;;;;;;;;;;;;;;;;;
;; public component ;;
;;;;;;;;;;;;;;;;;;;;;;

(defn editor [{:keys [content]}]
  [:div
   [toolbar content]
   [slate-editor content]])
