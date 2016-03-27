package pw.yumc.Yum.manager;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import pw.yumc.Yum.Yum;

/**
 * 网络代理处理类
 *
 * @since 2016年3月26日 下午4:24:09
 * @author 喵♂呜
 */
public class NetworkManager {
    private static boolean debug;

    private final Yum main;

    public NetworkManager(final Yum plugin) {
        this.main = plugin;
    }

    public void register() {
        main.getLogger().info("注入网络代理 将托管服务器网络!");
        ProxySelector.setDefault(new YumProxySelector(ProxySelector.getDefault()));
    }

    public NetworkManager setDebug(final boolean debug) {
        NetworkManager.debug = debug;
        return this;
    }

    public void unregister() {
        final ProxySelector cur = ProxySelector.getDefault();
        if (cur instanceof YumProxySelector) {
            main.getLogger().info("恢复网络代理 使用默认网络!");
            ProxySelector.setDefault(((YumProxySelector) cur).getDefaultSelector());
        }
    }

    class YumProxySelector extends ProxySelector {
        private final ProxySelector defaultSelector;
        private final HashMap<ClassLoader, Plugin> pluginMap = new HashMap<>();

        public YumProxySelector(final ProxySelector defaultSelector) {
            this.defaultSelector = defaultSelector;
        }

        @Override
        public void connectFailed(final URI uri, final SocketAddress sa, final IOException ioe) {
            defaultSelector.connectFailed(uri, sa, ioe);
        }

        public ProxySelector getDefaultSelector() {
            return defaultSelector;
        }

        @Override
        public List<Proxy> select(final URI uri) {
            if (debug || Bukkit.isPrimaryThread()) {
                try {
                    final Plugin plugin = this.getRequestingPlugin();
                    final String urlinfo = uri.getHost() + ":" + uri.getPort() + "/" + uri.getPath();
                    final String str = debug ? "[NetDebug] 插件 %s 尝试访问 %s 请注意服务器网络安全!" : "[NetManager] 插件 %s 尝试在主线程访问 %s 可能会导致服务器卡顿或无响应!";
                    if (plugin == null) {
                        main.getLogger().warning(String.format(str, "未知(请查看堆栈)", urlinfo));
                        Thread.dumpStack();
                    } else if (!plugin.getName().equalsIgnoreCase("Yum")) {
                        main.getLogger().warning(String.format(str, plugin.getName(), urlinfo));
                    }
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
            return defaultSelector.select(uri);
        }

        private void collectPlugin() {
            if (Bukkit.getPluginManager().getPlugins().length != pluginMap.keySet().size() - 1) {
                pluginMap.clear();
                for (final Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
                    pluginMap.put(plugin.getClass().getClassLoader(), plugin);
                }
            }
        }

        private Plugin getRequestingPlugin() {
            collectPlugin();
            final StackTraceElement[] stacktrace = new Exception().getStackTrace();
            for (final StackTraceElement element : stacktrace) {
                try {
                    final ClassLoader loader = Class.forName(element.getClassName(), false, getClass().getClassLoader()).getClassLoader();
                    if (pluginMap.containsKey(loader)) {
                        return pluginMap.get(loader);
                    }
                } catch (final ClassNotFoundException ex) {
                }
            }
            return null;
        }
    }

}
