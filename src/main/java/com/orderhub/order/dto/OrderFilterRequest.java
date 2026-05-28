package com.orderhub.order.dto;

public class OrderFilterRequest {

    private int page = 0;
    private int size = 10;
    private String status;

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public String getStatus() {
        return status;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}