package com.dyxia.nexuserp.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Service pour la génération sécurisée de QR codes pour le pointage physique sur borne (kiosque).
 */
@Service
public class QrCodeService {

    /**
     * Génère un QR Code au format PNG sous forme de tableau d'octets contenant un payload JSON sécurisé.
     * Le payload comprend le matricule, la date courante et un jeton unique (UUID).
     *
     * @param matricule Le matricule de l'employé (ex: E0001).
     * @return Les données de l'image du QR Code (PNG) sous forme de byte[].
     */
    public byte[] generateEmployeeQrCode(String matricule) {
        String token = UUID.randomUUID().toString();
        String date = LocalDate.now().toString();
        
        // Construction du payload JSON
        String jsonPayload = String.format(
            "{\"matricule\": \"%s\", \"date\": \"%s\", \"token\": \"%s\"}",
            matricule, date, token
        );

        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(jsonPayload, BarcodeFormat.QR_CODE, 300, 300);

            try (ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream()) {
                MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
                return pngOutputStream.toByteArray();
            }
        } catch (WriterException | IOException e) {
            throw new RuntimeException("Erreur lors de la génération du QR Code pour le matricule : " + matricule, e);
        }
    }
}
