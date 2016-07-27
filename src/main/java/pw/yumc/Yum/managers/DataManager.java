package pw.yumc.Yum.managers;

import cn.citycraft.PluginHelper.sql.DataBase;
import cn.citycraft.PluginHelper.sql.KeyValue;
import cn.citycraft.PluginHelper.sql.Type;

public class DataManager {
    private static DataBase db = ConfigManager.i().getDataBase();

    public static void init() {
        db.createTables(TableName.cmd, new KeyValue("name", Type.VARCHAR.get()), null);
    }

    static class TableName {
        public static String prefix = "monitor_";
        public static String cmd = prefix + "cmd";
        public static String event = prefix + "event";
        public static String task = prefix + "task";
    }
}
