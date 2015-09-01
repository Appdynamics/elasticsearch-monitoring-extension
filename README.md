# AppDynamics Elasticsearch Monitoring Extension

This extension works only with the standalone machine agent.

##Use Case

Elasticsearch is a distributed RESTful search server based on Lucene which provides a distributed multitenant-capable full text search engine.
This extension collects cluster health metrics, nodes and indices stats from a Elasticsearch engine and presents them in AppDynamics Metric Browser.


##Installation

1. To build from source, clone this repository and run `mvn clean install`. This will produce a ElasticSearchMonitor-VERSION.zip in the target directory. Alternatively, download the latest release archive from [Github](https://github.com/Appdynamics/elasticsearch-monitoring-extension/releases).
2. Copy the file ElasticSearchMonitor.zip located in the 'target' directory into `<machineagent install dir>/monitors/` and unzip the file.
3. In `<machineagent install dir>/monitors/ElasticSearchMonitor/`, open config.yaml and configure the ElasticSearch parameters.

   ```
     servers:
       - host: ""
         port: ""
         username: ""
         password: ""
         usessl: false
         displayName: ""
         # To disable the pull of json metrics
         enableJsonMetrics: true
         # To get the metrics through CAT apis
         catEndPoints:
           - endPoint: "/_cat/allocation?v&bytes=b&h=node,shards,diskUsed,diskAvailable,diskTotal,diskPercent"
              # Any prefixes
             metricPrefix: "Allocation"
              # The keys to be used in metric path. Picks up in the same order.
             metricKeys: [
                "node"
             ]

           - endPoint: "/_cat/indices?v&bytes=b&h=index,health,status,shardsPrimary,shardsReplica,docsCount,docsDeleted,storeSize,pri.store.size"
             metricPrefix: "Indices"
             metricKeys: [
                "index"
             ]

           - endPoint: "/_cat/recovery?v&bytes=b&h=index,shard,files,files_percent,bytes,bytes_percent"
             metricPrefix: "Recovery"
             metricKeys: [
                "index"
             ]

           - endPoint: "_cat/thread_pool?v&bytes=b&h=host,bulk.active,bulk.size,bulk.queue,bulk.queueSize,bulk.rejected,bulk.largest,bulk.completed,bulk.min,bulk.max"
             metricPrefix: "ThreadPool"
             metricKeys: [
               "host"
             ]

           - endPoint: "/_cat/shards?v&bytes=b&h=node,index,shard,docs,store"
             metricPrefix: "Shards"
             metricKeys: [
                "node",
                "index"
             ]

     metricPathPrefix: "Custom Metrics|Elastic Search|"
   ```
5. Restart the Machine Agent.

In the AppDynamics Metric Browser, look for: Application Infrastructure Performance  | \<Tier\> | Custom Metrics | Elastic Search


## Metrics

Cluster Metrics
The following metrics are reported under \<ClusterName\>

| Metric Name 			| Description |
|-------------------------------|-------------|
|status				| red (0), yellow (1), green(2)	|
|number of nodes		| number of nodes in this cluster|
|number of data nodes		| number of data nodes|
|active primary shards		| active primary shards|
|active shards			| active shards|
|relocating shards		| relocating shards|
|initializing shards		| initializing shards|
|unassigned shards		| unassigned shards|

Node Statistics
The following metrics are reported for each Node in the cluster under Nodes | \<NodeName\>

| Metric Name 			| Description |
|-------------------------------|-------------|
|size of indices		| size of indices (MB)	|
|num docs			| number of documents|
|open file descriptors		| open file descriptors count|
|threads count			| number of threads used|

Indices Statistics
The following metrics are reported for each Index under Indices | \<IndexName\>

| Metric Name 			| Description |
|-------------------------------|-------------|
|primary size			| primary size of index (MB)	|
|size				| total size of index (MB)|
|num docs			| number of documents|


Cat Level Statistics
As mentioned https://www.elastic.co/guide/en/elasticsearch/reference/current/cat.html


## Custom Dashboard
![](https://raw.github.com/Appdynamics/elasticsearch-monitoring-extension/master/Dashboard.png)

##Contributing

Always feel free to fork and contribute any changes directly here on GitHub.

##Community

Find out more in the [AppSphere](http://appsphere.appdynamics.com/t5/AppDynamics-eXchange/Elasticsearch-Monitoring-Extension/idi-p/6449) community.

##Support

For any questions or feature request, please contact [AppDynamics Support](mailto:help@appdynamics.com).


