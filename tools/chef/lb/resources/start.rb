actions :start

attribute :deployment_code, :name_attribute => true, :kind_of => String
attribute :owner, :kind_of => String
attribute :target, :kind_of => String

def initialize (*args) do
	super
	@action = :start
end