actions :deploy

attribute :deployment_code, :name_attribute => true, :kind_of => String
attribute :service, :kind_of => String
attribute :security, :kind_of => [TrueClass, FalseClass]
attribute :owner, :kind_of => String
attribute :group, :kind_of => String
attribute :target, :kind_of => String

def initialize(*args)
	super
	@action = :deploy
end