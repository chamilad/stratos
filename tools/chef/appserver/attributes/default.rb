include_attribute "base::base"

default[:appserver][:depsync_svn_repo]        ='https://svn.appfactory.domain.com/wso2/repo/'
#default[:appserver][:local_package_dir]      ='/mnt/packs'
default[:appserver][:domain]                  = 'wso2.com'
default[:appserver][:as_subdomain]            ='appserver'
default[:appserver][:management_subdomain]    ='management'
default[:appserver][:admin_username]          ='ADMIN_USER'
default[:appserver][:admin_password]          ='ADMIN_PASSWORD'
default[:appserver][:registry_user]           ='DB_USER'
default[:appserver][:registry_password]       ='DB_PASSWORD'
default[:appserver][:registry_database]       ='REGISTRY_DB'
default[:appserver][:userstore_user]          ='DB_USER'
default[:appserver][:userstore_password]      ='DB_PASSWORD'
default[:appserver][:userstore_database]      ='USERSTORE_DB'
default[:appserver][:svn_user]                ='wso2'
default[:appserver][:svn_password]            ='wso2123'
default[:appserver][:auto_scaler_epr]         ='http://xxx:9863/services/AutoscalerService/'
default[:appserver][:ldap_connection_uri]     ='ldap://localhost:10389'
default[:appserver][:bind_dn]                 ='uid=admin,ou=system'
default[:appserver][:bind_dn_password]        ='adminpassword'
default[:appserver][:user_search_base]        ='ou=system'
default[:appserver][:group_search_base]       ='ou=system'
default[:appserver][:sharedgroup_search_base] ='ou=SharedGroups,dc=wso2,dc=org'
default[:appserver][:http_proxy_port]         ='80'
default[:appserver][:https_proxy_port]        = '443'

default[:appserver][:version]                 =""
default[:appserver][:sub_cluster_domain]      =""
default[:appserver][:members]                 =""
default[:appserver][:offset]                  =0
default[:appserver][:hazelcast_port]          =4000
default[:appserver][:config_db]               ='governance'
default[:appserver][:config_target_path]      ='config/as'
default[:appserver][:maintenance_mode]        = true
default[:appserver][:depsync]                 =false
default[:appserver][:clustering]              =false
default[:appserver][:cloud]                   =true
default[:appserver][:owner]                   ='root'
default[:appserver][:group]                   ='root'
default[:appserver][:target]                  ="/mnt/#{node[:base][:server_ip]}"

default[:agent][:docroot]                     = "/mnt/#{node[:base][:server_ip]}/wso2as-5.2.1"