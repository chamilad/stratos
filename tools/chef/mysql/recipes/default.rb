#--------------------------------------------------------------
#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#
#--------------------------------------------------------------

cookbook_file "/etc/apt/apt.conf.d/90forceyes" do
  mode 0755
end

execute "apt-update" do
  command "apt-get update > /dev/null 2>&1"
end

packages = %w(
    'mysql-server'
    'phpmyadmin'
    'apache2')

#Installing packages
packages.each do |pkg|
  package pkg
  log "Installed package : #{pkg}"
end

#Creating phpmyadmin configuration file
template "/etc/apache2/conf.d/phpmyadmin.conf" do
  source "phpMyAdmin.conf.erb"
  owner 'root'
  group 'root'
  mode '0775'
  action :create
  ############  not if package code
end


#Creating mysql configuration file
template "/etc/mysql/my.cnf" do
  source "my.cnf.erb"
  owner 'root'
  group 'root'
  mode '0775'
  action :create
  ############  not if package code
end

#Creating default host file
template "/etc/apache2/sites-enabled/000-default" do
  source "000-default.erb"
  owner 'root'
  group 'root'
  mode '0775'
  action :create
  ############  not if package code
end

#set default mysql password root
execute "mysql-root-password-set" do
  command "mysqladmin -u root password root"
end

#restart mysql
execute "mysql-server-restart" do
  command "service mysql restart"
end

#restart apache
execute "mysql-server-restart" do
  command "service apache2 restart"
end





