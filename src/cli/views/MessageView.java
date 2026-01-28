package cli.views;

public class MessageView {

    public void invalidChoice() {
        System.out.println("Invalid choice. Please try again.");
    }

    public void feature(String msg) {
        System.out.println(msg);
    }

    public void underDevelopment(String featureName) {
        System.out.println(">> Feature [" + featureName + "] is under development.");
    }
}

