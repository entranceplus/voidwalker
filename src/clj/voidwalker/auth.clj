(ns voidwalker.auth
  (:require [compojure.core :refer [defroutes GET POST]]
            [korma.core :as korma]
            [ring.util.http-response :as response]
            [voidwalker.content.core :refer [send-response]]
            [voidwalker.db])
  (:use hiccup.page hiccup.element))

(korma/defentity users)

(defn login-html []
  (list
  [:h3 "Login"]
  [:form {:method "POST" :action "login"}
    [:div "Username:"
      [:input {:type "text" :name "username" :required "required"}]]
    [:div "Password:"
      [:input {:type "password" :name "password" :required "required"}]]
    [:div
      [:input {:type "submit" :value "Log In"}]]]))

(defn sighup-html []
  (list
  [:h3 "SIGNUP"]
  [:form {:method "POST" :action "signup"}
    [:div "Username:"
      [:input {:type "text" :name "username" :required "required"}]]
    [:div "Password:"
      [:input {:type "password" :name "password" :required "required"}]]
    [:div
      [:input {:type "submit" :value "sign up"}]]]))



(defn disp []
  (html5
    [:html
      [:head]
        [:body "hello from auth"
        [:div
          (login-html)
          (sighup-html)]]]))


(defn handle-signup [{:keys [username password] :as user}]
  (let [userlist (korma/select users
                              (korma/where {:username username}))]
    (when-not (seq userlist)
      (korma/insert users (korma/values user)))))


(defn handle-login [{:keys [username password] :as user}]
  (let [userlist (korma/select users
                              (korma/where {:username username
                                            :password password}))]
    (when-not (seq userlist)
       (korma/select users (korma/where {:username username})))))


(defroutes auth-routes
  (GET "/check" []
       (send-response (response/ok "Ok done")))
  (POST "/login" {{:keys [username password] :as user} :params}
       (if-not (nil? (handle-login user))
          (send-response (response/ok {:msg "you have not signed up. Please sign up before logging in "}))
          (send-response (response/ok {:msg "login successful"}))))




  (POST "/signup" {{:keys [username password] :as user} :params}
        (if-not (nil? (handle-signup user))
          (send-response (response/ok {:msg "Signed up"}))
          (send-response (response/ok {:msg "username is already taken. Please choose other username."})))))














