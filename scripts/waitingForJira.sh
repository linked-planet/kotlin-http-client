#!/bin/bash
maxWaitTime=120
index=1
result=0
echo "### Checking Jira is running"
while [ $result -eq 0 ]
do
  response=$(curl http://localhost:8080/status 2> /dev/null)
  if [ "$response" == '{"state":"RUNNING"}' ]
  then
    result=1
  fi
  if [ $index -ge $maxWaitTime ]
  then
    echo "!!! JIRA NOT RUNNING AFTER $maxWaitTime SECONDS"
    exit 1
  fi
  sleep 1
  echo -"### WAITING FOR JIRA since $index Seconds"
  index=$((index+1))
done
echo "### JIRA IS UP"
sleep 5
