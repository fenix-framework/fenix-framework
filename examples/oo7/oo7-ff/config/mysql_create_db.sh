#!/bin/bash
DB_NAME=ssilva;
USER=ssilva;
PASS=ssilva;
MYSQL=/usr/local/mysql/bin/mysql
echo "drop database if exists $DB_NAME;
create database $DB_NAME;" > mysql_create_db.tmp
$MYSQL -u$USER -p$PASS < mysql_create_db.tmp
rm -f mysql_create_db.tmp
echo "Database '$DB_NAME' created."
