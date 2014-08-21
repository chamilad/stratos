# action :push

# attribute :target, :kind_of => String

action :push do
	new_resource.name.each do |template_name|
		template "#{new_resource.target}/#{template_name}" do
			owner new_resource.owner
			group new_resource.group
			source "#{template_name}.erb"
			variables :carbon_home => new_resource.target
			mode '0755'
		end
	end
end