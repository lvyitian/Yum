package pw.yumc.Yum.managers;

import pw.yumc.YumCore.sql.DataBase;
import pw.yumc.YumCore.sql.core.KeyValue;

public class DataManager {
    private static DataBase db = ConfigManager.i().getDataBase();

    public static void init() {
        db.createTables(TableName.cmd,
                new KeyValue("plugin", "VARCHAR(30)").add("name", "VARCHAR(30)").add("total", "INT").add("count",
                        "INT"),
                null);
        db.createTables(TableName.event,
                new KeyValue("plugin", "VARCHAR(30)").add("name", "VARCHAR(30)").add("total", "INT").add("count",
                        "INT"),
                null);
        db.createTables(TableName.task,
                new KeyValue("plugin", "VARCHAR(30)").add("name", "VARCHAR(30)").add("total", "INT").add("count",
                        "INT"),
                null);
    }

    static class TableName {
        public static String prefix = "monitor_";
        public static String cmd = prefix + "cmd";
        public static String event = prefix + "event";
        public static String task = prefix + "task";
    }
}
