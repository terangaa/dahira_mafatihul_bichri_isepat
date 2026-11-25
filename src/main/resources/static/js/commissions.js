/* =========================================
   GESTION DES COMMISSIONS
   DAHIRA MAFATIHoul BICHRI
========================================= */

document.addEventListener("DOMContentLoaded", () => {

  "use strict";


  // =====================================
  // CONFIGURATION
  // =====================================

  const API_URL = "/api/commissions";

  const elements = {
    list: document.getElementById("commissions-list"),
    total: document.getElementById("total-commissions"),

    addButton: document.getElementById("add-commission-button"),
    refreshButton: document.getElementById("refresh-commissions"),

    modal: document.getElementById("commission-modal"),
    modalTitle: document.getElementById("modal-title"),
    form: document.getElementById("commission-form"),

    id: document.getElementById("commission-id"),
    name: document.getElementById("commission-name"),
    description: document.getElementById("commission-description"),

    saveButton: document.getElementById("save-commission"),
    closeButton: document.getElementById("close-commission-modal"),
    cancelButton: document.getElementById("cancel-commission"),

    deleteModal: document.getElementById("delete-modal"),
    cancelDelete: document.getElementById("cancel-delete"),
    confirmDelete: document.getElementById("confirm-delete"),

    message: document.getElementById("app-message"),

    userEmail: document.getElementById("user-email"),
    userRole: document.getElementById("user-role"),
    userAvatar: document.getElementById("user-avatar")
  };


  let commissions = [];
  let commissionToDelete = null;
  let messageTimeout = null;


  // =====================================
  // AUTHENTIFICATION
  // =====================================

  function getToken() {
    return localStorage.getItem("dahira_token");
  }


  function getRole() {
    return (
      localStorage.getItem("dahira_role") || ""
    ).toUpperCase();
  }


  function isAdmin() {
    return getRole() === "ADMIN";
  }


  function updateUserInformation() {

    const email = localStorage.getItem("dahira_email");

    if (email && elements.userEmail) {
      elements.userEmail.textContent = email;
    }

    if (elements.userRole) {
      elements.userRole.textContent = isAdmin()
        ? "Administrateur"
        : "Membre";
    }

    if (elements.userAvatar) {

      const initials = email
        ? email.substring(0, 2).toUpperCase()
        : "MB";

      elements.userAvatar.textContent = initials;
    }
  }


  function updatePermissions() {

    const admin = isAdmin();

    if (elements.addButton) {
      elements.addButton.hidden = !admin;
    }

    // Ne pas modifier ici les autres éléments .admin-only :
    // leur visibilité est gérée par app.js.
  }


  // =====================================
  // MESSAGES
  // =====================================

  function showMessage(message, type = "info") {

    if (!elements.message) return;

    clearTimeout(messageTimeout);

    elements.message.textContent = message;
    elements.message.className = `app-message ${type}`;
    elements.message.hidden = false;

    messageTimeout = setTimeout(() => {
      elements.message.hidden = true;
    }, 5000);
  }


  function hideMessage() {

    if (!elements.message) return;

    elements.message.hidden = true;
  }


  // =====================================
  // REQUÊTES HTTP
  // =====================================

  async function apiRequest(url, options = {}) {

    const token = getToken();

    if (!token) {
      throw new Error(
        "Votre session a expiré. Veuillez vous reconnecter."
      );
    }

    const headers = {
      "Authorization": `Bearer ${token}`,
      ...options.headers
    };

    if (options.body) {
      headers["Content-Type"] = "application/json";
    }

    const response = await fetch(url, {
      ...options,
      headers
    });

    if (response.status === 401) {
      throw new Error(
        "Session expirée. Veuillez vous reconnecter."
      );
    }

    if (response.status === 403) {
      throw new Error(
        "Accès refusé. Vous n'avez pas les autorisations nécessaires."
      );
    }

    if (response.status === 204) {
      return null;
    }

    const contentType = response.headers.get("content-type") || "";

    let data = null;

    if (contentType.includes("application/json")) {
      data = await response.json();
    } else {
      data = await response.text();
    }

    if (!response.ok) {

      const errorMessage =
        typeof data === "object" && data !== null
          ? data.message || data.error || data.detail
          : data;

      throw new Error(
        errorMessage || `Erreur HTTP ${response.status}`
      );
    }

    return data;
  }


  // =====================================
  // AFFICHAGE DU CHARGEMENT
  // =====================================

  function showLoading() {

    elements.list.innerHTML = `
            <div class="loading-state">
                <div class="loader"></div>
                <p>Chargement des commissions...</p>
            </div>
        `;
  }


  // =====================================
  // AFFICHAGE VIDE
  // =====================================

  function showEmptyState() {

    elements.list.innerHTML = `
            <div class="empty-state">

                <div class="empty-state-icon">🏢</div>

                <h3>Aucune commission disponible</h3>

                <p>
                    Aucune commission n'a encore été enregistrée.
                </p>

            </div>
        `;
  }


  // =====================================
  // SECURISATION DU TEXTE HTML
  // =====================================

  function escapeHTML(value) {

    return String(value ?? "").replace(
      /[&<>"']/g,
      character => ({
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        '"': "&quot;",
        "'": "&#039;"
      })[character]
    );
  }


  // =====================================
  // AFFICHAGE DES COMMISSIONS
  // =====================================

  function renderCommissions() {

    elements.total.textContent = commissions.length;

    if (!commissions.length) {
      showEmptyState();
      return;
    }

    const admin = isAdmin();

    elements.list.innerHTML = commissions.map(commission => {

      const id = Number(commission.id);

      const name = escapeHTML(commission.name);

      const description = commission.description
        ? escapeHTML(commission.description)
        : "Aucune description disponible pour cette commission.";

      return `

                <article class="commission-card">

                    <div class="commission-card-header">

                        <div class="commission-icon">
                            🏢
                        </div>

                        <div>
                            <h3>${name}</h3>
                        </div>

                    </div>

                    <p class="commission-description">
                        ${description}
                    </p>

                    <div class="commission-date">
                        Commission n° ${id}
                    </div>

                    ${admin ? `

                        <div class="commission-actions">

                            <button
                                type="button"
                                class="btn-edit"
                                data-action="edit"
                                data-id="${id}">
                                ✏️ Modifier
                            </button>

                            <button
                                type="button"
                                class="btn-delete"
                                data-action="delete"
                                data-id="${id}">
                                🗑️ Supprimer
                            </button>

                        </div>

                    ` : ""}

                </article>

            `;

    }).join("");

  }


  // =====================================
  // CHARGER LES COMMISSIONS
  // =====================================

  async function loadCommissions() {

    showLoading();
    hideMessage();

    try {

      const data = await apiRequest(API_URL, {
        method: "GET"
      });

      if (!Array.isArray(data)) {
        throw new Error(
          "Le serveur a retourné un format de données invalide."
        );
      }

      commissions = data;

      renderCommissions();

    } catch (error) {

      console.error(
        "Erreur chargement commissions :",
        error
      );

      commissions = [];

      elements.total.textContent = "0";

      elements.list.innerHTML = `
                <div class="empty-state">

                    <div class="empty-state-icon">⚠️</div>

                    <h3>Impossible de charger les commissions</h3>

                    <p>${escapeHTML(error.message)}</p>

                </div>
            `;

      showMessage(error.message, "error");
    }
  }


  // =====================================
  // OUVRIR LA MODALE
  // =====================================

  function openModal(commission = null) {

    if (!isAdmin()) {
      showMessage(
        "Vous n'avez pas le droit de gérer les commissions.",
        "error"
      );
      return;
    }

    elements.form.reset();

    if (commission) {

      elements.modalTitle.textContent =
        "Modifier une commission";

      elements.id.value = commission.id;
      elements.name.value = commission.name || "";

      elements.description.value =
        commission.description || "";

    } else {

      elements.modalTitle.textContent =
        "Ajouter une commission";

      elements.id.value = "";
    }

    elements.modal.hidden = false;

    setTimeout(() => {
      elements.name.focus();
    }, 100);
  }


  // =====================================
  // FERMER LA MODALE
  // =====================================

  function closeModal() {
    elements.modal.hidden = true;
    elements.form.reset();
  }


  // =====================================
  // AJOUTER / MODIFIER
  // =====================================

  async function saveCommission(event) {

    event.preventDefault();

    if (!isAdmin()) {
      showMessage("Accès refusé.", "error");
      return;
    }

    const id = elements.id.value.trim();

    const name = elements.name.value.trim();

    const description =
      elements.description.value.trim();

    if (!name) {
      showMessage(
        "Le nom de la commission est obligatoire.",
        "error"
      );
      elements.name.focus();
      return;
    }

    const commission = {
      name: name,
      description: description
    };

    const isEditing = Boolean(id);

    const url = isEditing
      ? `${API_URL}/${encodeURIComponent(id)}`
      : API_URL;

    const method = isEditing ? "PUT" : "POST";

    elements.saveButton.disabled = true;

    elements.saveButton.textContent = "Enregistrement...";

    try {

      await apiRequest(url, {
        method: method,
        body: JSON.stringify(commission)
      });

      closeModal();

      showMessage(
        isEditing
          ? "Commission modifiée avec succès."
          : "Commission ajoutée avec succès.",
        "success"
      );

      await loadCommissions();

    } catch (error) {

      console.error(
        "Erreur enregistrement commission :",
        error
      );

      showMessage(error.message, "error");

    } finally {

      elements.saveButton.disabled = false;

      elements.saveButton.textContent = "Enregistrer";
    }
  }


  // =====================================
  // MODIFIER UNE COMMISSION
  // =====================================

  function editCommission(id) {

    const commission = commissions.find(
      item => Number(item.id) === Number(id)
    );

    if (!commission) {
      showMessage(
        "Commission introuvable.",
        "error"
      );
      return;
    }

    openModal(commission);
  }


  // =====================================
  // OUVRIR CONFIRMATION SUPPRESSION
  // =====================================

  function openDeleteModal(id) {

    if (!isAdmin()) {
      showMessage("Accès refusé.", "error");
      return;
    }

    const commission = commissions.find(
      item => Number(item.id) === Number(id)
    );

    if (!commission) {
      showMessage(
        "Commission introuvable.",
        "error"
      );
      return;
    }

    commissionToDelete = commission;

    elements.deleteModal.hidden = false;
  }


  // =====================================
  // FERMER CONFIRMATION
  // =====================================

  function closeDeleteModal() {

    elements.deleteModal.hidden = true;

    commissionToDelete = null;
  }


  // =====================================
  // SUPPRIMER UNE COMMISSION
  // =====================================

  async function deleteCommission() {

    if (!isAdmin() || !commissionToDelete) {
      return;
    }

    const id = commissionToDelete.id;

    elements.confirmDelete.disabled = true;

    elements.confirmDelete.textContent = "Suppression...";

    try {

      await apiRequest(
        `${API_URL}/${encodeURIComponent(id)}`,
        {
          method: "DELETE"
        }
      );

      closeDeleteModal();

      showMessage(
        "Commission supprimée avec succès.",
        "success"
      );

      await loadCommissions();

    } catch (error) {

      console.error(
        "Erreur suppression commission :",
        error
      );

      showMessage(error.message, "error");

    } finally {

      elements.confirmDelete.disabled = false;

      elements.confirmDelete.textContent = "Supprimer";
    }
  }


  // =====================================
  // EVENEMENTS
  // =====================================

  // Ajouter
  elements.addButton.addEventListener("click", () => {
    openModal();
  });


  // Actualiser
  elements.refreshButton.addEventListener("click", () => {
    loadCommissions();
  });


  // Enregistrer
  elements.form.addEventListener("submit", saveCommission);


  // Fermer formulaire
  elements.closeButton.addEventListener("click", closeModal);

  elements.cancelButton.addEventListener("click", closeModal);


  // Annuler suppression
  elements.cancelDelete.addEventListener(
    "click",
    closeDeleteModal
  );


  // Confirmer suppression
  elements.confirmDelete.addEventListener(
    "click",
    deleteCommission
  );


  // Actions sur les cartes
  elements.list.addEventListener("click", event => {

    const button = event.target.closest("button[data-action]");

    if (!button) return;

    const id = button.dataset.id;

    const action = button.dataset.action;

    if (action === "edit") {
      editCommission(id);
    }

    if (action === "delete") {
      openDeleteModal(id);
    }

  });


  // Fermer en cliquant sur le fond
  elements.modal.addEventListener("click", event => {

    if (event.target === elements.modal) {
      closeModal();
    }

  });


  elements.deleteModal.addEventListener("click", event => {

    if (event.target === elements.deleteModal) {
      closeDeleteModal();
    }

  });


  // Fermer avec Escape
  document.addEventListener("keydown", event => {

    if (event.key === "Escape") {
      closeModal();
      closeDeleteModal();
    }

  });


  // =====================================
  // INITIALISATION
  // =====================================

  updateUserInformation();

  updatePermissions();

  loadCommissions();

});