package repo;

import models.users.MaintenanceWorker;
import libraries.collections.MyArrayList;
import libraries.collections.MyOptional;

public interface MaintenanceWorkerRepository {
    // finds a worker by id
    MyOptional<MaintenanceWorker> findById(String maintenanceWorkerId);
    // returns all workers
    MyArrayList<MaintenanceWorker> findAll();
}
