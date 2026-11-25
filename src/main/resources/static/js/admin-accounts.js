"use strict";

/* =====================================================
   CONFIGURATION
===================================================== */

const API_URL = "/api";

const TOKEN_KEYS = [
    "dahira_token",
    "accessToken",
    "token",
    "jwt"
];

const ROLE_KEYS = [
    "dahira_admin_role",
    "adminRole"
];

const elements = {
    table: document.getElementById("accounts-table-body"),

    total: document.getElementById("total-accounts"),
    members: document.getElementById("member-accounts"),
    admins: document.getElementById("admin-accounts"),
    without: document.getElementById("without-account"),

    search: document.getElementById("search-account"),
    filterRole: document.getElementById("filter-role"),
    filterAccount: document.getElementById("filter-account"),

    results: document.getElementById("results-count"),

    modal: document.getElementById("account-modal"),
    form: document.getElementById("account-form"),

    modalTitle: document.getElementById("modal-title"),

    memberSelect: document.getElementById("member-select"),
    memberId: document.getElementById("account-member-id"),

    email: document.getElementById("account-email"),
    password: document.getElementById("account-password"),

    role: document.getElementById("account-role"),
    adminRole: document.getElementById("admin-role"),

    adminRoleContainer:
        document.getElementById("admin-role-container"),

    passwordHelp:
        document.getElementById("password-help"),

    saveButton:
        document.getElementById("save-account-btn"),

    message:
        document.getElementById("app-message")
};

let members = [];
let accounts = [];

let editingAccount = false;
let messageTimer = null;


/* =====================================================
   AUTHENTIFICATION
===================================================== */

function getToken() {

    for (const key of TOKEN_KEYS) {

        const token = localStorage.getItem(key);

        if (token) {
            return token;
        }
    }

    return null;
}


function getAdminRole() {

    for (const key of ROLE_KEYS) {

        const role = localStorage.getItem(key);

        if (role) {
            return role.trim().toUpperCase();
        }
    }

    return "";
}


function checkAuthorization() {

    const token = getToken();
    const role = getAdminRole();

    if (!token) {

        window.location.replace("../login.html");

        return false;
    }

    if (role !== "ADMIN_PRINCIPAL") {

        alert(
            "Accès refusé : cette page est réservée à l'administrateur principal."
        );

        window.location.replace("../dashboard.html");

        return false;
    }

    return true;
}


/* =====================================================
   API
===================================================== */

async function apiRequest(url, options = {}) {

    const token = getToken();

    const response = await fetch(url, {

        ...options,

        headers: {

            "Content-Type": "application/json",

            "Authorization": `Bearer ${token}`,

            ...(options.headers || {})

        }

    });

    if (response.status === 401) {

        TOKEN_KEYS.forEach(key =>
            localStorage.removeItem(key)
        );

        window.location.replace("../login.html");

        throw new Error("Session expirée.");
    }

    if (response.status === 403) {

        throw new Error("Vous n'avez pas les autorisations nécessaires.");
    }

    const text = await response.text();

    let data = null;

    if (text) {

        try {
            data = JSON.parse(text);
        } catch {
            data = text;
        }
    }

    if (!response.ok) {

        const errorMessage =
            typeof data === "object" && data
                ? data.message || data.error
                : data;

        throw new Error(
            errorMessage || `Erreur HTTP ${response.status}`
        );
    }

    return data;
}


/* =====================================================
   UTILITAIRES
===================================================== */

function unwrapList(data) {

    if (Array.isArray(data)) {
        return data;
    }

    if (!data) {
        return [];
    }

    if (Array.isArray(data.content)) {
        return data.content;
    }

    if (Array.isArray(data.members)) {
        return data.members;
    }

    if (Array.isArray(data.data)) {
        return data.data;
    }

    return [];
}


function unwrapAccount(data) {

    if (!data) {
        return null;
    }

    if (data.account) {
        return data.account;
    }

    if (data.data && !Array.isArray(data.data)) {
        return data.data;
    }

    return data;
}


function getMemberName(member) {

    return [
        member.firstName || member.firstname || "",
        member.lastName || member.lastname || ""
    ].join(" ").trim()
        || member.fullName
        || member.name
        || "Membre sans nom";
}


function getMemberId(member) {

    return member.id ?? member.memberId;
}


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


function normalizeRole(role) {

    return String(role || "")
        .trim()
        .toUpperCase();
}


