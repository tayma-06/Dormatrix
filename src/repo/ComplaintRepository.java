package repo;
import models.complaints.Complaint;
import libraries.collections.MyArrayList;
import libraries.collections.MyOptional;


public interface ComplaintRepository {
    // saves a new complaint
    void save(Complaint c);
    // updates an existing complaint by rewriting it to storage
    boolean update(Complaint c);
    // finds complaint by id
    MyOptional<Complaint> findById(String complaintId);
    // returns all complaints
    MyArrayList<Complaint> findAll();
//    // returns complaints with no assigned worker
//    MyArrayList<Complaint> findUnassigned();
    // returns complaints filed by a students
    MyArrayList<Complaint> findByStudentId(String studentId);
    // returns complaints assigned to a worker
    MyArrayList<Complaint> findByAssignedWorker(String workerId);
}
