package org.main.models;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "auth_token")
public class AuthToken {

    @EmbeddedId
    private AuthTokenId id;

    @Column(name = "token")
    @MapsId("tokenId")
    private String token;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @MapsId("userId")
    private Persona user;

    @Column(name = "expires_at")
    @MapsId("expiresAtId")
    private LocalDateTime expiresAt;
    
    public AuthToken() {}

    public AuthToken(String token, Persona user) {
        this.token = token;
        this.user = user;
    }

    public Persona getUser() {
        return user;
    }

    public void setUser(Persona user) {
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    
}

