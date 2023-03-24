package com.datastax.stargate;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;

public class AstraCqlStressTest
{
    final Path scbPath;
    final String clientId, clientSecret;
    final String tableName;

    private AstraCqlStressTest(String scbPath,
            String clientId, String clientSecret,
            String tableName) {
        clientId = clientId.trim();
        if (clientId.isEmpty()) {
            System.err.println("Empty clientId.");
            System.exit(1);
        }
        this.clientId = clientId;
        clientSecret = clientSecret.trim();
        if (clientSecret.isEmpty()) {
            System.err.println("Empty clientSecret.");
            System.exit(1);
        }
        this.clientSecret = clientSecret;
        this.tableName = tableName;
        this.scbPath = Paths.get(scbPath);
        if (!this.scbPath.toFile().canRead()) {
            System.err.println("Path '"+scbPath+"' not readable.");
            System.exit(1);
        }
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
            String cql = "select * from "+tableName;
            ResultSet rs = session.execute(cql);
            Row row = rs.one();
            //Print the results of the CQL query to the console:
            if (row != null) {
                System.out.printf("INFO [%s] Fetched row from '%s': %d values.\n",
                        clientId, tableName, row.size());
            } else {
                System.err.printf("ERROR [%s] Fetch from '%s' failed (query '%s')\n",
                        clientId, tableName, cql);
            }
        }
    }

    private CqlSession createSession() throws Exception
    {
        // Create the CqlSession object:
        return CqlSession.builder()
                .withCloudSecureConnectBundle(scbPath)
                .withAuthCredentials(clientId, clientSecret)
                .withKeyspace("test")
                .build();
    }

    public static void main(String[] args) throws Exception
    {
        if (args.length != 4) {
            System.err.println("Usage: java -jar ... [path-to-scb] [client-id] [client-secret] [namespace.table]");
            System.exit(1);
        }
        new AstraCqlStressTest(args[0], args[1], args[2], args[3]).runTest();
   }    
}
