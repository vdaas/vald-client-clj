(ns vald-client-clj.command.stream-search-by-id
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
    :default 0.01
    :parse-fn #(Float/parseFloat %)]
   ["-t" "--timeout TIMEOUT"
    :id :timeout
    :default 3000000000
    :parse-fn #(Integer/parseInt %)]])

(defn usage [summary]
  (->> ["Usage: valdcli [OPTIONS] stream-search-by-id [SUBOPTIONS] IDs"
        ""
        "Search vectors using multiple IDs."
        ""
        "Sub Options:"
        summary
        ""]
       (string/join "\n")))

(defn run [client args]
  (let [parsed-result (cli/parse-opts args cli-options)
        {:keys [options summary arguments]} parsed-result
        {:keys [help? json? num radius epsilon timeout]} options
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
      (let [ids (-> (or (first arguments)
                        (util/read-from-stdin))
                    (read-string))
            res (-> client
                    (vald/stream-search-by-id writer config ids)
                    (deref))]
        (when (:error res)
          (throw (:error res)))))))
