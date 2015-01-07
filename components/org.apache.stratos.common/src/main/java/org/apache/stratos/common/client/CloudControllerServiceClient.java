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

package org.apache.stratos.common.client;

import org.apache.axis2.AxisFault;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.stratos.cloud.controller.stub.*;
import org.apache.stratos.cloud.controller.stub.domain.*;
import org.apache.stratos.cloud.controller.stub.domain.kubernetes.KubernetesCluster;
import org.apache.stratos.cloud.controller.stub.domain.kubernetes.KubernetesHost;
import org.apache.stratos.cloud.controller.stub.domain.kubernetes.KubernetesMaster;
import org.apache.stratos.common.constants.StratosConstants;

import java.rmi.RemoteException;

public class CloudControllerServiceClient {

	private CloudControllerServiceStub stub;

	private static final Log log = LogFactory.getLog(CloudControllerServiceClient.class);
    private static volatile CloudControllerServiceClient instance;

	private CloudControllerServiceClient(String epr) throws AxisFault {

		String ccSocketTimeout = System.getProperty(StratosConstants.CLOUD_CONTROLLER_CLIENT_SOCKET_TIMEOUT) == null ?
                StratosConstants.DEFAULT_CLIENT_SOCKET_TIMEOUT :
                System.getProperty(StratosConstants.CLOUD_CONTROLLER_CLIENT_SOCKET_TIMEOUT);

        String ccConnectionTimeout = System.getProperty(StratosConstants.CLOUD_CONTROLLER_CLIENT_CONNECTION_TIMEOUT) == null ?
                StratosConstants.DEFAULT_CLIENT_CONNECTION_TIMEOUT :
                System.getProperty(StratosConstants.CLOUD_CONTROLLER_CLIENT_CONNECTION_TIMEOUT);
		
		try {
			stub = new CloudControllerServiceStub(epr);
			stub._getServiceClient().getOptions().setProperty(HTTPConstants.SO_TIMEOUT, new Integer(ccSocketTimeout));
			stub._getServiceClient().getOptions().setProperty(HTTPConstants.CONNECTION_TIMEOUT, new Integer(ccConnectionTimeout));

		} catch (AxisFault axisFault) {
			String msg = "Could not initialize cloud controller service client";
			log.error(msg, axisFault);
			throw new AxisFault(msg, axisFault);
		}
	}

    public static CloudControllerServiceClient getInstance() throws AxisFault {
        if (instance == null) {
            synchronized (CloudControllerServiceClient.class) {
                if (instance == null) {
                    String cloudControllerServiceUrl = System.getProperty(StratosConstants.CLOUD_CONTROLLER_SERVICE_URL);
                    if(StringUtils.isBlank(cloudControllerServiceUrl)) {
                        throw new RuntimeException(String.format("System property not found: %s",
                                StratosConstants.CLOUD_CONTROLLER_SERVICE_URL));
                    }
                    instance = new CloudControllerServiceClient(cloudControllerServiceUrl);
                }
            }
        }
        return instance;
    }

    public void addCartridge(CartridgeConfig cartridgeConfig)
    		throws RemoteException, CloudControllerServiceInvalidCartridgeDefinitionExceptionException,
            CloudControllerServiceInvalidIaasProviderExceptionException {
		stub.addCartridge(cartridgeConfig);
	}

    public void removeCartridge(String cartridgeType) throws RemoteException, CloudControllerServiceInvalidCartridgeTypeExceptionException {
		stub.removeCartridge(cartridgeType);
	}

    public String [] getServiceGroupSubGroups(String name) throws RemoteException, CloudControllerServiceInvalidServiceGroupExceptionException {
    	return stub.getServiceGroupSubGroups(name);
    }
    
    public String [] getServiceGroupCartridges(String name) throws RemoteException, CloudControllerServiceInvalidServiceGroupExceptionException {
    	return stub.getServiceGroupCartridges(name);
    }
    
    public Dependencies getServiceGroupDependencies (String name)throws RemoteException, CloudControllerServiceInvalidServiceGroupExceptionException {
    	return stub.getServiceGroupDependencies(name);
    }
     
    public ServiceGroup getServiceGroup(String name) throws RemoteException, CloudControllerServiceInvalidServiceGroupExceptionException {
    	return stub.getServiceGroup(name);
    }

    public void terminateAllInstances(String clusterId) throws RemoteException, 
    CloudControllerServiceInvalidClusterExceptionException {
		stub.terminateInstances(clusterId);
	}

	public String[] getRegisteredCartridges() throws RemoteException {
		return stub.getCartridges();
	}

	public CartridgeInfo getCartridgeInfo(String cartridgeType) throws RemoteException, 
	CloudControllerServiceCartridgeNotFoundExceptionException {
		return stub.getCartridgeInfo(cartridgeType);
	}

    public ClusterContext getClusterContext (String clusterId) throws RemoteException {

        return stub.getClusterContext(clusterId);
    }
    
    public boolean deployKubernetesCluster(KubernetesCluster kubernetesCluster) throws RemoteException,
            CloudControllerServiceInvalidKubernetesClusterExceptionException {
        return stub.addKubernetesCluster(kubernetesCluster);
    }

    public boolean deployKubernetesHost(String kubernetesClusterId, KubernetesHost kubernetesHost)
            throws RemoteException, CloudControllerServiceInvalidKubernetesHostExceptionException,
            CloudControllerServiceNonExistingKubernetesClusterExceptionException {

        return stub.addKubernetesHost(kubernetesClusterId, kubernetesHost);
    }

    public boolean updateKubernetesMaster(KubernetesMaster kubernetesMaster) throws RemoteException,
            CloudControllerServiceInvalidKubernetesMasterExceptionException,
            CloudControllerServiceNonExistingKubernetesMasterExceptionException {
        return stub.updateKubernetesMaster(kubernetesMaster);
    }

    public KubernetesCluster[] getAvailableKubernetesClusters() throws RemoteException {
        return stub.getKubernetesClusters();
    }

    public KubernetesCluster getKubernetesCluster(String kubernetesClusterId) throws RemoteException,
            CloudControllerServiceNonExistingKubernetesClusterExceptionException {
        return stub.getKubernetesCluster(kubernetesClusterId);
    }

    public boolean undeployKubernetesCluster(String kubernetesClusterId) throws RemoteException,
            CloudControllerServiceNonExistingKubernetesClusterExceptionException {
        return stub.removeKubernetesCluster(kubernetesClusterId);
    }

    public boolean undeployKubernetesHost(String kubernetesHostId) throws RemoteException,
            CloudControllerServiceNonExistingKubernetesHostExceptionException {
        return stub.removeKubernetesHost(kubernetesHostId);
    }

    public KubernetesHost[] getKubernetesHosts(String kubernetesClusterId) throws RemoteException,
            CloudControllerServiceNonExistingKubernetesClusterExceptionException {
        return stub.getHostsForKubernetesCluster(kubernetesClusterId);
    }

    public KubernetesMaster getKubernetesMaster(String kubernetesClusterId) throws RemoteException,
            CloudControllerServiceNonExistingKubernetesClusterExceptionException {
        return stub.getMasterForKubernetesCluster(kubernetesClusterId);
    }

    public boolean updateKubernetesHost(KubernetesHost kubernetesHost) throws RemoteException,
            CloudControllerServiceInvalidKubernetesHostExceptionException,
            CloudControllerServiceNonExistingKubernetesHostExceptionException {
        return stub.updateKubernetesHost(kubernetesHost);
    }
}
