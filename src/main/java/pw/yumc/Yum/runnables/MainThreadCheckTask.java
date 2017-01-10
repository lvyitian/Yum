package pw.yumc.Yum.runnables;

import java.lang.Thread.State;
import java.lang.reflect.Method;
import java.util.TimerTask;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import pw.yumc.YumCore.bukkit.Log;
import pw.yumc.YumCore.bukkit.compatible.C;
import pw.yumc.YumCore.kit.PKit;
import pw.yumc.YumCore.plugin.protocollib.PacketKit;

/**
 * 线程安全检查任务
 *
 * @since 2016年6月22日 下午4:57:32
 * @author 喵♂呜
 */
public class MainThreadCheckTask extends TimerTask {
    private static Method tickMethod;
    private String prefix = "§6[§bYum §a线程管理§6] ";
    private String warnPNet = "§6插件 §b%s §c在主线程进行网络操作 §4服务器处于停止状态...";
    private String warnPIO = "§6插件 §b%s §c在主线程进行IO操作 §4服务器处于停止状态...";
    private String warnNet = "§c主线程存在网络操作 §4服务器处于停止状态...";
    private String warnIO = "§c主线程存在IO操作 §4服务器处于停止状态...";
    private String deliver = "§c服务器处于停止状态 已超过 %s 秒 激活心跳 防止线程关闭...";
    private int stopTime = 0;
    private Thread mainThread;

    static {
        try {
            Class clazz = Class.forName("org.spigotmc.WatchdogThread");
            tickMethod = clazz.getDeclaredMethod("tick");
        } catch (Exception ignored) {
        }
    }

    public MainThreadCheckTask(Thread mainThread) {
        this.mainThread = mainThread;
    }

    @Override
    public void run() {
        // According to this post the thread is still in Runnable although it's waiting for
        // file/http ressources
        // https://stackoverflow.com/questions/20795295/why-jstack-out-says-thread-state-is-runnable-while-socketread
        if (mainThread.getState() == State.RUNNABLE) {
            // Based on this post we have to check the top element of the stack
            // https://stackoverflow.com/questions/20891386/how-to-detect-thread-being-blocked-by-io
            StackTraceElement[] stackTrace = mainThread.getStackTrace();
            StackTraceElement topElement = stackTrace[0];
            if (topElement.isNativeMethod()) {
                // Socket/SQL (connect) - java.net.DualStackPlainSocketImpl.connect0
                // Socket/SQL (read) - java.net.SocketInputStream.socketRead0
                // Socket/SQL (write) - java.net.SocketOutputStream.socketWrite0
                if (isElementEqual(topElement, "java.net.DualStackPlainSocketImpl", "connect0")
                        || isElementEqual(topElement, "java.net.SocketInputStream", "socketRead0")
                        || isElementEqual(topElement, "java.net.SocketOutputStream", "socketWrite0")) {
                    Plugin plugin = PKit.getOperatePlugin(stackTrace);
                    if (plugin != null) {
                        Log.console(prefix + warnPNet, plugin.getName());
                    } else {
                        Log.console(prefix + warnNet);
                    }
                    tick();
                }
                // File (in) - java.io.FileInputStream.readBytes
                // File (out) - java.io.FileOutputStream.writeBytes
                else if (isElementEqual(topElement, "java.io.FileInputStream", "readBytes")
                        || isElementEqual(topElement, "java.io.FileOutputStream", "writeBytes")) {
                    Plugin plugin = PKit.getOperatePlugin(stackTrace);
                    if (plugin != null) {
                        Log.console(prefix + warnPIO, plugin.getName());
                    } else {
                        Log.console(prefix + warnIO);
                    }
                    tick();
                } else {
                    stopTime = 0;
                }
            } else {
                stopTime = 0;
            }
        }
    }

    private boolean isElementEqual(StackTraceElement traceElement, String className, String methodName) {
        return traceElement.getClassName().equals(className) && traceElement.getMethodName().equals(methodName);
    }

    private void tick() {
        stopTime += 5;
        if (stopTime >= 45) {
            Log.console(prefix + deliver, stopTime);
            wttick();
        }
    }

    /**
     * 保持服务器心跳
     */
    public static void wttick() {
        try {
            if (tickMethod != null) {
                tickMethod.invoke(null);
            }
            for (final Player player : C.Player.getOnlinePlayers()) {
                player.sendMessage("§4注意: §c服务器主线程处于停止状态 请等待操作完成!");
                PacketKit.keep_live(player);
            }
        } catch (final Throwable e) {
        }
    }
}
