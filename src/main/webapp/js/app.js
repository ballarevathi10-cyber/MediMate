
document.addEventListener("DOMContentLoaded", function () {

    // ==============================
    // WATER DASHBOARD
    // ==============================

    const waterGlasses = document.getElementById("waterGlasses");
    const waterAmount = document.getElementById("waterAmount");
    const waterStatText = document.getElementById("waterStatText");
    const waterBar = document.getElementById("waterBar");

    function updateWaterDashboard() {
        const today = new Date().toDateString();
        const savedDate = localStorage.getItem("medimateWaterDate");
        let amount = 0;

        if (savedDate === today) {
            amount = Number(localStorage.getItem("medimateWater")) || 0;
        }

        if (waterGlasses) {
            waterGlasses.textContent = Math.floor(amount / 250);
        }

        if (waterAmount) {
            waterAmount.textContent = amount;
        }

        if (waterStatText) {
            waterStatText.textContent = amount + " ml recorded today";
        }

        if (waterBar) {
            waterBar.style.width =
                Math.min((amount / 2000) * 100, 100) + "%";
        }
    }

    // ==============================
    // MEDICINE DASHBOARD
    // ==============================

    function updateMedicineDashboard() {
        let medicines = [];

        try {
            medicines = JSON.parse(
                localStorage.getItem("medimateMedicines") || "[]"
            );

            if (!Array.isArray(medicines)) medicines = [];
        } catch (error) {
            medicines = [];
        }

        const count = document.getElementById("medicineCount");

        if (count) {
            count.textContent = medicines.length + " reminders";
        }

        const list = document.getElementById("medicineList");

        if (!list) return;

        list.replaceChildren();

        if (medicines.length === 0) {
            const empty = document.createElement("div");
            empty.className = "empty-state";
            empty.textContent = "No medicine schedules added yet.";
            list.appendChild(empty);
            return;
        }

        medicines.slice(0, 4).forEach(function (medicine) {
            const item = document.createElement("div");
            item.className = "medicine-item";

            const icon = document.createElement("div");
            icon.className = "item-icon";
            icon.textContent = "💊";

            const info = document.createElement("div");
            info.className = "item-info";

            const name = document.createElement("h4");
            name.textContent = medicine.name || "Medicine";

            const details = document.createElement("p");
            details.textContent = medicine.dose || "Prescribed schedule";

            const time = document.createElement("span");
            time.className = "item-status";
            time.textContent = medicine.time || "";

            info.append(name, details);
            item.append(icon, info, time);
            list.appendChild(item);
        });
    }

    // ==============================
    // MEDICAL REPORTS DASHBOARD
    // ==============================

    function updateReportDashboard() {
        let reports = [];

        try {
            reports = JSON.parse(
                localStorage.getItem("medimateReports") || "[]"
            );

            if (!Array.isArray(reports)) reports = [];
        } catch (error) {
            reports = [];
        }

        const count = document.getElementById("reportCount");

        if (count) {
            count.textContent = reports.length + " reports";
        }

        const list = document.getElementById("recentReports");

        if (!list) return;

        list.replaceChildren();

        if (reports.length === 0) {
            const empty = document.createElement("div");
            empty.className = "empty-state";
            empty.textContent = "No medical records added yet.";
            list.appendChild(empty);
            return;
        }

        reports.slice(-4).reverse().forEach(function (report) {
            const item = document.createElement("div");
            item.className = "report-item";

            const icon = document.createElement("div");
            icon.className = "item-icon";
            icon.textContent = "📄";

            const info = document.createElement("div");
            info.className = "item-info";

            const name = document.createElement("h4");
            name.textContent = report.title || "Medical report";

            const details = document.createElement("p");
            details.textContent =
                (report.category || "Report") + " · " +
                (report.date || "");

            info.append(name, details);
            item.append(icon, info);
            list.appendChild(item);
        });
    }

    // ==============================
    // TODAY'S DATE
    // ==============================

    const dateElement = document.getElementById("todayDate");

    if (dateElement) {
        dateElement.textContent = new Date().toLocaleDateString("en-IN", {
            day: "numeric",
            month: "short",
            year: "numeric"
        });
    }

    updateWaterDashboard();
    updateMedicineDashboard();
    updateReportDashboard();

    // ==============================
    // QUICK WATER BUTTONS
    // ==============================

    const addWaterBtn = document.getElementById("addWaterBtn");
    const undoWaterBtn = document.getElementById("undoWaterBtn");

    function saveDashboardWater(amount) {
        const today = new Date().toDateString();
        const savedDate = localStorage.getItem("medimateWaterDate");
        let current = 0;

        if (savedDate === today) {
            current = Number(localStorage.getItem("medimateWater")) || 0;
        }

        current = Math.max(0, current + amount);

        localStorage.setItem("medimateWater", current);
        localStorage.setItem("medimateWaterDate", today);

        updateWaterDashboard();
    }

    if (addWaterBtn) {
        addWaterBtn.addEventListener("click", function () {
            saveDashboardWater(250);
        });
    }

    if (undoWaterBtn) {
        undoWaterBtn.addEventListener("click", function () {
            saveDashboardWater(-250);
        });
    }

    // ==============================
    // MEDIMATE AI CHAT
    // ==============================

    const chatForm = document.getElementById("chatForm");
    const chatInput = document.getElementById("message");
    const chatMessages = document.getElementById("chatMessages");

    function addChatMessage(text, sender) {
        if (!chatMessages) return;

        const messageElement = document.createElement("div");
        messageElement.className = "chat-message " + sender;

        // Use textContent so AI output is treated as text, not HTML.
        messageElement.textContent = text;

        chatMessages.appendChild(messageElement);
        chatMessages.scrollTop = chatMessages.scrollHeight;

        return messageElement;
    }

    if (chatForm && chatInput && chatMessages) {
        chatForm.addEventListener("submit", async function (event) {
            event.preventDefault();

            const question = chatInput.value.trim();

            if (!question) return;

            addChatMessage(question, "user");
            chatInput.value = "";

            const loadingMessage = addChatMessage(
                "MediMate is thinking...",
                "assistant"
            );

            const submitButton = chatForm.querySelector(
                'button[type="submit"]'
            );

            if (submitButton) submitButton.disabled = true;

            try {
                const response = await fetch("AIChatServlet", {
                    method: "POST",
                    headers: {
                        "Content-Type":
                            "application/x-www-form-urlencoded;charset=UTF-8"
                    },
                    body: new URLSearchParams({
                        message: question
                    })
                });

                const data = await response.json();

                if (!response.ok) {
                    throw new Error(
                        data.error || "AI service request failed."
                    );
                }

                // Groq chat-completions response format
                const answer =
                    data.choices &&
                    data.choices[0] &&
                    data.choices[0].message
                        ? data.choices[0].message.content
                        : null;

                if (!answer) {
                    throw new Error("No answer was returned by the AI.");
                }

                if (loadingMessage) {
                    loadingMessage.textContent = answer;
                }

            } catch (error) {
                if (loadingMessage) {
                    loadingMessage.textContent =
                        "Sorry, AI connection failed: " + error.message;
                }
                console.error("MediMate AI error:", error);

            } finally {
                if (submitButton) submitButton.disabled = false;
                chatInput.focus();
            }
        });
    }

});
