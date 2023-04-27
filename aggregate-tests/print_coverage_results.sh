#!/bin/bash

RESULTS=$(grep -o "<tfoot>.*</tfoot>" aggregate-tests/target/site/jacoco-aggregate/index.html | grep -o "<td class=\"ctr2\">[0-9]*%</td>" | sed "s/<td class=\"ctr2\">//g" | sed "s/<\/td>//g")

echo "Test Results"

echo -ne "Missed Instructions Coverage: "
echo $RESULTS | tr ' ' '\n' | head -n 1

echo -ne "Missed Branches Coverage: "
echo $RESULTS | tr ' ' '\n' | tail -n 1

