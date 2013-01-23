#!/bin/sh

while read line
do
	if [ -n "$line" ] 
	then 
		echo "db.execSQL(\"${line}\");" 
	else
		echo ""
	fi
done