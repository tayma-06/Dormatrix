package controllers.dashboard.room;

import controllers.room.RoomChangeApplicationController;
import controllers.room.RoomService;
import libraries.collections.MyArrayList;
import models.complaints.Complaint;
import models.room.Room;
import models.room.RoomChangeApplication;

import java.util.List;

public class StudentRoomDashboardController {

    private final RoomService roomService;
    private final RoomChangeApplicationController applicationController;

    public StudentRoomDashboardController(RoomService roomService) {
        this.roomService = roomService;
        this.applicationController = new RoomChangeApplicationController(roomService);
    }

    public String getStudentRoomNumber(String studentIdOrName) {
        return roomService.getStudentRoomNumber(studentIdOrName);
    }

    public Room getRoomDetails(String roomNumber) {
        if (roomNumber == null || roomNumber.equals("UNASSIGNED") || roomNumber.equals("N/A")) {
            return null;
        }
        return roomService.getRoomDetailsWithRealOccupancy(roomNumber);
    }

    public List<Room> getAvailableRooms() {
        return roomService.getAvailableRooms();
    }

    public MyArrayList<Complaint> getComplaints(String roomNumber) {
        return roomService.getComplaintsForRoom(roomNumber);
    }

    public String submitRoomChangeApplication(String studentIdOrName, String requestedRoom, String reason) {
        return applicationController.submitApplication(studentIdOrName, requestedRoom, reason);
    }

    public MyArrayList<RoomChangeApplication> getRoomChangeApplications(String studentIdOrName) {
        return applicationController.getApplicationsByStudent(studentIdOrName);
    }
}