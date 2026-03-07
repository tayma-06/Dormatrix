package cli.views.complaint;

import libraries.collections.MyArrayList;
import libraries.collections.MyString;
import models.complaints.Complaint;

import java.util.ArrayList;
import java.util.List;

public class ComplaintView {

    public void studentMenu(){
        System.out.println();
        System.out.println("═══════════════════════════════════════════════════════════════════════");
        System.out.println("║                      COMPLAINT(STUDENT)                             ║");
        System.out.println("═══════════════════════════════════════════════════════════════════════");
        System.out.println("1. File a Complaint");
        System.out.println("2. View My Complaints");
//        System.out.println("3. Track Complaint by ID");
        System.out.println("0. Back");
        System.out.print("Enter choice: ");
    }

    public void attendantMenu(){
        System.out.println();
        System.out.println("═══════════════════════════════════════════════════════════════════════");
        System.out.println("║                      COMPLAINT(ATTENDANT)                           ║");
        System.out.println("═══════════════════════════════════════════════════════════════════════");
        System.out.println("1. View ALL complaints");
        System.out.println("2. View PENDING");
        System.out.println("3. Reassign complaint (manual worker id)");
        System.out.println("4. Resolve complaint");
        System.out.println("5. View complaints by ROOM");
        System.out.println("6. View complaints by COMPLAINT ID");
        System.out.println("0. Back");
        System.out.print("Enter choice: ");
    }

    public void workerMenu(){
        System.out.println();
        System.out.println("════════════════════════════════════════════════════════════════════════");
        System.out.println("║                       TASK RELEVANT OPTIONS                          ║");
        System.out.println("════════════════════════════════════════════════════════════════════════");
        System.out.println("1. Update Progress");
//        System.out.println("3. Mark Completed");
        System.out.println("0. Back");
        System.out.println();
        System.out.print("Enter choice: ");
    }

    public void msg(String s){ System.out.println(s); }
    public void error(String s){ System.out.println("Error: " + s); }

    public void filed(Complaint c){
        System.out.println("\nComplaint Filed Successfully!");
        System.out.println("Complaint ID : " + c.getComplaintId());
        System.out.println("Status       : " + c.getStatus().name());
        String wid = c.getAssignedWorkerId();
        boolean blank = (wid == null) || new MyString(wid).trim().isEmpty();
        System.out.println("Assigned To  : " + (blank ? "(not assigned yet)" : wid));
        System.out.println();
    }

    public void studentList(MyArrayList<Complaint> list) {
        if (list == null || list.size() == 0) {
            System.out.println("\n(No complaints found)\n");
            return;
        }

        System.out.println("╔═════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                          YOUR COMPLAINTS                                ║");
        System.out.println("╠═════════════════════════════════════════════════════════════════════════╣");

        System.out.println(String.format("║ %-71s ║", "Total Complaints: " + list.size()));
        System.out.println("╠═════════════════════╦════════════╦═════════════════╦════════════════════╣");

        if (list.size() == 0) {
            System.out.println(String.format("║ %-67s ║", "No complaints found. Please file a complaint."));
        } else {
            // Column Headers
            System.out.println("║ COMPLAINT ID        ║ STATUS     ║ CATEGORY        ║ ASSIGNED WORKER    ║");
            System.out.println("╠═════════════════════╬════════════╬═════════════════╬════════════════════╣");

            for (int i = 0; i < list.size(); i++) {
                Complaint c = list.get(i);
                String wid = c.getAssignedWorkerId();
                boolean blank = (wid == null) || new MyString(wid).trim().isEmpty();

                System.out.println(String.format(
                        "║ %-19s ║ %-10s ║ %-15s ║ %-18s ║",
                        c.getComplaintId(),
                        c.getStatus().name(),
                        c.getCategory().name(),
                        (blank ? "(none)" : wid)
                ));
            }
        }

        System.out.println("╠═════════════════════╩════════════╩═════════════════╩════════════════════╣");
        System.out.println("║ Press Enter to return...                                                ║");
        System.out.println("╚═════════════════════════════════════════════════════════════════════════╝");
    }

    public void attendantList(MyArrayList<Complaint> list){
        if (list == null || list.size() == 0){
            System.out.println("\n(No complaints found)\n");
            return;
        }

        int descriptionMaxLength = 59;

        System.out.println("════════════════════════════════════════════════════════════════════════════");
        System.out.println("║                                 LIST                                     ║");
        System.out.println("════════════════════════════════════════════════════════════════════════════");
        for (int i = 0; i < list.size(); i++) {
            Complaint c = list.get(i);
            String wid = c.getAssignedWorkerId();
            boolean blank = (wid == null) || new MyString(wid).trim().isEmpty();

            // Start the double line for each complaint
            System.out.println("╠══════════════════════════════════════════════════════════════════════════╣");

            System.out.println(String.format("║ Complaint ID: %-58s ║", c.getComplaintId()));
            System.out.println(String.format("║ Student ID  : %-58s ║", c.getStudentId()));
            System.out.println(String.format("║ Room No     : %-58s ║", c.getStudentRoomNo()));
            System.out.println(String.format("║ Status      : %-58s ║", c.getStatus().name()));
            System.out.println(String.format("║ Priority    : %-58s ║", c.getPriority().name()));
            System.out.println(String.format("║ Category    : %-58s ║", c.getCategory().name()));
            System.out.println(String.format("║ Assigned Worker: %-55s ║", (blank ? "(none)" : wid)));
            // Wrap the description into lines of max length
            String description = c.getDescription();
            if (description != null && !description.isEmpty()) {
                // Split the description into multiple lines
                String[] descriptionLines = wrapText(description, descriptionMaxLength);
                for (int t = 0; t < descriptionLines.length; t++) {
                    if (t == 0) System.out.println(String.format("║ Description: %-59s ║", descriptionLines[t]));
                    else        System.out.println(String.format("║            : %-59s ║", descriptionLines[t])); // aligned continuation
                }
            } else {
                System.out.println("║ Description: (no description)                               ║");
            }

            // ---------------- TAGS ----------------
            String tagsRaw = c.getTags();
            if (tagsRaw != null) tagsRaw = tagsRaw.trim();

            if (tagsRaw == null || tagsRaw.isEmpty()) {
                System.out.println(String.format("║ Tags       : %-59s ║", "(none)"));
            } else {
                // supports both comma + semicolon in stored tags, but prints using ';'
                String[] tagLines = wrapTags(tagsRaw, 59);

                for (int t = 0; t < tagLines.length; t++) {
                    if (t == 0) System.out.println(String.format("║ Tags       : %-59s ║", tagLines[t]));
                    else        System.out.println(String.format("║            : %-59s ║", tagLines[t])); // aligned continuation
                }
            }

            // End the double line for each complaint
            System.out.println("╚══════════════════════════════════════════════════════════════════════════╝");
        }
    }

