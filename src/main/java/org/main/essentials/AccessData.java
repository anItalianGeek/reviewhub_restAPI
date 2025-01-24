package org.main.essentials;

import org.main.models.UserIdentity;

public class AccessData {

    private String token;
    private UserIdentity ruolo;

    public AccessData(String token, UserIdentity ruolo) {
        this.ruolo = ruolo;
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserIdentity getRuolo() {
        return ruolo;
    }

    public void setRuolo(UserIdentity ruolo) {
        this.ruolo = ruolo;
    }

}