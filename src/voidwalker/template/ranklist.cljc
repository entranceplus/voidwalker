(ns voidwalker.template.ranklist
  #?(:cljs (:require-macros [hiccups.core :refer [html]]))
  (:require #?(:cljs [reagent.core :as r])
            #?(:cljs [hiccups.runtime :as hiccupsrt])
            [clojure.walk :as w]
            [hickory.core :as h]
            [hickory.convert :as hc]
            [hickory.zip :as hzip]
            [hickory.render :as render]
            [re-frame.core :as rf]
            [clojure.zip :as z]
            #?(:clj [hiccup.core :refer [html]])))

(defn- data-tmpl  [idx {:keys [name location website mhrd placement mq mode]}]
  [:div.list {:key idx}
   [:div.list-heading
    [:p (+ idx 1)]
    [:p (:c name)]]
   [:div.list-content
    [:p [:span (str (:h location) ":")] (:c location)]
    [:p [:span (:h  mhrd)] (:c mhrd)]
    [:p [:span (:h  placement)] (:c placement)]]
   [:div.list-content
    [:p [:span (:h mq)]  (if (empty? (:c mq))
                           "No"
                           (:c mq))]
    [:p [:span (:h mode)] (:c mode)]
    [:a.last-info-content-child-a {:href (:c website)} "Visit Website"]]])


;; (def data @(rf/subscribe [:snow.files.ui/files :articles :voidwalker.template.ui/new :datasource]))

(defn template [data]
  (println "data is " data)
  [:div
   [:section.exam
    [:h1 "Title"]
    [:p "Description"]]
   (cond-> [:section.exam-list ]
     (some? data)  (conj (->> data
                              vals first
                              (map-indexed data-tmpl))))])

;; (html (document @(rf/subscribe [:snow.files.ui/files :articles :voidwalker.template.ui/new :datasource])))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The below parsing is to add any attribute to all elements in      ;;
;; hiccup. Turned out later to not be useful but maybe useful later  ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; (defn zip-map [f loc]
;;   " Map f over every node of the zipper.
;;     The function received has the form (f node-value loc),
;;     the node value and its location"
;;   (loop [z loc] 
;;     (if (z/end? z)
;;       (z/root z) ; perhaps you can call zip/seq-zip or zip/vector-zip?
;;       (recur (z/next (z/edit z f))))))

;; (defn merge-editable-attrs [n]
;;   (if (and (map? n)
;;            (= (:type n) :element))
;;     (update-in n [:attrs] merge ce)
;;     n))

;; for this to work root-tmpl should not specify ce
;; it may work but not tested

;; (defn as-editor []
;;   (->> (list (root-tmpl nil))
;;        hc/hiccup-fragment-to-hickory
;;        first
;;        hzip/hickory-zip
;;        (zip-map merge-editable-attrs)
;;        hc/hickory-to-hiccup))
