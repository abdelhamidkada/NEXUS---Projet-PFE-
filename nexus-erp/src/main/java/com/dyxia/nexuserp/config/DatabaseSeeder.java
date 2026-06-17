package com.dyxia.nexuserp.config;

import com.dyxia.nexuserp.model.*;
import com.dyxia.nexuserp.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Seeder de base de données pour populer MySQL avec un jeu de données riche,
 * réaliste et hiérarchisé pour la société DyxIA basée à Poitiers, incluant
 * le catalogue complet des compétences et leur association avec les profils.
 */
@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EmployeeProfileRepository employeeProfileRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final TimeTrackingRepository timeTrackingRepository;
    private final SkillRepository skillRepository;
    private final EmployeeSkillRepository employeeSkillRepository;
    private final NotificationRepository notificationRepository;
    private final MonthlyCycleRepository monthlyCycleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() >= 13) {
            System.out.println(">>> Base de données déjà initialisée par le DatabaseSeeder, saut du seeding.");
            return;
        }
        System.out.println(">>> DÉBUT DU SEEDING DE LA BASE DE DONNÉES (DyxIA)...");

        // 1. Purge complète des données transactionnelles, des compétences, des notifications et des profils/utilisateurs existants
        System.out.println(">>> Purge des anciennes données...");
        notificationRepository.deleteAll();
        leaveRequestRepository.deleteAll();
        timeTrackingRepository.deleteAll();
        monthlyCycleRepository.deleteAll();
        employeeSkillRepository.deleteAll();
        skillRepository.deleteAll();
        employeeProfileRepository.deleteAll();
        userRepository.deleteAll();

        // 2. Récupération des rôles (insérés via Flyway V1/V6)
        Role employeeRole = roleRepository.findByName("EMPLOYEE")
                .orElseGet(() -> roleRepository.save(Role.builder().name("EMPLOYEE").build()));
        Role managerRole = roleRepository.findByName("MANAGER")
                .orElseGet(() -> roleRepository.save(Role.builder().name("MANAGER").build()));
        Role hrAdminRole = roleRepository.findByName("HR_ADMIN")
                .orElseGet(() -> roleRepository.save(Role.builder().name("HR_ADMIN").build()));
        Role itAdminRole = roleRepository.findByName("IT_ADMIN")
                .orElseGet(() -> roleRepository.save(Role.builder().name("IT_ADMIN").build()));
        Role directionRole = roleRepository.findByName("DIRECTION")
                .orElseGet(() -> roleRepository.save(Role.builder().name("DIRECTION").build()));

        // 3. ÉTAPE 1 : Création du catalogue de compétences (Table SKILLS)
        System.out.println(">>> Création du catalogue de compétences...");

        // IT & Data
        Skill skillJava = createSkill("Java", "IT & Data");
        Skill skillSpringBoot = createSkill("Spring Boot", "IT & Data");
        Skill skillReact = createSkill("React", "IT & Data");
        Skill skillTypeScript = createSkill("TypeScript", "IT & Data");
        Skill skillDocker = createSkill("Docker", "IT & Data");
        Skill skillKubernetes = createSkill("Kubernetes", "IT & Data");
        Skill skillPython = createSkill("Python", "IT & Data");
        Skill skillSql = createSkill("SQL", "IT & Data");
        createSkill("DevOps", "IT & Data"); // DevOps catalog skill
        Skill skillArch = createSkill("Architecture Logicielle", "IT & Data");
        Skill skillReseaux = createSkill("Réseaux", "IT & Data");
        Skill skillMl = createSkill("Machine Learning", "IT & Data");

        // Design & Produit
        Skill skillFigma = createSkill("Figma", "Design & Produit");
        Skill skillUiUx = createSkill("UX/UI Design", "Design & Produit");
        Skill skillStoryline = createSkill("Articulate Storyline", "Design & Produit");
        Skill skillAdobe = createSkill("Adobe Creative Cloud", "Design & Produit");

        // Management & RH
        Skill skillLeadership = createSkill("Leadership", "Management & RH");
        Skill skillStrategie = createSkill("Stratégie d'entreprise", "Management & RH");
        Skill skillRecrutement = createSkill("Recrutement", "Management & RH");
        Skill skillDroit = createSkill("Droit du travail", "Management & RH");
        Skill skillAgile = createSkill("Gestion de projet Agile", "Management & RH");

        // Marketing & Ventes
        Skill skillSeo = createSkill("SEO", "Marketing & Ventes");
        Skill skillMarketingDigital = createSkill("Marketing Digital", "Marketing & Ventes");
        Skill skillCrm = createSkill("CRM", "Marketing & Ventes");
        Skill skillCommunication = createSkill("Communication", "Marketing & Ventes");

        System.out.println(">>> Catalogue de compétences créé avec succès.");

        // 4. Création des 13 Utilisateurs (avec mots de passe BCrypt)
        System.out.println(">>> Création des utilisateurs et profils...");

        // --- DIRECTION & MANAGEMENT ---
        User userKassimi = createUser("abdessamad.kassimi@dyxia.fr", "admin123", "Abdessamad", "Kassimi", directionRole);
        EmployeeProfile epKassimi = createProfile(userKassimi, "Président Directeur Général", "Direction", "E0001", "FR-CIN-01",
                "10 Rue de la Cathédrale, 86000 Poitiers, France", "+33 5 49 01 02 03", "CDI",
                LocalDate.of(2018, 5, 10), null, null,
                "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150",
                LocalDate.of(2018, 5, 10), "HYBRID", "MONTHLY", "FULL_TIME",
                "Poitiers, France", "LEAD", "Français, Anglais, Arabe");

        User userLaurent = createUser("sophie.laurent@dyxia.fr", "admin123", "Sophie", "Laurent", hrAdminRole);
        EmployeeProfile epLaurent = createProfile(userLaurent, "Directrice des Ressources Humaines", "Ressources Humaines", "E0002", "FR-CIN-02",
                "45 Rue du Marché Notre-Dame, 86000 Poitiers, France", "+33 5 49 04 05 06", "CDI",
                LocalDate.of(2020, 2, 15), null, null,
                "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150",
                LocalDate.of(2020, 2, 15), "OFFICE", "MONTHLY", "FULL_TIME",
                "Poitiers, France", "SENIOR", "Français, Anglais");

        User userDubois = createUser("thomas.dubois@dyxia.fr", "manager123", "Thomas", "Dubois", managerRole);
        EmployeeProfile epDubois = createProfile(userDubois, "Lead Developer / Manager IT", "Pôle IT & Support", "E0003", "FR-CIN-03",
                "8 Rue de l'Université, 86000 Poitiers, France", "+33 6 12 34 56 03", "CDI",
                LocalDate.of(2021, 9, 1), null, null,
                "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150",
                LocalDate.of(2021, 9, 1), "HYBRID", "MONTHLY", "FULL_TIME",
                "Poitiers, France", "LEAD", "Français, Anglais");

        User userLemaire = createUser("marc.lemaire@dyxia.fr", "manager123", "Marc", "Lemaire", managerRole);
        EmployeeProfile epLemaire = createProfile(userLemaire, "Responsable Marketing", "Marketing & Produit", "E0004", "FR-CIN-04",
                "15 Rue Gambetta, 86000 Poitiers, France", "+33 6 12 34 56 04", "CDI",
                LocalDate.of(2022, 4, 12), null, null,
                "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=150",
                LocalDate.of(2022, 4, 12), "HYBRID", "MONTHLY", "FULL_TIME",
                "Poitiers, France", "SENIOR", "Français, Anglais, Espagnol");

        // --- IT & SUPPORT ---
        User userLefebvre = createUser("antoine.lefebvre@dyxia.fr", "manager123", "Antoine", "Lefebvre", itAdminRole);
        EmployeeProfile epLefebvre = createProfile(userLefebvre, "Administrateur Systèmes et Réseaux", "Pôle IT & Support", "E0005", "FR-CIN-05",
                "32 Boulevard Pont Achard, 86000 Poitiers, France", "+33 6 12 34 56 05", "CDI",
                LocalDate.of(2023, 1, 10), null, null,
                "https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?w=150",
                LocalDate.of(2023, 1, 10), "OFFICE", "MONTHLY", "FULL_TIME",
                "Poitiers, France", "MID", "Français, Anglais");

        User userMorel = createUser("julien.morel@dyxia.fr", "employee123", "Julien", "Morel", employeeRole);
        EmployeeProfile epMorel = createProfile(userMorel, "Développeur Fullstack React/Spring", "Pôle IT & Support", "E0006", "FR-CIN-06",
                "18 Rue Carnot, 86000 Poitiers, France", "+33 6 12 34 56 06", "CDI",
                LocalDate.of(2024, 3, 1), null, userDubois,
                "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=150",
                LocalDate.of(2024, 3, 1), "HYBRID", "MONTHLY", "FULL_TIME",
                "Poitiers, France", "JUNIOR", "Français, Anglais");

        User userRoux = createUser("camille.roux@dyxia.fr", "employee123", "Camille", "Roux", employeeRole);
        EmployeeProfile epRoux = createProfile(userRoux, "Ingénieure DevOps", "Pôle IT & Support", "E0007", "FR-CIN-07",
                "7 Rue Pétonnet, 86000 Poitiers, France", "+33 6 12 34 56 07", "CDI",
                LocalDate.of(2024, 7, 15), null, userDubois,
                "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=150",
                LocalDate.of(2024, 7, 15), "WFH", "MONTHLY", "FULL_TIME",
                "Poitiers, France", "MID", "Français, Anglais");

        User userBlanc = createUser("lucas.blanc@dyxia.fr", "employee123", "Lucas", "Blanc", employeeRole);
        EmployeeProfile epBlanc = createProfile(userBlanc, "Data Analyst", "Pôle IT & Support", "E0008", "FR-CIN-08",
                "55 Grand'Rue, 86000 Poitiers, France", "+33 6 12 34 56 08", "CDI",
                LocalDate.of(2024, 11, 1), null, userDubois,
                "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=150",
                LocalDate.of(2024, 11, 1), "HYBRID", "MONTHLY", "FULL_TIME",
                "Poitiers, France", "JUNIOR", "Français, Anglais");

        // --- PRODUIT & PÉDAGOGIE ---
        User userPetit = createUser("emma.petit@dyxia.fr", "employee123", "Emma", "Petit", employeeRole);
        EmployeeProfile epPetit = createProfile(userPetit, "Conceptrice Pédagogique e-learning", "Marketing & Produit", "E0009", "FR-CIN-09",
                "10 Rue Victor Hugo, 86000 Poitiers, France", "+33 6 12 34 56 09", "CDI",
                LocalDate.of(2023, 6, 15), null, userLemaire,
                "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=150",
                LocalDate.of(2023, 6, 15), "WFH", "MONTHLY", "FULL_TIME",
                "Poitiers, France", "MID", "Français, Anglais");

        User userGirard = createUser("hugo.girard@dyxia.fr", "employee123", "Hugo", "Girard", employeeRole);
        EmployeeProfile epGirard = createProfile(userGirard, "UI/UX Designer", "Marketing & Produit", "E0010", "FR-CIN-10",
                "3 Rue de la Marne, 86000 Poitiers, France", "+33 6 12 34 56 10", "CDI",
                LocalDate.of(2023, 10, 1), null, userLemaire,
                "https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?w=150",
                LocalDate.of(2023, 10, 1), "HYBRID", "MONTHLY", "FULL_TIME",
                "Poitiers, France", "MID", "Français, Anglais");

        User userFontaine = createUser("chloe.fontaine@dyxia.fr", "employee123", "Chloé", "Fontaine", employeeRole);
        EmployeeProfile epFontaine = createProfile(userFontaine, "Chargée de Support Client", "Support Client", "E0011", "FR-CIN-11",
                "14 Rue de la Regratterie, 86000 Poitiers, France", "+33 6 12 34 56 11", "CDI",
                LocalDate.of(2024, 1, 15), null, null,
                "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150",
                LocalDate.of(2024, 1, 15), "OFFICE", "MONTHLY", "FULL_TIME",
                "Poitiers, France", "JUNIOR", "Français, Anglais, Espagnol");

        // --- LES STAGIAIRES ---
        User userKada = createUser("abdelhamid.kada@dyxia.fr", "employee123", "Kada", "Abdelhamid", employeeRole);
        EmployeeProfile epKada = createProfile(userKada, "Stagiaire Ingénieur Logiciel PFE", "Pôle IT & Support", "E0012", "FR-CIN-12",
                "2 Avenue de l'Europe, 86000 Poitiers, France", "+33 6 12 34 56 12", "STAGE",
                LocalDate.of(2026, 2, 1), 6, userDubois,
                "https://images.unsplash.com/photo-1488161628813-04466f872be2?w=150",
                LocalDate.of(2026, 2, 1), "HYBRID", "MONTHLY", "FULL_TIME",
                "Poitiers, France", "JUNIOR", "Arabe, Français, Anglais");

        User userGarcia = createUser("lea.garcia@dyxia.fr", "employee123", "Léa", "Garcia", employeeRole);
        EmployeeProfile epGarcia = createProfile(userGarcia, "Stagiaire Marketing Digital", "Marketing & Produit", "E0013", "FR-CIN-13",
                "9 Boulevard Solférino, 86000 Poitiers, France", "+33 6 12 34 56 13", "STAGE",
                LocalDate.of(2026, 3, 1), 6, userLemaire,
                "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=150",
                LocalDate.of(2026, 3, 1), "OFFICE", "MONTHLY", "FULL_TIME",
                "Poitiers, France", "JUNIOR", "Français, Anglais");

        System.out.println(">>> 13 utilisateurs et profils DyxIA créés.");

        // 5. ÉTAPE 2 : Association ciblée des compétences aux employés
        System.out.println(">>> Association des compétences aux collaborateurs...");

        // Abdessamad Kassimi (PDG)
        associateSkill(epKassimi, skillLeadership, 5);
        associateSkill(epKassimi, skillStrategie, 5);
        associateSkill(epKassimi, skillAgile, 4);

        // Sophie Laurent (DRH)
        associateSkill(epLaurent, skillRecrutement, 5);
        associateSkill(epLaurent, skillDroit, 5);
        associateSkill(epLaurent, skillCommunication, 4);

        // Thomas Dubois (Lead Dev)
        associateSkill(epDubois, skillJava, 5);
        associateSkill(epDubois, skillSpringBoot, 5);
        associateSkill(epDubois, skillArch, 5);
        associateSkill(epDubois, skillAgile, 4);

        // Antoine Lefebvre (Admin IT)
        associateSkill(epLefebvre, skillDocker, 5);
        associateSkill(epLefebvre, skillKubernetes, 4);
        associateSkill(epLefebvre, skillReseaux, 5);

        // Julien Morel (Dev Fullstack)
        associateSkill(epMorel, skillReact, 5);
        associateSkill(epMorel, skillTypeScript, 4);
        associateSkill(epMorel, skillSpringBoot, 3);

        // Camille Roux (DevOps)
        associateSkill(epRoux, skillDocker, 5);
        associateSkill(epRoux, skillKubernetes, 5);
        associateSkill(epRoux, skillPython, 4);

        // Lucas Blanc (Data Analyst)
        associateSkill(epBlanc, skillPython, 5);
        associateSkill(epBlanc, skillSql, 5);
        associateSkill(epBlanc, skillMl, 3);

        // Marc Lemaire (Marketing)
        associateSkill(epLemaire, skillMarketingDigital, 5);
        associateSkill(epLemaire, skillSeo, 4);
        associateSkill(epLemaire, skillCrm, 4);

        // Emma Petit (E-learning)
        associateSkill(epPetit, skillStoryline, 5);
        associateSkill(epPetit, skillUiUx, 3);

        // Hugo Girard (UI/UX)
        associateSkill(epGirard, skillFigma, 5);
        associateSkill(epGirard, skillAdobe, 5);
        associateSkill(epGirard, skillUiUx, 5);

        // Chloe Fontaine (Support)
        associateSkill(epFontaine, skillCrm, 5);
        associateSkill(epFontaine, skillCommunication, 5);

        // Kada Abdelhamid (Stagiaire PFE)
        associateSkill(epKada, skillReact, 4);
        associateSkill(epKada, skillSpringBoot, 4);
        associateSkill(epKada, skillSql, 4);
        associateSkill(epKada, skillDocker, 3);

        // Léa Garcia (Stagiaire Marketing)
        associateSkill(epGarcia, skillSeo, 3);
        associateSkill(epGarcia, skillMarketingDigital, 3);
        associateSkill(epGarcia, skillCommunication, 4);

        System.out.println(">>> Association des compétences réussie.");

        // 6. Génération de données transactionnelles (Congés)
        System.out.println(">>> Insertion des demandes de congés de test...");

        // Demande 1 : Kada Abdelhamid (PENDING)
        createLeaveRequest(
                LocalDate.of(2026, 7, 10),
                LocalDate.of(2026, 7, 15),
                LeaveType.ANNUAL,
                LeaveStatus.PENDING,
                "Besoin de quelques jours pour finaliser la rédaction du rapport de stage PFE.",
                epKada
        );

        // Demande 2 : Julien Morel (PENDING)
        createLeaveRequest(
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 15),
                LeaveType.ANNUAL,
                LeaveStatus.PENDING,
                "Voyage de noces et vacances d'été.",
                epMorel
        );

        // Demande 3 : Emma Petit (PENDING)
        createLeaveRequest(
                LocalDate.of(2026, 7, 24),
                LocalDate.of(2026, 7, 25),
                LeaveType.EXCEPTIONAL,
                LeaveStatus.PENDING,
                "Déménagement et installation dans un nouvel appartement à Poitiers.",
                epPetit
        );

        // Demande 4 : Camille Roux (VALIDATED_N1)
        createLeaveRequest(
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 5),
                LeaveType.SICK,
                LeaveStatus.VALIDATED_N1,
                "Arrêt maladie suite à une grippe saisonnière carabinée.",
                epRoux
        );

        // Demande 5 : Lucas Blanc (REJECTED)
        createLeaveRequest(
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 30),
                LeaveType.UNPAID,
                LeaveStatus.REJECTED,
                "Projet personnel de voyage humanitaire.",
                epBlanc
        );

        System.out.println(">>> SEEDING REUSSI : 5 demandes de congés transactionnelles insérées.");

        // Seeding monthly cycles for testing
        System.out.println(">>> Insertion des cycles mensuels de test...");
        // Kada: May 16 to June 15 (validated, not yet processed)
        monthlyCycleRepository.save(MonthlyCycle.builder()
                .employeeProfile(epKada)
                .startDate(LocalDate.of(2026, 5, 16))
                .endDate(LocalDate.of(2026, 6, 15))
                .validatedAsWorked(true)
                .processedForAccrual(false)
                .build());

        // Julien: May 16 to June 15 (not validated yet)
        monthlyCycleRepository.save(MonthlyCycle.builder()
                .employeeProfile(epMorel)
                .startDate(LocalDate.of(2026, 5, 16))
                .endDate(LocalDate.of(2026, 6, 15))
                .validatedAsWorked(false)
                .processedForAccrual(false)
                .build());

        // Seeding notifications
        System.out.println(">>> Insertion des notifications de test...");
        createNotification(userKada, "Congé validé");
        createNotification(userKada, "Nouveau document");
        createNotification(userKassimi, "Nouvelle demande de congé en attente");
        createNotification(userKassimi, "Nouveau rapport d'avancement disponible");
    }

    private User createUser(String email, String password, String firstName, String lastName, Role... roles) {
        Set<Role> roleSet = new HashSet<>(Arrays.asList(roles));
        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .firstName(firstName)
                .lastName(lastName)
                .isActive(true)
                .roles(roleSet)
                .build();
        return userRepository.save(user);
    }

    private EmployeeProfile createProfile(User user, String jobTitle, String department, String matricule, String cin,
                                          String adresse, String contact, String typeContrat,
                                          LocalDate dateDebutContrat, Integer dureeContrat,
                                          User manager, String photoUrl,
                                          LocalDate hireDate, String workModel, String payFrequency,
                                          String employmentFraction, String location,
                                          String seniorityLevel, String spokenLanguages) {
        EmployeeProfile profile = EmployeeProfile.builder()
                .user(user)
                .jobTitle(jobTitle)
                .department(department)
                .matricule(matricule)
                .cin(cin)
                .adresse(adresse)
                .contact(contact)
                .typeContrat(typeContrat)
                .dateDebutContrat(dateDebutContrat)
                .dureeContrat(dureeContrat)
                .hierarchie(manager)
                .photoUrl(photoUrl)
                .rib("FR763000600001123456789" + String.format("%02d", (int)(Math.random() * 90 + 10)))
                .signatureNumerique(user.getFirstName() + " " + user.getLastName())
                .hireDate(hireDate)
                .workModel(workModel)
                .payFrequency(payFrequency)
                .employmentFraction(employmentFraction)
                .location(location)
                .seniorityLevel(seniorityLevel)
                .spokenLanguages(spokenLanguages)
                .build();
        return employeeProfileRepository.save(profile);
    }

    private Skill createSkill(String name, String category) {
        Skill skill = Skill.builder()
                .name(name)
                .category(category)
                .build();
        return skillRepository.save(skill);
    }

    private void associateSkill(EmployeeProfile profile, Skill skill, int level) {
        EmployeeSkillId esId = EmployeeSkillId.builder()
                .employeeProfileId(profile.getId())
                .skillId(skill.getId())
                .build();

        EmployeeSkill es = EmployeeSkill.builder()
                .id(esId)
                .employeeProfile(profile)
                .skill(skill)
                .proficiencyLevel(level)
                .build();

        employeeSkillRepository.save(es);
    }

    private void createLeaveRequest(LocalDate start, LocalDate end, LeaveType type, LeaveStatus status,
                                    String reason, EmployeeProfile profile) {
        LeaveRequest leave = LeaveRequest.builder()
                .startDate(start)
                .endDate(end)
                .type(type)
                .status(status)
                .reason(reason)
                .employeeProfile(profile)
                .build();
        leaveRequestRepository.save(leave);
    }

    private void createNotification(User user, String message) {
        Notification notification = Notification.builder()
                .user(user)
                .message(message)
                .isRead(false)
                .createdAt(java.time.LocalDateTime.now())
                .build();
        notificationRepository.save(notification);
    }
}
