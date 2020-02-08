(ns vald-client-clj.cmd
  (:require
   [clojure.tools.cli :as cli]
   [clojure.string :as string]
   [camel-snake-kebab.core :as csk]
   [vald-client-clj.core :as vald]
   [vald-client-clj.command.exists :as command.exists]
   [vald-client-clj.command.insert :as command.insert]
   [vald-client-clj.command.stream-insert :as command.stream-insert]
   [vald-client-clj.command.search :as command.search]
   [vald-client-clj.command.stream-search :as command.stream-search]
   [vald-client-clj.command.search-by-id :as command.search-by-id]
   [vald-client-clj.command.stream-search-by-id :as command.stream-search-by-id]
   [vald-client-clj.command.update :as command.update]
   [vald-client-clj.command.stream-update :as command.stream-update]
   [vald-client-clj.command.remove :as command.remove]
   [vald-client-clj.command.stream-remove :as command.stream-remove]
   [vald-client-clj.command.get-object :as command.get-object]
   [vald-client-clj.command.stream-get-object :as command.stream-get-object])
  (:gen-class))

(set! *warn-on-reflection* true)

(def cli-options
  [[nil "--help" "show usage" :id :help?]
   ["-p" "--port PORT" "Port number"
    :id :port
    :default 8080
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 % 0x10000) "Must be a number between 0 and 65536"]]
   ["-h" "--host HOST" "Hostname"
    :id :host
    :default "localhost"]
   ["-a" "--agent" "connect as an agent client" :id :agent?]])

(defn usage [summary]
  (->> ["Usage: valdcli [OPTIONS] ACTION"
        ""
        "Options:"
        summary
        ""
        "Actions:"
        "  exists"
        "  insert"
        "  search"
        "  search-by-id"
        "  update"
        "  remove"
        "  get-object"
        "  stream-insert"
        "  stream-search"
        "  stream-search-by-id"
        "  stream-update"
        "  stream-remove"
        "  stream-get-object"
        ""]
       (string/join "\n")))

(defn main
  [{:keys [options summary arguments errors] :as parsed-result}]
  (let [{:keys [help? port host agent?]} options
        cmd (first arguments)]
    (if (or help? (nil? cmd))
      (-> summary
          (usage)
          (println))
      (let [args (rest arguments)
            client (if agent?
                     (vald/agent-client host port)
                     (vald/vald-client host port))]
        (case (csk/->kebab-case-keyword cmd)
          :exists (command.exists/run client args)
          :insert (command.insert/run client args)
          :stream-insert (command.stream-insert/run client args)
          :search (command.search/run client args)
          :stream-search (command.stream-search/run client args)
          :search-by-id (command.search-by-id/run client args)
          :stream-search-by-id (command.stream-search-by-id/run client args)
          :update (command.update/run client args)
          :stream-update (command.stream-update/run client args)
          :remove (command.remove/run client args)
          :stream-remove (command.stream-remove/run client args)
          :get-object (command.get-object/run client args)
          :stream-get-object (command.stream-get-object/run client args)
          (throw (Exception. "unknown subcommand")))))))

(defn -main [& args]
  (try
    (-> args
        (cli/parse-opts cli-options
                        :in-order true)
        (main))
    (catch Exception e
      (.println System/err (.getMessage e))
      (System/exit 1))
    (finally
      (shutdown-agents))))
