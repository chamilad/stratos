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
import time
import zipfile
import subprocess


class TomcatMetadataHandler(ICartridgeAgentPlugin):

    def run_plugin(self, values):
        # publish callback and issuer id from tomcat for IS to pickup
        publish_data = mdsclient.MDSPutRequest()
        # hostname_entry = {"key": "TOMCAT_HOSTNAME", "values": member_hostname}
        cluster_hostname = values["HOST_NAME"]
        callback_url = "https://%s/samlreceiver.jsp" % cluster_hostname
        saml_callback_entry = {"key": "CALLBACK_URL", "values": callback_url}
        issuer_entry = {"key": "SSO_ISSUER", "values": "travelocity.com"}
        # properties_data = [hostname_entry, saml_callback_entry]
        properties_data = [saml_callback_entry, issuer_entry]
        publish_data.properties = properties_data

        mdsclient.put(publish_data)

        # wait till SAML_ENDPOINT becomes available
        mds_response = None
        while mds_response is None:
            time.sleep(5)
            mds_response = mdsclient.get()

        saml_endpoint = mds_response.properties["SAML_ENDPOINT"]

        # edit properties file of the app.
        war_archive = zipfile.ZipFile("%s/travelocity.com.war" % values["APPLICATION_PATH"])
        app_folder = "%s/travelocity.com" % values["APPLICATION_PATH"]
        war_archive.extractall(app_folder)
        properties_file = "%s/WEB-INF/classes/travelocity.properties" % app_folder
        avis_properties_file = "%s/WEB-INF/classes/avis.properties" % app_folder

        replace_command = "sed -i \"s/SAML_ENDPOINT/%s/g\" %s" % (saml_endpoint, properties_file)
        avis_replace_command = "sed -i \"s/SAML_ENDPOINT/%s/g\" %s" % (saml_endpoint, avis_properties_file)
        p = subprocess.Popen(replace_command)
        output, errors = p.communicate()

        p = subprocess.Popen(avis_replace_command)
        output, errors = p.communicate()









