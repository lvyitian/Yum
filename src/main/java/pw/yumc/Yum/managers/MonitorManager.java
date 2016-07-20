package pw.yumc.Yum.managers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import cn.citycraft.PluginHelper.kit.PluginKit;

/**
 *
 * @since 2016年7月19日 下午3:55:54
 * @author 喵♂呜
 */
public class MonitorManager {
    public static int lagTime = 20;
    public static boolean debug = ConfigManager.i().isMonitorDebug();

    private static Map<String, Long> monitor = new HashMap<>();
    private static Map<String, Long> task = new HashMap<>();
    private static Map<String, Long> event = new HashMap<>();
    private static Map<String, Long> cmd = new HashMap<>();

    private final static double um = 1000000.00;

    public static void addCmd(final String pname, final long time) {
        monitor.put(pname, monitor.get(pname) + time);
        cmd.put(pname, cmd.get(pname) + time);
    }

    public static void addEvent(final String pname, final long time) {
        monitor.put(pname, monitor.get(pname) + time);
        event.put(pname, event.get(pname) + time);
    }

    public static void addTask(final String pname, final long time) {
        monitor.put(pname, monitor.get(pname) + time);
        task.put(pname, task.get(pname) + time);
    }

    public static Map<String, Long> getMonitor() {
        return sortMapByValue(monitor);
    }

    public static MonitorInfo getMonitorInfo(final String pname) {
        return new MonitorInfo(monitor.get(pname) / um, cmd.get(pname) / um, event.get(pname) / um, task.get(pname) / um);
    }

    public static void print(final Throwable e) {
        PluginKit.sc("§6异常名称: §c" + e.getClass().getName());
        PluginKit.sc("§6异常说明: §3" + e.getMessage());
        PluginKit.sc("§6简易错误信息如下:");
        final int l = e.getStackTrace().length > 5 ? 5 : e.getStackTrace().length;
        for (int i = 0; i < l; i++) {
            final StackTraceElement ste = e.getStackTrace()[i];
            PluginKit.sc("    §e位于 §c" + ste.getClassName() + "." + ste.getMethodName() + "(§4" + ste.getFileName() + ":" + ste.getLineNumber() + "§c)");
        }
        if (debug) {
            PluginKit.sc("§c开发人员调试信息如下:");
            e.printStackTrace();
        }
    }

    public static void reset(final String pname) {
        monitor.put(pname, 0L);
        task.put(pname, 0L);
        event.put(pname, 0L);
        cmd.put(pname, 0L);
    }

    /**
     * 使用 Map按value进行排序
     *
     * @param map
     * @return
     */
    public static Map<String, Long> sortMapByValue(final Map<String, Long> oriMap) {
        if (oriMap == null || oriMap.isEmpty()) {
            return null;
        }
        final Map<String, Long> sortedMap = new LinkedHashMap<String, Long>();
        final List<Map.Entry<String, Long>> entryList = new ArrayList<Map.Entry<String, Long>>(oriMap.entrySet());
        Collections.sort(entryList, new MonitorComparator());
        final Iterator<Map.Entry<String, Long>> iter = entryList.iterator();
        Entry<String, Long> tmpEntry = null;
        while (iter.hasNext()) {
            tmpEntry = iter.next();
            sortedMap.put(tmpEntry.getKey(), tmpEntry.getValue());
        }
        return sortedMap;
    }

    public static class MonitorInfo {
        public double monitor;
        public double cmd;
        public double event;
        public double task;

        public MonitorInfo(final double monitor, final double cmd, final double event, final double task) {
            this.monitor = monitor;
            this.cmd = cmd;
            this.event = event;
            this.task = task;
        }
    }

    static class MonitorComparator implements Comparator<Map.Entry<String, Long>> {
        @Override
        public int compare(final Entry<String, Long> o1, final Entry<String, Long> o2) {
            return o2.getValue().compareTo(o1.getValue());
        }
    }
}