function normalizeAdminRole(role) {

    return normalizeRole(role);
}


function displayRole(account) {

    const role = normalizeRole(account?.role);
    const adminRole = normalizeAdminRole(account?.adminRole);

    if (role === "MEMBER") {
        return "MEMBER";
    }

    return adminRole || role || "Non défini";
}


function roleClass(account) {

    const role = displayRole(account);

    switch (role) {

        case "MEMBER":
            return "role-member";

        case "ADMINISTRATEUR":
            return "role-admin";

        case "TRESORIER":
            return "role-treasurer";

        case "ADMIN_PRINCIPAL":
            return "role-principal";

        default:
            return "role-member";
    }
}


/* =====================================================
   MESSAGES
===================================================== */

function showMessage(message, type = "success") {

    clearTimeout(messageTimer);

    elements.message.textContent = message;

    elements.message.className =
        `app-message ${type}`;

    elements.message.scrollIntoView({
        behavior: "smooth",
        block: "nearest"
    });

    messageTimer = setTimeout(() => {

        elements.message.classList.add("hidden");

    }, 5000);
}


/* =====================================================
   CHARGEMENT DES MEMBRES ET COMPTES
===================================================== */

async function loadAccounts() {

    elements.table.innerHTML = `
        <tr>
            <td colspan="6" class="loading-cell">
                Chargement des comptes...
            </td>
        </tr>
    `;

    try {

        const data = await apiRequest(
            `${API_URL}/members`
        );

        members = unwrapList(data);

        const results = await Promise.allSettled(

            members.map(async member => {

                const id = getMemberId(member);

                if (id == null) {
                    return null;
                }

                try {

                    const accountData = await apiRequest(
                        `${API_URL}/admin/members/${id}/account`
                    );

                    return {
                        member,
                        account: unwrapAccount(accountData)
                    };

                } catch (error) {

                    // Un membre sans compte peut répondre 404.
                    if (error.message.includes("404")) {
                        return {
                            member,
                            account: null
                        };
                    }

                    throw error;
                }

            })

        );

        accounts = results.map(result => {

            if (result.status === "fulfilled") {
                return result.value;
            }

            console.error(
                "Erreur de récupération du compte :",
                result.reason
            );

            return null;

        }).filter(Boolean);

        updateStatistics();

        renderAccounts();

        populateMemberSelect();

    } catch (error) {

        console.error(error);

        elements.table.innerHTML = `
            <tr>
                <td colspan="6" class="empty-cell">
                    Impossible de charger les comptes.
                    ${escapeHTML(error.message)}
                </td>
            </tr>
        `;

        showMessage(error.message, "error");
    }
}


/* =====================================================
   STATISTIQUES
===================================================== */

function updateStatistics() {

    const withAccount = accounts.filter(
        item => item.account
    );

    const memberCount = withAccount.filter(
        item => normalizeRole(item.account.role) === "MEMBER"
    ).length;

    const adminCount = withAccount.filter(
        item => normalizeRole(item.account.role) === "ADMIN"
    ).length;

    elements.total.textContent = withAccount.length;

    elements.members.textContent = memberCount;

    elements.admins.textContent = adminCount;

    elements.without.textContent =
        members.length - withAccount.length;
}


/* =====================================================
   AFFICHAGE DES COMPTES
===================================================== */

