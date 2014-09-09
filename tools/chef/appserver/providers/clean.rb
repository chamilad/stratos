action :clean do
	if new_resource.mode == 'refresh'
		execute "Remove lock file #{new_resource.deployment}" do
			path ['/usr/local/sbin', '/usr/local/bin', '/usr/sbin', '/usr/bin', '/sbin', '/bin']
			only_if "test -f #{new_resource.target}/wso2carbon.lck"
			command "rm #{new_resource.target}/wso2carbon.lck"
		end

		execute "Stop process #{new_resource.deployment}" do
			path ['/usr/local/sbin', '/usr/local/bin', '/usr/sbin', '/usr/bin', '/sbin', '/bin', '/opt/java/bin/']
			command "kill -9 `cat #{new_resource.target}/wso2carbon.pid` ; /bin/echo Killed"
		end
	elsif new_resource.mode == 'new'
		execute "Stop process and remove CARBON_HOME #{new_resource.deployment}" do
			path ['/usr/local/sbin', '/usr/local/bin', '/usr/sbin', '/usr/bin', '/sbin', '/bin', '/opt/java/bin/']
			command "kill -9 `cat #{new_resource.target}/wso2carbon.pid` ; rm -rf #{new_resource.target}"
		end
	elsif new_resource.mode == 'zero'
		execute "Stop process and remove CARBON_HOME and pack #{new_resource.deployment}" do
			path ['/usr/local/sbin', '/usr/local/bin', '/usr/sbin', '/usr/bin', '/sbin', '/bin', '/opt/java/bin/']
			command "kill -9 `cat #{new_resource.target}/wso2carbon.pid` ; rm -rf #{new_resource.target} ; rm -f #{node[:base][:local_package_dir]}/wso2#{new_resource.service_code}-#{new_resource.version}.zip"
		end
	end
end