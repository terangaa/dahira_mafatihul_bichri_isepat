"use strict";

document.addEventListener("DOMContentLoaded", () => {

    /* =====================================================
       1. CONFIGURATION
    ===================================================== */

    const API_URL = "/api/admin/membership-requests";
    const LOGIN_URL = "/login.html";

    const PAGE_SIZE = 9;

    const TOKEN_KEYS = [
        "dahira_token",
        "token",
        "jwt",
        "jwtToken",
        "accessToken",
        "access_token",
        "authToken",
        "auth_token"
    ];

    const elements = {
        list: document.getElementById("requests-list"),
        loading: document.getElementById("requests-loading"),
        error: document.getElementById("requests-error"),
        errorText: document.getElementById("requests-error-text"),
        empty: document.getElementById("requests-empty"),
        noResults: document.getElementById("requests-no-results"),

        search: document.getElementById("request-search"),
        refresh: document.getElementById("refresh-requests"),
        retry: document.getElementById("retry-requests"),

        pendingCount: document.getElementById("pending-count"),
        displayedCount: document.getElementById("displayed-count"),
        sidebarCount: document.getElementById("sidebar-pending-count"),
        footerCount: document.getElementById("requests-footer-count"),

        message: document.getElementById("app-message"),

        dialog: document.getElementById("decision-dialog"),
        dialogIcon: document.getElementById("dialog-icon"),
        dialogTitle: document.getElementById("dialog-title"),
        dialogDescription: document.getElementById("dialog-description"),
        dialogCandidate: document.getElementById("dialog-candidate"),

        confirm: document.getElementById("confirm-decision"),
        cancel: document.getElementById("cancel-decision"),

        logout: document.getElementById("logout-button"),
        menuButton: document.getElementById("menu-button"),
        sidebar: document.querySelector(".sidebar"),

        userEmail: document.getElementById("user-email"),
        userRole: document.getElementById("user-role"),
        userAvatar: document.getElementById("user-avatar")
    };


    /* =====================================================
       2. AUTHENTIFICATION
    ===================================================== */

    function getToken() {

        for (const key of TOKEN_KEYS) {

            const value =
                localStorage.getItem(key) ||
                sessionStorage.getItem(key);

            if (value && value.trim()) {
                return value.trim();
            }
        }

        return null;
    }


    function getStoredValue(key) {

        return (
            localStorage.getItem(key) ||
            sessionStorage.getItem(key) ||
            ""
        ).trim();
    }


    function getUserRole() {

        const role = getStoredValue("dahira_role").toUpperCase();

        const adminRole = getStoredValue("dahira_admin_role")
            .toUpperCase();

        if (role === "ADMIN") {
            return adminRole;
        }

        if ([
            "ADMINISTRATEUR",
            "TRESORIER",
            "ADMIN_PRINCIPAL"
        ].includes(role)) {
            return role;
        }

        return "MEMBER";
    }


    const token = getToken();
    const currentRole = getUserRole();

    const canViewRequests = [
        "ADMINISTRATEUR",
        "TRESORIER",
        "ADMIN_PRINCIPAL"
    ].includes(currentRole);

    const canManageRequests = [
        "TRESORIER",
        "ADMIN_PRINCIPAL"
    ].includes(currentRole);


    function checkAccess() {

        if (!token) {
            window.location.replace(LOGIN_URL);
            return false;
        }

        if (!canViewRequests) {

            alert(
                "Vous n'avez pas les autorisations nécessaires pour consulter les demandes d'adhésion."
            );

            window.location.replace("/dashboard");

            return false;
        }

        return true;
    }


    if (!checkAccess()) return;


    console.info("=== DEMANDES D'ADHÉSION ===");
    console.info("Rôle :", currentRole);
    console.info("Consultation :", canViewRequests);
    console.info("Gestion :", canManageRequests);


    /* =====================================================
       3. INFORMATIONS UTILISATEUR
    ===================================================== */

    function displayUserInformation() {

        const email = getStoredValue("dahira_email");

        const roleLabels = {
            ADMINISTRATEUR: "Administrateur",
            TRESORIER: "Trésorier",
            ADMIN_PRINCIPAL: "Administrateur principal"
        };

        if (elements.userEmail) {
            elements.userEmail.textContent =
                email || "Utilisateur connecté";
        }

        if (elements.userRole) {
            elements.userRole.textContent =
                roleLabels[currentRole] || currentRole;
        }

        if (elements.userAvatar) {

            elements.userAvatar.textContent = currentRole
                .split("_")
                .map(part => part.charAt(0))
                .join("")
                .substring(0, 2);
        }

        if (!canManageRequests) {

            const description =
                document.querySelector(".hero-content > p") ||
                document.querySelector(".intro-description");

            if (description) {

                description.textContent =
                    "Consultez les candidatures reçues et leurs informations. Les décisions sont réservées au trésorier et à l'administrateur principal.";
            }

            const notice =
                document.querySelector(".security-notice");

            if (notice) {

                notice.innerHTML = `
                    <span class="security-icon">🔒</span>

                    <div>
                        <strong>Accès en consultation uniquement</strong>

                        <p>
                            Vous pouvez consulter les candidatures.
                            Les décisions d'admission sont réservées
                            aux responsables autorisés.
                        </p>
                    </div>
                `;
            }
        }
    }


    displayUserInformation();


    /* =====================================================
       4. VARIABLES
    ===================================================== */

    let requests = [];

    let currentDecision = null;

    let currentPage = 1;

    let isLoading = false;

    let isSubmitting = false;

    let messageTimeout = null;


    /* =====================================================
       5. PAGINATION DYNAMIQUE
    ===================================================== */

    const pagination = document.createElement("div");

    pagination.className = "requests-pagination";

    pagination.id = "requests-pagination";

    pagination.setAttribute("aria-label", "Pagination des demandes");


    const paginationStyle = document.createElement("style");

    paginationStyle.textContent = `

        .requests-pagination {
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 15px;
            padding-top: 20px;
            margin-top: 18px;
            border-top: 1px solid #e8edf5;
            flex-wrap: wrap;
        }

        .pagination-info {
            color: #78869b;
            font-size: 11px;
            font-weight: 600;
        }

        .pagination-controls {
            display: flex;
            align-items: center;
            gap: 6px;
            flex-wrap: wrap;
        }

        .pagination-button {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            min-width: 34px;
            height: 34px;
            padding: 0 10px;
            border: 1px solid #e5ebf4;
            border-radius: 8px;
            color: #43536b;
            background: white;
            font-family: inherit;
            font-size: 11px;
            font-weight: 700;
            cursor: pointer;
            transition: .2s;
        }

        .pagination-button:hover:not(:disabled) {
            color: #1261cf;
            border-color: #1261cf;
            background: #eaf2ff;
        }

        .pagination-button.active {
            color: white;
            border-color: #1261cf;
            background: #1261cf;
            box-shadow: 0 3px 9px rgba(18,97,207,.2);
        }

        .pagination-button:disabled {
            opacity: .4;
            cursor: not-allowed;
        }

        .pagination-dots {
            color: #78869b;
            font-size: 12px;
        }

        @media(max-width: 500px) {

            .requests-pagination {
                flex-direction: column;
                align-items: flex-start;
            }

            .pagination-controls {
                width: 100%;
                justify-content: center;
            }

        }

    `;

    document.head.appendChild(paginationStyle);

    if (elements.list) {
        elements.list.after(pagination);
    }


    /* =====================================================
       6. UTILITAIRES
    ===================================================== */

    function getAuthHeaders() {

        return {
            Accept: "application/json",
            Authorization: `Bearer ${token}`
        };
    }


    async function apiRequest(url, options = {}) {

        const headers = {
            ...getAuthHeaders(),
            ...(options.headers || {})
        };

        const response = await fetch(url, {
            ...options,
            headers,
            credentials: "same-origin"
        });

        if (response.status === 401) {

            throw new Error(
                "Votre session a expiré. Veuillez vous reconnecter."
            );
        }

        if (response.status === 403) {

            throw new Error(
                "Accès refusé. Votre rôle ne dispose pas des autorisations nécessaires."
            );
        }

        if (!response.ok) {

            let detail = "";

            try {

                const body = await response.json();

                detail = body.message || body.error || "";

            } catch (_) { }

            throw new Error(
                detail || `Erreur du serveur (${response.status}).`
            );
        }

        if (response.status === 204) return null;

        const contentType =
            response.headers.get("content-type") || "";

        if (contentType.includes("application/json")) {
            return response.json();
        }

        return null;
    }


    function escapeHtml(value) {

        return String(value ?? "").replace(/[&<>"']/g, character => ({
            "&": "&amp;",
            "<": "&lt;",
            ">": "&gt;",
            '"': "&quot;",
            "'": "&#039;"
        })[character]);
    }


    function setVisible(element, visible) {

        element?.classList.toggle("hidden", !visible);
    }


    function showMessage(message, type = "success") {

        if (!elements.message) return;

        clearTimeout(messageTimeout);

        elements.message.textContent = message;

        elements.message.classList.remove(
            "hidden",
            "success",
            "error",
            "warning"
        );

        elements.message.classList.add(type);

        messageTimeout = setTimeout(() => {

            elements.message.classList.add("hidden");

        }, 6000);
    }


    function getName(request) {

        return (
            `${request.firstName || ""} ${request.lastName || ""}`.trim() ||
            request.name ||
            "Candidat sans nom"
        );
    }


    function getInitials(request) {

        const first = String(request.firstName || "").trim();
        const last = String(request.lastName || "").trim();

        return (
            `${first.charAt(0)}${last.charAt(0)}`.toUpperCase() ||
            "AD"
        );
    }


    function formatDate(value) {

        if (!value) return "Non renseignée";

        const date = new Date(value);

        if (Number.isNaN(date.getTime())) {
            return String(value);
        }

        return new Intl.DateTimeFormat("fr-FR", {
            day: "2-digit",
            month: "short",
            year: "numeric"
        }).format(date);
    }


    function normalizeRequestList(data) {

        if (Array.isArray(data)) return data;

        if (Array.isArray(data?.content)) return data.content;

        if (Array.isArray(data?.data)) return data.data;

        if (Array.isArray(data?.requests)) return data.requests;

        if (Array.isArray(data?.members)) return data.members;

        return [];
    }


    /* =====================================================
       7. CONSTRUCTION D'UNE CARTE COMPACTE
    ===================================================== */

    function renderRequest(request, index) {

        const name = getName(request);

        const details = [

            {
                label: "Téléphone",
                value: request.phone || "Non renseigné",
                icon: "☎"
            },

            {
                label: "Commission",
                value: request.commission || "Non renseignée",
                icon: "◎"
            },

            {
                label: "Filière",
                value: request.filiere || "Non renseignée",
                icon: "▤"
            },

            {
                label: "Promotion",
                value: request.promotion || "Non renseignée",
                icon: "♙"
            },

            {
                label: "Inscription",
                value: formatDate(request.registrationDate),
                icon: "◷"
            }

        ];

        const detailsHtml = details.map(detail => `

            <div class="compact-detail">

                <span class="compact-detail-icon">
                    ${detail.icon}
                </span>

                <div class="compact-detail-content">

                    <span class="compact-detail-label">
                        ${escapeHtml(detail.label)}
                    </span>

                    <strong>
                        ${escapeHtml(detail.value)}
                    </strong>

                </div>

            </div>

        `).join("");


        const article = document.createElement("article");

        article.className = "request-item compact-request-card";

        article.style.setProperty(
            "--card-index",
            index
        );


        const actionsHtml = canManageRequests

            ? `

                <div class="request-actions">

                    <button
                        type="button"
                        class="request-action reject-button"
                        data-decision="reject">

                        ✕ Refuser

                    </button>

                    <button
                        type="button"
                        class="request-action approve-button"
                        data-decision="approve">

                        ✓ Accepter

                    </button>

                </div>

            `

            : `

                <div class="request-actions consultation-only">

                    <span class="consultation-label">
                        ◉ Consultation uniquement
                    </span>

                </div>

            `;


        article.innerHTML = `

            <div class="compact-card-header">

                <span class="request-reference">
                    CANDIDATURE
                </span>

                <span class="request-status">
                    <span class="status-dot"></span>
                    En attente
                </span>

            </div>


            <div class="request-person">

                <div class="request-avatar">
                    ${escapeHtml(getInitials(request))}
                </div>

                <div class="request-person-info">

                    <h3>${escapeHtml(name)}</h3>

                    <p title="${escapeHtml(request.email || "")}">
                        ${escapeHtml(request.email || "Email non renseigné")}
                    </p>

                </div>

            </div>


            <div class="compact-card-divider"></div>


            <div class="request-details">

                ${detailsHtml}

            </div>


            ${actionsHtml}

        `;


        if (canManageRequests) {

            article.querySelectorAll("[data-decision]")
                .forEach(button => {

                    button.addEventListener("click", () => {

                        openDecisionDialog(
                            request,
                            button.dataset.decision
                        );

                    });

                });
        }


        return article;
    }


    /* =====================================================
       8. RECHERCHE
    ===================================================== */

    function getFilteredRequests() {

        const query = (elements.search?.value || "")
            .trim()
            .toLocaleLowerCase("fr");

        return requests.filter(request => {

            const searchable = [

                request.firstName,
                request.lastName,
                request.name,
                request.email,
                request.phone,
                request.commission,
                request.filiere,
                request.promotion

            ]
                .filter(Boolean)
                .join(" ")
                .toLocaleLowerCase("fr");

            return searchable.includes(query);

        });
    }


    /* =====================================================
       9. PAGINATION
    ===================================================== */

    function renderPagination(totalItems, totalPages, start, end) {

        pagination.replaceChildren();

        if (totalItems === 0 || totalPages <= 1) {

            pagination.classList.add("hidden");

            return;
        }

        pagination.classList.remove("hidden");


        const info = document.createElement("span");

        info.className = "pagination-info";

        info.textContent =
            `Affichage ${start + 1} à ${end} sur ${totalItems} demandes`;


        const controls = document.createElement("div");

        controls.className = "pagination-controls";


        function addButton(label, page, options = {}) {

            const button = document.createElement("button");

            button.type = "button";

            button.className = "pagination-button";

            button.textContent = label;

            if (options.active) {
                button.classList.add("active");
                button.setAttribute("aria-current", "page");
            }

            if (options.disabled) {
                button.disabled = true;
            }

            button.addEventListener("click", () => {

                if (page === currentPage || page < 1 || page > totalPages) {
                    return;
                }

                currentPage = page;

                renderRequests();

                elements.list.scrollIntoView({
                    behavior: "smooth",
                    block: "start"
                });

            });

            controls.appendChild(button);
        }


        addButton(
            "‹",
            currentPage - 1,
            {
                disabled: currentPage === 1
            }
        );


        const pages = new Set([
            1,
            totalPages,
            currentPage - 1,
            currentPage,
            currentPage + 1
        ]);

        const sortedPages = [...pages]
            .filter(page => page >= 1 && page <= totalPages)
            .sort((a, b) => a - b);


        let previous = 0;

        sortedPages.forEach(page => {

            if (previous && page - previous > 1) {

                const dots = document.createElement("span");

                dots.className = "pagination-dots";

                dots.textContent = "…";

                controls.appendChild(dots);
            }

            addButton(
                String(page),
                page,
                {
                    active: page === currentPage
                }
            );

            previous = page;

        });


        addButton(
            "›",
            currentPage + 1,
            {
                disabled: currentPage === totalPages
            }
        );


        pagination.append(info, controls);
    }


    /* =====================================================
       10. AFFICHAGE DES DEMANDES
    ===================================================== */

    function renderRequests() {

        if (!elements.list) return;

        const filtered = getFilteredRequests();

        const totalItems = filtered.length;

        const totalPages = Math.ceil(totalItems / PAGE_SIZE);


        if (currentPage > totalPages) {
            currentPage = Math.max(1, totalPages);
        }

        if (currentPage < 1) {
            currentPage = 1;
        }


        const start = (currentPage - 1) * PAGE_SIZE;

        const end = Math.min(
            start + PAGE_SIZE,
            totalItems
        );

        const currentItems = filtered.slice(start, end);


        elements.list.replaceChildren();


        currentItems.forEach((request, index) => {

            elements.list.appendChild(
                renderRequest(request, index)
            );

        });


        /* Statistiques */

        if (elements.pendingCount) {
            elements.pendingCount.textContent =
                String(requests.length);
        }

        if (elements.displayedCount) {
            elements.displayedCount.textContent =
                String(currentItems.length);
        }

        if (elements.footerCount) {

            elements.footerCount.textContent =
                `${totalItems} demande(s) trouvée(s) sur ${requests.length}`;
        }


        /* Sidebar */

        if (elements.sidebarCount) {

            elements.sidebarCount.textContent =
                String(requests.length);

            setVisible(
                elements.sidebarCount,
                requests.length > 0
            );
        }


        /* États */

        setVisible(elements.loading, false);

        setVisible(elements.error, false);

        setVisible(
            elements.empty,
            requests.length === 0
        );

        setVisible(
            elements.noResults,
            requests.length > 0 && totalItems === 0
        );

        setVisible(
            elements.list,
            currentItems.length > 0
        );


        renderPagination(
            totalItems,
            totalPages,
            start,
            end
        );
    }


    /* =====================================================
       11. CHARGEMENT DES DEMANDES
    ===================================================== */

    async function loadRequests() {

        if (isLoading) return;

        isLoading = true;

        if (elements.refresh) {
            elements.refresh.disabled = true;
        }

        if (elements.retry) {
            elements.retry.disabled = true;
        }

        setVisible(elements.loading, true);

        setVisible(elements.error, false);

        setVisible(elements.empty, false);

        setVisible(elements.noResults, false);

        setVisible(elements.list, false);

        setVisible(pagination, false);


        try {

            const data = await apiRequest(API_URL, {
                method: "GET"
            });

            requests = normalizeRequestList(data);

            currentPage = 1;

            renderRequests();

            console.info(
                `${requests.length} demande(s) chargée(s).`
            );


        } catch (error) {

            console.error(
                "Erreur de chargement :",
                error
            );


            if (error.message.includes("session a expiré")) {

                clearAuthentication();

                window.location.replace(LOGIN_URL);

                return;
            }


            if (elements.errorText) {

                elements.errorText.textContent =
                    error.message || "Impossible de charger les demandes.";
            }


            setVisible(elements.loading, false);

            setVisible(elements.error, true);

            setVisible(elements.list, false);

            setVisible(elements.empty, false);

            setVisible(elements.noResults, false);

            setVisible(pagination, false);


            if (elements.footerCount) {
                elements.footerCount.textContent =
                    "Le chargement a échoué.";
            }

        } finally {

            isLoading = false;

            if (elements.refresh) {
                elements.refresh.disabled = false;
            }

            if (elements.retry) {
                elements.retry.disabled = false;
            }
        }
    }


    /* =====================================================
       12. FENÊTRE DE CONFIRMATION
    ===================================================== */

    function openDecisionDialog(request, decision) {

        if (!canManageRequests) {

            showMessage(
                "Vous n'avez pas les autorisations nécessaires.",
                "error"
            );

            return;
        }


        if (request.userId == null) {

            showMessage(
                "Identifiant utilisateur introuvable.",
                "error"
            );

            return;
        }


        currentDecision = {

            userId: request.userId,

            decision,

            name: getName(request)

        };


        const isApprove = decision === "approve";


        elements.dialogIcon.textContent =
            isApprove ? "✓" : "✕";

        elements.dialogIcon.classList.toggle(
            "reject",
            !isApprove
        );


        elements.dialogTitle.textContent =
            isApprove
                ? "Accepter cette demande ?"
                : "Refuser cette demande ?";


        elements.dialogDescription.textContent =
            isApprove
                ? "Le candidat sera autorisé à accéder à la plateforme après son approbation."
                : "Cette action marquera la demande comme rejetée.";


        elements.dialogCandidate.textContent =
            currentDecision.name;


        elements.confirm.textContent =
            isApprove
                ? "Confirmer l'acceptation"
                : "Confirmer le refus";


        elements.confirm.classList.toggle(
            "reject-confirm",
            !isApprove
        );


        if (typeof elements.dialog?.showModal === "function") {

            elements.dialog.showModal();

        } else {

            showMessage(
                "Votre navigateur ne prend pas en charge cette fenêtre.",
                "warning"
            );
        }
    }


    /* =====================================================
       13. ENREGISTREMENT DE LA DÉCISION
    ===================================================== */

    async function submitDecision() {

        if (!currentDecision || isSubmitting) return;

        if (!canManageRequests) {

            showMessage(
                "Action interdite pour votre rôle.",
                "error"
            );

            return;
        }


        const decision = currentDecision.decision;

        const userId = currentDecision.userId;

        const candidateName = currentDecision.name;


        const action = decision === "approve"
            ? "approve"
            : "reject";


        const endpoint =
            `${API_URL}/${encodeURIComponent(userId)}/${action}`;


        isSubmitting = true;

        elements.confirm.disabled = true;

        elements.cancel.disabled = true;

        elements.confirm.textContent =
            "Traitement en cours...";


        try {

            await apiRequest(endpoint, {
                method: "PUT"
            });


            elements.dialog.close();

            currentDecision = null;


            showMessage(

                decision === "approve"
                    ? `La demande de ${candidateName} a été acceptée.`
                    : `La demande de ${candidateName} a été refusée.`,

                "success"
            );


            await loadRequests();


        } catch (error) {

            console.error(
                "Erreur lors de la décision :",
                error
            );

            showMessage(
                error.message || "La décision n'a pas pu être enregistrée.",
                "error"
            );

        } finally {

            isSubmitting = false;

            elements.confirm.disabled = false;

            elements.cancel.disabled = false;

            elements.confirm.textContent =
                currentDecision?.decision === "reject"
                    ? "Confirmer le refus"
                    : "Confirmer l'acceptation";
        }
    }


    /* =====================================================
       14. DÉCONNEXION
    ===================================================== */

    function clearAuthentication() {

        TOKEN_KEYS.forEach(key => {

            localStorage.removeItem(key);

            sessionStorage.removeItem(key);

        });


        [
            "dahira_role",
            "dahira_admin_role",
            "dahira_email"

        ].forEach(key => {

            localStorage.removeItem(key);

            sessionStorage.removeItem(key);

        });
    }


    /* =====================================================
       15. ÉVÉNEMENTS
    ===================================================== */

    elements.refresh?.addEventListener(
        "click",
        loadRequests
    );

    elements.retry?.addEventListener(
        "click",
        loadRequests
    );


    elements.search?.addEventListener("input", () => {

        currentPage = 1;

        renderRequests();

    });


    elements.confirm?.addEventListener(
        "click",
        submitDecision
    );


    elements.cancel?.addEventListener("click", () => {

        if (isSubmitting) return;

        elements.dialog.close();

        currentDecision = null;

    });


    elements.dialog?.addEventListener("click", event => {

        if (event.target === elements.dialog && !isSubmitting) {

            elements.dialog.close();

            currentDecision = null;
        }

    });


    elements.dialog?.addEventListener("cancel", event => {

        if (isSubmitting) {
            event.preventDefault();
            return;
        }

        currentDecision = null;

    });


    elements.menuButton?.addEventListener("click", () => {

        elements.sidebar?.classList.toggle("open");

    });


    elements.logout?.addEventListener("click", () => {

        clearAuthentication();

        window.location.replace(LOGIN_URL);

    });


    /* =====================================================
       16. INITIALISATION
    ===================================================== */

    loadRequests();

});