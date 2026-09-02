const wsUri = "ws://localhost:8080/ws/messages";
let websocket = null;
let counter = 0;

const logElement = document.querySelector("#log");
function log(text) {
	logElement.innerText = `${logElement.innerText}${text}\n`;
	logElement.scrollTop = logElement.scrollHeight;
}

function initializeWebSocketListeners(ws) {
	ws.addEventListener("open", () => {
		log("CONNECTED");
	});

	ws.addEventListener("close", () => {
		log("DISCONNECTED");
	});

	ws.addEventListener("message", (e) => {
		log(`RECEIVED: ${e.data}: ${counter}`);
		counter++;
	});

	ws.addEventListener("error", (e) => {
		log(`ERROR`);
		console.log('e');
	});
}

window.addEventListener("pageshow", (event) => {
	if (event.persisted) {
		websocket = new WebSocket(wsUri);
		initializeWebSocketListeners(websocket);
	}
});

log("OPENING");
websocket = new WebSocket(wsUri);
initializeWebSocketListeners(websocket);

// Close the websocket when the user leaves.
window.addEventListener("pagehide", () => {
	if (websocket) {
		log("CLOSING");
		websocket.close();
		websocket = null;
	}
});

