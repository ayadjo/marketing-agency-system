package com.bsep.marketingacency.util;

import java.security.*;
import java.security.cert.CertificateException;
import java.io.FileInputStream;
import java.io.IOException;

public class KeystoreUtil {
    public static String getSecretKey(String keystorePath, String keystorePassword, String keyAlias, String keyPassword)
            throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException {

        KeyStore keystore = KeyStore.getInstance("PKCS12"); //PKCS12 format za keystore jer je uobičajen i podržava različite tipove ključeva
        try (FileInputStream fis = new FileInputStream(keystorePath)) {
            keystore.load(fis, keystorePassword.toCharArray());
        }

        PrivateKey key = (PrivateKey) keystore.getKey(keyAlias, keyPassword.toCharArray());
        if (key == null) {
            throw new KeyStoreException("No key found with alias: " + keyAlias);
        }

        return new String(key.getEncoded()); // Pretpostavimo da je ključ u plain text formatu
    }
}
