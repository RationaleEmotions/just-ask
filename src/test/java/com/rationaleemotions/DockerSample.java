package com.rationaleemotions;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.LoggingBuildHandler;
import com.spotify.docker.client.messages.*;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.openqa.selenium.net.PortProber;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class DockerSample {
    private static final String CENTOS = "192.168.43.130";
    public static final String SELENIUM_STANDALONE_CHROME = "selenium/standalone-chrome:3.0.1";
    //    public static final String SELENIUM_STANDALONE_CHROME = "selenium/standalone-chrome:LATEST";


    private static void foo() throws Exception {
        boolean debug = Boolean.parseBoolean(System.getProperty("debug", "false"));
        String dockerHost = String.format("http://%s:%d", CENTOS, 2375);
        DockerClient docker = null;
        try {
            docker = DefaultDockerClient.builder()
                .uri(dockerHost).build();
            Info info = docker.info();
            System.err.println("Information : " + info);
            if (debug) {
                docker.pull(SELENIUM_STANDALONE_CHROME, new LoggingBuildHandler());
            } else {
                docker.pull(SELENIUM_STANDALONE_CHROME);
            }
            final ImageInfo imageInfo = docker.inspectImage(SELENIUM_STANDALONE_CHROME);
            System.err.println("Information : " + imageInfo);
            // Bind container ports to host ports
            //            final String[] ports = {Integer.toString(PortProber.findFreePort())};
            final String[] ports = {"4444"};
            final Map<String, List<PortBinding>> portBindings = new HashMap<>();
            for (String port : ports) {
                List<PortBinding> hostPorts = new ArrayList<>();
                hostPorts.add(PortBinding.of("0.0.0.0", PortProber.findFreePort()));
                portBindings.put(port, hostPorts);
            }
            //            // Bind container port 443 to an automatically allocated available host port.
            //            List<PortBinding> randomPort = new ArrayList<>();
            //            randomPort.add(PortBinding.randomPort("0.0.0.0"));
            //            portBindings.put("443", randomPort);

            System.err.println("Printing the port mappings : " + portBindings);

            final HostConfig hostConfig = HostConfig.builder().portBindings(portBindings).build();

            final ContainerConfig containerConfig = ContainerConfig.builder()
                .hostConfig(hostConfig)
                .image(SELENIUM_STANDALONE_CHROME).exposedPorts(ports)
                .build();

            final ContainerCreation creation = docker.createContainer(containerConfig);
            final String id = creation.id();

            // Inspect container
            final ContainerInfo containerInfo = docker.inspectContainer(id);
            System.err.println("Container Information " + containerInfo);
            String msg = "Checking to see if the container with id [" + id + "] and name [" +
                containerInfo.name() + "]...";
            System.err.println(msg);
            if (! containerInfo.state().running()) {
                // Start container
                docker.startContainer(id);
                System.err.println(containerInfo.name() + " is now running.");
            } else {
                System.err.println(containerInfo.name() + " was already running.");
            }

            System.err.println("Lets wait here !!!");
        } finally {
            if (docker != null) {
                docker.close();
            }
        }
    }

    private static void checkServer(int port) throws IOException, URISyntaxException {
        CloseableHttpClient client = HttpClientBuilder.create().build();
        String url = String.format("http://localhost:%d/wd/hub/status", port);
        HttpGet httpGet = new HttpGet(new URL(url).toURI());
        HttpResponse response = client.execute(httpGet);
        InputStream content = response.getEntity().getContent();
        String line;
        BufferedReader reader = new BufferedReader(new InputStreamReader(content));
        System.err.println("****Printing the response***");
        while ((line = reader.readLine()) != null) {
            System.err.println(line);
        }
        client.close();
    }


    //    static void foo() {
    //        String dockerHost = String.format("tcp://%s:%d", CENTOS, 2375);
    //
    //        DefaultDockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
    //            .withRegistryUrl("https://hub.docker.com/")
    //            .withDockerHost(dockerHost).build();
    //
    //        DockerClient dockerClient = DockerClientBuilder.getInstance(config)
    //            //            .withDockerCmdExecFactory(dockerCmdExecFactory)
    //            .build();
    //        List<Image> images = dockerClient.listImagesCmd().exec();
    //        System.err.println("***Printing the images***");
    //        for (Image image : images) {
    //            System.err.println("Image :" + image);
    //        }
    //        Info info = dockerClient.infoCmd().exec();
    //        System.err.println("**Info " + info);
    //        dockerClient.pullImageCmd("https://hub.docker.com")
    //        ExposedPort tcp22 = ExposedPort.tcp(4444);
    //        Ports portBindings = new Ports();
    //        portBindings.bind(tcp22, Ports.Binding.bindPort(4444));
    //        CreateContainerResponse container = dockerClient.createContainerCmd("selenium/standalone-chrome")
    //
    //            .withCmd("true")
    //            .withExposedPorts(tcp22)
    //            .withPortBindings(portBindings)
    //            .withImage("selenium/standalone-chrome:3.0.1")
    //            .exec();
    //
    //        dockerClient.startContainerCmd(container.getId()).exec();
    //    }
}
