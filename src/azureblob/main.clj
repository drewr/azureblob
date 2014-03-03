(ns azureblob.main
  (:gen-class)
  (:require [clojure.tools.cli :as cli]
            [azureblob.core :as azure]
            [azureblob.log :as log]))

(def opts
  [["-n" "--name NAME" "Account name"]
   ["-k" "--key KEY" "Account key"]
   ["-c" "--container CONTAINER" "Blob container"]
   ["-h" "--help"]])

(defn -main [& args]
  (let [opts* (cli/parse-opts args opts)]
    (->> (azure/upload (:options opts*) (:arguments opts*))
         (map (fn [[k v]] (format "%s %s" (name k) v)))
         (apply log/log))))
