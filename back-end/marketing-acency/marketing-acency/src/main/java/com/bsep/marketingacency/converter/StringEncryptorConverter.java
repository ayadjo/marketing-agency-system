package com.bsep.marketingacency.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;

@Converter
public class StringEncryptorConverter implements AttributeConverter<String, String> {
    //podrazumevani algoritam kod jasypt biblioteke -> PBEWithMD5AndDES
    private static final String SECRET_KEY = "mySecretKey"; // Promenite ovo sa vašim tajnim ključem

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) {
            return null;
        }
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword(SECRET_KEY);
        return encryptor.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword(SECRET_KEY);
        return encryptor.decrypt(dbData);
    }
}

