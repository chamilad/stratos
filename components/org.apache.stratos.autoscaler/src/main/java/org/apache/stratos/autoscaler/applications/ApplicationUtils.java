/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.stratos.autoscaler.applications;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.stratos.autoscaler.applications.payload.BasicPayloadData;
import org.apache.stratos.autoscaler.applications.payload.PayloadData;
import org.apache.stratos.autoscaler.applications.payload.PayloadFactory;
import org.apache.stratos.autoscaler.exception.application.ApplicationDefinitionException;
import org.apache.stratos.autoscaler.util.AutoscalerUtil;
import org.apache.stratos.cloud.controller.stub.domain.CartridgeInfo;
import org.apache.stratos.cloud.controller.stub.domain.PortMapping;
import org.apache.stratos.common.Property;

import java.util.*;
import java.util.regex.Pattern;

public class ApplicationUtils {
    private static final Log log = LogFactory.getLog(ApplicationUtils.class);

    public static final String TOKEN_PAYLOD_PARAM_NAME = "TOKEN";

    public static boolean isAliasValid (String alias) {

        String patternString = "([a-z0-9]+([-][a-z0-9])*)+";
        Pattern pattern = Pattern.compile(patternString);

        return pattern.matcher(alias).matches();
    }

    public static boolean isValid (String arg) {

        if (arg == null || arg.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    public static Properties getGlobalPayloadData () {

        Properties globalProperties = new Properties();

        if (System.getProperty("puppet.ip") != null) {
            globalProperties.setProperty("PUPPET_IP", System.getProperty("puppet.ip"));
        }
        if (System.getProperty("puppet.hostname") != null) {
            globalProperties.setProperty("PUPPET_HOSTNAME", System.getProperty("puppet.hostname"));
        }
        if (System.getProperty("puppet.env") != null) {
            globalProperties.setProperty("PUPPET_ENV", System.getProperty("puppet.env"));
        }
        if (System.getProperty("puppet.dns.available") != null) {
            globalProperties.setProperty("PUPPET_DNS_AVAILABLE", System.getProperty("puppet.dns.available"));
        }

        return globalProperties;
    }

//    public static MetaDataHolder getClusterLevelPayloadData (String appId, String groupName, int tenantId, String key,
//                                                                String hostname, String tenantRange, String clusterId,
//                                                                SubscribableContext subscribableCtxt,
//                                                                SubscribableInfoContext subscribableInfoCtxt,
//                                                                Cartridge cartridge) {
//
//        MetaDataHolder metaDataHolder;
//        if (groupName != null) {
//            metaDataHolder = new MetaDataHolder(appId, groupName, clusterId);
//        } else {
//            metaDataHolder = new MetaDataHolder(appId, clusterId);
//        }
//
//        Properties clusterLevelPayloadProperties = new Properties();
//        // app id
//        clusterLevelPayloadProperties.setProperty("APP_ID", appId);
//        // group name
//        if (groupName != null) {
//            clusterLevelPayloadProperties.setProperty("GROUP_NAME", groupName);
//        }
//        // service name
//        if (subscribableCtxt.getType() != null) {
//            clusterLevelPayloadProperties.put("SERVICE_NAME", subscribableCtxt.getType());
//        }
//        // host name
//        if  (hostname != null) {
//            clusterLevelPayloadProperties.put("HOST_NAME", hostname);
//        }
//        // multi tenant
//        clusterLevelPayloadProperties.put("MULTITENANT", String.valueOf(cartridge.isMultiTenant()));
//        // tenant range
//        if (tenantRange != null) {
//            clusterLevelPayloadProperties.put("TENANT_RANGE", tenantRange);
//        }
//        // cartridge alias
//        if (subscribableCtxt.getAlias() != null) {
//            clusterLevelPayloadProperties.put("CARTRIDGE_ALIAS", subscribableCtxt.getAlias());
//        }
//        // cluster id
//        if (clusterId != null) {
//            clusterLevelPayloadProperties.put("CLUSTER_ID", clusterId);
//        }
//        // repo url
//        if (subscribableInfoCtxt.getRepoUrl() != null) {
//            clusterLevelPayloadProperties.put("REPO_URL", subscribableInfoCtxt.getRepoUrl());
//        }
//        // ports
////        if (createPortMappingPayloadString(cartridge) != null) {
////            clusterLevelPayloadProperties.put("PORTS", createPortMappingPayloadString(cartridge));
////        }
//        // provider
//        if (cartridge.getProvider() != null) {
//            clusterLevelPayloadProperties.put("PROVIDER", cartridge.getProvider());
//        }
//        // tenant id
//        clusterLevelPayloadProperties.setProperty("TENANT_ID", String.valueOf(tenantId));
//        // cartridge key
//        clusterLevelPayloadProperties.setProperty("CARTRIDGE_KEY", key);
//        // get global payload params
//        //clusterLevelPayloadProperties.putAll(ApplicationUtils.getGlobalPayloadData());
//
//        metaDataHolder.setProperties(clusterLevelPayloadProperties);
//        return metaDataHolder;
//    }

    private static String createPortMappingPayloadString (CartridgeInfo cartridge) {

        // port mappings
        StringBuilder portMapBuilder = new StringBuilder();
        PortMapping[] portMappings = cartridge.getPortMappings();
        for (PortMapping portMapping : portMappings) {
            String port = portMapping.getPort();
            portMapBuilder.append(port).append("|");
        }

        // remove last "|" character
        String portMappingString = portMapBuilder.toString().replaceAll("\\|$", "");

        return portMappingString;
    }

    public static StringBuilder getTextPayload (String appId, String groupName, String clusterId) {

        StringBuilder payloadBuilder = new StringBuilder();
        payloadBuilder.append("APP_ID=" + appId);
        if (groupName != null) {
            payloadBuilder.append(",");
            payloadBuilder.append("GROUP_NAME=" + groupName);
        }
        payloadBuilder.append(",");
        payloadBuilder.append("CLUSTER_ID=" + clusterId);
        // puppet related
        if (System.getProperty("puppet.ip") != null) {
            payloadBuilder.append(",");
            payloadBuilder.append("PUPPET_IP=" + System.getProperty("puppet.ip"));
        }
        if (System.getProperty("puppet.hostname") != null) {
            payloadBuilder.append(",");
            payloadBuilder.append("PUPPET_HOSTNAME=" + System.getProperty("puppet.hostname"));
        }
        if (System.getProperty("puppet.env") != null) {
            payloadBuilder.append(",");
            payloadBuilder.append("PUPPET_ENV=" + System.getProperty("puppet.env"));
        }
        if (System.getProperty("puppet.dns.available") != null) {
            payloadBuilder.append(",");
            payloadBuilder.append("PUPPET_DNS_AVAILABLE=" + System.getProperty("puppet.dns.available"));
        }
        // meta data endpoint
       // if (MetaDataClientConfig.getInstance().getMetaDataServiceBaseUrl() != null) {
            // TODO
            //payloadBuilder.append(",");
            //payloadBuilder.append("METADATA_ENDPOINT=" + MetaDataClientConfig.getInstance().getMetaDataServiceBaseUrl());
       // }
        payloadBuilder.append(",");

        return payloadBuilder;
    }

    public static PayloadData createPayload(String appId, String groupName, CartridgeInfo cartridgeInfo, String subscriptionKey, int tenantId, String clusterId,
                                            String hostName, String repoUrl, String alias, Map<String, String> customPayloadEntries, String[] dependencyAliases, 
                                            org.apache.stratos.common.Properties properties, String oauthToken,String[] dependencyClusterIDs)
            throws ApplicationDefinitionException {

        //Create the payload
        BasicPayloadData basicPayloadData = createBasicPayload(appId, groupName, cartridgeInfo, subscriptionKey,
                clusterId, hostName, repoUrl, alias, tenantId, dependencyAliases, dependencyClusterIDs);
        //Populate the basic payload details
        basicPayloadData.populatePayload();

        PayloadData payloadData = PayloadFactory.getPayloadDataInstance(cartridgeInfo.getProvider(),
                cartridgeInfo.getType(), basicPayloadData);

        // get the payload parameters defined in the cartridgeInfo definition file for this cartridgeInfo type
        if (cartridgeInfo.getProperties() != null && cartridgeInfo.getProperties().length != 0) {

            org.apache.stratos.common.Properties cartridgeProps = AutoscalerUtil.toCommonProperties(cartridgeInfo.getProperties());
            
            if (cartridgeProps != null) {

                for (Property propertyEntry : cartridgeProps.getProperties()) {
                    // check if a property is related to the payload. Currently
                    // this is done by checking if the
                    // property name starts with 'payload_parameter.' suffix. If
                    // so the payload param name will
                    // be taken as the substring from the index of '.' to the
                    // end of the property name.
                    if (propertyEntry.getName().startsWith("payload_parameter.")) {
                        String payloadParamName = propertyEntry.getName();
                        String payloadParamSubstring = payloadParamName.substring(payloadParamName.indexOf(".") + 1);
                        if ("DEPLOYMENT".equals(payloadParamSubstring)) {
                            payloadData.getBasicPayloadData().setDeployment(payloadParamSubstring);
                            continue;
                        }
                        payloadData.add(payloadParamSubstring, propertyEntry.getValue());
                    }
                }
            }
        }
        
        // get subscription payload parameters (MB_IP, MB_PORT so on) and set them to payload (kubernetes scenario)
        if (properties != null && properties.getProperties() != null && properties.getProperties().length != 0) {
			for (Property property : properties.getProperties()) {
				if (property.getName().startsWith("payload_parameter.")) {
                    String payloadParamName = property.getName();
                    String payloadParamSubstring = payloadParamName.substring(payloadParamName.indexOf(".") + 1);
                    payloadData.add(payloadParamSubstring, property.getValue());
				}
			}
		}

        if(!StringUtils.isEmpty(oauthToken)){
            payloadData.add(TOKEN_PAYLOD_PARAM_NAME, oauthToken);
        }
        //check if there are any custom payload entries defined
        if (customPayloadEntries != null) {
            //add them to the payload
            Set<Map.Entry<String,String>> entrySet = customPayloadEntries.entrySet();
            for (Map.Entry<String, String> entry : entrySet) {
                payloadData.add(entry.getKey(), entry.getValue());
            }
        }

        return payloadData;
    }

    private static BasicPayloadData createBasicPayload(String appId, String groupName, CartridgeInfo cartridge,
                                                       String subscriptionKey, String clusterId,
                                                       String hostName, String repoUrl, String alias,
                                                       int tenantId, String[] dependencyAliases,String[] dependencyCLusterIDs) {

        BasicPayloadData basicPayloadData = new BasicPayloadData();
        basicPayloadData.setAppId(appId);
        basicPayloadData.setGroupName(groupName);
        basicPayloadData.setApplicationPath(cartridge.getBaseDir());
        basicPayloadData.setSubscriptionKey(subscriptionKey);
        //basicPayloadData.setDeployment("default");//currently hard coded to default
        basicPayloadData.setMultitenant(String.valueOf(cartridge.getMultiTenant()));
        basicPayloadData.setPortMappings(createPortMappingPayloadString(cartridge));
        basicPayloadData.setServiceName(cartridge.getType());
        basicPayloadData.setProvider(cartridge.getProvider());

        if(repoUrl != null) {
            basicPayloadData.setGitRepositoryUrl(repoUrl);
        }

        if (clusterId != null) {
            basicPayloadData.setClusterId(clusterId);
        }

        if (hostName != null) {
            basicPayloadData.setHostName(hostName);
        }

        if (alias != null) {
            basicPayloadData.setSubscriptionAlias(alias);
        }

        basicPayloadData.setTenantId(tenantId);

        basicPayloadData.setTenantRange("*");
        basicPayloadData.setDependencyAliases(dependencyAliases);
	    basicPayloadData.setDependencyClusterIDs(dependencyCLusterIDs);
//        if(cartridge.getExportingProperties() != null){
//            basicPayloadData.setExportingProperties(cartridge.getExportingProperties());
//            log.info("testing1 getExportingProperties " + cartridge.getExportingProperties());
//
//        }

        return basicPayloadData;
    }
}
