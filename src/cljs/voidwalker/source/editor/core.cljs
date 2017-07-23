(ns voidwalker.source.editor.core
  (:require [webpack.bundle]
            [reagent.core :as r]
            [voidwalker.source.editor.initial-state :as state]))


(def slate (.. js/window -deps -slate))

(def schema {:nodes {:list-item (fn [props]
                                  [:li
                                   (.-attributes props)
                                   (.-children props)])}
             :marks {:bold {:fontWeight "bold"
                            :icon-name "format_bold"}
                     :italic {:fontStyle "italic"
                              :icon-name "format_italic"}
                     :underlined {:textDecoration "underline"
                                  :icon-name "format_underlined"}}})

(defn slate->clj [state]
  (js->clj (.toJS state)
           :keywordize-keys
           true))

;; (defn editor-change [e data state])

;; (defn on-click-block [e type])


(defn slate-editor [state]
  (r/with-let [sl-editor (r/adapt-react-class (.-Editor slate))]
    [:div.editor
     [sl-editor {:state @state
                 :schema (clj->js schema)
                 :placeholder "Compose an epic"
                 :onChange #(reset! state %)}]]))

(defn mark-buttons [state marks]
  (r/with-let [on-click-mark (fn [e mark-type]
                               (.preventDefault e)
                               (swap! state (fn [cur-state]
                                              (-> cur-state
                                                  .transform
                                                  (.toggleMark mark-type)
                                                  .apply))))]
    [:div (doall (map
                  (fn [mark]
                    (let [mark-type (name (key mark))
                          icon (:icon-name (val mark))]
                      [:span.button
                       {:on-mouse-down #(on-click-mark % mark-type)
                        :data-active (some #(= mark-type (:type %))
                                           (slate->clj (.-marks @state)))
                        :key mark-type}
                       [:span.material-icons icon]]))
                  marks))]))

(defn toolbar [state]
  [:div.menu.toolbar-menu [mark-buttons state (:marks schema)]])

(defn editor []
  (let [state (r/atom (.deserialize (.-Raw slate)
                                    (state/initial)
                                    #js {:terse true}))]
    [:div
     [toolbar state]
     [slate-editor state]]))


(let [state (.deserialize (.-Raw slate)
                          (state/initial)
                          #js {:terse true})]
  (array-seq (.-marks state)))
