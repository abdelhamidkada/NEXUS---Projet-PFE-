package com.dyxia.nexuserp.service;

import com.dyxia.nexuserp.model.Employee;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

/**
 * Service pour l'exportation des profils d'employés au format PDF.
 */
@Service
public class PdfExportService {

    /**
     * Génère un document PDF contenant les détails de l'employé.
     * 
     * @param employee Les détails de l'employé à exporter.
     * @return Les données du fichier PDF sous forme de tableau de octets (byte[]).
     */
    public byte[] generateEmployeePdf(Employee employee) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        
        try {
            PdfWriter.getInstance(document, out);
            document.open();
            
            // Titre du document
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("Fiche Employé — NEXUS ERP", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(25);
            document.add(title);
            
            // Polices pour les étiquettes et les valeurs
            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
            
            // Nom complet
            Paragraph fullName = new Paragraph();
            fullName.add(new Chunk("Nom Complet : ", labelFont));
            fullName.add(new Chunk(employee.getFullName(), valueFont));
            fullName.setSpacingAfter(10);
            document.add(fullName);
            
            // Matricule
            Paragraph matricule = new Paragraph();
            matricule.add(new Chunk("Matricule : ", labelFont));
            matricule.add(new Chunk(employee.getMatricule(), valueFont));
            matricule.setSpacingAfter(10);
            document.add(matricule);
            
            // Rôle / Poste
            Paragraph role = new Paragraph();
            role.add(new Chunk("Poste / Rôle : ", labelFont));
            role.add(new Chunk(employee.getRole() != null ? employee.getRole() : "N/A", valueFont));
            role.setSpacingAfter(10);
            document.add(role);
            
            // Département
            Paragraph department = new Paragraph();
            department.add(new Chunk("Département : ", labelFont));
            department.add(new Chunk(employee.getDepartment() != null ? employee.getDepartment() : "N/A", valueFont));
            department.setSpacingAfter(10);
            document.add(department);
            
            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Erreur lors de la génération du document PDF", e);
        }
        
        return out.toByteArray();
    }
}
