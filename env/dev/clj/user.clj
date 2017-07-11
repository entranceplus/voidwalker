(ns user
  (:require [mount.core :as mount]
            [voidwalker.figwheel :refer [start-fw stop-fw cljs]]
            voidwalker.core))

(defn start []
  (mount/start-without #'voidwalker.core/repl-server))

(defn stop []
  (mount/stop-except #'voidwalker.core/repl-server))

(defn restart []
  (stop)
  (start))


