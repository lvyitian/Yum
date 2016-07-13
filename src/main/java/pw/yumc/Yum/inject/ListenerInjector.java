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

public class ListenerInjector implements EventExecutor {

    private final EventExecutor originalExecutor;

    public Map<String, Long> eventTotalTime;
    public Map<String, Long> eventCount;

    public ListenerInjector(final EventExecutor originalExecutor) {
        this.originalExecutor = originalExecutor;
        eventTotalTime = new HashMap<>();
        eventCount = new HashMap<>();
    }

    public static void inject(final Plugin plugin) {
        final List<RegisteredListener> listeners = HandlerList.getRegisteredListeners(plugin);
        for (final RegisteredListener listener : listeners) {
            if (listener instanceof TimedRegisteredListener) {
                return;
            }
            final EventExecutor originalExecutor = Reflect.on(listener).get("executor");
            if (originalExecutor instanceof ListenerInjector) {
                return;
            }
            final ListenerInjector listenerInjector = new ListenerInjector(originalExecutor);
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
        if (!event.isAsynchronous()) {
            final long start = System.nanoTime();
            // TODO add a more aggressive 10 ms cpu sample
            originalExecutor.execute(listener, event);
            final long end = System.nanoTime();
            final String en = event.getEventName();
            if (eventTotalTime.containsKey(en)) {
                eventTotalTime.put(en, eventTotalTime.get(en) + end - start);
                eventCount.put(en, eventCount.get(en) + 1);
            } else {
                eventTotalTime.put(en, end - start);
                eventCount.put(en, 1L);
            }
        } else {
            originalExecutor.execute(listener, event);
        }
    }

    public EventExecutor getOriginalExecutor() {
        return originalExecutor;
    }
}
