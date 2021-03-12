(ns vald-client-clj.command.upsert
  (:require
   [clojure.tools.cli :as cli]
   [clojure.string :as string]
   [clojure.edn :as edn]
   [vald-client-clj.core :as vald]
   [vald-client-clj.util :as util]))

(def cli-options
  [["-h" "--help" :id :help?]
   ["-j" "--json" "read as json"
    :id :json?]
   [nil "--skip-strict-exist-check" "skip strict exist check"
    :id :skip-strict-exist-check?]
   [nil "--elapsed-time" "show elapsed time the request took"
    :id :elapsed-time?]])

(defn usage [summary]
  (->> ["Usage: valdcli [OPTIONS] upsert [SUBOPTIONS] ID VECTOR"
        ""
        "Upsert single vector."
        ""
        "Sub Options:"
        summary
        ""]
       (string/join "\n")))

(defn run [client args]
  (let [parsed-result (cli/parse-opts args cli-options)
        {:keys [options summary arguments]} parsed-result
        {:keys [help? json? skip-strict-exist-check? elapsed-time?]} options
        read-string (if json?
                      util/read-json
                      edn/read-string)
        writer (if json?
                 (comp println util/->json)
                 (comp println util/->edn))
        config {:skip-strict-exist-check skip-strict-exist-check?}
        id (first arguments)]
    (if (or help? (nil? id))
      (-> summary
          (usage)
          (println))
      (let [vector (-> (or (second arguments)
                           (util/read-from-stdin))
                       (read-string))
            f (fn []
                (-> client
                    (vald/upsert config id vector)
                    (writer)))]
        (if elapsed-time?
          (time (f))
          (f))))))
