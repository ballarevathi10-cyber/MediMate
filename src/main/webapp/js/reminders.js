
// MediMate AI - Reminders

document.addEventListener("DOMContentLoaded", function () {
    const medicines = JSON.parse(
        localStorage.getItem("medimateMedicines") || "[]"
    );

    // Ask permission for browser notifications
    if ("Notification" in window && Notification.permission === "default") {
        Notification.requestPermission();
    }

    // Check medicine reminders every 30 seconds
    function checkMedicineReminders() {
        const now = new Date();
        const currentTime = now.toTimeString().slice(0, 5);
        const today = now.toISOString().slice(0, 10);

        const latestMedicines = JSON.parse(
            localStorage.getItem("medimateMedicines") || "[]"
        );

        latestMedicines.forEach(function (medicine) {
            const reminderTime = medicine.time || medicine.reminderTime;

            if (!reminderTime || reminderTime.slice(0, 5) !== currentTime) {
                return;
            }

            const reminderKey =
                "medimateReminder_" + today + "_" +
                (medicine.id || medicine.name) + "_" + currentTime;

            if (localStorage.getItem(reminderKey)) {
                return;
            }

            alert("Medicine reminder: " + medicine.name +
                (medicine.dose ? "\nDose: " + medicine.dose : ""));

            if ("Notification" in window &&
                Notification.permission === "granted") {
                new Notification("MediMate AI Reminder", {
                    body: "Reminder for: " + medicine.name
                });
            }

            localStorage.setItem(reminderKey, "shown");
        });
    }

    checkMedicineReminders();
    setInterval(checkMedicineReminders, 30000);

    // Water reminder every 60 minutes while this page is open
    setInterval(function () {
        if ("Notification" in window &&
            Notification.permission === "granted") {
            new Notification("MediMate AI 💧", {
                body: "Time to check whether you need some water."
            });
        } else {
            alert("MediMate AI 💧: Remember to stay hydrated!");
        }
    }, 60 * 60 * 1000);
});

