package pw.yumc.Yum.inject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.TimedRegisteredListener;

import cn.citycraft.PluginHelper.ext.kit.Reflect;
import cn.citycraft.PluginHelper.kit.PluginKit;
import pw.yumc.Yum.commands.MonitorCommand;
import pw.yumc.Yum.managers.ConfigManager;
import pw.yumc.Yum.managers.MonitorManager;

public class ListenerInjector implements EventExecutor {
    private final String prefix = "§6[§bYum §a事件监控§6] ";
    private final EventExecutor originalExecutor;

    private final Plugin plugin;

    public Map<String, Long> eventTotalTime;
    public Map<String, Integer> eventCount;

    public ListenerInjector(final EventExecutor originalExecutor, final Plugin plugin) {
        this.originalExecutor = originalExecutor;
        this.plugin = plugin;
        eventTotalTime = new HashMap<>();
        eventCount = new HashMap<>();
    }

    public static void inject(final Plugin plugin) {
        final List<RegisteredListener> listeners = HandlerList.getRegisteredListeners(plugin);
        for (final RegisteredListener listener : listeners) {
            if (listener instanceof TimedRegisteredListener) {
                return;
            }
            EventExecutor originalExecutor = Reflect.on(listener).get("executor");
            if (originalExecutor instanceof ListenerInjector) {
                originalExecutor = ((ListenerInjector) originalExecutor).getOriginalExecutor();
            }
            final ListenerInjector listenerInjector = new ListenerInjector(originalExecutor, plugin);
            Reflect.on(listener).set("executor", listenerInjector);
        }
    }

    public static void uninject(final Plugin plugin) {
        final List<RegisteredListener> listeners = HandlerList.getRegisteredListeners(plugin);
        for (final RegisteredListener listener : listeners) {
            if (listener instanceof TimedRegisteredListener) {
                return;
            }
            final EventExecutor executor = Reflect.on(listener).get("executor");
            if (executor instanceof ListenerInjector) {
                Reflect.on(listener).set("executor", ((ListenerInjector) executor).getOriginalExecutor());
            }
        }
    }

    @Override
    public void execute(final Listener listener, final Event event) throws EventException {
        try {
            if (!event.isAsynchronous()) {
                final long start = System.nanoTime();
                // TODO 当操作大于10ms的时候添加一个Lag提示
                originalExecutor.execute(listener, event);
                final long end = System.nanoTime();
                final String en = event.getEventName();
                final long lag = end - start;
                if (lag / 1000000 > 10 && !ConfigManager.i().getMonitorIgnoreList().contains(plugin.getName())) {
                    PluginKit.sc("§6[§bYum §a能耗监控§6] §c注意! §6插件 §b" + plugin.getName() + " §6处理 §d" + event.getEventName() + " §6事件 §c耗时 §4" + lag / 1000000 + "ms!");
                }
                if (eventTotalTime.containsKey(en)) {
                    eventTotalTime.put(en, eventTotalTime.get(en) + lag);
                    eventCount.put(en, eventCount.get(en) + 1);
                } else {
                    eventTotalTime.put(en, end - start);
                    eventCount.put(en, 1);
                }
                MonitorManager.addEvent(plugin.getName(), lag);
            } else {
                originalExecutor.execute(listener, event);
            }
        } catch (Throwable e) {
            while (e.getCause() != null) {
                e = e.getCause();
            }
            MonitorCommand.lastError = e;
            PluginKit.sc(prefix + "§6插件 §b" + plugin.getName() + " §6处理 §d" + event.getEventName() + " §6事件时发生异常!");
            PluginKit.sc("§6异常名称: §c" + e.getClass().getName());
            PluginKit.sc("§6异常说明: §3" + e.getMessage());
            PluginKit.sc("§6简易错误信息如下:");
            final int l = e.getStackTrace().length > 5 ? 5 : e.getStackTrace().length;
            for (int i = 0; i < l; i++) {
                final StackTraceElement ste = e.getStackTrace()[i];
                PluginKit.sc("    §e位于 §c" + ste.getClassName() + "." + ste.getMethodName() + "(§4" + ste.getFileName() + ":" + ste.getLineNumber() + "§c)");
            }
            if (MonitorCommand.debug) {
                PluginKit.sc("§c开发人员调试信息如下:");
                e.printStackTrace();
            }
        }
    }

    public EventExecutor getOriginalExecutor() {
        return originalExecutor;
    }
}
