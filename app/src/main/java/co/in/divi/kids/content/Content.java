package co.in.divi.kids.content;

/**
 * Created by indraneel on 01-12-2014.
 * JSON marshalling!
 */
public class Content {
    public int versionCode;
    public String versionString;

    public Category[] categories;

    public class Category {
        public int id;
        public String name;
        public String shortDesc;
        public String longDesc;

        public SubCategory[] subCategories;
    }

    public class SubCategory {
        public String id;
        public String name;
        public String desc;

        public App[] apps;
        public Video[] videos;
    }

    public class App {
        public String packageName;
        public String name;
    }

    public class Video {
        public String youtubeId;
        public String name;
    }
}
