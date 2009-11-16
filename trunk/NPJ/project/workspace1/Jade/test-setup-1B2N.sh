#!/bin/bash       

$HOME/OscarRemove.command

ant jadeboot &
echo "BSH: jadeboot launched"
sleep 30
ant jadenode > outtestjadenode1 &
echo "BSH: jadenode1 launched"
sleep 15
ant jadenode > outtestjadenode2 &
echo "BSH: jadenode2 launched"


