package cli.routine;

import controllers.routine.RoutineController;
import libraries.collections.MyArrayList;
import models.users.StudentPublicInfo;
import utils.*;

import static utils.FastInput.readInt;
import static utils.TerminalUI.*;

public class AttendantRoutineCLI {

    private final RoutineController controller = new RoutineController();

    private static final MenuItem[] MENU = {
//        new MenuItem(1, "View masked student routine"),
            new MenuItem(1, "Access Through Room Number "),
        new MenuItem(0, "Back"),
    };

    public void show() {
        while (true) {
            try {
                ConsoleUtil.clearScreen();
                BackgroundFiller.applyAttendantTheme();
                System.out.print(HIDE_CUR);

                int menuStartRow = 3;
                int promptRow = drawDashboard(
                        "STUDENT ROUTINE VIEW (MASKED)",
                        "Attendant Access",
                        MENU,
                        TerminalUI.getActiveTextColor(),
                        TerminalUI.getActiveBoxColor(),
                        null,
                        menuStartRow
                );

                System.out.print(SHOW_CUR);
                int choice = readInt();
                System.out.print(RESET);

                if (choice == 0) {
                    return;
                }

                if (choice == 1) {
                    ConsoleUtil.clearScreen();
                    BackgroundFiller.applyAttendantTheme();
                    TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
                    TerminalUI.at(2, 1);
                    TerminalUI.tBoxTop();
                    TerminalUI.tBoxTitle("VIEW STUDENT ROUTINE");
                    TerminalUI.tBoxBottom();
                    tPrompt("Enter room number: ");
                    String room = FastInput.readNonEmptyLine();
                    MyArrayList<StudentPublicInfo> students = controller.findStudentsByRoom(room);
                    if (students == null || students.size() == 0) {
                        System.out.println("No students found in room " + room + ".");
                        return;
                    }

                    int selected = pickStudent(students, room);
                    if (selected < 0) return;

                    ConsoleUtil.clearScreen();
                    BackgroundFiller.applyAttendantTheme();
                    TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
                    TerminalUI.at(3, 1);

                    StudentPublicInfo student = students.get(selected);
                    controller.printMaskedRoutine(student.getStudentId());
                    tEmpty();
                    tPause();
                } else {
                    tError("Invalid choice.");
                    tPause();
                }

            } catch (Exception e) {
                cleanup();
                System.err.println("[AttendantRoutineCLI] " + e.getMessage());
            }


        }
    }

    private int pickStudent(MyArrayList<StudentPublicInfo> students, String room) {

        tBoxTop();
        tBoxTitle("STUDENT IN ROOM "+room);
        tBoxSep();

        for (int i = 0; i < students.size(); i++) {
            tBoxLine("[" + (i + 1) + "] " + students.get(i).getName() );
        }

//        if (students.size() == 1) {
////            System.out.println("One student found in room " + room + ": " + students.get(0).getName());
////            return 0;
//
//
//        }

//        System.out.println("Students in room " + room + ":");
//        for (int i = 0; i < students.size(); i++) {
//            System.out.println("[" + (i + 1) + "] " + students.get(i).getName());
//        }

        tBoxLine("[0] Cancel", ConsoleColors.Accent.EXIT);
        tBoxSep();

//        System.out.print("Choose student: ");

        tBoxSep();
        tInputRow();

        int x = FastInput.readInt();
        if (x == 0) {
            tBoxSep();
            tInputRow();
            return -1;
        }
        if (x < 1 || x > students.size()) {
            System.out.println("Invalid choice.");
            tBoxSep();
            tInputRow();
            return -1;
        }

        return x - 1;
    }

}

//public static void tSubDashboard(String title, String[] items) {
//    tBoxTop();
//    tBoxTitle(title);
//    tBoxSep();
//    for (String item : items) {
//        if (item.startsWith("[0]")) {
//            tBoxLine(item, ConsoleColors.Accent.EXIT);
//        } else {
//            tBoxLine(item);
//        }
//    }
//    // Input separator + input row inside the box (matches drawDashboard style)
//    tBoxSep();
//    tInputRow();
//}