    public void workerList(MyArrayList<Complaint> list) {
        if (list == null || list.size() == 0) {
            System.out.println("\n(No complaints found)\n");
            return;
        }

        int descriptionMaxLength = 59;

        System.out.println("════════════════════════════════════════════════════════════════════════════");
        System.out.println("║                                 LIST                                     ║");
        System.out.println("════════════════════════════════════════════════════════════════════════════");

        for (int i = 0; i < list.size(); i++) {
            Complaint c = list.get(i);  // Access complaint at index i
            String wid = c.getAssignedWorkerId();
            boolean blank = (wid == null) || new MyString(wid).trim().isEmpty();

            // Start the double line for each complaint
            System.out.println("╠══════════════════════════════════════════════════════════════════════════╣");

            // Display Complaint details (excluding student ID)
            System.out.println(String.format("║ Complaint ID: %-58s ║", c.getComplaintId()));
            System.out.println(String.format("║ Room No     : %-58s ║", c.getStudentRoomNo()));
            System.out.println(String.format("║ Status      : %-58s ║", c.getStatus().name()));
            System.out.println(String.format("║ Priority    : %-58s ║", c.getPriority().name()));
//            System.out.println(String.format("║ Assigned Worker: %-55s ║", (blank ? "(none)" : wid)));

            // Wrap the description into lines of max length
            String description = c.getDescription();
            if (description != null && !description.isEmpty()) {
                // Split the description into multiple lines
                String[] descriptionLines = wrapText(description, descriptionMaxLength);
                for (String line : descriptionLines) {
                    System.out.println(String.format("║ Description : %-58s ║", line));
                }
            } else {
                System.out.println("║ Description: (no description)                               ║");
            }

            // End the double line for each complaint
            System.out.println("╚══════════════════════════════════════════════════════════════════════════╝");
        }
    }

    // Helper method to split text into lines of a certain maximum length
    private String[] wrapText(String text, int maxLength) {
        List<String> lines = new ArrayList<>();
        while (text.length() > maxLength) {
            int spaceIndex = text.lastIndexOf(' ', maxLength);
            if (spaceIndex == -1) {
                spaceIndex = maxLength;  // If no space found, break at maxLength
            }
            lines.add(text.substring(0, spaceIndex));
            text = text.substring(spaceIndex).trim();
        }
        lines.add(text);  // Add the remaining part of the text
        return lines.toArray(new String[0]);


    }

    private String[] wrapTags(String tags, int maxLength) {
        // normalize: your policy sometimes uses ',' but notes use ';'
        String normalized = tags.replace(',', ';');

        // split by ';', trim, ignore empties
        String[] rawParts = normalized.split(";");
        List<String> parts = new ArrayList<>();
        for (int i = 0; i < rawParts.length; i++) {
            String p = rawParts[i].trim();
            if (!p.isEmpty()) parts.add(p);
        }

        // pack tags into lines <= maxLength, joined by ';'
        List<String> lines = new ArrayList<>();
        StringBuilder cur = new StringBuilder();

        for (int i = 0; i < parts.size(); i++) {
            String tag = parts.get(i);

            if (cur.length() == 0) {
                cur.append(tag);
            } else {
                String candidate = cur.toString() + ";" + tag;
                if (candidate.length() <= maxLength) {
                    cur.append(";").append(tag);
                } else {
                    lines.add(cur.toString());
                    cur.setLength(0);
                    cur.append(tag);
                }
            }
        }

        if (cur.length() > 0) lines.add(cur.toString());

        return lines.toArray(new String[0]);
    }

//    public void details(Complaint c){
//        String wid = c.getAssignedWorkerId();
//        boolean blank = (wid == null) || new MyString(wid).trim().isEmpty();
//
//        System.out.println("\n------------------ COMPLAINT DETAILS ------------------");
//        System.out.println("ID          : " + c.getComplaintId());
//        System.out.println("Student ID  : " + c.getStudentId());
//        System.out.println("Room        : " + c.getStudentRoomNo());
//        System.out.println("Category    : " + c.getCategory().name());
//        System.out.println("Priority    : " + c.getPriority().name());
//        System.out.println("Status      : " + c.getStatus().name());
//        System.out.println("Worker      : " + (blank ? "(none)" : wid));
//        System.out.println("Description : " + c.getDescription());
//        System.out.println("Notes/Tags  : " + (c.getTags() == null ? "" : c.getTags()));
//        System.out.println("-------------------------------------------------------\n");
//    }
}
