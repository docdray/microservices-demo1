const BOOKS_API = "/api/books";
const AUTHORS_API = "/api/authors";

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

function switchTab(tabName) {
	for (const btn of document.querySelectorAll(".tabs button")) {
		btn.classList.toggle("active", btn.dataset.tab === tabName);
	}
	document.getElementById("panel-books").classList.toggle("active", tabName === "books");
	document.getElementById("panel-authors").classList.toggle("active", tabName === "authors");
}

// ---------- Books ----------

let editingBookId = null;
let selectedAuthors = [];

function renderAuthorChips() {
	const container = document.getElementById("book-author-chips");
	container.innerHTML = "";
	for (const author of selectedAuthors) {
		const chip = document.createElement("span");
		chip.className = "chip";
		chip.textContent = `${author.firstName} ${author.lastName}`;
		const removeBtn = document.createElement("button");
		removeBtn.type = "button";
		removeBtn.textContent = "×";
		removeBtn.addEventListener("click", () => {
			selectedAuthors = selectedAuthors.filter((a) => a.id !== author.id);
			renderAuthorChips();
		});
		chip.appendChild(removeBtn);
		container.appendChild(chip);
	}
}

function hideAuthorSuggestions() {
	const list = document.getElementById("author-suggestions");
	list.hidden = true;
	list.innerHTML = "";
}

async function showAuthorSuggestions(term) {
	const list = document.getElementById("author-suggestions");
	if (!term || term.trim().length === 0) {
		hideAuthorSuggestions();
		return;
	}
	let authors;
	try {
		authors = await apiFetch(`${AUTHORS_API}?search=${encodeURIComponent(term)}`);
	} catch (e) {
		hideAuthorSuggestions();
		return;
	}
	const candidates = authors.filter((a) => !selectedAuthors.some((s) => s.id === a.id));
	if (candidates.length === 0) {
		hideAuthorSuggestions();
		return;
	}
	list.innerHTML = "";
	for (const author of candidates) {
		const item = document.createElement("li");
		item.textContent = `${author.firstName} ${author.lastName}`;
		item.addEventListener("click", () => {
			selectedAuthors.push(author);
			renderAuthorChips();
			document.getElementById("author-search").value = "";
			hideAuthorSuggestions();
		});
		list.appendChild(item);
	}
	list.hidden = false;
}

function renderBookRow(book) {
	const tr = document.createElement("tr");

	const authorNames = book.authors.map((a) => `${a.firstName} ${a.lastName}`).join(", ");
	tr.innerHTML = `
		<td>${book.id}</td>
		<td>${escapeHtml(book.title)}</td>
		<td>${escapeHtml(book.isbn)}</td>
		<td>${escapeHtml(authorNames)}</td>
	`;

	const actionsTd = document.createElement("td");
	actionsTd.className = "actions";

	const editBtn = document.createElement("button");
	editBtn.type = "button";
	editBtn.textContent = "Bearbeiten";
	editBtn.addEventListener("click", () => startEditBook(book));

	const deleteBtn = document.createElement("button");
	deleteBtn.type = "button";
	deleteBtn.textContent = "Löschen";
	deleteBtn.addEventListener("click", () => deleteBook(book.id));

	actionsTd.appendChild(editBtn);
	actionsTd.appendChild(deleteBtn);
	tr.appendChild(actionsTd);

	return tr;
}

async function loadBooks(searchTerm) {
	const url = searchTerm ? `${BOOKS_API}?search=${encodeURIComponent(searchTerm)}` : BOOKS_API;
	let books;
	try {
		books = await apiFetch(url);
	} catch (e) {
		setStatus(document.getElementById("book-status"), `Konnte Bücher nicht laden: ${e.message}`, true);
		return;
	}
	const tbody = document.getElementById("book-table-body");
	tbody.innerHTML = "";
	for (const book of books) {
		tbody.appendChild(renderBookRow(book));
	}
}

