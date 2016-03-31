package pw.yumc.Yum.manager;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import cn.citycraft.PluginHelper.config.FileConfig;
import cn.citycraft.PluginHelper.kit.ExceptionKit;
import cn.citycraft.PluginHelper.kit.PluginKit;
import pw.yumc.Yum.Yum;

/**
 * 网络代理处理类
 *
 * @since 2016年3月26日 下午4:24:09
 * @author 喵♂呜
 */
public class NetworkManager {

    public NetworkManager register(final Yum plugin) {
        Bukkit.getConsoleSender().sendMessage("§6[§bYum §a网络管理§6] §a注入网络管理系统 将托管服务器网络!");
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
        private final FileConfig config;
        private final ProxySelector defaultSelector;

        public YumProxySelector(final ProxySelector defaultSelector, final Yum plugin) {
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
            final boolean isPrimaryThread = Bukkit.isPrimaryThread();
            if (debug || isPrimaryThread) {
                final Plugin plugin = PluginKit.getOperatePlugin();
                final String urlinfo = uri.toString();
                if (!urlinfo.startsWith("socket") && !urlinfo.toLowerCase().contains("yumc") && !urlinfo.toLowerCase().contains("pom.xml") && !urlinfo.toLowerCase().contains("502647092")) {
                    final String str = isPrimaryThread ? "§6[§bYum §a网络管理§6] §c插件 §6%s §c尝试在主线程访问 §e%s §4可能会导致服务器卡顿或无响应!" : "§6[§bYum §a网络监控§6] §c插件 §6%s §c尝试访问 §e%s §c请注意服务器网络安全!";
                    if (plugin != null) {
                        Bukkit.getConsoleSender().sendMessage(String.format(str, plugin.getName(), urlinfo));
                        if (!allowPrimaryThread && isPrimaryThread) {
                            Bukkit.getConsoleSender().sendMessage("§6[§bYum §a网络管理§6] §4已阻止插件 §b" + plugin.getName() + " §4在主线程访问网络!");
                            ExceptionKit.throwException(new IOException("[Yum 网络防护] 已开启网络防护 不允许在主线程访问网络!"));
                        }
                    }
                }
            }
            return defaultSelector.select(uri);
        }

    }

}
