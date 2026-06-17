package com.dyxia.nexuserp.controller;

import com.dyxia.nexuserp.dto.HrDocumentResponse;
import com.dyxia.nexuserp.exception.ResourceNotFoundException;
import com.dyxia.nexuserp.model.DocumentType;
import com.dyxia.nexuserp.model.EmployeeProfile;
import com.dyxia.nexuserp.model.HrDocument;
import com.dyxia.nexuserp.repository.EmployeeProfileRepository;
import com.dyxia.nexuserp.repository.HrDocumentRepository;
import com.dyxia.nexuserp.service.FileStorageService;
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

/**
 * Contrôleur REST exposant les endpoints pour le coffre-fort numérique (Digital Vault).
 * Permet aux collaborateurs d'uploader et downloader leurs documents de manière sécurisée.
 */
@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final FileStorageService fileStorageService;
    private final EmployeeProfileRepository employeeProfileRepository;
    private final HrDocumentRepository hrDocumentRepository;

    /**
     * Endpoint POST pour uploader un document RH.
     * Enregistre le fichier sur disque et ajoute un enregistrement dans la base de données.
     *
     * @param file Le fichier physique envoyé.
     * @param employeeMatricule Le matricule de l'employé à qui appartient le document.
     * @param documentType Le type de document (CONTRACT, PAYSLIP, ID_CARD, OTHER).
     * @return Les détails du document sauvegardé sous forme de HrDocumentResponse DTO.
     */
    @PostMapping("/upload")
    public ResponseEntity<HrDocumentResponse> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("employeeMatricule") String employeeMatricule,
            @RequestParam(value = "documentType", defaultValue = "OTHER") DocumentType documentType) {

        // 1. Recherche du profil de l'employé par son matricule
        EmployeeProfile profile = employeeProfileRepository.findByMatricule(employeeMatricule)
                .orElseThrow(() -> new ResourceNotFoundException("Profil employé non trouvé avec le matricule : " + employeeMatricule));

        // 2. Enregistrement physique du fichier
        String relativePath = fileStorageService.storeFile(file, employeeMatricule);

        // 3. Création et sauvegarde de la fiche document dans la base de données
        HrDocument hrDocument = HrDocument.builder()
                .employeeProfile(profile)
                .documentType(documentType)
                .filePath(relativePath)
                .isSigned(false)
                .build();

        HrDocument savedDoc = hrDocumentRepository.save(hrDocument);

        // 4. Mapping et retour du DTO
        HrDocumentResponse response = HrDocumentResponse.builder()
                .id(savedDoc.getId())
                .documentType(savedDoc.getDocumentType().name())
                .filePath(savedDoc.getFilePath())
                .uploadDate(savedDoc.getUploadDate())
                .isSigned(savedDoc.getIsSigned())
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint GET pour télécharger un document depuis le coffre-fort.
     *
     * @param filePath Le chemin relatif du fichier à télécharger (ex: "E0002/mon_cv.pdf").
     * @return Le fichier sous forme de flux binaire téléchargeable.
     */
    @GetMapping("/download")
    public ResponseEntity<Resource> downloadDocument(@RequestParam("filePath") String filePath) {
        // 1. Chargement de la ressource
        Resource resource = fileStorageService.loadFileAsResource(filePath);

        // 2. Détection dynamique du type MIME
        String contentType = "application/octet-stream";
        try {
            String probedType = Files.probeContentType(Paths.get(resource.getURI()));
            if (probedType != null) {
                contentType = probedType;
            }
        } catch (IOException ex) {
            // Ignorer l'erreur et conserver le type générique par défaut
        }

        // 3. Retour du flux de données
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
