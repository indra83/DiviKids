package co.in.divi.kids.content;

/**
 * Created by indraneel on 01-12-2014.
 * JSON marshalling!
 */
public class Content {
    public String title;
    public String summary;
    public int versionCode;
    public String versionString;

    public Category[] categories;

    public static class Category {
        public String id;
        public String name;
        public String shortDesc;
        public String longDesc;

        public SubCategory[] subCategories;
    }

    public static class SubCategory {
        public String id;
        public String name;
        public String desc;

        public App[] apps;
        public Video[] videos;
    }

    public static class App {
        public String packageName;
        public String name;
    }

    public static class Video {
        public String youtubeId;
        public String name;
    }
}
