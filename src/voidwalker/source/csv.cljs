(ns voidwalker.source.csv
  (:require [clojure.string :as str]))

(def ^:private newlines
  {:lf "\n" :cr+lf "\r\n"})

(def ^:private newline-error-message
  (str ":newline must be one of [" (str/join "," (keys newlines)) "]"))


(defn read-csv
  "Reads data from String in CSV-format."
  [data & options]
  (let [{:keys [separator newline] :or {separator "," newline :lf}} options]
    (if-let [newline-char (get newlines newline)]
      (->> (str/split data newline-char)
           (map #(str/split % separator)))
      (throw (js/Error. newline-error-message)))))

(defn csv-data->maps [csv-data]
  (map zipmap
       (->> (first csv-data) ;; First row is the header
            ;;(map keyword) ;; Drop if you want string keys instead
            repeat)
       (rest csv-data)))

(defn process-row [row]
  (->>  row
        (map (fn [[key value]]
               {(-> key str/trim str/lower-case keyword) {:h key
                                                          :c value}}))
        (into {})))

(defn gen-datasource-map [csv]
  (->> (csv-data->maps csv)
       (map process-row)))

(defn read-normalized-csv
  "try with crlf if not able to parse then with ;f"
  [file-content]
  (let [csv (read-csv file-content :newline :cr+lf)]
    (if (= 1 (count csv))
      (read-csv file-content :newline :lf)
      csv)))

(defn csv->map
  [file-content]
  (println "file content received for processing to map " file-content)
  (-> file-content
      read-normalized-csv
      gen-datasource-map
      doall))
