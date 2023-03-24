package com.datastax.stargate;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;

public class AstraCqlStressTest
{
    final Path scbPath;
    final String clientId, clientSecret;
    final String tableName;
    final int threadCount;

    final AtomicInteger totalCalls = new AtomicInteger(0);
    final AtomicInteger totalFails = new AtomicInteger(0);

    final AtomicLong nextOkPrint = new AtomicLong(0);

    final ExecutorService executor;
    
    private AstraCqlStressTest(String scbPath,
            String clientId, String clientSecret,
            String tableName,
            String threadsStr) {
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
        int threads = -1;
        try {
            threads = Integer.parseInt(threadsStr);
        } catch (NumberFormatException e) { }
        if (threads <= 0) {
            System.err.println("Invalid thread count '"+threadsStr+"': need integer above 0");
            System.exit(1);
        }
        threadCount = threads;
        executor = Executors.newFixedThreadPool(threadCount);    
    }

    void runTest() throws Exception
    {
        // First: try running just once, as sanity check
        System.out.println("Starting tests: make one test request first...");
        try {
            runOnce("INIT", true);
        } catch (Exception e) {
            log("ERROR", "First test call failed, with: %s.", e.getMessage());
            log("ERROR", "Exiting.");
            System.exit(1);
        }

        log("INIT", "First test call succeeded, start ramp up.");
        runMainTest();
        log("MAIN", "Test complete!");
    }

    private void runMainTest() throws Exception
    {
        log("INIT", "ramp up threads (%d), 1 per second.", threadCount);

        for (int i = 0; i < threadCount; ++i) {
            String id = "Client-#%03d".formatted(i);
            log("INIT", "start client '%s'.", id);
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    runClient(id);
                }
            });
            Thread.sleep(1000L);
        }

        log("INIT", "all clients started, letting test just... run. CTRL-C to quit");

        while (true) {
            Thread.sleep(1000L);
            log("MAIN", "%d calls (%d fails)", totalCalls.get(), totalFails.get());
        }
        
    }

    private void runClient(String clientId) {
        int callCount = 0;
        while (true) {
            ++callCount;
            try {
                runOnce(clientId, (callCount == 1));
                // Add slight delay to cap throughput at 100/sec (even in local
                // setups; with latency more
                Thread.sleep(10L);
            } catch (InterruptedException e) {
                return;
            } catch (Exception e) {
                log("ERROR", "[%s] Call failed: %s",
                        clientId, e.getMessage());
            }
        }
    }
    
    private void runOnce(String clientId, boolean printOk) throws Exception
    {
        // Print ok call either if: (a) caller asks or, (b) it's been 100 msecs since last
        final long startTime = System.currentTimeMillis();

        totalCalls.incrementAndGet();
        try (CqlSession session = createSession()) {
            // Select the release_version from the system.local table:
            String cql = "select * from "+tableName;
            ResultSet rs = session.execute(cql);
            Row row = rs.one();
            final long now = System.currentTimeMillis();
            final long timeMsecs = now - startTime;
            if (row != null) {
                // Print the results of the CQL query to the console:
                if (printOk || beenAWhile(now)) {
                    log("INFO", "[%s] Fetched row from '%s' in %d msecs: %d values.",
                            clientId, tableName, timeMsecs, row.size());
                }
            } else {
                // How/why would this occur?
                totalFails.incrementAndGet();
                log("ERROR", "[%s] Fetch from '%s' failed in %d msecs (query '%s')",
                        clientId, tableName, timeMsecs, cql);
            }
        } catch (Exception e) {
            totalFails.incrementAndGet();
            throw e;
        }
    }

    private void log(String category, String tmpl, Object... args) {
        System.out.println(category+" "+tmpl.formatted(args));
    }
    
    // Let's only print call results up to ~5 / second
    private boolean beenAWhile(long now) {
        if (nextOkPrint.get() > now) {
            return false;
        }
        nextOkPrint.set(now + 200L);
        return true;
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
        if (args.length != 5) {
            System.err.println(
"Usage: java -jar ... [path-to-scb] [client-id] [client-secret] [namespace.table] [threadCount]");
            System.exit(1);
        }
        new AstraCqlStressTest(args[0], args[1], args[2], args[3], args[4]).runTest();
   }
}
