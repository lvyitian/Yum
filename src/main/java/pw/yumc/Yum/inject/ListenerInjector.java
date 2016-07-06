package pw.yumc.Yum.inject;

import java.util.List;

import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;

import cn.citycraft.PluginHelper.ext.kit.Reflect;

public class ListenerInjector implements EventExecutor {

    private final EventExecutor originalExecutor;

    public long totalTime;
    public long count;

    public ListenerInjector(final EventExecutor originalExecutor) {
        this.originalExecutor = originalExecutor;
    }

    public static void inject(final Plugin plugin) {
        final List<RegisteredListener> listeners = HandlerList.getRegisteredListeners(plugin);
        for (final RegisteredListener listener : listeners) {
            final EventExecutor originalExecutor = Reflect.on(listener).get("executor");
            final ListenerInjector listenerInjector = new ListenerInjector(originalExecutor);
            Reflect.on(listener).set("executor", listenerInjector);
        }
    }

    public static void uninject(final Plugin plugin) {
        final List<RegisteredListener> listeners = HandlerList.getRegisteredListeners(plugin);
        for (final RegisteredListener listener : listeners) {
            final ListenerInjector executor = Reflect.on(listener).get("executor");
            Reflect.on(listener).set("executor", executor.getOriginalExecutor());
        }
    }

    @Override
    public void execute(final Listener listener, final Event event) throws EventException {
        if (!event.isAsynchronous()) {
            final long start = System.nanoTime();
            // todo add a more aggressive 10 ms cpu sample
            originalExecutor.execute(listener, event);
            final long end = System.nanoTime();

            totalTime += end - start;
            count++;
        } else {
            originalExecutor.execute(listener, event);
        }
    }

    public EventExecutor getOriginalExecutor() {
        return originalExecutor;
    }
}
