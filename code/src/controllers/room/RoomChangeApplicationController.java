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
        return approveAndMoveToRoom(applicationId, opt.get().getRequestedRoom(), officerName, note);
    }

    public String approveAndMoveToRoom(String applicationId, String approvedRoom, String officerName, String note) {
        MyOptional<RoomChangeApplication> opt = repo.findById(applicationId);
        if (opt.isEmpty()) {
            return "Application not found.";
        }

        RoomChangeApplication app = opt.get();
        if (app.getStatus() != RoomChangeApplicationStatus.PENDING) {
            return "Only pending applications can be processed.";
        }

        if (approvedRoom == null || approvedRoom.trim().isEmpty()) {
            return "Approved room is required.";
        }

        String targetRoom = approvedRoom.trim();

        if (!roomService.roomExists(targetRoom)) {
            return "Selected room no longer exists.";
        }

        if (!roomService.isRoomAvailable(targetRoom)) {
            return "Selected room is currently full. Move cannot be completed.";
        }

        boolean moved = roomService.changeStudentRoom(app.getStudentId(), targetRoom);
        if (!moved) {
            return "Could not complete room move.";
        }

        app.setStatus(RoomChangeApplicationStatus.COMPLETED);
        app.setReviewedBy(officerName);
        app.setReviewNote(buildApprovalNote(app.getRequestedRoom(), targetRoom, note));
        app.setReviewedAt(nowStamp());

        repo.upsert(app);
        return targetRoom.equalsIgnoreCase(app.getRequestedRoom())
                ? "Room change approved and completed successfully."
                : "Requested room was unavailable, so the student was moved to suggested room " + targetRoom + ".";
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

    private String buildApprovalNote(String requestedRoom, String approvedRoom, String note) {
        if (note != null && !note.trim().isEmpty()) {
            return note.trim();
        }

        if (requestedRoom != null && requestedRoom.trim().equalsIgnoreCase(approvedRoom == null ? "" : approvedRoom.trim())) {
            return "Approved and moved by Hall Officer.";
        }

        return "Approved by Hall Officer and moved to suggested room " + approvedRoom + ".";
    }

    private String nowStamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}