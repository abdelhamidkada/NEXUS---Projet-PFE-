package com.dyxia.nexuserp.util;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Convertisseur JPA permettant de chiffrer et déchiffrer de manière transparente
 * des données sensibles (ex: RIB) dans la base de données.
 * Utilise l'algorithme AES-GCM-256 avec vecteur d'initialisation dynamique (IV)
 * pour une sécurité optimale.
 */
@Component
@Converter
public class AttributeEncryptor implements AttributeConverter<String, String> {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int IV_LENGTH_BYTES = 12;
    private static final int TAG_LENGTH_BIT = 128;

    @Value("${application.security.encryption.secret-key}")
    private String secretKeyString;

    private SecretKeySpec secretKey;
    private final SecureRandom secureRandom = new SecureRandom();

    @PostConstruct
    public void init() {
        if (secretKeyString == null || secretKeyString.trim().isEmpty()) {
            throw new IllegalStateException("La clé de chiffrement 'application.security.encryption.secret-key' n'est pas configurée.");
        }

        byte[] keyBytes;
        try {
            // Tente de décoder depuis du Base64 si possible
            keyBytes = Base64.getDecoder().decode(secretKeyString.trim());
        } catch (IllegalArgumentException e) {
            // Sinon utilise les octets UTF-8 bruts
            keyBytes = secretKeyString.getBytes(StandardCharsets.UTF_8);
        }

        if (keyBytes.length != 32) {
            throw new IllegalArgumentException("La clé secrète doit faire exactement 256 bits (32 octets). Taille actuelle : " + keyBytes.length + " octets.");
        }

        this.secretKey = new SecretKeySpec(keyBytes, "AES");
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }

        try {
            // Génération d'un vecteur d'initialisation (IV) dynamique pour chaque opération
            byte[] iv = new byte[IV_LENGTH_BYTES];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

            byte[] cipherText = cipher.doFinal(attribute.getBytes(StandardCharsets.UTF_8));

            // Concaténation de l'IV et du texte chiffré dans le buffer final
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + cipherText.length);
            byteBuffer.put(iv);
            byteBuffer.put(cipherText);

            return Base64.getEncoder().encodeToString(byteBuffer.array());
        } catch (Exception e) {
            throw new SecurityException("Erreur critique lors du chiffrement AES-256", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }

        try {
            byte[] encryptedMessage = Base64.getDecoder().decode(dbData);

            if (encryptedMessage.length < IV_LENGTH_BYTES) {
                throw new IllegalArgumentException("Données chiffrées corrompues ou trop courtes");
            }

            ByteBuffer byteBuffer = ByteBuffer.wrap(encryptedMessage);

            // Extraction de l'IV
            byte[] iv = new byte[IV_LENGTH_BYTES];
            byteBuffer.get(iv);

            // Extraction du texte chiffré
            byte[] cipherText = new byte[byteBuffer.remaining()];
            byteBuffer.get(cipherText);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

            byte[] decryptedText = cipher.doFinal(cipherText);

            return new String(decryptedText, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new SecurityException("Erreur critique lors du déchiffrement AES-256", e);
        }
    }
}
