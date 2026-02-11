import cli.dashboard.MainDashboard;
import utils.TimeManager;

public class Dormatrix {
    public static void main(String[] args) {
        TimeManager.setDemoMode(true);
        MainDashboard mainDashboard = new MainDashboard();
        mainDashboard.show();
    }
}
