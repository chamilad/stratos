actions :push

attribute :name, :name_attribute => true
attribute :target, :kind_of => String
attribute :owner, :kind_of => String
attribute :group, :kind_of => String

def initialize(*args)
	super
	@action = :push
end