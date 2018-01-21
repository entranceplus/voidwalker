(ns voidwalker.source.editor.core
  (:require [webpack.bundle]
            [reagent.core :as r]
            [voidwalker.source.util :as u]))

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

;; (def mc-editor [:> (aget js/window "deps" "react-tinymce") {:content (or @content "")
;;                                                             :initial-value "Welcome"
;;                                                             ;; :init   {:selector "#editor"
;;                                                             ;;          :plugins "link table image code"}
;;                                                             :init  {:plugins "code"}
;;                                                             :on-change (fn [e] (reset! content (-> e .-target .getContent)))}])

(defn init-tinymce [comp]
  (.init js/tinymce #js {:selector "#editor"
                         :plugins ["table"]
                         :menubar "table"
                         :toolbar "table"}))

(def tinymce (aget js/window "deps" "react-tinymce"))

(defn editor [content]
  (fn []
    [:> tinymce
      {:content (or @content "")
       :initial-value "Welcome"
       :init  {"plugins" "table"}
       :on-change (fn [e] (reset! content (-> e .-target .getContent)))}]))
