/* =========================================================
   DAHIRA MB — MEMBERS.JS
   Connecté à l'API Spring Boot (avec JWT)
========================================================= */

document.addEventListener("DOMContentLoaded", () => {

  // ⚠️ L'URL de ton API Spring Boot
  const API_BASE_URL = "http://localhost:8012/api";

  let members = [];
  let commissions = [];

  // --- SÉLECTION DES ÉLÉMENTS DU DOM ---
  const tbody = document.getElementById("members-table-body");
  const statTotal = document.getElementById("total-members");
  const statActive = document.getElementById("active-members");
  const statInactive = document.getElementById("inactive-members");
  const statCommissions = document.getElementById("total-commissions");
  const countLabel = document.getElementById("members-count");

  const memberModal = document.getElementById("member-modal");
  const memberForm = document.getElementById("member-form");
  const btnAddMember = document.getElementById("add-member-button");
  const modalTitle = memberModal.querySelector("h2");

  // ==========================================
  // GESTION DU TOKEN JWT
  // ==========================================
  function getAuthHeaders() {
    // On récupère le token (adapté selon comment ton login le stocke)
    const token = localStorage.getItem("token") || sessionStorage.getItem("token") || localStorage.getItem("jwt");

    const headers = {
      'Content-Type': 'application/json'
    };

    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    return headers;
  }

  // ==========================================
  // INITIALISATION
  // ==========================================
  async function init() {
    console.log("🚀 Démarrage de l'application...");
    await fetchMembers();
    setupEventListeners();
  }

  // ==========================================
  // CHARGEMENT DES MEMBRES DEPUIS L'API
  // ==========================================
  async function fetchMembers() {
    tbody.innerHTML = `<tr><td colspan="7" class="loading-cell">Chargement des membres...</td></tr>`;

    try {
      // On envoie les headers avec le Token
      const response = await fetch(`${API_BASE_URL}/members`, {
        method: 'GET',
        headers: getAuthHeaders()
      });

      // Gestion de la sécurité (Token expiré ou non connecté)
      if (response.status === 401 || response.status === 403) {
        console.warn("Accès refusé. Redirection vers la page de connexion.");
        // window.location.href = "/login.html"; // Décommente pour rediriger
        throw new Error("Non autorisé (401/403). Connectez-vous.");
      }

      if (!response.ok) {
        throw new Error(`Erreur HTTP: ${response.status}`);
      }

      const result = await response.json();
      console.log("✅ Données reçues de Spring Boot :", result);

      // On s'adapte au format (Tableau direct ou objet encapsulé)
      members = Array.isArray(result) ? result : (result.content || result.data || []);

      // On extrait les commissions UNIQUES
      const uniqueComms = [...new Set(members.map(m => m.commission).filter(c => c && c.trim() !== ""))];
      commissions = uniqueComms;

      populateSelects();
      renderTable();
      updateStats();

      console.log(`✅ ${members.length} membres affichés.`);

    } catch (error) {
      console.error("❌ ERREUR CRITIQUE :", error.message);
      tbody.innerHTML = `<tr><td colspan="7" class="loading-cell" style="color: #dc2626; font-weight: bold;">
                Impossible de contacter Spring Boot.<br>
                <span style="font-size:11px; font-weight:normal;">${error.message}</span>
            </td></tr>`;
    }
  }

  // ==========================================
  // REMPLISSAGE DES MENUS DÉROULANTS
  // ==========================================
  function populateSelects() {
    const formSelect = document.getElementById("commission");
    const filterSelect = document.getElementById("commission-filter");

    filterSelect.innerHTML = '<option value="">Toutes les commissions</option>';
    formSelect.innerHTML = '<option value="">Sélectionner une commission</option>';

    commissions.forEach(commName => {
      filterSelect.innerHTML += `<option value="${commName}">${commName}</option>`;
      formSelect.innerHTML += `<option value="${commName}">${commName}</option>`;
    });

    statCommissions.textContent = commissions.length;
  }

  // ==========================================
  // AFFICHAGE DU TABLEAU
  // ==========================================
  function renderTable() {
    tbody.innerHTML = "";
    countLabel.textContent = `${members.length} membre(s)`;

    if (members.length === 0) {
      document.getElementById("empty-members").style.display = "block";
      tbody.parentElement.style.display = "none";
      return;
    }

    document.getElementById("empty-members").style.display = "none";
    tbody.parentElement.style.display = "table";

    members.forEach(member => {
      const tr = document.createElement("tr");

      // Mapping des champs Spring Boot
      const fName = member.firstName || '';
      const lName = member.lastName || '';
      const phone = member.phone || '-';
      const comm = member.commission || '-';
      const filiere = member.filiere || '-';
      const promo = member.promotion || '-';
      const statut = (member.status || 'ACTIVE').toUpperCase();

      // Badge de statut
      let badgeClass = "status-active";
      let badgeText = "Actif";
      if (statut === "INACTIVE") { badgeClass = "status-inactive"; badgeText = "Inactif"; }
      else if (statut === "PENDING") { badgeClass = "status-pending"; badgeText = "En attente"; }

      const initials = (fName.charAt(0) + lName.charAt(0)).toUpperCase() || "?";

      tr.innerHTML = `
                <td>
                    <div class="member-cell">
                        <div class="member-avatar">${initials}</div>
                        <div class="member-name">
                            <strong>${fName} ${lName}</strong>
                        </div>
                    </div>
                </td>
                <td>${phone}</td>
                <td>${comm}</td>
                <td>${filiere}</td>
                <td>${promo}</td>
                <td><span class="badge ${badgeClass}">${badgeText}</span></td>
                <td>
                    <button class="action-btn btn-edit" data-id="${member.id}" title="Modifier">✎</button>
                    <button class="action-btn btn-delete" data-id="${member.id}" title="Supprimer" style="color: #dc2626;">🗑</button>
                </td>
            `;
      tbody.appendChild(tr);
    });

    // Attacher les événements
    document.querySelectorAll(".btn-edit").forEach(btn => {
      btn.addEventListener("click", () => openEditModal(btn.dataset.id));
    });
    document.querySelectorAll(".btn-delete").forEach(btn => {
      btn.addEventListener("click", () => deleteMember(btn.dataset.id));
    });
  }

  // ==========================================
  // STATISTIQUES
  // ==========================================
  function updateStats() {
    statTotal.textContent = members.length;
    statActive.textContent = members.filter(m => m.status === "ACTIVE").length;
    statInactive.textContent = members.filter(m => m.status === "INACTIVE").length;
  }

  // ==========================================
  // GESTION DES ÉVÉNEMENTS (BOUTONS)
  // ==========================================
  function setupEventListeners() {
    btnAddMember.addEventListener("click", () => {
      memberForm.reset();
      document.getElementById("member-id").value = "";
      modalTitle.textContent = "Ajouter un membre";
      memberModal.classList.add("active");
    });

    document.querySelectorAll("[data-close]").forEach(btn => {
      btn.addEventListener("click", () => {
        document.getElementById(btn.getAttribute("data-close")).classList.remove("active");
      });
    });

    document.querySelectorAll(".modal-overlay").forEach(overlay => {
      overlay.addEventListener("click", (e) => {
        if (e.target === overlay) overlay.classList.remove("active");
      });
    });

    memberForm.addEventListener("submit", handleFormSubmit);

    // Actualiser
    document.getElementById("refresh-members").addEventListener("click", async (e) => {
      const btn = e.currentTarget;
      const originalContent = btn.innerHTML;
      btn.innerHTML = "⏳ Chargement...";
      btn.disabled = true;
      await fetchMembers();
      btn.innerHTML = originalContent;
      btn.disabled = false;
    });
  }

  // ==========================================
  // AJOUT / MODIFICATION VIA L'API
  // ==========================================
  async function handleFormSubmit(e) {
    e.preventDefault();
    const id = document.getElementById("member-id").value;

    const memberData = {
      firstName: document.getElementById("firstName").value.trim(),
      lastName: document.getElementById("lastName").value.trim(),
      phone: document.getElementById("phone").value.trim(),
      commission: document.getElementById("commission").value,
      filiere: document.getElementById("filiere").value.trim(),
      promotion: document.getElementById("promotion").value.trim(),
      registrationDate: document.getElementById("registration-date").value || new Date().toISOString().split('T')[0],
      status: document.getElementById("member-status").value
    };

    const btnSubmit = document.getElementById("save-member-button");
    const originalText = btnSubmit.textContent;
    btnSubmit.textContent = "Enregistrement...";
    btnSubmit.disabled = true;

    try {
      const url = id ? `${API_BASE_URL}/members/${id}` : `${API_BASE_URL}/members`;
      const method = id ? 'PUT' : 'POST';

      const response = await fetch(url, {
        method: method,
        headers: getAuthHeaders(), // <-- Token inclus
        body: JSON.stringify(memberData)
      });

      if (response.status === 401 || response.status === 403) {
        alert("Vous n'avez pas les droits pour effectuer cette action.");
        return;
      }

      if (!response.ok) throw new Error("Erreur lors de l'enregistrement");

      memberModal.classList.remove("active");
      memberForm.reset();
      await fetchMembers();

    } catch (error) {
      console.error("Erreur enregistrement :", error);
      alert("Erreur lors de l'enregistrement. Vérifie la console.");
    } finally {
      btnSubmit.textContent = originalText;
      btnSubmit.disabled = false;
    }
  }

  // ==========================================
  // OUVRIR MODALE DE MODIFICATION
  // ==========================================
  function openEditModal(id) {
    const member = members.find(m => String(m.id) === String(id));
    if (!member) return;

    document.getElementById("member-id").value = member.id;
    document.getElementById("firstName").value = member.firstName || '';
    document.getElementById("lastName").value = member.lastName || '';
    document.getElementById("phone").value = member.phone || '';
    document.getElementById("commission").value = member.commission || '';
    document.getElementById("filiere").value = member.filiere || '';
    document.getElementById("promotion").value = member.promotion || '';
    document.getElementById("registration-date").value = member.registrationDate || '';
    document.getElementById("member-status").value = member.status || 'ACTIVE';

    modalTitle.textContent = "Modifier le membre";
    memberModal.classList.add("active");
  }

  // ==========================================
  // SUPPRESSION VIA L'API
  // ==========================================
  async function deleteMember(id) {
    if (confirm("Êtes-vous sûr de vouloir supprimer ce membre ?")) {
      try {
        const response = await fetch(`${API_BASE_URL}/members/${id}`, {
          method: 'DELETE',
          headers: getAuthHeaders() // <-- Token inclus
        });

        if (response.status === 401 || response.status === 403) {
          alert("Vous n'avez pas les droits pour supprimer ce membre.");
          return;
        }

        if (!response.ok) throw new Error("Erreur lors de la suppression");

        await fetchMembers();
      } catch (error) {
        console.error("Erreur suppression :", error);
        alert("Erreur lors de la suppression.");
      }
    }
  }

  // LANCEMENT
  init();
});