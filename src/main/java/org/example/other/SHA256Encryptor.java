package org.example.other;

import jdk.dynalink.beans.StaticClass;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class SHA256Encryptor {

    public static String encrypt(String input) {
        try {
            // create an instance of MessageDigest per SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // cypher string in bytes
            byte[] encodedHash = digest.digest(input.getBytes());

            // convert the result in hexadecimal format
            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedHash) {
                hexString.append(String.format("%02x", b));
            }

            // returned encrypted string
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }
    
}
