(() => {

    'use strict';

    // ==========================================
    // 1. CONFIGURATION
    // ==========================================

    const API_URL = '/api/dashboard';

    const LOGIN_URL = '/login.html';

    const token = localStorage.getItem('dahira_token');

    const role = (
        localStorage.getItem('dahira_role') || ''
    ).trim().toUpperCase();

    const adminRole = (
        localStorage.getItem('dahira_admin_role') || ''
    ).trim().toUpperCase();

    const email = localStorage.getItem('dahira_email') || '';

    // ==========================================
    // 2. VÉRIFICATION DE L'AUTHENTIFICATION
    // ==========================================

    function redirectToLogin() {

        localStorage.removeItem('dahira_token');
        localStorage.removeItem('dahira_refresh_token');

        localStorage.removeItem('dahira_role');
        localStorage.removeItem('dahira_admin_role');

        localStorage.removeItem('dahira_email');

        window.location.replace(LOGIN_URL);

    }


    if (!token) {

        redirectToLogin();
        return;

    }


    // ==========================================
    // 3. VÉRIFICATION DU PROFIL ADMINISTRATEUR
    // ==========================================

    if (role !== 'ADMIN' || adminRole !== 'ADMIN_PRINCIPAL') {

        alert(
            'Accès refusé. Cette page est réservée à l’administrateur principal.'
        );

        window.location.replace('/');

        return;

    }


    // ==========================================
    // 4. RÉCUPÉRATION DES ÉLÉMENTS HTML
    // ==========================================

    const elements = {

        members: document.getElementById('membersCount'),

        commissions: document.getElementById('commissionsCount'),

        events: document.getElementById('eventsCount'),

        projects: document.getElementById('projectsCount'),

        totalFinancial: document.getElementById('totalFinancial'),

        collectedMonthly: document.getElementById(
            'collectedMonthlyPayments'
        ),

        remainingMonthly: document.getElementById(
            'remainingMonthlyPayments'
        ),

        collectedContributions: document.getElementById(
            'collectedContributions'
        ),

        remainingContributions: document.getElementById(
            'remainingContributions'
        ),

        message: document.getElementById('dashboardMessage'),

        refreshButton: document.getElementById('refreshButton'),

        adminEmail: document.getElementById('adminEmail'),

        logoutButton: document.getElementById('logoutButton'),

        menuToggle: document.getElementById('menuToggle'),

        sidebar: document.getElementById('sidebar')

    };


    // ==========================================
    // 5. INITIALISATION
    // ==========================================

    elements.adminEmail.textContent = email || 'Administrateur principal';

    document.getElementById('currentYear').textContent =
        new Date().getFullYear();


    // ==========================================
    // 6. FORMATAGE DES NOMBRES
    // ==========================================

    function formatNumber(value) {

        const number = Number(value ?? 0);

        if (!Number.isFinite(number)) {
            return '0';
        }

        return new Intl.NumberFormat('fr-FR', {
            maximumFractionDigits: 0
        }).format(number);

    }


    // ==========================================
    // 7. FORMATAGE FINANCIER
    // ==========================================

    function formatMoney(value) {

        const number = Number(value ?? 0);

        if (!Number.isFinite(number)) {
            return '0 FCFA';
        }

        return `${formatNumber(number)} FCFA`;

    }


    // ==========================================
    // 8. GESTION DES MESSAGES
    // ==========================================

    function showMessage(message, type = 'error') {

        elements.message.textContent = message;

        elements.message.className =
            `dashboard-message show ${type}`;

    }


    function hideMessage() {

        elements.message.textContent = '';

        elements.message.className = 'dashboard-message';

    }


    // ==========================================
    // 9. ÉTAT DU BOUTON ACTUALISER
    // ==========================================

    function setLoading(loading) {

        elements.refreshButton.disabled = loading;

        elements.refreshButton.innerHTML = loading
            ? '<span>↻</span> Chargement...'
            : '<span>↻</span> Actualiser les données';

    }


    // ==========================================
    // 10. AFFICHAGE DES STATISTIQUES
    // ==========================================

    function displayStatistics(data) {

        elements.members.textContent =
            formatNumber(data.membersCount);

        elements.commissions.textContent =
            formatNumber(data.commissionsCount);

        elements.events.textContent =
            formatNumber(data.eventsCount);

        elements.projects.textContent =
            formatNumber(data.projectsCount);


        // Situation financière

        elements.collectedMonthly.textContent =
            formatMoney(data.collectedMonthlyPayments);

        elements.remainingMonthly.textContent =
            formatMoney(data.remainingMonthlyPayments);

        elements.collectedContributions.textContent =
            formatMoney(data.collectedContributions);

        elements.remainingContributions.textContent =
            formatMoney(data.remainingContributions);

        elements.totalFinancial.textContent =
            formatMoney(data.totalFinancial);

    }


    // ==========================================
    // 11. CHARGEMENT DES DONNÉES DU BACKEND
    // ==========================================

    async function loadDashboard() {

        hideMessage();

        setLoading(true);

        try {

            const response = await fetch(API_URL, {

                method: 'GET',

                headers: {

                    'Authorization': `Bearer ${token}`,

                    'Accept': 'application/json'

                },

                cache: 'no-store'

            });


            // Gestion des erreurs d'authentification

            if (response.status === 401) {

                redirectToLogin();

                return;

            }


            if (response.status === 403) {

                showMessage(
                    'Accès refusé par le serveur. Vérifiez que votre compte possède bien l’autorité ROLE_ADMIN_PRINCIPAL.'
                );

                return;

            }


            if (!response.ok) {

                throw new Error(
                    `Erreur du serveur : HTTP ${response.status}`
                );

            }


            const data = await response.json();


            // Vérification de la réponse

            if (!data || typeof data !== 'object') {

                throw new Error(
                    'Les données reçues du serveur sont invalides.'
                );

            }


            // Affichage des données

            displayStatistics(data);


            console.info(
                'Statistiques du tableau de bord chargées :',
                data
            );


        } catch (error) {

            console.error(
                'Erreur de chargement du tableau de bord :',
                error
            );

            showMessage(
                'Impossible de charger les statistiques. Vérifiez que votre serveur Spring Boot fonctionne et que votre connexion est toujours valide.'
            );

        } finally {

            setLoading(false);

        }

    }


    // ==========================================
    // 12. ACTUALISATION MANUELLE
    // ==========================================

    elements.refreshButton.addEventListener(
        'click',
        loadDashboard
    );


    // ==========================================
    // 13. DÉCONNEXION
    // ==========================================

    elements.logoutButton.addEventListener('click', () => {

        const confirmation = confirm(
            'Voulez-vous vraiment vous déconnecter ?'
        );

        if (!confirmation) {
            return;
        }

        const keys = [

            'dahira_token',

            'dahira_refresh_token',

            'dahira_role',

            'dahira_admin_role',

            'dahira_email'

        ];

        keys.forEach(key => {

            localStorage.removeItem(key);

            sessionStorage.removeItem(key);

        });

        window.location.replace(LOGIN_URL);

    });


    // ==========================================
    // 14. MENU MOBILE
    // ==========================================

    elements.menuToggle.addEventListener('click', () => {

        elements.sidebar.classList.toggle('open');

    });


    // Fermer le menu en cliquant à l'extérieur

    document.addEventListener('click', event => {

        const clickedInsideSidebar =
            elements.sidebar.contains(event.target);

        const clickedMenuButton =
            elements.menuToggle.contains(event.target);

        if (
            window.innerWidth <= 850 &&
            !clickedInsideSidebar &&
            !clickedMenuButton
        ) {

            elements.sidebar.classList.remove('open');

        }

    });


    // ==========================================
    // 15. CHARGEMENT AUTOMATIQUE
    // ==========================================

    loadDashboard();


})();