function startEditBook(book) {
	editingBookId = book.id;
	document.getElementById("book-form-title").textContent = `Buch #${book.id} bearbeiten`;
	document.getElementById("book-title").value = book.title;
	document.getElementById("book-isbn").value = book.isbn;
	selectedAuthors = book.authors.map((a) => ({ id: a.id, firstName: a.firstName, lastName: a.lastName }));
	renderAuthorChips();
	document.getElementById("book-submit-btn").textContent = "Speichern";
	document.getElementById("book-cancel-btn").hidden = false;
	setStatus(document.getElementById("book-status"), "", false);
	window.scrollTo({ top: document.getElementById("book-form").offsetTop, behavior: "smooth" });
}

function resetBookForm() {
	editingBookId = null;
	selectedAuthors = [];
	document.getElementById("book-form").reset();
	renderAuthorChips();
	document.getElementById("book-form-title").textContent = "Neues Buch anlegen";
	document.getElementById("book-submit-btn").textContent = "Anlegen";
	document.getElementById("book-cancel-btn").hidden = true;
}

async function submitBookForm(event) {
	event.preventDefault();
	const statusEl = document.getElementById("book-status");
	const title = document.getElementById("book-title").value.trim();
	const isbn = document.getElementById("book-isbn").value.trim();

	if (!title || !isbn) {
		setStatus(statusEl, "Titel und ISBN sind erforderlich.", true);
		return;
	}
	if (selectedAuthors.length === 0) {
		setStatus(statusEl, "Ein Buch benötigt mindestens einen Autor.", true);
		return;
	}

	const payload = { title, isbn, authorIds: selectedAuthors.map((a) => a.id) };
	try {
		if (editingBookId) {
			await apiFetch(`${BOOKS_API}/${editingBookId}`, { method: "PUT", body: JSON.stringify(payload) });
			setStatus(statusEl, "Buch aktualisiert.", false);
		} else {
			await apiFetch(BOOKS_API, { method: "POST", body: JSON.stringify(payload) });
			setStatus(statusEl, "Buch angelegt.", false);
		}
		resetBookForm();
		await loadBooks(document.getElementById("book-search").value.trim());
	} catch (e) {
		setStatus(statusEl, e.message, true);
	}
}

async function deleteBook(id) {
	if (!confirm(`Buch #${id} wirklich löschen?`)) return;
	try {
		await apiFetch(`${BOOKS_API}/${id}`, { method: "DELETE" });
		await loadBooks(document.getElementById("book-search").value.trim());
	} catch (e) {
		setStatus(document.getElementById("book-status"), e.message, true);
	}
}

// ---------- Authors ----------

let editingAuthorId = null;

function renderAuthorRow(author) {
	const tr = document.createElement("tr");
	tr.innerHTML = `
		<td>${author.id}</td>
		<td>${escapeHtml(author.firstName)}</td>
		<td>${escapeHtml(author.lastName)}</td>
		<td>${escapeHtml(author.birthDate)}</td>
	`;

	const actionsTd = document.createElement("td");
	actionsTd.className = "actions";

	const editBtn = document.createElement("button");
	editBtn.type = "button";
	editBtn.textContent = "Bearbeiten";
	editBtn.addEventListener("click", () => startEditAuthor(author));

	const deleteBtn = document.createElement("button");
	deleteBtn.type = "button";
	deleteBtn.textContent = "Löschen";
	deleteBtn.addEventListener("click", () => deleteAuthor(author.id));

	actionsTd.appendChild(editBtn);
	actionsTd.appendChild(deleteBtn);
	tr.appendChild(actionsTd);

	return tr;
}

async function loadAuthors(searchTerm) {
	const url = searchTerm ? `${AUTHORS_API}?search=${encodeURIComponent(searchTerm)}` : AUTHORS_API;
	let authors;
	try {
		authors = await apiFetch(url);
	} catch (e) {
		setStatus(document.getElementById("author-status"), `Konnte Autoren nicht laden: ${e.message}`, true);
		return;
	}
	const tbody = document.getElementById("author-table-body");
	tbody.innerHTML = "";
	for (const author of authors) {
		tbody.appendChild(renderAuthorRow(author));
	}
}

