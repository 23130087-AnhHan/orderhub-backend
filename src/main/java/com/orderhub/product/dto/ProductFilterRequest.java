package com.orderhub.product.dto;

public class ProductFilterRequest {

    private int page = 0;
    private int size = 10;
    private String keyword;
    private Long categoryId;
    private String status = "ACTIVE";

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public String getKeyword() {
        return keyword;
    }

    public Long getCategoryId() {
        return categoryId;
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

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}