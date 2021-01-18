(ns vald-client-clj.core
  (:refer-clojure :exclude [update remove])
  (:import
   [org.vdaas.vald.api.v1.vald
    InsertGrpc SearchGrpc UpdateGrpc RemoveGrpc UpsertGrpc ObjectGrpc]
   [org.vdaas.vald.api.v1.agent.core AgentGrpc]
   [org.vdaas.vald.api.v1.errors Errors$RPC]
   [org.vdaas.vald.api.v1.payload
    Insert$Request Insert$MultiRequest Insert$Config
    Search$Request Search$IDRequest Search$Config
    Search$MultiRequest Search$MultiIDRequest
    Search$Response Search$Responses
    Search$StreamResponse Search$StreamResponse$PayloadCase
    Update$Request Update$MultiRequest Update$Config
    Remove$Request Remove$MultiRequest Remove$Config
    Upsert$Request Upsert$MultiRequest Upsert$Config
    Object$ID Object$IDs
    Object$Vector Object$Vectors
    Object$Location Object$Locations
    Object$Distance
    Object$StreamLocation Object$StreamLocation$PayloadCase
    Object$StreamVector Object$StreamVector$PayloadCase
    Filter$Config
    Control$CreateIndexRequest
    Info$Index$Count
    Empty]
   [io.grpc ManagedChannelBuilder]
   [io.grpc.stub StreamObserver]))

(defn ->filter-config [config]
  (-> (Filter$Config/newBuilder)
      (.setAllTargets (:targets config))
      (.build)))

(defn ->search-config [config]
  (-> (Search$Config/newBuilder)
      (.setNum (or (:num config) 10))
      (.setRadius (or (:radius config) -1.0))
      (.setEpsilon (or (:epsilon config) 0.01))
      (.setTimeout (or (:timeout config) 3000000000))
      (cond->
        (:filters config) (.setFilters
                            (->filter-config (:filters config))))
      (.build)))

(defn ->insert-config [config]
  (-> (Insert$Config/newBuilder)
      (.setSkipStrictExistCheck (or (:skip-strict-exist-check config) false))
      (cond->
        (:filters config) (.setFilters
                            (->filter-config (:filters config))))
      (.build)))

(defn ->update-config [config]
  (-> (Update$Config/newBuilder)
      (.setSkipStrictExistCheck (or (:skip-strict-exist-check config) false))
      (cond->
        (:filters config) (.setFilters
                            (->filter-config (:filters config))))
      (.build)))

(defn ->remove-config [config]
  (-> (Remove$Config/newBuilder)
      (.setSkipStrictExistCheck (or (:skip-strict-exist-check config) false))
      (.build)))

(defn empty->map [e]
  (when (instance? Empty e)
    {}))

(defn object-id->map [id]
  {:id (.getId id)})

(defn object-distance->map [dist]
  {:id (.getId dist)
   :distance (.getDistance dist)})

(defn object-vector->map [obj-vec]
  {:id (.getId obj-vec)
   :vector (seq (.getVectorList obj-vec))})

(defn object-location->map [obj-loc]
  {:name (.getName obj-loc)
   :uuid (.getUuid obj-loc)
   :ips (seq (.getIpsList obj-loc))})

(defn object-locations->map [obj-locs]
  (->> (.getLocationsList obj-locs)
       (mapv object-location->map)))

(defn parse-search-response [res]
  (-> res
      (.getResultsList)
      (->> (mapv object-distance->map))))

(defn parse-errors-rpc [err]
  {:type (.getType err)
   :msg (.getMsg err)
   :details (seq (.getDetailsList err))
   :instance (.getInstance err)
   :status (.getStatus err)
   :error (.getError err)
   :roots (seq (->> (.getRootsList err)
                    (mapv parse-errors-rpc)))})

(defn parse-stream-object-location [locs]
  (let [pc (-> locs
               (.getPayloadCase))]
    (cond
      (= pc Object$StreamLocation$PayloadCase/LOCATION)
      (-> locs
          (.getLocation)
          (object-location->map))
      (= pc Object$StreamLocation$PayloadCase/ERROR)
      (-> locs
          (.getError)
          (parse-errors-rpc))
      :else
      {})))

