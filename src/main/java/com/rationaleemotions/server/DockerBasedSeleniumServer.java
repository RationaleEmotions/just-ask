package com.rationaleemotions.server;

import com.rationaleemotions.config.ConfigReader;
import com.rationaleemotions.server.docker.DeviceInfo;
import com.spotify.docker.client.UnixConnectionSocketFactory;
import com.spotify.docker.client.exceptions.DockerException;
import org.openqa.grid.internal.TestSession;
import org.openqa.selenium.remote.CapabilityType;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a {@link ISeleniumServer} implementation that is backed by a docker container which executes the
 * selenium server within a docker container.
 *
 */
public class DockerBasedSeleniumServer implements ISeleniumServer {
    protected DockerHelper.ContainerInfo containerInfo;

    @Override
    public int startServer(TestSession session) throws ServerException {
        try {
            String browser = (String) session.getRequestedCapabilities().get(CapabilityType.BROWSER_NAME);
            String image = ConfigReader.getInstance().getMapping().get(browser).getTarget();
            containerInfo = DockerHelper.startContainerFor(image, isPrivileged(), getDeviceInfos());
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
    	URI uri=ConfigReader.getInstance().getDockerRestApiUri();
		if (uri.getScheme().equals(DockerHelper.UNIX_SCHEME)) {
			return UnixConnectionSocketFactory.sanitizeUri(uri).getHost();
		} else {
			return uri.getHost();
		}
	}

    @Override
    public void shutdownServer() throws ServerException {
        try {
            DockerHelper.killAndRemoveContainer(containerInfo.getContainerId());
        } catch (DockerException | InterruptedException e) {
            throw new ServerException(e.getMessage(), e);
        }
    }

    public boolean isPrivileged() {
        return false;
    }
    public List<DeviceInfo> getDeviceInfos() {
        return new ArrayList<>();
    }

}
