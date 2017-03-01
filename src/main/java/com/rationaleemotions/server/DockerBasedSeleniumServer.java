package com.rationaleemotions.server;

import com.rationaleemotions.config.ConfigReader;
import com.spotify.docker.client.exceptions.DockerException;
import org.openqa.selenium.remote.CapabilityType;

import java.util.Map;

/**
 * Represents a {@link ISeleniumServer} implementation that is backed by a docker container which executes the
 * selenium server within a docker container.
 *
 */
public class DockerBasedSeleniumServer implements ISeleniumServer {
    private DockerHelper.ContainerInfo containerInfo;

    @Override
    public int startServer(Map<String, Object> requestedCapabilities) throws ServerException {
        try {
            String browser = (String) requestedCapabilities.get(CapabilityType.BROWSER_NAME);
            String image = ConfigReader.getInstance().getMapping().get(browser).getTarget();
            containerInfo = DockerHelper.startContainerFor(image);
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
