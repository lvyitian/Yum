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

import cn.citycraft.PluginHelper.config.FileConfig;
import pw.yumc.Yum.Yum;

/**
 * 网络代理处理类
 *
 * @since 2016年3月26日 下午4:24:09
 * @author 喵♂呜
 */
public class NetworkManager {
    public static void throwException(final Throwable exception) {
        NetworkManager.<RuntimeException> throwException0(exception);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void throwException0(final Throwable exception) throws T {
        throw (T) exception;
    }

    public NetworkManager register(final Yum plugin) {
        plugin.getLogger().info("注入网络代理 将托管服务器网络!");
        ProxySelector.setDefault(new YumProxySelector(ProxySelector.getDefault(), plugin));
        return this;
    }

    public void unregister() {
        final ProxySelector cur = ProxySelector.getDefault();
        if (cur instanceof YumProxySelector) {
            ProxySelector.setDefault(((YumProxySelector) cur).getDefaultSelector());
        }
    }

    class YumProxySelector extends ProxySelector {
        private final boolean debug;
        private final boolean allowPrimaryThread;
        private final Yum main;
        private final FileConfig config;
        private final ProxySelector defaultSelector;
        private final HashMap<ClassLoader, Plugin> pluginMap = new HashMap<>();

        public YumProxySelector(final ProxySelector defaultSelector, final Yum plugin) {
            this.main = plugin;
            this.config = plugin.getConfig();
            this.defaultSelector = defaultSelector;
            this.debug = config.getBoolean("NetworkDebug");
            this.allowPrimaryThread = config.getBoolean("AllowPrimaryThread");
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
                final Plugin plugin = this.getRequestingPlugin();
                final String urlinfo = uri.toString();
                if (!urlinfo.startsWith("socket") && !urlinfo.toLowerCase().contains("yumc") && !urlinfo.toLowerCase().contains("pom.xml")) {
                    final String str = debug ? "§6[§bNetDebug§6] §c插件 §6%s §c尝试访问 §e%s §c请注意服务器网络安全!" : "§6[§bNetManager§6] §c插件 §6%s §c尝试在主线程访问 §e%s §4可能会导致服务器卡顿或无响应!";
                    if (plugin == null) {
                        Bukkit.getConsoleSender().sendMessage(String.format(str, "未知(请查看堆栈)", urlinfo));
                        Thread.dumpStack();
                    } else if (!plugin.getName().equalsIgnoreCase("Yum")) {
                        Bukkit.getConsoleSender().sendMessage(String.format(str, plugin.getName(), urlinfo));
                        if (!allowPrimaryThread) {
                            Bukkit.getConsoleSender().sendMessage("§6[§bNetManager§6] §4已阻止插件 §b" + plugin.getName() + " §4在主线程访问网络!");
                            throwException(new IOException("Yum 已开启网络防护 不允许在主线程访问网络!"));
                        }
                    }
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
                        final Plugin p = pluginMap.get(loader);
                        if (element.getClassName().contains("pw.yumc.Yum.utils.") || !p.getName().equalsIgnoreCase("Yum")) {
                            return p;
                        }
                    }
                } catch (final ClassNotFoundException ex) {
                }
            }
            return null;
        }
    }

}
