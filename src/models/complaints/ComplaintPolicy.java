package models.complaints;

import libraries.collections.MyString;
import models.enums.PriorityLevel;
import models.enums.ComplaintCategory;
import models.enums.WorkerField;

public class ComplaintPolicy {
    private MyString ms(String s)
    {
        return (s == null) ? new MyString("") : new MyString(s);
    }

    // applies rules to produce a decision (priority + tags)
    public DormDecision decide(ComplaintCategory category, String description)
    {
        boolean emergency = isEmergency(category, description);

        PriorityLevel priority = priorityBase(category, emergency);
        String tags = "";
//        if (emergency) tags = addTags(tags,"EMERGENCY");
        return new DormDecision(priority, emergency, tags);
    }

    // Returns the recommended worker field for a complaint category
    public WorkerField recommendedWorkerField(ComplaintCategory category){
        return switch (category){
            case ELECTRICITY -> WorkerField.ELECTRICIAN;
            case PLUMBING -> WorkerField.PLUMBER;
            case INTERNET -> WorkerField.INTERNET_TECH;
            case CLEANING -> WorkerField.CLEANING;
//            case SECURITY -> WorkerField.SECURITY;
            default -> WorkerField.CLEANING;
        };
    }

    // Picks base priority for a category and whether it is emergency
    private PriorityLevel priorityBase(ComplaintCategory cat, boolean emergency){
        if (emergency) return PriorityLevel.EMERGENCY;

        return switch (cat) {
//            case SECURITY -> PriorityLevel.HIGH;
            case ELECTRICITY, PLUMBING  -> PriorityLevel.NORMAL;
            case INTERNET, CLEANING -> PriorityLevel.NORMAL;
        };
    }

    private boolean isEmergency(ComplaintCategory cat, String desc){
        // converts String to MyString inorder to use toLowerCase() function
        MyString d = ms(desc).toLowerCase();

        // general emergency keywords
        if(d.containsAny(ms("fire"), ms("smoke"), ms("burning"), ms("sparks"),
                ms("electric shock"), ms("flood"), ms("burst pipe"), ms("overflow"),
                ms("danger"), ms("panic")
//                ms("harassment"), ms("assault"), ms("stalker"),
//                ms("intruder"), ms("violent"),
//                ms("bleeding"), ms("unconscious"), ms("fracture")
        ))
        { return true; }

        // category-based emergencies
        return switch(cat){
//            case SECURITY -> true;
            case ELECTRICITY -> d.containsAny(
                    ms("short circuit"), ms("sparks"), ms("burning smell"), ms("shock")
            );
            case PLUMBING -> d.containsAny(
                    ms("burst"), ms("flood"), ms("overflow"), ms("sweage"), ms("blocked main")
            );
            default -> false;

        };

    }

//    private String addTags(String tags, String tag){
//        MyString mtags = ms(tags);
//        if(mtags == null || mtags.isEmpty()) return tag;
//        if(mtags.contains(ms(tag))) return tags;
//        return tags + "," + tag;
//    }

    // Nested Class
    public static class DormDecision{
        private final PriorityLevel priority;
        private final boolean emergency;
        private final String tags;

        public DormDecision(PriorityLevel priority, boolean emergency, String tags){
            this.priority = priority;
            this.emergency = emergency;
            this.tags = tags;

        }

        public PriorityLevel getPriority(){ return priority; }
        public boolean isEmergency(){ return emergency; }
        public String getTags(){ return tags; }
    }
}
