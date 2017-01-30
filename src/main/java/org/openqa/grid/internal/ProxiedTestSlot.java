package org.openqa.grid.internal;

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
        boolean isRemoteURLSet = remoteURL != null;
        String u = remoteURL + getPath();
        try {
            return new URL(u);
        } catch (MalformedURLException e) {
            if (isRemoteURLSet) {
                throw new GridException("Configuration error for the node." + u + " isn't a valid URL");
            }
        }
        return null;
    }

    @Override
    boolean matches(Map<String, Object> desiredCapabilities) {
        return true;
    }

    public void setRemoteURL(URL remoteURL) {
        this.remoteURL = remoteURL;
    }
}
