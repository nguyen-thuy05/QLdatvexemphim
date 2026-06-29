package com.example.codeappdatvexemphim;

public class Movie {
    private int id;
    private String title;
    private String genre;
    private int duration;
    private double rating;
    private String releaseDate;
    private String synopsis;
    private String posterUri;
    private String coverUri;
    private String status; // 'now_showing' or 'coming_soon'
    private boolean isHot;

    public Movie(int id, String title, String genre, int duration, double rating, 
                 String releaseDate, String synopsis, String posterUri, String coverUri, String status, boolean isHot) {
        this.id = id;
        this.title = title;
        this.genre = genre;
        this.duration = duration;
        this.rating = rating;
        this.releaseDate = releaseDate;
        this.synopsis = synopsis;
        this.posterUri = posterUri;
        this.coverUri = coverUri;
        this.status = status;
        this.isHot = isHot;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getGenre() { return genre; }
    public int getDuration() { return duration; }
    public double getRating() { return rating; }
    public String getReleaseDate() { return releaseDate; }
    public String getSynopsis() { return synopsis; }
    public String getPosterUri() { return posterUri; }
    public String getCoverUri() { return coverUri; }
    public String getStatus() { return status; }
    public boolean isHot() { return isHot; }
    public void setHot(boolean hot) { isHot = hot; }
}
