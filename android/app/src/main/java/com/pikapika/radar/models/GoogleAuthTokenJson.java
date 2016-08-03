package com.pikapika.radar.models;
public class GoogleAuthTokenJson {
    private String error;
    private String access_token;
    private String token_type;
    private int expires_in;
    private String refresh_token;
    private String id_token;

    private long init_time;

    public GoogleAuthTokenJson() {
    }

    public String getError() {
        return this.error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getAccess_token() {
        return this.access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public String getToken_type() {
        return this.token_type;
    }

    public void setToken_type(String token_type) {
        this.token_type = token_type;
    }

    public int getExpires_in() {
        return this.expires_in;
    }

    public void setExpires_in(int expires_in) {
        this.expires_in = expires_in;
    }

    public String getRefresh_token() {
        return this.refresh_token;
    }

    public void setRefresh_token(String refresh_token) {
        this.refresh_token = refresh_token;
    }

    public String getId_token() {
        return this.id_token;
    }

    public void setId_token(String id_token) {
        this.id_token = id_token;
    }

    public void setInit_time(long init_time) {
        this.init_time = init_time;
    }

    public long getInit_time() {
        return init_time;
    }
}