(ns azureblob.core
  (:require [clojure.java.io :as io])
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

(defn upload [opts [cmd srcfile blobname]]
  (condp #(= %1 (keyword %2)) cmd
    :put
     (let [acct (CloudStorageAccount/parse
                 (connstr (:name opts) (:key opts)))
           client (.createCloudBlobClient acct)
           container (.getContainerReference client (:container opts))
           perms (BlobContainerPermissions.)
           blob (.getBlockBlobReference container blobname)
           fileref (io/file srcfile)]
       (.createIfNotExists container)
       (.uploadPermissions container perms)
       (.upload blob (io/input-stream fileref) (.length fileref)))
     (throw (Exception. (format "%s not implemented" cmd)))))

