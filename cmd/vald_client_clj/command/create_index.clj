(ns vald-client-clj.command.create-index
  (:require
   [clojure.tools.cli :as cli]
   [clojure.string :as string]
   [vald-client-clj.core :as vald]))

(def cli-options
  [["-h" "--help" :id :help?]
   [nil "--elapsed-time" "show elapsed time the request took"
    :id :elapsed-time?]])

(defn usage [summary]
  (->> ["Usage: valdcli [OPTIONS] create-index [SUBOPTIONS] POOL_SIZE"
        ""
        "Call create-index command."
        "This functionality is only for Agents."
        ""
        "Sub Options:"
        summary
        ""]
       (string/join "\n")))

(defn run [client args]
  (let [parsed-result (cli/parse-opts args cli-options)
        {:keys [options summary arguments]} parsed-result
        {:keys [help? elapsed-time?]} options
        pool-size (or (first arguments)
                      10000)]
    (if help?
      (-> summary
          (usage)
          (println))
      (let [f (fn []
                (-> client
                    (vald/create-index pool-size)))]
        (if elapsed-time?
          (time (f))
          (f))
        (println "create-index called.")))))
