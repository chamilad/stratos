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

package org.apache.stratos.rest.endpoint.util.converter;

import org.apache.commons.lang.StringUtils;
import org.apache.stratos.autoscaler.stub.deployment.partition.ChildLevelNetworkPartition;
import org.apache.stratos.autoscaler.stub.deployment.partition.ChildLevelPartition;
import org.apache.stratos.autoscaler.stub.deployment.policy.ChildPolicy;
import org.apache.stratos.autoscaler.stub.pojo.*;
import org.apache.stratos.autoscaler.stub.pojo.Dependencies;
import org.apache.stratos.autoscaler.stub.pojo.ServiceGroup;
import org.apache.stratos.cloud.controller.stub.domain.*;
import org.apache.stratos.common.Properties;
import org.apache.stratos.common.Property;
import org.apache.stratos.common.beans.application.*;
import org.apache.stratos.common.beans.application.domain.mapping.DomainMappingBean;
import org.apache.stratos.common.beans.application.signup.ApplicationSignUpBean;
import org.apache.stratos.common.beans.artifact.repository.ArtifactRepositoryBean;
import org.apache.stratos.common.beans.cartridge.*;
import org.apache.stratos.common.beans.kubernetes.KubernetesClusterBean;
import org.apache.stratos.common.beans.kubernetes.KubernetesHostBean;
import org.apache.stratos.common.beans.kubernetes.KubernetesMasterBean;
import org.apache.stratos.common.beans.kubernetes.PortRangeBean;
import org.apache.stratos.common.beans.partition.ApplicationLevelNetworkPartitionBean;
import org.apache.stratos.common.beans.partition.ChildLevelNetworkPartitionBean;
import org.apache.stratos.common.beans.partition.ChildLevelPartitionBean;
import org.apache.stratos.common.beans.partition.PartitionBean;
import org.apache.stratos.common.beans.policy.autoscale.*;
import org.apache.stratos.common.beans.policy.deployment.ApplicationPolicyBean;
import org.apache.stratos.common.beans.policy.deployment.ChildPolicyBean;
import org.apache.stratos.common.beans.policy.deployment.DeploymentPolicyBean;
import org.apache.stratos.common.beans.topology.*;
import org.apache.stratos.common.util.CommonUtil;
import org.apache.stratos.manager.service.stub.domain.application.signup.ApplicationSignUp;
import org.apache.stratos.manager.service.stub.domain.application.signup.ArtifactRepository;
import org.apache.stratos.manager.service.stub.domain.domain.mapping.DomainMapping;
import org.apache.stratos.messaging.domain.application.Application;
import org.apache.stratos.messaging.domain.application.Group;
import org.apache.stratos.messaging.domain.instance.ApplicationInstance;
import org.apache.stratos.messaging.domain.instance.ClusterInstance;
import org.apache.stratos.messaging.domain.instance.GroupInstance;
import org.apache.stratos.messaging.domain.topology.Cluster;
import org.apache.stratos.rest.endpoint.exception.ServiceGroupDefinitionException;
import org.wso2.carbon.stratos.common.beans.TenantInfoBean;

import java.util.*;

public class ObjectConverter {

    public static CartridgeConfig convertCartridgeBeanToStubCartridgeConfig(
            CartridgeBean cartridgeBean) {

        CartridgeConfig cartridgeConfig = new CartridgeConfig();

	    cartridgeConfig.setType(cartridgeBean.getType());
	    cartridgeConfig.setHostName(cartridgeBean.getHost());
	    cartridgeConfig.setProvider(cartridgeBean.getProvider());
	    cartridgeConfig.setCategory(cartridgeBean.getCategory());
	    cartridgeConfig.setVersion(cartridgeBean.getVersion());
	    cartridgeConfig.setMultiTenant(cartridgeBean.isMultiTenant());
	    cartridgeConfig.setIsPublic(cartridgeBean.isPublic());
	    cartridgeConfig.setDisplayName(cartridgeBean.getDisplayName());
	    cartridgeConfig.setDescription(cartridgeBean.getDescription());
	    cartridgeConfig.setDefaultAutoscalingPolicy(cartridgeBean.getDefaultAutoscalingPolicy());
	    cartridgeConfig.setDefaultDeploymentPolicy(cartridgeBean.getDefaultDeploymentPolicy());
	    cartridgeConfig.setServiceGroup(cartridgeBean.getServiceGroup());
        cartridgeConfig.setTenantPartitions(cartridgeBean.getTenantPartitions());

        //deployment information
        if (cartridgeBean.getDeployment() != null) {
            cartridgeConfig.setBaseDir(cartridgeBean.getDeployment().getBaseDir());
            if (cartridgeBean.getDeployment().getDir() != null && !cartridgeBean.getDeployment().getDir().isEmpty()) {
                cartridgeConfig.setDeploymentDirs(cartridgeBean.getDeployment().getDir().
                        toArray(new String[cartridgeBean.getDeployment().getDir().size()]));
            }
        }
        //port mapping
        if (cartridgeBean.getPortMapping() != null && !cartridgeBean.getPortMapping().isEmpty()) {
            cartridgeConfig.setPortMappings(convertPortMappingBeansToStubPortMappings(cartridgeBean.getPortMapping()));
        }

        //persistance mapping
        if (cartridgeBean.getPersistence() != null) {
            cartridgeConfig.setPersistence(convertPersistenceBeanToStubPersistence(cartridgeBean.getPersistence()));
        }

        //IaaS
        if (cartridgeBean.getIaasProvider() != null && !cartridgeBean.getIaasProvider().isEmpty()) {
            cartridgeConfig.setIaasConfigs(convertIaasProviderBeansToStubIaasConfig(cartridgeBean.getIaasProvider()));
        }
        //Properties
        if (cartridgeBean.getProperty() != null && !cartridgeBean.getProperty().isEmpty()) {
            cartridgeConfig.setProperties(convertPropertyBeansToCCStubProperties(cartridgeBean.getProperty()));
        }

        if (cartridgeBean.getExportingProperties() != null) {
            List<String> propertiesList = cartridgeBean.getExportingProperties();
            String[] propertiesArray = propertiesList.toArray(new String[propertiesList.size()]);
            cartridgeConfig.setExportingProperties(propertiesArray);
        }
        return cartridgeConfig;
    }

    private static PortMapping[] convertPortMappingBeansToStubPortMappings(List<PortMappingBean> portMappingBeans) {

        //convert to an array
        PortMappingBean[] portMappingBeanArray = new PortMappingBean[portMappingBeans.size()];
        portMappingBeans.toArray(portMappingBeanArray);
        PortMapping[] portMappingArray = new PortMapping[portMappingBeanArray.length];

        for (int i = 0; i < portMappingBeanArray.length; i++) {
            PortMapping portMapping = new PortMapping();
            portMapping.setProtocol(portMappingBeanArray[i].getProtocol());
            portMapping.setPort(Integer.toString(portMappingBeanArray[i].getPort()));
            portMapping.setProxyPort(Integer.toString(portMappingBeanArray[i].getProxyPort()));
            portMappingArray[i] = portMapping;
        }

        return portMappingArray;
    }

    private static IaasConfig[] convertIaasProviderBeansToStubIaasConfig(List<IaasProviderBean> iaasProviderBeans) {

        //convert to an array
        IaasProviderBean[] iaasProviderBeansArray = new IaasProviderBean[iaasProviderBeans.size()];
        iaasProviderBeans.toArray(iaasProviderBeansArray);
        IaasConfig[] iaasConfigsArray = new IaasConfig[iaasProviderBeansArray.length];

        for (int i = 0; i < iaasProviderBeansArray.length; i++) {
            IaasConfig iaasConfig = new IaasConfig();
            iaasConfig.setType(iaasProviderBeansArray[i].getType());
            iaasConfig.setImageId(iaasProviderBeansArray[i].getImageId());
            iaasConfig.setName(iaasProviderBeansArray[i].getName());
            iaasConfig.setClassName(iaasProviderBeansArray[i].getClassName());
            iaasConfig.setCredential(iaasProviderBeansArray[i].getCredential());
            iaasConfig.setIdentity(iaasProviderBeansArray[i].getIdentity());
            iaasConfig.setProvider(iaasProviderBeansArray[i].getProvider());

            if (iaasProviderBeansArray[i].getProperty() != null && !iaasProviderBeansArray[i].getProperty().isEmpty()) {
                //set the Properties instance to IaasConfig instance
                iaasConfig.setProperties(convertPropertyBeansToCCStubProperties(iaasProviderBeansArray[i].getProperty()));
            }

            if (iaasProviderBeansArray[i].getNetworkInterfaces() != null && !iaasProviderBeansArray[i].getNetworkInterfaces().isEmpty()) {
                iaasConfig.setNetworkInterfaces(ObjectConverter.convertNetworkInterfaceBeansToNetworkInterfaces(iaasProviderBeansArray[i].getNetworkInterfaces()));
            }

            iaasConfigsArray[i] = iaasConfig;
        }
        return iaasConfigsArray;
    }

    public static Persistence convertPersistenceBeanToStubPersistence(org.apache.stratos.common.beans.cartridge.PersistenceBean persistenceBean) {
        Persistence persistence = new Persistence();
        persistence.setPersistanceRequired(persistenceBean.isRequired());
        VolumeBean[] volumeBean = new VolumeBean[persistenceBean.getVolume().size()];
        persistenceBean.getVolume().toArray(volumeBean);
        Volume[] volumes = new Volume[persistenceBean.getVolume().size()];
        for (int i = 0; i < volumes.length; i++) {
            Volume volume = new Volume();
            volume.setId(volumeBean[i].getId());
            volume.setVolumeId(volumeBean[i].getVolumeId());
            if (StringUtils.isEmpty(volume.getVolumeId())) {
                volume.setSize(Integer.parseInt(volumeBean[i].getSize()));
            }

            volume.setDevice(volumeBean[i].getDevice());
            volume.setRemoveOntermination(volumeBean[i].isRemoveOnTermination());
            volume.setMappingPath(volumeBean[i].getMappingPath());
            volume.setSnapshotId(volumeBean[i].getSnapshotId());

            volumes[i] = volume;
        }
        persistence.setVolumes(volumes);
        return persistence;

    }

    public static Properties convertPropertyBeansToProperties(List<org.apache.stratos.common.beans.cartridge.PropertyBean> propertyBeans) {
        org.apache.stratos.common.beans.cartridge.PropertyBean[] propertyBeansArray = new org.apache.stratos.common.beans.cartridge.PropertyBean[propertyBeans.size()];
        propertyBeans.toArray(propertyBeansArray);
        Property[] propertyArray = new Property[propertyBeansArray.length];

        for (int j = 0; j < propertyBeansArray.length; j++) {
            Property property = new Property();
            property.setName(propertyBeansArray[j].getName());
            property.setValue(propertyBeansArray[j].getValue());
            propertyArray[j] = property;
        }

        Properties properties = new Properties();
        properties.setProperties(propertyArray);
        return properties;
    }
    
