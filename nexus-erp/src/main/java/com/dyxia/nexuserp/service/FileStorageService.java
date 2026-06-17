package com.dyxia.nexuserp.service;

import com.dyxia.nexuserp.exception.ResourceNotFoundException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Service gérant le stockage physique et la récupération des documents du coffre-fort numérique.
 */
@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    /**
     * Constructeur injectant le chemin racine depuis application.yml.
     */
    public FileStorageService(@Value("${nexus.storage.path}") String storagePath) {
        this.fileStorageLocation = Paths.get(storagePath).toAbsolutePath().normalize();
    }

    /**
     * Initialisation automatique : crée le dossier de stockage de base s'il n'existe pas.
     */
    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException ex) {
            throw new IllegalStateException("Impossible de créer le dossier racine de stockage : " + this.fileStorageLocation, ex);
        }
    }

    /**
     * Enregistre un fichier physique dans un sous-dossier nommé d'après le matricule de l'employé.
     * Cette ségrégation garantit la protection des données.
     *
     * @param file Le fichier Multipart envoyé par le client.
     * @param employeeMatricule Le matricule de l'employé.
     * @return Le chemin relatif du fichier sauvegardé (ex: "E0002/fiche_de_paie.pdf").
     */
    public String storeFile(MultipartFile file, String employeeMatricule) {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            // Protection de sécurité : interdire le directory traversal (ex: filename contenant "../")
            if (fileName.contains("..")) {
                throw new IllegalArgumentException("Le nom du fichier contient des séquences de chemin invalides : " + fileName);
            }

            // Définition et création du sous-dossier de l'employé
            Path targetFolder = this.fileStorageLocation.resolve(employeeMatricule).normalize();
            Files.createDirectories(targetFolder);

            // Copie ou remplacement du fichier
            Path targetLocation = targetFolder.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Retourne le chemin relatif avec des slashs génériques
            return Paths.get(employeeMatricule).resolve(fileName).toString().replace("\\", "/");
        } catch (IOException ex) {
            throw new IllegalStateException("Échec de l'enregistrement du fichier " + fileName + ".", ex);
        }
    }

    /**
     * Charge un fichier sous forme de Resource Spring pour permettre le téléchargement.
     *
     * @param filePath Le chemin relatif du fichier (ex: "E0002/fiche_de_paie.pdf").
     * @return La ressource correspondante.
     */
    public Resource loadFileAsResource(String filePath) {
        try {
            Path targetPath = this.fileStorageLocation.resolve(filePath).normalize();

            // Protection de sécurité : interdire la navigation en dehors du dossier racine de stockage
            if (!targetPath.startsWith(this.fileStorageLocation)) {
                throw new SecurityException("Accès non autorisé en dehors de l'espace de stockage : " + filePath);
            }

            Resource resource = new UrlResource(targetPath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("Fichier introuvable ou non lisible : " + filePath);
            }
        } catch (MalformedURLException ex) {
            throw new ResourceNotFoundException("Chemin de fichier mal formé : " + filePath, ex);
        }
    }
}
