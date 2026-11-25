/* ============================================================
DAHIRA MAFATIHoul BICHRI
Interface commune : rôles, navigation et déconnexion

IMPORTANT :
Ce fichier contrôle l'affichage.
Les autorisations réelles restent assurées par Spring Security.
============================================================ */

document.addEventListener('DOMContentLoaded', () => {


  'use strict';

  // ----------------------------------------------------------
  // 1. Récupération des informations d'authentification
  // ----------------------------------------------------------

  function getStoredValue(key) {
    return localStorage.getItem(key)
      || sessionStorage.getItem(key)
      || '';
  }

  function getToken() {
    return getStoredValue('dahira_token')
      || getStoredValue('accessToken')
      || getStoredValue('access_token')
      || getStoredValue('jwt')
      || getStoredValue('jwtToken')
      || getStoredValue('authToken')
      || getStoredValue('auth_token');
  }

  function normalizeRole(value) {
    if (!value) {
      return '';
    }

    let role = String(value).trim().toUpperCase();

    if (role.startsWith('ROLE_')) {
      role = role.substring(5);
    }

    return role;
  }

  function getRoleFromToken(token) {
    if (!token) {
      return '';
    }

    try {
      const parts = token.split('.');

      if (parts.length !== 3) {
        return '';
      }

      const payload = parts[1]
        .replace(/-/g, '+')
        .replace(/_/g, '/');

      const paddedPayload = payload.padEnd(
        Math.ceil(payload.length / 4) * 4,
        '='
      );

      const claims = JSON.parse(atob(paddedPayload));

      const possibleRoles = [
        claims.role,
        claims.roles,
        claims.authorities,
        claims.authority
      ];

      for (const candidate of possibleRoles) {

        if (typeof candidate === 'string') {
          const role = normalizeRole(candidate);

          if (role === 'ADMIN' || role === 'MEMBER') {
            return role;
          }
        }

        if (Array.isArray(candidate)) {
          for (const item of candidate) {

            const role = normalizeRole(
              typeof item === 'string'
                ? item
                : item?.authority || item?.name
            );

            if (role === 'ADMIN' || role === 'MEMBER') {
              return role;
            }
          }
        }
      }

    } catch (error) {
      console.warn(
        'Impossible de lire le rôle depuis le JWT.',
        error
      );
    }

    return '';
  }

  function getCurrentRole() {
    const storedRole = normalizeRole(
      getStoredValue('dahira_role')
    );

    if (storedRole === 'ADMIN' || storedRole === 'MEMBER') {
      return storedRole;
    }

    return getRoleFromToken(getToken());
  }

  const token = getToken();
  const role = getCurrentRole();

  const isAdmin = role === 'ADMIN';
  const isMember = role === 'MEMBER';

  // ----------------------------------------------------------
  // 2. Détection des pages et contrôle de navigation
  // ----------------------------------------------------------

  const adminOnlyPages = [
    '/pages/members.html',
    '/pages/membership-requests.html',
    '/pages/contributions.html'
  ];

  const currentPath = window.location.pathname.replace(/\/+$/, '') || '/';

  const isAdminOnlyPage = adminOnlyPages.includes(currentPath);

  // Les membres ne doivent pas accéder aux pages de gestion.
  if (isAdminOnlyPage && !isAdmin) {
    window.location.replace('/member-dashboard.html?access=denied');
    return;
  }



  // Les pages privées d'administration ne doivent pas être
  // affichées à un utilisateur sans rôle valide.
  //
  // La protection des données reste impérativement assurée
  // par les règles d'autorisation du backend.

  if (isAdminOnlyPage && !isAdmin) {
    window.location.replace(
      '/pages/index.html?access=denied'
    );
    return;
  }

  // ----------------------------------------------------------
  // 3. Masquage des fonctionnalités administratives
  // ----------------------------------------------------------

  document.querySelectorAll(
    '.admin-only, .admin-dashboard-only'
  ).forEach(element => {

    if (isAdmin) {
      element.classList.remove('hidden');
      element.hidden = false;
    } else {
      element.classList.add('hidden');
      element.hidden = true;
    }

  });

  // ----------------------------------------------------------
  // 4. Affichage de l'espace personnel du membre
  // ----------------------------------------------------------

  document.querySelectorAll('.member-dashboard-only').forEach(
    element => {

      if (isMember) {
        element.classList.remove('hidden');
        element.hidden = false;
      } else {
        element.classList.add('hidden');
        element.hidden = true;
      }

    }
  );

  // ----------------------------------------------------------
  // 5. Adaptation du titre et des informations du compte
  // ----------------------------------------------------------

  const userRoleElement = document.getElementById('user-role');
  const userEmailElement = document.getElementById('user-email');
  const dashboardHeading = document.getElementById('dashboard-heading');
  const dashboardKicker = document.getElementById('dashboard-kicker');

  if (userRoleElement) {
    if (isAdmin) {
      userRoleElement.textContent = 'Administrateur';
    } else if (isMember) {
      userRoleElement.textContent = 'Membre';
    } else {
      userRoleElement.textContent = 'Rôle non identifié';
    }
  }

  if (isMember) {

    if (dashboardHeading) {
      dashboardHeading.textContent =
        'Bienvenue dans votre espace personnel';
    }

    if (dashboardKicker) {
      dashboardKicker.textContent = 'Mon espace membre';
    }

  } else if (isAdmin) {

    if (dashboardKicker) {
      dashboardKicker.textContent = 'Administration du Dahira';
    }

  }

  const email = getStoredValue('dahira_email');

  if (userEmailElement && email) {
    userEmailElement.textContent = email;
  }

  const avatar = document.getElementById('user-avatar');

  if (avatar && email) {
    avatar.textContent = email
      .split('@')[0]
      .substring(0, 2)
      .toUpperCase();
  }

  const memberWelcomeName = document.getElementById(
    'member-welcome-name'
  );

  if (memberWelcomeName && email) {
    memberWelcomeName.textContent = email.split('@')[0];
  }

  // ----------------------------------------------------------
  // 6. Messages de notification
  // ----------------------------------------------------------

  document.querySelectorAll('[data-close-message]').forEach(
    button => {

      button.addEventListener('click', () => {
        button.closest('.app-message')
          ?.classList.add('hidden');
      });

    }
  );

  const params = new URLSearchParams(window.location.search);

  if (params.get('access') === 'denied') {

    const message = document.getElementById('app-message');

    if (message) {
      message.textContent =
        "Accès refusé : cette fonctionnalité est réservée à l'administration.";

      message.classList.remove('hidden');
    }

    // Nettoyer le paramètre de l'URL.
    window.history.replaceState(
      {},
      document.title,
      window.location.pathname
    );
  }

  // ----------------------------------------------------------
  // 7. Année courante
  // ----------------------------------------------------------

  const currentYear = new Date().getFullYear();

  const yearInput = document.getElementById('generation-year');
  const paymentYear = document.getElementById('payment-year');
  const footerYear = document.getElementById('footer-year');

  if (yearInput && !yearInput.value) {
    yearInput.value = currentYear;
  }

  if (paymentYear && !paymentYear.value) {
    paymentYear.value = currentYear;
  }

  if (footerYear) {
    footerYear.textContent = currentYear;
  }

  // ----------------------------------------------------------
  // 8. Déconnexion
  // ----------------------------------------------------------

  document.querySelectorAll(
    '[data-logout], #logout-button, #logout, .logout-button'
  ).forEach(button => {

    button.addEventListener('click', event => {

      event.preventDefault();

      if (button.dataset.loggingOut === 'true') {
        return;
      }

      button.dataset.loggingOut = 'true';

      const keysToRemove = [
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
      ];

      [localStorage, sessionStorage].forEach(storage => {

        keysToRemove.forEach(key => {
          storage.removeItem(key);
        });

        for (let i = storage.length - 1; i >= 0; i--) {

          const key = storage.key(i);

          if (
            key &&
            /^(jwt|token|auth|refresh.?token|access.?token)/i
              .test(key)
          ) {
            storage.removeItem(key);
          }

        }

      });

      window.location.replace('/login.html');

    });

  });

  // ----------------------------------------------------------
  // 9. Diagnostic temporaire du rôle
  // ----------------------------------------------------------

  console.info('===== DAHIRA : CONTRÔLE DU RÔLE =====');
  console.info('Rôle détecté :', role || 'NON IDENTIFIÉ');
  console.info('Accès administrateur :', isAdmin);
  console.info('Accès membre :', isMember);
  console.info('Jeton présent :', Boolean(token));
  console.info('====================================');


});
