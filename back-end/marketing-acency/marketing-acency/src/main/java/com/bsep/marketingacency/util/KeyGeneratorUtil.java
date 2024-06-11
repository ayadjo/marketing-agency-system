package com.bsep.marketingacency.util;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;

public class KeyGeneratorUtil {
    private static final String KEYSTORE_TYPE = "JCEKS";
    private static final String KEYSTORE_FILE = "myKeystore.jks";
    private static final String KEY_ALIAS = "myKey";
    private static final String KEYSTORE_PASSWORD = "marketing-agency";
    private static final String KEY_PASSWORD = "marketing-agency";

    public static void generateAndStoreKeyForClient(String alias) {
        try {
            KeyStore keystore = KeyStore.getInstance(KEYSTORE_TYPE);
            File keystoreFile = new File(KEYSTORE_FILE);

            if (keystoreFile.exists()) {
                // Učitavanje postojećeg keystore-a
                try (FileInputStream fis = new FileInputStream(keystoreFile)) {
                    keystore.load(fis, KEYSTORE_PASSWORD.toCharArray());
                    System.out.println("Keystore already exists, loaded successfully.");
                }
            } else {
                // Kreiranje novog keystore-a
                keystore.load(null, KEYSTORE_PASSWORD.toCharArray());
                System.out.println("Keystore does not exist, creating a new one.");
            }

            // Proveravamo da li ključ sa zadatim aliasom već postoji
            if (keystore.containsAlias(alias)) {
                System.out.println("Key with alias '" + alias + "' already exists in the keystore.");
            } else {
                // Generisanje AES ključa
                KeyGenerator keyGen = KeyGenerator.getInstance("AES");
                keyGen.init(256); // 256-bit ključ
                SecretKey secretKey = keyGen.generateKey();

                // Skladištenje ključa u keystore
                KeyStore.SecretKeyEntry keyEntry = new KeyStore.SecretKeyEntry(secretKey);
                KeyStore.ProtectionParameter entryPassword = new KeyStore.PasswordProtection(KEY_PASSWORD.toCharArray());
                keystore.setEntry(alias, keyEntry, entryPassword);

                // Skladištenje keystore-a u datoteku
                try (FileOutputStream fos = new FileOutputStream(KEYSTORE_FILE)) {
                    keystore.store(fos, KEYSTORE_PASSWORD.toCharArray());
                }

                System.out.println("Key for alias '" + alias + "' successfully generated and stored in keystore.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error generating or storing key for alias '" + alias + "': " + e.getMessage(), e);
        }
    }
}
