package controllers.contacts;

import libraries.collections.MyArrayList;
import libraries.collections.MyOptional;
import models.contacts.EmergencyContactEntry;
import repo.file.FileEmergencyContactRepository;

public class EmergencyContactController {

    private static final String[] KEYS = {
            "HALL_ATTENDANT_1",
            "HALL_ATTENDANT_2",
            "AMBULANCE",
            "MEDICAL_CENTRE",
            "PHARMACY",
            "FIRE_SERVICE",
            "SECURITY_DESK"
    };

    private static final String[] LABELS = {
            "Hall Attendant 1",
            "Hall Attendant 2",
            "Ambulance",
            "Medical Centre",
            "Pharmacy",
            "Fire Service",
            "Security Desk"
    };

    private final FileEmergencyContactRepository repo = new FileEmergencyContactRepository();

    public EmergencyContactController() {
        ensureDefaults();
    }

    public String renderBoard() {
        ensureDefaults();

        StringBuilder sb = new StringBuilder();
        sb.append("EMERGENCY CONTACTS\n");
        sb.append("--------------------------------------------------------------\n");

        for (int i = 0; i < KEYS.length; i++) {
            EmergencyContactEntry entry = getOrDefault(KEYS[i], LABELS[i]);

            sb.append("[").append(i + 1).append("] ").append(entry.getLabel()).append("\n");
            sb.append("    Contact : ").append(show(entry.getContactName())).append("\n");
            sb.append("    Phone   : ").append(show(entry.getPhone())).append("\n");
            if (entry.getNote() != null && !entry.getNote().trim().isEmpty()) {
                sb.append("    Note    : ").append(entry.getNote().trim()).append("\n");
            }
            sb.append("\n");
        }

        sb.append("--------------------------------------------------------------\n");
        return sb.toString();
    }

    public String renderEditMenu() {
        StringBuilder sb = new StringBuilder();
        sb.append("Choose contact to update\n");
        sb.append("[1] Hall Attendant 1\n");
        sb.append("[2] Hall Attendant 2\n");
        sb.append("[3] Ambulance\n");
        sb.append("[4] Medical Centre\n");
        sb.append("[5] Pharmacy\n");
        sb.append("[6] Fire Service\n");
        sb.append("[7] Security Desk\n");
        sb.append("[0] Cancel\n");
        return sb.toString();
    }

    public boolean updateKnownContact(int option,
                                      String contactName,
                                      String phone,
                                      String note,
                                      String updatedBy) {
        if (option < 1 || option > KEYS.length) return false;

        int idx = option - 1;
        repo.upsert(new EmergencyContactEntry(
                KEYS[idx],
                LABELS[idx],
                safe(contactName),
                safe(phone),
                safe(note),
                safe(updatedBy)
        ));
        return true;
    }

    public boolean clearKnownContact(int option, String updatedBy) {
        if (option < 1 || option > KEYS.length) return false;

        int idx = option - 1;
        repo.upsert(new EmergencyContactEntry(
                KEYS[idx],
                LABELS[idx],
                "",
                "",
                "",
                safe(updatedBy)
        ));
        return true;
    }

    private void ensureDefaults() {
        for (int i = 0; i < KEYS.length; i++) {
            if (repo.findByKey(KEYS[i]).isEmpty()) {
                repo.upsert(new EmergencyContactEntry(
                        KEYS[i],
                        LABELS[i],
                        "",
                        "",
                        "",
                        ""
                ));
            }
        }
    }

    private EmergencyContactEntry getOrDefault(String key, String label) {
        MyOptional<EmergencyContactEntry> opt = repo.findByKey(key);
        if (opt.isPresent()) return opt.get();
        return new EmergencyContactEntry(key, label, "", "", "", "");
    }

    private String show(String s) {
        if (s == null || s.trim().isEmpty()) return "(not set)";
        return s.trim();
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
