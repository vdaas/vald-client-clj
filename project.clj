(defproject vald-client-clj "0.1.0-SNAPSHOT"
  :description "A client library for Vald."
  :url "https://github.com/rinx/vald-client-clj"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [io.grpc/grpc-api "1.27.0"]
                 [io.grpc/grpc-core "1.27.0"
                  :exlusions [io.grpc/grpc-api]]
                 [io.grpc/grpc-protobuf "1.27.0"]
                 [io.grpc/grpc-stub "1.27.0"]
                 [org.vdaas.vald/vald-client-java "0.0.2"]]
  :repl-options {:init-ns vald-client-clj.core}
  :profiles {:dev
             {:dependencies [[io.grpc/grpc-okhttp "1.27.0"
                              :exclusions [io.grpc/grpc-core]]]}
             :cmd
             {:source-paths ["cmd"]
              :dependencies [[org.clojure/tools.cli "0.4.2"]
                             [io.grpc/grpc-okhttp "1.27.0"
                              :exclusions [io.grpc/grpc-core]]
                             [metosin/jsonista "0.2.2"]
                             [camel-snake-kebab "0.4.0"]]
              :plugins [[io.taylorwood/lein-native-image "0.3.1"]]
              :aot :all
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
                      "--initialize-at-build-time"
                      "--allow-incomplete-classpath"
                      "--verbose"
                      "-J-Xms1g"
                      "-J-Xmx6g"]
               :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}}
             :static
             {:source-paths ["cmd"]
              :dependencies [[org.clojure/tools.cli "0.4.2"]
                             [io.grpc/grpc-okhttp "1.27.0"
                              :exclusions [io.grpc/grpc-core]]
                             [metosin/jsonista "0.2.2"]
                             [camel-snake-kebab "0.4.0"]]
              :plugins [[io.taylorwood/lein-native-image "0.3.1"]]
              :aot :all
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
                      "--initialize-at-build-time"
                      "--allow-incomplete-classpath"
                      "--verbose"
                      "--static"
                      "-J-Xms1g"
                      "-J-Xmx6g"]
               :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}}})
