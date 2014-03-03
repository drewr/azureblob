(ns azureblob.main
  (:gen-class)
  (:require [clojure.tools.cli :as cli]
            [azureblob.core :as azure]))

   "es1"
   (str "fwabTQeFvQb9/45oklR9B"
        "AH4k7e1WsU7hYZJbFoL"
        "CdCMF6BCsE/85/WqlEbYBj4B"
        "kEj8+m9Z5Nrr9Lvob1zIaQ==")

(def opts
  [["-n" "--name NAME" "Account name"]
   ["-k" "--key KEY" "Account key"]
   ["-c" "--container CONTAINER" "Blob container"]
   ;; A boolean option defaulting to nil
   ["-h" "--help"]])

(defn -main [& args]
  (let [opts* (cli/parse-opts args opts)]
    (->> (azure/upload (:options opts*) (:arguments opts*))
         (map (fn [[k v]] (format "%s %s" (name k) v)))
         (apply println))))
