package org.main.other;

public final class ServerSignatureGenerator {
    
    public static String generateSignature() {
        String s = "";
        for (int i = 0; i < 64; i++)
            s += (char)(int)(Math.random() * (126 - 32 + 1)) + 32;
        return s;
    }
    
}
