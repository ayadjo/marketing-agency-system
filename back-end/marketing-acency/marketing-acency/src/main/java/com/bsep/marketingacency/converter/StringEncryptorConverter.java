package com.bsep.marketingacency.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import com.bsep.marketingacency.util.KeystoreUtil;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Base64;

@Converter
@Component
public class StringEncryptorConverter implements AttributeConverter<String, String> {
    //podrazumevani algoritam kod jasypt biblioteke -> PBEWithMD5AndDES
    private final StringEncryptor textEncryptor;

    public StringEncryptorConverter() {
        this.textEncryptor = createStringEncryptor();
    }

    //za enkripciju vrednosti polja pre nego što se upiše u bazu podataka
    @Override
    public String convertToDatabaseColumn(String attribute) {
        return textEncryptor.encrypt(attribute);
    }

    //za dekripciju vrednosti polja kada se čita iz baze podataka
    @Override
    public String convertToEntityAttribute(String dbData) {
        return textEncryptor.decrypt(dbData);
    }
    public static StringEncryptor createStringEncryptor() {
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();

        try {
            String keystorePath = "C:\\Users\\38160\\Desktop\\Projekti 8.semestar\\bezbednost-projekat2\\bsep-ra-2024-kt2-tim-15\\back-end\\marketing-acency\\marketing-acency\\myKeystore.jks";
            String keystorePassword = "marketing-agency";
            String keyAlias = "myKey";
            String keyPassword = "marketing-agency";

            // Load the secret key from the keystore as byte array
            byte[] secretKeyBytes = KeystoreUtil.getSecretKey(keystorePath, keystorePassword, keyAlias, keyPassword).getBytes();
            // Convert the binary key to a Base64 string which is ASCII compatible
            String secretKeyBase64 = Base64.getEncoder().encodeToString(secretKeyBytes);

            encryptor.setPassword(secretKeyBase64);
            encryptor.setAlgorithm("PBEWithHmacSHA512AndAES_256");
            encryptor.setIvGenerator(new org.jasypt.iv.RandomIvGenerator());
            encryptor.setSaltGenerator(new org.jasypt.salt.RandomSaltGenerator());
            encryptor.setKeyObtentionIterations(1000);

        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException | UnrecoverableKeyException e) {
            e.printStackTrace();
            throw new RuntimeException("Error loading encryption key from keystore", e);
        }

        return encryptor;  //vracanje enkriptora i koriscenje za enkripciju i dekripciju
    }

    //Koristim Jasypt biblioteku za enkripciju podataka, u application.properties je postavljen
    // algoritam enkripcije na 'PBEWithHmacSHA512AndAES_256'
    //StringEncryptorConverter klasa se koristi za konvertovanje polja u bazi podataka
    //Metoda createStringEncryptor kreira StringEncryptor sa odgovarajucim podesavanjima za enkripciju
        //kljuc za enkripicju se dobija iz keystore fajla pomocu metode getSecretKey
    //Vrednosti se enkriptuju/dekriptuju pomocu StringEncryptor objekta
    //Klasa KeystoreUtil - za citanje kljuca iz keystore
    //Polje phoneNumber ce biti enkripotvano pre nego sto se sacuva u bazi, a dekriptovano kada se cita iz baze

}

