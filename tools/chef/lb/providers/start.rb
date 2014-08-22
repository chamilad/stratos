
# attribute :deployment_code, :name_attribute => true, :kind_of => String
# attribute :owner, :kind_of => String
# attribute :target, :kind_of => String

action :start do
	execute "Starting #{new_resource.deployment_code}" do
		user new_resource.owner
		path '/usr/local/sbin', '/usr/local/bin', '/usr/sbin', '/usr/bin', '/sbin', '/bin', '/opt/java/bin/']
		command "touch #{new_resource.target}/wso2carbon.lck; #{new_resource.target}/bin/stratos.sh > /dev/null 2>&1 &"
		creates "#{new_resource.target}/repository/wso2carbon.log"
		not_if { ::File.exists?("#{new_resource.target}/wso2carbon.lck")}
	end
end