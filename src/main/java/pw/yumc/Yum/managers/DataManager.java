package pw.yumc.Yum.managers;

import cn.citycraft.PluginHelper.sql.Type;
import pw.yumc.YumCore.sql.DataBase;
import pw.yumc.YumCore.sql.core.KeyValue;

public class DataManager {
    private static DataBase db = ConfigManager.i().getDataBase();

    public static void init() {
        db.createTables(TableName.cmd, new KeyValue("plugin", Type.VARCHAR.get()).add("name", Type.VARCHAR.get()).add("total", Type.DOUBLE.get()).add("count", Type.INTEGER.get()), null);
        db.createTables(TableName.event, new KeyValue("plugin", Type.VARCHAR.get()).add("name", Type.VARCHAR.get()).add("total", Type.DOUBLE.get()).add("count", Type.INTEGER.get()), null);
        db.createTables(TableName.task, new KeyValue("plugin", Type.VARCHAR.get()).add("name", Type.VARCHAR.get()).add("total", Type.DOUBLE.get()).add("count", Type.INTEGER.get()), null);
    }

    static class TableName {
        public static String prefix = "monitor_";
        public static String cmd = prefix + "cmd";
        public static String event = prefix + "event";
        public static String task = prefix + "task";
    }
}
