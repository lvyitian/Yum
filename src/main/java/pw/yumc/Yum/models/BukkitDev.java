package pw.yumc.Yum.models;

import java.io.Serializable;
import java.util.List;

public class BukkitDev implements Serializable {
    public volatile static String HOST = "https://api.curseforge.com";
    public volatile static String MODULE = "/servermods";
    public volatile static String SEARCH = HOST + MODULE + "/projects?search=%s";
    public volatile static String PLUGIN = HOST + MODULE + "/files?projectIds=%s";

    public List<Projects> projects;
    public List<Files> files;

    public static class Files {
        public int projectId;
        public String name;
        public String fileUrl;
        public String fileName;
        public String downloadUrl;
        public String gameVersion;
        public String md5;
        public String releaseType;
    }

    public static class Projects {
        public int id;
        public String name;
        public String slug;
        public String stage;
    }
}
