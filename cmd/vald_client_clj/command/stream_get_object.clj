(ns vald-client-clj.command.stream-get-object
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
        {:keys [help?]} options]
    (if help?
      (println summary)
      (let [ids (-> (or (first arguments)
                        (util/read-from-stdin))
                    (edn/read-string))]
        (-> client
            (vald/stream-get-object ids)
            (println))))))
