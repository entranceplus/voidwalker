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

(def mc-editor (r/adapt-react-class (aget js/window "deps" "react-tinymce")))

(defn init-tinymce [comp]
  (.init js/tinymce #js {:selector "#editor"}))

(defn editor [content]
  [mc-editor {:content @content
              :on-change (fn [e]
                           (reset! content (-> e .-target .getContent)))}])