(defn parse-stream-object-vector [v]
  (let [pc (-> v
               (.getPayloadCase))]
    (cond
      (= pc Object$StreamVector$PayloadCase/VECTOR)
      (-> v
          (.getVector)
          (object-vector->map))
      (= pc Object$StreamVector$PayloadCase/ERROR)
      (-> v
          (.getError)
          (parse-errors-rpc))
      :else
      {})))

(defn parse-stream-search-response [res]
  (let [pc (-> res
               (.getPayloadCase))]
    (cond
      (= pc Search$StreamResponse$PayloadCase/RESPONSE)
      (-> res
          (.getResponse)
          (parse-search-response))
      (= pc Search$StreamResponse$PayloadCase/ERROR)
      (-> res
          (.getError)
          (parse-errors-rpc))
      :else
      {})))

(defn index-info-count->map [iic]
  {:stored (.getStored iic)
   :uncommitted (.getUncommitted iic)
   :indexing (.getIndexing iic)})

(defprotocol IClient
  "A protocol for Vald client."
  (close
    [this]
    "Close channel.")
  (exists
    [this id]
    "Check whether `id` exists or not.")
  (search
    [this config vector]
    "Search with `vector`.")
  (search-by-id
    [this config id]
    "Search with `id`.")
  (stream-search
    [this f config vectors]
    "Stream search with `vectors`.
    `f` will be applied to each responses.")
  (stream-search-by-id
    [this f config ids]
    "Stream search with `ids`
    `f` will be applied to each responses.")
  (insert
    [this config id vector]
    "Insert `id` and `vector` pair.")
  (stream-insert
    [this f config vectors]
    "Stream insert with `vectors`.
    `f` will be applied to each responses.")
  (multi-insert
    [this config vectors]
    "Multi insert with `vectors`.")
  (update
    [this config id vector]
    "Update `id` and `vector` pair.")
  (stream-update
    [this f config vectors]
    "Stream update with `vectors`.
    `f` will be applied to each responses.")
  (multi-update
    [this config vectors]
    "Multi update with `vectors`.")
  (remove-id
    [this config id]
    "Remove `id`.")
  (stream-remove
    [this f config ids]
    "Stream remove with `ids`.
    `f` will be applied to each responses.")
  (multi-remove
    [this config ids]
    "Multi remove with `ids`.")
  (get-object
    [this id]
    "Get object with `id`.")
  (stream-get-object
    [this f ids]
    "Stream get object with `ids`.
    `f` will be applied to each responses.")
  (create-index
    [this pool-size]
    "Call create-index command.
    This functionality is only for Agents.")
  (save-index
    [this]
    "Call save-index command.
    This functionality is only for Agents.")
  (create-and-save-index
    [this pool-size]
    "Call create-and-save-index command.
    This functionality is only for Agents.")
  (index-info
    [this]
    "Fetch index info.
    This functionality is only for Agents."))

(defrecord Client [channel]
  IClient
  (close [this]
    (when (false? (.isShutdown channel))
      (-> channel
          (.shutdown))))
  (exists [this id]
    (when (false? (.isShutdown channel))
      (let [id (-> (Object$ID/newBuilder)
                   (.setId id)
                   (.build))]
        (-> (ObjectGrpc/newBlockingStub channel)
            (.exists id)
            (object-id->map)))))
  (search [this config vector]
    (when (false? (.isShutdown channel))
      (let [req (-> (Search$Request/newBuilder)
                    (.addAllVector (seq (map float vector)))
                    (.setConfig (->search-config config))
                    (.build))]
        (-> (SearchGrpc/newBlockingStub channel)
            (.search req)
            (parse-search-response)))))
  (search-by-id [this config id]
    (when (false? (.isShutdown channel))
      (let [req (-> (Search$IDRequest/newBuilder)
                    (.setId id)
                    (.setConfig (->search-config config))
                    (.build))]
        (-> (SearchGrpc/newBlockingStub channel)
            (.searchByID req)
            (parse-search-response)))))
  (stream-search [this f config vectors]
    (when (false? (.isShutdown channel))
      (let [pm (promise)
            cnt (atom 0)
            config (->search-config config)
            reqs (->> vectors
                      (mapv #(-> (Search$Request/newBuilder)
                                 (.addAllVector (seq (map float %)))
                                 (.setConfig config)
                                 (.build))))
            observer (-> (SearchGrpc/newStub channel)
                         (.streamSearch
                          (reify StreamObserver
                            (onNext [this res]
                              (swap! cnt inc)
                              (-> (parse-stream-search-response res)
                                  (f)))
                            (onError [this throwable]
                              (deliver pm {:status :error
                                           :error throwable
                                           :count @cnt}))
                            (onCompleted [this]
                              (deliver pm {:status :done
                                           :count @cnt})))))]
        (->> reqs
             (map #(-> ^StreamObserver observer
                       (.onNext %)))
             (doall))
        (-> ^StreamObserver observer
            (.onCompleted))
        pm)))
  (stream-search-by-id [this f config ids]
    (when (false? (.isShutdown channel))
      (let [pm (promise)
            cnt (atom 0)
            config (->search-config config)
            reqs (->> ids
                      (mapv #(-> (Search$IDRequest/newBuilder)
                                 (.setId %)
                                 (.setConfig config)
                                 (.build))))
            observer (-> (SearchGrpc/newStub channel)
                         (.streamSearchByID
                          (reify StreamObserver
                            (onNext [this res]
                              (swap! cnt inc)
                              (-> (parse-stream-search-response res)
                                  (f)))
                            (onError [this throwable]
                              (deliver pm {:status :error
                                           :error throwable
                                           :count @cnt}))
                            (onCompleted [this]
                              (deliver pm {:status :done
                                           :count @cnt})))))]
        (->> reqs
             (map #(-> ^StreamObserver observer
                       (.onNext %)))
             (doall))
        (-> ^StreamObserver observer
            (.onCompleted))
        pm)))
  (insert [this config id vector]
    (when (false? (.isShutdown channel))
      (let [v (-> (Object$Vector/newBuilder)
                    (.setId id)
                    (.addAllVector (seq (map float vector)))
                    (.build))
            config (->insert-config config)
            req (-> (Insert$Request/newBuilder)
                    (.setVector v)
                    (.setConfig config)
                    (.build))]
        (-> (InsertGrpc/newBlockingStub channel)
            (.insert req)
            (object-location->map)))))
  (stream-insert [this f config vectors]
    (when (false? (.isShutdown channel))
      (let [pm (promise)
            cnt (atom 0)
            config (->insert-config config)
            reqs (->> vectors
                      (map #(-> (Object$Vector/newBuilder)
                                (.setId (:id %))
                                (.addAllVector (seq (map float (:vector %))))
                                (.build)))
                      (mapv #(-> (Insert$Request/newBuilder)
                                 (.setVector %)
                                 (.setConfig config)
                                 (.build))))
            observer (-> (InsertGrpc/newStub channel)
                         (.streamInsert
                          (reify StreamObserver
                            (onNext [this res]
                              (swap! cnt inc)
                              (-> res
                                  (parse-stream-object-location)
                                  (f)))
                            (onError [this throwable]
                              (deliver pm {:status :error
                                           :error throwable
                                           :count @cnt}))
                            (onCompleted [this]
                              (deliver pm {:status :done
                                           :count @cnt})))))]
        (->> reqs
             (map #(-> ^StreamObserver observer
                       (.onNext %)))
             (doall))
        (-> ^StreamObserver observer
            (.onCompleted))
        pm)))
  (multi-insert [this config vectors]
    (when (false? (.isShutdown channel))
      (let [config (->insert-config config)
            reqs (->> vectors
                      (map #(-> (Object$Vector/newBuilder)
                                (.setId (:id %))
                                (.addAllVector (seq (map float (:vector %))))
                                (.build)))
                      (mapv #(-> (Insert$Request/newBuilder)
                                 (.setVector %)
                                 (.setConfig config)
                                 (.build))))
            req (-> (Insert$MultiRequest/newBuilder)
                    (.addAllRequests reqs))]
        (-> (InsertGrpc/newBlockingStub channel)
            (.multiInsert req)
            (object-locations->map)))))
  (update [this config id vector]
    (when (false? (.isShutdown channel))
      (let [config (->update-config config)
            v (-> (Object$Vector/newBuilder)
                    (.setId id)
                    (.addAllVector (seq (map float vector)))
                    (.build))
            req (-> (Update$Request/newBuilder)
                    (.setVector v)
                    (.setConfig config)
                    (.build))]
        (-> (UpdateGrpc/newBlockingStub channel)
            (.update req)
            (object-location->map)))))
  (stream-update [this f config vectors]
    (when (false? (.isShutdown channel))
      (let [pm (promise)
            cnt (atom 0)
            config (->update-config config)
            reqs (->> vectors
                      (map #(-> (Object$Vector/newBuilder)
                                (.setId (:id %))
                                (.addAllVector (seq (map float (:vector %))))
                                (.build)))
                      (mapv #(-> (Update$Request/newBuilder)
                                 (.setVector %)
                                 (.setConfig config)
                                 (.build))))
            observer (-> (UpdateGrpc/newStub channel)
                         (.streamUpdate
                          (reify StreamObserver
                            (onNext [this res]
                              (swap! cnt inc)
                              (-> res
                                  (parse-stream-object-location)
                                  (f)))
                            (onError [this throwable]
                              (deliver pm {:status :error
                                           :error throwable
                                           :count @cnt}))
                            (onCompleted [this]
                              (deliver pm {:status :done
                                           :count @cnt})))))]
        (->> reqs
             (map #(-> ^StreamObserver observer
                       (.onNext %)))
             (doall))
        (-> ^StreamObserver observer
            (.onCompleted))
        pm)))
  (multi-update [this config vectors]
    (when (false? (.isShutdown channel))
      (let [config (->update-config config)
            reqs (->> vectors
                      (map #(-> (Object$Vector/newBuilder)
                                (.setId (:id %))
                                (.addAllVector (seq (map float (:vector %))))
                                (.build)))
                      (mapv #(-> (Update$Request/newBuilder)
                                 (.setVector %)
                                 (.setConfig config)
                                 (.build))))
            req (-> (Update$MultiRequest/newBuilder)
                    (.addAllRequests reqs))]
        (-> (UpdateGrpc/newBlockingStub channel)
            (.multiUpdate req)
            (object-locations->map)))))
  (remove-id [this config id]
    (when (false? (.isShutdown channel))
      (let [config (->remove-config config)
            id (-> (Object$ID/newBuilder)
                   (.setId id)
                   (.build))
            req (-> (Remove$Request/newBuilder)
                    (.setId id)
                    (.setConfig config)
                    (.build))]
        (-> (RemoveGrpc/newBlockingStub channel)
            (.remove req)
            (object-location->map)))))
  (stream-remove [this f config ids]
    (when (false? (.isShutdown channel))
      (let [pm (promise)
            cnt (atom 0)
            config (->remove-config config)
            reqs (->> ids
                      (map #(-> (Object$ID/newBuilder)
                                 (.setId %)
                                 (.build)))
                      (mapv #(-> (Remove$Request/newBuilder)
                                 (.setId %)
                                 (.setConfig config)
                                 (.build))))
            observer (-> (RemoveGrpc/newStub channel)
                         (.streamRemove
                          (reify StreamObserver
                            (onNext [this res]
                              (swap! cnt inc)
                              (-> res
                                  (parse-stream-object-location)
                                  (f)))
                            (onError [this throwable]
                              (deliver pm {:status :error
                                           :error throwable
                                           :count @cnt}))
                            (onCompleted [this]
                              (deliver pm {:status :done
                                           :count @cnt})))))]
        (->> reqs
             (map #(-> ^StreamObserver observer
                       (.onNext %)))
             (doall))
        (-> ^StreamObserver observer
            (.onCompleted))
        pm)))
  (multi-remove [this config ids]
    (when (false? (.isShutdown channel))
      (let [config (->remove-config config)
            reqs (->> ids
                      (mapv #(-> (Remove$Request/newBuilder)
                                 (.setId %)
                                 (.setConfig config)
                                 (.build))))
            req (-> (Remove$MultiRequest/newBuilder)
                    (.addAllRequests reqs)
                    (.build))]
        (-> (RemoveGrpc/newBlockingStub channel)
            (.multiRemove req)
            (object-locations->map)))))
  (get-object [this id]
    (when (false? (.isShutdown channel))
      (let [id (-> (Object$ID/newBuilder)
                   (.setId id)
                   (.build))]
        (-> (ObjectGrpc/newBlockingStub channel)
            (.getObject id)
            (object-vector->map)))))
  (stream-get-object [this f ids]
    (when (false? (.isShutdown channel))
      (let [pm (promise)
            cnt (atom 0)
            reqs (->> ids
                      (mapv #(-> (Object$ID/newBuilder)
                                 (.setId %)
                                 (.build))))
            observer (-> (ObjectGrpc/newStub channel)
                         (.streamGetObject
                          (reify StreamObserver
                            (onNext [this res]
                              (swap! cnt inc)
                              (-> (parse-stream-object-vector res)
                                  (f)))
                            (onError [this throwable]
                              (deliver pm {:status :error
                                           :error throwable
                                           :count @cnt}))
                            (onCompleted [this]
                              (deliver pm {:status :done
                                           :count @cnt})))))]
        (->> reqs
             (map #(-> ^StreamObserver observer
                       (.onNext %)))
             (doall))
        (-> ^StreamObserver observer
            (.onCompleted))
        pm)))
  (create-index [this pool-size]
    (when (false? (.isShutdown channel))
      (let [req (-> (Control$CreateIndexRequest/newBuilder)
                    (.setPoolSize pool-size)
                    (.build))]
        (-> (AgentGrpc/newBlockingStub channel)
            (.createIndex req)
            (empty->map)))))
  (save-index [this]
    (when (false? (.isShutdown channel))
      (let [req (-> (Empty/newBuilder)
                    (.build))]
        (-> (AgentGrpc/newBlockingStub channel)
            (.saveIndex req)
            (empty->map)))))
  (create-and-save-index [this pool-size]
    (when (false? (.isShutdown channel))
      (let [req (-> (Control$CreateIndexRequest/newBuilder)
                    (.setPoolSize pool-size)
                    (.build))]
        (-> (AgentGrpc/newBlockingStub channel)
            (.createAndSaveIndex req)
            (empty->map)))))
  (index-info [this]
    (when (false? (.isShutdown channel))
      (let [req (-> (Empty/newBuilder)
                    (.build))]
        (-> (AgentGrpc/newBlockingStub channel)
            (.indexInfo req)
            (index-info-count->map))))))

