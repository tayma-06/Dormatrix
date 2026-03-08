package cli.routine;

import cli.Input;
import controllers.routine.RoutineController;
import libraries.collections.MyArrayList;
import models.users.StudentPublicInfo;

import java.util.Scanner;

public class AttendantRoutineCLI {

    private final RoutineController controller = new RoutineController();
    private final Scanner sc = Input.SC;

    public void show() {
        String room = readNonEmpty("Enter room number: ");
        MyArrayList<StudentPublicInfo> students = controller.findStudentsByRoom(room);

        if (students == null || students.size() == 0) {
            System.out.println("No students found in room " + room + ".");
            return;
        }

        int selected = pickStudent(students, room);
        if (selected < 0) return;

        StudentPublicInfo chosen = students.get(selected);
        System.out.println();
        System.out.println(controller.renderMaskedRoutineForStudent(chosen.getStudentId()));
    }

    private int pickStudent(MyArrayList<StudentPublicInfo> students, String room) {
        if (students.size() == 1) {
            System.out.println("One student found in room " + room + ": " + students.get(0).getName());
            return 0;
        }

        System.out.println("Students in room " + room + ":");
        for (int i = 0; i < students.size(); i++) {
            System.out.println("[" + (i + 1) + "] " + students.get(i).getName());
        }
        System.out.println("[0] Cancel");
        System.out.print("Choose student: ");

        int x = readInt();
        if (x == 0) return -1;
        if (x < 1 || x > students.size()) {
            System.out.println("Invalid choice.");
            return -1;
        }

        return x - 1;
    }

    private int readInt() {
        while (true) {
            String line = sc.nextLine().trim();
            if (line.isEmpty()) continue;
            try {
                return Integer.parseInt(line);
            } catch (Exception e) {
                System.out.print("Invalid number. Enter again: ");
            }
        }
    }

    private String readNonEmpty(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine().trim();
            if (!line.isEmpty()) return line;
            System.out.println("Input can not be empty.");
        }
    }
}
