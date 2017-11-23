#!/bin/bash
java -cp .:./target/classes/ -Djava.security.policy=client.policy cloudatlasClient/Client localhost SELECT 1 AS one

