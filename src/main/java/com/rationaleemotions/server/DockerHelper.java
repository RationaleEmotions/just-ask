package com.rationaleemotions.server;

import com.google.common.base.Preconditions;
import com.rationaleemotions.config.ConfigReader;
import com.rationaleemotions.config.MappingInfo;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.LoggingBuildHandler;
import com.spotify.docker.client.ProgressHandler;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.*;
import org.openqa.selenium.net.PortProber;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.rationaleemotions.config.ConfigReader.getInstance;

/**
 * A Helper class that facilitates interaction with a Docker Daemon.
 */
class DockerHelper {
    private interface Marker {
    }


    private static final Logger LOG = Logger.getLogger(Marker.class.getEnclosingClass().getName());

    private DockerHelper() {
        DockerClient client = getClient();
        Runtime.getRuntime().addShutdownHook(new Thread(new DockerCleanup(client)));
    }

    /**
     * @param id - The ID of the container that is to be cleaned up.
     * @throws DockerException      - In case of any issues.
     * @throws InterruptedException - In case of any issues.
     */
    static void killContainer(String id) throws DockerException, InterruptedException {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Killing the container : [" + id + "].");
        }
        getClient().killContainer(id);
    }

    /**
     * @param image - The name of the image for which a docker container is to be spun off. For e.g., you could
     *              specify the image name as <code>selenium/standalone-chrome:3.0.1</code> to download the
     *              <code>standalone-chrome</code> image with its tag as <code>3.0.1</code>
     * @return - A {@link ContainerInfo} object that represents the newly spun off container.
     * @throws DockerException      - In case of any issues.
     * @throws InterruptedException - In case of any issues.
     */
    static ContainerInfo startContainerFor(String image) throws DockerException, InterruptedException {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Commencing starting of container for the image [" + image + "].");
        }
        Preconditions.checkState("ok".equalsIgnoreCase(getClient().ping()),
            "Ensuring that the Docker Daemon is up and running.");
        DockerHelper.predownloadImagesIfRequired();

        final Map<String, List<PortBinding>> portBindings = new HashMap<>();

        List<PortBinding> randomPort = new ArrayList<>();
        int port = PortProber.findFreePort();
        String localHost = ConfigReader.getInstance().getLocalhost();
        PortBinding binding = PortBinding.create(localHost, Integer.toString(port));

        randomPort.add(binding);
        portBindings.put(ConfigReader.getInstance().getDockerImagePort(), randomPort);

        final HostConfig hostConfig = HostConfig.builder().portBindings(portBindings).build();

        final ContainerConfig containerConfig = ContainerConfig.builder()
            .hostConfig(hostConfig)
            .image(image).exposedPorts(ConfigReader.getInstance().getDockerImagePort())
            .build();

        final ContainerCreation creation = getClient().createContainer(containerConfig);

        final String id = creation.id();

        // Inspect container
        final com.spotify.docker.client.messages.ContainerInfo containerInfo = getClient().inspectContainer(id);
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine(String.format("Container Information %s", containerInfo));
            String msg = "Checking to see if the container with id [" + id + "] and name [" +
                containerInfo.name() + "]...";
            LOG.fine(msg);
        }

        if (! containerInfo.state().running()) {
            // Start container
            getClient().startContainer(id);
            if (LOG.isLoggable(Level.FINE)) {
                LOG.info(containerInfo.name() + " is now running.");
            }
        } else {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.info(containerInfo.name() + " was already running.");
            }
        }
        ContainerInfo info = new ContainerInfo(id, port);
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("******" + info + "******");
        }
        return info;
    }

    private static void predownloadImagesIfRequired() throws DockerException, InterruptedException {

        DockerClient client = getClient();
        LOG.warning("Commencing download of images.");
        Collection<MappingInfo> images = getInstance().getMapping().values();

        ProgressHandler handler = new LoggingBuildHandler();
        for (MappingInfo image : images) {
            List<Image> foundImages = client.listImages(DockerClient.ListImagesParam.byName(image.getTarget()));
            if (! foundImages.isEmpty()) {
                LOG.warning(String.format("Skipping download for Image [%s] because it's already available.",
                    image.getTarget()));
                continue;
            }
            client.pull(image.getTarget(), handler);
        }
    }

    private static DockerClient getClient() {
        return DefaultDockerClient.builder().uri(getInstance().getDockerUrl()).build();
    }

    /**
     * A Simple POJO that represents the newly spun off container, encapsulating the container Id and the port on which
     * the container is running.
     */
    static class ContainerInfo {
        private int port;
        private String containerId;

        ContainerInfo(String containerId, int port) {
            this.port = port;
            this.containerId = containerId;
        }

        int getPort() {
            return port;
        }

        String getContainerId() {
            return containerId;
        }

        @Override
        public String toString() {
            return String.format("%s running on %d", containerId, port);
        }
    }


    private static class DockerCleanup implements Runnable {
        private DockerClient client;

        DockerCleanup(DockerClient client) {
            this.client = client;
        }

        @Override
        public void run() {
            if (client != null) {
                client.close();
            }
        }
    }

}
