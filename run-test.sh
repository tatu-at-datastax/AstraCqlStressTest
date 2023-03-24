#!/bin/sh

# Various tables that are pre-created
#
# * system.local
# * system_schema.keyspaces

java -jar target/AstraCqlStressTest-0.5.jar ./secure-connect-test.zip "token" "$ASTRA_TOKEN" \
    system.local 20
