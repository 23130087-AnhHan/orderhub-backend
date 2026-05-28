package com.orderhub.common.response;

public class ErrorDetail {

    private String field;
    private String message;

    public ErrorDetail() {
    }

    public ErrorDetail(String field, String message) {
        this.field = field;
        this.message = message;
    }

    public static ErrorDetail of(String field, String message) {
        return new ErrorDetail(field, message);
    }

    public String getField() {
        return field;
    }

    public String getMessage() {
        return message;
    }

    public void setField(String field) {
        this.field = field;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}