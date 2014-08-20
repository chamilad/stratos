# action :init

# attribute :repo, :kind_of => String
# attribute :version, :kind_of => String
# attribute :service, :kind_of => String
# attribute :local_dir, :kind_of => String
# attribute :target, :kind_of => String
# attribute :owner, :kind_of => String

action :init do
	directory "#{@current_resource.local_dir}"

	cookbook_file "#{@current_resource.local_dir}/apache-stratos-#{@current_resource.service}-#{@current_resource.version}-bin.zip"

	execute "Creating target for #{@current_resource.name}" do
		path '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin'
		command "mkdir -p #{@current_resource.target}"
		action :run
	end

	execute "Creating local package repo for #{@current_resource.name}" do
		path '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/java/bin/'
		command "mkdir -p #{@current_resource.local_dir}"
		not_if { ::File.exists?(@current_resource.local_dir) }
	end

	# execute "Downloading apache-stratos-#{@current_resource.service}-#{@current_resource.version}-bin.zip" do
	# 	path '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin'
	# 	cwd @current_resource.local_dir
	# 	not_if { ::File.exists? ("#{@current_resource.local_dir}/apache-stratos-#{@current_resource.service}-#{@current_resource.version}-bin.zip")}

	# end

	execute "Extracting stratos #{@current_resource.service}-#{@current_resource.version}.zip for #{@current_resource.name}" do
		path '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin'
		cwd @current_resource.target
		not_if { ::File.exists? ("#{@current_resource.target}/apache-stratos-#{@current_resource.service}-#{@current_resource.version}/conf")}
		command "unzip #{@current_resource.local_dir}/apache-stratos-#{@current_resource.service}-#{@current_resource.version}-bin.zip"
		creates "#{@current_resource.target}/apache-stratos-#{@current_resource.service}-#{@current_resource.version}/repository"
		timeout 0
	end

	execute "Setting permission for #{@current_resource.name}" do
		path '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin'
		cwd @current_resource.target
		command "chown -R #{@current_resource.owner}:#{@current_resource.owner} #{@current_resource.target}/apache-stratos-#{@current_resource.service}-#{@current_resource.version} ;
				 chmod -R 755 #{@current_resource.target}/apache-stratos-#{@current_resource.service}-#{@current_resource.version}"
		timeout 0
	end

	remote_directory "/#{@current_resource.target}/apache-stratos-#{@current_resource.service}-#{@current_resource.version}/lib" do
		source node[:mb][:type]
	end



end