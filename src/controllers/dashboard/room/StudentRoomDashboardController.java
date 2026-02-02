package controllers.dashboard.room;

import controllers.room.RoomService;
import libraries.collections.MyArrayList;
import models.complaints.Complaint;
import models.room.Room;

public class StudentRoomDashboardController {

    private final RoomService roomService;

    public StudentRoomDashboardController(RoomService roomService) {
        this.roomService = roomService;
    }

    public String getStudentRoomNumber(String studentIdOrName) {
        return roomService.getStudentRoomNumber(studentIdOrName);
    }

    public Room getRoomDetails(String roomNumber) {
        if (roomNumber.equals("UNASSIGNED") || roomNumber.equals("N/A")) return null;
        return roomService.getRoomDetailsWithRealOccupancy(roomNumber);
    }

    public MyArrayList<Complaint> getComplaints(String roomNumber) {
        return roomService.getComplaintsForRoom(roomNumber);
    }
}
