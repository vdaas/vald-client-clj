(ns vald-client-clj.cmd
  (:require
   [clojure.tools.cli :as cli]
   [vald-client-clj.core :as vald])
  (:gen-class))

(def cli-options
  [["-d" "--debug" :id :debug?]
   ["-h" "--help" :id :help?]])

(defn main
  [{:keys [options summary arguments] :as parsed-result}]
  (let [cmd (first arguments)]
    (case cmd
      :exists (println :exists)
      :insert (println :insert)
      (throw (Exception. "unknown subcommand")))))

(defn -main [& args]
  (try
    (-> args
        (cli/parse-opts cli-options
                        :in-order true)
        (main))
    (catch Exception e
      (.println *err* (.getMessage e))
      (System/exit 1))
    (finally
      (shutdown-agents))))
