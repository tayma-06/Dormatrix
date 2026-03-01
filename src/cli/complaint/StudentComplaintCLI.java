package cli.complaint;

import cli.Input;
import cli.forms.complaint.ComplaintForm;
import cli.views.complaint.ComplaintView;

import libraries.collections.MyArrayList;
import libraries.collections.MyOptional;

import models.complaints.Complaint;
import models.enums.ComplaintCategory;
import models.users.StudentPublicInfo;

import module.complaint.ComplaintModule;
import repo.file.FileComplaintRepository;
import repo.file.FileMaintenanceWorkerRepository;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class StudentComplaintCLI {

    private static final String STUDENT_FILE = "data/users/students.txt";

    private final ComplaintView view = new ComplaintView();
    private final ComplaintForm form = new ComplaintForm(Input.SC);

    private final ComplaintModule module =
            new ComplaintModule(new FileComplaintRepository(), new FileMaintenanceWorkerRepository());

    private final FileComplaintRepository repo = new FileComplaintRepository();

    public void start(String studentIdentifier) {
        while (true) {
            view.studentMenu();
            int ch = form.readInt();

            if (ch == 0) return;

            if (ch == 1) {
                MyOptional<StudentPublicInfo> infoOpt = resolveStudentPublicInfo(studentIdentifier);
                if (infoOpt.isEmpty()) {
                    view.error("Could not identify student (name/id mismatch).");
                    continue;
                }

                ComplaintCategory cat = form.readCategory();

                if (cat == null){
                    return;
                }

                String desc = form.readNonEmpty("Enter complaint description: ");

                Complaint c = module.fileComplaint(infoOpt.get(), cat, desc);
                view.filed(c);

            } else if (ch == 2) {
                MyOptional<StudentPublicInfo> infoOpt = resolveStudentPublicInfo(studentIdentifier);
                if (infoOpt.isEmpty()) {
                    view.error("Could not identify student (name/id mismatch).");
                    continue;
                }

                MyArrayList<Complaint> mine = repo.findByStudentId(infoOpt.get().getStudentId());
                view.studentList(mine);

            } else {
                view.error("Invalid choice.");
            }
        }
    }

    // -------------------- Helper: NAME/ID -> StudentPublicInfo --------------------
    private MyOptional<StudentPublicInfo> resolveStudentPublicInfo(String target) {
        try (BufferedReader br = new BufferedReader(new FileReader(STUDENT_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|", -1);
                if (parts.length < 2) continue;

                String id = parts[0].trim().replace("\uFEFF", "");
                String name = parts[1].trim();
                String room = (parts.length > 7) ? parts[7].trim() : "UNASSIGNED";
                if (room.isEmpty()) room = "UNASSIGNED";

                boolean matchesId = id.equals(target.trim());
                boolean matchesName = name.equalsIgnoreCase(target.trim());

                if (matchesId || matchesName) {
                    return MyOptional.of(new StudentPublicInfo(id, name, room));
                }
            }
        } catch (IOException e) {
            return MyOptional.empty();
        }
        return MyOptional.empty();
    }
}
