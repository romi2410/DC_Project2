#!/bin/bash

# Change this to your netid
netid=kxs141930

# Root directory of your project
PROJDIR=$HOME/DC_Project1

# Directory where the config file is located on your local system
CONFIGLOCAL=$HOME/DC_Project1/config/config.txt

n=6

cat $CONFIGLOCAL | sed -e "s/#.*//" | sed -e "/^\s*$/d" |
(
    read i
    echo $i
    while [[ $n -gt 0 ]]
    do
    	read line
        host=$( echo $line | awk '{ print $2 }' )

        echo $host
        gnome-terminal -e "ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no $netid@$host killall -u $netid" &
        sleep 1

        n=$(( n - 1 ))
    done
   
)


echo "Cleanup complete"
