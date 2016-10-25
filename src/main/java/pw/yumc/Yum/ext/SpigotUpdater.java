/**
 * ProtocolLib - Bukkit server library that allows access to the Minecraft protocol.
 * Copyright (C) 2015 dmulloy2
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 */
package pw.yumc.Yum.ext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.bukkit.plugin.Plugin;

import com.google.common.base.Charsets;
import com.google.common.io.Closer;

/**
 * Adapted version of the Bukkit updater for use with Spigot resources
 *
 * @author dmulloy2
 */

public class SpigotUpdater extends Updater {
    private static String PROTOCOL = "https://";

    private static String RESOURCE_URL = PROTOCOL + "www.spigotmc.org/resources/protocollib.1997/";

    private static String API_URL = PROTOCOL + "www.spigotmc.org/api/general.php";

    private static String ACTION = "POST";

    private static int ID = 1997;

    private static byte[] API_KEY = ("key=98BE0FE67F88AB82B4C197FAF1DC3B69206EFDCC4D3B80FC83A00037510B99B4&resource="
            + ID).getBytes(Charsets.UTF_8);
    private String remoteVersion;

    public SpigotUpdater(Plugin plugin, UpdateType type, boolean announce) {
        super(plugin, type, announce);
    }

    @Override
    public String getRemoteVersion() {
        return remoteVersion;
    }

    @Override
    public String getResult() {
        waitForThread();
        return String.format(result.toString(), remoteVersion, plugin.getDescription().getVersion(), RESOURCE_URL);
    }

    public String getSpigotVersion() throws IOException {
        Closer closer = Closer.create();
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(API_URL).openConnection();
            con.setDoOutput(true);
            con.setRequestMethod(ACTION);
            con.getOutputStream().write(API_KEY);

            InputStreamReader isr = closer.register(new InputStreamReader(con.getInputStream()));
            BufferedReader br = closer.register(new BufferedReader(isr));
            return br.readLine();
        } finally {
            closer.close();
        }
    }

    @Override
    public void start(UpdateType type) {
        waitForThread();
        this.type = type;
        this.thread = new Thread(new SpigotUpdateRunnable());
        this.thread.start();
    }

    private class SpigotUpdateRunnable implements Runnable {
        @Override
        public void run() {
            try {
                String version = getSpigotVersion();
                remoteVersion = version;
                if (versionCheck(version)) {
                    result = UpdateResult.SPIGOT_UPDATE_AVAILABLE;
                } else {
                    result = UpdateResult.NO_UPDATE;
                }
            } catch (Throwable ex) {
            } finally {
                // Invoke the listeners on the main thread
                for (Runnable listener : listeners) {
                    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, listener);
                }
            }
        }
    }
}