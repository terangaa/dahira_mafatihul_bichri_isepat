/* =========================================================
   DAHIRA MAFTIHOUL BICHRI
   GESTION COMPLETE DES EVENEMENTS
   CRUD : CREATE - READ - UPDATE - DELETE
========================================================= */

document.addEventListener("DOMContentLoaded", () => {

  "use strict";

  // =====================================================
  // CONFIGURATION
  // =====================================================

  const API_URL = "/api/events";

  let events = [];
  let eventToDelete = null;
  let messageTimeout = null;

  const el = {
    list: document.getElementById("events-list"),

    total: document.getElementById("total-events"),
    upcoming: document.getElementById("upcoming-events"),

    add: document.getElementById("new-event"),
    refresh: document.getElementById("refresh-events"),

    modal: document.getElementById("event-modal"),
    form: document.getElementById("event-form"),
    modalTitle: document.getElementById("modal-title"),

    id: document.getElementById("event-id"),
    name: document.getElementById("event-title"),
    date: document.getElementById("event-date"),
    location: document.getElementById("event-location"),
    description: document.getElementById("event-description"),

    save: document.getElementById("save-event"),
    close: document.getElementById("close-event-modal"),
    cancel: document.getElementById("cancel-event"),

    deleteModal: document.getElementById("delete-event-modal"),
    cancelDelete: document.getElementById("cancel-event-delete"),
    confirmDelete: document.getElementById("confirm-event-delete"),

    message: document.getElementById("app-message"),

    userEmail: document.getElementById("user-email"),
    userRole: document.getElementById("user-role"),
    userAvatar: document.getElementById("user-avatar")
  };


  // =====================================================
  // VERIFICATION DES ELEMENTS HTML
  // =====================================================

  if (!el.list || !el.form || !el.modal) {
    console.error(
      "Erreur : certains éléments HTML de la page événements sont introuvables."
    );
    return;
  }


  // =====================================================
  // AUTHENTIFICATION
  // =====================================================

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

    if (el.userEmail) {
      el.userEmail.textContent = email || "Utilisateur";
    }

    if (el.userAvatar) {
      el.userAvatar.textContent = email
        ? email.substring(0, 2).toUpperCase()
        : "MB";
    }

    if (el.userRole) {
      el.userRole.textContent = isAdmin()
        ? "Administrateur"
        : "Membre";
    }

    if (el.add) {
      el.add.hidden = !isAdmin();
    }
  }


  // =====================================================
  // MESSAGES
  // =====================================================

  function showMessage(message, type = "info") {

    if (!el.message) {
      console.log(message);
      return;
    }

    clearTimeout(messageTimeout);

    el.message.textContent = message;
    el.message.className = `app-message ${type}`;
    el.message.hidden = false;

    messageTimeout = setTimeout(() => {
      el.message.hidden = true;
    }, 5000);
  }


  // =====================================================
  // PROTECTION CONTRE LES INJECTIONS HTML
  // =====================================================

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


  // =====================================================
  // COMMUNICATION AVEC LE BACKEND
  // =====================================================

  async function apiRequest(url, options = {}) {

    const token = getToken();

    if (!token) {
      throw new Error(
        "Votre session a expiré. Veuillez vous reconnecter."
      );
    }

    const headers = {
      "Authorization": `Bearer ${token}`,
      ...(options.headers || {})
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
        "Accès refusé : vous ne disposez pas des autorisations nécessaires."
      );
    }

    if (response.status === 204) {
      return null;
    }

    const contentType =
      response.headers.get("content-type") || "";

    let data = null;

    if (contentType.includes("application/json")) {

      data = await response.json();

    } else {

      data = await response.text();
    }

    if (!response.ok) {

      let errorMessage = `Erreur HTTP ${response.status}`;

      if (typeof data === "object" && data !== null) {

        errorMessage =
          data.message ||
          data.error ||
          data.detail ||
          errorMessage;

      } else if (data) {

        errorMessage = data;
      }

      throw new Error(errorMessage);
    }

    return data;
  }


  // =====================================================
  // GESTION DES DATES
  // =====================================================

  function normalizeDate(value) {

    if (!value) {
      return "";
    }

    return String(value).substring(0, 10);
  }


  function formatDate(value) {

    const dateValue = normalizeDate(value);

    if (!dateValue) {
      return "Date non renseignée";
    }

    const date = new Date(`${dateValue}T00:00:00`);

    if (Number.isNaN(date.getTime())) {
      return dateValue;
    }

    return new Intl.DateTimeFormat("fr-FR", {
      day: "2-digit",
      month: "long",
      year: "numeric"
    }).format(date);
  }


  function isUpcoming(value) {

    const dateValue = normalizeDate(value);

    if (!dateValue) {
      return false;
    }

    const today = new Date();

    today.setHours(0, 0, 0, 0);

    const eventDate = new Date(`${dateValue}T00:00:00`);

    return (
      !Number.isNaN(eventDate.getTime()) &&
      eventDate >= today
    );
  }


  // =====================================================
  // CHARGEMENT
  // =====================================================

  function showLoading() {

    el.list.innerHTML = `
            <div class="loading-state">
                <div class="loader"></div>
                <p>Chargement des événements...</p>
            </div>
        `;
  }


  function showEmpty() {

    el.list.innerHTML = `
            <div class="empty-state">

                <div class="empty-state-icon">📅</div>

                <h3>Aucun événement disponible</h3>

                <p>
                    Aucun événement n'a encore été enregistré.
                </p>

            </div>
        `;
  }


  // =====================================================
  // AFFICHAGE DES EVENEMENTS
  // =====================================================

  function renderEvents() {

    // Statistiques
    if (el.total) {
      el.total.textContent = events.length;
    }

    if (el.upcoming) {
      el.upcoming.textContent =
        events.filter(event => isUpcoming(event.eventDate)).length;
    }

    if (events.length === 0) {
      showEmpty();
      return;
    }

    const admin = isAdmin();

    // Tri chronologique
    const sortedEvents = [...events].sort((a, b) => {

      return normalizeDate(a.eventDate).localeCompare(
        normalizeDate(b.eventDate)
      );
    });


    el.list.innerHTML = sortedEvents.map(event => {

      const id = escapeHTML(event.id);

      // IMPORTANT : noms correspondant à EventEntity
      const name = escapeHTML(event.name);

      const description = escapeHTML(
        event.description || "Aucune description disponible."
      );

      const location = escapeHTML(
        event.location || "Lieu non précisé"
      );

      const eventDate = normalizeDate(event.eventDate);

      const formattedDate = escapeHTML(
        formatDate(event.eventDate)
      );

      const upcoming = isUpcoming(event.eventDate);

      let day = "--";
      let month = "---";

      if (eventDate) {

        day = eventDate.substring(8, 10);

        month = new Date(`${eventDate}T00:00:00`)
          .toLocaleDateString("fr-FR", {
            month: "short"
          })
          .replace(".", "");
      }

      return `
                <article class="event-card">

                    <div class="event-card-header">

                        <div class="event-calendar-icon">

                            <strong>${escapeHTML(day)}</strong>

                            <span>${escapeHTML(month)}</span>

                        </div>

                        <div class="event-heading-info">

                            <h3>${name}</h3>

                            <span class="event-status ${upcoming ? "" : "past"}">
                                ${upcoming ? "À venir" : "Terminé"}
                            </span>

                        </div>

                    </div>

                    <p class="event-description">
                        ${description}
                    </p>

                    <div class="event-details">

                        <div class="event-detail">
                            <span>📅</span>
                            <span>${formattedDate}</span>
                        </div>

                        <div class="event-detail">
                            <span>📍</span>
                            <span>${location}</span>
                        </div>

                    </div>

                    ${admin ? `

                        <div class="event-actions">

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


  // =====================================================
  // RECUPERER LES EVENEMENTS
  // =====================================================

  async function loadEvents() {

    showLoading();

    if (el.refresh) {
      el.refresh.disabled = true;
    }

    try {

      const data = await apiRequest(API_URL, {
        method: "GET"
      });

      if (!Array.isArray(data)) {
        throw new Error(
          "Le serveur n'a pas retourné une liste d'événements valide."
        );
      }

      events = data;

      renderEvents();

    } catch (error) {

      console.error(
        "Erreur lors du chargement des événements :",
        error
      );

      el.list.innerHTML = `
                <div class="empty-state">

                    <div class="empty-state-icon">⚠️</div>

                    <h3>Impossible de charger les événements</h3>

                    <p>${escapeHTML(error.message)}</p>

                    <button
                        type="button"
                        class="btn-refresh"
                        id="retry-events">
                        ↻ Réessayer
                    </button>

                </div>
            `;

      showMessage(error.message, "error");

    } finally {

      if (el.refresh) {
        el.refresh.disabled = false;
      }
    }
  }


  // =====================================================
  // OUVRIR LE FORMULAIRE
  // =====================================================

  function openModal(event = null) {

    if (!isAdmin()) {

      showMessage(
        "Vous n'avez pas le droit de gérer les événements.",
        "error"
      );

      return;
    }

    el.form.reset();

    if (event) {

      el.modalTitle.textContent = "Modifier un événement";

      el.id.value = event.id ?? "";

      // Propriétés correctes du backend
      el.name.value = event.name ?? "";

      el.date.value = normalizeDate(event.eventDate);

      el.location.value = event.location ?? "";

      el.description.value = event.description ?? "";

    } else {

      el.modalTitle.textContent = "Ajouter un événement";

      el.id.value = "";

    }

    el.modal.hidden = false;

    setTimeout(() => {
      el.name.focus();
    }, 100);
  }


  // =====================================================
  // FERMER LE FORMULAIRE
  // =====================================================

  function closeModal() {

    el.modal.hidden = true;

    el.form.reset();

    el.id.value = "";
  }


  // =====================================================
  // AJOUTER / MODIFIER UN EVENEMENT
  // =====================================================

  async function saveEvent(event) {

    event.preventDefault();

    if (!isAdmin()) {

      showMessage("Accès refusé.", "error");

      return;
    }

    const id = el.id.value.trim();

    const name = el.name.value.trim();

    const eventDate = el.date.value;

    const location = el.location
      ? el.location.value.trim()
      : "";

    const description = el.description
      ? el.description.value.trim()
      : "";


    // Validation côté navigateur
    if (!name) {

      showMessage(
        "Le nom de l'événement est obligatoire.",
        "error"
      );

      el.name.focus();

      return;
    }

    if (!eventDate) {

      showMessage(
        "La date de l'événement est obligatoire.",
        "error"
      );

      el.date.focus();

      return;
    }


    // IMPORTANT :
    // Les noms correspondent à EventEntity.java
    const payload = {
      name: name,
      eventDate: eventDate,
      description: description,
      location: location
    };


    const editing = Boolean(id);

    const url = editing
      ? `${API_URL}/${encodeURIComponent(id)}`
      : API_URL;


    el.save.disabled = true;

    el.save.textContent = "Enregistrement...";


    try {

      await apiRequest(url, {

        method: editing ? "PUT" : "POST",

        body: JSON.stringify(payload)

      });


      closeModal();

      showMessage(
        editing
          ? "L'événement a été modifié avec succès."
          : "L'événement a été ajouté avec succès.",
        "success"
      );

      await loadEvents();


    } catch (error) {

      console.error(
        "Erreur lors de l'enregistrement :",
        error
      );

      showMessage(error.message, "error");


    } finally {

      el.save.disabled = false;

      el.save.textContent = "Enregistrer";
    }
  }


  // =====================================================
  // MODIFIER UN EVENEMENT
  // =====================================================

  function editEvent(id) {

    const event = events.find(
      item => String(item.id) === String(id)
    );

    if (!event) {

      showMessage(
        "Événement introuvable.",
        "error"
      );

      return;
    }

    openModal(event);
  }


  // =====================================================
  // OUVRIR LA CONFIRMATION DE SUPPRESSION
  // =====================================================

  function openDeleteModal(id) {

    if (!isAdmin()) {

      showMessage("Accès refusé.", "error");

      return;
    }

    eventToDelete = events.find(
      item => String(item.id) === String(id)
    );

    if (!eventToDelete) {

      showMessage(
        "Événement introuvable.",
        "error"
      );

      return;
    }

    el.deleteModal.hidden = false;
  }


  // =====================================================
  // FERMER LA CONFIRMATION
  // =====================================================

  function closeDeleteModal() {

    el.deleteModal.hidden = true;

    eventToDelete = null;
  }


  // =====================================================
  // SUPPRIMER UN EVENEMENT
  // =====================================================

  async function deleteEvent() {

    if (!isAdmin() || !eventToDelete) {
      return;
    }

    el.confirmDelete.disabled = true;

    el.confirmDelete.textContent = "Suppression...";

    try {

      await apiRequest(
        `${API_URL}/${encodeURIComponent(eventToDelete.id)}`,
        {
          method: "DELETE"
        }
      );

      closeDeleteModal();

      showMessage(
        "L'événement a été supprimé avec succès.",
        "success"
      );

      await loadEvents();


    } catch (error) {

      console.error(
        "Erreur lors de la suppression :",
        error
      );

      showMessage(error.message, "error");


    } finally {

      el.confirmDelete.disabled = false;

      el.confirmDelete.textContent = "Supprimer";
    }
  }


  // =====================================================
  // EVENEMENTS HTML
  // =====================================================

  if (el.add) {

    el.add.addEventListener("click", () => {
      openModal();
    });
  }


  if (el.refresh) {

    el.refresh.addEventListener("click", () => {
      loadEvents();
    });
  }


  el.form.addEventListener("submit", saveEvent);


  if (el.close) {

    el.close.addEventListener("click", closeModal);
  }


  if (el.cancel) {

    el.cancel.addEventListener("click", closeModal);
  }


  if (el.cancelDelete) {

    el.cancelDelete.addEventListener(
      "click",
      closeDeleteModal
    );
  }


  if (el.confirmDelete) {

    el.confirmDelete.addEventListener(
      "click",
      deleteEvent
    );
  }


  // Actions sur les cartes
  el.list.addEventListener("click", event => {

    const retryButton = event.target.closest("#retry-events");

    if (retryButton) {
      loadEvents();
      return;
    }

    const button = event.target.closest(
      "button[data-action]"
    );

    if (!button) {
      return;
    }

    const id = button.dataset.id;

    if (button.dataset.action === "edit") {
      editEvent(id);
    }

    if (button.dataset.action === "delete") {
      openDeleteModal(id);
    }
  });


  // Fermer en cliquant à l'extérieur
  el.modal.addEventListener("click", event => {

    if (event.target === el.modal) {
      closeModal();
    }
  });


  if (el.deleteModal) {

    el.deleteModal.addEventListener("click", event => {

      if (event.target === el.deleteModal) {
        closeDeleteModal();
      }
    });
  }


  // Fermer avec Echap
  document.addEventListener("keydown", event => {

    if (event.key === "Escape") {

      closeModal();

      closeDeleteModal();
    }
  });


  // =====================================================
  // INITIALISATION
  // =====================================================

  updateUserInformation();

  loadEvents();

});