    public static org.apache.stratos.cloud.controller.stub.Properties convertPropertyBeansToCCStubProperties(
            List<org.apache.stratos.common.beans.cartridge.PropertyBean> propertyBeans) {
        if (propertyBeans == null) {
            return null;
        }

        List<org.apache.stratos.cloud.controller.stub.Property> stubPropertiesList =
                new ArrayList<org.apache.stratos.cloud.controller.stub.Property>();

        for (org.apache.stratos.common.beans.cartridge.PropertyBean propertyBean : propertyBeans) {
            org.apache.stratos.cloud.controller.stub.Property stubProperty = new org.apache.stratos.cloud.controller.stub.Property();
            stubProperty.setName(propertyBean.getName());
            stubProperty.setValue(propertyBean.getValue());
            stubPropertiesList.add(stubProperty);
        }

        org.apache.stratos.cloud.controller.stub.Properties stubProperties = new org.apache.stratos.cloud.controller.stub.Properties();
        org.apache.stratos.cloud.controller.stub.Property[] stubPropertiesArray =
                stubPropertiesList.toArray(new org.apache.stratos.cloud.controller.stub.Property[stubPropertiesList.size()]);
        stubProperties.setProperties(stubPropertiesArray);

        return stubProperties;
    }


    public static org.apache.stratos.autoscaler.stub.Properties convertProperyBeansToStubProperties(
            List<org.apache.stratos.common.beans.cartridge.PropertyBean> propertyBeans) {
        if (propertyBeans == null || propertyBeans.isEmpty()) {
            return null;
        }

        //convert to an array
        org.apache.stratos.common.beans.cartridge.PropertyBean[] propertyBeansArray = new org.apache.stratos.common.beans.cartridge.PropertyBean[propertyBeans.size()];
        propertyBeans.toArray(propertyBeansArray);
        org.apache.stratos.autoscaler.stub.Property[] propertyArray = new org.apache.stratos.autoscaler.stub.Property[propertyBeansArray.length];

        for (int j = 0; j < propertyBeansArray.length; j++) {
            org.apache.stratos.autoscaler.stub.Property property = new org.apache.stratos.autoscaler.stub.Property();
            property.setName(propertyBeansArray[j].getName());
            property.setValue(propertyBeansArray[j].getValue());
            propertyArray[j] = property;
        }

        org.apache.stratos.autoscaler.stub.Properties properties = new org.apache.stratos.autoscaler.stub.Properties();
        properties.setProperties(propertyArray);
        return properties;
    }

    private static NetworkInterfaces convertNetworkInterfaceBeansToNetworkInterfaces(List<NetworkInterfaceBean> networkInterfaceBeans) {
        NetworkInterface[] networkInterfacesArray = new NetworkInterface[networkInterfaceBeans.size()];

        int i = 0;
        for (NetworkInterfaceBean nib : networkInterfaceBeans) {
            NetworkInterface networkInterface = new NetworkInterface();
            networkInterface.setNetworkUuid(nib.getNetworkUuid());
            networkInterface.setFixedIp(nib.getFixedIp());
            networkInterface.setPortUuid(nib.getPortUuid());
            if (nib.getFloatingNetworks() != null && !nib.getFloatingNetworks().isEmpty()) {
            	networkInterface.setFloatingNetworks(ObjectConverter.convertFloatingNetworkBeansToFloatingNetworks(nib.getFloatingNetworks()));
            }

            networkInterfacesArray[i++] = networkInterface;
        }

        NetworkInterfaces networkInterfaces = new NetworkInterfaces();
        networkInterfaces.setNetworkInterfaces(networkInterfacesArray);
        return networkInterfaces;
    }
    
    private static FloatingNetworks convertFloatingNetworkBeansToFloatingNetworks(List<FloatingNetworkBean> floatingNetworkBeans) {

        FloatingNetwork[] floatingNetworksArray = new FloatingNetwork[floatingNetworkBeans.size()];

        int i = 0;
        for (FloatingNetworkBean floatingNetworkBean : floatingNetworkBeans) {
            FloatingNetwork floatingNetwork = new FloatingNetwork();
            floatingNetwork.setName(floatingNetworkBean.getName());
            floatingNetwork.setNetworkUuid(floatingNetworkBean.getNetworkUuid());
            floatingNetwork.setFloatingIP(floatingNetworkBean.getFloatingIP());
            floatingNetworksArray[i++] = floatingNetwork;
        }

        FloatingNetworks floatingNetworks = new FloatingNetworks();
        floatingNetworks.setFloatingNetworks(floatingNetworksArray);
        return floatingNetworks;
    }

    public static org.apache.stratos.autoscaler.stub.deployment.partition.Partition convertStubPartitionToPartition
            (PartitionBean partition) {

        org.apache.stratos.autoscaler.stub.deployment.partition.Partition stubPartition = new
                org.apache.stratos.autoscaler.stub.deployment.partition.Partition();

        stubPartition.setId(partition.getId());
        stubPartition.setDescription(partition.getDescription());
        stubPartition.setIsPublic(partition.isPublic());
        stubPartition.setProvider(partition.getProvider());
        stubPartition.setKubernetesClusterId(partition.getKubernetesClusterId());

        if (partition.getProperty() != null && !partition.getProperty().isEmpty()) {
            stubPartition.setProperties(convertProperyBeansToStubProperties(partition.getProperty()));
        }
        return stubPartition;
    }

    public static org.apache.stratos.autoscaler.stub.autoscale.policy.AutoscalePolicy convertToCCAutoscalerPojo(AutoscalePolicyBean
                                                                                                                        autoscalePolicyBean) {

        org.apache.stratos.autoscaler.stub.autoscale.policy.AutoscalePolicy autoscalePolicy = new
                org.apache.stratos.autoscaler.stub.autoscale.policy.AutoscalePolicy();

        autoscalePolicy.setId(autoscalePolicyBean.getId());
        autoscalePolicy.setDescription(autoscalePolicyBean.getDescription());
        autoscalePolicy.setIsPublic(autoscalePolicyBean.getIsPublic());
        autoscalePolicy.setDisplayName(autoscalePolicyBean.getDisplayName());
        autoscalePolicy.setInstanceRoundingFactor(autoscalePolicyBean.getInstanceRoundingFactor());

        if (autoscalePolicyBean.getLoadThresholds() != null) {

            org.apache.stratos.autoscaler.stub.autoscale.policy.LoadThresholds loadThresholds = new
                    org.apache.stratos.autoscaler.stub.autoscale.policy.LoadThresholds();

            if (autoscalePolicyBean.getLoadThresholds().getLoadAverage() != null) {

                //set load average information
                loadThresholds.setLoadAverageThreshold(
                        autoscalePolicyBean.getLoadThresholds().getLoadAverage().getThreshold());
            }
            if (autoscalePolicyBean.getLoadThresholds().getRequestsInFlight() != null) {

                //set request in flight information
                loadThresholds.setRequestsInFlightThreshold(
                        autoscalePolicyBean.getLoadThresholds().getRequestsInFlight().getThreshold());
            }
            if (autoscalePolicyBean.getLoadThresholds().getMemoryConsumption() != null) {

                //set memory consumption information
                loadThresholds.setMemoryConsumptionThreshold(
                        autoscalePolicyBean.getLoadThresholds().
                                getMemoryConsumption().getThreshold());
            }

            autoscalePolicy.setLoadThresholds(loadThresholds);
        }

        return autoscalePolicy;
    }

    public static org.apache.stratos.autoscaler.stub.deployment.policy.DeploymentPolicy convetToASDeploymentPolicyPojo(
            String applicationId, DeploymentPolicyBean deploymentPolicyBean) {

        org.apache.stratos.autoscaler.stub.deployment.policy.DeploymentPolicy deploymentPolicy =
                new org.apache.stratos.autoscaler.stub.deployment.policy.DeploymentPolicy();

        deploymentPolicy.setApplicationId(applicationId);
        deploymentPolicy.setDescription(deploymentPolicyBean.getDescription());
        deploymentPolicy.setIsPublic(deploymentPolicyBean.isPublic());
        if (deploymentPolicyBean.getApplicationPolicy() != null
                && deploymentPolicyBean.getApplicationPolicy().getNetworkPartition() != null
                && !deploymentPolicyBean.getApplicationPolicy().getNetworkPartition().isEmpty()) {
            deploymentPolicy
                    .setApplicationLevelNetworkPartitions(
                            convertApplicationLevelNetworkPartitionToStubApplicationLevelNetworkPartition(
                                    deploymentPolicyBean.getApplicationPolicy().getNetworkPartition()));
        }

        if (deploymentPolicyBean.getChildPolicies() != null && !deploymentPolicyBean.getChildPolicies().isEmpty()) {
            deploymentPolicy.setChildPolicies(convertChildPoliciesToStubChildPolicies(deploymentPolicyBean.getChildPolicies()));
        }

        return deploymentPolicy;
    }

    public static DeploymentPolicyBean convertStubDeploymentPolicyToDeploymentPolicy(
            org.apache.stratos.autoscaler.stub.deployment.policy.DeploymentPolicy stubDeploymentPolicy) {

        if(stubDeploymentPolicy == null) {
            return null;
        }

        DeploymentPolicyBean deploymentPolicy = new DeploymentPolicyBean();
        deploymentPolicy.setDescription(stubDeploymentPolicy.getDescription());
        deploymentPolicy.setPublic(stubDeploymentPolicy.getIsPublic());
        if (stubDeploymentPolicy.getApplicationLevelNetworkPartitions() != null) {
            org.apache.stratos.autoscaler.stub.deployment.partition.ApplicationLevelNetworkPartition[]
                    networkPartitions = stubDeploymentPolicy.getApplicationLevelNetworkPartitions();
            if(networkPartitions != null) {
                deploymentPolicy.setApplicationPolicy(new ApplicationPolicyBean());
                List<ApplicationLevelNetworkPartitionBean> networkPartitionList = new ArrayList<ApplicationLevelNetworkPartitionBean>();
                for(org.apache.stratos.autoscaler.stub.deployment.partition.ApplicationLevelNetworkPartition
                        networkPartition : networkPartitions) {
                    if(networkPartition != null) {
                        networkPartitionList.add(convertStubNetworkPartitionToNetworkPartition(networkPartition));
                    }
                }
                deploymentPolicy.getApplicationPolicy().setNetworkPartition(networkPartitionList);
            }
        }

        if (stubDeploymentPolicy.getChildPolicies() != null) {
            List<ChildPolicyBean> childPolicyBeanList =
                    new ArrayList<ChildPolicyBean>();
            for(org.apache.stratos.autoscaler.stub.deployment.policy.ChildPolicy stubChildDeploymentPolicy :
                    stubDeploymentPolicy.getChildPolicies()) {
                if(stubChildDeploymentPolicy != null) {
                    childPolicyBeanList.add(convertStubChildPolicyToChildPolicy(stubChildDeploymentPolicy));
                }
            }
            deploymentPolicy.setChildPolicies(childPolicyBeanList);
        }
        return deploymentPolicy;
    }

