include_attribute "base::base"
default[:tomcat][:target] = '/mnt'
default[:tomcat][:owner] = 'root'
default[:tomcat][:group] = 'root'
default[:agent][:docroot] = "/mnt/apache-tomcat-#{node[:tomcat][:version]}/webapps/"
default[:agent][:samlalias] = "/mnt/apache-tomcat-#{node[:tomcat][:version]}/webapps/"