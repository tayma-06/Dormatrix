package cli.announcement;

import controllers.announcement.AnnouncementController;

public class AnnouncementBoardCLI {

    private final AnnouncementController controller = new AnnouncementController();

    public void show() {
        System.out.println();
        System.out.println(controller.renderBoard());
    }
}