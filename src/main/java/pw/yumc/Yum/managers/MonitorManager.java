package pw.yumc.Yum.managers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import cn.citycraft.PluginHelper.kit.LogKit;
import cn.citycraft.PluginHelper.kit.PluginKit;

/**
 *
 * @since 2016年7月19日 下午3:55:54
 * @author 喵♂呜
 */
public class MonitorManager {
    public final static String prefix = "§6[§bYum §a能耗监控§6] ";
    private final static String errMsg = prefix + "§c命令执行异常 请反馈下列信息给腐竹!";
    private final static String errP = "§6插件名称: §b%s";
    private final static String errN = "§6异常名称: §c%s";
    private final static String errM = "§6异常说明: §3%s";
    private final static String errInfo = "§6简易错误信息如下:";
    private final static String errStackTrace = "    §e位于 §c%s.%s(§4%s:%s§c)";
    private final static String devInfo = "§c开发人员调试信息如下:";

    public final static int lagTime = 20;
    public final static boolean debug = ConfigManager.i().isMonitorDebug();
    public final static boolean log_to_file = ConfigManager.i().isLogToFile();

    private final static Map<String, Long> monitor = new ConcurrentHashMap<>();
    private final static Map<String, Long> task = new ConcurrentHashMap<>();
    private final static Map<String, Long> event = new ConcurrentHashMap<>();
    private final static Map<String, Long> cmd = new ConcurrentHashMap<>();

    private final static double um = 1000000.00;

    private final static LogKit mlog = new LogKit("monitor.log");
    private final static LogKit elog = new LogKit("error.log");

    public static void addCmd(final String pname, final long time) {
        add(pname, time, monitor, cmd);
    }

    public static void addEvent(final String pname, final long time) {
        add(pname, time, monitor, event);
    }

    public static void addTask(final String pname, final long time) {
        add(pname, time, monitor, task);
    }

    public static void elog(final String message) {
        if (log_to_file) {
            elog.logSender(message);
        } else {
            PluginKit.sc(message);
        }
    }

    public static Map<String, Long> getMonitor() {
        return sortMapByValue(monitor);
    }

    public static MonitorInfo getMonitorInfo(final String pname) {
        return new MonitorInfo(monitor.get(pname) / um, cmd.get(pname) / um, event.get(pname) / um, task.get(pname) / um);
    }

    public static void init() {
        for (final Plugin p : Bukkit.getPluginManager().getPlugins()) {
            reset(p.getName());
        }
    }

    public static void lagTip(final String message) {
        log(prefix + message);
    }

    public static void log(final String message) {
        if (log_to_file) {
            mlog.logSender(message);
        } else {
            PluginKit.sc(message);
        }
    }

    public static void printThrowable(final String title, final Throwable e) {
        elog(title);
        elog(String.format(errN, e.getClass().getName()));
        elog(String.format(errM, e.getMessage()));
        elog(errInfo);
        final int l = e.getStackTrace().length > 5 ? 5 : e.getStackTrace().length;
        for (int i = 0; i < l; i++) {
            final StackTraceElement ste = e.getStackTrace()[i];
            elog(String.format(errStackTrace, ste.getClassName(), ste.getMethodName(), ste.getFileName(), ste.getLineNumber()));
        }
        if (debug) {
            PluginKit.sc(devInfo);
            e.printStackTrace();
        }
    }

    public static void reset(final String pname) {
        monitor.put(pname, 0L);
        task.put(pname, 0L);
        event.put(pname, 0L);
        cmd.put(pname, 0L);
    }

    public static void sendError(final CommandSender sender, final Plugin plugin, final Throwable e) {
        sender.sendMessage(errMsg);
        sender.sendMessage(String.format(errP, plugin.getName()));
        sender.sendMessage(String.format(errN, e.getClass().getName()));
        sender.sendMessage(String.format(errM, e.getMessage()));
    }

    public static void sendObject(final CommandSender sender) {
        sender.sendMessage(String.format("monitor@%s cmd@%s event@%s task@%s", monitor.hashCode(), cmd.hashCode(), event.hashCode(), task.hashCode()));
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

    @SafeVarargs
    private static void add(final String pname, final long time, final Map<String, Long>... maps) {
        for (final Map<String, Long> map : maps) {
            map.put(pname, map.get(pname) + time);
        }
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
