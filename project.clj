(defproject vald-client-clj #=(clojure.string/trim #=(slurp "VALD_CLIENT_CLJ_VERSION"))
  :description "A client library for Vald."
  :url "https://github.com/vdaas/vald-client-clj"
  :license {:name "Apache License 2.0"
            :url "https://www.apache.org/licenses/LICENSE-2.0"}
  :deploy-repositories [["clojars" {:sign-releases false
                                    :username :env/clojars_user
                                    :password :env/clojars_pass
                                    :url "https://clojars.org/repo"}]]
  :dependencies [[org.clojure/clojure "1.10.2-alpha1"]
                 [io.grpc/grpc-api "1.27.0"]
                 [io.grpc/grpc-core "1.27.0"
                  :exlusions [io.grpc/grpc-api]]
                 [io.grpc/grpc-protobuf "1.27.0"]
                 [io.grpc/grpc-stub "1.27.0"]
                 [org.vdaas.vald/vald-client-java
                  #=(subs
                      #=(clojure.string/trim
                          #=(slurp "VALD_CLIENT_CLJ_VERSION")) 1)]]
  :repl-options {:init-ns vald-client-clj.core}
  :profiles {:dev
             {:dependencies [[io.grpc/grpc-okhttp "1.27.0"
                              :exclusions [io.grpc/grpc-core]]]}
             :cmd
             {:source-paths ["cmd"]
              :dependencies [[org.clojure/tools.cli "0.4.2"]
                             [io.grpc/grpc-okhttp "1.27.0"
                              :exclusions [io.grpc/grpc-core]]
                             [metosin/jsonista "0.2.5"]
                             [camel-snake-kebab "0.4.0"]]
              :plugins [[io.taylorwood/lein-native-image "0.3.1"]]
              :aot :all
              :global-vars {*assert* false}
              :main vald-client-clj.cmd
              :native-image
              {:name "valdcli"
               :opts ["-H:+ReportExceptionStackTraces"
                      "-H:Log=registerResource:"
                      "-H:ConfigurationFileDirectories=native-config"
                      "--enable-url-protocols=http,https"
                      "--enable-all-security-services"
                      "--no-fallback"
                      "--no-server"
                      "--report-unsupported-elements-at-runtime"
                      "--initialize-at-run-time=java.lang.Math$RandomNumberGeneratorHolder"
                      "--initialize-at-build-time"
                      "--allow-incomplete-classpath"
                      "--verbose"
                      "-J-Dclojure.compiler.direct-linking=true"
                      "-J-Dclojure.spec.skip-macros=true"
                      "-J-Xms1g"
                      "-J-Xmx6g"]
               :jvm-opts ["-Dclojure.compiler.direct-linking=true"
                          "-Dclojure.spec.skip-macros=true"]}}
             :static
             {:source-paths ["cmd"]
              :dependencies [[org.clojure/tools.cli "0.4.2"]
                             [io.grpc/grpc-okhttp "1.27.0"
                              :exclusions [io.grpc/grpc-core]]
                             [metosin/jsonista "0.2.5"]
                             [camel-snake-kebab "0.4.0"]]
              :plugins [[io.taylorwood/lein-native-image "0.3.1"]]
              :aot :all
              :global-vars {*assert* false}
              :main vald-client-clj.cmd
              :native-image
              {:name "valdcli"
               :opts ["-H:+ReportExceptionStackTraces"
                      "-H:Log=registerResource:"
                      "-H:ConfigurationFileDirectories=native-config"
                      "--enable-url-protocols=http,https"
                      "--enable-all-security-services"
                      "--no-fallback"
                      "--no-server"
                      "--report-unsupported-elements-at-runtime"
                      "--initialize-at-run-time=java.lang.Math$RandomNumberGeneratorHolder"
                      "--initialize-at-build-time"
                      "--allow-incomplete-classpath"
                      "--verbose"
                      "--static"
                      "-J-Dclojure.compiler.direct-linking=true"
                      "-J-Dclojure.spec.skip-macros=true"
                      "-J-Xms1g"
                      "-J-Xmx6g"]
               :jvm-opts ["-Dclojure.compiler.direct-linking=true"
                          "-Dclojure.spec.skip-macros=true"]}}})
