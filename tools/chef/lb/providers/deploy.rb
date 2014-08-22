# actions :deploy

# attribute :deployment_code, :name_attribute => true, :kind_of => String
# attribute :service, :kind_of => String
# attribute :security, :kind_of => [TrueClass, FalseClass]
# attribute :owner, :kind_of => String
# attribute :group, :kind_of => String
# attribute :target, :kind_of => String

action :deploy do
	remote_directory "/tmp//#{new_resource.deployment_code}" do
		cookbook new_resource.service
		source "configs"
	end

	remote_directory "/tmp//#{new_resource.deployment_code}" do
		cookbook new_resource.service
		source "patches"
	end

	execute "Copy #{new_resource.deployment_code} modules to carbon home" do
		path ['/usr/local/sbin', '/usr/local/bin', '/usr/sbin', '/usr/bin', '/sbin', '/bin', '/opt/java/bin/']
		command "cp -r /tmp/#{new_resource.deployment_code}/* #{new_resource.target}/; chown -R #{new_resource.owner}:#{new_resource.owner} #{new_resource.target}/; chmod -R 755 #{new_resource.target}/"
	end

	execute "Remove #{new_resource.deployment_code} temporary modules directory" do
		path ['/usr/local/sbin', '/usr/local/bin', '/usr/sbin', '/usr/bin', '/sbin', '/bin', '/opt/java/bin/']
		command "rm -rf /tmp/#{new_resource.deployment_code}"
	end
end