package models.complaints;
import libraries.collections.MyString;
import models.enums.ComplaintCategory;
import models.enums.ComplaintStatus;
import models.enums.PriorityLevel;
import models.users.StudentPublicInfo;

public class Complaint{
    private final String complaintId;
    private final String studentId;
    private final String studentName;
    private final String studentRoomNo;
    private final ComplaintCategory category;
    private final String description;
    private ComplaintStatus status;
    private String assignedWorkerId;
    private PriorityLevel priority;
    private String tags;

    public Complaint(String complaintId,
                     String studentId, String studentName, String studentRoomNo,
                     ComplaintCategory category, String description,
                     ComplaintStatus status, String assignedWorkerId,
                     PriorityLevel priority, String tags){
        this.complaintId = complaintId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentRoomNo = studentRoomNo;
        this.category = category;
        this.description = description;
        this.status = status;
        this.assignedWorkerId = assignedWorkerId;
        this.priority = priority == null ? PriorityLevel.NORMAL : priority;
        this.tags = tags == null ? "" : tags;
     
    }

    // Used when creating a new complaint from student
    public static Complaint createNew(String complaintId, StudentPublicInfo publicInfo,
                                     ComplaintCategory category, String description,
                                     PriorityLevel priority, String tags){
        return new Complaint(
            complaintId,
            publicInfo.getStudentId(), publicInfo.getName(), publicInfo.getRoomNo(),
            category, description, ComplaintStatus.SUBMITTED, "",
            priority, tags
        );
    }

    public void assignTo(String workerId){
        this.assignedWorkerId = workerId;
        this.status = ComplaintStatus.ASSIGNED;
    }

    public void clearAssignment(){
        this.assignedWorkerId = "";
        this.status = ComplaintStatus.SUBMITTED;
    }


    public void setStatus(ComplaintStatus status)
    {
        this.status = status;
    }
    public void setTags(String t) { this.tags = t; }

    public String getComplaintId(){ return complaintId; }
    public String getStudentId(){ return studentId; }
    public String getStudentName(){ return studentName; }
    public String getStudentRoomNo(){ return studentRoomNo; }
    public ComplaintCategory getCategory(){ return category; }
    public String getDescription(){ return description; }
    public ComplaintStatus getStatus(){ return status; }
    public String getAssignedWorkerId(){ return assignedWorkerId; }
    public PriorityLevel getPriority(){ return priority; }
    public String getTags(){ return tags; }

    public void appendTagNote(String note){
        if (note == null) return;

        // prevent breaking file format (| delimiter)
        String safe = new MyString(note).replace('|', '/').getValue();

        if (this.tags == null || new MyString(this.tags).trim().isEmpty()) this.tags = safe;
        else this.tags = this.tags + ";" + safe;
    }
    
}





















// package models.complaints;
// import libraries.collections.MyString;

// public class Complaint {
//     private String complaintID;
//     private String userID;
//     private String workerID;
//     private String roomNo;
//     private String title;
//     private String description;
//     private String status;
//     private String resolutionDetails;

//     public Complaint(String complaintID, String userID, String roomNo, String title, String description)
//     {
//         this.complaintID = complaintID;
//         this.userID = userID;
//         this.roomNo = roomNo;
//         this.title = title;
//         this.description = description;
//         this.status = "PENDING";
//         this.workerID = null;
//         this.resolutionDetails = "";
//     }

//      public String getComplaintID()
//     {   return complaintID; }
//     public String getUserID()
//     {   return userID;  }
//     public String getWorkerID()
//     {   return workerID;    }
//     public String getRoomNo()
//     {   return roomNo;  }
//     public String getTitle()
//     {   return title;}
//     public String getDescription()
//     {   return description; }
//     public String getStatus()
//     {   return status;  }
//     public String getResolutionDetails()
//     {   return resolutionDetails;   }
//     public void assignToWorker(String workerID)
//     {
//         this.workerID = workerID;
//         this.status = "ASSIGNED";
//     }
//     public void startWorking()
//     {
//         this.status = "IN_PROGRESS";
//     }
//     public void resolveComplaint(String resolutionDetails)
//     {
//         this.resolutionDetails = resolutionDetails;
//         this.status = "RESOLVED";
//     }
//     public void verifyComplaint()
//     {
//         this.status = "VERIFIED";
//     }
//     public void rejectComplaint()
//     {
//         this.status = "REJECTED";
//     }
//     // Converting my complaints to file format
//     public String toComplaintFileString()
//     {
//         return complaintID+"|"+userID+"|"+roomNo+"|"+(workerID != null ? workerID : "null")+"|"+title+"|"+description+"|"+status+"|"+resolutionDetails;
//     }

//     public static Complaint fromComplaintFileString(String fileString)
//     {
//         MyString[] parts = new MyString(fileString).split('|');
//         if (parts.length >= 8)
//         {
//             Complaint complaint = new Complaint(
//                 parts[0].getValue(), 
//                 parts[1].getValue(), 
//                 parts[2].getValue(), 
//                 parts[4].getValue(), 
//                 parts[5].getValue()
//             );
//             // updating the worker id if not null
//             if (!parts[3].equals("null"))
//             {
//                 complaint.workerID = parts[3].getValue();
//             }

//             complaint.status = parts[6].getValue();
//             complaint.resolutionDetails = parts[7].getValue();
//             return complaint;
//         }
//         return null;
//     }

//     @Override
//     public String toString()
//     {
//         return "Complaint:\ncomplaintID = "+complaintID+
//         "\nuserID = "+userID+
//         "\nroomNo = "+roomNo+
//         "\nworkerID = "+workerID+
//         "\ntitle = "+title+
//         "\ndescription = "+description+
//         "\nstatus = "+status;
//     }
// }
