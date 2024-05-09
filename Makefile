XMS = 2g
XMX = 6g

REPO       ?= vdaas
NAME        = vald
VALDREPO    = github.com/$(REPO)/$(NAME)
LANGUAGE    = clj
PKGNAME     = $(NAME)-client-$(LANGUAGE)
PKGREPO     = github.com/$(REPO)/$(PKGNAME)

VALD_DIR    = vald
VALD_SHA    = VALD_SHA
VALD_CLIENT_CLJ_VERSION = VALD_CLIENT_CLJ_VERSION
VALD_CHECKOUT_REF ?= main

VERSION=$(shell cat VALD_CLIENT_CLJ_VERSION)

NATIVE_IMAGE_CONFIG_OUTPUT_DIR=native-config

TARGET_JAR=target/vald-client-clj-$(VERSION)-standalone.jar

TEST_DATASET_PATH = wordvecs1000.json

LEIN_PATH = ./lein

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

$(LEIN_PATH):
	curl -o lein https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein \
	&& chmod a+x lein \
	&& ./lein version

$(TARGET_JAR): $(LEIN_PATH) src cmd
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

$(VALD_DIR):
	git clone https://$(VALDREPO) $(VALD_DIR)

.PHONY: pom
## update dependencies
pom: $(LEIN_PATH)
	./lein pom

.PHONY: proto
## build proto
proto:
	@echo "Nothing to do"

.PHONY: vald/checkout
## checkout vald repository
vald/checkout: $(VALD_DIR)
	cd $(VALD_DIR) && git checkout $(VALD_CHECKOUT_REF)

.PHONY: vald/origin/sha/print
## print origin VALD_SHA value
vald/origin/sha/print: $(VALD_DIR)
	@cd $(VALD_DIR) && git rev-parse HEAD | tr -d '\n'

.PHONY: vald/sha/print
## print VALD_SHA value
vald/sha/print:
	@cat $(VALD_SHA)

.PHONY: vald/sha/update
## update VALD_SHA value
vald/sha/update: $(VALD_DIR)
	(cd $(VALD_DIR); git rev-parse HEAD | tr -d '\n' > ../$(VALD_SHA))

.PHONY: vald/client/version/print
## print VALD_CLIENT_JAVA_VERSION value
vald/client/version/print:
	@cat $(VALD_CLIENT_CLJ_VERSION)

.PHONY: vald/client/version/update
## update VALD_CLIENT_JAVA_VERSION value
vald/client/version/update: $(VALD_DIR)
	cp $(VALD_DIR)/versions/VALD_VERSION $(VALD_CLIENT_CLJ_VERSION)

.PHONY: test
## Execute test
test: $(LEIN_PATH) $(TEST_DATASET_PATH)
	./lein test

$(TEST_DATASET_PATH):
	curl -L https://raw.githubusercontent.com/rinx/word2vecjson/master/data/wordvecs1000.json -o $(TEST_DATASET_PATH)

.PHONY: ci/deps/install
## install deps for CI environment
ci/deps/install: $(LEIN_PATH)

.PHONY: ci/deps/update
## update deps for CI environment
ci/deps/update: pom

.PHONY: ci/package/prepare
## prepare for publich
ci/package/prepare: ci/deps/install

.PHONY: ci/package/publish
## publich packages
ci/package/publish: ci/deps/install
	./lein deploy clojars
