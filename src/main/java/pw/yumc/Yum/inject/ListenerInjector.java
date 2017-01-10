package pw.yumc.Yum.inject;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.Validate;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.TimedRegisteredListener;

import pw.yumc.Yum.commands.MonitorCommand;
import pw.yumc.Yum.managers.ConfigManager;
import pw.yumc.Yum.managers.MonitorManager;
import pw.yumc.YumCore.reflect.Reflect;

public class ListenerInjector implements EventExecutor {
    private static String prefix = "§6[§bYum §a事件监控§6] ";
    private static String warn = "§c注意! §6插件 §b%s §6处理 §d%s §6事件 §c耗时 §4%sms §c平均耗时 §4%sms!";
    private static String err = prefix + "§6插件 §b%s §6处理 §d%s §6事件时发生异常!";
    private static String inject_error = prefix + "§6插件 §b%s §c注入能耗监控失败 §6注入类: §3%s!";
    private static String plugin_is_null = "插件不得为NULL!";
    private EventExecutor originalExecutor;

    private Plugin plugin;

    public Map<String, Long> eventTotalTime = new ConcurrentHashMap<>();
    public Map<String, Integer> eventCount = new ConcurrentHashMap<>();

    public ListenerInjector(EventExecutor originalExecutor, Plugin plugin) {
        this.originalExecutor = originalExecutor;
        this.plugin = plugin;
    }

    public static void inject(Plugin plugin) {
        Validate.notNull(plugin, plugin_is_null);
        List<RegisteredListener> listeners = HandlerList.getRegisteredListeners(plugin);
        for (RegisteredListener listener : listeners) {
            try {
                if (listener instanceof TimedRegisteredListener) { return; }
                // 兼容PerWorldPlugin
                if (listener.getClass().getName().contains("PWPRegisteredListener")) {
                    Field f = Reflect.getDeclaredField(RegisteredListener.class, "executor");
                    f.setAccessible(true);
                    EventExecutor originalExecutor = (EventExecutor) f.get(listener);
                    if (originalExecutor instanceof ListenerInjector) { return; }
                    ListenerInjector listenerInjector = new ListenerInjector(originalExecutor, plugin);
                    f.set(listener, listenerInjector);
                } else {
                    EventExecutor originalExecutor = Reflect.on(listener).get("executor");
                    if (originalExecutor instanceof ListenerInjector) { return; }
                    ListenerInjector listenerInjector = new ListenerInjector(originalExecutor, plugin);
                    Reflect.on(listener).set("executor", listenerInjector);
                }
            } catch (Throwable e) {
                MonitorManager.log(String.format(inject_error, plugin.getName(), listener.getClass().getName()));
                e.printStackTrace();
            }
        }
    }

    public static void uninject(Plugin plugin) {
        Validate.notNull(plugin, plugin_is_null);
        try {
            List<RegisteredListener> listeners = HandlerList.getRegisteredListeners(plugin);
            for (RegisteredListener listener : listeners) {
                if (listener instanceof TimedRegisteredListener) { return; }
                EventExecutor executor = Reflect.on(listener).get("executor");
                if (executor instanceof ListenerInjector) {
                    Reflect.on(listener).set("executor", ((ListenerInjector) executor).getOriginalExecutor());
                }
            }
        } catch (Throwable ignored) {
        }
    }

    @Override
    public void execute(Listener listener, Event event) throws EventException {
        try {
            if (!event.isAsynchronous()) {
                long start = System.nanoTime();
                originalExecutor.execute(listener, event);
                long end = System.nanoTime();
                String en = event.getEventName();
                long lag = end - start;
                if (eventTotalTime.containsKey(en)) {
                    eventTotalTime.put(en, eventTotalTime.get(en) + lag);
                    eventCount.put(en, eventCount.get(en) + 1);
                } else {
                    eventTotalTime.put(en, end - start);
                    eventCount.put(en, 1);
                }
                long lagms = lag / MonitorManager.um;
                long avglagms = eventTotalTime.get(en) / eventCount.get(en) / MonitorManager.um;
                if (avglagms > MonitorManager.lagTime && lagms > MonitorManager.lagTime && !ConfigManager.i().getMonitorIgnoreList().contains(plugin.getName())) {
                    MonitorManager.lagTip(String.format(warn, plugin.getName(), event.getEventName(), lagms, avglagms));
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
            MonitorManager.printThrowable(String.format(err, plugin.getName(), event.getEventName()), e);
        }
    }

    public EventExecutor getOriginalExecutor() {
        return originalExecutor;
    }
}
