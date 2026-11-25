document.addEventListener('DOMContentLoaded', () => {
  'use strict';

  const API_BASE = '/api';
  let membersList = [];
  let paymentsList = [];

  // ==========================================================
  // 1. GESTION DU TOKEN ET DES RÔLES
  // ==========================================================
  function getStoredValue(key) {
    return localStorage.getItem(key) || sessionStorage.getItem(key) || '';
  }

  function getToken() {
    return getStoredValue('dahira_token')
      || getStoredValue('accessToken')
      || getStoredValue('token')
      || getStoredValue('jwt')
      || '';
  }

  function getAuthHeaders() {
    return {
      'Authorization': `Bearer ${getToken()}`,
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    };
  }

  function getRole() {
    let role = getStoredValue('dahira_role').trim().toUpperCase();
    if (role.startsWith('ROLE_')) role = role.substring(5);
    if (role) return role;

    const token = getToken();
    try {
      if (!token) return '';
      const payload = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
      const claims = JSON.parse(atob(payload.padEnd(Math.ceil(payload.length / 4) * 4, '=')));
      const candidates = [claims.role, claims.roles, claims.authorities];
      for (const candidate of candidates) {
        if (!candidate) continue;
        const values = Array.isArray(candidate) ? candidate : [candidate];
        for (const value of values) {
          const normalized = (typeof value === 'string' ? value : value?.authority || '').toUpperCase().replace(/^ROLE_/, '').trim();
          if (['ADMIN', 'MEMBER', 'ADMINISTRATEUR', 'TRESORIER', 'ADMIN_PRINCIPAL'].includes(normalized)) return normalized;
        }
      }
    } catch (e) { console.warn(e); }
    return '';
  }

  // ==========================================================
  // 2. UTILITAIRES
  // ==========================================================
  function formatFCFA(value) {
    const amount = Number(value ?? 0);
    return new Intl.NumberFormat('fr-FR', { maximumFractionDigits: 0 }).format(Number.isFinite(amount) ? amount : 0) + ' FCFA';
  }

  function setText(id, value) {
    const el = document.getElementById(id);
    if (el) el.textContent = value;
  }

  function getMonthName(monthNumber) {
    const months = { 1: "Janvier", 2: "Février", 3: "Mars", 4: "Avril", 5: "Mai", 6: "Juin", 7: "Juillet", 8: "Août", 9: "Septembre", 10: "Octobre", 11: "Novembre", 12: "Décembre" };
    return months[parseInt(monthNumber)] || monthNumber;
  }

  // ==========================================================
  // 3. CHARGEMENT DES DONNÉES
  // ==========================================================
  async function loadAllData() {
    const role = getRole();
    const allowedRoles = ['ADMIN', 'MEMBER', 'ADMINISTRATEUR', 'TRESORIER', 'ADMIN_PRINCIPAL'];
    if (!allowedRoles.includes(role)) return;

    try {
      const resMembers = await fetch(`${API_BASE}/members`, { headers: getAuthHeaders() });
      if (resMembers.ok) {
        membersList = await resMembers.json();
        console.log("✅ Membres chargés :", membersList.length);
        populateMembersSelect();
      }

      const resPayments = await fetch(`${API_BASE}/monthly-payments`, { headers: getAuthHeaders() });
      if (resPayments.ok) {
        paymentsList = await resPayments.json();
        console.log("✅ Cotisations chargées :", paymentsList.length);
        renderTable(paymentsList);
        updateCount(paymentsList.length);
        calculateTotalsFromPayments(paymentsList);
      }
    } catch (error) {
      console.error('Erreur de chargement :', error);
    }
  }

  async function apiGet(endpoint) {
    const token = getToken();
    if (!token) throw new Error('Non connecté');
    const response = await fetch(API_BASE + endpoint, {
      method: 'GET',
      headers: { 'Authorization': 'Bearer ' + token, 'Accept': 'application/json' }
    });
    if (!response.ok) throw new Error(`Erreur API ${response.status}`);
    return response.json();
  }

  // ==========================================================
  // 4. CALCUL DES TOTAUX À PARTIR DU TABLEAU
  // ==========================================================
  function calculateTotalsFromPayments(payments) {
    if (!payments || payments.length === 0) {
      setText('finance-total-collected', '0 FCFA');
      setText('finance-personal-total', '0 FCFA');
      setText('finance-personal-remaining', '0 FCFA');
      setText('finance-monthly-count', '0');
      setText('stat-paid', '0');
      setText('stat-partial', '0');
      setText('stat-pending', '0');
      setText('stat-cancelled', '0');
      return;
    }

    let totalPaid = 0;
    let totalRemaining = 0;
    let countPaid = 0;
    let countPartial = 0;
    let countPending = 0;
    let countCancelled = 0;

    payments.forEach(p => {
      const status = (p.status || "NON_PAYEE").toUpperCase();
      const expected = Number(p.expectedAmount || 0);
      const paid = Number(p.paidAmount || 0);

      // Comptage par statut
      if (["PAID", "PAYE", "PAYÉ", "PAYEE"].includes(status)) {
        countPaid++;
      } else if (["PARTIAL", "PARTIEL"].includes(status)) {
        countPartial++;
      } else if (["CANCELLED", "ANNULE", "ANNULÉE", "ANNULEE"].includes(status)) {
        countCancelled++;
      } else {
        countPending++;
      }

      // Exclure les annulées des totaux financiers
      if (["CANCELLED", "ANNULE", "ANNULÉE", "ANNULEE"].includes(status)) return;

      totalPaid += paid;
      totalRemaining += (expected - paid);
    });

    setText('finance-total-collected', formatFCFA(totalPaid));
    setText('finance-personal-total', formatFCFA(totalPaid));
    setText('finance-personal-remaining', formatFCFA(totalRemaining));
    setText('finance-monthly-count', payments.length);
    setText('stat-paid', countPaid);
    setText('stat-partial', countPartial);
    setText('stat-pending', countPending);
    setText('stat-cancelled', countCancelled);

    console.log("📊 Totaux recalculés :", {
      totalCotisations: payments.length,
      payees: countPaid,
      partielles: countPartial,
      enAttente: countPending,
      annulees: countCancelled,
      totalPaye: totalPaid,
      totalRestant: totalRemaining
    });
  }

  // ==========================================================
  // 5. AFFICHAGE DES CARTES DE RÉSUMÉ (MEMBRE)
  // ==========================================================
  function displayMemberFinance(data) {
    const paid = Number(data.totalPaid ?? 0);
    const remaining = Number(data.totalRemaining ?? 0);
    const contributions = Number(data.totalContributions ?? 0);
    setText('finance-total-collected', formatFCFA(paid + contributions));
    setText('finance-monthly-count', '—');
    setText('finance-personal-total', formatFCFA(paid));
    setText('finance-personal-remaining', formatFCFA(remaining));
  }

  // ==========================================================
  // 6. AFFICHAGE DU TABLEAU
  // ==========================================================
  function getMemberName(payment) {
    if (payment.memberId && membersList.length > 0) {
      const found = membersList.find(m => String(m.id) === String(payment.memberId));
      if (found) {
        return `${found.firstName || ''} ${found.lastName || ''}`.trim();
      }
    }
    return payment.memberId ? `Membre #${payment.memberId}` : "Membre inconnu";
  }

  function renderTable(payments) {
    const tableBody = document.getElementById("payments-table");
    if (!tableBody) return;

    if (!payments || payments.length === 0) {
      tableBody.innerHTML = `<tr><td colspan="10" class="empty-state" style="text-align:center;">Aucune cotisation enregistrée.</td></tr>`;
      return;
    }

    tableBody.innerHTML = "";
    payments.forEach(p => {
      const memberName = getMemberName(p);
      const period = `${getMonthName(p.month)} ${p.year}`;
      const expected = Number(p.expectedAmount || 0);
      const paid = Number(p.paidAmount || 0);
      const remaining = expected - paid;
      const date = p.paymentDate ? new Date(p.paymentDate).toLocaleDateString('fr-FR') : "-";
      const status = (p.status || "NON_PAYEE").toUpperCase();

      let badge = `<span class="status-badge status-pending">En attente</span>`;
      if (["PAID", "PAYE", "PAYÉ", "PAYEE"].includes(status)) {
        badge = `<span class="status-badge status-active">Payé</span>`;
      } else if (["PARTIAL", "PARTIEL"].includes(status)) {
        badge = `<span class="status-badge status-warning">Partiel</span>`;
      } else if (["CANCELLED", "ANNULE", "ANNULÉE", "ANNULEE"].includes(status)) {
        badge = `<span class="status-badge status-inactive">Annulé</span>`;
      }

      let resteClass = "reste-cell";
      let resteText = "";

      if (["CANCELLED", "ANNULE", "ANNULÉE", "ANNULEE"].includes(status)) {
        resteClass += " reste-annule";
        resteText = "Annulé";
      } else if (remaining <= 0) {
        resteClass += " reste-solde";
        resteText = "✓ Soldé";
      } else if (paid === 0) {
        resteClass += " reste-impaye";
        resteText = `${remaining.toLocaleString('fr-FR')} FCFA`;
      } else {
        resteClass += " reste-partiel";
        resteText = `${remaining.toLocaleString('fr-FR')} FCFA`;
      }

      const tr = document.createElement("tr");
      tr.innerHTML = `
          <td><strong>${memberName}</strong></td>
          <td>${period}</td>
          <td>${expected.toLocaleString('fr-FR')} FCFA</td>
          <td style="color:#059669; font-weight:700;">${paid.toLocaleString('fr-FR')} FCFA</td>
          <td><span class="${resteClass}">${resteText}</span></td>
          <td>${date}</td>
          <td>${badge}</td>
          <td>${p.paymentMethod || "-"}</td>
          <td title="${p.observation || ''}">${(p.observation || "").substring(0, 20)}${(p.observation || "").length > 20 ? '...' : ''}</td>
          <td>
              <button class="action-btn btn-edit" data-id="${p.id}" title="Modifier">✎</button>
              <button class="action-btn btn-delete" data-id="${p.id}" title="Annuler" style="color:#dc2626;">✕</button>
          </td>
      `;
      tableBody.appendChild(tr);
    });

    document.querySelectorAll(".btn-edit").forEach(btn => btn.addEventListener("click", () => openEditPayment(btn.dataset.id)));
    document.querySelectorAll(".btn-delete").forEach(btn => btn.addEventListener("click", () => cancelPayment(btn.dataset.id)));
  }

  function updateCount(count) {
    const counter = document.getElementById("payment-total");
    if (counter) counter.textContent = `${count} cotisation${count > 1 ? 's' : ''}`;
  }

  function populateMembersSelect() {
    const select = document.getElementById("payment-member-id");
    if (!select) return;
    select.innerHTML = '<option value="">Sélectionner un membre</option>';
    membersList.forEach(m => {
      select.innerHTML += `<option value="${m.id}">${m.firstName} ${m.lastName}</option>`;
    });
  }

  // ==========================================================
  // 7. GESTION DES MODALES
  // ==========================================================
  const paymentModal = document.getElementById("payment-modal");
  const generateModal = document.getElementById("generate-modal");

  document.getElementById("new-payment")?.addEventListener("click", () => {
    document.getElementById("payment-form").reset();
    document.getElementById("payment-id").value = "";
    document.getElementById("payment-form-title").textContent = "Nouvelle cotisation";
    paymentModal.classList.add("active");
  });

  document.getElementById("generate-payments-button")?.addEventListener("click", () => {
    document.getElementById("generate-payments-form").reset();
    generateModal.classList.add("active");
  });

  document.querySelectorAll("[data-close]").forEach(btn => {
    btn.addEventListener("click", () => {
      const modalId = btn.getAttribute("data-close");
      document.getElementById(modalId)?.classList.remove("active");
    });
  });

  document.querySelectorAll(".modal-overlay").forEach(overlay => {
    overlay.addEventListener("click", (e) => {
      if (e.target === overlay) overlay.classList.remove("active");
    });
  });

  // ==========================================================
  // 8. ENREGISTREMENT (POST / PUT)
  // ==========================================================
  document.getElementById("payment-form")?.addEventListener("submit", async (e) => {
    e.preventDefault();
    const id = document.getElementById("payment-id").value;
    const dateValue = document.getElementById("payment-date").value;

    const data = {
      memberId: parseInt(document.getElementById("payment-member-id").value),
      month: parseInt(document.getElementById("payment-month").value),
      year: parseInt(document.getElementById("payment-year").value),
      expectedAmount: parseFloat(document.getElementById("payment-expected").value) || 0,
      paidAmount: parseFloat(document.getElementById("payment-paid").value) || 0,
      status: document.getElementById("payment-status").value,
      paymentMethod: document.getElementById("payment-method").value || null,
      observation: document.getElementById("payment-observation").value || null
    };

    if (dateValue) data.paymentDate = dateValue;

    try {
      const url = id ? `${API_BASE}/monthly-payments/${id}` : `${API_BASE}/monthly-payments`;
      const res = await fetch(url, {
        method: id ? 'PUT' : 'POST',
        headers: getAuthHeaders(),
        body: JSON.stringify(data)
      });

      if (!res.ok) {
        let errorMessage = `Erreur ${res.status}`;
        try {
          const errorData = await res.json();
          errorMessage = errorData.message || errorData.error || errorMessage;
        } catch {
          const errorText = await res.text();
          errorMessage = errorText || errorMessage;
        }

        if (errorMessage.toLowerCase().includes("existe déjà")) {
          alert("⚠️ Cette cotisation existe déjà.\n\nUn membre ne peut pas avoir deux cotisations pour le même mois et la même année.\n\nPour modifier une cotisation existante, utilisez le bouton ✎ (Modifier) dans le tableau.");
          paymentModal.classList.remove("active");
          loadAllData();
          return;
        }

        throw new Error(errorMessage);
      }

      alert(id ? "✅ Cotisation modifiée !" : "✅ Cotisation ajoutée !");
      paymentModal.classList.remove("active");
      loadAllData();
    } catch (err) {
      console.error(err);
      alert("Erreur : " + err.message);
    }
  });

  // ==========================================================
  // 9. GÉNÉRATION AUTOMATIQUE
  // ==========================================================
  document.getElementById("generate-payments-form")?.addEventListener("submit", async (e) => {
    e.preventDefault();
    const data = {
      month: parseInt(document.getElementById("generation-month").value),
      year: parseInt(document.getElementById("generation-year").value),
      amount: parseFloat(document.getElementById("generation-amount").value)
    };

    try {
      const res = await fetch(`${API_BASE}/monthly-payments/generate`, {
        method: 'POST',
        headers: getAuthHeaders(),
        body: JSON.stringify(data)
      });
      if (!res.ok) throw new Error(`Erreur ${res.status}`);
      const result = await res.json();
      alert(`✅ Génération réussie !\n\nCotisations créées : ${result.created}\nDéjà existantes : ${result.alreadyExists}\nMembres concernés : ${result.totalActiveMembers}`);
      generateModal.classList.remove("active");
      loadAllData();
    } catch (err) {
      alert(err.message);
    }
  });

  // ==========================================================
  // 10. MODIFIER ET ANNULER
  // ==========================================================
  function openEditPayment(id) {
    const payment = paymentsList.find(p => String(p.id) === String(id));
    if (!payment) return;

    document.getElementById("payment-id").value = payment.id;
    document.getElementById("payment-member-id").value = payment.memberId;
    document.getElementById("payment-month").value = payment.month;
    document.getElementById("payment-year").value = payment.year;
    document.getElementById("payment-expected").value = payment.expectedAmount;
    document.getElementById("payment-paid").value = payment.paidAmount || 0;
    document.getElementById("payment-date").value = payment.paymentDate ? payment.paymentDate.split('T')[0] : "";
    document.getElementById("payment-status").value = payment.status || "NON_PAYEE";
    document.getElementById("payment-method").value = payment.paymentMethod || "";
    document.getElementById("payment-observation").value = payment.observation || "";

    document.getElementById("payment-form-title").textContent = "Modifier la cotisation";
    paymentModal.classList.add("active");
  }

  async function cancelPayment(id) {
    if (!confirm("Voulez-vous vraiment annuler cette cotisation ?")) return;
    try {
      const res = await fetch(`${API_BASE}/monthly-payments/${id}/cancel`, {
        method: 'POST',
        headers: getAuthHeaders()
      });
      if (!res.ok) throw new Error("Erreur d'annulation");
      alert("✅ Cotisation annulée !");
      loadAllData();
    } catch (err) {
      alert(err.message);
    }
  }

  // ==========================================================
  // LANCEMENT
  // ==========================================================
  loadAllData();
});