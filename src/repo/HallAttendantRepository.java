package repo;

import models.users.HallAttendant;
import libraries.collections.MyArrayList;
import libraries.collections.MyOptional;

public interface HallAttendantRepository {
    // Finds HallAttendant by id
    MyOptional<HallAttendant> findById(String attendantId);
    // Returns all staff members
    MyArrayList<HallAttendant> findAll();
}
