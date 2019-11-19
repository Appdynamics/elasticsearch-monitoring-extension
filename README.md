# AppDynamics Monitoring Extension for use with Elasticsearch

This extension requires a Java Machine Agent

##Use Case

Elasticsearch is a distributed RESTful search server based on Lucene which provides a distributed multitenant-capable full text search engine.
This extension collects cluster health metrics, nodes and indices stats from a Elasticsearch engine and presents them in AppDynamics Metric Browser


##Installation

1. To build from source, clone this repository and run `mvn clean install`. This will produce a ElasticSearchMonitor-VERSION.zip in the target directory. Alternatively, download the latest release archive from [Github](https://github.com/Appdynamics/elasticsearch-monitoring-extension/releases).
2. Copy the file ElasticSearchMonitor.zip located in the 'target' directory into `<machineagent install dir>/monitors/` and unzip the file.
3. In `<machineagent install dir>/monitors/ElasticSearchMonitor/`, open config.yml and configure the ElasticSearch parameters.

   ```
     servers:
       - displayName: Server1
         uri: "http://localhost:9200"
         username: ""
         password: ""     
         
     # To get the metrics through CAT apis
     catEndPoints:
     
       - endPoint: "/_cat/health?v&h=cluster,nodeTotal,nodeData,shardsTotal,shardsPrimary,shardsRelocating,shardsInitializing,shardsUnassigned,pendingTasks"
         metricKeys: [
             "cluster"
         ]
              
       - endPoint: "/_cat/allocation?v&bytes=b&h=node,shards,diskUsed,diskAvailable,diskTotal,diskPercent"
          # Any prefixes
         metricPrefix: "Allocation"
          # The keys to be used in metric path. Picks up in the same order.
         metricKeys: [
            "node"
         ]
 
       - endPoint: "/_cat/indices?v&bytes=b&time=s&h=index,health,status,shardsPrimary,shardsReplica,docsCount,docsDeleted,storeSize,pri.store.size,searchQueryTime,searchQueryTotal,searchQueryCurrent,searchFetchTotal,searchFetchTime,searchFetchCurrent,indexingIndexTotal,indexingIndexTime"
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
     
     
     connection:
       socketTimeout: 2000
       connectTimeout: 2000
     
     
     # number of concurrent tasks.
     # This doesn't need to be changed unless many instances are configured
     numberOfThreads: 5
   ```
5. Restart the Machine Agent.

In the AppDynamics Metric Browser, look for: Application Infrastructure Performance  | \<Tier\> | Individual Nodes | \<Node\> | Custom Metrics | Elastic Search


## Metrics

Cat Level Statistics
As mentioned https://www.elastic.co/guide/en/elasticsearch/reference/current/cat.html

## WorkBench
Workbench is a feature that lets you preview the metrics before registering it with the controller. This is useful if you want to fine tune the configurations. Workbench is embedded into the extension jar.
To use the workbench

1. Follow all the installation steps
2. Start the workbench with the command
`java -jar /path/to/MachineAgent/monitors/ElasticSearchMonitor/elasticsearch-monitoring-extension.jar`
This starts an http server at `http://host:9090/`. This can be accessed from the browser.
3. If the server is not accessible from outside/browser, you can use the following end points to see the list of registered metrics and errors.
#Get the stats
`curl http://localhost:9090/api/stats`
#Get the registered metrics
`curl http://localhost:9090/api/metric-paths`
4. You can make the changes to config.yml and validate it from the browser or the API
5. Once the configuration is complete, you can kill the workbench and start the Machine Agent

## Troubleshooting 
1. Verify Machine Agent Data:Please start the Machine Agent without the extension and make sure that it reports data. Verify that the machine agent status is UP and it is reporting Hardware Metrics.
2. Metric Limit: Please start the machine agent with the argument -Dappdynamics.agent.maxMetrics=2000, if there is a metric limit reached error in the logs.
3. Collect Debug Logs: Edit the file, `<MachineAgent>/conf/logging/log4j.xml` and update the level of the appender "com.appdynamics" and "com.singularity" to debug.

## Custom Dashboard
![](https://raw.github.com/Appdynamics/elasticsearch-monitoring-extension/master/Dashboard.png)

##Contributing

Always feel free to fork and contribute any changes directly here on GitHub.

##Community

Find out more in the [AppSphere](https://www.appdynamics.com/community/exchange/extension/elasticsearch-monitoring-extension/) community.

##Support
For any questions or feature request, please contact [AppDynamics Support](mailto:help@appdynamics.com).


