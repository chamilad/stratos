#!/bin/bash
# --------------------------------------------------------------
#
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
# 	http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#
# --------------------------------------------------------------

MKDIR=`which mkdir`
UNZIP=`which unzip`
ECHO=`which echo`
FIND=`which find`
GREP=`which grep`
RM=`which rm`
XARGS=`which xargs`
SED=`which sed`
CUT=`which cut`
AWK=`which awk`
IFCONFIG=`which ifconfig`
HOSTNAME=`which hostname`
SLEEP=`which sleep`
TR=`which tr`
HEAD=`which head`
WGET=`which wget`

IP=`${IFCONFIG} eth0 | ${GREP} -e "inet addr" | ${AWK} '{print $2}' | ${CUT} -d ':' -f 2`
LOG=/tmp/puppet-init.log
echo "IP address : ${IP}" | tee -a $LOG

HOSTSFILE=/etc/hosts
HOSTNAMEFILE=/etc/hostname
PUPPETCONF=/etc/puppet/puppet.conf
PUPPET_SELECTED="true"

is_public_ip_assigned() {

while true
do
   wget http://169.254.169.254/latest/meta-data/public-ipv4
   if [ ! -f public-ipv4 ]
    	then
      	echo "Public ipv4 file not found. Sleep and retry" | tee -a $LOG
      	sleep 2;
      	continue;
    	else
      	echo "public-ipv4 file is available. Read value" | tee -a $LOG
      	# Here means file is available. Read the file
      	read -r ip<public-ipv4;
      	echo "value is **[$ip]** " | tee -a $LOG

      	if [ -z "$ip" ]
        	then
          	echo "File is empty. Retry...." | tee -a $LOG
          	sleep 2
          	rm public-ipv4
          	continue
         	else
           	echo "public ip is assigned. value is [$ip]. Remove file" | tee -a $LOG
           	rm public-ipv4
           	break
         	fi
    	fi
done
}

run_puppet_agent() {
    ${ECHO} "Running puppet agent" | tee -a $LOG

    PUPPET=`which puppet`
    PUPPETAGENT="${PUPPET} agent"
    RUNPUPPET="${PUPPETAGENT} -vt"

    ${SLEEP} 5
    ${PUPPETAGENT} --enable
    ${RUNPUPPET}
    ${PUPPETAGENT} --disable
}

run_chef_client() {
    ${ECHO} "Configuring chef-client" | tee -a $LOG

    CHEF_CLIENT=`which chef-client`
    VALIDATER_KEY="/etc/chef/chef-validator.pem"

    # Remove old client.pem file if it exists
    if [ -f /etc/chef/client.pem ]; then
      ${ECHO} "Removing old client certificates" | tee -a $LOG
      ${RM} -rf /etc/chef/client.pem
    fi

    ${ECHO} "Registering chef-client with server" | tee -a $LOG
    CHEF_REGISTRATION="${CHEF_CLIENT} -S https://${CHEF_HOSTNAME} -K ${VALIDATER_KEY}"
    ${CHEF_REGISTRATION}

    ${ECHO} "Creating chef-client configuration" | tee -a $LOG
    cat > /etc/chef/client.rb << EOF
    log_level       :info
    log_location    STDOUT
    chef_server_url "https://${CHEF_HOSTNAME}"
    validation_key  "${VALIDATER_KEY}"
    no_lazy_load  true
EOF

    ${ECHO} "Creating run list" | tee -a $LOG
    RUN_LIST_FILE=/etc/chef/run_list.json
    printf '{"run_list" : ["role[%s]"]}\n' "${SERVICE_NAME}" > ${RUN_LIST_FILE}

    ${ECHO} "Running chef-client" | tee -a $LOG
    CHEF_CLIENT_RUN="${CHEF_CLIENT} -j ${RUN_LIST_FILE}"
    ${CHEF_CLIENT_RUN}
}

set_hostnames() {
    local ipaddress=$1
    local hostname=$2

    ${ECHO} "${ipaddress}  ${hostname}" >> ${HOSTSFILE}
    ${ECHO} "127.0.0.1 ${HOST}" >> ${HOSTSFILE}

    /etc/init.d/hostname start
}

