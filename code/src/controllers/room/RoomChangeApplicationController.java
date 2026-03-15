package controllers.room;

import libraries.collections.MyArrayList;
import libraries.collections.MyOptional;
import models.enums.RoomChangeApplicationStatus;
import models.room.RoomChangeApplication;
import repo.file.FileRoomChangeApplicationRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RoomChangeApplicationController {

    private final RoomService roomService;
    private final FileRoomChangeApplicationRepository repo = new FileRoomChangeApplicationRepository();

    public RoomChangeApplicationController(RoomService roomService) {
        this.roomService = roomService;
    }

    public String submitApplication(String studentIdOrName, String requestedRoom, String reason) {
        String studentId = roomService.resolveStudentId(studentIdOrName);
        if (studentId == null || studentId.trim().isEmpty()) {
            return "Could not identify student.";
        }

        String studentName = roomService.getStudentName(studentId);
        String currentRoom = roomService.getStudentRoomNumber(studentId);

        if (currentRoom.equals("UNASSIGNED") || currentRoom.equals("N/A")) {
            return "You do not have an assigned room yet.";
        }

        if (requestedRoom == null || requestedRoom.trim().isEmpty()) {
            return "Requested room is required.";
        }

        if (reason == null || reason.trim().isEmpty()) {
            return "Reason is required.";
        }

        String wantedRoom = requestedRoom.trim();

        if (!roomService.roomExists(wantedRoom)) {
            return "Requested room does not exist.";
        }

        if (wantedRoom.equalsIgnoreCase(currentRoom)) {
            return "Requested room is the same as your current room.";
        }

        MyArrayList<RoomChangeApplication> mine = repo.findByStudentId(studentId);
        for (int i = 0; i < mine.size(); i++) {
            if (mine.get(i).getStatus() == RoomChangeApplicationStatus.PENDING) {
                return "You already have a pending room change application.";
            }
        }

        RoomChangeApplication app = new RoomChangeApplication(
                "RCA-" + System.currentTimeMillis(),
                studentId,
                studentName == null ? studentId : studentName,
                currentRoom,
                wantedRoom,
                reason.trim(),
                RoomChangeApplicationStatus.PENDING,
                nowStamp(),
                "",
                "",
                ""
        );

        repo.save(app);
        return "Room change application submitted successfully!";
    }

    public MyArrayList<RoomChangeApplication> getPendingApplications() {
        return repo.findPending();
    }

    public MyArrayList<RoomChangeApplication> getApplicationsByStudent(String studentIdOrName) {
        String studentId = roomService.resolveStudentId(studentIdOrName);
        if (studentId == null || studentId.trim().isEmpty()) {
            return new MyArrayList<>();
        }
        return repo.findByStudentId(studentId);
    }

    public MyOptional<RoomChangeApplication> getById(String applicationId) {
        return repo.findById(applicationId);
    }

    public String approveAndMove(String applicationId, String officerName, String note) {
        MyOptional<RoomChangeApplication> opt = repo.findById(applicationId);
        if (opt.isEmpty()) {
            return "Application not found.";
        }

        RoomChangeApplication app = opt.get();
        if (app.getStatus() != RoomChangeApplicationStatus.PENDING) {
            return "Only pending applications can be processed.";
        }

        if (!roomService.roomExists(app.getRequestedRoom())) {
            return "Requested room no longer exists.";
        }

        if (!roomService.isRoomAvailable(app.getRequestedRoom())) {
            return "Requested room is currently full. Move cannot be completed.";
        }

        boolean moved = roomService.changeStudentRoom(app.getStudentId(), app.getRequestedRoom());
        if (!moved) {
            return "Could not complete room move.";
        }

        app.setStatus(RoomChangeApplicationStatus.COMPLETED);
        app.setReviewedBy(officerName);
        app.setReviewNote(note == null || note.trim().isEmpty()
                ? "Approved and moved by Hall Officer."
                : note.trim());
        app.setReviewedAt(nowStamp());

        repo.upsert(app);
        return "Room change approved and completed successfully.";
    }

    public String reject(String applicationId, String officerName, String note) {
        MyOptional<RoomChangeApplication> opt = repo.findById(applicationId);
        if (opt.isEmpty()) {
            return "Application not found.";
        }

        RoomChangeApplication app = opt.get();
        if (app.getStatus() != RoomChangeApplicationStatus.PENDING) {
            return "Only pending applications can be processed.";
        }

        app.setStatus(RoomChangeApplicationStatus.REJECTED);
        app.setReviewedBy(officerName);
        app.setReviewNote(note == null || note.trim().isEmpty()
                ? "Rejected by Hall Officer."
                : note.trim());
        app.setReviewedAt(nowStamp());

        repo.upsert(app);
        return "Room change application rejected.";
    }

    private String nowStamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}