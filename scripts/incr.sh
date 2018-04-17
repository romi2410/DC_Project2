#!/bin/bash

# Change this to your netid
#netid=rkp170230
netid=kxs141930

# Root directory of your project
PROJDIR=$HOME/DC_Project1

# Directory where the config file is located on your local system
CONFIGLOCAL=$HOME/DC_Project1/config/config2copy.txt

# Directory your java classes are in
javac $HOME/DC_Project1/src/dc_project1/*.java
BINDIR=$HOME/DC_Project1/src/dc_project1

# Your main project class
PROG=DC_Project1

JAR=$HOME/DC_Project1/dist/DC_Project1.jar

FILE=$HOME/DC_Project1/config/config3copy.txt

cat $CONFIGLOCAL | sed -e "s/#.*//" | sed -e "/^\s*$/d" |
(
    read i
    echo $i >> $FILE
    n=$i
    while [[ $i -gt 0 ]]
    do
    	read line
        node=$( echo $line | awk '{ print $1 }' )
	host=$( echo $line | awk '{ print $2 }' )
        port=$( echo $line | awk '{ print $3 }' )
        port=$(( port + 10 ))
        echo "$node $host $port" >> $FILE

        i=$(( i - 1 ))
    done
   
    i=$n
    while [[ $i -gt 0 ]]
        do
	   read line
	   echo $line >> $FILE

           i=$(( i - 1 ))
        done
)
mv $FILE $CONFIGLOCAL
