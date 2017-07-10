(ns voidwalker.env
  (:require [clojure.tools.logging :as log]))

(defn from-profiles []
  {})

(def defaults
  {:init
   (fn []
     (log/info "\n-=[voidwalker started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[voidwalker has shut down successfully]=-"))
   :middleware identity})
