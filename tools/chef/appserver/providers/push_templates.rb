
# attribute :service_templates, :name_attribute => true, :kind_of => [String]
# attribute :target, :kind_of => String
# attribute :directory, :kind_of => String

action :push do
	new_resource.service_templates.each do |template_name|
		template "#{new_resource.target}/repository/#{template_name}" do
			owner new_resource.owner
			group new_resource.group
			source "#{template_name}.erb"
			mode '0755'
			variables :carbon_home => new_resource.target
		end
	end
end