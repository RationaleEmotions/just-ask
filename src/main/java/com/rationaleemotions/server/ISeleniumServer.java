package com.rationaleemotions.server;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.openqa.grid.common.exception.GridConfigurationException;

import java.io.IOException;
import java.util.Map;

/**
 * Represents the capabilities that a typical selenium server should possess.
 */
public interface ISeleniumServer {
    /**
     * @return - <code>true</code> if the server is running.
     */
    default boolean isServerRunning() {
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
     * @param requestedCapabilities - the capabilities the client requested.
     * @return - The port on which the server was spun off.
     * @throws ServerException - In case of problems.
     */
    int startServer(Map<String, Object> requestedCapabilities) throws ServerException;

    /**
     * @return - The port on which the server was spun off.
     */
    int getPort();

    /**
     *
     * @return - The host on which the server is running.
     */
    String getHost();

    /**
     * Shutsdown the server.
     *
     * @throws ServerException - In case of problems.
     */
    void shutdownServer() throws ServerException;

    /**
     * Represents all exceptions that can arise out of attempts to manipulate server.
     */
    class ServerException extends Exception {
        public ServerException(String message, Throwable e) {
            super(message, e);
        }
    }
}
