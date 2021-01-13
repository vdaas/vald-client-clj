(ns vald-client-clj.repl
  (:require
   [clojure.tools.cli :as cli]
   [clojure.string :as string]
   [camel-snake-kebab.core :as csk]
   [vald-client-clj.client :as client]))

(def cli-options
  [["-h" "--help" :id :help?]])

(defn usage [summary]
  (->> ["Usage: valdcli [OPTIONS] repl [SUBOPTIONS]"
        ""
        "Start REPL mode."
        ""
        "Sub Options:"
        summary
        ""]
       (string/join "\n")))

(defn prheader []
  (println "valdcli REPL.")
  (println "Use exit or quit to quit the REPL.")
  (println "To show help, use help command."))

(defn prprompt []
  (print (str "=>" " "))
  (flush))

(defn repl-help []
  (->> ["REPL Commands:"
        "  exit       Quit the REPL."
        "  quit       Quit the REPL."
        "  help       Show this message."
        "  reconnect  Reconnect to the host."
        "Available Commands:"
        client/usages
        ""]
       (string/join "\n")))

(defn tokenize [ss]
  ss)

(defn repl [client-fn]
  (prheader)
  (let [client (atom (client-fn))]
    (loop []
      (prprompt)
      (let [cmd-args (-> (read-line)
                         (string/trim)
                         (string/split #"\s+"))
            cmd (-> cmd-args
                    (first)
                    (csk/->kebab-case-keyword))
            args (->> (rest cmd-args)
                      (tokenize))]
        (case cmd
          :exit :ok
          :quit :ok
          (do
            (case cmd
              :help (println (repl-help))
              :reconnect (reset! client (client-fn))
              (let [client? (get-in client/cmds [cmd :client?])
                    cli-fn (get-in client/cmds [cmd :cli-fn])]
                (try
                  (if cli-fn
                    (if client?
                      (cli-fn @client args)
                      (cli-fn args))
                    (println "unknown command:" (name cmd)))
                  (catch Exception e
                    (println "command failed with error:" e)))))
            (recur)))))))

(defn run [{:keys [host port agent?]} args]
  (let [parsed-result (cli/parse-opts args cli-options)
        {:keys [options summary arguments]} parsed-result
        {:keys [help?]} options]
    (if help?
      (-> summary
          (usage)
          (println))
      (let [cfn (client/client-fn host port agent?)
            client-fn (fn []
                        (let [client (cfn)]
                          (println "connected to" (str host ":" port))
                          client))]
        (repl client-fn)))))
