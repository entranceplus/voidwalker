(ns voidwalker.source.quill
  (:require
   [reagent.core :as r]
   [voidwalker.source.editor.core :as e]))

(defn editor [{:keys [id content selection on-change-fn]}]
  [e/editor])

  
(defn display-area [{:keys [id content]}]
  (let [this (r/atom nil)]
    (r/create-class
     {:component-did-mount
      (fn [component]
        (reset! this (js/Quill. (r/dom-node component)
                                #js {:theme "snow"
                                     :placeholder "Compose an epic..."}))
        (.disable @this))

      :component-will-receive-props
      (fn [component next-props]
        (.pasteHTML @this (:content (second next-props))))

      :display-name  (str "quill-display-area-" id)

      :reagent-render
      (fn []
        [:div {:id (str "quill-display-area-" id)
               :class "quill-display-area"
               :dangerouslySetInnerHTML {:__html content}}])})))
