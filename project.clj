(defproject vald-client-clj #=(clojure.string/trim #=(slurp "VALD_CLIENT_CLJ_VERSION"))
  :description "A client library for Vald."
  :url "https://github.com/vdaas/vald-client-clj"
  :license {:name "Apache License 2.0"
            :url "https://www.apache.org/licenses/LICENSE-2.0"}
  :deploy-repositories [["clojars" {:sign-releases false
                                    :username :env/clojars_user
                                    :password :env/clojars_pass
                                    :url "https://clojars.org/repo"}]]
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [io.grpc/grpc-api "1.58.0"]
                 [io.grpc/grpc-core "1.58.0"
                  :exlusions [io.grpc/grpc-api]]
                 [io.grpc/grpc-protobuf "1.58.0"]
                 [io.grpc/grpc-stub "1.58.0"]
                 [build.buf/protovalidate "0.1.8"]
                 [org.vdaas.vald/vald-client-java
                  #=(subs
                      #=(clojure.string/trim
                          #=(slurp "VALD_CLIENT_CLJ_VERSION")) 1)]]
  :repl-options {:init-ns vald-client-clj.core}
  :profiles {:dev
             {:dependencies [[io.grpc/grpc-okhttp "1.58.0"
                              :exclusions [io.grpc/grpc-core]]
                             [metosin/jsonista "0.3.7"]
                             [camel-snake-kebab "0.4.3"]]}
             :cmd
             {:source-paths ["cmd"]
              :dependencies [[org.clojure/tools.cli "1.0.219"]
                             [io.grpc/grpc-okhttp "1.58.0"
                              :exclusions [io.grpc/grpc-core]]
                             [metosin/jsonista "0.3.7"]
                             [camel-snake-kebab "0.4.3"]]
              :aot :all
              :global-vars {*assert* false}
              :main vald-client-clj.cmd}})