function renderAccounts() {

    const search = elements.search.value
        .trim()
        .toLowerCase();

    const roleFilter = elements.filterRole.value;

    const accountFilter = elements.filterAccount.value;

    let filtered = accounts.filter(item => {

        const member = item.member;
        const account = item.account;

        const name = getMemberName(member).toLowerCase();

        const email = String(
            account?.email || member.email || ""
        ).toLowerCase();

        const matchesSearch =
            name.includes(search) ||
            email.includes(search);

        const role = account
            ? displayRole(account)
            : "";

        const matchesRole =
            !roleFilter ||
            role === roleFilter;

        const matchesAccount =
            !accountFilter ||
            (
                accountFilter === "WITH_ACCOUNT"
                    ? Boolean(account)
                    : !account
            );

        return (
            matchesSearch &&
            matchesRole &&
            matchesAccount
        );

    });

    elements.results.textContent =
        `${filtered.length} résultat(s)`;


    if (!filtered.length) {

        elements.table.innerHTML = `
            <tr>
                <td colspan="6" class="empty-cell">
                    Aucun compte trouvé.
                </td>
            </tr>
        `;

        return;
    }


    elements.table.innerHTML = filtered.map(item => {

        const member = item.member;
        const account = item.account;

        const name = escapeHTML(getMemberName(member));

        const email = escapeHTML(
            account?.email || member.email || "—"
        );

        const role = account
            ? displayRole(account)
            : "";

        const accountExists = Boolean(account);

        const roleLabel = {
            MEMBER: "Membre",
            ADMINISTRATEUR: "Administrateur",
            TRESORIER: "Trésorier",
            ADMIN_PRINCIPAL: "Admin principal"
        };

        const roleText = roleLabel[role] || role || "—";

        return `

            <tr>

                <td>
                    <span class="member-name">${name}</span>

                    <span class="member-subtitle">
                        ${escapeHTML(member.filiere || "Membre")}
                    </span>
                </td>

                <td>${email}</td>

                <td>

                    ${accountExists
                ? `<span class="role-badge ${roleClass(account)}">
                                ${escapeHTML(roleText)}
                           </span>`
                : "—"
            }

                </td>

                <td>
                    ${accountExists ? escapeHTML(roleText) : "—"}
                </td>

                <td>

                    <span class="account-badge ${accountExists ? "account-yes" : "account-no"
            }">

                        ${accountExists ? "Créé" : "Absent"}

                    </span>

                </td>

                <td>

                    <div class="action-group">

                        ${accountExists
                ? `
                                <button
                                    class="action-btn action-edit"
                                    data-action="edit"
                                    data-id="${escapeHTML(getMemberId(member))}">
                                    Modifier
                                </button>
                            `
                : `
                                <button
                                    class="action-btn action-create"
                                    data-action="create"
                                    data-id="${escapeHTML(getMemberId(member))}">
                                    Créer
                                </button>
                            `
            }

                    </div>

                </td>

            </tr>

        `;

    }).join("");

}


/* =====================================================
   SELECT MEMBRES
===================================================== */

function populateMemberSelect() {

    const availableMembers = accounts.filter(
        item => !item.account
    );

    elements.memberSelect.innerHTML = `

        <option value="">
            Sélectionner un membre
        </option>

        ${availableMembers.map(item => {

        const member = item.member;

        return `
                <option value="${escapeHTML(getMemberId(member))}">
                    ${escapeHTML(getMemberName(member))}
                </option>
            `;

    }).join("")}

    `;

}


/* =====================================================
   MODAL
===================================================== */

function openModal() {

    elements.modal.classList.remove("hidden");

    document.body.style.overflow = "hidden";
}


function closeModal() {

    elements.modal.classList.add("hidden");

    document.body.style.overflow = "";

    elements.form.reset();

    elements.memberId.value = "";

    editingAccount = false;

    updateRoleFields();
}


function resetModal() {

    elements.form.reset();

    elements.memberId.value = "";

    elements.memberSelect.disabled = false;

    elements.email.readOnly = false;

    elements.password.required = true;

    elements.passwordHelp.textContent =
        "Obligatoire lors de la création.";

    elements.modalTitle.textContent =
        "Créer un compte";

    elements.saveButton.textContent =
        "Créer le compte";

    editingAccount = false;

    updateRoleFields();
}


/* =====================================================
   CRÉATION
===================================================== */

function openCreateModal() {

    resetModal();

    if (!accounts.some(item => !item.account)) {

        showMessage(
            "Tous les membres possèdent déjà un compte.",
            "error"
        );

        return;
    }

    openModal();
}


/* =====================================================
   MODIFICATION
===================================================== */

function openEditModal(memberId) {

    const item = accounts.find(
        entry => String(getMemberId(entry.member)) === String(memberId)
    );

    if (!item || !item.account) {

        showMessage(
            "Compte introuvable.",
            "error"
        );

        return;
    }

    resetModal();

    editingAccount = true;

    const account = item.account;

    elements.memberId.value = memberId;

    elements.memberSelect.innerHTML = `
        <option value="${escapeHTML(memberId)}">
            ${escapeHTML(getMemberName(item.member))}
        </option>
    `;

    elements.memberSelect.value = String(memberId);

    elements.memberSelect.disabled = true;

    elements.email.value = account.email || "";

    elements.email.readOnly = false;

    elements.password.value = "";

    elements.password.required = false;

    elements.passwordHelp.textContent =
        "Laissez vide pour conserver le mot de passe actuel.";

    elements.role.value =
        normalizeRole(account.role) || "MEMBER";

    elements.adminRole.value =
        normalizeAdminRole(account.adminRole);

    elements.modalTitle.textContent =
        "Modifier le compte";

    elements.saveButton.textContent =
        "Enregistrer les modifications";

    updateRoleFields();

    openModal();
}


