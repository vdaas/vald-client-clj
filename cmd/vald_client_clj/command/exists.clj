(ns vald-client-clj.command.exists
  (:require
   [clojure.tools.cli :as cli]
   [vald-client-clj.core :as vald]))

(def cli-options
  [["-d" "--debug" :id :debug?]
   ["-h" "--help" :id :help?]])
