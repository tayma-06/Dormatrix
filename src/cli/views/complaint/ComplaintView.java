package cli.views.complaint;

import libraries.collections.MyArrayList;
import libraries.collections.MyString;
import models.complaints.Complaint;
import utils.TerminalUI;

import java.util.ArrayList;
import java.util.List;

public class ComplaintView {

    public void studentMenu() {
        TerminalUI.tSubDashboard("COMPLAINT (STUDENT)", new String[]{
            "[1] File a Complaint",
            "[2] View My Complaints",
            "[0] Back"
        });
    }

    public void attendantMenu() {
        TerminalUI.tSubDashboard("COMPLAINT (ATTENDANT)", new String[]{
            "[1] View ALL complaints",
            "[2] View PENDING",
            "[3] Reassign complaint (manual worker id)",
            "[4] Resolve complaint",
            "[5] View complaints by ROOM",
            "[6] View complaints by COMPLAINT ID",
            "[0] Back"
        });
    }

    public void workerMenu() {
        TerminalUI.tSubDashboard("TASK RELEVANT OPTIONS", new String[]{
            "[1] Update Progress",
            "[0] Back"
        });
    }

    public void msg(String s) {
        TerminalUI.tPrint(s);
    }

    public void error(String s) {
        TerminalUI.tError(s);
    }

    public void filed(Complaint c) {
        String wid = c.getAssignedWorkerId();
        boolean blank = (wid == null) || new MyString(wid).trim().isEmpty();
        TerminalUI.tEmpty();
        TerminalUI.tSuccess("Complaint Filed Successfully!");
        TerminalUI.tBoxTop();
        TerminalUI.tBoxLine("Complaint ID : " + c.getComplaintId());
        TerminalUI.tBoxLine("Status       : " + c.getStatus().name());
        TerminalUI.tBoxLine("Assigned To  : " + (blank ? "(not assigned yet)" : wid));
        TerminalUI.tBoxBottom();
        TerminalUI.tEmpty();
    }

    public void studentList(MyArrayList<Complaint> list) {
        if (list == null || list.size() == 0) {
            TerminalUI.tEmpty();
            TerminalUI.tPrint("(No complaints found)");
            TerminalUI.tEmpty();
            return;
        }

        TerminalUI.tBoxTop();
        TerminalUI.tBoxTitle("YOUR COMPLAINTS");
        TerminalUI.tBoxSep();
        TerminalUI.tBoxLine("Total Complaints: " + list.size());
        TerminalUI.tBoxSep();
        TerminalUI.tBoxLine("ID                  | STATUS     | CATEGORY        | WORKER");
        TerminalUI.tBoxSep();

        for (int i = 0; i < list.size(); i++) {
            Complaint c = list.get(i);
            String wid = c.getAssignedWorkerId();
            boolean blank = (wid == null) || new MyString(wid).trim().isEmpty();
            TerminalUI.tBoxLine(String.format("%-19s | %-10s | %-15s | %s",
                    c.getComplaintId(), c.getStatus().name(),
                    c.getCategory().name(), (blank ? "(none)" : wid)));
        }

        TerminalUI.tBoxSep();
        TerminalUI.tBoxLine("Press Enter to continue...");
        TerminalUI.tBoxBottom();
    }

    public void attendantList(MyArrayList<Complaint> list) {
        if (list == null || list.size() == 0) {
            TerminalUI.tEmpty();
            TerminalUI.tPrint("(No complaints found)");
            TerminalUI.tEmpty();
            return;
        }

        int descriptionMaxLength = 50;

        TerminalUI.tBoxTop();
        TerminalUI.tBoxTitle("COMPLAINT LIST");
        TerminalUI.tBoxSep();

        for (int i = 0; i < list.size(); i++) {
            Complaint c = list.get(i);
            String wid = c.getAssignedWorkerId();
            boolean blank = (wid == null) || new MyString(wid).trim().isEmpty();

            if (i > 0) {
                TerminalUI.tBoxSep();
            }

            TerminalUI.tBoxLine("Complaint ID: " + c.getComplaintId());
            TerminalUI.tBoxLine("Student ID  : " + c.getStudentId());
            TerminalUI.tBoxLine("Room No     : " + c.getStudentRoomNo());
            TerminalUI.tBoxLine("Status      : " + c.getStatus().name());
            TerminalUI.tBoxLine("Priority    : " + c.getPriority().name());
            TerminalUI.tBoxLine("Category    : " + c.getCategory().name());
            TerminalUI.tBoxLine("Worker      : " + (blank ? "(none)" : wid));

            String description = c.getDescription();
            if (description != null && !description.isEmpty()) {
                String[] descriptionLines = wrapText(description, descriptionMaxLength);
                for (int t = 0; t < descriptionLines.length; t++) {
                    if (t == 0) {
                        TerminalUI.tBoxLine("Description : " + descriptionLines[t]);
                    } else {
                        TerminalUI.tBoxLine("            : " + descriptionLines[t]);
                    }
                }
            } else {
                TerminalUI.tBoxLine("Description : (no description)");
            }

            String tagsRaw = c.getTags();
            if (tagsRaw != null) {
                tagsRaw = tagsRaw.trim();
            }
            if (tagsRaw == null || tagsRaw.isEmpty()) {
                TerminalUI.tBoxLine("Tags        : (none)");
            } else {
                String[] tagLines = wrapTags(tagsRaw, 50);
                for (int t = 0; t < tagLines.length; t++) {
                    if (t == 0) {
                        TerminalUI.tBoxLine("Tags        : " + tagLines[t]);
                    } else {
                        TerminalUI.tBoxLine("            : " + tagLines[t]);
                    }
                }
            }
        }
        TerminalUI.tBoxBottom();
    }

    public void workerList(MyArrayList<Complaint> list) {
        if (list == null || list.size() == 0) {
            TerminalUI.tEmpty();
            TerminalUI.tPrint("(No complaints found)");
            TerminalUI.tEmpty();
            return;
        }

        int descriptionMaxLength = 50;

        TerminalUI.tBoxTop();
        TerminalUI.tBoxTitle("TASK LIST");
        TerminalUI.tBoxSep();

        for (int i = 0; i < list.size(); i++) {
            Complaint c = list.get(i);

            if (i > 0) {
                TerminalUI.tBoxSep();
            }

            TerminalUI.tBoxLine("Complaint ID: " + c.getComplaintId());
            TerminalUI.tBoxLine("Room No     : " + c.getStudentRoomNo());
            TerminalUI.tBoxLine("Status      : " + c.getStatus().name());
            TerminalUI.tBoxLine("Priority    : " + c.getPriority().name());

            String description = c.getDescription();
            if (description != null && !description.isEmpty()) {
                String[] descriptionLines = wrapText(description, descriptionMaxLength);
                for (String line : descriptionLines) {
                    TerminalUI.tBoxLine("Description : " + line);
                }
            } else {
                TerminalUI.tBoxLine("Description : (no description)");
            }
        }
        TerminalUI.tBoxBottom();
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
            if (!p.isEmpty()) {
                parts.add(p);
            }
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

        if (cur.length() > 0) {
            lines.add(cur.toString());
        }

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
