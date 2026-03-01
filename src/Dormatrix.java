
import cli.dashboard.MainDashboard;
import utils.TimeManager;
import utils.ServiceManager;

public class Dormatrix {

    public static void main(String[] args) {
        TimeManager.initialize();
        TimeManager.setDemoMode(true);
        ServiceManager.initialize();

        MainDashboard mainDashboard = new MainDashboard();
        mainDashboard.show();

        ServiceManager.shutdown();
    }
}
