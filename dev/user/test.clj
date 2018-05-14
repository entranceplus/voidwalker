(ns user.test
  (:require [cljs-test-runner.main :as cljs]
            [voidwalker.systems :refer [system-config]]
            [snow.repl :as repl]            
            [clojure.spec.alpha :as s]
            [expound.alpha :as e]
            [shadow.cljs.devtools.server :as server]
            [cider.nrepl :refer [cider-nrepl-handler]]
            [clojure.tools.nrepl.server :as nrepl]
            [clojure.core.async :refer [go]]
            [shadow.cljs.devtools.api :as shadow]))

(alter-var-root #'s/*explain-out* (constantly e/printer))

(s/check-asserts true)

(keys (repl/system))

(defn -main [& args]
  (repl/start! system-config)
  (repl/start-nrepl)
  (server/start!)
  (go (shadow/dev :test))
  (go (shadow/dev :app)))
