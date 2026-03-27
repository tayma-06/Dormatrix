
package cli.complaint;

import cli.Input;
import cli.forms.complaint.ComplaintForm;
import cli.views.complaint.ComplaintView;

import libraries.collections.MyArrayList;
import libraries.collections.MyOptional;

import models.complaints.Complaint;
import models.enums.ComplaintCategory;
import models.enums.ComplaintStatus;
import models.users.StudentPublicInfo;

import module.complaint.ComplaintModule;
import repo.file.FileComplaintRepository;
import repo.file.FileMaintenanceWorkerRepository;
import utils.BackgroundFiller;
import utils.ConsoleUtil;
import utils.FastInput;
import utils.TerminalUI;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import static utils.TerminalUI.*;
import static utils.TerminalUIExtras.*;

public class StudentComplaintCLI {

    private static final String STUDENT_FILE = "data/users/students.txt";

    // Tag prefixes — used to store student scheduling notes inside the tags field
    public static final String SCHED_PREF_PREFIX  = "SCHED_PREF:";
    public static final String SCHED_REQ_PREFIX   = "SCHED_REQ:";

    private final ComplaintView view = new ComplaintView();
    private final ComplaintForm form = new ComplaintForm(Input.SC);

    private final ComplaintModule module
            = new ComplaintModule(new FileComplaintRepository(), new FileMaintenanceWorkerRepository());

    private final FileComplaintRepository repo = new FileComplaintRepository();

    private static final TerminalUI.MenuItem[] MENU = {
            new TerminalUI.MenuItem(1, "File a Complaint"),
            new TerminalUI.MenuItem(2, "View My Complaints"),
            new TerminalUI.MenuItem(3, "Set Preferred Visit Time"),
            new TerminalUI.MenuItem(4, "Request Reschedule"),
            new TerminalUI.MenuItem(0, "Back"),
    };


