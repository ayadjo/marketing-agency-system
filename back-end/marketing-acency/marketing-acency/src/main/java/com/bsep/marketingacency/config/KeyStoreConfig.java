package com.bsep.marketingacency.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.crypto.keygen.BytesKeyGenerator;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.security.crypto.keygen.StringKeyGenerator;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.SecretKey;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;

@Configuration
public class KeyStoreConfig {
    /*private static final String KEY_STORE_FILE = "C:\\Users\\38160\\Desktop\\Projekti 8.semestar\\bezbednost-projekat2\\bsep-ra-2024-kt2-tim-15\\back-end\\marketing-acency\\marketing-acency\\myKeystore.jks";
    private static final String KEY_STORE_PASSWORD = "marketing-agency";
    private static final String KEY_ALIAS = "myKey";
    private static final String KEY_PASSWORD = "marketing-agency";

    @Bean
    public BytesEncryptor bytesEncryptor() throws IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException, InvalidKeySpecException {
        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
        Resource resource = new ClassPathResource(KEY_STORE_FILE);
        keystore.load(new FileInputStream(resource.getFile()), KEY_STORE_PASSWORD.toCharArray());
        Key key = keystore.getKey(KEY_ALIAS, KEY_PASSWORD.toCharArray());

        // Generisanje tajnog ključa iz lozinke
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        PBEKeySpec spec = new PBEKeySpec(KEY_PASSWORD.toCharArray(), key.getEncoded(), 65536, 256);
        SecretKey secretKey = factory.generateSecret(spec);

        // Konvertujemo salt u CharSequence
        CharSequence salt = "your_salt_here"; // Neka bude vaš salt

        // Koristimo Encryptors.stronger() da bismo dobili BytesEncryptor
        return Encryptors.stronger(KEY_PASSWORD, salt);
    }


    @Bean
    public TextEncryptor textEncryptor() throws IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException, InvalidKeySpecException {
        return Encryptors.queryableText(KEY_PASSWORD, "your_salt_here");
    }

    @Bean
    public BytesKeyGenerator bytesKeyGenerator() {
        return KeyGenerators.secureRandom();
    }

    @Bean
    public StringKeyGenerator stringKeyGenerator() {
        return KeyGenerators.string();
    }*/
}
