package com.luckmerlin.browser;

public class ClientMeta {
    private String mName;
    private String mHost;
    private String mIcon;

    public ClientMeta setHost(String host) {
        this.mHost = host;
        return this;
    }

    public ClientMeta setName(String name) {
        this.mName = name;
        return this;
    }

    public ClientMeta setIcon(String icon) {
        this.mIcon = icon;
        return this;
    }

    public String getHost() {
        return mHost;
    }

    public String getName() {
        return mName;
    }

    public String getIcon() {
        return mIcon;
    }
}
