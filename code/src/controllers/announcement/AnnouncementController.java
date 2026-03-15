package controllers.announcement;

import libraries.collections.MyArrayList;
import models.announcements.Announcement;
import repo.file.FileAnnouncementRepository;
import utils.TimeManager;

import java.util.ArrayList;

public class AnnouncementController {

    private final FileAnnouncementRepository repo = new FileAnnouncementRepository();

    public void postAnnouncement(String authorName, String title, String body) {
        String id = "ANN-" + System.currentTimeMillis();
        String createdAt = TimeManager.nowDate().toString() + " " + TimeManager.nowTime().withSecond(0).withNano(0).toString();

        repo.save(new Announcement(
                id,
                authorName == null || authorName.trim().isEmpty() ? "Hall Attendant" : authorName,
                title == null ? "" : title.trim(),
                body == null ? "" : body.trim(),
                createdAt
        ));
    }

    public String renderBoard() {
        MyArrayList<Announcement> all = repo.findAll();
        StringBuilder sb = new StringBuilder();

        if (all.size() == 0) {
            // use tBoxTop etc. directly since this is called from CLI
            utils.TerminalUI.tBoxTop();
            utils.TerminalUI.tBoxTitle("ANNOUNCEMENTS");
            utils.TerminalUI.tBoxSep();
            utils.TerminalUI.tBoxLine("No announcements yet.");
            utils.TerminalUI.tBoxBottom();
            return "";
        }

        utils.TerminalUI.tBoxTop();
        utils.TerminalUI.tBoxTitle("ANNOUNCEMENTS");

        for (int i = all.size() - 1; i >= 0; i--) {
            Announcement a = all.get(i);
            utils.TerminalUI.tBoxSep();
            utils.TerminalUI.tBoxLine("Title   : " + a.getTitle());
            utils.TerminalUI.tBoxLine("By      : " + a.getAuthorName());
            utils.TerminalUI.tBoxLine("Created : " + a.getCreatedAt());

            String[] wrapped = wrap(a.getBody(), 55);
            if (wrapped.length == 0) {
                utils.TerminalUI.tBoxLine("Message : (empty)");
            } else {
                utils.TerminalUI.tBoxLine("Message : " + wrapped[0]);
                for (int w = 1; w < wrapped.length; w++) {
                    utils.TerminalUI.tBoxLine("          " + wrapped[w]);
                }
            }
        }

        utils.TerminalUI.tBoxBottom();
        return "";
    }

    private String formatLine(String value) {
        if (value == null) value = "";
        if (value.length() > 67) value = value.substring(0, 67);
        return String.format("║ %-67s ║%n", value);
    }

    private String[] wrap(String text, int maxLen) {
        if (text == null || text.trim().isEmpty()) return new String[0];

        ArrayList<String> lines = new ArrayList<>();
        String remaining = text.trim();

        while (remaining.length() > maxLen) {
            int space = remaining.lastIndexOf(' ', maxLen);
            if (space <= 0) space = maxLen;
            lines.add(remaining.substring(0, space).trim());
            remaining = remaining.substring(space).trim();
        }

        if (!remaining.isEmpty()) lines.add(remaining);
        return lines.toArray(new String[0]);
    }
}