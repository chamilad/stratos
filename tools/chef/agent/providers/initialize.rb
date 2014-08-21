# action :init

# attribute :repo, :kind_of => String
# attribute :version, :kind_of => String
# attribute :service, :kind_of => String
# attribute :local_dir, :kind_of => String
# attribute :target, :kind_of => String
# attribute :owner, :kind_of => String

action :init do
	directory "#{new_resource.local_dir}"

	cookbook_file "#{new_resource.local_dir}/apache-stratos-#{new_resource.service}-#{new_resource.version}.zip"

	execute "Creating target for #{new_resource.name}" do
		path ['/usr/local/sbin', '/usr/local/bin', '/usr/sbin', '/usr/bin','/sbin','/bin']
		command "mkdir -p #{new_resource.target}"
		action :run
	end

	execute "Creating local package repo for #{new_resource.name}" do
		path ['/usr/local/sbin', '/usr/local/bin', '/usr/sbin', '/usr/bin', '/sbin', '/bin', '/opt/java/bin/']
		command "mkdir -p #{new_resource.local_dir}"
		not_if { ::File.exists?(new_resource.local_dir) }
	end

	# execute "Downloading apache-stratos-#{new_resource.service}-#{new_resource.version}-bin.zip" do
	# 	path '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin'
	# 	cwd new_resource.local_dir
	# 	not_if { ::File.exists? ("#{new_resource.local_dir}/apache-stratos-#{new_resource.service}-#{new_resource.version}-bin.zip")}

	# end

	execute "Extracting stratos #{new_resource.service}-#{new_resource.version}.zip for #{new_resource.name}" do
		path ['/usr/local/sbin', '/usr/local/bin', '/usr/sbin', '/usr/bin', '/sbin', '/bin']
		cwd new_resource.target
		not_if { ::File.exists? ("#{new_resource.target}/apache-stratos-#{new_resource.service}-#{new_resource.version}/conf")}
		command "unzip #{new_resource.local_dir}/apache-stratos-#{new_resource.service}-#{new_resource.version}.zip"
		creates "#{new_resource.target}/apache-stratos-#{new_resource.service}-#{new_resource.version}/repository"
		#timeout 0
	end

	execute "Setting permission for #{new_resource.name}" do
		path ['/usr/local/sbin', '/usr/local/bin', '/usr/sbin', '/usr/bin', '/sbin', '/bin']
		cwd new_resource.target
		command "chown -R #{new_resource.owner}:#{new_resource.owner} #{new_resource.target}/apache-stratos-#{new_resource.service}-#{new_resource.version} ;
				 chmod -R 755 #{new_resource.target}/apache-stratos-#{new_resource.service}-#{new_resource.version}"
		#timeout 0
	end

	remote_directory "/#{new_resource.target}/apache-stratos-#{new_resource.service}-#{new_resource.version}/lib" do
		source node[:mb][:type]
	end



end