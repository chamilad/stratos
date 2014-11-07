# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

class python_agent(
  $version                = '4.1.0',
  $owner                  = 'root',
  $group                  = 'root',
  $target                 = "/mnt",
  $type                   = 'default',
  $enable_artifact_update = true,
  $auto_commit            = false,
  $auto_checkout          = true,
  $module                 = 'undef',
  $custom_templates       = [],
  $exclude_templates	  = []
){

  $service_code    = 'cartridge-agent'
  $agent_name = "apache-stratos-python-${service_code}-${version}"
  $agent_home= "${target}/${agent_name}"

  $split_mburl = split($mb_url, "//")
  $split_mburl = split($split_mburl[1], ":")
  $mb_ip = $split_mburl[0]
  $mb_port = $split_mburl[1]

  tag($service_code)

  $default_templates = [
    'extensions/clean.sh',
    'extensions/instance-activated.sh',
    'extensions/instance-started.sh',
    'extensions/start-servers.sh',
    'extensions/artifacts-copy.sh',
    'extensions/artifacts-updated.sh',
    'extensions/complete-tenant.sh',
    'extensions/complete-topology.sh',
    'extensions/member-activated.sh',
    'extensions/member-suspended.sh',
    'extensions/member-terminated.sh',
    'extensions/mount-volumes.sh',
    'extensions/subscription-domain-added.sh',
    'extensions/subscription-domain-removed.sh',
    'agent.conf',
    'logging.ini',
  ]

  agent::initialize { $service_code:
    repo      => $package_repo,
    version   => $carbon_version,
    service   => $agent_name,
    local_dir => $local_package_dir,
    target    => $target,
    owner     => $owner,
  }

  exec { 'copy launch-params to agent_home':
    path    => '/bin/',
    command => "mkdir -p ${agent_home}/payload; cp /tmp/payload/launch-params ${agent_home}/payload/launch-params",
    require => Agent::Initialize[$service_code];
  }

  exec { 'make extension folder':
    path    => '/bin/',
    command => "mkdir -p ${target}/${service_code}/extensions",
    require => Agent::Initialize[$service_code];
  }


# excluding templates which are not needed by a cartridge module from default_templates
  $default_templates_excluded = difference($default_templates,$exclude_templates)

# excluding custom_templates, if any,(which will be overrided by a cartridge module) from default_templates
  $service_templates = $module ? {
    'undef'    => $default_templates_excluded,
    default   => difference($default_templates_excluded,$custom_templates)
  }

# applying default extensions
  agent::push_templates {
    $service_templates:
      target    => $agent_home,
      template_dir => "agent",
      require   => Agent::Initialize[$service_code];
  }

# applying custom extensions
  unless $module == 'undef' {
    agent::push_templates {
      $custom_templates:
        target    => $carbon_home,
        template_dir => "${module}/agent",
        require   => [Agent::Initialize[$service_code]]
    }
  }

# removing default extensions which are shipped by agent.zip
  agent::remove_templates {
    $exclude_templates:
      target    => $carbon_home,
  }

  $required_resources = $module ? {
    'undef'  => [
      Exec['copy launch-params to agent_home'],
      Agent::Push_templates[$default_templates_excluded],
    ],
    default =>[
      Exec['copy launch-params to agent_home'],
      Agent::Push_templates[$default_templates_excluded],
      Agent::Push_templates[$custom_templates]         ]
  }

  agent::start { $service_code:
    owner   => $owner,
    target  => $agent_home,
    require => $required_resources
  }
}