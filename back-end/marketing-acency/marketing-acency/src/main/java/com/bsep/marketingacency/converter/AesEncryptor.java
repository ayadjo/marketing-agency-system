package com.bsep.marketingacency.converter;

import com.bsep.marketingacency.util.ThreadLocalClientId;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.util.Base64;

@Converter
public class AesEncryptor implements AttributeConverter<String, String> {
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String KEYSTORE_TYPE = "JCEKS";
    private static final String KEYSTORE_FILE = "myKeystore.jks";
    private static final String KEYSTORE_PASSWORD = "marketing-agency";
    private static final String KEY_PASSWORD = "marketing-agency";
    private static final String IV = "RandomInitVector"; // IV bi trebao da bude dinamički, ali za jednostavnost, koristićemo fiksnu vrednost (16 bajtova)

    private static final String CLIENT_ALIAS_PREFIX = "clientKey-";

    @Override
    public String convertToDatabaseColumn(String attribute) {
        try {
            if (attribute == null) {
                return null;
            }

            Cipher cipher = Cipher.getInstance(ALGORITHM, "SunJCE");
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(), new IvParameterSpec(IV.getBytes("UTF-8")));
            byte[] encryptedBytes = cipher.doFinal(attribute.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(encryptedBytes);

        } catch (Exception e) {
            throw new RuntimeException("Error encrypting data: " + e.getMessage(), e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        try {
            if (dbData == null) {
                return null;
            }

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), new IvParameterSpec(IV.getBytes("UTF-8")));
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(dbData));
            return new String(decryptedBytes, "UTF-8");

        } catch (Exception e) {
            throw new RuntimeException("Error decrypting data: " + e.getMessage(), e);
        }
    }

    private SecretKey getSecretKey() throws Exception {
        KeyStore keystore = KeyStore.getInstance(KEYSTORE_TYPE);
        keystore.load(getClass().getClassLoader().getResourceAsStream(KEYSTORE_FILE), KEYSTORE_PASSWORD.toCharArray());
        String clientAlias = CLIENT_ALIAS_PREFIX + ThreadLocalClientId.get(); // Preuzimanje aliasa klijenta iz ThreadLocal
        return (SecretKey) keystore.getKey(clientAlias, KEY_PASSWORD.toCharArray());
    }
}
