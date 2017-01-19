package pw.yumc.Yum.managers;

import java.util.ArrayList;
import java.util.Collection;
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

import pw.yumc.YumCore.bukkit.Log;
import pw.yumc.YumCore.kit.LogKit;

/**
 * 能耗监控管理
 *
 * @since 2016年7月19日 下午3:55:54
 * @author 喵♂呜
 */
public class MonitorManager {
    public static String prefix = "§6[§bYum §a能耗监控§6] ";
    private static String errMsg = prefix + "§c命令执行异常 请反馈下列信息给腐竹!";
    private static String errP = "§6插件名称: §b%s";
    private static String errN = "§6异常名称: §c%s";
    private static String errM = "§6异常说明: §3%s";
    private static String errInfo = "§6简易错误信息如下:";
    private static String errStackTrace = "    §e位于 §c%s.%s(§4%s:%s§c)";
    private static String devInfo = "§c开发人员调试信息如下:";

    public static int lagTime = 20;
    public static int um = 1000000;
    public static boolean debug = ConfigManager.i().isMonitorDebug();
    public static boolean log_to_file = ConfigManager.i().isLogToFile();

    private static double totalTime = 0;

    private static Map<String, Long> monitor = new ConcurrentHashMap<>();
    private static Map<String, Long> task = new ConcurrentHashMap<>();
    private static Map<String, Long> event = new ConcurrentHashMap<>();
    private static Map<String, Long> cmd = new ConcurrentHashMap<>();

    private static LogKit mlog = new LogKit("monitor.log");
    private static LogKit elog = new LogKit("error.log");

    public static void addCmd(String pname, long time) {
        add(pname, time, monitor, cmd);
    }

    public static void addEvent(String pname, long time) {
        add(pname, time, monitor, event);
    }

    public static void addTask(String pname, long time) {
        add(pname, time, monitor, task);
    }

    public static void elog(String message) {
        if (log_to_file) {
            elog.console(message);
        } else {
            Log.console(message);
        }
    }

    public static Map<String, Long> getMonitor() {
        return sortMapByValue(monitor);
    }

    public static MonitorInfo getMonitorInfo(String pname) {
        double per = 100.00;
        return new MonitorInfo(monitor.get(pname) / totalTime * per, cmd.get(pname) / totalTime * per, event.get(pname) / totalTime * per, task.get(pname) / totalTime * per);
    }

    public static void init() {
        for (Plugin p : Bukkit.getPluginManager().getPlugins()) {
            reset(p.getName());
        }
    }

    public static void lagTip(String message) {
        log(prefix + message);
    }

    public static void log(String message) {
        if (log_to_file) {
            mlog.console(message);
        } else {
            Log.console(message);
        }
    }

    public static void printThrowable(String title, Throwable e) {
        elog(title);
        elog(String.format(errN, e.getClass().getName()));
        elog(String.format(errM, e.getMessage()));
        elog(errInfo);
        int l = e.getStackTrace().length > 5 ? 5 : e.getStackTrace().length;
        for (int i = 0; i < l; i++) {
            StackTraceElement ste = e.getStackTrace()[i];
            elog(String.format(errStackTrace, ste.getClassName(), ste.getMethodName(), ste.getFileName() == null ? "未知" : ste.getFileName(), ste.getLineNumber()));
        }
        if (debug) {
            Log.console(devInfo);
            e.printStackTrace();
        }
    }

    public static void reset(String pname) {
        monitor.put(pname, 0L);
        task.put(pname, 0L);
        event.put(pname, 0L);
        cmd.put(pname, 0L);
    }

    public static void sendError(CommandSender sender, Plugin plugin, Throwable e) {
        sender.sendMessage(errMsg);
        sender.sendMessage(String.format(errP, plugin.getName()));
        sender.sendMessage(String.format(errN, e.getClass().getName()));
        sender.sendMessage(String.format(errM, e.getMessage()));
    }

    /**
     * 使用 Map按value进行排序
     *
     * @param oriMap
     * @return
     */
    public static Map<String, Long> sortMapByValue(Map<String, Long> oriMap) {
        if (oriMap == null || oriMap.isEmpty()) { return oriMap; }
        Map<String, Long> sortedMap = new LinkedHashMap<>();
        List<Map.Entry<String, Long>> entryList = new ArrayList<>(oriMap.entrySet());
        Collections.sort(entryList, new MonitorComparator());
        Iterator<Map.Entry<String, Long>> iter = entryList.iterator();
        Entry<String, Long> tmpEntry;
        while (iter.hasNext()) {
            tmpEntry = iter.next();
            sortedMap.put(tmpEntry.getKey(), tmpEntry.getValue());
        }
        return sortedMap;
    }

    @SafeVarargs
    private static void add(String pname, long time, Map<String, Long>... maps) {
        totalTime += time;
        for (Map<String, Long> map : maps) {
            map.put(pname, map.get(pname) + time);
        }
    }

    private static long sum(Collection<? extends Long> numbers) {
        int result = 0;
        for (Long num : numbers) {
            result += num;
        }
        return result;
    }

    public static class MonitorInfo {
        public double monitor;
        public double cmd;
        public double event;
        public double task;

        public MonitorInfo(double monitor, double cmd, double event, double task) {
            this.monitor = monitor;
            this.cmd = cmd;
            this.event = event;
            this.task = task;
        }
    }

    static class MonitorComparator implements Comparator<Map.Entry<String, Long>> {
        @Override
        public int compare(Entry<String, Long> o1, Entry<String, Long> o2) {
            return o2.getValue().compareTo(o1.getValue());
        }
    }
}
