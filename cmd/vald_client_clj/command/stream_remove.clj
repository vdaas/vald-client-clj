(ns vald-client-clj.command.stream-remove
  (:require
   [clojure.tools.cli :as cli]
   [vald-client-clj.core :as vald]))

(def cli-options
  [["-d" "--debug" :id :debug?]
   ["-h" "--help" :id :help?]])
