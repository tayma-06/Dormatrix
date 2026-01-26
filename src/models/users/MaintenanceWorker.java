package models.users;

import models.enums.WorkerField;

public class MaintenanceWorker extends User{
    private final WorkerField field;

    public MaintenanceWorker(String id, String name, String role, String passwordHash, String phoneNumber, WorkerField field) {
        super(id, name, role, passwordHash, phoneNumber);
        this.field = field;
    }

    public WorkerField getField(){ return field; }

    @Override
    public String toFileString() {
        return id + "|" + name + "|MAINTENANCE_WORKER|"  + "|" + passwordHash + "|" + phoneNumber+ "|"+ field;
    }
}
