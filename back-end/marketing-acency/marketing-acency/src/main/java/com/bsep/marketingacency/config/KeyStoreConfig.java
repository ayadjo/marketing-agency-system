package com.bsep.marketingacency.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.security.KeyStore;

@Configuration
public class KeyStoreConfig {
    private static final String KEY_STORE_FILE = "myKeystore.jks";
    private static final String KEY_STORE_PATH = "C:\\Users\\38160\\Desktop\\Projekti 8.semestar\\bezbednost-projekat2\\bsep-ra-2024-kt2-tim-15\\back-end\\marketing-acency\\marketing-acency\\myKeystore.jks";
    private static final String KEY_STORE_PASSWORD = "marketing-agency";

    @Bean
    public KeyStore keyStore() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("JKS");
        try (FileInputStream fis = new FileInputStream(KEY_STORE_PATH)) {
            keyStore.load(fis, KEY_STORE_PASSWORD.toCharArray());
        } catch (FileNotFoundException e) {
            keyStore = createKeyStore();
        }
        return keyStore;
    }

    private KeyStore createKeyStore() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(null, KEY_STORE_PASSWORD.toCharArray());
        try (FileOutputStream fos = new FileOutputStream(KEY_STORE_PATH)) {
            keyStore.store(fos, KEY_STORE_PASSWORD.toCharArray());
        }
        return keyStore;
    }
}
