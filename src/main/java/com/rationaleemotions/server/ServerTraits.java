package com.rationaleemotions.server;

/**
 * Represents the capabilities of a Server [ can be remote or local ]
 */
public interface ServerTraits {
    /**
     * Helps start a selenium server.
     *
     * @return - The port on which the server was spun off.
     * @throws ServerException - In case of problems.
     */
    int startServer() throws ServerException;

    /**
     * @return - The port on which the server was spun off.
     */
    int getPort();

    /**
     * @return - <code>true</code> if the server is running.
     */
    boolean isServerRunning();

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

        ServerException(String message, Throwable e) {
            super(message, e);
        }

    }
}