    private static ChildPolicyBean
    convertStubChildPolicyToChildPolicy(ChildPolicy stubChildDeploymentPolicy) {
        if(stubChildDeploymentPolicy == null) {
            return null;
        }
        ChildPolicyBean childPolicyBean = new
                ChildPolicyBean();
        childPolicyBean.setAlias(stubChildDeploymentPolicy.getAlias());
        if(stubChildDeploymentPolicy.getChildLevelNetworkPartitions() != null) {
            List<ChildLevelNetworkPartitionBean> networkPartitionList
                    = new ArrayList<ChildLevelNetworkPartitionBean>();
            for(org.apache.stratos.autoscaler.stub.deployment.partition.ChildLevelNetworkPartition
                    stubChildLevelNetworkPartition : stubChildDeploymentPolicy.getChildLevelNetworkPartitions()) {
                networkPartitionList.add(convertStubChildLevelNetworkPartitionToChildLevelNetworkPartition(stubChildLevelNetworkPartition));
            }
            childPolicyBean.setNetworkPartition(networkPartitionList);
        }
        return childPolicyBean;
    }

    private static ChildLevelNetworkPartitionBean
    convertStubChildLevelNetworkPartitionToChildLevelNetworkPartition(
            ChildLevelNetworkPartition stubChildLevelNetworkPartition) {
        if(stubChildLevelNetworkPartition == null) {
            return null;
        }
        ChildLevelNetworkPartitionBean childLevelNetworkPartition =
                new ChildLevelNetworkPartitionBean();
        childLevelNetworkPartition.setId(stubChildLevelNetworkPartition.getId());
        childLevelNetworkPartition.setPartitionAlgo(stubChildLevelNetworkPartition.getPartitionAlgo());
        if(stubChildLevelNetworkPartition.getChildLevelPartitions() != null) {
            List<ChildLevelPartitionBean> partitionList =
                    new ArrayList<ChildLevelPartitionBean>();
            for(org.apache.stratos.autoscaler.stub.deployment.partition.ChildLevelPartition stubChildLevelPartition : stubChildLevelNetworkPartition.getChildLevelPartitions()) {
                partitionList.add(convertStubChildLevelPartitionToChildLevelPartition(stubChildLevelPartition));
            }
            childLevelNetworkPartition.setPartitions(partitionList);
        }
        return childLevelNetworkPartition;
    }

    private static ChildLevelPartitionBean
    convertStubChildLevelPartitionToChildLevelPartition(ChildLevelPartition stubChildLevelPartition) {
        if(stubChildLevelPartition == null) {
            return null;
        }
        ChildLevelPartitionBean childLevelPartition =
                new ChildLevelPartitionBean();
        childLevelPartition.setId(stubChildLevelPartition.getPartitionId());
        childLevelPartition.setMax(stubChildLevelPartition.getMax());
        return childLevelPartition;
    }

    private static ApplicationLevelNetworkPartitionBean convertStubNetworkPartitionToNetworkPartition(
            org.apache.stratos.autoscaler.stub.deployment.partition.ApplicationLevelNetworkPartition stubNetworkPartition) {
        if(stubNetworkPartition == null) {
            return null;
        }

        ApplicationLevelNetworkPartitionBean networkPartition = new ApplicationLevelNetworkPartitionBean();
        networkPartition.setId(stubNetworkPartition.getId());
        networkPartition.setActiveByDefault(stubNetworkPartition.getActiveByDefault());
        if(stubNetworkPartition.getPartitions() != null) {
            List<PartitionBean> partitionList = new ArrayList<PartitionBean>();
            for(org.apache.stratos.autoscaler.stub.deployment.partition.Partition stubPartition :
                    stubNetworkPartition.getPartitions()) {
                if(stubPartition != null) {
                    partitionList.add(convertStubPartitionToPartition(stubPartition));
                }
            }
            networkPartition.setPartitions(partitionList);
        }
        return networkPartition;
    }

    private static PartitionBean convertStubPartitionToPartition(org.apache.stratos.autoscaler.stub.deployment.partition.Partition stubPartition) {
        if(stubPartition == null) {
            return null;
        }
        PartitionBean partition = new PartitionBean();
        partition.setId(stubPartition.getId());
        partition.setPublic(stubPartition.getIsPublic());
        partition.setDescription(stubPartition.getDescription());
        partition.setProvider(stubPartition.getProvider());
        partition.setKubernetesClusterId(stubPartition.getKubernetesClusterId());
        if(stubPartition.getProperties() != null) {
            List<org.apache.stratos.common.beans.cartridge.PropertyBean> propertyBeanList = new ArrayList<org.apache.stratos.common.beans.cartridge.PropertyBean>();
            for(org.apache.stratos.autoscaler.stub.Property stubProperty : stubPartition.getProperties().getProperties()) {
                if(stubProperty != null) {
                    propertyBeanList.add(convertStubPropertyToPropertyBean(stubProperty));
                }
            }
            partition.setProperty(propertyBeanList);
        }
        return partition;
    }

    private static org.apache.stratos.common.beans.cartridge.PropertyBean convertStubPropertyToPropertyBean(org.apache.stratos.autoscaler.stub.Property stubProperty) {
        if ((stubProperty == null) || (!(stubProperty.getValue() instanceof String))) {
            return null;
        }

        org.apache.stratos.common.beans.cartridge.PropertyBean propertyBean = new org.apache.stratos.common.beans.cartridge.PropertyBean();
        propertyBean.setName(stubProperty.getName());
        propertyBean.setValue(String.valueOf(stubProperty.getValue()));
        return propertyBean;
    }

    private static org.apache.stratos.autoscaler.stub.deployment.partition.ApplicationLevelNetworkPartition[]
        convertApplicationLevelNetworkPartitionToStubApplicationLevelNetworkPartition(
            List<ApplicationLevelNetworkPartitionBean> networkPartitionBeans) {

        org.apache.stratos.autoscaler.stub.deployment.partition.ApplicationLevelNetworkPartition[]
                appNWPartitions = new
                org.apache.stratos.autoscaler.stub.deployment.partition.ApplicationLevelNetworkPartition
                [networkPartitionBeans.size()];

        for (int i = 0; i < networkPartitionBeans.size(); i++) {
            org.apache.stratos.autoscaler.stub.deployment.partition.ApplicationLevelNetworkPartition appNWPartition = new
                    org.apache.stratos.autoscaler.stub.deployment.partition.ApplicationLevelNetworkPartition();

            ApplicationLevelNetworkPartitionBean networkPartition = networkPartitionBeans.get(i);

            appNWPartition.setId(networkPartition.getId());
            appNWPartition.setKubernetesClusterId(networkPartition.getKubernetesClusterId());
            appNWPartition.setActiveByDefault(networkPartition.isActiveByDefault());
            if (networkPartition.getPartitions() != null && !networkPartition.getPartitions().isEmpty()) {
                appNWPartition.setPartitions(convertToCCPartitionPojos(networkPartition.getPartitions()));
            }

            appNWPartitions[i] = appNWPartition;
        }

        return appNWPartitions;
    }

    private static ChildPolicy[] convertChildPoliciesToStubChildPolicies(List<ChildPolicyBean> childPolicies) {
        ChildPolicy[] stubChildPolicyArray = new ChildPolicy[childPolicies.size()];
        for (int i = 0; i < childPolicies.size(); i++) {
            ChildPolicy childPolicy = new ChildPolicy();
            childPolicy.setAlias(childPolicies.get(i).getAlias());
            childPolicy.setChildLevelNetworkPartitions(convertToCCChildNetworkPartition(childPolicies.get(i).getNetworkPartition()));
            stubChildPolicyArray[i] = childPolicy;
        }
        return stubChildPolicyArray;
    }

    private static ChildLevelNetworkPartition[] convertToCCChildNetworkPartition(List<ChildLevelNetworkPartitionBean> networkPartitions) {

        ChildLevelNetworkPartition[] childLevelNetworkPartitions = new ChildLevelNetworkPartition[networkPartitions.size()];

        for (int i = 0; i < networkPartitions.size(); i++) {
            ChildLevelNetworkPartition childLevelNetworkPartition = new ChildLevelNetworkPartition();
            childLevelNetworkPartition.setId(networkPartitions.get(i).getId());
            childLevelNetworkPartition.setPartitionAlgo(networkPartitions.get(i).getPartitionAlgo());
            childLevelNetworkPartition.setChildLevelPartitions(convertToCCChildPartitionPojos(networkPartitions.get(i).getPartitions()));

            childLevelNetworkPartitions[i] = childLevelNetworkPartition;
        }

        return childLevelNetworkPartitions;
    }

    public static ClusterBean convertClusterToClusterBean(Cluster cluster, String alias) {
        ClusterBean clusterBean = new
                ClusterBean();
        clusterBean.setAlias(alias);
        clusterBean.setServiceName(cluster.getServiceName());
        clusterBean.setClusterId(cluster.getClusterId());
        clusterBean.setLbCluster(cluster.isLbCluster());
        clusterBean.setTenantRange(cluster.getTenantRange());
        clusterBean.setProperty(convertJavaUtilPropertiesToPropertyBeans(cluster.getProperties()));
        clusterBean.setMember(new ArrayList<MemberBean>());
        clusterBean.setHostNames(new ArrayList<String>());
        Collection<ClusterInstance> clusterInstances = cluster.getClusterInstances();
        List<InstanceBean> instancesList =
        		new ArrayList<InstanceBean>();
		if (clusterInstances != null) {
			for (ClusterInstance clusterInstance : clusterInstances) {
				InstanceBean instance =
						new InstanceBean();
				instance.setInstanceId(clusterInstance.getInstanceId());
				instance.setStatus(clusterInstance.getStatus().toString());
				instancesList.add(instance);
			}
			clusterBean.setInstances(instancesList);
		}

        for (org.apache.stratos.messaging.domain.topology.Member member : cluster.getMembers()) {
            MemberBean memberBean = new MemberBean();
            memberBean.setServiceName(member.getServiceName());
            memberBean.setClusterId(member.getClusterId());
            memberBean.setMemberId(member.getMemberId());
            memberBean.setClusterInstanceId(member.getClusterInstanceId());

            memberBean.setLbClusterId(member.getLbClusterId());
            memberBean.setNetworkPartitionId(member.getNetworkPartitionId());
            memberBean.setPartitionId(member.getPartitionId());
            if (member.getDefaultPrivateIP() == null) {
                memberBean.setDefaultPrivateIP("NULL");
            } else {
                memberBean.setDefaultPrivateIP(member.getDefaultPrivateIP());
            }
            if (member.getDefaultPublicIP() == null) {
                memberBean.setDefaultPublicIP("NULL");
            } else {
                memberBean.setDefaultPublicIP(member.getDefaultPublicIP());
            }
            memberBean.setMemberPrivateIPs(member.getMemberPrivateIPs());
            memberBean.setMemberPublicIPs(member.getMemberPublicIPs());
            memberBean.setStatus(member.getStatus().toString());
            memberBean.setProperty(convertJavaUtilPropertiesToPropertyBeans(member.getProperties()));
            clusterBean.getMember().add(memberBean);
        }

        for (String hostname : cluster.getHostNames()) {
            clusterBean.getHostNames().add(hostname);
        }
        return clusterBean;
    }

