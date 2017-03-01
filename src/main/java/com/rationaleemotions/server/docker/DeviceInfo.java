package com.rationaleemotions.server.docker;

/**
 *
 */
public class DeviceInfo {
    private String pathOnContainer;
    private String pathOnHost;
    private String groupPermissions;

    public DeviceInfo(String pathOnHost) {
        this(pathOnHost, pathOnHost);
    }

    public DeviceInfo(String pathOnHost, String pathOnContainer) {
        this(pathOnHost, pathOnContainer, "crw");
    }

    public DeviceInfo(String pathOnHost, String pathOnContainer, String groupPermissions) {
        this.pathOnContainer = pathOnHost;
        this.pathOnHost = pathOnContainer;
        this.groupPermissions = groupPermissions;
    }

    public String getGroupPermissions() {
        return groupPermissions;
    }

    public String getPathOnContainer() {
        return pathOnContainer;
    }

    public String getPathOnHost() {
        return pathOnHost;
    }

    @Override
    public String toString() {
        return "DeviceInfo{" +
            "pathOnContainer='" + pathOnContainer + '\'' +
            ", pathOnHost='" + pathOnHost + '\'' +
            ", groupPermissions='" + groupPermissions + '\'' +
            '}';
    }
}
