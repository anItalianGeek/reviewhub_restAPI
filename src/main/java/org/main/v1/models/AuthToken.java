package org.main.v1.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "auth_token")
@Deprecated
public class AuthToken {

    @EmbeddedId
    private AuthTokenId id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @MapsId("userId")
    @JsonIgnore
    private Persona user;

    
    public AuthToken() {}

    public AuthToken(String token, Persona user) {
        this.user = user;
        this.id = new AuthTokenId();
        this.id.setTokenId(token);
        this.id.setUserId(user.getEmail());
        this.id.setExpiresAtId(LocalDateTime.now().plusMonths(1));
    }

    public Persona getUser() {
        return user;
    }

    public void setUser(Persona user) {
        this.user = user;
    }

    public AuthTokenId getId() {
        return id;
    }

    public void setId(AuthTokenId id) {
        this.id = id;
    }
}

