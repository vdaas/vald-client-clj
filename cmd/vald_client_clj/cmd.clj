(ns vald-client-clj.cmd
  (:require
   [clojure.tools.cli :as cli]
   [camel-snake-kebab.core :as csk]
   [vald-client-clj.core :as vald])
  (:gen-class))

(def cli-options
  [["-d" "--debug" :id :debug?]
   ["-h" "--help" :id :help?]])

(defn main
  [{:keys [options summary arguments] :as parsed-result}]
  (let [cmd (-> (first arguments)
                (csk/->kebab-case-keyword))
        host "localhost"
        port 8080
        client (vald/vald-client host port)
             #_(vald/agent-client host port)]
    (case cmd
      :exists (println :exists)
      :insert (println :insert)
      :stream-insert (println :stream-insert)
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
