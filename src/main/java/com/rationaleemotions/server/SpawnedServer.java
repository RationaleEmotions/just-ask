package com.rationaleemotions.server;

import org.openqa.grid.common.exception.GridException;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class SpawnedServer {
    private interface Marker {
    }


    private static final String JVM_ARG = "server.impl";
    private static final String SERVER_IMPL = System.getProperty(JVM_ARG, JvmBasedSeleniumServer.class
        .getCanonicalName());
    private static final Logger LOG = Logger.getLogger(Marker.class.getEnclosingClass().getName());

    private ServerTraits server;

    private SpawnedServer() {
        //We have a factory method. Hiding the constructor.
    }

    public static SpawnedServer spawnInstance() {

        SpawnedServer server = new SpawnedServer();
        try {
            server.server = newInstance();
            int port = server.server.startServer();
            do {
                TimeUnit.SECONDS.sleep(15);
            } while (! server.server.isServerRunning());
            LOG.info("***Server started on [" + port + "]****");
            return server;
        } catch (Exception e) {
            throw new GridException(e.getMessage(), e);
        }
    }

    public int getPort() {
        return server.getPort();
    }

    private static ServerTraits newInstance()
        throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        return (ServerTraits) getServerClass().newInstance();
    }

    private static Class<?> getServerClass() throws ClassNotFoundException {
        Class<?> clazz = Class.forName(SERVER_IMPL);
        if (ServerTraits.class.isAssignableFrom(clazz)) {
            return clazz;
        }
        throw new IllegalStateException(SERVER_IMPL + " does not implement " + ServerTraits.class.getCanonicalName());
    }

    public void shutdown() {
        try {
            server.shutdownServer();
        } catch (Exception e) {
            LOG.warning(e.getMessage());
        }
    }

}
