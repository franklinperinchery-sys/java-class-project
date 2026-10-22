package movieticket.model;

/** Entity class: one screening of a movie. */
public class Show {
    private int id;
    private int movieId;
    private String movieTitle;
    private String screen;
    private String showDate;     // yyyy-MM-dd
    private String showTime;     // HH:mm
    private double basePrice;

    public Show(int id, int movieId, String movieTitle, String screen, String showDate, String showTime,
            double basePrice) {
        this.id = id;
        this.movieId = movieId;
        this.movieTitle = movieTitle;
        this.screen = screen;
        this.showDate = showDate;
        this.showTime = showTime;
        this.basePrice = basePrice;
    }

    /** Constructor overloading: a new show not yet stored in the database. */
    public Show(int movieId, String screen, String showDate, String showTime, double basePrice) {
        this(0, movieId, "", screen, showDate, showTime, basePrice);
    }

    public int getId() {
        return id;
    }

    public int getMovieId() {
        return movieId;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public String getScreen() {
        return screen;
    }

    public String getShowDate() {
        return showDate;
    }

    public String getShowTime() {
        return showTime;
    }

    public double getBasePrice() {
        return basePrice;
    }

    @Override
    public String toString() {
        return movieTitle + " | " + screen + " | " + showDate + " " + showTime;
    }
}
