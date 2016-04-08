package pw.yumc.Yum.events;

import java.net.URI;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;

public class PluginNetworkEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final boolean isPrimaryThread;
    private final Plugin plugin;
    private boolean cancel;
    private URI url;

    public static HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * 插件联网事件
     *
     * @param plugin
     *            插件
     * @param url
     *            地址
     * @param isMainThread
     *            是否为主线程
     */
    public PluginNetworkEvent(final Plugin plugin, final URI url, final boolean isPrimaryThread) {
        this.plugin = plugin;
        this.url = url;
        this.isPrimaryThread = isPrimaryThread;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    /**
     * @return 插件
     */
    public Plugin getPlugin() {
        return plugin;
    }

    /**
     * @return 网址
     */
    public URI getUrl() {
        return url;
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    /**
     * @return 是否在主线程
     */
    public boolean isPrimaryThread() {
        return isPrimaryThread;
    }

    @Override
    public void setCancelled(final boolean value) {
        cancel = value;
    }

    /**
     * @param url
     *            设置新的URL地址
     */
    public void setUrl(final URI url) {
        this.url = url;
    }

}
