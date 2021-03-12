(ns vald-client-clj.command.stream-upsert
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
  (->> ["Usage: valdcli [OPTIONS] stream-upsert [SUBOPTIONS] VECTORS"
        ""
        "Upsert multiple vectors."
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
        config {:skip-strict-exist-check skip-strict-exist-check?}]
    (if help?
      (-> summary
          (usage)
          (println))
      (let [vectors (-> (or (first arguments)
                            (util/read-from-stdin))
                        (read-string))
            f (fn []
                (-> client
                    (vald/stream-upsert writer config vectors)
                    (deref)))
            res (if elapsed-time?
                  (time (f))
                  (f))]
        (if (:error res)
          (throw (:error res))
          (->> res
               (:count)
               (str "upserted: ")
               (println)))))))
