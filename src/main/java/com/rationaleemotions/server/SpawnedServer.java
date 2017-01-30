package com.rationaleemotions.server;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SpawnedServer {
    private interface Marker {
    }


    private static final String JVM_ARG = "server.impl";
    private static final String SERVER_IMPL = System.getProperty(JVM_ARG, DockerBasedSeleniumServer.class
        .getCanonicalName());
    private static final Logger LOG = Logger.getLogger(Marker.class.getEnclosingClass().getName());

    private ISeleniumServer server;

    private SpawnedServer() {
        //We have a factory method. Hiding the constructor.
    }

    public static SpawnedServer spawnInstance(Map<String, Object> requestedCapabilities) throws Exception {
        SpawnedServer server = new SpawnedServer();
        AtomicInteger attempts = new AtomicInteger(0);
        server.server = newInstance();
        int port = server.server.startServer(requestedCapabilities);

        do {
            TimeUnit.SECONDS.sleep(2);
        } while ((! server.server.isServerRunning()) || (attempts.incrementAndGet() <= 5));
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

    private static ISeleniumServer newInstance()
        throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        return (ISeleniumServer) getServerClass().newInstance();
    }

    private static Class<?> getServerClass() throws ClassNotFoundException {
        Class<?> clazz = Class.forName(SERVER_IMPL);
        LOG.info("Working with the implementation : [" + clazz.getCanonicalName() + "].");
        if (ISeleniumServer.class.isAssignableFrom(clazz)) {
            return clazz;
        }
        throw new IllegalStateException(SERVER_IMPL + " does not extend " + ISeleniumServer.class
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
