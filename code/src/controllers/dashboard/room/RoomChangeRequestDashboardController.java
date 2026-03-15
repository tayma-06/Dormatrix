package controllers.dashboard.room;

import controllers.room.RoomChangeApplicationController;
import controllers.room.RoomService;
import libraries.collections.MyArrayList;
import libraries.collections.MyOptional;
import models.room.RoomChangeApplication;

public class RoomChangeRequestDashboardController {

    private final RoomChangeApplicationController controller;

    public RoomChangeRequestDashboardController() {
        this.controller = new RoomChangeApplicationController(new RoomService());
    }

    public MyArrayList<RoomChangeApplication> getPendingApplications() {
        return controller.getPendingApplications();
    }

    public MyOptional<RoomChangeApplication> getById(String applicationId) {
        return controller.getById(applicationId);
    }

    public String approveAndMove(String applicationId, String officerName, String note) {
        return controller.approveAndMove(applicationId, officerName, note);
    }

    public String reject(String applicationId, String officerName, String note) {
        return controller.reject(applicationId, officerName, note);
    }
}