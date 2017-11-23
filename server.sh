#!/bin/bash
java -classpath .:./target/classes/:$CLASSPATH -Djava.rmi.server.hostname=localhost -Djava.security.policy=server.policy cloudatlasServer.Server