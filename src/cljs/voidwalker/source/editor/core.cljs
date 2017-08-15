(ns voidwalker.source.editor.core
  (:require [webpack.bundle]
            [reagent.core :as r]
            [voidwalker.source.editor.initial-state :as state]
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

(def state (r/atom (.deserialize (.-Raw slate)
                                    (state/initial)
                                    #js {:terse true})))

(def default-node "paragraph")


;;;;;;;;;;;;;;;;;;;;;;;;
;; edit table helpers ;;
;;;;;;;;;;;;;;;;;;;;;;;;

(defn insert-table [transform]
  (println "trying to insert table")
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

;;;;;;;;;;;;;;;;
;; for blocks ;;
;;;;;;;;;;;;;;;;

(defn transform-block-elements [state type list?]
  (transform-state state
                   (fn [transform]
                     (let [tf (set-block transform
                                         (if (has-block? state type)
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

(defn on-click-block [state e type]
  (println "block clicked " type)
  (let [list? (has-block? state "list-item")]
    (cond
      (or (= type "bulleted-list")
           (= type "numbered-list")) (transform-lists state type list?)
      (= type "table") (transform-state state insert-table)
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

(defn toolbar []
  (let [{:keys [marks nodes context-transforms]}  (schema/app)]
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

     [:div.row.some-padding [toolbar-buttons
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


(defn slate-editor []
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

(defn editor []
  [:div
   [toolbar]
   [slate-editor]])
