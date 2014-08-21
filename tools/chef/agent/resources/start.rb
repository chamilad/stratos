actions :start

attribute :name, :name_attribute => true, :kind_of => String
attribute :owner, :kind_of => String
attribute :target, :kind_of => String

def initialize(*args)
	super
	@action = :start
end