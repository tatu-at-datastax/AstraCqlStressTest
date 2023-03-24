#!/bin/sh

java -jar target/AstraCqlStressTest-0.5.jar ./secure-connect-test.zip "token" "$ASTRA_TOKEN" system.local
