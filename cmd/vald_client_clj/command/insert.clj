(ns vald-client-clj.command.insert
  (:require
   [clojure.tools.cli :as cli]
   [clojure.edn :as edn]
   [vald-client-clj.core :as vald]
   [vald-client-clj.util :as util]))

(def cli-options
  [["-h" "--help" :id :help?]])

(defn run [client args]
  (let [parsed-result (cli/parse-opts args cli-options)
        {:keys [options summary arguments]} parsed-result
        {:keys [help?]} options
        id (first arguments)]
    (if (or help? (nil? id))
      (println summary)
      (let [vector (-> (or (second arguments)
                           (util/read-from-stdin))
                       (edn/read-string))]
        (vald/insert client id vector)))))
