package com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.entity;

public enum AdminRole {

    /**
     * Administrateur principal.
     *
     * Accès complet à la gestion financière.
     */
    ADMIN_PRINCIPAL,

    /**
     * Trésorier.
     *
     * Gère les opérations financières courantes.
     */
    TRESORIER,

    /**
     * Administrateur classique.
     *
     * Peut consulter les finances mais ne peut pas
     * modifier les données financières.
     */
    ADMINISTRATEUR
}