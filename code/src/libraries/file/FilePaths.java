package libraries.file;

import java.io.File;

public class FilePaths {
    private static final String SEP = File.separator;

    public static final String DATA_DIR = "data";
    public static final String MAINTENANCE_WORKERS = DATA_DIR + SEP + "users" + SEP + "maintenance_workers.txt";
    public static final String COMPLAINTS = DATA_DIR + SEP + "complaints" +SEP+ "complaints.txt";

    private FilePaths() {}
}
