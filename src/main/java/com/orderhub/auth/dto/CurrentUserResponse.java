package com.orderhub.auth.dto;

import java.util.List;

public class CurrentUserResponse {

    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String status;
    private List<String> roles;

    public CurrentUserResponse() {
    }

    public CurrentUserResponse(Long id, String fullName, String email, String phone, String status, List<String> roles) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.status = status;
        this.roles = roles;
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getStatus() {
        return status;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}