    public static ClusterInstanceBean convertClusterToClusterInstanceBean(String instanceId,
                                                                          Cluster cluster, String alias) {
        ClusterInstanceBean clusterInstanceBean = new ClusterInstanceBean();
        clusterInstanceBean.setAlias(alias);
        clusterInstanceBean.setServiceName(cluster.getServiceName());
        clusterInstanceBean.setClusterId(cluster.getClusterId());
        clusterInstanceBean.setInstanceId(instanceId);
        clusterInstanceBean.setParentInstanceId(instanceId);
        if (cluster.getInstanceContexts(instanceId) != null) {
            clusterInstanceBean.setStatus(cluster.getInstanceContexts(instanceId).
                    getStatus().toString());
        }
        clusterInstanceBean.setTenantRange(cluster.getTenantRange());
        clusterInstanceBean.setMember(new ArrayList<MemberBean>());
        clusterInstanceBean.setHostNames(new ArrayList<String>());

        for (org.apache.stratos.messaging.domain.topology.Member member : cluster.getMembers()) {
            if (member.getClusterInstanceId().equals(instanceId)) {
                MemberBean memberBean = new MemberBean();
                memberBean.setClusterId(member.getClusterId());
                memberBean.setLbClusterId(member.getLbClusterId());
                memberBean.setNetworkPartitionId(member.getNetworkPartitionId());
                memberBean.setPartitionId(member.getPartitionId());
                memberBean.setMemberId(member.getMemberId());
                if (member.getDefaultPrivateIP() == null) {
                    memberBean.setDefaultPrivateIP("NULL");
                } else {
                    memberBean.setDefaultPrivateIP(member.getDefaultPrivateIP());
                }
                if (member.getDefaultPublicIP() == null) {
                    memberBean.setDefaultPublicIP("NULL");
                } else {
                    memberBean.setDefaultPublicIP(member.getDefaultPublicIP());
                }
                memberBean.setMemberPrivateIPs(member.getMemberPrivateIPs());
                memberBean.setMemberPublicIPs(member.getMemberPublicIPs());
                memberBean.setServiceName(member.getServiceName());
                memberBean.setStatus(member.getStatus().toString());
                memberBean.setProperty(convertJavaUtilPropertiesToPropertyBeans(member.getProperties()));
                clusterInstanceBean.getMember().add(memberBean);
            }

        }

        for (String hostname : cluster.getHostNames()) {
            clusterInstanceBean.getHostNames().add(hostname);
        }
        return clusterInstanceBean;
    }

    private static org.apache.stratos.autoscaler.stub.deployment.partition.Partition[] convertToCCPartitionPojos
            (List<PartitionBean> partitionList) {

        org.apache.stratos.autoscaler.stub.deployment.partition.Partition[] partitions =
                new org.apache.stratos.autoscaler.stub.deployment.partition.Partition[partitionList.size()];
        for (int i = 0; i < partitionList.size(); i++) {
            partitions[i] = convertStubPartitionToPartition(partitionList.get(i));
        }

        return partitions;
    }

    private static ChildLevelPartition[] convertToCCChildPartitionPojos
            (List<ChildLevelPartitionBean> partitionList) {

        ChildLevelPartition[] childLevelPartitions = new ChildLevelPartition[partitionList.size()];
        for (int i = 0; i < partitionList.size(); i++) {
            ChildLevelPartition childLevelPartition = new ChildLevelPartition();
            childLevelPartition.setPartitionId(partitionList.get(i).getId());
            childLevelPartition.setMax(partitionList.get(i).getMax());

            childLevelPartitions[i] = childLevelPartition;
        }

        return childLevelPartitions;
    }

    public static PartitionBean[] populatePartitionPojos(org.apache.stratos.cloud.controller.stub.domain.Partition[]
                                                             partitions) {

        PartitionBean[] partitionBeans;
        if (partitions == null) {
            partitionBeans = new PartitionBean[0];
            return partitionBeans;
        }

        partitionBeans = new PartitionBean[partitions.length];
        for (int i = 0; i < partitions.length; i++) {
            partitionBeans[i] = populatePartitionPojo(partitions[i]);
        }
        return partitionBeans;
    }

    public static PartitionBean populatePartitionPojo(org.apache.stratos.cloud.controller.stub.domain.Partition
                                                          partition) {

        PartitionBean partitionBeans = new PartitionBean();
        if (partition == null) {
            return partitionBeans;
        }

        partitionBeans.setId(partition.getId());
        partitionBeans.setDescription(partition.getDescription());
        partitionBeans.setPublic(partition.getIsPublic());
        partitionBeans.setProvider(partition.getProvider());
        /*partitionBeans.partitionMin = partition.getPartitionMin();
        partitionBeans.partitionMax = partition.getPartitionMax();*/
        //properties 
        if (partition.getProperties() != null) {
            List<org.apache.stratos.common.beans.cartridge.PropertyBean> propertyBeans = convertCCStubPropertiesToPropertyBeans(partition.getProperties());
            partitionBeans.setProperty(propertyBeans);
        }

        return partitionBeans;
    }

    private static List<org.apache.stratos.common.beans.cartridge.PropertyBean> convertJavaUtilPropertiesToPropertyBeans(java.util.Properties properties) {

        List<org.apache.stratos.common.beans.cartridge.PropertyBean> propertyBeans = null;
        if (properties != null && !properties.isEmpty()) {
            Enumeration<?> e = properties.propertyNames();
            propertyBeans = new ArrayList<org.apache.stratos.common.beans.cartridge.PropertyBean>();

            while (e.hasMoreElements()) {
                String key = (String) e.nextElement();
                String value = properties.getProperty(key);
                org.apache.stratos.common.beans.cartridge.PropertyBean propertyBean = new org.apache.stratos.common.beans.cartridge.PropertyBean();
                propertyBean.setName(key);
                propertyBean.setValue(value);
                propertyBeans.add(propertyBean);
            }
        }
        return propertyBeans;
    }

    public static AutoscalePolicyBean[] convertStubAutoscalePoliciesToAutoscalePolicies(
            org.apache.stratos.autoscaler.stub.autoscale.policy.AutoscalePolicy[] autoscalePolicies) {

        AutoscalePolicyBean[] autoscalePolicyBeans;
        if (autoscalePolicies == null) {
            autoscalePolicyBeans = new AutoscalePolicyBean[0];
            return autoscalePolicyBeans;
        }

        autoscalePolicyBeans = new AutoscalePolicyBean[autoscalePolicies.length];
        for (int i = 0; i < autoscalePolicies.length; i++) {
            autoscalePolicyBeans[i] = convertStubAutoscalePolicyToAutoscalePolicy(autoscalePolicies[i]);
        }
        return autoscalePolicyBeans;
    }

    public static AutoscalePolicyBean convertStubAutoscalePolicyToAutoscalePolicy(org.apache.stratos.autoscaler.stub.autoscale.policy.AutoscalePolicy
                                                                                      autoscalePolicy) {
        if (autoscalePolicy == null) {
            return null;
        }

        AutoscalePolicyBean autoscalePolicyBean = new AutoscalePolicyBean();
        autoscalePolicyBean.setId(autoscalePolicy.getId());
        autoscalePolicyBean.setDescription(autoscalePolicy.getDescription());
        autoscalePolicyBean.setIsPublic(autoscalePolicy.getIsPublic());
        autoscalePolicyBean.setDisplayName(autoscalePolicy.getDisplayName());
        autoscalePolicyBean.setDescription(autoscalePolicy.getDescription());
        autoscalePolicyBean.setInstanceRoundingFactor(autoscalePolicy.getInstanceRoundingFactor());
        if (autoscalePolicy.getLoadThresholds() != null) {
            autoscalePolicyBean.setLoadThresholds(convertStubLoadThreasholdsToLoadThresholds(autoscalePolicy.getLoadThresholds()));
        }

        return autoscalePolicyBean;
    }

    private static LoadThresholdsBean convertStubLoadThreasholdsToLoadThresholds(org.apache.stratos.autoscaler.stub.autoscale.policy.LoadThresholds
                                                                                     loadThresholds) {

        LoadThresholdsBean loadThresholdBean = new LoadThresholdsBean();
        if (loadThresholds.getLoadAverageThreshold() != 0) {
            LoadAverageThresholdsBean loadAverage = new LoadAverageThresholdsBean();
            loadAverage.setThreshold(loadThresholds.getLoadAverageThreshold());
            loadThresholdBean.setLoadAverage(loadAverage);
        }
        if (loadThresholds.getMemoryConsumptionThreshold() != 0) {
            MemoryConsumptionThresholdsBean memoryConsumption = new MemoryConsumptionThresholdsBean();
            memoryConsumption.setThreshold(loadThresholds.getMemoryConsumptionThreshold());
            loadThresholdBean.setMemoryConsumption(memoryConsumption);
        }
        if (loadThresholds.getRequestsInFlightThreshold() != 0) {
            RequestsInFlightThresholdsBean requestsInFlight = new RequestsInFlightThresholdsBean();
            requestsInFlight.setThreshold(loadThresholds.getRequestsInFlightThreshold());
            loadThresholdBean.setRequestsInFlight(requestsInFlight);
        }

        return loadThresholdBean;
    }

    public static ApplicationLevelNetworkPartitionBean convertStubApplicationLevelNetworkPartitionToApplicationLevelNetworkPartition(
            org.apache.stratos.autoscaler.stub.deployment.partition.ApplicationLevelNetworkPartition stubApplicationLevelNetworkPartition) {

        ApplicationLevelNetworkPartitionBean networkPartitionBean = new ApplicationLevelNetworkPartitionBean();
        if (stubApplicationLevelNetworkPartition == null) {
            return networkPartitionBean;
        }

        networkPartitionBean.setId(stubApplicationLevelNetworkPartition.getId());
        networkPartitionBean.setKubernetesClusterId(stubApplicationLevelNetworkPartition.getKubernetesClusterId());
        networkPartitionBean.setActiveByDefault(stubApplicationLevelNetworkPartition.getActiveByDefault());

        //FIXME update with new deployment policy pattern
//        networkPartitionBean.partitionAlgo = partitionGroup.getPartitionAlgo();
//        if (partitionGroup.getPartitions() != null && partitionGroup.getPartitions().length > 0) {
//            partitionGroupBean.partition = convertStubPartitionsToPartitions(partitionGroup.getPartitions());
//        }

        return networkPartitionBean;
    }

