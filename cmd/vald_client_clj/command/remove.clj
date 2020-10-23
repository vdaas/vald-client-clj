(ns vald-client-clj.command.remove
  (:require
   [clojure.tools.cli :as cli]
   [clojure.string :as string]
   [vald-client-clj.core :as vald]
   [vald-client-clj.util :as util]))

(def cli-options
  [["-h" "--help" :id :help?]
   ["-j" "--json" "read and write as json"
    :id :json?]
   [nil "--skip-strict-exist-check" "skip strict exist check"
    :id :skip-strict-exist-check?]
   [nil "--elapsed-time" "show elapsed time the request took"
    :id :elapsed-time?]])

(defn usage [summary]
  (->> ["Usage: valdcli [OPTIONS] remove [SUBOPTIONS] ID"
        ""
        "Remove single ID."
        ""
        "Sub Options:"
        summary
        ""]
       (string/join "\n")))

(defn run [client args]
  (let [parsed-result (cli/parse-opts args cli-options)
        {:keys [options summary arguments]} parsed-result
        {:keys [help? json? skip-strict-exist-check? elapsed-time?]} options
        writer (if json?
                 (comp println util/->json)
                 (comp println util/->edn))
        config {:skip-strict-exist-check skip-strict-exist-check?}
        id (first arguments)]
    (if (or help? (nil? id))
      (-> summary
          (usage)
          (println))
      (let [f (fn []
                (-> client
                    (vald/remove-id config id)
                    (writer)))]
        (if elapsed-time?
          (time (f))
          (f))))))
