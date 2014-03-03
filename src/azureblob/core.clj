(ns azureblob.core
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [azureblob.log :as log])
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

(defn make-key [path file]
  (let [relpath (string/replace-first (str file) #"^[/ ]+" "")]
    (string/replace-first (str (io/file path relpath)) #"^/+" "")))

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
    (let [remote-path (make-key blobpath file)
          blob (.getBlockBlobReference container remote-path)]
      (merge {:file (str file)
              :blob (str (.getName container) "/" remote-path)}
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
                (log/log "error" (:blob report))
                (update-in m [:error] (fnil inc 0)))
              (do
                (log/log (if (:took report)
                           (format "%.2fKbps" (kbps
                                             (:length report) (:took report)))
                           "skip")
                         (:blob report))
                (merge-with + m (dissoc report :file :blob)))))]
    (reduce f {} (upload-seq (files src) container blobpath))))

(defn put-file [src container blobname]
  (if (.exists src)
    (let [blob (.getBlockBlobReference container blobname)
          len (.length src)
          r (timed
             (.upload blob (io/input-stream src) len))]
      (when (:error r)
        (log/log "error" blobname (:error r)))
      (merge r {:file blobname}))))

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
       (let [fileref (io/file srcfile)
             r (if (.isDirectory fileref)
                 (sync-dir fileref container blobname)
                 (put-file fileref container blobname))]
         (assoc r :kbps (kbps (:length r) (:took r)))))
     (throw (Exception. (format "%s not implemented" cmd)))))

