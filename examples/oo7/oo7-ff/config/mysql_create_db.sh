#!/bin/bash
DB_NAME=ssilva;
USER=ssilva;
PASS=ssilva;
MYSQL=/usr/local/mysql/bin/mysql
echo "drop database if exists $DB_NAME;
create database $DB_NAME;" > mysql_create_db.tmp
$MYSQL -u$USER -p$PASS < mysql_create_db.tmp
rm -f mysql_create_db.tmp
$MYSQL -u$USER -p$PASS $DB_NAME < ojb.sql
$MYSQL -u$USER -p$PASS $DB_NAME < dml.sql
$MYSQL -u$USER -p$PASS $DB_NAME < ../classes/oo7.sql
echo "Database '$DB_NAME' created."
