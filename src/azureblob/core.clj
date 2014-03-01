(ns azureblob.core
  (:require [clojure.java.io :as io]
            [clojure.string :as string])
  (:import (com.microsoft.windowsazure.storage CloudStorageAccount)
           (com.microsoft.windowsazure.storage.blob CloudBlobClient
                                                    CloudBlobContainer
                                                    BlobContainerPermissions)))

(defn connstr [name key]
  (format
   (str "DefaultEndpointsProtocol=http;"
        "AccountName=%s;"
        "AccountKey=%s")
   name key))

(defn kbps [bytes ms]
  (if (and (number? bytes)
           (number? ms)
           (pos? ms))
    (let [secs (float (/ ms 1000))]
      (/ (/ bytes secs) 1024))
    0))

(defn files [path]
  (filter #(.isFile %1) (file-seq (io/file path))))

(defn make-path [path file]
  (let [relpath (string/replace-first (str file) #"^/+" "")]
    (str (io/file path relpath))))

(defmacro timed [& body]
  `(let [start# (System/currentTimeMillis)
         res# (try
                ~@body
                (catch Exception e#
                  e#))
         took# (- (System/currentTimeMillis) start#)]
     {:result res#
      :error (instance? Exception res#)
      :took took#}))

(defn upload-seq [files container blobpath]
  (for [file files]
    (let [remote-path (make-path blobpath file)
          blob (.getBlockBlobReference container remote-path)]
      (merge {:file (str file)
              :blob (str (.getName container) remote-path)}
             (if-not (.exists blob)
               (let [len (.length file)
                     result (timed
                             (.upload blob (io/input-stream file) len))]
                 (if-not (:error result)
                   {:length len
                    :took (:took result)})))))))

(defn sync-dir [src container blobpath]
  (let [f (fn [m report]
            (if (:error report)
              (do
                (println (:blob report) "error")
                (update-in m [:error] (fnil inc 0)))
              (do
                (println (:blob report)
                         (if (:took report)
                           (format "%sKbps" (kbps
                                             (:length report) (:took report)))
                           "skip"))
                (merge-with + m (dissoc report :file :blob)))))]
    (reduce f {} (upload-seq (files src) container blobpath))))

(defn put-file [src container blobname]
  (if (.exists src)
    (let [blob (.getBlockBlobReference container blobname)
          ]
      (.upload blob (io/input-stream src) (.length src)))))

(defn upload [opts [cmd srcfile blobname]]
  (condp #(= %1 (keyword %2)) cmd
    :put
     (let [acct (CloudStorageAccount/parse
                 (connstr (:name opts) (:key opts)))
           client (.createCloudBlobClient acct)
           container (.getContainerReference client (:container opts))
           perms (BlobContainerPermissions.)]
       (.createIfNotExists container)
       (.uploadPermissions container perms)
       (let [fileref (io/file srcfile)]
         (if (.isDirectory fileref)
           (sync-dir fileref container blobname)
           (put-file fileref container blobname))))
     (throw (Exception. (format "%s not implemented" cmd)))))

