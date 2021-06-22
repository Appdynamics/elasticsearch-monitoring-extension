# AppDynamics Monitoring Extension for use with Elasticsearch

This extension requires a Java Machine Agent

## Use Case

Elasticsearch is a distributed RESTful search server based on Lucene which provides a distributed multitenant-capable full text search engine.
This extension collects metrics using cat endpoints of Elasticsearch REST API and presents them in AppDynamics Metric Browser
## Prerequisites
Before the extension is installed, the prerequisites mentioned [here](https://community.appdynamics.com/t5/Knowledge-Base/Extensions-Prerequisites-Guide/ta-p/35213) need to be met. Please do not proceed with the extension installation if the specified prerequisites are not met.

Elasticsearch's HTTP module should be enabled since this extension collects metrics using cat API over HTTP. 
## Installation
1.  Unzip the contents of "ElasticsearchMonitor-VERSION.zip" as "ElasticsearchMonitor" and copy the "ElasticsearchMonitor" directory to `<MachineAgentHome>/monitors/`
2. Configure the extension by referring to the below section.
3. Configure the path to the config.yml file by editing the task-arguments in the monitor.xml file.
    ```
        <task-arguments>
            <argument name="config-file" is-required="true" default-value="monitors/ElasticsearchMonitor/config.yml" />
        </task-arguments>
    ```
4. Restart the machine agent. 

Please place the extension in the "monitors" directory of your Machine Agent installation directory. Do not place the extension in the "extensions" directory of your Machine Agent installation directory.

## Configuration
Note : Please make sure not to use tab (\t) while editing yaml files. You can validate the yaml file using a [yaml validator](http://yamllint.com)

Configure the extension by editing the config.yml file in `<MachineAgentHome>/monitors/ElasticsearchMonitor/`. The metricPrefix of the extension has to be configured as specified [here](https://community.appdynamics.com/t5/Knowledge-Base/How-do-I-troubleshoot-missing-custom-metrics-or-extensions/ta-p/28695#Configuring%20an%20Extension). Please make sure that the right metricPrefix is chosen based on your machine agent deployment, otherwise this could lead to metrics not being visible in the controller.
###config.yml
1. Configure the Elasticsearch server in the `servers` section. This is done by configuring `displayName`(required), `uri`(required), `username`, `password` or `encryptedPassword`. The `uri` only consists of the `http:\\{host}:{port}`, do not configure the complete cat endpoint here.
2. Next step is setting the `catEndpoints`. All the `servers` will share this configuration. To define a cat API you have to provide the following -
    * __endpoint__ - `/_cat/{cat api name}`. For example to fetch metrics from cat health API, endpoint will be `/_cat/health?v`. Please refer to elasticsearch documents for [API conventions](https://www.elastic.co/guide/en/elasticsearch/reference/6.6/api-conventions.html) and list of [CAT APIs](https://www.elastic.co/guide/en/elasticsearch/reference/6.6/cat.html).
    * __displayName__ - Used in metric path, all metrics configured for this endpoint will be listed under this display name in the metric browser.
    * __metricPathKeys__ - if the cat endpoint has any string value in its output then it can be included in the metric path by providing the header of that column. For example, in the below curl the node name(aliased as name) has string values. When this field is configured as `metricPathKeys: ["name"]` then metrics will be reported as `<metricPrefix>|...|N8sjO48|heap.percent` for node name `N8sjO48`
    ```
   curl 'http://localhost:9200/_cat/nodes?v&h=heap.percent,ram.percent,cpu,load_1m,name'
   heap.percent ram.percent cpu load_1m name
             49          98   3    0.47 N8sjO48
             53          98   3    0.47 NwL3LHB
   ```
   * __metrics__ - Configure `name` and `properties`. `name` should match the column header in the response returned by the `endpoint`. `properties` include [metric qualifier](https://docs.appdynamics.com/display/PRO45/Build+a+Monitoring+Extension+Using+Java) and [metrics transforms](https://community.appdynamics.com/t5/Knowledge-Base/Extensions-Commons-Library-Metric-Transformers/ta-p/35413).
### numberOfThreads
Use the following formula for calculating `numberOfThreads`
```
numberOfThreads = number of servers *(1 + cat endpoints configured)
```
for example if there are 7 cat endpoints for one server then numberOfThreads = 1 * (1 + 7) = 8
### metricPathReplacements
Please visit [this](https://community.appdynamics.com/t5/Knowledge-Base/Metric-Path-CharSequence-Replacements-in-Extensions/ta-p/35412) page to get detailed instructions on configuring Metric Path Character sequence replacements in Extensions.
### customDashboard
Please visit [this](https://community.appdynamics.com/t5/Knowledge-Base/Uploading-Dashboards-Automatically-with-AppDynamics-Extensions/ta-p/35408) page to get detailed instructions on automatic dashboard upload with extension.

## Metrics
Number Statistics exposed through CAT APIs
as mentioned [here](https://www.elastic.co/guide/en/elasticsearch/reference/current/cat.html).

## Credentials Encryption
Please visit [this](https://community.appdynamics.com/t5/Knowledge-Base/How-to-use-Password-Encryption-with-Extensions/ta-p/29397) page to get detailed instructions on password encryption. The steps in this document will guide you through the whole process.
## Extensions Workbench
Workbench is an inbuilt feature provided with each extension in order to assist you to fine tune the extension setup before you actually deploy it on the controller. Please review the following [document](https://community.appdynamics.com/t5/Knowledge-Base/How-to-use-the-Extensions-WorkBench/ta-p/30130) for how to use the Extensions WorkBench
## Troubleshooting
Please follow the steps listed in the [extensions troubleshooting document](https://community.appdynamics.com/t5/Knowledge-Base/How-to-troubleshoot-missing-custom-metrics-or-extensions-metrics/ta-p/28695) in order to troubleshoot your issue. These are a set of common issues that customers might have faced during the installation of the extension. 

To troubleshoot this extension please follow these steps -
1. Ensure that elasticsearch is up and running by executing this command - 
    ```
    curl -v "http://<elasticsearch-host>:<port>"
    ```
    This command will return a JSON response and the `tagline` will read `You Know, for Search`
2. If there is a problem in fetching metrics from one of the configured endpoints then run the command -
    ```
    curl -v "http://<elasticsearch-host>:<port>/_cat/<endpoint-configured>?v"
    ```
    Ensure that the metric names configured in config.yml matches the names in the header of the response. For example, if there are issues in fetching metrics from cat health api then run the command and ensure that you get a valid output and that the header names match with the configured metric names (`v` parameter in the query is to enable verbose elasticsearch output).
    ```
   curl -v "http://<elasticsearch-host>:<port>/_cat/health?v"
   ```
If these don't solve your issue, there last step on the troubleshooting-document to contact the support team.


## Support Tickets
If after going through the Troubleshooting Document you have not been able to get your extension working, please file a ticket and add the following information.

Please provide us with the following for us to assist you better:
1. Config.yml & monitor.xml (`<MachineAgentHome>/monitors/DgraphMonitor`)
2. Controller-info.xml (`<MachineAgentHome>/conf/controller-info.xml`)
3. Enable Machine Agent `DEBUG` logging by changing the level values of the following logger elements from `INFO` to `DEBUG` in `<MachineAgent>/conf/logging/log4j.xml`:
    ```
    <logger name="com.singularity">
    <logger name="com.appdynamics">
    ```
4. After letting the Machine Agent run for 10-15 minutes, attach the complete `<MachineAgentHome>/logs/` directory.

For any support related questions, you can also contact [help@appdynamics.com](mailto:help@appdynamics.com).
## Contributing
Always feel free to fork and contribute any changes directly via [GitHub](https://github.com/Appdynamics/elasticsearch-monitoring-extension)

## Version
|          Name            |  Version   |
|--------------------------|------------|
|Extension Version         |3.0.1       |
|Controller Compatibility  |4.5 or Later|
|Machine Agent Version     |4.5.13+     |
|Product Tested on         |6.6.2      |
|Last Update               |15/01/2021  |