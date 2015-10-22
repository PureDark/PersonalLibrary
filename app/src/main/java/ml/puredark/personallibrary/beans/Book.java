package ml.puredark.personallibrary.beans;

import android.nfc.Tag;

import java.util.Map;

public final class Book {
    public long bid;
    public String image, title, summary, pubdate;
    public String[] author, translator;
    public Map<String, String> images;
    public String pages;
    public String isbn13;

    public final class Tag {
        public int cont;
        public String name, title;
    }
    public final class Rating {
        public int max, numRaters, min;
        public String average;
    }
}