    public static ApplicationLevelNetworkPartitionBean[] convertStubApplicationLevelNetworkPartitionsToApplicationLevelNetworkPartitions(
            org.apache.stratos.autoscaler.stub.deployment.partition.ApplicationLevelNetworkPartition[] partitionGroups) {

        ApplicationLevelNetworkPartitionBean[] networkPartitionGroupsBeans;
        if (partitionGroups == null) {
            networkPartitionGroupsBeans = new ApplicationLevelNetworkPartitionBean[0];
            return networkPartitionGroupsBeans;
        }

        networkPartitionGroupsBeans = new ApplicationLevelNetworkPartitionBean[partitionGroups.length];

        for (int i = 0; i < partitionGroups.length; i++) {
            networkPartitionGroupsBeans[i] = convertStubApplicationLevelNetworkPartitionToApplicationLevelNetworkPartition(partitionGroups[i]);
        }

        return networkPartitionGroupsBeans;
    }

//    public static ServiceDefinitionBean convertToServiceDefinitionBean(Service service) {
//
//        ServiceDefinitionBean serviceDefinitionBean = new ServiceDefinitionBean();
//        serviceDefinitionBean.setCartridgeType(service.getType());
//        serviceDefinitionBean.setTenantRange(service.getTenantRange());
//        serviceDefinitionBean.setClusterDomain(service.getClusterId());
//        serviceDefinitionBean.setIsPublic(service.getIsPublic());
//        serviceDefinitionBean.setAutoscalingPolicyName(service.getAutoscalingPolicyName());
//        serviceDefinitionBean.setDeploymentPolicyName(service.getDeploymentPolicyName());
//
//        return serviceDefinitionBean;
//    }

//    public static List<ServiceDefinitionBean> convertToServiceDefinitionBeans(Collection<Service> services) {
//
//        List<ServiceDefinitionBean> serviceDefinitionBeans = new ArrayList<ServiceDefinitionBean>();
//
//        for (Service service : services) {
//            serviceDefinitionBeans.add(convertToServiceDefinitionBean(service));
//        }
//        return serviceDefinitionBeans;
//    }

    public static org.apache.stratos.cloud.controller.stub.domain.kubernetes.KubernetesCluster
        convertToCCKubernetesClusterPojo(KubernetesClusterBean kubernetesClusterBean) {

        org.apache.stratos.cloud.controller.stub.domain.kubernetes.KubernetesCluster kubernetesCluster = new
                org.apache.stratos.cloud.controller.stub.domain.kubernetes.KubernetesCluster();

        kubernetesCluster.setClusterId(kubernetesClusterBean.getClusterId());
        kubernetesCluster.setDescription(kubernetesClusterBean.getDescription());
        kubernetesCluster.setKubernetesMaster(convertStubKubernetesMasterToKubernetesMaster(kubernetesClusterBean.getKubernetesMaster()));
        kubernetesCluster.setPortRange(convertPortRangeToStubPortRange(kubernetesClusterBean.getPortRange()));
        kubernetesCluster.setKubernetesHosts(convertToASKubernetesHostsPojo(kubernetesClusterBean.getKubernetesHosts()));
        kubernetesCluster.setProperties((convertPropertyBeansToCCStubProperties(kubernetesClusterBean.getProperty())));

        return kubernetesCluster;
    }

    private static org.apache.stratos.cloud.controller.stub.domain.kubernetes.KubernetesHost[]
        convertToASKubernetesHostsPojo(List<KubernetesHostBean> kubernetesHosts) {

        if (kubernetesHosts == null || kubernetesHosts.isEmpty()) {
            return null;
        }
        int kubernetesHostCount = kubernetesHosts.size();
        org.apache.stratos.cloud.controller.stub.domain.kubernetes.KubernetesHost[]
                kubernetesHostsArr = new org.apache.stratos.cloud.controller.stub.domain.kubernetes.KubernetesHost[kubernetesHostCount];
        for (int i = 0; i < kubernetesHostCount; i++) {
            KubernetesHostBean kubernetesHostBean = kubernetesHosts.get(i);
            kubernetesHostsArr[i] = convertKubernetesHostToStubKubernetesHost(kubernetesHostBean);
        }
        return kubernetesHostsArr;
    }


    private static org.apache.stratos.cloud.controller.stub.domain.kubernetes.PortRange
        convertPortRangeToStubPortRange(PortRangeBean portRangeBean) {

        if (portRangeBean == null) {
            return null;
        }
        org.apache.stratos.cloud.controller.stub.domain.kubernetes.PortRange
                portRange = new org.apache.stratos.cloud.controller.stub.domain.kubernetes.PortRange();
        portRange.setLower(portRangeBean.getLower());
        portRange.setUpper(portRangeBean.getUpper());
        return portRange;
    }

    public static org.apache.stratos.cloud.controller.stub.domain.kubernetes.KubernetesHost
        convertKubernetesHostToStubKubernetesHost(KubernetesHostBean kubernetesHostBean) {

        if (kubernetesHostBean == null) {
            return null;
        }

        org.apache.stratos.cloud.controller.stub.domain.kubernetes.KubernetesHost
                kubernetesHost = new org.apache.stratos.cloud.controller.stub.domain.kubernetes.KubernetesHost();
        kubernetesHost.setHostId(kubernetesHostBean.getHostId());
        kubernetesHost.setHostIpAddress(kubernetesHostBean.getHostIpAddress());
        kubernetesHost.setHostname(kubernetesHostBean.getHostname());
        kubernetesHost.setProperties(convertPropertyBeansToCCStubProperties(kubernetesHostBean.getProperty()));

        return kubernetesHost;
    }

    public static org.apache.stratos.cloud.controller.stub.domain.kubernetes.KubernetesMaster
        convertStubKubernetesMasterToKubernetesMaster(KubernetesMasterBean kubernetesMasterBean) {

        if (kubernetesMasterBean == null) {
            return null;
        }

        org.apache.stratos.cloud.controller.stub.domain.kubernetes.KubernetesMaster
                kubernetesMaster = new org.apache.stratos.cloud.controller.stub.domain.kubernetes.KubernetesMaster();
        kubernetesMaster.setHostId(kubernetesMasterBean.getHostId());
        kubernetesMaster.setHostIpAddress(kubernetesMasterBean.getHostIpAddress());
        kubernetesMaster.setHostname(kubernetesMasterBean.getHostname());
        kubernetesMaster.setEndpoint(kubernetesMasterBean.getEndpoint());
        kubernetesMaster.setProperties(convertPropertyBeansToCCStubProperties(kubernetesMasterBean.getProperty()));

        return kubernetesMaster;
    }

    public static KubernetesClusterBean[] convertStubKubernetesClustersToKubernetesClusters(org.apache.stratos.cloud.controller.stub.domain.kubernetes.KubernetesCluster[] kubernetesClusters) {

        if (kubernetesClusters == null) {
            return null;
        }
        KubernetesClusterBean[] kubernetesClustersBean = new KubernetesClusterBean[kubernetesClusters.length];
        for (int i = 0; i < kubernetesClusters.length; i++) {
            kubernetesClustersBean[i] = convertStubKubernetesClusterToKubernetesCluster(kubernetesClusters[i]);
        }
        return kubernetesClustersBean;
    }

    public static KubernetesClusterBean convertStubKubernetesClusterToKubernetesCluster(org.apache.stratos.cloud.controller.stub.domain.kubernetes.KubernetesCluster kubernetesCluster) {
        if (kubernetesCluster == null) {
            return null;
        }
        KubernetesClusterBean kubernetesClusterBean = new KubernetesClusterBean();
        kubernetesClusterBean.setClusterId(kubernetesCluster.getClusterId());
        kubernetesClusterBean.setDescription(kubernetesCluster.getDescription());
        kubernetesClusterBean.setPortRange(convertStubPortRangeToPortRange(kubernetesCluster.getPortRange()));
        kubernetesClusterBean.setKubernetesHosts(convertStubKubernetesHostsToKubernetesHosts(kubernetesCluster.getKubernetesHosts()));
        kubernetesClusterBean.setKubernetesMaster(convertStubKubernetesMasterToKubernetesMaster(kubernetesCluster.getKubernetesMaster()));
        kubernetesClusterBean.setProperty(convertCCStubPropertiesToPropertyBeans(kubernetesCluster.getProperties()));
        return kubernetesClusterBean;
    }

    public static KubernetesMasterBean convertStubKubernetesMasterToKubernetesMaster(org.apache.stratos.cloud.controller.stub.domain.kubernetes.KubernetesMaster kubernetesMaster) {
        if (kubernetesMaster == null) {
            return null;
        }
        KubernetesMasterBean kubernetesMasterBean = new KubernetesMasterBean();
        kubernetesMasterBean.setHostId(kubernetesMaster.getHostId());
        kubernetesMasterBean.setHostname(kubernetesMaster.getHostname());
        kubernetesMasterBean.setHostIpAddress(kubernetesMaster.getHostIpAddress());
        kubernetesMasterBean.setProperty(convertCCStubPropertiesToPropertyBeans(kubernetesMaster.getProperties()));
        kubernetesMasterBean.setEndpoint(kubernetesMaster.getEndpoint());
        return kubernetesMasterBean;
    }

    public static List<KubernetesHostBean> convertStubKubernetesHostsToKubernetesHosts(org.apache.stratos.cloud.controller.stub.domain.kubernetes.KubernetesHost[] kubernetesHosts) {
        if (kubernetesHosts == null) {
            return null;
        }
        List<KubernetesHostBean> kubernetesHostList = new ArrayList<KubernetesHostBean>();
        for (int i = 0; i < kubernetesHosts.length; i++) {
            kubernetesHostList.add(convertStubKubernetesHostToKubernetesHost(kubernetesHosts[i]));
        }
        return kubernetesHostList;
    }

    private static KubernetesHostBean convertStubKubernetesHostToKubernetesHost(
            org.apache.stratos.cloud.controller.stub.domain.kubernetes.KubernetesHost kubernetesHost) {
        if (kubernetesHost == null) {
            return null;
        }
        KubernetesHostBean kubernetesHostBean = new KubernetesHostBean();
        kubernetesHostBean.setHostId(kubernetesHost.getHostId());
        kubernetesHostBean.setHostname(kubernetesHost.getHostname());
        kubernetesHostBean.setHostIpAddress(kubernetesHost.getHostIpAddress());
        kubernetesHostBean.setProperty(convertCCStubPropertiesToPropertyBeans(kubernetesHost.getProperties()));
        return kubernetesHostBean;
    }
    
