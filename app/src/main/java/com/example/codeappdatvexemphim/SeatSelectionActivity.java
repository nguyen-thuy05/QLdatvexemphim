package com.example.codeappdatvexemphim;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SeatSelectionActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView txtMovieTitle, txtShowtimeInfo, txtSelectedSeats, txtTotalPrice;
    private LinearLayout layoutSeatGrid;
    private AppCompatButton btnCheckout;

    private DatabaseHelper dbHelper;
    private int movieId;
    private int showtimeId;
    private Movie movie;
    private Showtime showtime;

    private List<String> bookedSeats = new ArrayList<>();
    private List<String> selectedSeats = new ArrayList<>();
    private double ticketPrice = 0.0;

    private final char[] ROWS = {'A', 'B', 'C', 'D', 'E', 'F'};
    private final int SEATS_PER_ROW = 8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seat_selection);

        dbHelper = DatabaseHelper.getInstance(this);

        // Retrieve info from Intent
        movieId = getIntent().getIntExtra("movie_id", -1);
        showtimeId = getIntent().getIntExtra("showtime_id", -1);

        movie = dbHelper.getMovieById(movieId);
        showtime = dbHelper.getShowtimeById(showtimeId);

        if (movie == null || showtime == null) {
            Toast.makeText(this, "Lỗi tải thông tin lịch chiếu!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ticketPrice = showtime.getPrice();

        // Load already booked seats for this showtime
        bookedSeats = dbHelper.getBookedSeatsForShowtime(showtimeId);

        initViews();
        generateSeatGrid();
        updateBottomBar();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        txtMovieTitle = findViewById(R.id.txt_movie_title);
        txtShowtimeInfo = findViewById(R.id.txt_showtime_info);
        txtSelectedSeats = findViewById(R.id.txt_selected_seats);
        txtTotalPrice = findViewById(R.id.txt_total_price);
        layoutSeatGrid = findViewById(R.id.layout_seat_grid);
        btnCheckout = findViewById(R.id.btn_checkout);

        txtMovieTitle.setText(movie.getTitle());
        txtShowtimeInfo.setText(showtime.getCinema() + " | " + showtime.getDate() + " | " + showtime.getTime());

        btnBack.setOnClickListener(v -> finish());

        btnCheckout.setOnClickListener(v -> {
            try {
                if (selectedSeats.isEmpty()) {
                    Toast.makeText(this, "Vui lòng chọn ít nhất 1 vị trí ghế!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Join selected seats as a comma-separated string
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < selectedSeats.size(); i++) {
                    sb.append(selectedSeats.get(i));
                    if (i < selectedSeats.size() - 1) {
                        sb.append(", ");
                    }
                }

                double totalPrice = selectedSeats.size() * ticketPrice;

                android.util.Log.d("SeatSelection", "Passing data: movie_id=" + movieId + 
                        ", showtime_id=" + showtimeId + ", selected_seats=" + sb.toString() + 
                        ", total_price=" + totalPrice);

                Intent intent = new Intent(SeatSelectionActivity.this, TicketConfirmationActivity.class);
                intent.putExtra("movie_id", movieId);
                intent.putExtra("showtime_id", showtimeId);
                intent.putExtra("selected_seats", sb.toString());
                intent.putExtra("total_price", totalPrice);
                startActivity(intent);
            } catch (Exception e) {
                android.util.Log.e("SeatSelection", "Error starting TicketConfirmationActivity", e);
                Toast.makeText(this, "Lỗi chuyển màn hình: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void generateSeatGrid() {
        layoutSeatGrid.removeAllViews();
        int seatSize = dpToPx(34);
        int margin = dpToPx(5);

        for (char rowChar : ROWS) {
            LinearLayout rowLayout = new LinearLayout(this);
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);
            rowLayout.setGravity(Gravity.CENTER);
            rowLayout.setPadding(0, 0, 0, 0);

            // Row Label Left
            TextView txtRowLabelLeft = new TextView(this);
            txtRowLabelLeft.setText(String.valueOf(rowChar));
            txtRowLabelLeft.setTextColor(getResources().getColor(R.color.text_muted));
            txtRowLabelLeft.setTextSize(14);
            txtRowLabelLeft.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams leftLabelParams = new LinearLayout.LayoutParams(dpToPx(24), seatSize);
            leftLabelParams.setMargins(0, 0, dpToPx(10), 0);
            txtRowLabelLeft.setLayoutParams(leftLabelParams);
            rowLayout.addView(txtRowLabelLeft);

            // Seats
            for (int i = 1; i <= SEATS_PER_ROW; i++) {
                String seatName = rowChar + String.valueOf(i);

                TextView txtSeat = new TextView(this);
                txtSeat.setText(String.valueOf(i));
                txtSeat.setGravity(Gravity.CENTER);
                txtSeat.setTextSize(11);
                txtSeat.setTypeface(null, android.graphics.Typeface.BOLD);
                txtSeat.setTextColor(getResources().getColor(R.color.text_primary));

                LinearLayout.LayoutParams seatParams = new LinearLayout.LayoutParams(seatSize, seatSize);
                seatParams.setMargins(margin, margin, margin, margin);
                txtSeat.setLayoutParams(seatParams);

                // Add gap in the middle to make 2 blocks (A1-A4 and A5-A8)
                if (i == 5) {
                    seatParams.setMargins(margin + dpToPx(16), margin, margin, margin);
                }

                if (bookedSeats.contains(seatName)) {
                    // Booked state
                    txtSeat.setBackgroundResource(R.drawable.bg_seat_booked);
                    txtSeat.setTextColor(getResources().getColor(R.color.white));
                    txtSeat.setEnabled(false);
                } else {
                    // Available state
                    txtSeat.setBackgroundResource(R.drawable.bg_seat_available);
                    txtSeat.setOnClickListener(v -> {
                        if (selectedSeats.contains(seatName)) {
                            // Deselect
                            selectedSeats.remove(seatName);
                            txtSeat.setBackgroundResource(R.drawable.bg_seat_available);
                            txtSeat.setTextColor(getResources().getColor(R.color.text_primary));
                        } else {
                            // Select
                            selectedSeats.add(seatName);
                            txtSeat.setBackgroundResource(R.drawable.bg_seat_selected);
                            txtSeat.setTextColor(getResources().getColor(R.color.black));
                        }
                        updateBottomBar();
                    });
                }

                rowLayout.addView(txtSeat);
            }

            // Row Label Right
            TextView txtRowLabelRight = new TextView(this);
            txtRowLabelRight.setText(String.valueOf(rowChar));
            txtRowLabelRight.setTextColor(getResources().getColor(R.color.text_muted));
            txtRowLabelRight.setTextSize(14);
            txtRowLabelRight.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams rightLabelParams = new LinearLayout.LayoutParams(dpToPx(24), seatSize);
            rightLabelParams.setMargins(dpToPx(10), 0, 0, 0);
            txtRowLabelRight.setLayoutParams(rightLabelParams);
            rowLayout.addView(txtRowLabelRight);

            layoutSeatGrid.addView(rowLayout);
        }
    }

    private void updateBottomBar() {
        if (selectedSeats.isEmpty()) {
            txtSelectedSeats.setText("Ghế: Chưa chọn");
            txtTotalPrice.setText("0 đ");
        } else {
            // Join selected seats
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < selectedSeats.size(); i++) {
                sb.append(selectedSeats.get(i));
                if (i < selectedSeats.size() - 1) {
                    sb.append(", ");
                }
            }
            txtSelectedSeats.setText("Ghế: " + sb.toString());

            double total = selectedSeats.size() * ticketPrice;
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            txtTotalPrice.setText(currencyFormat.format(total));
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
