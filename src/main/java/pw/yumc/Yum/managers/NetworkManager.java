package pw.yumc.Yum.managers;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.List;

import org.bukkit.Bukkit;

import cn.citycraft.PluginHelper.kit.ExceptionKit;
import cn.citycraft.PluginHelper.kit.PluginKit;
import pw.yumc.Yum.Yum;
import pw.yumc.Yum.events.PluginNetworkEvent;

/**
 * 网络代理处理类
 *
 * @since 2016年3月26日 下午4:24:09
 * @author 喵♂呜
 */
public class NetworkManager {

    public static void register(final Yum plugin) {
        Bukkit.getConsoleSender().sendMessage("§6[§bYum §a网络管理§6] §a注入网络管理系统 将托管服务器网络!");
        ProxySelector.setDefault(new YumProxySelector(ProxySelector.getDefault(), plugin));
    }

    public static void unregister() {
        final ProxySelector cur = ProxySelector.getDefault();
        if (cur instanceof YumProxySelector) {
            ProxySelector.setDefault(((YumProxySelector) cur).getDefaultSelector());
        }
    }

    static class YumProxySelector extends ProxySelector {
        private final ProxySelector defaultSelector;

        public YumProxySelector(final ProxySelector defaultSelector, final Yum plugin) {
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
            final PluginNetworkEvent pne = new PluginNetworkEvent(PluginKit.getOperatePlugin(), uri, Bukkit.isPrimaryThread());
            Bukkit.getPluginManager().callEvent(pne);
            if (pne.isCancelled()) {
                ExceptionKit.throwException(new IOException("[Yum 网络防护] 已开启网络防护 不允许在主线程访问网络!"));
            }
            return defaultSelector.select(uri);
        }

    }

}
