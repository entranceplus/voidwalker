(ns voidwalker.source.editor.core
  (:require [webpack.bundle]
            [reagent.core :as r]
            [voidwalker.source.util :as u]
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
  "Transforms a js Slate object and returns the state after application"
  (clj->js (-> state
               .transform
               transform-fn
               .apply)))

(defn transform-state-atom [state transform-fn]
  "transform-state wrapped in an atom"
  (swap! state #(transform-state % transform-fn)))


;;;;;;;;;;;;;;;
;; for marks ;;
;;;;;;;;;;;;;;;

(defn get-mark [state mark-type]
  "get the first mark of type mark-type in slate"
  (first (filter (fn [mark]
                   (=  (:type mark) mark-type)) (slate->clj (.-marks state)))))

(defn transform-element [state type transform attrs]
  (cond
    (check-type (condp = (:transform-type attrs)
                  :inline (.-inlines @state)
                  :mark (.-marks @state)) type) ((:has-element? attrs))
    (.-isExpanded @state) (do (println "is epp " (:expanded attrs))
                              ((:expanded attrs)))
    :else (do (println "asd")
              ((:otherwise attrs)))))

;; all this seems like a trick to solve eager evaluation,, maybe consider a
;; macro .. the thing I still don't have a good idea of how the final model
;; should be.. it could be sth liek a dsl for writing marks and nodes
;; i think i cam reinventing the pugin maybe??? TODO check
(defn transform-inline [state type transform]
  "transform an inline element like an href"
  (transform-element state type transform
                     {:transform-type :inline
                      :has-element? (fn []
                              
                                      (.unwrapInline transform type))
                      :expanded (fn []
                                  (println "expanded")
                                  (-> transform
                                      (.wrapInline (clj->js {:type type
                                                             :data (when (= type "link")
                                                                     {:href (u/prompt! "Enter the url of the link")})}))
                                      .collapseToEnd))
                      :otherwise (fn []

                                   (let [text (u/prompt! "Enter text for link")
                                         href (u/prompt! "Enter url for the link")]
                                     (println "href is " href)
                                     (-> transform
                                         (.insertText text)
                                         (.extend (- 0 (count text)))
                                         (.wrapInline (clj->js
                                                       {:type type
                                                        :data {:href href}}))
                                         .collapseToEnd)))}))


;; transforms could be described as [transform :removeMark :addMark] etc
;; we'll see


(defn remove-mark [transform mark]
  (println "remove amrks adapter")
  (if (not= nil mark)
    (.removeMark transform (clj->js mark))
    transform))

(defn transform-mark [state mark-props transform]
  "transform a mark element"
  (println "OOH here itseled " mark-props)
  (transform-element state (:type mark-props) transform
                     {:transform-type :mark
                      :has-element? (fn []
                                      (println "has stuff")
                                      (if (.-isExpanded @state)
                                        (-> transform
                                            (remove-mark (get-mark @state
                                                                   (:type mark-props)))
                                            (.addMark (clj->js mark-props)))
                                        (println "[SlateJS][FontSizePlugin] selection collapsed, w/ inline.")))
                      :expanded (fn []
                                  (println "expanded")
                                  (-> transform
                                      (.addMark (clj->js mark-props))))
                      :otherwise (fn []
                                   (println "otherwise")
                                   (println "[SlateJS][FontSizePlugin] selection collapsed, w/o inline."))})
  transform)


(defn on-click-mark [state e mark-props]
  (.preventDefault e)
  (println "processing mark " mark-props)
  (let [js-mark-type (clj->js mark-props)
        type (:type mark-props)
        transform-fn (if (= type "font-size")
                       #(transform-mark state mark-props %)
                       #(.toggleMark %  js-mark-type))]
    (transform-state-atom state transform-fn)))



(defn has-mark? [state type]
  (check-type (.-marks @state) type))

;;;;;;;;;;;;;;;;;
;; for inlines ;;
;;;;;;;;;;;;;;;;;


;;;;;;;;;;;;;;;;
;; for blocks ;;
;;;;;;;;;;;;;;;;

(defn transform-block-elements [state type list?]
  (transform-state-atom state
                   (fn [transform]
                     (let [tf (set-block transform
                                         (if (has-block? state type)
                                           default-node
                                           type))]
                       (if list? (unwrap-list-blocks tf) tf)))))


(defn transform-lists [state type list?]
  (transform-state-atom state
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

;;; this function is supposed to be used after partial application
;;; see if this can be replaced by a macro. I feel most of the
;;; glue code can be..... but should we? I still don't think so
;;; HOF are working but things are getting complicated


(defn on-click-block [state e type]
  (println "block clicked " type)
  (let [list? (has-block? state "list-item")]
    (cond
      (or (= type "bulleted-list")
           (= type "numbered-list")) (transform-lists state type list?)
      (= type "table") (transform-state-atom state insert-table)
      (= type "link") (transform-state-atom state (partial transform-inline state type))
      :else (transform-block-elements state type list?))))

(defn on-click-context [state e type]
  (println "handling click of type " type)
  (cond
    (= type "remove-table") (transform-state-atom state remove-table)
    (= type "remove-row") (transform-state-atom state remove-row)
    (= type "remove-column") (transform-state-atom state remove-column)
    (= type "add-column") (transform-state-atom state add-column)
    (= type "add-row") (transform-state-atom state add-row)))


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


(defn custom-buttons [{:keys [on-click active? type icon element state]}]
  (fn []
    [:span.col-md-2
     (condp = element
       :button [:button.btn.btn-info
                {:on-click (fn [e]
                             (.preventDefault e)
                             (on-click e type))
                 :key type} type]
       :number-input [:input
                      {:type "number"
                       :key type
                       :placeholder (str "Enter value for " type)
                       :value (u/num (get-in (get-mark @state type)
                                             [:data :font-size]))
                       :on-key-down (fn [e]
                                      (when (= 13 (u/keycode e))
                                        (on-click e
                                                  {:data {:font-size (str  (u/get-value e) "px")}
                                                   :type type})))}])]))


(defn toolbar-render [{:keys [btn-list-md
                              on-mouse-down
                              active?
                              state]}]
  [:div (doall (for [btn-md btn-list-md]
                 (let [attrs {:on-click on-mouse-down
                              :active? active?
                              :type (name (key btn-md))
                              :icon (:icon-name (val btn-md))
                              :element (:element (val btn-md))
                              :key (name (key btn-md))
                              :state state}
                       button? (:button? (val btn-md))]
                   (println "as " (:element attrs))
                   (if (= nil (:element attrs))
                     (icon-buttons attrs)
                     [custom-buttons attrs]))))])


(defn toolbar-buttons [{:keys [btn-list-md on-mouse-down active? state] :as props}]
  (r/create-class {:component-did-mount
                   #(.tooltip ((.-$ js/window) "a"))

                   :display-name "Toolbar-Buttons"

                   :reagent-render #(toolbar-render props)}))

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
                              :state state
                              :on-mouse-down (partial on-click-mark state)
                              :active? (partial has-mark? state)}]]

     ;; block-toggling toolbar buttons
     [:div.row.some-padding [toolbar-buttons
                             {:btn-list-md nodes
                              :state state
                              :on-mouse-down (partial on-click-block state)
                              :active? (partial has-block? state)}]]

     ;; context buttons
     [:div.row.some-padding
      {:class (when-not (table? state) "hidden")
       ;; :style (when-not (table? slate) #js {:display "none"})
       }
      [toolbar-buttons
       {:btn-list-md context-transforms
        :state state
        :on-mouse-down (partial on-click-context state)
        :active? false}]]]))

;;;;;;;;;;;;
;; editor ;;
;;;;;;;;;;;;

(defn jsx [e attrs & children]
  (.createElement js/React
                  e
                  (clj->js attrs)
                  children))

(defn a-plugin [options]
  {:onKeyDown (fn [event data state]
                (.preventDefault event)
                (transform-state state (fn [transform]
                                         (.toggleMark transform "underline"))))})


(defn slate-editor [state]
  (jsx "div"
       {:className "editor"}
       (jsx (.-Editor slate)
            {:state @state
             :schema (schema/slate)
             :plugins [ edit-table-plugin, a-plugin ]
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
