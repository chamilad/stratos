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

import urllib2
from urllib2 import URLError, HTTPError
import json
from modules.util.log import LogFactory
from config import CartridgeAgentConfiguration
import constants


log = LogFactory().get_log(__name__)
config = CartridgeAgentConfiguration()
mds_url = config.read_property(constants.METADATA_SERVICE_URL)


def put(put_req):
    """
    :param list put_req:
    :return:
    :raises ParameterNotFoundException
    """
    # serialize put request object to json
    request_data = json.dumps(put_req, default=lambda o: o.__dict__)
    alias = config.read_property(constants.CARTRIDGE_ALIAS)
    app_id = config.read_property(constants.APPLICATION_ID)
    resource_url = mds_url + "/metadata/api/application/" + app_id + "/cluster/" + alias + "/property"
    put_request = urllib2.Request(resource_url)
    token = config.read_property(constants.TOKEN)

    put_request.add_header("Authorization", "Bearer %s" % token)
    put_request.add_header('Content-Type', 'application/json')

    try:
        log.debug("Publishing metadata to Metadata service. [URL] %s, [DATA] %s" % (resource_url, request_data))
        put_response = urllib2.urlopen(put_request, request_data)
        log.debug("Metadata service response: %s" % put_response.getcode())

        return put_response
    except HTTPError as e:
        log.exception("Error while publishing to Metadata service. The server couldn\'t fulfill the request.: %s" % e)
    except URLError as e:
        log.exception("Error while publishing to Metadata service. Couldn't reach server URL. : %s" % e)


def get():
    raise NotImplementedError


def update(app_id, alias, data):
    raise NotImplementedError


def delete(app_id, alias, keys):
    raise NotImplementedError


class MDSPutRequest:
    properties = []
    """ :type list[MDSEntry] """



# def get_launch_param_file_location():
#     # launch_params_file = os.path.abspath(os.path.dirname(__file__)).split("extensions")[0] + "payload/launch-params"
#     launch_params_file = "payload/launch-params"
#     logging.debug("Launch param file location " + launch_params_file)
#     return launch_params_file


# param_file = get_launch_param_file_location()
# logging.debug("Payload file location " + param_file)
#
# try:
#     metadata_file = open(param_file, "r")
# except IOError:
#     logging.error("Cannot open payload param file at ")
#     raise RuntimeError
#
# metadata_payload_content = metadata_file.read()
#
# logging.debug("Payload : " + metadata_payload_content)
# properties = {}
# for param in metadata_payload_content.split(","):
#     if param.strip() != "":
#         param_value = param.strip().split("=")
#         properties[param_value[0]] = param_value[1]
#
# logging.debug("Payload properties : ")
# logging.debug(properties)

#
# def get_paylod_property(name):
#     logging.debug("[property  " + name + " = " + properties[name] + "]")
#     return properties[name]

#
# def get_metadataserviceurl():
#     # return properties['METADATASERVICE_URL']
#     return "https://localhost:9443"


# def do_post(url, data):
#     req = urllib2.Request(url)
#     token = properties['TOKEN']
#     if not token:
#         logging.error("TOKEN is not found in payload")
#
#     req.add_header("Authorization", "Bearer %s" % token)
#     req.add_header('Content-Type', 'application/json')
#
#     try:
#         logging.info('sending to ' + url)
#         logging.info('sent data ' + json.dumps(data))
#         response = urllib2.urlopen(req, json.dumps(data))
#     except HTTPError as e:
#         logging.error('The server couldn\'t fulfill the request.')
#         logging.error('Error code .' + e.code)
#     except URLError as e:
#         print 'We failed to reach a server.'
#         logging.error('We failed to reach a server.')
#         logging.error('Reason: ' + e.reason)


# my_ip = urllib2.urlopen('http://ip.42.pl/raw').read()
# my_username = "root"
# my_password = "root"
#
# data = {"key": "MYSQLIP", "values": my_ip}
# # do_post(resource_url, data)
# data = {"key": "MYSQL_PASS", "values": my_password}
# # do_post(resource_url, data)
#
# data = {"key": "MYSQL_USERNAME", "values": my_username}
# #do_post(resource_url, data)


# print("************************")

#
# ===================
#
# import urllib2
# import json
# import os
# import subprocess
#
# def get_app_is():
#     return "single-cartridge-app"
#
# def get_alias():
#     return "tomcat1"
#
# def get_metadata_url():
#     url = "https://localhost:9443/metadata/api/application/" + get_app_is() + "/cluster/" + get_alias() + "/properties"
#     return url;
#
# url = get_metadata_url()
#
# response = json.loads(urllib2.urlopen(url).read())
# propertis = response["properties"]
#
# for prop in propertis:
#     key =  prop["key"]
#     value = prop["values"]
#     os.environ[key] = str(value)
#
#
# def restart_service(name):
#     command = ['/usr/sbin/service', name, 'restart'];
#     #shell=FALSE for sudo to work.
#     subprocess.call(command, shell=False)
#
# print "Restarting apache server"
# restart_service("apache2")