function startEditAuthor(author) {
	editingAuthorId = author.id;
	document.getElementById("author-form-title").textContent = `Autor #${author.id} bearbeiten`;
	document.getElementById("author-first-name").value = author.firstName;
	document.getElementById("author-last-name").value = author.lastName;
	document.getElementById("author-birth-date").value = author.birthDate;
	document.getElementById("author-submit-btn").textContent = "Speichern";
	document.getElementById("author-cancel-btn").hidden = false;
	setStatus(document.getElementById("author-status"), "", false);
	window.scrollTo({ top: document.getElementById("author-form").offsetTop, behavior: "smooth" });
}

function resetAuthorForm() {
	editingAuthorId = null;
	document.getElementById("author-form").reset();
	document.getElementById("author-form-title").textContent = "Neuen Autor anlegen";
	document.getElementById("author-submit-btn").textContent = "Anlegen";
	document.getElementById("author-cancel-btn").hidden = true;
}

async function submitAuthorForm(event) {
	event.preventDefault();
	const statusEl = document.getElementById("author-status");
	const firstName = document.getElementById("author-first-name").value.trim();
	const lastName = document.getElementById("author-last-name").value.trim();
	const birthDate = document.getElementById("author-birth-date").value;

	if (!firstName || !lastName || !birthDate) {
		setStatus(statusEl, "Vorname, Nachname und Geburtsdatum sind erforderlich.", true);
		return;
	}

	const payload = { firstName, lastName, birthDate };
	try {
		if (editingAuthorId) {
			await apiFetch(`${AUTHORS_API}/${editingAuthorId}`, { method: "PUT", body: JSON.stringify(payload) });
			setStatus(statusEl, "Autor aktualisiert.", false);
		} else {
			await apiFetch(AUTHORS_API, { method: "POST", body: JSON.stringify(payload) });
			setStatus(statusEl, "Autor angelegt.", false);
		}
		resetAuthorForm();
		await loadAuthors(document.getElementById("author-search-list").value.trim());
	} catch (e) {
		setStatus(statusEl, e.message, true);
	}
}

async function deleteAuthor(id) {
	if (!confirm(`Autor #${id} wirklich löschen? Bücher, die auf diesen Autor verweisen, zeigen ihn danach als "unbekannt" an.`)) return;
	try {
		await apiFetch(`${AUTHORS_API}/${id}`, { method: "DELETE" });
		await loadAuthors(document.getElementById("author-search-list").value.trim());
	} catch (e) {
		setStatus(document.getElementById("author-status"), e.message, true);
	}
}

// ---------- Shared ----------

function escapeHtml(value) {
	const div = document.createElement("div");
	div.textContent = value ?? "";
	return div.innerHTML;
}

function init() {
	for (const btn of document.querySelectorAll(".tabs button")) {
		btn.addEventListener("click", () => switchTab(btn.dataset.tab));
	}

	document.getElementById("book-search-btn").addEventListener("click", () => {
		loadBooks(document.getElementById("book-search").value.trim());
	});
	document.getElementById("book-search").addEventListener(
		"input",
		debounce((e) => loadBooks(e.target.value.trim()), 300)
	);

	document.getElementById("author-search-btn").addEventListener("click", () => {
		loadAuthors(document.getElementById("author-search-list").value.trim());
	});
	document.getElementById("author-search-list").addEventListener(
		"input",
		debounce((e) => loadAuthors(e.target.value.trim()), 300)
	);

	document.getElementById("author-search").addEventListener(
		"input",
		debounce((e) => showAuthorSuggestions(e.target.value), 250)
	);
	document.addEventListener("click", (e) => {
		if (!e.target.closest(".author-picker")) hideAuthorSuggestions();
	});

	document.getElementById("book-form").addEventListener("submit", submitBookForm);
	document.getElementById("book-cancel-btn").addEventListener("click", resetBookForm);

	document.getElementById("author-form").addEventListener("submit", submitAuthorForm);
	document.getElementById("author-cancel-btn").addEventListener("click", resetAuthorForm);

	loadBooks("");
	loadAuthors("");
}

document.addEventListener("DOMContentLoaded", init);
