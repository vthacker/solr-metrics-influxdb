/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.apache.solr.metrics.reporters;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.ScheduledReporter;
import metrics_influxdb.HttpInfluxdbProtocol;
import metrics_influxdb.InfluxdbReporter;
import metrics_influxdb.api.measurements.CategoriesMetricMeasurementTransformer;
import org.apache.solr.metrics.SolrMetricManager;
import org.apache.solr.metrics.SolrMetricReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

/**
 * Metrics reporter that wraps {@link metrics_influxdb.InfluxdbReporter}.
 */
public class SolrInfluxdbReporter extends SolrMetricReporter {
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private String scheme = "http";
    private String host = null;
    private int port = -1;
    private String user = null;
    private String password = null;
    private String db = null;
    private int period = 60;

    private ScheduledReporter reporter = null;

    /**
     * Create a Graphite reporter for metrics managed in a named registry.
     *
     * @param metricManager metric manager instance that manages the selected registry
     * @param registryName  registry to use, one of registries managed by
     *                      {@link SolrMetricManager}
     */
    public SolrInfluxdbReporter(SolrMetricManager metricManager, String registryName) {
        super(metricManager, registryName);
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setDb(String db) {
        this.db = db;
    }

    public void setPeriod(int period) {
        this.period = period;
    }


    @Override
    protected void validate() throws IllegalStateException {
        if (!enabled) {
            log.info("Reporter disabled for registry " + registryName);
            return;
        }
        if (reporter != null) {
            throw new IllegalStateException("Already started once?");
        }


        if (host == null) {
            throw new IllegalStateException("Init argument 'host' must be set to a valid InfluxDB server name.");
        }
        if (port == -1) {
            throw new IllegalStateException("Init argument 'port' must be set to a valid InfluxDB server port.");
        }
        if (user == null) {
            throw new IllegalStateException("Init argument 'user' must be specified");
        }
        if (password == null) {
            throw new IllegalStateException("Init argument 'password' must be specified");
        }
        if (db == null) {
            throw new IllegalStateException("Init argument 'db' must be specified");
        }
        if (period < 1) {
            throw new IllegalStateException("Init argument 'period' is in time unit 'seconds' and must be at least 1.");
        }

        String hostname;
        try {
            InetAddress addr = InetAddress.getLocalHost();
            hostname = addr.getCanonicalHostName();
        } catch (UnknownHostException e) {
            hostname = "unknown_host";
        }

        reporter = InfluxdbReporter.forRegistry(metricManager.registry(registryName))
                .protocol(new HttpInfluxdbProtocol(scheme, host, port, user, password, db))
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .filter(MetricFilter.ALL)
                .skipIdleMetrics(false)
                .tag("server_ip", hostname)
                .transformer(new CategoriesMetricMeasurementTransformer())
                .build();
        reporter.start(period, TimeUnit.SECONDS);

    }

    @Override
    public void close() throws IOException {
        if (reporter != null) {
            reporter.close();
        }
    }
}