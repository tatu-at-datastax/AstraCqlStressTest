# AstraCqlStressTest

Simple test utility for stress-testing authenticated CQL sessions

## Build

Uses Maven (may added wrapper) but until then:

   mvn clean package

which builds an executable uber-jar under `target/`

## Usage

Basic usage is shown in `./run-test.sh`:

   java -jar target/AstraCqlStressTest-0.5.jar ./secure-connect-test.zip "token" "$ASTRA_TOKEN" \
    system.local 20

where you define:

1. Path to Secure Connection Bundle (zip file)
2. Pass Astra token in $ASTRA_TOKEN -- or, if you prefer, replace "token"/$ASTRA_TOKEN with Client Id / Client Secret
3. Specify table to do "SELECT *" from (`system.local` always exists, hence default)
4. May change Thread Count, defaults to 20
