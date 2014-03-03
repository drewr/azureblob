(ns azureblob.log
  (:import (java.util.concurrent Executors)))

(def svc (Executors/newFixedThreadPool 1))

(def logger-agent (agent nil))

(defn print-log [msg]
  (apply println msg))

(defn log [& msg]
  (send-via svc logger-agent
            (fn [_]
              (print-log msg))))


