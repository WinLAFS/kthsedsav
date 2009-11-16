#!/bin/sh

export DKS_HOME=/home/joel/MyWorkbench/DKS

JAVA_HOME=/usr/java/jdk1.6.0/bin/java

#
# log4j configuration neads at least 65 stack frames so ss=64k is too little
#

TIMESTAMP=`date +%F_%H%M%S`

LOGFILE="$DKS_HOME/logs/dks-$3-$TIMESTAMP.log"
LOG4JFILE="$DKS_HOME/logs/niche-$3-$TIMESTAMP.log4j"

CLASSPATH=$DKS_HOME/dks.jar:$DKS_HOME/lib/log4j-1.2.14.jar:$DKS_HOME/lib/junit-4.1.jar

# PROF_ARGS="-agentlib:hprof=heap=sites,cpu=samples"

JVM_ARGS="-server -classpath $CLASSPATH -Ddks.propFile=$DKS_HOME/dksParam.prop -Dorg.apache.log4j.config.file=$DKS_HOME/log4j.config -Ddks.log.file=$LOG4JFILE"

nohup java $JVM_ARGS $* > $LOGFILE 2>&1 &

EXIT_VALUE=$?

PID=$!

if [ $EXIT_VALUE -eq 0 ]; then
	echo -n $PID
	exit 0;
else
	cat $LOGFILE
	exit $EXIT_VALUE;
fi
