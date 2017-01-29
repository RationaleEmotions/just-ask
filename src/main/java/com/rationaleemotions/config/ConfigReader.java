package com.rationaleemotions.config;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class ConfigReader {
    private static final String CONFIG = "just-ask.json";
    private Configuration configuration;


    private interface Marker {
    }


    private static final Logger LOG = Logger.getLogger(Marker.class.getEnclosingClass().getName());


    public static ConfigReader getInstance() {
        return ReaderInstance.instance;
    }

    public String getDockerUrl() {
        if (configuration == null) {
            return null;
        }
        return String.format("http://%s:%s", configuration.getDockerHost(), configuration.getDockerPort());
    }

    public List<String> getImagesToDownload() {
        if (configuration == null) {
            return null;
        }
        return configuration.getImages();
    }

    public String getDockerHost() {
        if (configuration == null) {
            return null;
        }
        return configuration.getDockerHost();
    }

    public String getLocalhost() {
        if (configuration == null) {
            return null;
        }
        return configuration.getLocalhost();
    }

    public String getDockerImagePort() {
        if (configuration == null) {
            return null;
        }
        return configuration.getDockerImagePort();
    }

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
            InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(CONFIG);
            Preconditions.checkState(stream != null, "Unable to load configuration file.");
            instance.configuration = new Gson().fromJson(new JsonReader(new InputStreamReader(stream)), Configuration
                .class);
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Successfully initialized configuration [" + instance.configuration + "]");
            }
        }
    }


    private static class Configuration {
        private String dockerHost;
        private String localhost;
        private String dockerPort;
        private List<String> images;
        private String dockerImagePort;
        private int maxSession;

        public List<String> getImages() {
            return images;
        }

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

        @Override
        public String toString() {
            return "Configuration{" +
                "dockerHost='" + dockerHost + '\'' +
                ", localhost='" + localhost + '\'' +
                ", dockerPort='" + dockerPort + '\'' +
                ", images=" + images +
                ", dockerImagePort='" + dockerImagePort + '\'' +
                ", maxSession=" + maxSession +
                '}';
        }
    }
}
