package com.dyxia.nexuserp.service;

import com.dyxia.nexuserp.model.Employee;
import com.dyxia.nexuserp.model.EmployeeProfile;
import com.dyxia.nexuserp.repository.EmployeeProfileRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.draw.LineSeparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Service OpenPDF pour la génération de documents PDF RH.
 *
 * <p>Fournit deux types de génération :</p>
 * <ul>
 *   <li>{@link #generateEmployeePdf(Employee)} — Fiche collaborateur (profil 360°)</li>
 *   <li>{@link #generateAttestationPdf(EmployeeProfile)} — Attestation de travail à usage légal</li>
 * </ul>
 */
@Service
public class PdfExportService {

    @Autowired
    private EmployeeProfileRepository employeeProfileRepository;

    private static final DateTimeFormatter FR_DATE = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.FRENCH);
    private static final Color NEXUS_BLUE    = new Color(30, 64, 175);   // #1E40AF
    private static final Color NEXUS_LIGHT   = new Color(239, 246, 255); // #EFF6FF
    private static final Color DARK_TEXT     = new Color(17, 24, 39);    // #111827
    private static final Color MUTED_TEXT    = new Color(107, 114, 128); // #6B7280

    // ─────────────────────────────────────────────────────────────────────────
    // 1. FICHE COLLABORATEUR (profil 360°)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Génère une fiche collaborateur simple au format PDF.
     *
     * @param employee Les données de l'employé (modèle simplifié).
     * @return Tableau d'octets représentant le fichier PDF.
     */
    @Transactional(readOnly = true)
    public byte[] generateEmployeePdf(Employee employee) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);

        try {
            String today = LocalDate.now().format(FR_DATE);
            String email = "N/A";
            String cin = "N/A";
            String contact = "N/A";
            String typeContrat = "CDI";
            String location = "France";
            String hireDateStr = today;

            EmployeeProfile profile = employeeProfileRepository.findByMatricule(employee.getMatricule()).orElse(null);
            if (profile != null) {
                if (profile.getUser() != null) {
                    email = profile.getUser().getEmail() != null ? profile.getUser().getEmail() : "N/A";
                }
                cin = profile.getCin() != null ? profile.getCin() : "N/A";
                contact = profile.getContact() != null ? profile.getContact() : "N/A";
                typeContrat = profile.getTypeContrat() != null ? profile.getTypeContrat() : "CDI";
                location = profile.getLocation() != null ? profile.getLocation() : "France";
                LocalDate hireDate = profile.getHireDate() != null
                        ? profile.getHireDate()
                        : (profile.getDateDebutContrat() != null ? profile.getDateDebutContrat() : LocalDate.now());
                hireDateStr = hireDate.format(FR_DATE);
            }

            final String finalMatricule = employee.getMatricule() != null ? employee.getMatricule() : "N/A";

            PdfWriter writer = PdfWriter.getInstance(document, out);
            writer.setPageEvent(new PdfPageEventHelper() {
                @Override
                public void onEndPage(PdfWriter writer, Document document) {
                    PdfContentByte cb = writer.getDirectContent();
                    cb.setColorStroke(NEXUS_BLUE);
                    cb.setLineWidth(0.5f);
                    cb.moveTo(document.left(), document.bottom() - 5);
                    cb.lineTo(document.right(), document.bottom() - 5);
                    cb.stroke();

                    Font footerFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 7, MUTED_TEXT);
                    String footerText = "Document généré automatiquement par NEXUS ERP le " + today +
                        " — Référence : FICHE-" + finalMatricule + "-" + LocalDate.now().getYear() +
                        " — Confidentiel — Ne pas diffuser sans autorisation RH.";

                    ColumnText.showTextAligned(
                        cb,
                        Element.ALIGN_CENTER,
                        new Phrase(footerText, footerFont),
                        (document.left() + document.right()) / 2,
                        document.bottom() - 15,
                        0
                    );
                }
            });

            document.open();

            // ── BLOC EMPLOYEUR (haut gauche) ──────────────────────────────────
            Font employerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, DARK_TEXT);
            Font employerLight = FontFactory.getFont(FontFactory.HELVETICA, 9, MUTED_TEXT);

            Paragraph employerBlock = new Paragraph();
            employerBlock.add(new Chunk("DyxIA — Direction des Ressources Humaines\n", employerFont));
            employerBlock.add(new Chunk("Système d'Information RH NEXUS ERP\n", employerLight));
            employerBlock.add(new Chunk("contact.rh@dyxia.fr  |  www.dyxia.fr\n", employerLight));
            employerBlock.setSpacingAfter(4);
            document.add(employerBlock);

            // Date d'émission (haut droit)
            Font dateFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, MUTED_TEXT);
            Paragraph dateBlock = new Paragraph("Fait à " + location + ", le " + today, dateFont);
            dateBlock.setAlignment(Element.ALIGN_RIGHT);
            dateBlock.setSpacingAfter(12);
            document.add(dateBlock);

            // ── TITRE CENTRAL ──────────────────────────────────────────────────
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, NEXUS_BLUE);
            Paragraph title = new Paragraph("FICHE COLLABORATEUR", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(2);
            document.add(title);

            // Sous-titre
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10, MUTED_TEXT);
            Paragraph subtitle = new Paragraph("Document officiel émis par le Département RH", subtitleFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(10);
            document.add(subtitle);

            document.add(new Chunk(new LineSeparator(1f, 100f, NEXUS_BLUE, Element.ALIGN_CENTER, -2)));

            // ── TABLEAU 1 : INFORMATIONS D'IDENTITÉ ────────────────────────────
            Font sectionFont  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, NEXUS_BLUE);
            Paragraph section1 = new Paragraph("Informations d'Identité", sectionFont);
            section1.setSpacingBefore(8);
            section1.setSpacingAfter(4);
            document.add(section1);

            PdfPTable identityTable = new PdfPTable(2);
            identityTable.setWidthPercentage(100f);
            identityTable.setWidths(new float[]{35f, 65f});
            identityTable.setSpacingAfter(8);

            addTableRow(identityTable, "Nom Complet",         employee.getFullName() != null ? employee.getFullName() : "N/A");
            addTableRow(identityTable, "Matricule",           finalMatricule);
            addTableRow(identityTable, "Email",               email);
            addTableRow(identityTable, "CIN",                 cin);
            addTableRow(identityTable, "Contact",             contact);

            document.add(identityTable);

            // ── TABLEAU 2 : INFORMATIONS PROFESSIONNELLES ─────────────────────
            Paragraph section2 = new Paragraph("Informations Professionnelles", sectionFont);
            section2.setSpacingBefore(8);
            section2.setSpacingAfter(4);
            document.add(section2);

            PdfPTable professionalTable = new PdfPTable(2);
            professionalTable.setWidthPercentage(100f);
            professionalTable.setWidths(new float[]{35f, 65f});
            professionalTable.setSpacingAfter(12);

            addTableRow(professionalTable, "Poste",              employee.getRole() != null ? employee.getRole() : "N/A");
            addTableRow(professionalTable, "Département",        employee.getDepartment() != null ? employee.getDepartment() : "N/A");
            addTableRow(professionalTable, "Type de Contrat",    typeContrat);
            addTableRow(professionalTable, "Date d'embauche",    hireDateStr);

            document.add(professionalTable);

            document.add(new Chunk(new LineSeparator(1f, 100f, NEXUS_BLUE, Element.ALIGN_CENTER, -2)));

            // ── SIGNATURE ─────────────────────────────────────────────────────
            Font signFont  = FontFactory.getFont(FontFactory.HELVETICA, 10, DARK_TEXT);
            Font signBold  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, DARK_TEXT);

            Paragraph signBlock = new Paragraph();
            signBlock.setAlignment(Element.ALIGN_RIGHT);
            signBlock.setSpacingBefore(12);
            signBlock.add(new Chunk("Pour la Direction des Ressources Humaines,\n", signFont));
            signBlock.add(new Chunk("DyxIA — NEXUS ERP Platform\n", signBold));
            signBlock.add(Chunk.NEWLINE);
            signBlock.add(Chunk.NEWLINE);
            signBlock.add(Chunk.NEWLINE);
            signBlock.add(Chunk.NEWLINE);
            signBlock.add(new Chunk("_________________________________\n", signFont));
            signBlock.add(new Chunk("DRH / Responsable Paie", signFont));
            document.add(signBlock);

            document.close();

        } catch (DocumentException e) {
            throw new RuntimeException("Erreur lors de la génération de la fiche PDF", e);
        }

        return out.toByteArray();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. ATTESTATION DE TRAVAIL (document légal)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Génère une attestation de travail officielle au format PDF via OpenPDF.
     *
     * <p>Le document contient :</p>
     * <ul>
     *   <li>En-tête avec identité employeur (DyxIA / NEXUS ERP)</li>
     *   <li>Date de génération et lieu d'émission</li>
     *   <li>Corps légal avec coordonnées complètes du salarié</li>
     *   <li>Type de contrat, date d'embauche et ancienneté calculée</li>
     *   <li>Formule de clôture officielle</li>
     *   <li>Pied de page avec mention légale et horodatage</li>
     * </ul>
     *
     * @param profile Le profil JPA complet du collaborateur.
     * @return Tableau d'octets représentant le fichier PDF.
     */
    public byte[] generateAttestationPdf(EmployeeProfile profile) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);

        try {
            // ── Informations collaborateur ────────────────────────────────────
            String firstName = "";
            String lastName  = "";
            String email     = "";
            if (profile.getUser() != null) {
                firstName = profile.getUser().getFirstName() != null ? profile.getUser().getFirstName() : "";
                lastName  = profile.getUser().getLastName()  != null ? profile.getUser().getLastName()  : "";
                email     = profile.getUser().getEmail()     != null ? profile.getUser().getEmail()     : "";
            }
            String fullName    = (firstName + " " + lastName).trim();
            String matricule   = profile.getMatricule()   != null ? profile.getMatricule()   : "N/A";
            String jobTitle    = profile.getJobTitle()    != null ? profile.getJobTitle()    : "N/A";
            String department  = profile.getDepartment()  != null ? profile.getDepartment()  : "N/A";
            String typeContrat = profile.getTypeContrat() != null ? profile.getTypeContrat() : "CDI";
            String location    = profile.getLocation()    != null ? profile.getLocation()    : "France";

            // Date d'embauche et ancienneté
            LocalDate hireDate    = profile.getHireDate() != null
                    ? profile.getHireDate()
                    : (profile.getDateDebutContrat() != null ? profile.getDateDebutContrat() : LocalDate.now());
            String hireDateStr   = hireDate.format(FR_DATE);
            long yearsOfService  = java.time.temporal.ChronoUnit.YEARS.between(hireDate, LocalDate.now());

            String today = LocalDate.now().format(FR_DATE);

            PdfWriter writer = PdfWriter.getInstance(document, out);
            writer.setPageEvent(new PdfPageEventHelper() {
                @Override
                public void onEndPage(PdfWriter writer, Document document) {
                    PdfContentByte cb = writer.getDirectContent();
                    cb.setColorStroke(NEXUS_BLUE);
                    cb.setLineWidth(0.5f);
                    cb.moveTo(document.left(), document.bottom() - 5);
                    cb.lineTo(document.right(), document.bottom() - 5);
                    cb.stroke();

                    Font footerFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 7, MUTED_TEXT);
                    String footerText = "Document généré automatiquement par NEXUS ERP le " + today +
                        " — Référence : ATT-" + matricule + "-" + LocalDate.now().getYear() +
                        " — Confidentiel — Ne pas diffuser sans autorisation RH.";

                    ColumnText.showTextAligned(
                        cb,
                        Element.ALIGN_CENTER,
                        new Phrase(footerText, footerFont),
                        (document.left() + document.right()) / 2,
                        document.bottom() - 15,
                        0
                    );
                }
            });

            document.open();

            // ── BLOC EMPLOYEUR (haut gauche) ──────────────────────────────────
            Font employerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, DARK_TEXT);
            Font employerLight = FontFactory.getFont(FontFactory.HELVETICA, 9, MUTED_TEXT);

            Paragraph employerBlock = new Paragraph();
            employerBlock.add(new Chunk("DyxIA — Direction des Ressources Humaines\n", employerFont));
            employerBlock.add(new Chunk("Système d'Information RH NEXUS ERP\n", employerLight));
            employerBlock.add(new Chunk("contact.rh@dyxia.fr  |  www.dyxia.fr\n", employerLight));
            employerBlock.setSpacingAfter(4);
            document.add(employerBlock);

            // Date d'émission (haut droit simulé via un second paragraphe)
            Font dateFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, MUTED_TEXT);
            Paragraph dateBlock = new Paragraph("Fait à " + location + ", le " + today, dateFont);
            dateBlock.setAlignment(Element.ALIGN_RIGHT);
            dateBlock.setSpacingAfter(12);
            document.add(dateBlock);

            // ── TITRE CENTRAL ──────────────────────────────────────────────────
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, NEXUS_BLUE);
            Paragraph title = new Paragraph("ATTESTATION DE TRAVAIL", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(2);
            document.add(title);

            // Sous-titre
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10, MUTED_TEXT);
            Paragraph subtitle = new Paragraph("Document officiel émis par le Département RH", subtitleFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(10);
            document.add(subtitle);

            document.add(new Chunk(new LineSeparator(1f, 100f, NEXUS_BLUE, Element.ALIGN_CENTER, -2)));

            // ── TABLEAU D'IDENTITÉ DU COLLABORATEUR ────────────────────────────
            Font sectionFont  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, NEXUS_BLUE);
            Paragraph section1 = new Paragraph("Informations du Collaborateur", sectionFont);
            section1.setSpacingBefore(8);
            section1.setSpacingAfter(4);
            document.add(section1);

            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100f);
            infoTable.setWidths(new float[]{35f, 65f});
            infoTable.setSpacingAfter(8);

            addTableRow(infoTable, "Nom et Prénom",         fullName.isEmpty() ? "N/A" : fullName);
            addTableRow(infoTable, "Matricule",              matricule);
            addTableRow(infoTable, "Adresse e-mail",         email.isEmpty() ? "N/A" : email);
            addTableRow(infoTable, "Poste occupé",           jobTitle);
            addTableRow(infoTable, "Département",            department);
            addTableRow(infoTable, "Lieu d'affectation",     location);

            document.add(infoTable);

            // ── TABLEAU CONTRAT ────────────────────────────────────────────────
            Paragraph section2 = new Paragraph("Détails du Contrat de Travail", sectionFont);
            section2.setSpacingAfter(4);
            document.add(section2);

            PdfPTable contractTable = new PdfPTable(2);
            contractTable.setWidthPercentage(100f);
            contractTable.setWidths(new float[]{35f, 65f});
            contractTable.setSpacingAfter(10);

            addTableRow(contractTable, "Nature du contrat",    typeContrat);
            addTableRow(contractTable, "Date d'embauche",      hireDateStr);
            addTableRow(contractTable, "Ancienneté",
                    yearsOfService > 0
                            ? yearsOfService + " an(s)"
                            : "Moins d'un an (période d'essai ou récent)");

            document.add(contractTable);

            document.add(new Chunk(new LineSeparator(1f, 100f, NEXUS_BLUE, Element.ALIGN_CENTER, -2)));

            // ── CORPS LÉGAL ───────────────────────────────────────────────────
            Font bodyFont  = FontFactory.getFont(FontFactory.HELVETICA, 10, DARK_TEXT);
            Font boldFont  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, DARK_TEXT);

            Paragraph body = new Paragraph();
            body.setLeading(14f);
            body.setSpacingBefore(10);
            body.setSpacingAfter(8);

            body.add(new Chunk("La société ", bodyFont));
            body.add(new Chunk("DyxIA", boldFont));
            body.add(new Chunk(", représentée par sa Direction des Ressources Humaines, " +
                    "atteste par la présente que ", bodyFont));
            body.add(new Chunk("M. / Mme " + fullName, boldFont));
            body.add(new Chunk(", portant le matricule ", bodyFont));
            body.add(new Chunk(matricule, boldFont));
            body.add(new Chunk(", est bien employé(e) au sein de notre organisation en qualité de ", bodyFont));
            body.add(new Chunk(jobTitle, boldFont));
            body.add(new Chunk(" au sein du département ", bodyFont));
            body.add(new Chunk(department, boldFont));
            body.add(new Chunk(".", bodyFont));

            Paragraph body2 = new Paragraph();
            body2.setLeading(14f);
            body2.setSpacingAfter(8);
            body2.add(new Chunk("Ce contrat de travail de type ", bodyFont));
            body2.add(new Chunk(typeContrat, boldFont));
            body2.add(new Chunk(" est en vigueur depuis le ", bodyFont));
            body2.add(new Chunk(hireDateStr, boldFont));
            body2.add(new Chunk(". Cette attestation est délivrée à la demande de l'intéressé(e) " +
                    "pour faire valoir ce que de droit.", bodyFont));

            document.add(body);
            document.add(body2);

            // ── SIGNATURE ─────────────────────────────────────────────────────
            Font signFont  = FontFactory.getFont(FontFactory.HELVETICA, 10, DARK_TEXT);
            Font signBold  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, DARK_TEXT);

            Paragraph signBlock = new Paragraph();
            signBlock.setAlignment(Element.ALIGN_RIGHT);
            signBlock.setSpacingBefore(12);
            signBlock.add(new Chunk("Pour la Direction des Ressources Humaines,\n", signFont));
            signBlock.add(new Chunk("DyxIA — NEXUS ERP Platform\n", signBold));
            signBlock.add(Chunk.NEWLINE);
            signBlock.add(Chunk.NEWLINE);
            signBlock.add(Chunk.NEWLINE);
            signBlock.add(Chunk.NEWLINE);
            signBlock.add(new Chunk("_________________________________\n", signFont));
            signBlock.add(new Chunk("DRH / Responsable Paie", signFont));
            document.add(signBlock);

            document.close();

        } catch (DocumentException e) {
            throw new RuntimeException("Erreur lors de la génération de l'attestation PDF", e);
        }

        return out.toByteArray();
    }



    /** Ajoute une ligne stylisée dans un tableau à 2 colonnes. */
    private void addTableRow(PdfPTable table, String label, String value) {
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, NEXUS_BLUE);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 9, DARK_TEXT);

        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBackgroundColor(NEXUS_LIGHT);
        labelCell.setBorderColor(new Color(219, 234, 254)); // #DBECFE
        labelCell.setPadding(6f);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorderColor(new Color(219, 234, 254));
        valueCell.setPadding(6f);
        table.addCell(valueCell);
    }
}
