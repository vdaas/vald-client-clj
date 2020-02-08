XMS = 2g
XMX = 6g

NATIVE_IMAGE_CONFIG_OUTPUT_DIR=native-config

TARGET_JAR=target/vald-client-clj-0.1.0-SNAPSHOT-standalone.jar

.PHONY: all
all: clean

.PHONY: clean
clean:
	rm -rf target

.PHONY: uberjar
uberjar: $(TARGET_JAR)

.PHONY: valdcli
valdcli: target/valdcli

.PHONY: install/native-image
install/native-image:
	gu install native-image

.PHONY: profile/native-image-config
profile/native-image-config: \
	$(NATIVE_IMAGE_CONFIG_OUTPUT_DIR) \
	$(TARGET_JAR)
	- java -agentlib:native-image-agent=config-output-dir=$(NATIVE_IMAGE_CONFIG_OUTPUT_DIR) \
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

target/valdcli: src cmd
	lein with-profile +cmd native-image
