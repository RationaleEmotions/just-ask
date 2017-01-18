package com.rationaleemotions.proxy;

import com.google.gson.JsonObject;
import org.openqa.grid.common.RegistrationRequest;
import org.openqa.grid.internal.Registry;
import org.openqa.grid.internal.RemoteProxy;
import org.openqa.grid.internal.TestSession;
import org.openqa.grid.internal.TestSlot;
import org.openqa.grid.internal.utils.CapabilityMatcher;
import org.openqa.grid.internal.utils.HtmlRenderer;
import org.openqa.grid.internal.utils.configuration.GridNodeConfiguration;
import org.openqa.selenium.remote.internal.HttpClientFactory;

import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Represents a simple wrapped {@link RemoteProxy} implementation.
 */
public class DecoratedProxy implements RemoteProxy {
    private RemoteProxy proxy;
    private URL remoteURL;

    DecoratedProxy(RemoteProxy proxy, URL remoteURL) {
        this.proxy = proxy;
        this.remoteURL = remoteURL;
    }

    @Override
    public List<TestSlot> getTestSlots() {
        return proxy.getTestSlots();
    }

    @Override
    public Registry getRegistry() {
        return proxy.getRegistry();
    }

    @Override
    public CapabilityMatcher getCapabilityHelper() {
        return proxy.getCapabilityHelper();
    }

    @Override
    public void setupTimeoutListener() {
        proxy.setupTimeoutListener();
    }

    @Override
    public String getId() {
        return proxy.getId();
    }

    @Override
    public void teardown() {
        proxy.teardown();
    }

    @Override
    public GridNodeConfiguration getConfig() {
        return proxy.getConfig();
    }

    @Override
    public RegistrationRequest getOriginalRegistrationRequest() {
        return proxy.getOriginalRegistrationRequest();
    }

    @Override
    public int getMaxNumberOfConcurrentTestSessions() {
        return proxy.getMaxNumberOfConcurrentTestSessions();
    }

    @Override
    public URL getRemoteHost() {
        return this.remoteURL;
    }

    @Override
    public TestSession getNewSession(Map<String, Object> requestedCapability) {
        return null;
    }

    @Override
    public int getTotalUsed() {
        return proxy.getTotalUsed();
    }

    @Override
    public HtmlRenderer getHtmlRender() {
        return proxy.getHtmlRender();
    }

    @Override
    public int getTimeOut() {
        return proxy.getTimeOut();
    }

    @Override
    public HttpClientFactory getHttpClientFactory() {
        return proxy.getHttpClientFactory();
    }

    @Override
    public JsonObject getStatus() {
        return proxy.getStatus();
    }

    @Override
    public boolean hasCapability(Map<String, Object> requestedCapability) {
        return proxy.hasCapability(requestedCapability);
    }

    @Override
    public boolean isBusy() {
        return proxy.isBusy();
    }

    @Override
    public float getResourceUsageInPercent() {
        return proxy.getResourceUsageInPercent();
    }

    @Override
    public long getLastSessionStart() {
        return proxy.getLastSessionStart();
    }

    @Override
    public int compareTo(RemoteProxy o) {
        return proxy.compareTo(o);
    }
}
