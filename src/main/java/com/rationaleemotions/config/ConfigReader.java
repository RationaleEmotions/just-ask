package com.rationaleemotions.config;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * A singleton instance that works as a configuration data source.
 */
public class ConfigReader {
    private static final String JVM_ARG = "config.file";
    private static final String CONFIG = System.getProperty(JVM_ARG);
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
     * @return - The docker rest api uri.
     */
    public URI getDockerRestApiUri() {
        if (configuration == null) {
            return null;
        }
        return URI.create(configuration.getDockerRestApiUri().replaceAll("^unix:///", "unix://localhost/"));
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
     * @return - The environmental variables to pass to container
     */
    public Map<String, String> getEnvironment() {
        if (configuration == null) {
            return new HashMap<>();
        }        
        return configuration.getEnvironment();
    }

    /**
     * @return - The browser to target (target could for e.g., be docker image) mapping.
     */
    public Map<String, MappingInfo> getMapping() {
        Map<String, MappingInfo> mapping = new HashMap<>();
        for (MappingInfo each : configuration.getMapping()) {
            mapping.put(each.getBrowser(), each);
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
    
    /**
     * @return - The volume to mount to a container
     */
    public String getVolume() {
        if (configuration == null) {
            return "";
        }
        return configuration.getVolume();
    }

    private static class ReaderInstance {
        static final ConfigReader instance = new ConfigReader();

        static {
            init();
        }

        private ReaderInstance() {
            //Defeat instantiation.
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
        private String dockerRestApiUri;
        private String localhost;
        private String dockerImagePort;
        private int maxSession;
        private String volume;
        private List<MappingInfo> mapping;
        private Map<String, String> environment;

        public String getDockerRestApiUri() {
            return dockerRestApiUri;
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
        
        public String getVolume() {
            return volume;
        }

        public List<MappingInfo> getMapping() {
            return mapping;
        }
        
        public Map<String, String> getEnvironment() {	
            return environment;
        }

        @Override
        public String toString() {
            return "Configuration{" +
                "dockerRestApiUri='" + dockerRestApiUri + '\'' +
                ", localhost='" + localhost + '\'' +
                ", dockerImagePort='" + dockerImagePort + '\'' +
                ", maxSession=" + maxSession +
                ", volume=" + volume +
                ", environment=" + environment +
                ", mapping=" + mapping +
                '}';
        }
    }
}
