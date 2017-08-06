package com.rationaleemotions.server;

import com.rationaleemotions.config.ConfigReader;
import org.openqa.grid.internal.TestSession;
import org.openqa.selenium.remote.CapabilityType;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SpawnedServer {
    private interface Marker {
    }

    private static final Logger LOG = Logger.getLogger(Marker.class.getEnclosingClass().getName());

    private ISeleniumServer server;

    private SpawnedServer() {
        //We have a factory method. Hiding the constructor.
    }

    public static SpawnedServer spawnInstance(TestSession session) throws Exception {
        SpawnedServer server = new SpawnedServer();
        AtomicInteger attempts = new AtomicInteger(0);
        String browser = (String) session.getRequestedCapabilities().get(CapabilityType.BROWSER_NAME);
        server.server = newInstance(browser);
        int port = server.server.startServer(session);

        do {
            TimeUnit.SECONDS.sleep(2);
        } while (!server.server.isServerRunning() && attempts.incrementAndGet() <= 5);
        if (LOG.isLoggable(Level.WARNING)) {
            LOG.warning(String.format("***Server started on [%d]****", port));
        }
        return server;
    }

    public String getHost() {
        return server.getHost();
    }

    public int getPort() {
        return server.getPort();
    }

    private static ISeleniumServer newInstance(String browser)
        throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        return (ISeleniumServer) getServerClass(browser).newInstance();
    }

    private static Class<?> getServerClass(String browser) throws ClassNotFoundException {
        String serverImpl = ConfigReader.getInstance().getMapping().get(browser).getImplementation();
        Class<?> clazz = Class.forName(serverImpl);
        LOG.info("Working with the implementation : [" + clazz.getCanonicalName() + "].");
        if (ISeleniumServer.class.isAssignableFrom(clazz)) {
            return clazz;
        }
        throw new IllegalStateException(serverImpl + " does not extend " + ISeleniumServer.class
            .getCanonicalName());
    }

    public void shutdown() {
        try {
            server.shutdownServer();
            LOG.warning("***Server running on [" + getPort() + "] has been stopped****");
        } catch (Exception e) {
            LOG.warning(e.getMessage());
        }
    }

    @Override
    public String toString() {
        return "SpawnedServer[" + getHost() + ":" + getPort() + "]";
    }
}
