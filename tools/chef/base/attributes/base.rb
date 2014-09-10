# require stratos_base

default[:base][:package_repo]          = 'http://10.4.128.7'
default[:base][:local_package_dir]     = '/mnt/packs'

default[:mb][:ip]                      = 'MB_IP'
default[:mb][:port]                    = '61616'
default[:mb][:type]                    = 'activemq' #in wso2 mb case, value should be 'wso2mb'

default[:cep][:ip]                     = 'CEP_IP'
default[:cep][:port]                   = '7611'

default[:truststore][:password]        = 'wso2carbon'

#default[:java][:distribution]          ='jdk-7u51-linux-x64.tar.gz'
default[:java][:distribution]          ='jdk-7u7-linux-x64.tar.gz'
#default[:java][:name]                  ='jdk1.7.0_51'
default[:java][:name]                  ='jdk1.7.0_07'

default[:lb][:member_type_ip]          ='private'
default[:lb][:http_port]               = '80'
default[:lb][:https_port]              = '443'

default[:tomcat][:version]             = '7.0.54'

default[:agent][:enable_log_publisher] = 'false'

default[:bam][:ip]                     ='BAM_IP'
default[:bam][:port]                   ='7611'
default[:bam][:secure_port]            ='7711'
default[:bam][:username]               = 'admin'
default[:bam][:password]               = 'admin'

#include_attribute "stratos_base" included in the recipe

default[:base][:greg_url]                               = 'https://localhost/registry'
default[:base][:local_package_dir]                      = '/mnt/packs'
default[:base][:mysql_max_active]                       = '150'
default[:base][:mysql_max_connections]                  = '100000'
default[:base][:mysql_max_wait]                         = '360000'
default[:base][:mysql_port]                             = 'DB_PORT'
default[:base][:mysql_server]                           = 'DB_HOST'
default[:base][:package_repo]                           = 'http://10.4.128.7'
default[:base][:server_ip]                              = node['ipaddress']