    private static List<org.apache.stratos.common.beans.cartridge.PropertyBean> convertCCStubPropertiesToPropertyBeans(org.apache.stratos.cloud.controller.stub.Properties properties) {
        if (properties == null || properties.getProperties() == null) {
            return null;
        }
        List<org.apache.stratos.common.beans.cartridge.PropertyBean> propertyBeanList = new ArrayList<org.apache.stratos.common.beans.cartridge.PropertyBean>();
        for (int i = 0; i < properties.getProperties().length; i++) {
            propertyBeanList.add(convertStubPropertyToPropertyBean(properties.getProperties()[i]));
        }
        return propertyBeanList;
    }

    private static org.apache.stratos.common.beans.cartridge.PropertyBean convertAsStubPropertyToPropertyBean(org.apache.stratos.autoscaler.stub.Property propertyE) {
        if ((propertyE == null) || (!(propertyE.getValue() instanceof String))) {
            return null;
        }

        org.apache.stratos.common.beans.cartridge.PropertyBean propertyBean = new org.apache.stratos.common.beans.cartridge.PropertyBean();
        propertyBean.setName(propertyE.getName());
        propertyBean.setValue(String.valueOf(propertyE.getValue()));
        return propertyBean;
    }
    
    private static org.apache.stratos.common.beans.cartridge.PropertyBean convertStubPropertyToPropertyBean(org.apache.stratos.cloud.controller.stub.Property propertyE) {
        if (propertyE == null) {
            return null;
        }
        org.apache.stratos.common.beans.cartridge.PropertyBean propertyBean = new org.apache.stratos.common.beans.cartridge.PropertyBean();
        propertyBean.setName(propertyE.getName());
        propertyBean.setValue(propertyE.getValue());
        return propertyBean;
    }

    private static PortRangeBean convertStubPortRangeToPortRange(org.apache.stratos.cloud.controller.stub.domain.kubernetes.PortRange portRange) {
        if (portRange == null) {
            return null;
        }
        PortRangeBean portRangeBean = new PortRangeBean();
        portRangeBean.setUpper(portRange.getUpper());
        portRangeBean.setLower(portRange.getLower());
        return portRangeBean;
    }

    public static ApplicationContext convertApplicationDefinitionToStubApplicationContext(ApplicationBean applicationDefinition) {

        org.apache.stratos.autoscaler.stub.pojo.ApplicationContext applicationContext =
                new org.apache.stratos.autoscaler.stub.pojo.ApplicationContext();
        applicationContext.setApplicationId(applicationDefinition.getApplicationId());
        applicationContext.setAlias(applicationDefinition.getAlias());
        applicationContext.setMultiTenant(applicationDefinition.isMultiTenant());
        applicationContext.setName(applicationDefinition.getName());
        applicationContext.setDescription(applicationDefinition.getDescription());
        applicationContext.setStatus(applicationDefinition.getStatus());

        // convert and set components
        if (applicationDefinition.getComponents() != null) {
            org.apache.stratos.autoscaler.stub.pojo.ComponentContext componentContext =
                    new org.apache.stratos.autoscaler.stub.pojo.ComponentContext();
                      
            // top level Groups
            if (applicationDefinition.getComponents().getGroups() != null) {
                componentContext.setGroupContexts(
                        convertGroupDefinitionsToStubGroupContexts(applicationDefinition.getComponents().getGroups()));
            }
            // top level dependency information
            if (applicationDefinition.getComponents().getDependencies() != null) {
                componentContext.setDependencyContext(
                        convertDependencyDefinitionsToDependencyContexts(applicationDefinition.getComponents().getDependencies()));
            }
            // top level cartridge context information
            if (applicationDefinition.getComponents().getCartridges() != null) {
                componentContext.setCartridgeContexts(
                        convertCartridgeReferenceBeansToStubCartridgeContexts(applicationDefinition.getComponents().getCartridges()));
            }
            applicationContext.setComponents(componentContext);
        }
        return applicationContext;
    }

    public static ApplicationBean convertStubApplicationContextToApplicationDefinition(
            ApplicationContext applicationContext) {
        if(applicationContext == null) {
            return null;
        }

        ApplicationBean applicationDefinition = new ApplicationBean();
        applicationDefinition.setApplicationId(applicationContext.getApplicationId());
        applicationDefinition.setAlias(applicationContext.getAlias());
        applicationDefinition.setMultiTenant(applicationContext.getMultiTenant());
        applicationDefinition.setName(applicationContext.getName());
        applicationDefinition.setDescription(applicationContext.getDescription());
        applicationDefinition.setStatus(applicationContext.getStatus());

        // convert and set components
        if (applicationContext.getComponents() != null) {
            applicationDefinition.setComponents(new ComponentBean());
            // top level Groups
            if (applicationContext.getComponents().getGroupContexts() != null) {
                applicationDefinition.getComponents().setGroups(
                        convertStubGroupContextsToGroupDefinitions(applicationContext.getComponents().getGroupContexts()));
            }
            // top level dependency information
            if (applicationContext.getComponents().getDependencyContext() != null) {
                applicationDefinition.getComponents().setDependencies(
                        convertStubDependencyContextsToDependencyDefinitions(applicationContext.getComponents().getDependencyContext()));
            }
            // top level cartridge context information
            if (applicationContext.getComponents().getCartridgeContexts() != null) {
                applicationDefinition.getComponents().setCartridges(
                        convertStubCartridgeContextsToCartridgeReferenceBeans(applicationContext.getComponents().getCartridgeContexts()));
            }
        }
        return applicationDefinition;
    }

    private static List<GroupReferenceBean> convertStubGroupContextsToGroupDefinitions(GroupContext[] groupContexts) {
        List<GroupReferenceBean> groupDefinitions = new ArrayList<GroupReferenceBean>();
        if(groupContexts != null) {
            for (GroupContext groupContext : groupContexts) {
                if(groupContext != null) {
                    GroupReferenceBean groupDefinition = new GroupReferenceBean();
                    groupDefinition.setAlias(groupContext.getAlias());
                    groupDefinition.setGroupMaxInstances(groupContext.getGroupMaxInstances());
                    groupDefinition.setGroupMinInstances(groupContext.getGroupMinInstances());
                    groupDefinition.setGroupScalingEnabled(groupContext.getGroupScalingEnabled());
                    groupDefinition.setName(groupContext.getName());
                    groupDefinition.setGroups(convertStubGroupContextsToGroupDefinitions(groupContext.getGroupContexts()));
                    groupDefinition.setCartridges(convertStubCartridgeContextsToCartridgeReferenceBeans(
                            groupContext.getCartridgeContexts()));
                    groupDefinitions.add(groupDefinition);
                }
            }
        }
        return groupDefinitions;
    }

    private static DependencyBean convertStubDependencyContextsToDependencyDefinitions(DependencyContext dependencyContext) {
        DependencyBean dependencyDefinitions = new DependencyBean();
        dependencyDefinitions.setTerminationBehaviour(dependencyContext.getTerminationBehaviour());

            if(dependencyContext.getStartupOrdersContexts() != null) {
                List<String> startupOrders = new ArrayList<String>();
                for(String item : dependencyContext.getStartupOrdersContexts()) {
                    startupOrders.add(item);
                }
                dependencyDefinitions.setStartupOrders(startupOrders);
            }
            if (dependencyContext.getScalingDependents() != null) {
                List<String> scalingDependents = new ArrayList<String>();
                for(String item : dependencyContext.getScalingDependents()) {
                    scalingDependents.add(item);
                }
                dependencyDefinitions.setScalingDependants(scalingDependents);
            }
        return dependencyDefinitions;
    }

    private static List<CartridgeReferenceBean> convertStubCartridgeContextsToCartridgeReferenceBeans(CartridgeContext[] cartridgeContexts) {
        List<CartridgeReferenceBean> cartridgeDefinitions = new ArrayList<CartridgeReferenceBean>();
        if(cartridgeContexts != null) {
            for (CartridgeContext cartridgeContext : cartridgeContexts) {
                if(cartridgeContext != null) {
                    CartridgeReferenceBean cartridgeDefinition = new CartridgeReferenceBean();
                    cartridgeDefinition.setType(cartridgeContext.getType());
                    cartridgeDefinition.setCartridgeMin(cartridgeContext.getCartridgeMin());
                    cartridgeDefinition.setCartridgeMax(cartridgeContext.getCartridgeMax());
                    cartridgeDefinition.setSubscribableInfo(convertStubSubscribableInfoContextToSubscribableInfo(cartridgeContext.getSubscribableInfoContext()));
                    cartridgeDefinitions.add(cartridgeDefinition);
                }
            }
        }
        return cartridgeDefinitions;
    }

    private static SubscribableInfo convertStubSubscribableInfoContextToSubscribableInfo(
            SubscribableInfoContext subscribableInfoContext) {
        SubscribableInfo subscribableInfo = new SubscribableInfo();
        subscribableInfo.setAlias(subscribableInfoContext.getAlias());
        subscribableInfo.setAutoscalingPolicy(subscribableInfoContext.getAutoscalingPolicy());
        if(!CommonUtil.isEmptyArray(subscribableInfoContext.getDependencyAliases())) {
            subscribableInfo.setDependencyAliases(subscribableInfoContext.getDependencyAliases());
        }
        subscribableInfo.setDeploymentPolicy(subscribableInfoContext.getDeploymentPolicy());
        subscribableInfo.setMinMembers(subscribableInfoContext.getMinMembers());
        subscribableInfo.setMaxMembers(subscribableInfoContext.getMaxMembers());
        subscribableInfo.setProperty(convertStubPropertiesToPropertyBeanList(subscribableInfoContext.getProperties()));

        if(subscribableInfoContext.getArtifactRepositoryContext() != null) {
            ArtifactRepositoryContext artifactRepositoryContext = subscribableInfoContext.getArtifactRepositoryContext();

            ArtifactRepositoryBean artifactRepository = new ArtifactRepositoryBean();
            artifactRepository.setAlias(artifactRepositoryContext.getAlias());
            artifactRepository.setRepoUrl(artifactRepositoryContext.getRepoUrl());
            artifactRepository.setPrivateRepo(artifactRepositoryContext.getPrivateRepo());
            artifactRepository.setRepoUsername(artifactRepositoryContext.getRepoUsername());
            artifactRepository.setRepoPassword(artifactRepositoryContext.getRepoPassword());

            subscribableInfo.setArtifactRepository(artifactRepository);
        }
        return subscribableInfo;
    }

