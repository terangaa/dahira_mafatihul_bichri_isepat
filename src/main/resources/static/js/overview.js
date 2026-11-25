/**

* Dahira Mafatihoul Bichri
* Tableau de bord : administrateur et membre.
*
* Fichier : src/main/resources/static/js/overview.js
  */

(() => {
  'use strict';


  document.addEventListener('DOMContentLoaded', initializeOverview);

  function initializeOverview() {
    const token = localStorage.getItem('dahira_token');
    const role = (
      localStorage.getItem('dahira_role') || ''
    ).trim().toUpperCase();

    const email = localStorage.getItem('dahira_email') || 'Utilisateur';

    // Une session est obligatoire pour accéder au tableau de bord.
    if (!token || !['ADMIN', 'MEMBER'].includes(role)) {
      clearInvalidSession();
      window.location.replace('/login.html');
      return;
    }

    const isAdmin = role === 'ADMIN';

    configureNavigation(isAdmin);
    configureDashboard(isAdmin);
    configureUserInformation(email, role);
    configureLogout();
  }

  /**
   * Affiche uniquement les liens correspondant au rôle.
   */
  function configureNavigation(isAdmin) {
    document.querySelectorAll('.admin-only').forEach(element => {
      element.classList.toggle('hidden', !isAdmin);
      element.setAttribute('aria-hidden', String(!isAdmin));

      if (!isAdmin) {
        element.setAttribute('tabindex', '-1');
      } else {
        element.removeAttribute('tabindex');
      }
    });
  }

  /**
   * Sépare le tableau de bord administratif de celui du membre.
   */
  function configureDashboard(isAdmin) {
    document.querySelectorAll('.admin-dashboard-only').forEach(element => {
      element.classList.toggle('hidden', !isAdmin);
    });

    const memberPanel = document.getElementById('member-dashboard-panel');

    if (memberPanel) {
      memberPanel.classList.toggle('hidden', isAdmin);

      if (!isAdmin) {
        memberPanel.replaceChildren();

        const heading = document.createElement('h2');
        heading.textContent = 'Bienvenue dans votre espace membre';

        const message = document.createElement('p');
        message.textContent =
          'Consultez les événements, les informations et les activités du Dahira.';

        memberPanel.append(heading, message);
      }
    }

    const kicker = document.getElementById('dashboard-kicker');
    const heading = document.getElementById('dashboard-heading');

    if (!isAdmin) {
      if (kicker) {
        kicker.textContent = 'Votre espace personnel';
      }

      if (heading) {
        heading.textContent = 'Bienvenue au Dahira Mafatihoul Bichri';
      }
    }
  }

  /**
   * Met à jour l'identité visible dans la barre supérieure.
   */
  function configureUserInformation(email, role) {
    const emailElement = document.getElementById('user-email');
    const roleElement = document.getElementById('user-role');
    const avatarElement = document.getElementById('user-avatar');

    if (emailElement) {
      emailElement.textContent = email;
    }

    if (roleElement) {
      roleElement.textContent =
        role === 'ADMIN' ? 'Administrateur' : 'Membre actif';
    }

    if (avatarElement) {
      const initials = email
        .split('@')[0]
        .split(/[._-]+/)
        .filter(Boolean)
        .slice(0, 2)
        .map(part => part.charAt(0).toUpperCase())
        .join('');

      avatarElement.textContent = initials || 'DM';
    }
  }

  /**
   * Déconnexion et nettoyage des informations locales.
   */
  function configureLogout() {
    document.querySelectorAll(
      '[data-logout], #logout-button, #logout, .logout-button'
    ).forEach(button => {
      // Éviter d'enregistrer plusieurs fois le même gestionnaire.
      if (button.dataset.overviewLogoutBound === 'true') {
        return;
      }

      button.dataset.overviewLogoutBound = 'true';

      button.addEventListener('click', event => {
        event.preventDefault();

        [
          'dahira_token',
          'dahira_refresh_token',
          'dahira_role',
          'dahira_email',
          'token',
          'jwt',
          'jwtToken',
          'accessToken',
          'access_token',
          'authToken',
          'auth_token',
          'refreshToken',
          'refresh_token',
          'user',
          'currentUser',
          'current_user',
          'authUser'
        ].forEach(key => {
          localStorage.removeItem(key);
          sessionStorage.removeItem(key);
        });

        window.location.replace('/login.html');
      });
    });
  }

  function clearInvalidSession() {
    [
      'dahira_token',
      'dahira_refresh_token',
      'dahira_role',
      'dahira_email'
    ].forEach(key => {
      localStorage.removeItem(key);
      sessionStorage.removeItem(key);
    });
  }


})();
