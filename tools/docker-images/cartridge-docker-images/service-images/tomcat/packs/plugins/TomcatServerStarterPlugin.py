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
from modules.util.log import LogFactory


class TomcatSamlSsoMetadataReader(ICartridgeAgentPlugin):

    def run_plugin(self, values):
        log = LogFactory().get_log(__name__)
        # wait till SAML_ENDPOINT becomes available
        mds_response = None
        while mds_response is None:
            log.debug("Waiting for metadata service to be available for app ID: %s" % values["APPLICATION_ID"])
            time.sleep(5)
            mds_response = mdsclient.get()

        saml_endpoint = mds_response.properties["SAML_ENDPOINT"]
        log.debug("SAML_ENDPOINT value read from Metadata service: %s" % saml_endpoint)

        # edit properties file of the app.
        # TODO: travelocity app specific
        log.debug("Changing Travelocity app properties")
        war_archive = zipfile.ZipFile("%s/travelocity.com.war" % values["APPLICATION_PATH"])
        app_folder = "%s/travelocity.com" % values["APPLICATION_PATH"]
        war_archive.extractall(app_folder)
        properties_file = "%s/WEB-INF/classes/travelocity.properties" % app_folder
        avis_properties_file = "%s/WEB-INF/classes/avis.properties" % app_folder

        replace_command = "sed -i \"s/SAML_ENDPOINT/%s/g\" %s" % (saml_endpoint, properties_file)
        avis_replace_command = "sed -i \"s/SAML_ENDPOINT/%s/g\" %s" % (saml_endpoint, avis_properties_file)

        p = subprocess.Popen(replace_command)
        output, errors = p.communicate()
        log.debug("Changed travelocity.properties")

        p = subprocess.Popen(avis_replace_command)
        output, errors = p.communicate()
        log.debug("Changed avis.properties")

        # start tomcat
        log.info("Starting Tomcat server")
        tomcat_start_command = "exec ${CATALINA_HOME}/bin/catalina.sh run"
        p = subprocess.Popen(tomcat_start_command)
        output, errors = p.communicate()
        log.debug("Tomcat server started")








