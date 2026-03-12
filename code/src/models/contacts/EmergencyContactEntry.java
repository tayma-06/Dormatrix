package models.contacts;

public class EmergencyContactEntry {

    private final String key;
    private final String label;
    private final String contactName;
    private final String phone;
    private final String note;
    private final String updatedBy;

    public EmergencyContactEntry(String key,
                                 String label,
                                 String contactName,
                                 String phone,
                                 String note,
                                 String updatedBy) {
        this.key = key;
        this.label = label;
        this.contactName = contactName;
        this.phone = phone;
        this.note = note;
        this.updatedBy = updatedBy;
    }

    public String getKey() { return key; }
    public String getLabel() { return label; }
    public String getContactName() { return contactName; }
    public String getPhone() { return phone; }
    public String getNote() { return note; }
    public String getUpdatedBy() { return updatedBy; }
}
