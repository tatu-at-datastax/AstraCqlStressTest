# AstraCqlStressTest

Simple test utility for stress-testing authenticated CQL sessions

## Build

Uses Maven (may added wrapper) but until then:

   mvn clean package

which builds an executable uber-jar under `target/`

## Usage

Basic usage is shown in `./run-test.sh`:

   java -jar target/AstraCqlStressTest-0.5.jar ./secure-connect-test.zip "$CLIENT_SECRET"

where you may need to change Secure Connect Bundle path, and need to define client secret appropriately.