    public void start(String studentIdentifier) {
        while (true) {
            try {
                ConsoleUtil.clearScreen();
                BackgroundFiller.applyStudentTheme();
                TerminalUI.setActiveTheme(
                        utils.ConsoleColors.fgRGB(60, 140, 255),
                        utils.ConsoleColors.ThemeText.STUDENT_TEXT,
                        utils.ConsoleColors.bgRGB(0, 6, 45)
                );
                TerminalUI.fillBackground(TerminalUI.getActiveBgColor());

                TerminalUI.drawDashboard(
                        "COMPLAINT (STUDENT)", "",
                        MENU,
                        utils.ConsoleColors.ThemeText.STUDENT_TEXT,
                        utils.ConsoleColors.fgRGB(60, 140, 255),
                        null, 3
                );

                int ch = TerminalUI.readChoiceArrow();

                if (ch == 0) {
                    ConsoleUtil.clearScreen();
                    return;
                }

                // clear before any sub-screen
                ConsoleUtil.clearScreen();
                BackgroundFiller.applyStudentTheme();
                TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
                TerminalUI.at(2, 1);

            // ── [1] File a new complaint ──────────────────────────────────
                // ── [1] File a new complaint ──────────────────────────────────
                if (ch == 1) {
                    MyOptional<StudentPublicInfo> infoOpt = resolveStudentPublicInfo(studentIdentifier);
                    if (infoOpt.isEmpty()) {
                        tError("Could not identify student.");
                        tPause();
                        continue;
                    }

                    ComplaintCategory cat = form.readCategory();
                    if (cat == null) {
                        continue;
                    }

                    ConsoleUtil.clearScreen();
                    BackgroundFiller.applyStudentTheme();
                    TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
                    TerminalUI.at(2, 1);
                    TerminalUI.tInfoBox("FILE A COMPLAINT",
                            "Selected Category: " + cat,
                            "Write a short description of the problem below.");

                    String desc = form.readNonEmpty("Enter complaint description: ");

                    try {
                        Complaint c = module.fileComplaint(infoOpt.get(), cat, desc);
                        view.filed(c);
                    } catch (IllegalStateException e) {
                        tBoxTop();
                        tBoxTitle("CANNOT FILE COMPLAINT");
                        tBoxSep();
                        tBoxLine("You must be assigned to a room before filing a complaint.");
                        tBoxLine("Please contact the Hall Office to get a room assigned.");
                        tBoxBottom();
                    }
                    tPause();
                } else if (ch == 2) {
                    MyOptional<StudentPublicInfo> infoOpt = resolveStudentPublicInfo(studentIdentifier);
                    if (infoOpt.isEmpty()) {
                        view.error("Could not identify student (name/id mismatch).");
                        continue;
                    }

                    MyArrayList<Complaint> mine = repo.findByStudentId(infoOpt.get().getStudentId());
                    view.studentList(mine);
                    ConsoleUtil.pause();

                    // ── [3] Set preferred time for a complaint's maintenance visit ─
                } else if (ch == 3) {
                    MyOptional<StudentPublicInfo> infoOpt = resolveStudentPublicInfo(studentIdentifier);
                    if (infoOpt.isEmpty()) {
                        view.error("Could not identify student.");
                        ConsoleUtil.pause();
                        continue;
                    }

                    MyArrayList<Complaint> mine = getUnresolvedComplaints(infoOpt.get().getStudentId());
                    if (mine.size() == 0) {
                        tBoxTop();
                        tBoxTitle("SET PREFERRED VISIT TIME");
                        tBoxSep();
                        tBoxLine("You have no unresolved complaints.");
                        tBoxBottom();
                        tPause();
                        continue;
                    }

                    // Arrow-key picker for student's own complaints
                    String[] labels = buildComplaintLabels(mine);
                    int idx;
                    try {
                        idx = tArrowSelect("SELECT COMPLAINT", labels);
                    } catch (InterruptedException e) {
                        continue;
                    }
                    if (idx < 0 || idx >= mine.size()) continue;

                    Complaint selected = mine.get(idx);

                    // Show existing preference if any
                    String existing = extractTag(selected.getTags(), SCHED_PREF_PREFIX);
                    if (existing != null) {
                        tBoxTop();
                        tBoxLine("Current preference: " + existing);
                        tBoxBottom();
                    }

                    // Collect preferred day
                    String[] dayOptions = {
                            "[1] Monday", "[2] Tuesday", "[3] Wednesday", "[4] Thursday",
                            "[5] Friday", "[6] Saturday", "[7] Sunday", "[8] Any day"
                    };
                    int dayIdx;
                    try { dayIdx = tArrowSelect("PREFERRED DAY", dayOptions); }
                    catch (InterruptedException e) { continue; }

                    String dayStr = (dayIdx >= 0 && dayIdx < dayOptions.length)
                            ? dayOptions[dayIdx] : "Any day";

                    // ← clear before time slot picker
                    ConsoleUtil.clearScreen();
                    BackgroundFiller.applyStudentTheme();
                    TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
                    TerminalUI.at(2, 1);


                    // Collect preferred time of day
                    String[] timeOptions = {
                            "[1] 08:00 - 10:00", "[2] 10:00 - 12:00", "[3] 12:00 - 14:00",
                            "[4] 14:00 - 16:00", "[5] 16:00 - 18:00", "[6] 18:00 - 20:00", "[7] Any time"
                    };
                    int timeIdx;
                    try { timeIdx = tArrowSelect("PREFERRED TIME SLOT", timeOptions); }
                    catch (InterruptedException e) { continue; }

                    String timeStr = (timeIdx >= 0 && timeIdx < timeOptions.length)
                            ? timeOptions[timeIdx] : "Any time";

                    // ← clear before showing note box
                    ConsoleUtil.clearScreen();
                    BackgroundFiller.applyStudentTheme();
                    TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
                    TerminalUI.at(2, 1);

                    tBoxTop();
                    tBoxTitle("ADDITIONAL NOTE");
                    tBoxSep();
                    tBoxLine("Optional - press Enter to skip");
                    tBoxSep();
                    tCustomInputRow("Your Note  : ");
                    String note = FastInput.readLine().trim();

                    String prefValue = dayStr + ", " + timeStr + (note.isEmpty() ? "" : " - " + note);

                 // Remove old preference tag if exists, then append new one
                    String cleanedTags = removePrefixedTag(selected.getTags(), SCHED_PREF_PREFIX);
                    setTagsDirect(selected, cleanedTags);
                    selected.appendTagNote(SCHED_PREF_PREFIX + prefValue);

                    if (repo.update(selected)) {
                        tBoxTop();
                        tBoxLine("Preference saved: " + prefValue);
                    tBoxBottom();
                    } else {
                        tError("Failed to save preference.");
                    }
                    tPause();

                    // ── [4] Request reschedule of a complaint ─────────────────────
                } else if (ch == 4) {
                    MyOptional<StudentPublicInfo> infoOpt = resolveStudentPublicInfo(studentIdentifier);
                    if (infoOpt.isEmpty()) {
                        view.error("Could not identify student.");
                        ConsoleUtil.pause();
                        continue;
                    }
                    MyArrayList<Complaint> mine = getUnresolvedComplaints(infoOpt.get().getStudentId());
                    if (mine.size() == 0) {
                        tBoxTop();
                        tBoxTitle("REQUEST RESCHEDULE");
                        tBoxSep();
                        tBoxLine("You have no unresolved complaints.");
                        tBoxBottom();
                        tPause();
                        continue;
                    }

                    // Arrow-key picker
                    String[] labels = buildComplaintLabels(mine);
                    int idx;
                    try {
                            idx = tArrowSelect("SELECT COMPLAINT TO RESCHEDULE", labels);
                    } catch (InterruptedException e) {
                        continue;
                    }
                    if (idx < 0 || idx >= mine.size()) continue;

                    // ← clear screen before showing next box
                    ConsoleUtil.clearScreen();
                    BackgroundFiller.applyStudentTheme();
                    TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
                    TerminalUI.at(2, 1);

                    Complaint selected = mine.get(idx);

                    // Show existing reschedule request if any
                    String existing = extractTag(selected.getTags(), SCHED_REQ_PREFIX);
                    if (existing != null) {
                        tBoxTop();
                        tBoxLine("Existing request: " + existing);
                        tBoxBottom();
                    }

                    tBoxTop();
                    tBoxTitle("RESCHEDULE REASON");
                    tBoxSep();
                    tBoxLine("Enter reason for reschedule request:");
                    tBoxSep();
                    tCustomInputRow("Your Note  : ");
                    String reason = FastInput.readLine().trim();
                    if (reason.isEmpty()) {
                        tError("Reason cannot be empty.");
                        tPause();
                        continue;
                    }

                    // Remove old reschedule request if any, then append new
                    String cleanedTags = removePrefixedTag(selected.getTags(), SCHED_REQ_PREFIX);
                    setTagsDirect(selected, cleanedTags);
                    selected.appendTagNote(SCHED_REQ_PREFIX + reason);

                    if (repo.update(selected)) {
                        tBoxTop();
                        tBoxLine("Reschedule request sent to attendant.");
                        tBoxBottom();
                    } else {
                        tError("Failed to send request.");
                    }
                    tPause();

                } else {
                    view.error("Invalid choice.");
                    ConsoleUtil.pause();
                }

            } catch (Exception e) {
                TerminalUI.cleanup();
                System.err.println("[StudentComplaintCLI] " + e.getMessage());
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private MyArrayList<Complaint> getUnresolvedComplaints(String studentId) {
        MyArrayList<Complaint> all = repo.findByStudentId(studentId);
        MyArrayList<Complaint> out = new MyArrayList<>();
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getStatus() != ComplaintStatus.RESOLVED) {
                out.add(all.get(i));
            }
        }
        return out;
    }

    private String[] buildComplaintLabels(MyArrayList<Complaint> complaints) {
        String[] labels = new String[complaints.size()];
        for (int i = 0; i < complaints.size(); i++) {
            Complaint c = complaints.get(i);
            String cat  = c.getCategory() == null ? "UNKNOWN" : c.getCategory().name();
            String room = (c.getStudentRoomNo() == null || c.getStudentRoomNo().trim().isEmpty())
                    ? "?" : c.getStudentRoomNo().trim();
            String pref = extractTag(c.getTags(), SCHED_PREF_PREFIX) != null ? " [PREF SET]" : "";
            String req  = extractTag(c.getTags(), SCHED_REQ_PREFIX)  != null ? " [RESCHEDULE REQ]" : "";

            labels[i] = String.format("%-5s%-8s | %-15s | Room %-6s%s%s",
                    "[" + (i + 1) + "]", c.getComplaintId(), cat, room, pref, req);
        }
        return labels;
    }

    /**
     * Extracts the value of a tag with the given prefix, or null if not found.
     * e.g. extractTag("SCHED_PREF:Monday morning;other", "SCHED_PREF:") -> "Monday morning"
     */
    public static String extractTag(String tags, String prefix) {
        if (tags == null || tags.isEmpty()) return null;
        String[] parts = tags.split(";");
        for (String part : parts) {
            if (part.startsWith(prefix)) {
                return part.substring(prefix.length()).trim();
            }
        }
        return null;
    }

    /**
     * Removes all tags with the given prefix from the tags string.
     */
    private static String removePrefixedTag(String tags, String prefix) {
        if (tags == null || tags.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        String[] parts = tags.split(";");
        for (String part : parts) {
            if (part.trim().isEmpty() || part.startsWith(prefix)) continue;
            if (sb.length() > 0) sb.append(";");
            sb.append(part);
        }
        return sb.toString();
    }

    /**
     * Directly sets the tags field on a complaint via appendTagNote workaround.
     * Since tags has no setter, we clear it first by replacing with empty via reflection-free trick:
     * we build a fresh complaint copy approach — instead we use a known safe way.
     * Actually Complaint has appendTagNote but no setTags. We handle this by
     * passing cleaned tags through the constructor via a helper.
     */
    private static void setTagsDirect(Complaint c, String newTags) {
        c.setTags(newTags);
    }

    // ── Name/ID -> StudentPublicInfo ──────────────────────────────────────────

    private MyOptional<StudentPublicInfo> resolveStudentPublicInfo(String target) {
        try (BufferedReader br = new BufferedReader(new FileReader(STUDENT_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|", -1);
                if (parts.length < 2) continue;

                String id   = parts[0].trim().replace("\uFEFF", "");
                String name = parts[1].trim();
                String room = (parts.length > 7) ? parts[7].trim() : "UNASSIGNED";
                if (room.isEmpty()) room = "UNASSIGNED";

                if (id.equals(target.trim()) || name.equalsIgnoreCase(target.trim())) {
                    return MyOptional.of(new StudentPublicInfo(id, name, room));
                }
            }
        } catch (IOException e) {
            return MyOptional.empty();
        }
        return MyOptional.empty();
    }
}
