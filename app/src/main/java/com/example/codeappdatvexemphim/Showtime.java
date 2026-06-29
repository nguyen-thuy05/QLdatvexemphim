package com.example.codeappdatvexemphim;

public class Showtime {
    private int id;
    private int movieId;
    private String cinema;
    private String date;
    private String time;
    private double price;

    public Showtime(int id, int movieId, String cinema, String date, String time, double price) {
        this.id = id;
        this.movieId = movieId;
        this.cinema = cinema;
        this.date = date;
        this.time = time;
        this.price = price;
    }

    public int getId() { return id; }
    public int getMovieId() { return movieId; }
    public String getCinema() { return cinema; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public double getPrice() { return price; }
}