(defn grpc-channel [host port]
  (-> (ManagedChannelBuilder/forAddress host port)
      (.usePlaintext)
      (.build)))

(defn vald-client
  "Open channel and returns Vald gateway client instance."
  [host port]
  (let [channel (grpc-channel host port)]
    (->Client channel)))

(defn agent-client
  "Open channel and returns Vald agent client instance."
  {:deprecated "v0.1.0"}
  [host port]
  (vald-client host port))

(comment
  (def client (vald-client "localhost" 8081))
  (def client (agent-client "localhost" 8081))
  (close client)

  (defn rand-vec []
    (take 784 (repeatedly #(float (rand)))))

  (-> client
      (exists "test"))
  (-> client
      (search {:num 10} (rand-vec)))
  (-> client
      (search-by-id {:num 10} "test"))

  (deref
   (stream-search client println {:num 10} (take 10 (repeatedly #(rand-vec)))))
  (deref
   (stream-search-by-id client println {:num 3} ["test" "abc"]))
  (deref
   (stream-get-object client println ["zz1" "zz3"]))

  (-> client
      (insert {} "test" (rand-vec)))

  (->> (take 300 (range))
       (map
        (fn [i]
          (-> client
              (insert {} (str "zz" i) (rand-vec))))))

  (let [vectors (->> (take 10 (range))
                     (map
                      (fn [i]
                        {:id (str "a" i)
                         :vector (rand-vec)})))]
    @(stream-insert client println {} vectors))

  (-> client
      (get-object "zz3"))

  (-> client
      (create-index 10000))

  (-> client
      (save-index))

  (-> client
      (create-and-save-index 1000))

  (-> client
      (index-info)))