DATE=`date +%d%m%y%S`
RANDOMNUMBER="`${TR} -c -d 0-9 < /dev/urandom | ${HEAD} -c 4`${DATE}"

if [ ! -d /tmp/payload ]; then

	## Check whether the public ip is assigned
	is_public_ip_assigned

	echo "Public ip have assigned. Continue.." | tee -a $LOG

	${MKDIR} -p /tmp/payload
	${WGET} http://169.254.169.254/latest/user-data -O /tmp/payload/launch-params

	cd /tmp/payload
	SERVICE_NAME=`sed 's/,/\n/g' launch-params | grep SERVICE_NAME | cut -d "=" -f 2`
	DEPLOYMENT=`sed 's/,/\n/g' launch-params | grep DEPLOYMENT | cut -d "=" -f 2`
	INSTANCE_HOSTNAME=`sed 's/,/\n/g' launch-params | grep HOSTNAME | cut -d "=" -f 2`
	CONFIG_AUTO_FLAG=`sed 's/,/\n/g' launch-params | grep CONFIG_AUTO_FLAG | cut -d "=" -f 2`

	if [[ "${CONFIG_AUTO_FLAG}" = "puppet" ]]; then
      PUPPET_SELECTED="true"
      ${ECHO} "Configuring Puppet" | tee -a $LOG
      PUPPET_IP=`sed 's/,/\n/g' launch-params | grep PUPPET_IP | cut -d "=" -f 2`
      PUPPET_HOSTNAME=`sed 's/,/\n/g' launch-params | grep PUPPET_HOSTNAME | cut -d "=" -f 2`
      PUPPET_ENV=`sed 's/,/\n/g' launch-params | grep PUPPET_ENV | cut -d "=" -f 2`

      #essential to have PUPPET_HOSTNAME at the end in order to auto-sign the certs
      DOMAIN="${PUPPET_HOSTNAME}"
	elif [[ "${CONFIG_AUTO_FLAG}" = "chef" ]]; then
	    PUPPET_SELECTED="false"
      ${ECHO} "Configuring Chef" | tee -a $LOG
	    CHEF_IP=`sed 's/,/\n/g' launch-params | grep CHEF_IP | cut -d "=" -f 2`
      CHEF_HOSTNAME=`sed 's/,/\n/g' launch-params | grep CHEF_HOSTNAME | cut -d "=" -f 2`

      # Assigning Chef server domain name for consistansy
      DOMAIN="${CHEF_HOSTNAME}"
	fi

	NODEID="${RANDOMNUMBER}.${DEPLOYMENT}.${SERVICE_NAME}"

	${ECHO} -e "\nNode Id ${NODEID}\n" | tee -a $LOG
	${ECHO} -e "\nDomain ${DOMAIN}\n" | tee -a $LOG

	HOST="${NODEID}.${DOMAIN}"
  ${ECHO} "Assigning hostname ${HOST}" | tee -a $LOG
	${HOSTNAME} ${HOST}
	${ECHO} "${HOST}" > ${HOSTNAMEFILE}

	if [[ "${PUPPET_SELECTED}" = "true" ]]; then
	    sed -i "s/server=.*/server=${PUPPET_HOSTNAME}/g"  ${PUPPETCONF}
	    /etc/init.d/puppet restart
	    ARGS=("-n${NODEID}" "-d${DOMAIN}" "-s${PUPPET_IP}")
	    #${ECHO} "${PUPPET_IP}  ${PUPPET_HOSTNAME}" >> ${HOSTSFILE}
	    set_hostnames ${PUPPET_IP} ${PUPPET_HOSTNAME}

	    run_puppet_agent
	else
	    #${ECHO} "${CHEF_IP}  ${CHEF_HOSTNAME}" >> ${HOSTSFILE}
	    set_hostnames ${CHEF_IP} ${CHEF_HOSTNAME}

	    run_chef_client
	fi

    ${ECHO} -e "Initialization completed successfully." | tee -a $LOG
fi

# END