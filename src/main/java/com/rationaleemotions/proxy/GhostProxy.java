package com.rationaleemotions.proxy;

import com.google.common.collect.MapMaker;
import com.google.gson.JsonObject;
import com.rationaleemotions.config.ConfigReader;
import com.rationaleemotions.server.SpawnedServer;
import org.openqa.grid.common.RegistrationRequest;
import org.openqa.grid.common.SeleniumProtocol;
import org.openqa.grid.common.exception.GridException;
import org.openqa.grid.internal.Registry;
import org.openqa.grid.internal.TestSession;
import org.openqa.grid.internal.TestSlot;
import org.openqa.grid.selenium.proxy.DefaultRemoteProxy;
import org.openqa.grid.web.servlet.handler.RequestType;
import org.openqa.grid.web.servlet.handler.SeleniumBasedRequest;
import org.openqa.selenium.remote.DesiredCapabilities;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.openqa.grid.common.RegistrationRequest.*;
import static org.openqa.grid.web.servlet.handler.RequestType.STOP_SESSION;

/**
 * Represents a simple {@link DefaultRemoteProxy} implementation that relies on spinning off a server
 * and then routing the session traffic to the spawned server.
 */
public class GhostProxy extends DefaultRemoteProxy {
    private final AtomicInteger counter = new AtomicInteger(1);
    private interface Marker {
    }


    private static final Logger LOG = Logger.getLogger(Marker.class.getEnclosingClass().getName());
    private Map<String, SpawnedServer> servers = new MapMaker().initialCapacity(500).makeMap();

    private List<TestSlot> testSlots = new ArrayList<>();

    public GhostProxy(RegistrationRequest request, Registry registry) {
        super(request, registry);
        List<DesiredCapabilities> capabilities = request.getConfiguration().capabilities;

        List<TestSlot> slots = new ArrayList<>();
        for (DesiredCapabilities capability : capabilities) {
            Object maxInstance = capability.getCapability(MAX_INSTANCES);

            SeleniumProtocol protocol = getProtocol(capability);
            String path = getPath(capability);

            if (maxInstance == null) {
                LOG.warning("Max instance not specified. Using default = 1 instance");
                maxInstance = "1";
            }

            int value = Integer.parseInt(maxInstance.toString());
            for (int i = 0; i < value; i++) {
                Map<String, Object> c = new HashMap<>();
                for (String k : capability.asMap().keySet()) {
                    c.put(k, capability.getCapability(k));
                }
                slots.add(new ProxiedTestSlot(this, protocol, path, c));
            }
        }

        this.testSlots = Collections.unmodifiableList(slots);
        LOG.info("Maximum sessions supported : " + ConfigReader.getInstance().getMaxSession());
    }

    @Override
    public List<TestSlot> getTestSlots() {
        return this.testSlots;
    }

    @Override
    public TestSession getNewSession(Map<String, Object> requestedCapability) {

        if (counter.get() > ConfigReader.getInstance().getMaxSession()) {
            LOG.info("Waiting for remote nodes to be available");
            return null;
        }
        if (isDown()) {
            return null;
        }

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine(String.format("Trying to create a new session on node %s", this));
        }

        if (!hasCapability(requestedCapability)) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine(String.format("Node %s has no matching capability", this));
            }
            return null;
        }
        // any slot left at all?
        if (getTotalUsed() >= config.maxSession) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine(String.format("Node %s has no free slots", this));
            }
            return null;
        }
        // any slot left for the given app ?
        for (TestSlot testslot : getTestSlots()) {
            TestSession session = testslot.getNewSession(requestedCapability);

            if (session != null) {
                return session;
            }
        }
        return null;
    }

    @Override
    public void beforeSession(TestSession session) {
        startServerForTestSession(session);
    }

    @Override
    public void afterCommand(TestSession session, HttpServletRequest request, HttpServletResponse response) {
        super.afterCommand(session, request, response);
        RequestType type =
            SeleniumBasedRequest.createFromRequest(request, getRegistry()).extractRequestType();
        if (type == STOP_SESSION) {
            stopServerForTestSession(session);
            LOG.info("Counter value after decrementing : " + counter.decrementAndGet());
        }
    }

    @Override
    public boolean hasCapability(Map<String, Object> requestedCapability) {
        return true;
    }

    @Override
    public JsonObject getStatus() {
        return new JsonObject();
    }

    private String getPath(DesiredCapabilities capability) {
        String type = (String) capability.getCapability(PATH);
        if (type == null) {
            switch (getProtocol(capability)) {
                case Selenium:
                    return "/selenium-server/driver";
                case WebDriver:
                    return "/wd/hub";
                default:
                    throw new GridException("Protocol not supported.");
            }
        }
        return type;
    }

    private SeleniumProtocol getProtocol(DesiredCapabilities capability) {
        String type = (String) capability.getCapability(SELENIUM_PROTOCOL);

        SeleniumProtocol protocol;
        if (type == null) {
            protocol = SeleniumProtocol.WebDriver;
        } else {
            try {
                protocol = SeleniumProtocol.valueOf(type);
            } catch (IllegalArgumentException e) {
                throw new GridException(type + " isn't a valid protocol type for grid.", e);
            }
        }
        return protocol;
    }

    private void startServerForTestSession(TestSession session) {
        try {
            SpawnedServer server = SpawnedServer.spawnInstance();
            String key = "http://" + server.getHost() + ":" + server.getPort();
            URL url = new URL(key);
            servers.put(key, server);
            ((ProxiedTestSlot)session.getSlot()).setRemoteURL(url);
            LOG.info("Forwarding session to :" + session.getSlot().getRemoteURL());
            LOG.info ("Counter value after incrementing : " + counter.incrementAndGet());
        } catch (Exception e) {
            throw new GridException(e.getMessage(), e);
        }
    }

    private void stopServerForTestSession(TestSession session) {
        URL url = session.getSlot().getRemoteURL();
        String key = String.format("%s://%s:%d", url.getProtocol(), url.getHost(), url.getPort());
        LOG.info("Obtained Key is : " + key);
        SpawnedServer localServer = servers.get(key);
        if (localServer != null) {
            localServer.shutdown();
            servers.remove(key);
        }
    }

}
