(ns voidwalker.source.editor.core
  (:require [webpack.bundle]
            [reagent.core :as r]
            [voidwalker.source.editor.initial-state :as state]
            [voidwalker.source.editor.schema :as schema]))


(def slate (.. js/window -deps -slate))

(def state (r/atom (.deserialize (.-Raw slate)
                                    (state/initial)
                                    #js {:terse true})))

(def default-node "paragraph")

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

(defn on-click-block [state e type]
  (let [list? (has-block? state "list-item")]
    (if (and (not= type "bulleted-list")
             (not= type "numbered-list"))
      (transform-state state
                       (fn [transform]
                         (let [tf (set-block transform
                                             (if (has-block? state type)
                                               default-node
                                               type))]
                           (if list? (unwrap-list-blocks tf) tf))))
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
                               (wrap-block type))))))))
;;;;;;;;;;;;;
;; toolbar ;;
;;;;;;;;;;;;;

(defn toolbar-buttons [{:keys [btn-list-md on-mouse-down active?]}]
  (doall (for [btn-md btn-list-md]
           (let [type (name (key btn-md))
                 icon (:icon-name (val btn-md))]
             [:span.button
              {:on-click (fn [e]
                                (on-mouse-down e type))
               :data-active (active? type)
               :key type}
              [:span.material-icons icon]]))))

(defn toolbar []
  (let [{:keys [marks nodes]}  (schema/app)]
    [:div.menu.toolbar-menu

     ;; mark-toggling toolbar buttons
     (toolbar-buttons
      {:btn-list-md marks
       :on-mouse-down (partial on-click-mark state)
       :active? (partial has-mark? state)})

     ;; block-toggling toolbar buttons
     (toolbar-buttons
      {:btn-list-md nodes
       :on-mouse-down (partial on-click-block state)
       :active? (partial has-block? state)})
     ]))

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
             :placeholder "Compose an epic"
             :onChange #(reset! state %)})))



(defn editor []
  [:div
     [toolbar]
     [slate-editor]])
