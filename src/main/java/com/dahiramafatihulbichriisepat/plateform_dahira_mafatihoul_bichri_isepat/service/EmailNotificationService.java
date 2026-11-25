
package com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);

    private final JavaMailSender mailSender;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.mail.from}")
    private String senderEmail;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    public EmailNotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Envoie un e-mail simple.
     * Une erreur d'envoi ne doit pas annuler une décision métier.
     */
    private boolean sendEmail(String recipient, String subject, String content) {

        if (!mailEnabled) {
            log.info(
                    "Notification désactivée. Destinataire : {}",
                    recipient);
            return false;
        }

        if (recipient == null || recipient.isBlank()) {
            log.warn("E-mail non envoyé : destinataire absent.");
            return false;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();

            message.setFrom(senderEmail);
            message.setTo(recipient);
            message.setSubject(subject);
            message.setText(content);

            mailSender.send(message);

            log.info("E-mail envoyé avec succès à {}", recipient);
            return true;

        } catch (Exception exception) {
            log.error(
                    "Échec de l'envoi d'un e-mail à {} : {}",
                    recipient,
                    exception.getMessage());

            return false;
        }
    }

    /**
     * Informe l'administrateur et le demandeur
     * qu'une demande d'adhésion a été enregistrée.
     */
    public void notifyNewRegistration(
            String applicantName,
            String applicantEmail) {
        String name = safeName(applicantName);

        sendEmail(
                adminEmail,
                "Nouvelle demande d'adhésion - Dahira Mafatihoul Bichri",
                """
                        Bonjour,

                        Une nouvelle demande d'adhésion a été enregistrée sur la
                        plateforme Dahira Mafatihoul Bichri - Touba ISEPAT.

                        Nom du demandeur : %s
                        Adresse e-mail : %s

                        Veuillez vous connecter à la plateforme pour examiner
                        cette demande.

                        Cordialement,
                        Plateforme Dahira Mafatihoul Bichri
                        """.formatted(name, safeEmail(applicantEmail)));

        sendEmail(
                applicantEmail,
                "Confirmation de votre demande d'adhésion",
                """
                        Bonjour %s,

                        Nous avons bien reçu votre demande d'adhésion au Dahira
                        Mafatihoul Bichri - Touba ISEPAT de Diamniadio.

                        Votre dossier est actuellement en attente de validation
                        par l'administration.

                        Vous recevrez un nouvel e-mail lorsque votre demande
                        aura été examinée.

                        Merci pour votre confiance.

                        Cordialement,
                        Administration du Dahira Mafatihoul Bichri
                        """.formatted(name));
    }

    /**
     * Informe le demandeur que son compte a été approuvé.
     */
    public void notifyMembershipApproved(
            String applicantName,
            String applicantEmail) {
        String name = safeName(applicantName);

        sendEmail(
                applicantEmail,
                "Votre demande d'adhésion a été approuvée",
                """
                        Bonjour %s,

                        Nous avons le plaisir de vous informer que votre demande
                        d'adhésion au Dahira Mafatihoul Bichri - Touba ISEPAT
                        de Diamniadio a été approuvée.

                        Vous pouvez maintenant vous connecter à la plateforme
                        avec l'adresse e-mail utilisée lors de votre inscription
                        et le mot de passe que vous avez choisi.

                        Accédez à la page de connexion de la plateforme pour
                        ouvrir votre compte.

                        Pour votre sécurité, ne communiquez jamais votre mot
                        de passe à une autre personne.

                        Bienvenue au Dahira Mafatihoul Bichri !

                        Cordialement,
                        Administration du Dahira Mafatihoul Bichri
                        """.formatted(name));
    }

    /**
     * Informe le demandeur que sa demande a été rejetée.
     */
    public void notifyMembershipRejected(
            String applicantName,
            String applicantEmail,
            String reason) {
        String name = safeName(applicantName);

        String reasonText = reason == null || reason.isBlank()
                ? "Aucun motif complémentaire n'a été communiqué."
                : reason.trim();

        sendEmail(
                applicantEmail,
                "Information concernant votre demande d'adhésion",
                """
                        Bonjour %s,

                        Après examen de votre dossier, nous vous informons que
                        votre demande d'adhésion au Dahira Mafatihoul Bichri -
                        Touba ISEPAT de Diamniadio n'a pas été acceptée.

                        Motif communiqué :
                        %s

                        Pour toute information complémentaire, veuillez
                        contacter l'administration du Dahira.

                        Cordialement,
                        Administration du Dahira Mafatihoul Bichri
                        """.formatted(name, reasonText));
    }

    private String safeName(String name) {
        return name == null || name.isBlank()
                ? "Membre"
                : name.trim();
    }

    private String safeEmail(String email) {
        return email == null || email.isBlank()
                ? "Non renseignée"
                : email.trim();
    }
}
