package com.rationaleemotions.server.docker;

import java.util.Arrays;
import java.util.List;

public class ContainerAttributes {
    private String imageName;
    private boolean isPrivileged;
    private List<DeviceInfo> deviceInfos;

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public boolean isPrivileged() {
        return isPrivileged;
    }

    public void setPrivileged(boolean privileged) {
        isPrivileged = privileged;
    }

    public List<DeviceInfo> getDeviceInfos() {
        return deviceInfos;
    }

    public void setDeviceInfos(DeviceInfo... deviceInfos) {
        this.deviceInfos = Arrays.asList(deviceInfos);
    }
}
