(defproject com.draines/azureblob "1.0.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [com.microsoft.windowsazure.storage/microsoft-windowsazure-storage-sdk "0.6.0"]
                 [org.clojure/tools.cli "0.3.1"]]
  :aot :all
  :main azureblob.main)
