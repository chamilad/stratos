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

package org.apache.stratos.common.beans.application;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;

@XmlRootElement(name = "dependencies")
public class DependencyBean implements Serializable {

	private static final long serialVersionUID = 1L;

    private List<String> startupOrders;
    private List<String> scalingDependants;
    private String terminationBehaviour;

    public String getTerminationBehaviour() {
        return terminationBehaviour;
    }

    public void setTerminationBehaviour(String terminationBehaviour) {
        this.terminationBehaviour = terminationBehaviour;
    }

	public List<String> getStartupOrders() {
		return startupOrders;
	}

	public void setStartupOrders(List<String> startupOrders) {
		this.startupOrders = startupOrders;
	}

	public List<String> getScalingDependants() {
		return scalingDependants;
	}

	public void setScalingDependants(List<String> scalingDependants) {
		this.scalingDependants = scalingDependants;
	}
	
}
