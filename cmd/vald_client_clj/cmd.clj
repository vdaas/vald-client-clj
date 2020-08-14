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
   [vald-client-clj.command.stream-get-object :as command.stream-get-object]
   [vald-client-clj.command.create-index :as command.create-index]
   [vald-client-clj.command.save-index :as command.save-index]
   [vald-client-clj.command.create-and-save-index :as command.casi]
   [vald-client-clj.command.index-info :as command.index-info]
   [vald-client-clj.command.rand-vec :as command.rand-vec])
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
    :default "localhost"]
   ["-a" "--agent" "connect as an agent client" :id :agent?]])

(defmacro get-version []
  (System/getProperty "vald-client-clj.version"))

(defn usage [summary]
  (->> ["Usage: valdcli [OPTIONS] ACTION"
        ""
        "Options:"
        summary
        ""
        "Actions:"
        "  exists                Check whether ID exists or not."
        "  insert                Insert single vector."
        "  search                Search single vector."
        "  search-by-id          Search vectors using single ID."
        "  update                Update single vector."
        "  remove                Remove single ID."
        "  get-object            Get object info of single ID."
        "  stream-insert         Insert multiple vectors."
        "  stream-search         Search multiple vectors."
        "  stream-search-by-id   Search vectors using multiple IDs."
        "  stream-update         Update multiple vectors."
        "  stream-remove         Remove multiple IDs."
        "  stream-get-object     Get object info of multiple IDs."
        "  create-index          Call create-index command. (only for Agent)"
        "  save-index            Call save-index command. (only for Agent)"
        "  create-and-save-index Call create-and-save-index command. (only for Agent)"
        "  index-info            Fetch index info. (only for Agent)"
        "  rand-vec              Prints randomized vector."
        "  rand-vecs             Prints randomized vectors."
        ""]
       (string/join "\n")))

(defn run
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
          :create-index (command.create-index/run client args)
          :save-index (command.save-index/run client args)
          :create-and-save-index (command.casi/run client args)
          :index-info (command.index-info/run client args)
          :rand-vec (command.rand-vec/run-vec args)
          :rand-vecs (command.rand-vec/run-vecs args)
          (throw (Exception. "unknown subcommand")))))))

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
