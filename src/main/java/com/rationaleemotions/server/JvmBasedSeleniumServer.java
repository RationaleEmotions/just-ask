package com.rationaleemotions.server;

import org.openqa.grid.selenium.GridLauncherV3;
import org.openqa.selenium.net.PortProber;

import java.io.File;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a {@link ISeleniumServer} implementation that is backed by a new JVM which executes the
 * selenium server as a separate process.
 */
class JvmBasedSeleniumServer implements ISeleniumServer {
    private static final Logger LOG = Logger.getLogger(Marker.class.getEnclosingClass().getName());
    private static final String JAVA = System.getProperty("java.home") + File.separator + "bin" + File.separator +
        "java";
    private static final String CP = "-cp";
    private static final String CLASSPATH = System.getProperty("java.class.path");
    private static final String PORT_ARG = "-port";
    private static final String MAIN_CLASS = GridLauncherV3.class.getCanonicalName();
    private Process process;
    private int port;

    private static String[] getArgs(int port) {
        return new String[] {
            JAVA,
            CP,
            CLASSPATH,
            MAIN_CLASS,
            PORT_ARG,
            Integer.toString(port)
        };
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public int startServer(Map<String, Object> requestedCapabilities) throws ServerException {
        port = PortProber.findFreePort();
        String[] args = getArgs(port);
        if (LOG.isLoggable(Level.INFO)) {
            LOG.info(String.format("Spawning a Selenium server using the arguments [%s]", Arrays.toString(args)));
        }
        ProcessBuilder pb = new ProcessBuilder(getArgs(port));
        try {
            this.process = pb.start();
            return port;
        } catch (Exception e) {
            throw new ServerException(e.getMessage(), e);
        }
    }

    @Override
    public void shutdownServer() {
        if (process != null) {
            process.destroyForcibly();
            LOG.warning("***Server shutdown****");
            process = null;
        }
    }

    @Override
    public String getHost() {
        return "localhost";
    }

    private interface Marker {
    }

}
