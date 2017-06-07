# solr-metrics-influxdb
Solr Reporter for InfluxDB

#Steps to run the plugin
-  Compile custom metrics reporter : Run `mvn clean package`
-  Put the custom metrics reporter and the dependency in a directory. Example
    <pre><code>
    ~/customReporterLibs$ ls
    
    metrics-influxdb-0.9.3.jar		solr-metrics-influxdb-1.0-SNAPSHOT.jar
    </code></pre>
- Now modify the solr.xml in the node specifying the reporter and the path to the metrics library

      <solr>
        <str name="sharedLib">~/customReporterLibs</str>
    
        <solrcloud>
          ...
        </solrcloud>
    
        <shardHandlerFactory name="shardHandlerFactory"
          ...
        </shardHandlerFactory>
    
        <metrics>
          <reporter name="influxdb" class="org.apache.solr.metrics.reporters.SolrInfluxdbReporter">
            <str name="host">localhost</str>
            <int name="port">8086</int>
            <int name="period">5</int>
            <str name="user">admin</str>
            <str name="password">admin</str>
            <str name="db">metrics_monitoring</str>
          </reporter>
        </metrics>
      </solr>