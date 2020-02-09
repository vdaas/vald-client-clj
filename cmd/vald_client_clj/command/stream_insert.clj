(ns vald-client-clj.command.stream-insert
  (:require
   [clojure.tools.cli :as cli]
   [clojure.edn :as edn]
   [vald-client-clj.core :as vald]
   [vald-client-clj.util :as util]))

(def cli-options
  [["-h" "--help" :id :help?]
   ["-j" "--json" "read as json"
    :id :json?]])

(defn run [client args]
  (let [parsed-result (cli/parse-opts args cli-options)
        {:keys [options summary arguments]} parsed-result
        {:keys [help? json?]} options
        read-string (if json?
                      util/read-json
                      edn/read-string)]
    (if help?
      (println summary)
      (let [vectors (-> (or (first arguments)
                            (util/read-from-stdin))
                        (read-string))
            res (-> client
                    (vald/stream-insert println vectors)
                    (deref))]
        (if (:error res)
          (throw (:error res))
          (->> res
               (:count)
               (str "inserted: ")
               (println)))))))
