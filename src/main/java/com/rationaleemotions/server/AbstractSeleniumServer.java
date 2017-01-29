package com.rationaleemotions.server;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.openqa.grid.common.exception.GridConfigurationException;

import java.io.IOException;

/**
 *
 */
public abstract class AbstractSeleniumServer {
    /**
     * @return - <code>true</code> if the server is running.
     */
    public boolean isServerRunning() {
        String url = String.format("http://%s:%d/wd/hub/status", getHost(), getPort());
        HttpGet host = new HttpGet(url);
        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            HttpResponse response = client.execute(host);
            return response.getStatusLine().getStatusCode() == HttpStatus.SC_OK;
        } catch (IOException | GridConfigurationException e) {
            //Gobble Exception
        }
        return false;
    }

    /**
     * Helps start a selenium server.
     *
     * @return - The port on which the server was spun off.
     * @throws ServerException - In case of problems.
     */
    public abstract int startServer() throws ServerException;

    /**
     * @return - The port on which the server was spun off.
     */
    public abstract int getPort();

    public abstract String getHost();

    /**
     * Shutsdown the server.
     *
     * @throws ServerException - In case of problems.
     */
    public abstract void shutdownServer() throws ServerException;

    /**
     * Represents all exceptions that can arise out of attempts to manipulate server.
     */
    public static class ServerException extends Exception {
        public ServerException(String message, Throwable e) {
            super(message, e);
        }
    }
}
