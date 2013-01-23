#!/bin/sh

VERB_ID=1
while read line
do
	PRESENT=`echo ${line} | awk '{print $1}'`
	PRETERITE=`echo ${line} | awk '{print $3}'`
	PERFECT=`echo ${line} | awk '{print $4}'`
	TRANSLATION=`echo ${line} | awk '{print $5}'`
	
	echo "INSERT INTO verb (id, present) VALUES(${VERB_ID}, '${PRESENT}');"
	
	# TRANSLATION
	for TRAN in $(echo ${TRANSLATION} | tr '/' ' ')
	do
		echo "INSERT INTO translation (verb_id, translation) VALUES(${VERB_ID}, '${TRAN}');"
	done
	
	# PRETERITE
	for PRET in $(echo ${PRETERITE} | tr '/' ' ')
	do
		echo "INSERT INTO preterite (verb_id, preterite) VALUES(${VERB_ID}, '${PRET}');"
	done
	
	# PERFECT
	for PERF in $(echo ${PERFECT} | tr '/' ' ')
	do
		echo "INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(${VERB_ID}, 'habe', '${PERF}');"
	done
	
	echo ""
	((VERB_ID++))
done