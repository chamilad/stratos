/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.stratos.messaging.domain.domain.mapping;

import java.io.Serializable;

/**
 * Domain mapping definition.
 */
public class DomainMapping implements Serializable {

    private static final long serialVersionUID = -3718485901172753504L;

    private final int tenantId;
    private final String applicationId;
    private final String serviceName;
    private final String clusterId;
    private final String domainName;
    private final String contextPath;

    public DomainMapping(int tenantId, String applicationId, String serviceName, String clusterId,
                         String domainName, String contextPath) {
        this.tenantId = tenantId;
        this.applicationId = applicationId;
        this.serviceName = serviceName;
        this.clusterId = clusterId;
        this.domainName = domainName;
        this.contextPath = contextPath;
    }

    public int getTenantId() {
        return tenantId;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getClusterId() {
        return clusterId;
    }

    public String getDomainName() {
        return domainName;
    }

    public String getContextPath() {
        return contextPath;
    }
}
