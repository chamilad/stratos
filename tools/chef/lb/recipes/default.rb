#
# Cookbook Name:: lb
# Recipe:: default
#
# Copyright 2014, YOUR_COMPANY_NAME
#
# All rights reserved - Do Not Redistribute
#

deployment_code = 'lb'
carbon_version  = node[:lb][:version]
service_code    = 'load-balancer'
carbon_home     = "#{node[:lb][:target]}/apache-stratos-#{service_code}-#{carbon_version}"

service_templates=%w(conf/axis2/axis2.xml
					conf/loadbalancer.conf
					conf/templates/jndi.properties.template)

lb_clean deployment_code do
	mode 			node[:lb][:maintenance_mode]
	target 			carbon_home
	service_code	service_code
	version 		carbon_version
end

lb_initialize deployment_code do
	repo 		node[:base][:package_repo]
	version 	carbon_version
	service 	service_code
	local_dir 	node[:base][:local_package_dir]
	target 		node[:lb][:target]
	mode 		node[:lb][:maintenance_mode]
	owner 		node[:lb][:owner]
end

lb_deploy deployment_code do
	service 	deployment_code
	security	true
	owner		node[:lb][:owner]
	group 		node[:lb][:group]
	target		carbon_home
end

lb_push_templates service_templates do
	target 		carbon_home
	directory 	deployment_code
	owner		node[:lb][:owner]
	group 		node[:lb][:group]
end

lb_start deployment_code do
	owner 	node[:lb][:owner]
	target	carbon_home
end