    private static List<org.apache.stratos.common.beans.PropertyBean>
        convertStubPropertiesToPropertyBeanList(org.apache.stratos.autoscaler.stub.Properties properties) {

        List<org.apache.stratos.common.beans.PropertyBean> propertyBeanList =
                new ArrayList<org.apache.stratos.common.beans.PropertyBean>();
        if((properties != null) && (properties.getProperties() != null)) {
            for (org.apache.stratos.autoscaler.stub.Property property : properties.getProperties()) {
                if((property != null) && (property.getValue() instanceof String)) {
                    org.apache.stratos.common.beans.PropertyBean propertyBean =
                            new org.apache.stratos.common.beans.PropertyBean();
                    propertyBean.setName(property.getName());
                    propertyBean.setValue(String.valueOf(property.getValue()));
                    propertyBeanList.add(propertyBean);
                }
            }
        }
        return propertyBeanList;
    }

    private static CartridgeContext[] convertCartridgeReferenceBeansToStubCartridgeContexts(
            List<CartridgeReferenceBean> cartridges) {

    	CartridgeContext[] cartridgeContextArray = new CartridgeContext[cartridges.size()];
    	int i = 0;
    	for (CartridgeReferenceBean cartridgeDefinition : cartridges) {
    		CartridgeContext context = new CartridgeContext();
    		context.setCartridgeMax(cartridgeDefinition.getCartridgeMax());
    		context.setCartridgeMin(cartridgeDefinition.getCartridgeMin());
    		context.setType(cartridgeDefinition.getType());
    		context.setSubscribableInfoContext(convertSubscribableInfo(cartridgeDefinition.getSubscribableInfo()));  
    		cartridgeContextArray[i++] = context;
        }
    	
	    return cartridgeContextArray;
    }

	private static SubscribableInfoContext convertSubscribableInfo(
            SubscribableInfo subscribableInfo) {

        SubscribableInfoContext infoContext = new SubscribableInfoContext();
		infoContext.setAlias(subscribableInfo.getAlias());
		infoContext.setAutoscalingPolicy(subscribableInfo.getAutoscalingPolicy());
		infoContext.setDependencyAliases(subscribableInfo.getDependencyAliases());
		infoContext.setDeploymentPolicy(subscribableInfo.getDeploymentPolicy());
		infoContext.setMaxMembers(subscribableInfo.getMaxMembers());
		infoContext.setMinMembers(subscribableInfo.getMinMembers());

        if(subscribableInfo.getArtifactRepository() != null) {
            ArtifactRepositoryBean artifactRepository = subscribableInfo.getArtifactRepository();

            ArtifactRepositoryContext artifactRepositoryContext = new ArtifactRepositoryContext();
            artifactRepositoryContext.setAlias(infoContext.getAlias());
            artifactRepositoryContext.setPrivateRepo(artifactRepository.isPrivateRepo());
            artifactRepositoryContext.setRepoUrl(artifactRepository.getRepoUrl());
            artifactRepositoryContext.setRepoUsername(artifactRepository.getRepoUsername());
            artifactRepositoryContext.setRepoPassword(artifactRepository.getRepoPassword());
            infoContext.setArtifactRepositoryContext(artifactRepositoryContext);
        }

        infoContext.setProperties(convertPropertyBeansToStubProperties(subscribableInfo.getProperty()));
	    return infoContext;
    }

	private static org.apache.stratos.autoscaler.stub.Properties convertPropertyBeansToStubProperties(
            List<org.apache.stratos.common.beans.PropertyBean> property) {
		org.apache.stratos.autoscaler.stub.Properties prop = new org.apache.stratos.autoscaler.stub.Properties();
		if (property != null) {
			for (org.apache.stratos.common.beans.PropertyBean propertyBean : property) {
				org.apache.stratos.autoscaler.stub.Property p = new org.apache.stratos.autoscaler.stub.Property();
				p.setName(propertyBean.getName());
				p.setValue(propertyBean.getValue());
				prop.addProperties(p);
			}
		}
	    return prop;
    }

	
    private static DependencyContext convertDependencyDefinitionsToDependencyContexts(DependencyBean dependencyDefinitions) {
        DependencyContext dependencyContext = new DependencyContext();
        dependencyContext.setTerminationBehaviour(dependencyDefinitions.getTerminationBehaviour());

        if (dependencyDefinitions != null){
            if(dependencyDefinitions.getStartupOrders() != null) {
                String[] startupOrders = new String[dependencyDefinitions.getStartupOrders().size()];
                startupOrders = dependencyDefinitions.getStartupOrders().toArray(startupOrders);
                dependencyContext.setStartupOrdersContexts(startupOrders);
            }
            if (dependencyDefinitions.getScalingDependants() != null) {
                String[] scalingDependents = new String[dependencyDefinitions.getScalingDependants().size()];
                scalingDependents = dependencyDefinitions.getScalingDependants().toArray(scalingDependents);
                dependencyContext.setScalingDependents(scalingDependents);
            }
        }
        return dependencyContext;
    }

    private static org.apache.stratos.autoscaler.stub.pojo.GroupContext[]
        convertGroupDefinitionsToStubGroupContexts(List<GroupReferenceBean> groupDefinitions) {

        GroupContext[] groupContexts = new GroupContext[groupDefinitions.size()];
        int i = 0;
        for (GroupReferenceBean groupDefinition : groupDefinitions) {
            GroupContext groupContext = new GroupContext();
            groupContext.setName(groupDefinition.getName());
            groupContext.setAlias(groupDefinition.getAlias());
            groupContext.setGroupMaxInstances(groupDefinition.getGroupMaxInstances());
            groupContext.setGroupMinInstances(groupDefinition.getGroupMinInstances());
            groupContext.setGroupScalingEnabled(groupDefinition.isGroupScalingEnabled());
           
            // Groups
            if (groupDefinition.getGroups() != null) {
                groupContext.setGroupContexts(convertGroupDefinitionsToStubGroupContexts(groupDefinition.getGroups()));
            }
            
            groupContext.setCartridgeContexts(convertCartridgeReferenceBeansToStubCartridgeContexts(groupDefinition.getCartridges()));
            groupContexts[i++] = groupContext;
        }

        return groupContexts;
    }


    public static ApplicationInfoBean convertApplicationToApplicationBean(Application application) {
        if (application == null) {
            return null;
        }

        ApplicationInfoBean applicationBean = new ApplicationInfoBean();
        applicationBean.setId(application.getUniqueIdentifier());
        applicationBean.setName(application.getName());
        applicationBean.setDescription(application.getDescription());
        applicationBean.setTenantDomain(application.getTenantDomain());
        applicationBean.setTenantAdminUsername(application.getTenantAdminUserName());
        //applicationBean.set(convertApplicationToApplicationInstanceBean(application));
        return applicationBean;
    }

    public static ApplicationInfoBean convertApplicationToApplicationInstanceBean(Application application) {
        if (application == null) {
            return null;
        }

        ApplicationInfoBean applicationBean = new
                ApplicationInfoBean();
        applicationBean.setId(application.getUniqueIdentifier());
        applicationBean.setName(application.getName());
        applicationBean.setDescription(application.getDescription());
        applicationBean.setTenantDomain(application.getTenantDomain());
        applicationBean.setTenantAdminUsername(application.getTenantAdminUserName());
        applicationBean.setApplicationInstances(convertApplicationInstancesToApplicationInstances(application));
        return applicationBean;
    }

    private static List<ApplicationInstanceBean> convertApplicationInstancesToApplicationInstances(
            Application application) {
        List<ApplicationInstanceBean> applicationInstanceList = new ArrayList<ApplicationInstanceBean>();
        Collection<ApplicationInstance> applicationInstancesInTopology =
                application.getInstanceIdToInstanceContextMap().values();

        if (applicationInstancesInTopology != null) {
            for (ApplicationInstance applicationInstance : applicationInstancesInTopology) {
                ApplicationInstanceBean instance = new ApplicationInstanceBean();
                instance.setInstanceId(applicationInstance.getInstanceId());
                instance.setApplicationId(application.getUniqueIdentifier());
                instance.setParentInstanceId(applicationInstance.getParentId());
                instance.setStatus(applicationInstance.getStatus().toString());
                applicationInstanceList.add(instance);
            }
        }
        return applicationInstanceList;
    }

    public static List<GroupInstanceBean> convertGroupToGroupInstancesBean(String instanceId, Group group) {
        if (group == null) {
            return null;
        }

        List<GroupInstanceBean> groupInstanceBeans = new ArrayList<GroupInstanceBean>();
        if (group.getInstanceContexts(instanceId) != null) {
            GroupInstance groupInstance = group.getInstanceContexts(instanceId);
            GroupInstanceBean groupInstanceBean = new GroupInstanceBean();
            groupInstanceBean.setParentInstanceId(instanceId);
            groupInstanceBean.setInstanceId(groupInstance.getInstanceId());
            groupInstanceBean.setStatus(groupInstance.getStatus().toString());
            groupInstanceBean.setGroupId(group.getUniqueIdentifier());
            /*for(Group group1 : group.getGroups()) {
                groupInstanceBean.setGroupInstances(convertGroupToGroupInstancesBean(
                        groupInstance.getInstanceId(), group1));
            }*/
            groupInstanceBeans.add(groupInstanceBean);

        } else {
            List<org.apache.stratos.messaging.domain.instance.Instance> groupInstances =
                    group.getInstanceContextsWithParentId(instanceId);
            for (org.apache.stratos.messaging.domain.instance.Instance groupInstance : groupInstances) {
                GroupInstanceBean groupInstanceBean = new GroupInstanceBean();
                groupInstanceBean.setParentInstanceId(instanceId);
                groupInstanceBean.setInstanceId(groupInstance.getInstanceId());
                groupInstanceBean.setStatus(((GroupInstance) groupInstance).getStatus().toString());
                groupInstanceBean.setGroupId(group.getUniqueIdentifier());
                /*for(Group group1 : group.getGroups()) {
                    groupInstanceBean.setGroupInstances(convertGroupToGroupInstancesBean(
                            groupInstance.getInstanceId(), group1));
                }*/
                groupInstanceBeans.add(groupInstanceBean);
            }
        }

        return groupInstanceBeans;
    }

    private static List<InstanceBean> convertGroupInstancesToInstances(Group group) {
	    List<InstanceBean> instanceList = new ArrayList<InstanceBean>();
	    Collection<GroupInstance> instancesInTopology = group.getInstanceIdToInstanceContextMap().values();
	    if(instancesInTopology != null) {
	    	for (GroupInstance groupInstance : instancesInTopology) {
	            InstanceBean instance = new InstanceBean();
	            instance.setStatus(groupInstance.getStatus().toString());
	            instance.setInstanceId(groupInstance.getInstanceId());
	            instanceList.add(instance);
            }
	    }
	    return instanceList;
    }

