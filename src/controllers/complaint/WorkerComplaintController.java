package controllers.complaint;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import libraries.collections.MyArrayList;
import libraries.collections.MyOptional;

import models.complaints.Complaint;
import models.enums.ComplaintStatus;

import repo.file.FileComplaintRepository;

public class WorkerComplaintController {
    private static final String WORKER_FILE = "data/users/maintenance_workers.txt";
    private final FileComplaintRepository repo = new FileComplaintRepository();

    public MyArrayList<Complaint> queue(String workerIdentifier){
        String wid = resolveWorkerId(workerIdentifier);
        if (wid == null) return new MyArrayList<>();

        MyArrayList<Complaint> list = repo.findByAssignedWorker(wid);
        MyArrayList<Complaint> active = new MyArrayList<>();
        for (int i = 0; i < list.size(); i++){
            if (list.get(i).getStatus() != ComplaintStatus.RESOLVED) active.add(list.get(i));
        }
        return active;
    }

    public boolean update(String workerIdentifier, String complaintId, String note, boolean complete){
        String wid = resolveWorkerId(workerIdentifier);
        if (wid == null) return false;

        MyOptional<Complaint> cOpt = repo.findById(complaintId);
        if (cOpt.isEmpty()) return false;

        Complaint c = cOpt.get();
        if (c.getAssignedWorkerId() == null) return false;
        if (!c.getAssignedWorkerId().trim().equals(wid.trim())) return false;

        c.setStatus(complete ? ComplaintStatus.RESOLVED : ComplaintStatus.IN_PROGRESS);
        c.appendTagNote((complete ? "WORKER_DONE:" : "WORKER_PROGRESS:") + (note == null ? "" : note));
        return repo.update(c);
    }

    private String resolveWorkerId(String target){
        try (BufferedReader br = new BufferedReader(new FileReader(WORKER_FILE))){
            String line;
            while((line = br.readLine()) != null){
                String[] p = line.split("\\|", -1);
                if (p.length < 2) continue;

                String id = p[0].trim().replace("\uFEFF", "");
                String name = p[1].trim();
                if (id.equals(target.trim()) || name.equalsIgnoreCase(target.trim())) return id;
            }
        } catch (IOException e){
            return null;
        }
        return null;
    }
}
