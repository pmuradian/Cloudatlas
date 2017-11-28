#!/bin/bash
java -cp .:./target/classes/ -Djava.security.policy=client.policy pl.edu.mimuw.cloudatlas.cloudatlasClient.Client

