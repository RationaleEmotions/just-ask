package com.rationaleemotions.server;

import com.rationaleemotions.config.ConfigReader;
import com.spotify.docker.client.exceptions.DockerException;

/**
 * Represents a {@link ISeleniumServer} implementation that is backed by a docker container which executes the
 * selenium server within a docker container.
 *
 */
class DockerBasedSeleniumServer implements ISeleniumServer {
    private DockerHelper.ContainerInfo containerInfo;
    private static final String SELENIUM_STANDALONE_CHROME = "selenium/standalone-chrome:3.0.1";

    @Override
    public int startServer() throws ServerException {
        try {
            containerInfo = DockerHelper.startContainerFor(SELENIUM_STANDALONE_CHROME);
            return containerInfo.getPort();
        } catch (DockerException | InterruptedException e) {
            throw new ServerException(e.getMessage(), e);
        }
    }

    @Override
    public int getPort() {
        return this.containerInfo.getPort();
    }

    @Override
    public String getHost() {
        return ConfigReader.getInstance().getDockerHost();
    }

    @Override
    public void shutdownServer() throws ServerException {
        try {
            DockerHelper.killContainer(containerInfo.getContainerId());
        } catch (DockerException | InterruptedException e) {
            throw new ServerException(e.getMessage(), e);
        }
    }
}
