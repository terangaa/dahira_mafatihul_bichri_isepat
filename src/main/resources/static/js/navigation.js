
"use strict";

/* =========================================================
   1. CONFIGURATION GÉNÉRALE
========================================================= */

const DahiraNavigation = (() => {

    const TOKEN_KEY = "dahira_token";
    const ROLE_KEY = "dahira_role";
    const ADMIN_ROLE_KEY = "dahira_admin_role";

    const LOGIN_URL = "/login.html";

    const ROUTES = {
        ADMIN_PRINCIPAL: "/pages/admin-principal-dashboard.html",
        TRESORIER: "/pages/tresorier-dashboard.html",
        ADMINISTRATEUR: "/pages/administrateur-dashboard.html",
        MEMBER: "/pages/member-dashboard.html"
    };

    /* =====================================================
       2. RÉCUPÉRATION DES INFORMATIONS UTILISATEUR
    ===================================================== */

    function getToken() {
        return localStorage.getItem(TOKEN_KEY);
    }

    function getRole() {
        return (
            localStorage.getItem(ROLE_KEY) || ""
        ).trim().toUpperCase();
    }

    function getAdminRole() {
        return (
            localStorage.getItem(ADMIN_ROLE_KEY) || ""
        ).trim().toUpperCase();
    }

    function isAuthenticated() {
        return Boolean(getToken());
    }

    function isAdmin() {
        return getRole() === "ADMIN";
    }

    function isMember() {
        return getRole() === "MEMBER";
    }

    function isAdminPrincipal() {
        return isAdmin() && getAdminRole() === "ADMIN_PRINCIPAL";
    }

    function isTresorier() {
        return isAdmin() && getAdminRole() === "TRESORIER";
    }

    function isAdministrateur() {
        return isAdmin() && getAdminRole() === "ADMINISTRATEUR";
    }

    /* =====================================================
       3. IDENTIFICATION DU PROFIL
    ===================================================== */

    function getProfile() {

        if (isAdminPrincipal()) {
            return "ADMIN_PRINCIPAL";
        }

        if (isTresorier()) {
            return "TRESORIER";
        }

        if (isAdministrateur()) {
            return "ADMINISTRATEUR";
        }

        if (isMember()) {
            return "MEMBER";
        }

        if (isAdmin()) {
            return "ADMIN";
        }

        return "UNKNOWN";
    }

    /* =====================================================
       4. DÉTERMINER LE DASHBOARD
    ===================================================== */

    function getDashboardUrl() {

        const profile = getProfile();

        return ROUTES[profile] || LOGIN_URL;
    }

    /* =====================================================
       5. GESTION DES PERMISSIONS
    ===================================================== */

    const PERMISSIONS = {

        ADMIN_PRINCIPAL: [
            "MEMBERS_READ",
            "MEMBERS_CREATE",
            "MEMBERS_UPDATE",
            "MEMBERS_DELETE",

            "ACCOUNTS_READ",
            "ACCOUNTS_CREATE",
            "ACCOUNTS_UPDATE",
            "ACCOUNTS_DELETE",

            "COMMISSIONS_READ",
            "COMMISSIONS_MANAGE",

            "EVENTS_READ",
            "EVENTS_MANAGE",

            "PROJECTS_READ",
            "PROJECTS_MANAGE",

            "FINANCE_READ",
            "FINANCE_MANAGE",

            "CONTRIBUTIONS_READ",
            "CONTRIBUTIONS_MANAGE",

            "MONTHLY_PAYMENTS_READ",
            "MONTHLY_PAYMENTS_MANAGE",

            "HISTORY_READ"
        ],

        TRESORIER: [
            "MEMBERS_READ",

            "COMMISSIONS_READ",

            "EVENTS_READ",

            "PROJECTS_READ",

            "FINANCE_READ",
            "FINANCE_MANAGE",

            "CONTRIBUTIONS_READ",
            "CONTRIBUTIONS_MANAGE",

            "MONTHLY_PAYMENTS_READ",
            "MONTHLY_PAYMENTS_MANAGE",

            "HISTORY_READ"
        ],

        ADMINISTRATEUR: [
            "MEMBERS_READ",

            "COMMISSIONS_READ",
            "EVENTS_READ",
            "PROJECTS_READ",

            "FINANCE_READ",

            "CONTRIBUTIONS_READ",
            "MONTHLY_PAYMENTS_READ",

            "HISTORY_READ"
        ],

        MEMBER: [
            "EVENTS_READ",
            "PROJECTS_READ"
        ]
    };

    function hasPermission(permission) {

        const profile = getProfile();

        return (
            PERMISSIONS[profile] || []
        ).includes(permission);
    }

    /* =====================================================
       6. VISIBILITÉ DES ÉLÉMENTS DU MENU
    ===================================================== */

    function updateMenuVisibility() {

        document.querySelectorAll("[data-permission]")
            .forEach(element => {

                const permission =
                    element.dataset.permission;

                element.hidden = !hasPermission(permission);
            });

        document.querySelectorAll(".admin-only")
            .forEach(element => {

                element.hidden = !isAdmin();
            });

        document.querySelectorAll(".member-only")
            .forEach(element => {

                element.hidden = !isMember();
            });

        document.querySelectorAll(".admin-principal-only")
            .forEach(element => {

                element.hidden = !isAdminPrincipal();
            });

        document.querySelectorAll(".tresorier-only")
            .forEach(element => {

                element.hidden = !isTresorier();
            });

        document.querySelectorAll(".administrateur-only")
            .forEach(element => {

                element.hidden = !isAdministrateur();
            });

        document.querySelectorAll(".authenticated-only")
            .forEach(element => {

                element.hidden = !isAuthenticated();
            });

        document.querySelectorAll(".guest-only")
            .forEach(element => {

                element.hidden = isAuthenticated();
            });
    }

    /* =====================================================
       7. NAVIGATION ACTIVE
    ===================================================== */

    function updateActiveLink() {

        const currentPath = window.location.pathname
            .replace(/\/+$/, "")
            .toLowerCase();

        document.querySelectorAll(
            ".sidebar a[href], .nav-link[href]"
        ).forEach(link => {

            const href = link.getAttribute("href");

            if (!href || href.startsWith("#")) {
                return;
            }

            const linkPath = new URL(
                href,
                window.location.origin
            ).pathname
                .replace(/\/+$/, "")
                .toLowerCase();

            const active = currentPath === linkPath;

            link.classList.toggle("active", active);

            if (active) {
                link.setAttribute("aria-current", "page");
            } else {
                link.removeAttribute("aria-current");
            }
        });
    }

    /* =====================================================
       8. REDIRECTION SELON LE PROFIL
    ===================================================== */

    function redirectToDashboard() {

        if (!isAuthenticated()) {
            window.location.replace(LOGIN_URL);
            return;
        }

        const destination = getDashboardUrl();

        if (window.location.pathname !== destination) {
            window.location.replace(destination);
        }
    }

    /* =====================================================
       9. PROTECTION DES PAGES
    ===================================================== */

    function protectPage() {

        const currentPath = window.location.pathname;

        const publicPages = [
            "/",
            "/home.html",
            "/login.html",
            "/register.html"
        ];

        if (publicPages.includes(currentPath)) {
            return;
        }

        if (!isAuthenticated()) {
            window.location.replace(LOGIN_URL);
            return;
        }

        const restrictedProfile =
            document.body.dataset.allowedRoles;

        if (restrictedProfile) {

            const allowedRoles = restrictedProfile
                .split(",")
                .map(role => role.trim().toUpperCase());

            const currentProfile = getProfile();

            if (!allowedRoles.includes(currentProfile)) {

                redirectToDashboard();
            }
        }
    }

    /* =====================================================
       10. GESTION DE LA DÉCONNEXION
    ===================================================== */

    function logout() {

        localStorage.removeItem(TOKEN_KEY);
        localStorage.removeItem(ROLE_KEY);
        localStorage.removeItem(ADMIN_ROLE_KEY);

        localStorage.removeItem("accessToken");
        localStorage.removeItem("token");
        localStorage.removeItem("jwt");

        sessionStorage.clear();

        window.location.replace(LOGIN_URL);
    }

    function initializeLogout() {

        document.querySelectorAll(
            "#logout-btn, .logout-btn, [data-action='logout']"
        ).forEach(button => {

            button.addEventListener("click", event => {

                event.preventDefault();

                logout();
            });
        });
    }

    /* =====================================================
       11. INFORMATIONS DU PROFIL DANS LE MENU
    ===================================================== */

    function updateProfileInformation() {

        const profile = getProfile();

        const profileNames = {
            ADMIN_PRINCIPAL: "Administrateur principal",
            TRESORIER: "Trésorier",
            ADMINISTRATEUR: "Administrateur",
            MEMBER: "Membre",
            ADMIN: "Administrateur"
        };

        document.querySelectorAll("[data-user-role]")
            .forEach(element => {

                element.textContent =
                    profileNames[profile] || "Utilisateur";
            });
    }

    /* =====================================================
       12. INITIALISATION
    ===================================================== */

    function init() {

        if (!document.body) {
            return;
        }

        protectPage();

        updateMenuVisibility();

        updateActiveLink();

        initializeLogout();

        updateProfileInformation();

        document.dispatchEvent(
            new CustomEvent("dahira:navigation-ready", {
                detail: {
                    role: getRole(),
                    adminRole: getAdminRole(),
                    profile: getProfile()
                }
            })
        );
    }

    /* =====================================================
       13. API PUBLIQUE
    ===================================================== */

    return {

        init,

        getToken,
        getRole,
        getAdminRole,
        getProfile,

        isAuthenticated,
        isAdmin,
        isMember,
        isAdminPrincipal,
        isTresorier,
        isAdministrateur,

        hasPermission,

        getDashboardUrl,

        redirectToDashboard,

        logout
    };

})();

/* =========================================================
   14. LANCEMENT AUTOMATIQUE
========================================================= */

if (document.readyState === "loading") {

    document.addEventListener(
        "DOMContentLoaded",
        DahiraNavigation.init
    );

} else {

    DahiraNavigation.init();
}

/* =========================================================
   15. ACCÈS GLOBAL
========================================================= */

window.DahiraNavigation = DahiraNavigation;
