actions :push

attribute :service_templates, :name_attribute => true
attribute :target, :kind_of => String
attribute :directory, :kind_of => String
attribute :owner, :kind_of => String
attribute :group, :kind_of => String

def initialize(*args)
	super
	@action = :push
end