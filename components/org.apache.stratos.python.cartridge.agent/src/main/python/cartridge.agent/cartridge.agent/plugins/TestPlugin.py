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
#
# from plugins.contracts import ICartridgeAgentPlugin
# from modules.util.log import LogFactory
#
#
# class TestPlugin(ICartridgeAgentPlugin):
#
#     def run_plugin(self, values):
#         log = LogFactory().get_log(__name__)
#         log.debug("Running test plugin for event %s" % values["EVENT"])
#         for key, value in values.iteritems():
#             log.debug("%s => %s" % (key, value))