(ns user.test
  (:require [cljs-test-runner.main :as cljs]
            [voidwalker.systems :refer [system-config]]
            [snow.repl :as repl]
            [snow.env :refer [read-edn]]
            [clojure.spec.alpha :as s]
            [expound.alpha :as e]
            [shadow.cljs.devtools.server :as server]
            [cider.nrepl :refer [cider-nrepl-handler]]
            [clojure.tools.nrepl.server :as nrepl]
            [clojure.core.async :refer [go]]
            [shadow.cljs.devtools.api :as shadow]))

(alter-var-root #'s/*explain-out* (constantly e/printer))

(s/check-asserts true)

(def config (read-edn "profiles.edn"))

(keys (repl/system))

(defn -main [& args]
  (repl/start-systems {:snow.systems/system-fn system-config
                       :snow.systems/config config})
  (nrepl/start-server :port (:repl-port config) :handler cider-nrepl-handler)
  (server/start!) 
  (go (shadow/dev :test))
  (go (shadow/dev :app)))
