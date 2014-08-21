# action :start

# attribute :owner, :kind_of => String
# attribute :target, :kind_of => String

action :start do
	execute "Starting #{@current_resource.name}" do
		not_if { ::File.exists?("#{@current_resource.target}/wso2carbon.lck")}
		user @current_resource.owner
		path ['/usr/local/sbin', '/usr/local/bin', '/usr/sbin', '/usr/bin', '/sbin', '/bin', '/opt/java/bin/']
		command "touch #{@current_resource.target}/wso2carbon.lck; bash stratos.sh > /dev/null 2>&1 &"
		cwd "#{@current_resource.target}/bin/"
		action :run
	end

end