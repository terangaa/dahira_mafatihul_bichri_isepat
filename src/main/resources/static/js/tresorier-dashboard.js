(() => {

    'use strict';

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

    function redirectToLogin() {

        [
            'dahira_token',
            'dahira_refresh_token',
            'dahira_role',
            'dahira_admin_role',
            'dahira_email'
        ].forEach(key => {
            localStorage.removeItem(key);
            sessionStorage.removeItem(key);
        });

        window.location.replace(LOGIN_URL);
    }

    if (!token) {
        redirectToLogin();
        return;
    }

    if (role !== 'ADMIN' || adminRole !== 'TRESORIER') {

        alert(
            'Accès refusé. Cette page est réservée au trésorier.'
        );

        window.location.replace('/');
        return;
    }

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

    elements.adminEmail.textContent = email || 'Trésorier';

    document.getElementById('currentYear').textContent =
        new Date().getFullYear();

    function formatNumber(value) {

        const number = Number(value ?? 0);

        if (!Number.isFinite(number)) {
            return '0';
        }

        return new Intl.NumberFormat('fr-FR', {
            maximumFractionDigits: 0
        }).format(number);
    }

    function formatMoney(value) {
        return `${formatNumber(value)} FCFA`;
    }

    function showMessage(message, type = 'error') {

        elements.message.textContent = message;

        elements.message.className =
            `dashboard-message show ${type}`;
    }

    function hideMessage() {

        elements.message.textContent = '';
        elements.message.className = 'dashboard-message';
    }

    function setLoading(loading) {

        elements.refreshButton.disabled = loading;

        elements.refreshButton.innerHTML = loading
            ? '<span>↻</span> Chargement...'
            : '<span>↻</span> Actualiser les données';
    }

    function displayStatistics(data) {

        elements.members.textContent =
            formatNumber(data.membersCount);

        elements.commissions.textContent =
            formatNumber(data.commissionsCount);

        elements.events.textContent =
            formatNumber(data.eventsCount);

        elements.projects.textContent =
            formatNumber(data.projectsCount);

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

            if (response.status === 401) {
                redirectToLogin();
                return;
            }

            if (response.status === 403) {

                showMessage(
                    'Le serveur refuse l’accès aux statistiques. ' +
                    'Le backend doit autoriser le rôle TRESORIER ' +
                    'à consulter les données de ce tableau de bord.'
                );

                return;
            }

            if (!response.ok) {
                throw new Error(`Erreur HTTP ${response.status}`);
            }

            const data = await response.json();

            if (!data || typeof data !== 'object') {
                throw new Error('Réponse du serveur invalide.');
            }

            displayStatistics(data);

            console.info(
                'Tableau de bord Trésorier chargé :',
                data
            );

        } catch (error) {

            console.error(
                'Erreur du tableau de bord Trésorier :',
                error
            );

            showMessage(
                'Impossible de charger les statistiques. ' +
                'Vérifiez la connexion au serveur.'
            );

        } finally {
            setLoading(false);
        }
    }

    elements.refreshButton.addEventListener(
        'click',
        loadDashboard
    );

    elements.logoutButton.addEventListener('click', () => {

        if (!confirm('Voulez-vous vraiment vous déconnecter ?')) {
            return;
        }

        redirectToLogin();
    });

    elements.menuToggle.addEventListener('click', () => {
        elements.sidebar.classList.toggle('open');
    });

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

    loadDashboard();

})();