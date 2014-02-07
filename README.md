# AppDynamics Elasticsearch Monitoring Extension

This extension works only with the standalone machine agent.

##Use Case

Elasticsearch is a distributed RESTful search server based on Lucene which provides a distributed multitenant-capable full text search engine.
This extension collects cluster health metrics, nodes and indices stats from a Elasticsearch engine and presents them in AppDynamics Metric Browser.


##Installation

1. Run 'mvn clean install' from the elasticsearch-monitoring-extension directory
2. Download the file ElasticSearchMonitor.zip located in the 'target' directory into \<machineagent install dir\>/monitors/
3. Unzip the downloaded file
4. In \<machineagent install dir\>/monitors/ElasticSearchMonitor/, open monitor.xml and configure the ElasticSearch parameters.
     <pre>
     &lt;argument name="host" is-required="true" default-value="localhost" /&gt;
     &lt;argument name="port" is-required="true" default-value="9200" /&gt;
     </pre>
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
|heap used			| heap used (MB)|
|heap committed			| heap committed (MB)|
|non heap used			| non heap used (MB)|
|non heap committed		| non heap committed (MB)|
|threads count			| number of threads used|

Indices Statistics
The following metrics are reported for each Index under Indices | \<IndexName\>

| Metric Name 			| Description |
|-------------------------------|-------------|
|primary size			| primary size of index (MB)	|
|size				| total size of index (MB)|
|num docs			| number of documents|

## Custom Dashboard
![](https://raw.github.com/Appdynamics/elasticsearch-monitoring-extension/master/Dashboard.png)

##Contributing

Always feel free to fork and contribute any changes directly here on GitHub.

##Community

Find out more in the [AppSphere](http://appsphere.appdynamics.com/t5/AppDynamics-eXchange/Elasticsearch-Monitoring-Extension/idi-p/6449) community.

##Support

For any questions or feature request, please contact [AppDynamics Center of Excellence](mailto:ace-request@appdynamics.com).