    public static org.apache.stratos.common.beans.TenantInfoBean convertCarbonTenantInfoBeanToTenantInfoBean(
            TenantInfoBean carbonTenantInfoBean) {

        if(carbonTenantInfoBean == null) {
            return null;
        }

        org.apache.stratos.common.beans.TenantInfoBean tenantInfoBean =
                new org.apache.stratos.common.beans.TenantInfoBean();
        tenantInfoBean.setTenantId(carbonTenantInfoBean.getTenantId());
        tenantInfoBean.setTenantDomain(carbonTenantInfoBean.getTenantDomain());
        tenantInfoBean.setActive(carbonTenantInfoBean.isActive());
        tenantInfoBean.setAdmin(carbonTenantInfoBean.getAdmin());
        tenantInfoBean.setEmail(carbonTenantInfoBean.getEmail());
        tenantInfoBean.setAdminPassword(carbonTenantInfoBean.getAdminPassword());
        tenantInfoBean.setFirstname(carbonTenantInfoBean.getFirstname());
        tenantInfoBean.setLastname(carbonTenantInfoBean.getLastname());
        tenantInfoBean.setCreatedDate(carbonTenantInfoBean.getCreatedDate().getTimeInMillis());
        return tenantInfoBean;
    }

    public static TenantInfoBean convertTenantInfoBeanToCarbonTenantInfoBean(
            org.apache.stratos.common.beans.TenantInfoBean tenantInfoBean) {

        if(tenantInfoBean == null) {
            return null;
        }

        TenantInfoBean carbonTenantInfoBean = new TenantInfoBean();
        carbonTenantInfoBean.setTenantId(tenantInfoBean.getTenantId());
        carbonTenantInfoBean.setTenantDomain(tenantInfoBean.getTenantDomain());
        carbonTenantInfoBean.setActive(tenantInfoBean.isActive());
        carbonTenantInfoBean.setAdmin(tenantInfoBean.getAdmin());
        carbonTenantInfoBean.setEmail(tenantInfoBean.getEmail());
        carbonTenantInfoBean.setAdminPassword(tenantInfoBean.getAdminPassword());
        carbonTenantInfoBean.setFirstname(tenantInfoBean.getFirstname());
        carbonTenantInfoBean.setLastname(tenantInfoBean.getLastname());
        if(tenantInfoBean.getCreatedDate() > 0) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(tenantInfoBean.getCreatedDate());
            carbonTenantInfoBean.setCreatedDate(calendar);
        }
        return carbonTenantInfoBean;
    }

    public static ServiceGroup convertServiceGroupDefinitionToASStubServiceGroup(GroupBean serviceGroupDefinition) throws ServiceGroupDefinitionException {
        ServiceGroup servicegroup = new ServiceGroup();

        // implement conversion (mostly List -> Array)
        servicegroup.setGroupscalingEnabled(serviceGroupDefinition.isGroupScalingEnabled());
        List<GroupBean> groupsDef = serviceGroupDefinition.getGroups();
        List<String> cartridgesDef = serviceGroupDefinition.getCartridges();

        servicegroup.setName(serviceGroupDefinition.getName());

        if (groupsDef == null) {
            groupsDef = new ArrayList<GroupBean>(0);
        }

        if (cartridgesDef == null) {
            cartridgesDef = new ArrayList<String>(0);
        }

        ServiceGroup[] subGroups = new ServiceGroup[groupsDef.size()];
        String[] cartridges = new String[cartridgesDef.size()];

        int i = 0;
        for (GroupBean groupDefinition : groupsDef) {
            subGroups[i] = convertServiceGroupDefinitionToASStubServiceGroup(groupDefinition);
            ++i;
        }

        servicegroup.setGroups(subGroups);
        cartridges = cartridgesDef.toArray(cartridges);
        servicegroup.setCartridges(cartridges);

        DependencyBean depDefs = serviceGroupDefinition.getDependencies();

        if (depDefs != null) {
            Dependencies dependencies = new Dependencies();
            List<String> startupOrdersDef = depDefs.getStartupOrders();
            if (startupOrdersDef != null) {
                String[] startupOrders = new String[startupOrdersDef.size()];
                startupOrders = startupOrdersDef.toArray(startupOrders);
                dependencies.setStartupOrders(startupOrders);
            }
            // validate termination behavior
            validateTerminationBehavior(depDefs.getTerminationBehaviour());
            dependencies.setTerminationBehaviour(depDefs.getTerminationBehaviour());
            if (depDefs.getScalingDependants() != null) {
                dependencies.setScalingDependants(depDefs.getScalingDependants()
                        .toArray(new String[depDefs.getScalingDependants().size()]));
            }
            servicegroup.setDependencies(dependencies);
        }

        return servicegroup;
    }

    public static GroupBean convertStubServiceGroupToServiceGroupDefinition(ServiceGroup serviceGroup) {
        if(serviceGroup == null) {
            return null;
        }

        GroupBean servicegroupDef = new GroupBean();
        servicegroupDef.setName(serviceGroup.getName());
        servicegroupDef.setGroupScalingEnabled(serviceGroup.getGroupscalingEnabled());
        String[] cartridges = serviceGroup.getCartridges();
        ServiceGroup[] groups = serviceGroup.getGroups();
        org.apache.stratos.autoscaler.stub.pojo.Dependencies deps = serviceGroup.getDependencies();

        List<GroupBean> groupDefinitions = new ArrayList<GroupBean>(groups.length);
        for (ServiceGroup group : groups) {
            if (group != null) {
                groupDefinitions.add(convertStubServiceGroupToServiceGroupDefinition(group));
            }
        }

        if (deps != null) {
            DependencyBean depsDef = new DependencyBean();
            String[] startupOrders = deps.getStartupOrders();
            if (startupOrders != null && startupOrders[0] != null) {
                List<String> startupOrdersDef = Arrays.asList(startupOrders);
                depsDef.setStartupOrders(startupOrdersDef);
            }

            String [] scalingDependants = deps.getScalingDependants();
            if (scalingDependants != null && scalingDependants[0] != null) {
                List<String> scalingDependenciesDef = Arrays.asList(scalingDependants);
                depsDef.setScalingDependants(scalingDependenciesDef);
            }

            depsDef.setTerminationBehaviour(deps.getTerminationBehaviour());
            servicegroupDef.setDependencies(depsDef);
        }

        List<String> cartridgesDef = new ArrayList<String>(Arrays.asList(cartridges));
        //List<ServiceGroupDefinition> subGroupsDef = new ArrayList<ServiceGroupDefinition>(groups.length);
        if (cartridges[0] != null) {
            servicegroupDef.setCartridges(cartridgesDef);
        }
        if (groups != null) {
            servicegroupDef.setGroups(groupDefinitions);
        }

        return servicegroupDef;
    }

    /**
     * Validates terminationBehavior. The terminationBehavior should be one of the following:
     * 1. terminate-none
     * 2. terminate-dependents
     * 3. terminate-all
     *
     * @throws ServiceGroupDefinitionException if terminationBehavior is different to what is
     *                                        listed above
     */
    private static void validateTerminationBehavior(String terminationBehavior) throws ServiceGroupDefinitionException {

        if (!(terminationBehavior == null || "terminate-none".equals(terminationBehavior) ||
                "terminate-dependents".equals(terminationBehavior) || "terminate-all".equals(terminationBehavior))) {
            throw new ServiceGroupDefinitionException("Invalid Termination Behaviour specified: [ " +
                    terminationBehavior + " ], should be one of 'terminate-none', 'terminate-dependents', " +
                    " 'terminate-all' ");
        }
    }

    public static ApplicationSignUp convertApplicationSignUpBeanToStubApplicationSignUp(ApplicationSignUpBean applicationSignUpBean) {
        ApplicationSignUp applicationSignUp = new ApplicationSignUp();

        if(applicationSignUpBean.getArtifactRepositories() != null) {
            List<ArtifactRepository> artifactRepositoryList = new ArrayList<ArtifactRepository>();
            for(ArtifactRepositoryBean artifactRepositoryBean : applicationSignUpBean.getArtifactRepositories()) {
                ArtifactRepository artifactRepository = new ArtifactRepository();

                artifactRepository.setAlias(artifactRepositoryBean.getAlias());
                artifactRepository.setPrivateRepo(artifactRepositoryBean.isPrivateRepo());
                artifactRepository.setRepoUrl(artifactRepositoryBean.getRepoUrl());
                artifactRepository.setRepoUsername(artifactRepositoryBean.getRepoUsername());
                artifactRepository.setRepoPassword(artifactRepositoryBean.getRepoPassword());

                artifactRepositoryList.add(artifactRepository);
            }
            ArtifactRepository[] artifactRepositoryArray = artifactRepositoryList.toArray(new ArtifactRepository[
                    artifactRepositoryList.size()]);
            applicationSignUp.setArtifactRepositories(artifactRepositoryArray);
        }
        return applicationSignUp;
    }

    public static ApplicationSignUpBean convertStubApplicationSignUpToApplicationSignUpBean(ApplicationSignUp applicationSignUp) {
        ApplicationSignUpBean applicationSignUpBean = new ApplicationSignUpBean();

        if(applicationSignUp.getArtifactRepositories() != null) {
            List<ArtifactRepositoryBean> artifactRepositoryBeanList = new ArrayList<ArtifactRepositoryBean>();
            for(ArtifactRepository artifactRepository : applicationSignUp.getArtifactRepositories()) {
                if(artifactRepository != null) {
                    ArtifactRepositoryBean artifactRepositoryBean = new ArtifactRepositoryBean();

                    artifactRepositoryBean.setAlias(artifactRepository.getAlias());
                    artifactRepositoryBean.setPrivateRepo(artifactRepository.getPrivateRepo());
                    artifactRepositoryBean.setRepoUrl(artifactRepository.getRepoUrl());
                    artifactRepositoryBean.setRepoUsername(artifactRepository.getRepoUsername());
                    artifactRepositoryBean.setRepoPassword(artifactRepository.getRepoPassword());

                    artifactRepositoryBeanList.add(artifactRepositoryBean);
                }
            }
            applicationSignUpBean.setArtifactRepositories(artifactRepositoryBeanList);
        }
        return applicationSignUpBean;
    }

    public static DomainMapping convertDomainMappingBeanToStubDomainMapping(DomainMappingBean domainMappingBean) {
        DomainMapping domainMapping = new DomainMapping();
        domainMapping.setDomainName(domainMappingBean.getDomainName());
        domainMapping.setContextPath(domainMappingBean.getContextPath());
        return domainMapping;
    }

    public static DomainMappingBean convertStubDomainMappingToDomainMappingBean(DomainMapping domainMapping) {
        DomainMappingBean domainMappingBean = new DomainMappingBean();
        domainMappingBean.setDomainName(domainMapping.getDomainName());
        domainMappingBean.setContextPath(domainMapping.getContextPath());
        return domainMappingBean;
    }
}
