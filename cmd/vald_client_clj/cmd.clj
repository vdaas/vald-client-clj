(ns vald-client-clj.cmd
  (:require
   [clojure.tools.cli :as cli]
   [clojure.string :as string]
   [camel-snake-kebab.core :as csk]
   [vald-client-clj.core :as vald]
   [vald-client-clj.client :as client]
   [vald-client-clj.repl :as repl])
  (:gen-class))

(set! *warn-on-reflection* true)

(def cli-options
  [[nil "--help" "show usage" :id :help?]
   [nil "--version" "show version" :id :version?]
   ["-d" "--debug" "debug mode" :id :debug?]
   ["-p" "--port PORT" "Port number"
    :id :port
    :default 8080
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 % 0x10000) "Must be a number between 0 and 65536"]]
   ["-h" "--host HOST" "Hostname"
    :id :host
    :default "localhost"]])

(defmacro get-version []
  (System/getProperty "vald-client-clj.version"))

(defn usage [summary]
  (->> ["Usage: valdcli [OPTIONS] ACTION"
        ""
        "Options:"
        summary
        ""
        "Actions:"
        client/usages
        ""]
       (string/join "\n")))

(defn run
  [{:keys [options summary arguments errors] :as parsed-result}]
  (let [{:keys [help? port host]} options
        cmd (first arguments)]
    (if (or help? (nil? cmd))
      (-> summary
          (usage)
          (println))
      (let [client-opts {:host host
                         :port port}
            cmd (csk/->kebab-case-keyword cmd)
            args (rest arguments)]
        (if (= cmd :repl)
          (repl/run client-opts args)
          (client/exec client-opts cmd args))))))

(defn main [{:keys [options] :as parsed-result}]
  (let [{:keys [version? debug?]} options]
    (if version?
      (println (get-version))
      (try
        (run parsed-result)
        (catch Exception e
          (when debug?
            (throw e))
          (.println System/err (.getMessage e))
          (System/exit 1))
        (finally
          (shutdown-agents))))))

(defn -main [& args]
  (-> args
      (cli/parse-opts cli-options
                      :in-order true)
      (main)))
