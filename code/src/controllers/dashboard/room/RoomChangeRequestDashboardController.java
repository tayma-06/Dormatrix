package controllers.dashboard.room;

import controllers.room.RoomChangeApplicationController;
import controllers.room.RoomService;
import libraries.collections.MyArrayList;
import libraries.collections.MyOptional;
import models.room.Room;
import models.room.RoomChangeApplication;

import java.util.List;

public class RoomChangeRequestDashboardController {

    private final RoomChangeApplicationController controller;
    private final RoomService roomService;

    public RoomChangeRequestDashboardController() {
        this.roomService = new RoomService();
        this.controller = new RoomChangeApplicationController(roomService);
    }

    public MyArrayList<RoomChangeApplication> getPendingApplications() {
        return controller.getPendingApplications();
    }

    public MyOptional<RoomChangeApplication> getById(String applicationId) {
        return controller.getById(applicationId);
    }

    public boolean isRequestedRoomAvailable(RoomChangeApplication app) {
        return app != null && roomService.isRoomAvailable(app.getRequestedRoom());
    }

    public List<Room> getSuggestedRooms(RoomChangeApplication app, int maxCount) {
        if (app == null) {
            return java.util.Collections.emptyList();
        }
        return roomService.getSuggestedRooms(app.getCurrentRoom(), app.getRequestedRoom(), maxCount);
    }

    public String approveAndMove(String applicationId, String officerName, String note) {
        return controller.approveAndMove(applicationId, officerName, note);
    }

    public String approveAndMoveToSuggestedRoom(String applicationId, String approvedRoom, String officerName, String note) {
        return controller.approveAndMoveToRoom(applicationId, approvedRoom, officerName, note);
    }

    public String reject(String applicationId, String officerName, String note) {
        return controller.reject(applicationId, officerName, note);
    }
}