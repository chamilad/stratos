# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

import mdsclient
from plugins.contracts import ICartridgeAgentPlugin
from xml.dom.minidom import parse
import socket
from modules.util.log import LogFactory


class WSO2ISMetaDataHandler(ICartridgeAgentPlugin):

    def run_plugin(self, values):
        log = LogFactory().get_log(__name__)
        # read tomcat app related values from metadata
        mds_response = mdsclient.get()
        issuer = mds_response.properties["SSO_ISSUER"]
        acs = mds_response.properties["CALLBACK_URL"]

        # add a service provider in the security/sso-idp-config.xml file
        is_root = values["APPLICATION_PATH"]
        sso_idp_file = "%s/repository/conf/security/sso-idp-config.xml" % is_root

        # <SSOIdentityProviderConfig>
        #     <ServiceProviders>
        #         <ServiceProvider>
        #         <Issuer>wso2.my.dashboard</Issuer>
        #         <AssertionConsumerService>https://is.wso2.com/dashboard/acs</AssertionConsumerService>
        #         <SignAssertion>true</SignAssertion>
        #         <SignResponse>true</SignResponse>
        #         <EnableAttributeProfile>false</EnableAttributeProfile>
        #         <IncludeAttributeByDefault>false</IncludeAttributeByDefault>
        #         <Claims>
        #             <Claim>http://wso2.org/claims/role</Claim>
        #         </Claims>
        #         <EnableSingleLogout>false</EnableSingleLogout>
        #         <SingleLogoutUrl></SingleLogoutUrl>
        #         <EnableAudienceRestriction>true</EnableAudienceRestriction>
        #         <AudiencesList>
        #             <Audience>carbonServer</Audience>
        #         </AudiencesList>
        #         <ConsumingServiceIndex></ConsumingServiceIndex>
        #     </ServiceProvider>
        with open(sso_idp_file, "r") as f:
            sp_dom = parse(f)

        root_element = sp_dom.documentElement
        sps_element = sp_dom.getElementsByTagName("ServiceProviders")[0]

        sp_entry = sp_dom.createElement("ServiceProvider")

        sp_entry_issuer = sp_dom.createElement("Issuer")
        sp_entry_issuer.appendChild(sp_dom.createTextNode(issuer))

        sp_entry_acs = sp_dom.createElement("AssertionConsumerService")
        sp_entry_acs.appendChild(sp_dom.createTextNode(acs))

        sp_entry_sign_resp = sp_dom.createElement("SignResponse")
        sp_entry_sign_resp.appendChild(sp_dom.createTextNode("true"))

        sp_entry_sign_assert = sp_dom.createElement("SignAssertion")
        sp_entry_sign_assert.appendChild(sp_dom.createTextNode("true"))

        sp_entry_single_logout = sp_dom.createElement("EnableSingleLogout")
        sp_entry_single_logout.appendChild(sp_dom.createTextNode("true"))

        sp_entry_attribute_profile = sp_dom.createElement("EnableAttributeProfile")
        sp_entry_attribute_profile.appendChild(sp_dom.createTextNode("true"))

        sp_entry.appendChild(sp_entry_issuer)
        sp_entry.appendChild(sp_entry_acs)
        sp_entry.appendChild(sp_entry_sign_resp)
        sp_entry.appendChild(sp_entry_sign_assert)
        sp_entry.appendChild(sp_entry_single_logout)
        sp_entry.appendChild(sp_entry_attribute_profile)

        sps_element.appendChild(sp_entry)

        with open(sso_idp_file, 'w+') as f:
            root_element.writexml(f, newl="\n")
        # root_element.writexml(f)

        # data = json.loads(urllib.urlopen("http://ip.jsontest.com/").read())
        # ip_entry = data["ip"]

        # publish SAML_ENDPOINT to metadata service
        member_hostname = socket.gethostname()
        saml_endpoint = "https://%s:9443/samlsso" % member_hostname
        publish_data = mdsclient.MDSPutRequest()
        hostname_entry = {"key": "SAML_ENDPOINT", "values": saml_endpoint}
        properties_data = [hostname_entry]
        publish_data.properties = properties_data

        mdsclient.put(publish_data)

        # start servers
        log.info("Starting WSO2 IS server")
        wso2is_start_command = "exec ${CARBON_HOME}/bin/wso2server.sh start"
        p = subprocess.Popen(wso2is_start_command)
        output, errors = p.communicate()
        log.debug("WSO2 IS server started")
