actions :clean

attribute :deployment, :name_attribute => true, :kind_of => String
attribute :mode, :kind_of => String
attribute :target, :kind_of => String
attribute :service_code, :kind_of => String
attribute :version, :kind_of => String

def initialize(*args) do
	super
	@action = :clean
end