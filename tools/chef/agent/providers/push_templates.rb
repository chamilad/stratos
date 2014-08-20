# action :push

# attribute :target, :kind_of => String

action :push do
	name.each do |template_name|
		template "#{@current_resource.target}/#{template_name}" do
			owner @current_resource.owner
			group @current_resource.group
			mode '0755'
		end
	end
end