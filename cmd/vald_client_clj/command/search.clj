(ns vald-client-clj.command.search
  (:require
   [clojure.tools.cli :as cli]
   [clojure.string :as string]
   [clojure.edn :as edn]
   [vald-client-clj.core :as vald]
   [vald-client-clj.util :as util]))

(def cli-options
  [["-h" "--help" :id :help?]
   ["-j" "--json" "read and write as json"
    :id :json?]
   [nil "--elapsed-time" "show elapsed time the request took"
    :id :elapsed-time?]
   ["-n" "--num NUM"
    :id :num
    :default 10
    :parse-fn #(Integer/parseInt %)]
   ["-r" "--radius RADIUS"
    :id :radius
    :default -1.0
    :parse-fn #(Float/parseFloat %)]
   ["-e" "--epsilon EPSILON"
    :id :epsilon
    :default 0.1
    :parse-fn #(Float/parseFloat %)]
   ["-t" "--timeout TIMEOUT"
    :id :timeout
    :default 3000000000
    :parse-fn #(Integer/parseInt %)]])

(defn usage [summary]
  (->> ["Usage: valdcli [OPTIONS] search [SUBOPTIONS] VECTOR"
        ""
        "Search single vector."
        ""
        "Sub Options:"
        summary
        ""]
       (string/join "\n")))

(defn run [client args]
  (let [parsed-result (cli/parse-opts args cli-options)
        {:keys [options summary arguments]} parsed-result
        {:keys [help? json? elapsed-time? num radius epsilon timeout]} options
        read-string (if json?
                      util/read-json
                      edn/read-string)
        writer (if json?
                 (comp println util/->json)
                 (comp println util/->edn))
        config {:num num
                :radius radius
                :epsilon epsilon
                :timeout timeout}]
    (if help?
      (-> summary
          (usage)
          (println))
      (let [vector (-> (or (first arguments)
                           (util/read-from-stdin))
                       (read-string))
            f (fn []
                (-> client
                    (vald/search config vector)
                    (writer)))]
        (if elapsed-time?
          (time (f))
          (f))))))
