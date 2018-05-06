(ns voidwalker.source.inline
  (:require [re-frame.core :as rf]))

(defn data-tmpl  [idx {:keys [name location website mhrd placement mq mode]}]
  [:div.list-container {:key idx}
   [:div.list-heading
    [:p (+ idx 1)]
    [:p (:c name)]]
   [:div.list-info-container
    [:div.list-info-content
     [:p [:span (str (:h location) ":")] (:c location)]
     [:p [:span (:h  mhrd)] (:c mhrd)]
     [:p.last-info-content-child [:span (:h  placement)] (:c placement)]]
    [:div.list-info-content
     [:p [:span (:h mq)]  (if (empty? (:c mq))
                            "No"
                            (:c mq))]
     [:p [:span (:h mode)] (:c mode)]
     [:a.last-info-content-child-a {:href (:c website)} "Visit Website"]]]])

(defn root-tmpl [data]
  [:div {:content-editable true}
   [:section.exam
    [:h1 "Title"]
    [:p "Description"]]
   [:section.exam-list (map-indexed data-tmpl data)]])

(rf/reg-sub
 :test/data
 (fn [_ _] (rf/subscribe [:snow.files.ui/all-files]))
 (fn [files _]
   (-> files :datasource vals first)))

(defn page []
  [:div [root-tmpl @(rf/subscribe [:test/data])]])
