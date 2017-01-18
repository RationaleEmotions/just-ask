package com.rationaleemotions.proxy;

import com.rationaleemotions.server.SpawnedServer;
import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import org.openqa.grid.common.RegistrationRequest;
import org.openqa.grid.common.SeleniumProtocol;
import org.openqa.grid.common.exception.GridException;
import org.openqa.grid.internal.Registry;
import org.openqa.grid.internal.RemoteProxy;
import org.openqa.grid.internal.TestSession;
import org.openqa.grid.internal.TestSlot;
import org.openqa.grid.selenium.proxy.DefaultRemoteProxy;
import org.openqa.grid.web.servlet.handler.RequestType;
import org.openqa.grid.web.servlet.handler.SeleniumBasedRequest;
import org.openqa.selenium.remote.DesiredCapabilities;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.logging.Logger;

import static org.openqa.grid.common.RegistrationRequest.PATH;
import static org.openqa.grid.common.RegistrationRequest.SELENIUM_PROTOCOL;
import static org.openqa.grid.web.servlet.handler.RequestType.START_SESSION;
import static org.openqa.grid.web.servlet.handler.RequestType.STOP_SESSION;

/**
 * Represents a simple {@link DefaultRemoteProxy} implementation that relies on spinning off a server
 * and then routing the session traffic to the spawned server.
 */
public class OndemandServerProxy extends DefaultRemoteProxy {
    private interface Marker {
    }


    private static final Logger LOG = Logger.getLogger(Marker.class.getEnclosingClass().getName());
    private Map<URI, SpawnedServer> servers = Maps.newConcurrentMap();

    public OndemandServerProxy(RegistrationRequest request, Registry registry) {
        super(request, registry);
    }

    @Override
    public void beforeCommand(TestSession session, HttpServletRequest request, HttpServletResponse response) {
        RequestType type =
            SeleniumBasedRequest.createFromRequest(request, getRegistry()).extractRequestType();
        if (type == START_SESSION) {
            startServerForTestSession(session);
        }
        super.beforeCommand(session, request, response);
    }

    @Override
    public void afterCommand(TestSession session, HttpServletRequest request, HttpServletResponse response) {
        super.afterCommand(session, request, response);
        RequestType type =
            SeleniumBasedRequest.createFromRequest(request, getRegistry()).extractRequestType();
        if (type == STOP_SESSION) {
            stopServerForTestSession(session);
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
            URL url = new URL("http://localhost:" + server.getPort());
            servers.put(url.toURI(), server);
            modifySession(session, newSlot(url, session.getRequestedCapabilities()));
            LOG.info("Forwarding session to :" + session.getSlot().getRemoteURL());
        } catch (Exception e) {
            throw new GridException(e.getMessage(), e);
        }
    }

    private void stopServerForTestSession(TestSession session) {
        try {
            URI key = session.getSlot().getRemoteURL().toURI();
            SpawnedServer localServer = servers.get(key);
            if (localServer != null) {
                localServer.shutdown();
                servers.remove(key);
            }
        } catch (URISyntaxException e) {
            LOG.warning(e.getMessage());
        }
    }

    private TestSlot newSlot(URL url, Map<String, Object> requestedCapability) {
        DesiredCapabilities capabilities = new DesiredCapabilities(requestedCapability);
        RemoteProxy proxy = new DecoratedProxy(this, url);
        return new TestSlot(proxy, getProtocol(capabilities), getPath(capabilities), requestedCapability);
    }

    private void modifySession(TestSession session, TestSlot newSlot)
        throws NoSuchFieldException, IllegalAccessException {
        Field slot = session.getClass().getDeclaredField("slot");
        slot.setAccessible(true);
        slot.set(session, newSlot);
    }
}
