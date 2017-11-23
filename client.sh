#!/bin/bash
java -cp .:./target/classes/:./target/classes/cloudatlasRMI/ -Djava.security.policy=client.policy cloudatlasClient/Client localhost SELECT 1 AS one

