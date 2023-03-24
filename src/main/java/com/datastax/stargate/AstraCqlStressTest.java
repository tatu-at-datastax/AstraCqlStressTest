package com.datastax.stargate;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;

public class AstraCqlStressTest
{
    final Path scbPath;
    final String clientSecret;
    
    private AstraCqlStressTest(String scbPath, String clientSecret) {
        clientSecret = clientSecret.trim();
        if (clientSecret.isEmpty()) {
            System.err.println("Empty clientSecret.");
            System.exit(1);
        }
        this.scbPath = Paths.get(scbPath);
        if (!this.scbPath.toFile().canRead()) {
            System.err.println("Path '"+scbPath+" not readable.");
            System.exit(1);
        }
        this.clientSecret = clientSecret;
    }

    void runTest() throws Exception
    {
        // First: try running just once, as sanity check
        System.out.println("Starting tests: make one test request first...");
        try {
            runOnce("INIT");
        } catch (Exception e) {
            System.err.println("INIT call failed, with: "+e.getMessage());
            System.err.println("INIT call failed, exiting.");
            System.exit(1);
        }

        System.out.println("INIT call succeeded, start test...");
        runMainTest();
        System.out.println("TEST complete!");
    }

    private void runMainTest() throws Exception
    {
        // !!! TO BE IMPLEMENTED

        System.err.println("Main test to be implemented -- Stay tuned! Quitting");
    }
    
    private void runOnce(String clientId) throws Exception
    {
        try (CqlSession session = createSession()) {
            // Select the release_version from the system.local table:
            ResultSet rs = session.execute("select release_version from system.local");
            Row row = rs.one();
            //Print the results of the CQL query to the console:
            if (row != null) {
                System.out.printf("INFO [%s] Fetched row: %s.\n", clientId, row.getString("release_version"));
            } else {
                System.err.printf("ERROR [%s] Fetch failed!\n");
            }
        }
    }

    private CqlSession createSession() throws Exception
    {
        // Create the CqlSession object:
        return CqlSession.builder()
                .withCloudSecureConnectBundle(scbPath)
                .withAuthCredentials("clientId", clientSecret)
                .withKeyspace("test")
                .build();
    }

    public static void main(String[] args) throws Exception
    {
        if (args.length != 2) {
            System.err.println("Usage: java -jar ... [path-to-scb] [client-secret]");
            System.exit(1);
        }
        new AstraCqlStressTest(args[0], args[1]).runTest();
   }    
}
