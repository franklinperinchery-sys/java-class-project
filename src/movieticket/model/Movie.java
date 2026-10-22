package movieticket.model;

/** Entity class for the movies table (encapsulation: private fields + getters/setters). */
public class Movie {
    private int id;
    private String title;
    private String genre;
    private String language;
    private int durationMinutes;
    private double rating;

    public Movie(int id, String title, String genre, String language, int durationMinutes, double rating) {
        this.id = id;
        this.title = title;
        this.genre = genre;
        this.language = language;
        this.durationMinutes = durationMinutes;
        this.rating = rating;
    }

    /** Constructor overloading: a movie that is not yet saved has no id. */
    public Movie(String title, String genre, String language, int durationMinutes, double rating) {
        this(0, title, genre, language, durationMinutes, rating);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    /** Duration like "2h 28m" built with integer division and modulus operators. */
    public String getDurationText() {
        return (durationMinutes / 60) + "h " + (durationMinutes % 60) + "m";
    }

    @Override
    public String toString() {
        return title + " (" + language + ")";
    }
}
