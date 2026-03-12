package cli.schedule;

import controllers.schedule.WorkerScheduleController;
import utils.TerminalUI;

public class WorkerVisitBoardCLI {

    private final WorkerScheduleController controller = new WorkerScheduleController();

    public void show(String workerToken) {
        System.out.println();
        System.out.println(controller.renderWorkerWeek(workerToken));
        TerminalUI.tPause();
    }
}