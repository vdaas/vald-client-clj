XMS = 2g
XMX = 6g

VERSION=$(shell cat VALD_CLIENT_CLJ_VERSION)

NATIVE_IMAGE_CONFIG_OUTPUT_DIR=native-config

TARGET_JAR=target/vald-client-clj-$(VERSION)-standalone.jar

.PHONY: all
all: clean

.PHONY: clean
clean:
	rm -rf target valdcli

.PHONY: uberjar
uberjar: $(TARGET_JAR)

.PHONY: install/native-image
install/native-image:
	gu install native-image

.PHONY: profile/native-image-config
profile/native-image-config: \
	$(NATIVE_IMAGE_CONFIG_OUTPUT_DIR) \
	$(TARGET_JAR)
	- java -agentlib:native-image-agent=config-merge-dir=$(NATIVE_IMAGE_CONFIG_OUTPUT_DIR) \
	    -jar $(TARGET_JAR) exists test
	- java -agentlib:native-image-agent=config-merge-dir=$(NATIVE_IMAGE_CONFIG_OUTPUT_DIR) \
	    -jar $(TARGET_JAR) insert test "[0.1 0.2 0.3 0.4 0.5 0.6]"
	- java -agentlib:native-image-agent=config-merge-dir=$(NATIVE_IMAGE_CONFIG_OUTPUT_DIR) \
	    -jar $(TARGET_JAR) update test "[0.1 0.2 0.3 0.4 0.5 0.6]"
	- java -agentlib:native-image-agent=config-merge-dir=$(NATIVE_IMAGE_CONFIG_OUTPUT_DIR) \
	    -jar $(TARGET_JAR) search "[0.1 0.2 0.3 0.4 0.5 0.6]"
	- java -agentlib:native-image-agent=config-merge-dir=$(NATIVE_IMAGE_CONFIG_OUTPUT_DIR) \
	    -jar $(TARGET_JAR) search-by-id test
	- java -agentlib:native-image-agent=config-merge-dir=$(NATIVE_IMAGE_CONFIG_OUTPUT_DIR) \
	    -jar $(TARGET_JAR) remove test
	- java -agentlib:native-image-agent=config-merge-dir=$(NATIVE_IMAGE_CONFIG_OUTPUT_DIR) \
	    -jar $(TARGET_JAR) get-object test

$(NATIVE_IMAGE_CONFIG_OUTPUT_DIR):
	mkdir -p $@

lein:
	curl -o lein https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein \
	&& chmod a+x lein \
	&& ./lein version

$(TARGET_JAR): src cmd
	lein with-profile +cmd uberjar

valdcli: $(TARGET_JAR)
	native-image \
	-jar $(TARGET_JAR) \
	-H:Name=valdcli \
	-H:+ReportExceptionStackTraces \
	-H:Log=registerResource: \
	-H:ConfigurationFileDirectories=native-config \
	-H:+RemoveSaturatedTypeFlows \
	--enable-http \
	--enable-https \
	--enable-all-security-services \
	--no-fallback \
	--no-server \
	--report-unsupported-elements-at-runtime \
	--initialize-at-build-time \
	--allow-incomplete-classpath \
	--verbose \
	$(OPTS) \
	-J-Dclojure.compiler.direct-linking=true \
	-J-Dclojure.spec.skip-macros=true \
	-J-Xms$(XMS) \
	-J-Xmx$(XMX)
