deployment_code = 'appserver'
carbon_version = node[:appserver][:version]
service_code = 'as'
carbon_home = "#{node[:appserver][:target]}/wso2#{service_code}-#{carbon_version}"

case node[:appserver][:sub_cluster_domain]
when 'mgt'
	service_templates = ['conf/axis2/axis2.xml',
      'conf/carbon.xml',
#      'conf/datasources/master-datasources.xml',
#      'conf/registry.xml',
#      'conf/tomcat/catalina-server.xml',
#      'conf/user-mgt.xml'
	]
when 'worker'
	service_templates = [
		'conf/axis2/axis2.xml',
      'conf/carbon.xml',
#      'conf/datasources/master-datasources.xml',
#      'conf/registry.xml',
#      'conf/tomcat/catalina-server.xml',
#      'conf/user-mgt.xml'
	]
else
	service_templates = [
		'conf/axis2/axis2.xml',
      'conf/carbon.xml',
      'conf/datasources/master-datasources.xml',
      'conf/registry.xml',
      'conf/tomcat/catalina-server.xml',
      'conf/user-mgt.xml',
      'conf/log4j.properties',
      'conf/security/authenticators.xml'
	]
end

appserver_clean deployment_code do
	mode 			node[:appserver][:maintenance_mode]
	target			carbon_home
	service_code	service_code
	version 		carbon_version
end

appserver_initialize deployment_code do
	repo      node[:base][:package_repo]
    version   carbon_version
    service   service_code
    local_dir node[:base][:local_package_dir]
    target    node[:appserver][:target]
    mode      node[:appserver][:maintenance_mode]
    owner     node[:appserver][:owner]
end

appserver_deploy deployment_code do
	security 	true
	owner 		node[:appserver][:owner]
	group 		node[:appserver][:group]
	target		carbon_home
end

appserver_push_templates service_templates do
	target 		carbon_home
	directory 	deployment_code
	owner 		node[:appserver][:owner]
	group 		node[:appserver][:group]
end

# appserver_start deployment_code do
# 	owner node[:appserver][:owner]
# 	target carbon_home
# end