/* =====================================================
   GESTION DES ROLES
===================================================== */

function updateRoleFields() {

    const isAdmin =
        elements.role.value === "ADMIN";

    elements.adminRoleContainer.classList.toggle(
        "hidden",
        !isAdmin
    );

    elements.adminRole.required = isAdmin;

    if (!isAdmin) {
        elements.adminRole.value = "";
    }
}


/* =====================================================
   ENREGISTREMENT
===================================================== */

async function saveAccount(event) {

    event.preventDefault();

    const memberId = editingAccount
        ? elements.memberId.value
        : elements.memberSelect.value;

    if (!memberId) {

        showMessage(
            "Veuillez sélectionner un membre.",
            "error"
        );

        return;
    }

    const email = elements.email.value.trim();

    const password = elements.password.value;

    const role = elements.role.value;

    const adminRole = elements.adminRole.value;

    if (role === "ADMIN" && !adminRole) {

        showMessage(
            "Veuillez sélectionner une responsabilité administrative.",
            "error"
        );

        return;
    }

    if (!editingAccount && !password) {

        showMessage(
            "Le mot de passe est obligatoire.",
            "error"
        );

        return;
    }

    const payload = {

        email,

        role,

        adminRole: role === "ADMIN"
            ? adminRole
            : null

    };

    if (password) {
        payload.password = password;
    }

    const url =
        `${API_URL}/admin/members/${memberId}/account`;

    const method = editingAccount
        ? "PUT"
        : "POST";

    elements.saveButton.disabled = true;

    elements.saveButton.textContent =
        "Enregistrement...";

    try {

        await apiRequest(url, {

            method,

            body: JSON.stringify(payload)

        });

        closeModal();

        showMessage(
            editingAccount
                ? "Compte modifié avec succès."
                : "Compte créé avec succès."
        );

        await loadAccounts();

    } catch (error) {

        console.error(error);

        showMessage(error.message, "error");

    } finally {

        elements.saveButton.disabled = false;

        elements.saveButton.textContent =
            "Enregistrer";

    }
}


/* =====================================================
   EVENEMENTS
===================================================== */

elements.search.addEventListener(
    "input",
    renderAccounts
);

elements.filterRole.addEventListener(
    "change",
    renderAccounts
);

elements.filterAccount.addEventListener(
    "change",
    renderAccounts
);

elements.role.addEventListener(
    "change",
    updateRoleFields
);

elements.form.addEventListener(
    "submit",
    saveAccount
);

document.getElementById(
    "create-account-btn"
).addEventListener(
    "click",
    openCreateModal
);

document.getElementById(
    "close-modal"
).addEventListener(
    "click",
    closeModal
);

document.getElementById(
    "cancel-modal"
).addEventListener(
    "click",
    closeModal
);

document.getElementById(
    "refresh-btn"
).addEventListener(
    "click",
    loadAccounts
);

document.getElementById(
    "logout-btn"
).addEventListener(
    "click",
    () => {

        TOKEN_KEYS.forEach(key =>
            localStorage.removeItem(key)
        );

        ROLE_KEYS.forEach(key =>
            localStorage.removeItem(key)
        );

        localStorage.removeItem("dahira_role");

        window.location.replace("../login.html");

    }
);


elements.table.addEventListener(
    "click",
    event => {

        const button = event.target.closest(
            "[data-action]"
        );

        if (!button) {
            return;
        }

        const id = button.dataset.id;

        if (button.dataset.action === "create") {

            resetModal();

            elements.memberSelect.value = id;

            openModal();

        }

        if (button.dataset.action === "edit") {

            openEditModal(id);

        }

    }
);


elements.modal.addEventListener(
    "click",
    event => {

        if (event.target === elements.modal) {
            closeModal();
        }

    }
);


/* =====================================================
   INITIALISATION
===================================================== */

document.addEventListener(
    "DOMContentLoaded",
    () => {

        if (!checkAuthorization()) {
            return;
        }

        loadAccounts();

    }
);