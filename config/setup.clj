#!/usr/bin/env boot

;;(set-env! :dependencies '[])
(require '[clojure.java.shell :as s])
;; thoughts-> we can also build up all the commands
;; and then execute as a batch job

;; what follows is just for fun
;; this could be flawed concept/code

(def xampp-binary "xampp-linux-x64-5.6.30-0-installer.run")

(def xampp-install-location "/opt/lampp/")

;;;;;;;;;;;;;;
;; packages ;;
;;;;;;;;;;;;;;

(defn update-packages! []
  (s/sh "apt-get" "update" "-y"))

(defn install-package! [package]
  (s/sh "apt-get" "install" package "-y"))

;;;;;;;;;;;;;;;;;;;;;
;; ubuntu services ;;
;;;;;;;;;;;;;;;;;;;;;

(defn enable-service [service]
  (s/sh "systemctl" "enable" service)
  (s/sh "systemctl" "restart" service))


;;;;;;;;;;;
;; mysql ;;
;;;;;;;;;;;

(defn mysql-present? []
  (try  (s/sh "/opt/lampp/bin/mysql")
        (catch Exception _)))

(defn install-xampp! []
  (s/sh "wget" (str "https://www.apachefriends.org/xampp-files/5.6.30/"
                  xampp-binary))
  (s/sh "chmod" "a+x" xampp-binary)
  (s/sh (str "./" xampp-binary) "--mode" "unattended"))


(defn create-mysqldb! [db]
  (s/sh (str xampp-install-location "bin/mysql")
        "-u" "root" "-e" (s/sh (str "create database if not exists " db))))


(defn start-mysql! [database]
  (update-packages!)
;; todo add if condition to check
  (when-not (mysql-present?)
    (install-xampp!))
  (s/sh (str xampp-install-location "lampp") "startmysql")
  (create-mysqldb!))


;;;;;;;;;;
;; java ;;
;;;;;;;;;;

(defn install-java []
  (install-package! "default-jdk")
  (println (s/sh "java" "-version")))

;;;;;;;;;;;
;; nginx ;;
;;;;;;;;;;;

(defn configure-nginx []
  (s/sh (str xampp-install-location "lampp" "stopapache"))
  (install-package! "nginx")
  (s/sh "cp" "-ru" "default" "/etc/nginx/sites-available/default")
  (enable-service "nginx"))

;;;;;;;;;;;;;;;;
;; app config ;;
;;;;;;;;;;;;;;;;

(defn app-config []
  (s/sh "cp" "./voidwalker.service" "/lib/systemd/system/voidwalker.service") 
  (set-env! :database-url "jdbc:mysql://localhost/voidwalker?user=root&password=")
  (s/sh "java" "-jar" "voidwalkervoidwalker.jar" "migrate")
  (s/sh "systemctl" "daemon-reload")
  (enable-service "voidwalker"))


(defn -main [& args]
  (println "Starting setup")
  (do (println (:out (s/sh "ls")))
      (s/sh "cd" "voidwalker")
      (start-mysql!)
      (app-config)
      (configure-nginx))
  (println "Setup complete"))

