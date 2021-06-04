(ns vald-client-clj.command.search-by-id
  (:require
   [clojure.tools.cli :as cli]
   [clojure.string :as string]
   [vald-client-clj.core :as vald]
   [vald-client-clj.util :as util]))

(def cli-options
  [["-h" "--help" :id :help?]
   ["-j" "--json" "write as json"
    :id :json?]
   [nil "--elapsed-time" "show elapsed time the request took"
    :id :elapsed-time?]
   ["-n" "--num NUMBER"
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
  (->> ["Usage: valdcli [OPTIONS] search-by-id [SUBOPTIONS] ID"
        ""
        "Search vectors using single ID."
        ""
        "Sub Options:"
        summary
        ""]
       (string/join "\n")))

(defn run [client args]
  (let [parsed-result (cli/parse-opts args cli-options)
        {:keys [options summary arguments]} parsed-result
        {:keys [help? json? elapsed-time? num radius epsilon timeout]} options
        writer (if json?
                 (comp println util/->json)
                 (comp println util/->edn))
        config {:num num
                :radius radius
                :epsilon epsilon
                :timeout timeout}
        id (first arguments)]
    (if help?
      (-> summary
          (usage)
          (println))
      (let [f (fn []
                (-> client
                    (vald/search-by-id config id)
                    (writer)))]
        (if elapsed-time?
          (time (f))
          (f))))))
