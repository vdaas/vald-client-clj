(ns vald-client-clj.command.rand-vec
  (:require
   [clojure.tools.cli :as cli]
   [clojure.string :as string]
   [vald-client-clj.util :as util])
  (:import
   [java.util UUID]))

(def cli-options-vec
  [["-h" "--help" :id :help?]
   ["-j" "--json" "write value as json"
    :id :json?]
   ["-d" "--dimension DIMENSION" "dimension"
    :id :dimension
    :default 10
    :parse-fn #(Integer/parseInt %)]])

(def cli-options-vecs
  [["-h" "--help" :id :help?]
   [nil "--with-ids" "generate with uuids"
    :id :with-ids?]
   ["-j" "--json" "write values as json"
    :id :json?]
   ["-n" "--number NUMBER" "number of generated vecotrs"
    :id :number
    :default 10
    :parse-fn #(Integer/parseInt %)]
   ["-d" "--dimension DIMENSION" "dimension"
    :id :dimension
    :default 10
    :parse-fn #(Integer/parseInt %)]])

(defn usage-vec [summary]
  (->> ["Usage: valdcli [OPTIONS] rand-vec [SUBOPTIONS]"
        ""
        "Prints randomized vector."
        ""
        "Sub Options:"
        summary
        ""]
       (string/join "\n")))

(defn usage-vecs [summary]
  (->> ["Usage: valdcli [OPTIONS] rand-vecs [SUBOPTIONS]"
        ""
        "Prints randomized vectors."
        ""
        "Sub Options:"
        summary
        ""]
       (string/join "\n")))

(defn run-vec [args]
  (let [parsed-result (cli/parse-opts args cli-options-vec)
        {:keys [options summary]} parsed-result
        {:keys [help? json? number dimension]} options]
    (if help?
      (-> summary
          (usage-vec)
          (println))
      (let [vector (vec (take dimension (repeatedly rand)))
            writer (if json?
                     (comp println util/->json)
                     (comp println util/->minified-edn))]
        (-> vector
            (writer))))))

(defn run-vecs [args]
  (let [parsed-result (cli/parse-opts args cli-options-vecs)
        {:keys [options summary]} parsed-result
        {:keys [help? with-ids? json? number dimension]} options]
    (if help?
      (-> summary
          (usage-vecs)
          (println))
      (let [generate (if with-ids?
                       (fn [] {:id (-> (UUID/randomUUID)
                                       (.toString))
                               :vector (vec (take dimension (repeatedly rand)))})
                       (fn [] (vec (take dimension (repeatedly rand)))))
            vectors (vec (take number (repeatedly generate)))
            writer (if json?
                     (comp println util/->json)
                     (comp println util/->minified-edn))]
        (-> vectors
            (writer))))))
