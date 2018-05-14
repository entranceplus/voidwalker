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

(defn csv->map
  [file-content]
  (-> file-content
      (read-csv  :newline :cr+lf)
      gen-datasource-map
      doall))
