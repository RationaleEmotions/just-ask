package com.rationaleemotions.proxy;

import org.openqa.grid.common.SeleniumProtocol;
import org.openqa.grid.common.exception.GridException;
import org.openqa.grid.internal.RemoteProxy;
import org.openqa.grid.internal.TestSlot;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * A proxied Test Slot that has the ability to delegate the calls to an actual proxy node.
 */
public class ProxiedTestSlot extends TestSlot {
    private URL remoteURL;
    public ProxiedTestSlot(RemoteProxy proxy, SeleniumProtocol protocol,
        String path, Map<String, Object> capabilities) {
        super(proxy, protocol, path, capabilities);
    }

    @Override
    public URL getRemoteURL() {
        String u = remoteURL + getPath();
        try {
            return new URL(u);
        } catch (MalformedURLException e) {
            throw new GridException("Configuration error for the node." + u + " isn't a valid URL");
        }
    }

    public void setRemoteURL(URL remoteURL) {
        this.remoteURL = remoteURL;
    }
}
