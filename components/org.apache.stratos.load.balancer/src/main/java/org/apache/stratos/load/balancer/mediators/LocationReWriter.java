/**
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at

 *  http://www.apache.org/licenses/LICENSE-2.0

 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.stratos.load.balancer.mediators;

import org.apache.commons.lang3.StringUtils;
import org.apache.stratos.load.balancer.conf.LoadBalancerConfiguration;
import org.apache.stratos.load.balancer.context.LoadBalancerContext;
import org.apache.stratos.load.balancer.util.LoadBalancerConstants;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * Re-write HTTP Location header with load balancer hostname and port.
 */
public class LocationReWriter extends AbstractMediator {

    public static final String LOCATION = "Location";
    public static final String HTTP = "http";
    public static final String HTTPS = "https";

    @Override
    public boolean mediate(MessageContext messageContext) {
        if (LoadBalancerConfiguration.getInstance().isReWriteLocationHeader()) {
            rewriteLocationHeader(messageContext);
        }
        return true;
    }

    private void rewriteLocationHeader(MessageContext messageContext) {
        try {
            // Read transport headers
            Map transportHeaders = (Map) ((Axis2MessageContext) messageContext).getAxis2MessageContext().
                    getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
            if (transportHeaders != null) {
                // Find location header
                String inLocation = (String) transportHeaders.get(LOCATION);
                if (StringUtils.isNotBlank(inLocation)) {
                    URL inLocationUrl = null;
                    try {
                        inLocationUrl = new URL(inLocation);
                    } catch (MalformedURLException e) {
                        return;
                    }

                    // Check whether the location host is an ip address of a known member
                    String hostname = LoadBalancerContext.getInstance().getMemberIpHostnameMap().get(inLocationUrl.getHost());
                    if (StringUtils.isEmpty(hostname)) {
                        
                        if (!LoadBalancerContext.getInstance().getHostNameClusterMap().containsCluster(inLocationUrl.getHost())) {
                        	if (log.isDebugEnabled()) {
                                log.debug(String.format("A hostname not found for ip: [ip-address] %s", inLocationUrl.getHost()));
                            }
                        	return;
                        } else {
                        	hostname = inLocationUrl.getHost();
                        }
                    }

                    if (log.isDebugEnabled()) {
                        log.debug(String.format("A location header found with member ip: [member-ip] %s " +
                                "[hostname] %s ", inLocationUrl.getHost(), hostname));
                    }

                    int targetPort = -1;
                    if (HTTP.equals(inLocationUrl.getProtocol())) {
                        targetPort = Integer.valueOf((String) messageContext.getProperty(LoadBalancerConstants.LB_HTTP_PORT));
                    } else if (HTTPS.equals(inLocationUrl.getProtocol())) {
                        targetPort = Integer.valueOf((String) messageContext.getProperty(LoadBalancerConstants.LB_HTTPS_PORT));
                    } else {
                        if (log.isWarnEnabled()) {
                            log.warn(String.format("An unknown protocol found: %s", inLocationUrl.getProtocol()));
                        }
                    }

                    if (targetPort != -1) {
                        // Re-write location header
                        URL outLocationUrl = new URL(inLocationUrl.getProtocol(), hostname, targetPort, inLocationUrl.getFile());
                        transportHeaders.put(LOCATION, outLocationUrl.toString());
                        if (log.isDebugEnabled()) {
                            log.debug(String.format("Location header re-written: %s", outLocationUrl.toString()));
                        }
                    }
                }
            }
        } catch (Exception e) {
            if (log.isWarnEnabled()) {
                log.warn("Could not re-write location header", e);
            }
        }
    }
}
