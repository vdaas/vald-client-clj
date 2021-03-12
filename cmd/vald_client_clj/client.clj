(ns vald-client-clj.client
  (:require
   [clojure.string :as string]
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
   [vald-client-clj.command.upsert :as command.upsert]
   [vald-client-clj.command.stream-upsert :as command.stream-upsert]
   [vald-client-clj.command.remove :as command.remove]
   [vald-client-clj.command.stream-remove :as command.stream-remove]
   [vald-client-clj.command.get-object :as command.get-object]
   [vald-client-clj.command.stream-get-object :as command.stream-get-object]
   [vald-client-clj.command.create-index :as command.create-index]
   [vald-client-clj.command.save-index :as command.save-index]
   [vald-client-clj.command.create-and-save-index :as command.casi]
   [vald-client-clj.command.index-info :as command.index-info]
   [vald-client-clj.command.rand-vec :as command.rand-vec]))

(def cmds
  {:exists
   {:description "Check whether ID exists or not."
    :client? true
    :cli-fn command.exists/run}
   :insert
   {:description "Insert single vector."
    :client? true
    :cli-fn command.insert/run}
   :stream-insert
   {:description "Insert multiple vectors."
    :client? true
    :cli-fn command.stream-insert/run}
   :search
   {:description "Search single vector."
    :client? true
    :cli-fn command.search/run}
   :stream-search
   {:description "Search multiple vectors."
    :client? true
    :cli-fn command.stream-search/run}
   :search-by-id
   {:description "Search vectors using single ID."
    :client? true
    :cli-fn command.search-by-id/run}
   :stream-search-by-id
   {:description "Search vectors using multiple IDs."
    :client? true
    :cli-fn command.stream-search-by-id/run}
   :update
   {:description "Update single vector."
    :client? true
    :cli-fn command.update/run}
   :stream-update
   {:description "Update multiple vectors."
    :client? true
    :cli-fn command.stream-update/run}
   :upsert
   {:description "Upsert single vector."
    :client? true
    :cli-fn command.upsert/run}
   :stream-upsert
   {:description "Upsert multiple vectors."
    :client? true
    :cli-fn command.stream-upsert/run}
   :remove
   {:description "Remove single ID."
    :client? true
    :cli-fn command.remove/run}
   :stream-remove
   {:description "Remove multiple IDs."
    :client? true
    :cli-fn command.stream-remove/run}
   :get-object
   {:description "Get object info of single ID."
    :client? true
    :cli-fn command.get-object/run}
   :stream-get-object
   {:description "Get object info of multiple IDs."
    :client? true
    :cli-fn command.stream-get-object/run}
   :create-index
   {:description "Call create-index command."
    :agent? true
    :client? true
    :cli-fn command.create-index/run}
   :save-index
   {:description "Call save-index command."
    :agent? true
    :client? true
    :cli-fn command.save-index/run}
   :create-and-save-index
   {:description "Call create-and-save-index command."
    :agent? true
    :client? true
    :cli-fn command.casi/run}
   :index-info
   {:description "Fetch index info."
    :agent? true
    :client? true
    :cli-fn command.index-info/run}
   :rand-vec
   {:description "Prints randomized vector."
    :client? false
    :cli-fn command.rand-vec/run-vec}
   :rand-vecs
   {:description "Prints randomized vectors."
    :client? false
    :cli-fn command.rand-vec/run-vecs}})

(def usages
  (let [max-len (->> cmds
                     (keys)
                     (map name)
                     (map count)
                     (apply max))
        fmt-str (str "  %-" max-len "s")
        ->line (fn [[k v]]
                 (let [k (name k)
                       agent? (get v :agent?)
                       desc (get v :description)
                       desc' (if agent?
                               (str desc " (only for Agent)")
                               desc)]
                   (str (format fmt-str k) " " desc')))]
    (->> cmds
         (mapv ->line)
         (sort)
         (string/join "\n"))))

(defn client-fn [host port]
  (fn []
    (vald/vald-client host port)))

(defn exec [{:keys [host port]} cmd args]
  (let [client-fn (client-fn host port)
        client? (get-in cmds [cmd :client?])
        cli-fn (get-in cmds [cmd :cli-fn])]
    (if cli-fn
      (if client?
        (cli-fn (client-fn) args)
        (cli-fn args))
      (throw (Exception. "unknown subcommand")))))
