/*
 * Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.stratos.load.balancer.context.map;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Host/domain name context path map.
 */
public class HostNameAppContextMap {
    private ConcurrentHashMap<String, String> concurrentHashMap;

    public HostNameAppContextMap() {
        concurrentHashMap = new ConcurrentHashMap<String, String>();
    }

    public void addContextPath(String hostName, String appContext) {
        concurrentHashMap.put(hostName, appContext);
    }

    public String getAppContext(String hostName) {
        return concurrentHashMap.get(hostName);
    }

    public void removeContextPath(String hostName) {
        if(contains(hostName)) {
            concurrentHashMap.remove(hostName);
        }
    }

    public boolean contains(String hostName) {
        return concurrentHashMap.containsKey(hostName);
    }
}
