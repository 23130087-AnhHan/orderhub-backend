package com.orderhub.auth.dto;

import java.util.List;

public class AuthResponse {

    private String accessToken;
    private String tokenType = "Bearer";
    private UserInfo user;

    public AuthResponse() {
    }

    public AuthResponse(String accessToken, UserInfo user) {
        this.accessToken = accessToken;
        this.user = user;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public UserInfo getUser() {
        return user;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public void setUser(UserInfo user) {
        this.user = user;
    }

    public static class UserInfo {

        private Long id;
        private String fullName;
        private String email;
        private List<String> roles;

        public UserInfo() {
        }

        public UserInfo(Long id, String fullName, String email, List<String> roles) {
            this.id = id;
            this.fullName = fullName;
            this.email = email;
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

        public void setRoles(List<String> roles) {
            this.roles = roles;
        }
    }
}