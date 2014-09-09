actions :init

attribute :deployment_code, :name_attribute => true, :kind_of => String
attribute :repo, :kind_of => String
attribute :version, :kind_of => String
attribute :service, :kind_of => String
attribute :local_dir, :kind_of => String
attribute :target, :kind_of => String
attribute :mode, :kind_of => String
attribute :owner, :kind_of => String

def initialize(*args)
	super
	@action = :init
end