package com.example.codeappdatvexemphim;

public class Booking {
    private int id;
    private int userId;
    private int showtimeId;
    private String seats;
    private double totalPrice;
    private String bookingTime;
    private String paymentMethod;
    private String ticketCode;
    private String status; // "active" or "cancelled"

    // Helper fields for joined database queries
    private String movieTitle;
    private String moviePoster;
    private String cinema;
    private String showDate;
    private String showTime;

    // Full constructor for database query results
    public Booking(int id, int userId, int showtimeId, String seats, double totalPrice, 
                   String bookingTime, String paymentMethod, String ticketCode,
                   String movieTitle, String moviePoster, String cinema, String showDate, String showTime, String status) {
        this.id = id;
        this.userId = userId;
        this.showtimeId = showtimeId;
        this.seats = seats;
        this.totalPrice = totalPrice;
        this.bookingTime = bookingTime;
        this.paymentMethod = paymentMethod;
        this.ticketCode = ticketCode;
        this.movieTitle = movieTitle;
        this.moviePoster = moviePoster;
        this.cinema = cinema;
        this.showDate = showDate;
        this.showTime = showTime;
        this.status = status;
    }

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public int getShowtimeId() { return showtimeId; }
    public String getSeats() { return seats; }
    public double getTotalPrice() { return totalPrice; }
    public String getBookingTime() { return bookingTime; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getTicketCode() { return ticketCode; }
    public String getMovieTitle() { return movieTitle; }
    public String getMoviePoster() { return moviePoster; }
    public String getCinema() { return cinema; }
    public String getShowDate() { return showDate; }
    public String getShowTime() { return showTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
