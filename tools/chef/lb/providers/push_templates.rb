
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
			variables ({
				:carbon_home => new_resource.target,
				:stratos_lb_category => ENV['STRATOS_LB_CATEGORY'],
				:stratos_cluster_id => ENV['STRATOS_CLUSTER_ID'],
				:stratos_network_partition_id => ENV['STRATOS_NETWORK_PARTITION_ID']
				})
		end
	end
end