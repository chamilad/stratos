# action :start

# attribute :owner, :kind_of => String
# attribute :target, :kind_of => String

action :start do
	execute "Starting #{new_resource.name}" do
		not_if { ::File.exists?("#{new_resource.target}/wso2carbon.lck")}
		user new_resource.owner
		path ['/usr/local/sbin', '/usr/local/bin', '/usr/sbin', '/usr/bin', '/sbin', '/bin', '/opt/java/bin/']
		command "touch #{new_resource.target}/wso2carbon.lck; bash stratos.sh > /dev/null 2>&1 &"
		cwd "#{new_resource.target}/bin/"
		action :run
	end

end