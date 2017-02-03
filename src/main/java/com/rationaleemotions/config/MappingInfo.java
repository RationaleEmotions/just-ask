package com.rationaleemotions.config;

/**
 * A simple POJO that represents the mapping
 */
public class MappingInfo {
    private String browser;
    private String target;
    private String implementation;

    public String getBrowser() {
        return browser;
    }

    public String getTarget() {
        return target;
    }

    public String getImplementation() {
        return implementation;
    }

    @Override
    public String toString() {
        return String.format("MappingInfo{browser='%s',target='%s', implementation='%s'}",
            browser, target,implementation);
    }
}
