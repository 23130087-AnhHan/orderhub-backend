package com.orderhub.notification.dto;

public class NotificationFilterRequest {

    private int page = 0;
    private int size = 10;

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public void setSize(int size) {
        this.size = size;
    }
}