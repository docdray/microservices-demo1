const MESSAGES_API = "/api/messages";

function debounce(fn, delayMs) {
	let timer = null;
	return (...args) => {
		clearTimeout(timer);
		timer = setTimeout(() => fn(...args), delayMs);
	};
}

async function apiFetch(url, options = {}) {
	const res = await fetch(url, {
		...options,
		headers: { "Content-Type": "application/json", ...(options.headers || {}) },
	});
	const text = await res.text();
	let body = null;
	if (text) {
		try {
			body = JSON.parse(text);
		} catch (e) {
			body = null;
		}
	}
	if (!res.ok) {
		const message = body && body.error ? body.error : `Fehler ${res.status}`;
		throw new Error(message);
	}
	return body;
}

function setStatus(element, message, isError) {
	element.textContent = message || "";
	element.className = "status" + (message ? (isError ? " error" : " ok") : "");
}

function formatTimestamp(value) {
	if (!value) return "";
	const date = new Date(value);
	if (Number.isNaN(date.getTime())) return value;
	return date.toLocaleString("de-DE");
}

function escapeHtml(value) {
	const div = document.createElement("div");
	div.textContent = value ?? "";
	return div.innerHTML;
}

function currentFilters() {
	const params = new URLSearchParams();
	const search = document.getElementById("message-search").value.trim();
	const createdFrom = document.getElementById("created-from").value;
	const createdTo = document.getElementById("created-to").value;
	const updatedFrom = document.getElementById("updated-from").value;
	const updatedTo = document.getElementById("updated-to").value;

	if (search) params.set("search", search);
	if (createdFrom) params.set("createdFrom", createdFrom);
	if (createdTo) params.set("createdTo", createdTo);
	if (updatedFrom) params.set("updatedFrom", updatedFrom);
	if (updatedTo) params.set("updatedTo", updatedTo);

	return params;
}

let editingMessageId = null;

function renderMessageRow(message) {
	const tr = document.createElement("tr");
	tr.innerHTML = `
		<td>${message.id}</td>
		<td class="text-cell">${escapeHtml(message.text)}</td>
		<td>${formatTimestamp(message.createdAt)}</td>
		<td>${formatTimestamp(message.updatedAt)}</td>
	`;

	const actionsTd = document.createElement("td");
	actionsTd.className = "actions";

	const editBtn = document.createElement("button");
	editBtn.type = "button";
	editBtn.textContent = "Bearbeiten";
	editBtn.addEventListener("click", () => startEditMessage(message));

	const deleteBtn = document.createElement("button");
	deleteBtn.type = "button";
	deleteBtn.textContent = "Löschen";
	deleteBtn.addEventListener("click", () => deleteMessage(message.id));

	actionsTd.appendChild(editBtn);
	actionsTd.appendChild(deleteBtn);
	tr.appendChild(actionsTd);

	return tr;
}

async function loadMessages() {
	const params = currentFilters();
	const query = params.toString();
	const url = query ? `${MESSAGES_API}?${query}` : MESSAGES_API;
	let messages;
	try {
		messages = await apiFetch(url);
	} catch (e) {
		setStatus(document.getElementById("message-status"), `Konnte Nachrichten nicht laden: ${e.message}`, true);
		return;
	}
	const tbody = document.getElementById("message-table-body");
	tbody.innerHTML = "";
	for (const message of messages) {
		tbody.appendChild(renderMessageRow(message));
	}
}

function startEditMessage(message) {
	editingMessageId = message.id;
	document.getElementById("message-form-title").textContent = `Nachricht #${message.id} bearbeiten`;
	document.getElementById("message-text").value = message.text;
	document.getElementById("message-submit-btn").textContent = "Speichern";
	document.getElementById("message-cancel-btn").hidden = false;
	setStatus(document.getElementById("message-status"), "", false);
	window.scrollTo({ top: document.getElementById("message-form").offsetTop, behavior: "smooth" });
}

function resetMessageForm() {
	editingMessageId = null;
	document.getElementById("message-form").reset();
	document.getElementById("message-form-title").textContent = "Neue Nachricht anlegen";
	document.getElementById("message-submit-btn").textContent = "Anlegen";
	document.getElementById("message-cancel-btn").hidden = true;
}

async function submitMessageForm(event) {
	event.preventDefault();
	const statusEl = document.getElementById("message-status");
	const text = document.getElementById("message-text").value.trim();

	if (!text) {
		setStatus(statusEl, "Text darf nicht leer sein.", true);
		return;
	}

	const payload = { text };
	try {
		if (editingMessageId) {
			await apiFetch(`${MESSAGES_API}/${editingMessageId}`, { method: "PUT", body: JSON.stringify(payload) });
			setStatus(statusEl, "Nachricht aktualisiert.", false);
		} else {
			await apiFetch(MESSAGES_API, { method: "POST", body: JSON.stringify(payload) });
			setStatus(statusEl, "Nachricht angelegt.", false);
		}
		resetMessageForm();
		await loadMessages();
	} catch (e) {
		setStatus(statusEl, e.message, true);
	}
}

async function deleteMessage(id) {
	if (!confirm(`Nachricht #${id} wirklich löschen?`)) return;
	try {
		await apiFetch(`${MESSAGES_API}/${id}`, { method: "DELETE" });
		await loadMessages();
	} catch (e) {
		setStatus(document.getElementById("message-status"), e.message, true);
	}
}

function resetFilters() {
	document.getElementById("message-filters").reset();
	loadMessages();
}

function init() {
	document.getElementById("message-filters").addEventListener("submit", (e) => {
		e.preventDefault();
		loadMessages();
	});
	document.getElementById("message-filter-reset-btn").addEventListener("click", resetFilters);
	document.getElementById("message-search").addEventListener(
		"input",
		debounce(() => loadMessages(), 300)
	);

	document.getElementById("message-form").addEventListener("submit", submitMessageForm);
	document.getElementById("message-cancel-btn").addEventListener("click", resetMessageForm);

	loadMessages();
}

document.addEventListener("DOMContentLoaded", init);
