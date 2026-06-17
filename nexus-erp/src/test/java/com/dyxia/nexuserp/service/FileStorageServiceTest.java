package com.dyxia.nexuserp.service;

import com.dyxia.nexuserp.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Suite de tests unitaires pour FileStorageService.
 * Utilise @TempDir pour s'assurer que les fichiers créés lors des tests
 * n'impactent pas l'environnement local de développement.
 */
class FileStorageServiceTest {

    @TempDir
    Path tempDir;

    private FileStorageService fileStorageService;

    @BeforeEach
    void setUp() {
        fileStorageService = new FileStorageService(tempDir.toString());
        fileStorageService.init();
    }

    @Test
    void testStoreFileSuccess() throws IOException {
        String content = "Contenu de test du document PDF";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "fiche_de_paie.pdf",
                "application/pdf",
                content.getBytes(StandardCharsets.UTF_8)
        );

        String relativePath = fileStorageService.storeFile(file, "E0012");

        // Vérifie le chemin de retour relatif
        assertEquals("E0012/fiche_de_paie.pdf", relativePath);

        // Vérifie l'écriture physique du fichier dans la structure ségréguée
        Path savedFilePath = tempDir.resolve("E0012").resolve("fiche_de_paie.pdf");
        assertTrue(Files.exists(savedFilePath), "Le fichier devrait exister sur le disque");
        assertEquals(content, Files.readString(savedFilePath), "Le contenu du fichier devrait correspondre");
    }

    @Test
    void testStoreFileDirectoryTraversalThrowsException() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "../../malicieux.txt",
                "text/plain",
                "donnees".getBytes()
        );

        // Doit lever une exception IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () ->
                fileStorageService.storeFile(file, "E0012")
        );
    }

    @Test
    void testLoadFileAsResourceSuccess() throws IOException {
        Path employeeDir = tempDir.resolve("E0002");
        Files.createDirectories(employeeDir);
        Path targetFile = employeeDir.resolve("contrat.txt");
        String content = "Ceci est un contrat de travail de test";
        Files.writeString(targetFile, content);

        Resource resource = fileStorageService.loadFileAsResource("E0002/contrat.txt");

        assertNotNull(resource);
        assertTrue(resource.exists());
        assertTrue(resource.isReadable());
        assertEquals(content, Files.readString(Paths.get(resource.getURI())), "Le contenu de la ressource lue doit être identique");
    }

    @Test
    void testLoadFileAsResourceNotFoundThrowsException() {
        // Chargement d'un fichier inexistant
        assertThrows(ResourceNotFoundException.class, () ->
                fileStorageService.loadFileAsResource("E0002/inexistant.txt")
        );
    }

    @Test
    void testLoadFileAsResourceDirectoryTraversalThrowsSecurityException() {
        // Tenter d'accéder à un fichier en dehors de la racine (ex: un niveau au dessus de tempDir)
        assertThrows(SecurityException.class, () ->
                fileStorageService.loadFileAsResource("../outside_file.txt")
        );
    }
}
