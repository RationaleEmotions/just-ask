package com.rationaleemotions.server;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.openqa.grid.common.exception.GridConfigurationException;
import org.openqa.grid.common.exception.GridException;
import org.openqa.grid.selenium.GridLauncherV3;
import org.openqa.selenium.net.PortProber;
import org.openqa.selenium.remote.internal.HttpClientFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Represents a {@link ServerTraits} implementation that is backed by a new JVM which executes the
 * selenium server as a separate process.
 */
class JvmBasedSeleniumServer implements ServerTraits {
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
    public int startServer() throws ServerException {
        port = PortProber.findFreePort();
        String[] args = getArgs(port);
        LOG.info("Spawning a Selenium server using the arguments [" + Arrays.toString(args) + "]");
        ProcessBuilder pb = new ProcessBuilder(getArgs(port));
        try {
            this.process = pb.start();
            do {
                TimeUnit.SECONDS.sleep(15);
            } while (! isServerRunning());
            LOG.warning("***Server started**** on [" + port + "]");
            return port;
        } catch (Exception e) {
            throw new ServerException(e.getMessage(), e);
        }
    }

    @Override
    public boolean isServerRunning() {
        String url = "http://localhost:" + port + "/wd/hub/status";
        HttpClientFactory httpClientFactory = new HttpClientFactory();
        try {

            HttpGet host = new HttpGet(url);
            HttpClient client = httpClientFactory.getHttpClient();
            HttpResponse response = client.execute(host);
            return response.getStatusLine().getStatusCode() == HttpStatus.SC_OK;
        } catch (IOException | GridConfigurationException e) {
            throw new GridException(e.getMessage(), e);
        } finally {
            httpClientFactory.close();
        }
    }

    @Override
    public void shutdownServer() {
        if (process != null) {
            process.destroyForcibly();
            LOG.warning("***Server shutdown****");
        }
    }

    private interface Marker {
    }

}
