package com.rationaleemotions.config;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A singleton instance that works as a configuration data source.
 */
public class ConfigReader {
    private static final String JVM_ARG = "config.file";
    private static final String CONFIG = System.getProperty(JVM_ARG, "just-ask.json");
    private Configuration configuration;


    private interface Marker {
    }


    private static final Logger LOG = Logger.getLogger(Marker.class.getEnclosingClass().getName());

    /**
     * @return - A {@link ConfigReader} that represents the configuration.
     */
    public static ConfigReader getInstance() {
        return ReaderInstance.instance;
    }

    /**
     * @return - The Docker Daemon URL
     */
    public String getDockerUrl() {
        if (configuration == null) {
            return null;
        }
        return String.format("http://%s:%s", configuration.getDockerHost(), configuration.getDockerPort());
    }

    /**
     * @return - The docker host.
     */
    public String getDockerHost() {
        if (configuration == null) {
            return null;
        }
        return configuration.getDockerHost();
    }

    /**
     * @return - The IP address of the machine wherein the docker daemon is running. It is typically left as
     * <code>0.0.0.0</code>
     */
    public String getLocalhost() {
        if (configuration == null) {
            return null;
        }
        return configuration.getLocalhost();
    }

    /**
     * @return - The port number that is exposed in the docker image to which traffic is to be routed to.
     */
    public String getDockerImagePort() {
        if (configuration == null) {
            return null;
        }
        return configuration.getDockerImagePort();
    }

    /**
     * @return - The browser to target (target could for e.g., be docker image) mapping.
     */
    public Map<String, String> getMapping() {
        Map<String, String> mapping = new HashMap<>();
        for (MappingInfo each : configuration.getMapping()) {
            mapping.put(each.getBrowser(), each.getTarget());
        }
        return mapping;
    }

    /**
     * @return - How many number of sessions are to be honoured at any given point in time.
     * This kind of resembles the threshold value after which new session requests would be put into the
     * Hub's wait queue.
     */
    public int getMaxSession() {
        if (configuration == null) {
            return 10;
        }
        return configuration.getMaxSession();
    }

    private static class ReaderInstance {
        static final ConfigReader instance = new ConfigReader();

        static {
            init();
        }

        private static void init() {
            InputStream stream = getStream();
            Preconditions.checkState(stream != null, "Unable to load configuration file.");
            instance.configuration = new Gson().fromJson(new JsonReader(new InputStreamReader(stream)), Configuration
                .class);
            LOG.info(String.format("Working with the Configuration : %s", instance.configuration));
        }
    }

    private static InputStream getStream() {
        try {
            LOG.fine(String.format("Attempting to read %s as resource.", CONFIG));
            InputStream stream =  Thread.currentThread().getContextClassLoader().getResourceAsStream(CONFIG);
            if (stream == null) {
                LOG.fine(String.format("Re-attempting to read %s as a local file.", CONFIG));
                return new FileInputStream(new File(CONFIG));
            }
        } catch (Exception e) {
                //Gobble exception
        }
        return null;
    }


    private static class Configuration {
        private String dockerHost;
        private String localhost;
        private String dockerPort;
        private String dockerImagePort;
        private int maxSession;
        private List<MappingInfo> mapping;

        public String getDockerPort() {
            return dockerPort;
        }

        public String getDockerHost() {
            return dockerHost;
        }

        public String getLocalhost() {
            return localhost;
        }

        public String getDockerImagePort() {
            return dockerImagePort;
        }

        public int getMaxSession() {
            return maxSession;
        }

        public List<MappingInfo> getMapping() {
            return mapping;
        }

        @Override
        public String toString() {
            return "Configuration{" +
                "dockerHost='" + dockerHost + '\'' +
                ", localhost='" + localhost + '\'' +
                ", dockerPort='" + dockerPort + '\'' +
                ", dockerImagePort='" + dockerImagePort + '\'' +
                ", maxSession=" + maxSession +
                ", mapping=" + mapping +
                '}';
        }
    }


    private static class MappingInfo {
        private String browser;
        private String target;

        String getBrowser() {
            return browser;
        }

        String getTarget() {
            return target;
        }

        @Override
        public String toString() {
            return String.format("MappingInfo{browser='%s',target='%s'}", browser, target);
        }
    }
}
