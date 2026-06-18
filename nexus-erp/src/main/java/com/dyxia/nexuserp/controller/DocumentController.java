package com.dyxia.nexuserp.controller;

import com.dyxia.nexuserp.dto.HrDocumentResponse;
import com.dyxia.nexuserp.exception.ResourceNotFoundException;
import com.dyxia.nexuserp.model.DocumentType;
import com.dyxia.nexuserp.model.EmployeeProfile;
import com.dyxia.nexuserp.model.HrDocument;
import com.dyxia.nexuserp.repository.EmployeeProfileRepository;
import com.dyxia.nexuserp.repository.HrDocumentRepository;
import com.dyxia.nexuserp.service.FileStorageService;
import com.dyxia.nexuserp.service.PdfExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Contrôleur REST exposant les endpoints pour le Coffre-Fort Numérique (Digital Vault).
 *
 * <p>Routes disponibles :</p>
 * <ul>
 *   <li>POST  /api/v1/documents/{matricule}/upload                – Upload d'un document RH</li>
 *   <li>GET   /api/v1/documents/{matricule}/download/{filename}   – Téléchargement d'un fichier</li>
 *   <li>GET   /api/v1/documents/{matricule}/export/attestation    – Génération d'une attestation de travail PDF</li>
 *   <li>GET   /api/v1/documents/{matricule}/list                  – Liste des documents du collaborateur</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
@Tag(name = "Coffre-Fort Numérique", description = "Gestion sécurisée des documents RH des collaborateurs")
public class DocumentController {

    private final FileStorageService fileStorageService;
    private final PdfExportService pdfExportService;
    private final EmployeeProfileRepository employeeProfileRepository;
    private final HrDocumentRepository hrDocumentRepository;

    // ──────────────────────────────────────────────────────────────────────────
    // 1. UPLOAD — POST /api/v1/documents/{matricule}/upload
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Endpoint POST pour uploader un document RH dans le coffre-fort de l'employé.
     * Le fichier est physiquement stocké dans un sous-dossier isolé par matricule
     * (ex: /storage/E0003/contrat.pdf) pour garantir la ségrégation stricte des données.
     *
     * @param matricule    Le matricule de l'employé propriétaire du document.
     * @param file         Le fichier multipart envoyé par le client.
     * @param documentType Le type de document (CONTRACT, PAYSLIP, ID_CARD, DIPLOMA, OTHER).
     * @return Les métadonnées du document sauvegardé (DTO HrDocumentResponse).
     */
    @PostMapping("/{matricule}/upload")
    @Operation(summary = "Uploader un document RH dans le coffre-fort de l'employé")
    public ResponseEntity<HrDocumentResponse> uploadDocument(
            @PathVariable String matricule,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "documentType", defaultValue = "OTHER") DocumentType documentType) {

        // 1. Résolution du profil par matricule
        EmployeeProfile profile = findProfileByMatricule(matricule);

        // 2. Sauvegarde physique isolée dans /storage/{matricule}/
        String relativePath = fileStorageService.storeFile(file, matricule);

        // 3. Enregistrement de la fiche document en BDD
        HrDocument hrDocument = HrDocument.builder()
                .employeeProfile(profile)
                .documentType(documentType)
                .filePath(relativePath)
                .isSigned(false)
                .build();

        HrDocument savedDoc = hrDocumentRepository.save(hrDocument);

        // 4. Mapping vers le DTO de réponse
        HrDocumentResponse response = HrDocumentResponse.builder()
                .id(savedDoc.getId())
                .documentType(savedDoc.getDocumentType().name())
                .filePath(savedDoc.getFilePath())
                .uploadDate(savedDoc.getUploadDate())
                .isSigned(savedDoc.getIsSigned())
                .build();

        return ResponseEntity.ok(response);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 2. DOWNLOAD — GET /api/v1/documents/{matricule}/download/{filename}
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Endpoint GET pour télécharger un fichier depuis le coffre-fort d'un employé.
     * La protection contre le path traversal est assurée côté FileStorageService.
     *
     * @param matricule Le matricule de l'employé (sert à construire le chemin relatif).
     * @param filename  Le nom du fichier à télécharger.
     * @return Le fichier sous forme de flux binaire téléchargeable.
     */
    @GetMapping("/{matricule}/download/{filename:.+}")
    @Operation(summary = "Télécharger un document depuis le coffre-fort de l'employé")
    public ResponseEntity<Resource> downloadDocument(
            @PathVariable String matricule,
            @PathVariable String filename) {

        // Construit le chemin relatif: "E0003/contrat.pdf"
        String filePath = matricule + "/" + filename;
        Resource resource = fileStorageService.loadFileAsResource(filePath);

        // Détection dynamique du type MIME pour un rendu correct dans le navigateur
        String contentType = "application/octet-stream";
        try {
            String probedType = Files.probeContentType(Paths.get(resource.getURI()));
            if (probedType != null) {
                contentType = probedType;
            }
        } catch (IOException ex) {
            // Conserver le type générique par défaut en cas d'erreur
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 3. ATTESTATION — GET /api/v1/documents/{matricule}/export/attestation
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Endpoint GET pour générer et télécharger une attestation de travail au format PDF.
     * Le document est généré à la volée via OpenPDF à partir des données du profil JPA.
     * Aucune persistence sur disque n'est effectuée — le PDF est renvoyé directement.
     *
     * @param matricule Le matricule de l'employé pour lequel générer l'attestation.
     * @return Un flux PDF téléchargeable (application/pdf).
     */
    @GetMapping("/{matricule}/export/attestation")
    @Operation(summary = "Générer une attestation de travail PDF pour un employé")
    public ResponseEntity<byte[]> exportAttestationPdf(@PathVariable String matricule) {

        // 1. Chargement du profil complet depuis la BDD
        EmployeeProfile profile = findProfileByMatricule(matricule);

        // 2. Génération du PDF via le PdfExportService (OpenPDF)
        byte[] pdfBytes = pdfExportService.generateAttestationPdf(profile);

        // 3. Retour du flux PDF avec les entêtes appropriées
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"attestation_travail_" + matricule + ".pdf\"")
                .body(pdfBytes);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 4. LIST — GET /api/v1/documents/{matricule}/list
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Endpoint GET pour lister tous les documents du coffre-fort d'un employé.
     *
     * @param matricule Le matricule de l'employé.
     * @return Liste des documents enregistrés (DTOs).
     */
    @GetMapping("/{matricule}/list")
    @Operation(summary = "Lister les documents du coffre-fort d'un employé")
    public ResponseEntity<List<HrDocumentResponse>> listDocuments(@PathVariable String matricule) {
        EmployeeProfile profile = findProfileByMatricule(matricule);

        List<HrDocumentResponse> docs = hrDocumentRepository.findByEmployeeProfile(profile)
                .stream()
                .map(doc -> HrDocumentResponse.builder()
                        .id(doc.getId())
                        .documentType(doc.getDocumentType().name())
                        .filePath(doc.getFilePath())
                        .uploadDate(doc.getUploadDate())
                        .isSigned(doc.getIsSigned())
                        .build())
                .toList();

        return ResponseEntity.ok(docs);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Helper privé
    // ──────────────────────────────────────────────────────────────────────────

    private EmployeeProfile findProfileByMatricule(String matricule) {
        return employeeProfileRepository.findByMatricule(matricule)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Profil employé non trouvé avec le matricule : " + matricule));
    }
}
