package org.main.models;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

// Nested embedded ID class
@Embeddable
public class AuthTokenId implements Serializable {

    private String tokenId;
    private Persona userId;
    private LocalDateTime expiresAtId;

    public AuthTokenId() {}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthTokenId that = (AuthTokenId) o;
        return Objects.equals(tokenId, that.tokenId) &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(expiresAtId, that.expiresAtId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tokenId, userId, expiresAtId);
    }
}
