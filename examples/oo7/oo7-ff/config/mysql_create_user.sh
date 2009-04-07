#!/bin/bash
USER=ssilva
PASS=ssilva
ROOTPASS=
HOST=localhost
MYSQL=/usr/local/mysql/bin/mysql
echo "use mysql;
drop user $USER;
create user $USER;
update user SET password = PASSWORD('$PASS') where user='$USER';
GRANT USAGE ON *.* TO '$USER'@'$HOST' IDENTIFIED BY '$PASS';
GRANT ALL PRIVILEGES ON *.* TO '$USER'@'$HOST' IDENTIFIED BY '$PASS';" > mysql_create_user.tmp
$MYSQL --user=root --password=$ROOTPASS mysql < mysql_create_user.tmp
rm -f mysql_create_user.tmp
echo "User '$USER' created with privileges."
