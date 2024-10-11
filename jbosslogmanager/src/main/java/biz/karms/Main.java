package biz.karms;

import org.jboss.logmanager.LogManager;
import org.jboss.logmanager.Logger;

public class Main {
    private static final Logger karmLogger;

    static {
        System.setProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager");
        karmLogger = (org.jboss.logmanager.Logger) LogManager.getLogManager().getLogger("KARM");
    }

    public static void main(String[] args) {
        karmLogger.severe("This is a red error message from KARM logger!");
    }
}
