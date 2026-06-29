package com.example.codeappdatvexemphim;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class BookingActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView txtMovieTitle, txtNoShowtimes;
    private LinearLayout layoutDateContainer, layoutCinemaContainer;

    private DatabaseHelper dbHelper;
    private int movieId;
    private Movie movie;
    private String selectedDate = "2026-06-12"; // Default date

    // Local Date Model for representation
    static class DateItem {
        String dbDate;      // "2026-06-12"
        String dayOfWeek;   // "T6" / "CN"
        String dayNum;      // "12"
        String label;       // "Th06"
        
        DateItem(String dbDate, String dayOfWeek, String dayNum, String label) {
            this.dbDate = dbDate;
            this.dayOfWeek = dayOfWeek;
            this.dayNum = dayNum;
            this.label = label;
        }
    }

    private List<DateItem> dateList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        dbHelper = DatabaseHelper.getInstance(this);
        movieId = getIntent().getIntExtra("movie_id", -1);
        movie = dbHelper.getMovieById(movieId);

        if (movie == null) {
            Toast.makeText(this, "Lỗi tải phim!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize lists and views
        initDateList();
        initViews();
        displayDates();
        loadCinemasAndShowtimes();
    }

    private void initDateList() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        java.text.SimpleDateFormat dbFormat = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        java.text.SimpleDateFormat dayNumFormat = new java.text.SimpleDateFormat("dd", java.util.Locale.getDefault());
        java.text.SimpleDateFormat monthFormat = new java.text.SimpleDateFormat("'Th'MM", java.util.Locale.getDefault());
        
        String[] daysOfWeek = {"CN", "T2", "T3", "T4", "T5", "T6", "T7"};

        for (int i = 0; i < 4; i++) {
            String dbDate = dbFormat.format(cal.getTime());
            int dayOfWeekInt = cal.get(java.util.Calendar.DAY_OF_WEEK); // 1 = Sunday, 2 = Monday, ...
            String dayOfWeek = daysOfWeek[dayOfWeekInt - 1];
            String dayNum = dayNumFormat.format(cal.getTime());
            String label = monthFormat.format(cal.getTime());
            
            dateList.add(new DateItem(dbDate, dayOfWeek, dayNum, label));
            
            if (i == 0) {
                selectedDate = dbDate; // Automatically select today's date as default
            }
            
            cal.add(java.util.Calendar.DAY_OF_MONTH, 1);
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        txtMovieTitle = findViewById(R.id.txt_movie_title);
        txtNoShowtimes = findViewById(R.id.txt_no_showtimes);
        layoutDateContainer = findViewById(R.id.layout_date_container);
        layoutCinemaContainer = findViewById(R.id.layout_cinema_container);

        txtMovieTitle.setText(movie.getTitle());
        btnBack.setOnClickListener(v -> finish());
    }

    private void displayDates() {
        layoutDateContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < dateList.size(); i++) {
            DateItem item = dateList.get(i);
            View dateView = inflater.inflate(R.layout.item_date_card, layoutDateContainer, false);
            
            TextView txtDay = dateView.findViewById(R.id.txt_day_of_week);
            TextView txtNum = dateView.findViewById(R.id.txt_day_number);
            TextView txtMonth = dateView.findViewById(R.id.txt_month);

            txtDay.setText(item.dayOfWeek);
            txtNum.setText(item.dayNum);
            txtMonth.setText(item.label);

            // Highlight selected date
            if (item.dbDate.equals(selectedDate)) {
                dateView.setBackgroundResource(R.drawable.bg_chip_selected);
                txtDay.setTextColor(getResources().getColor(R.color.black));
                txtNum.setTextColor(getResources().getColor(R.color.black));
                txtMonth.setTextColor(getResources().getColor(R.color.black));
            } else {
                dateView.setBackgroundResource(R.drawable.bg_chip_unselected);
                txtDay.setTextColor(getResources().getColor(R.color.text_secondary));
                txtNum.setTextColor(getResources().getColor(R.color.text_primary));
                txtMonth.setTextColor(getResources().getColor(R.color.text_muted));
            }

            dateView.setOnClickListener(v -> {
                selectedDate = item.dbDate;
                displayDates(); // Re-render dates to update selection styling
                loadCinemasAndShowtimes(); // Load new showtimes
            });

            layoutDateContainer.addView(dateView);
        }
    }

    private void loadCinemasAndShowtimes() {
        layoutCinemaContainer.removeAllViews();
        List<String> cinemas = dbHelper.getCinemasForMovieAndDate(movieId, selectedDate);

        if (cinemas == null || cinemas.isEmpty()) {
            txtNoShowtimes.setVisibility(View.VISIBLE);
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        boolean hasAnyShowtime = false;

        for (String cinema : cinemas) {
            List<Showtime> showtimes = dbHelper.getShowtimes(movieId, selectedDate, cinema);
            List<Showtime> futureShowtimes = new ArrayList<>();
            
            java.util.Calendar currentCal = java.util.Calendar.getInstance();
            java.text.SimpleDateFormat dbFormat = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            String todayStr = dbFormat.format(currentCal.getTime());

            for (Showtime showtime : showtimes) {
                if (selectedDate.equals(todayStr)) {
                    try {
                        String[] timeParts = showtime.getTime().split(":");
                        int showHour = Integer.parseInt(timeParts[0]);
                        int showMin = Integer.parseInt(timeParts[1]);

                        int curHour = currentCal.get(java.util.Calendar.HOUR_OF_DAY);
                        int curMin = currentCal.get(java.util.Calendar.MINUTE);

                        if (showHour < curHour || (showHour == curHour && showMin <= curMin)) {
                            continue; // Skip past showtimes on today's date
                        }
                    } catch (Exception e) {
                        // Keep showtime if parsing fails
                    }
                }
                futureShowtimes.add(showtime);
            }

            if (futureShowtimes.isEmpty()) {
                continue; // Skip this cinema if no future showtimes are available
            }

            hasAnyShowtime = true;

            View cinemaView = inflater.inflate(R.layout.item_cinema, layoutCinemaContainer, false);
            TextView txtCinemaName = cinemaView.findViewById(R.id.txt_cinema_name);
            txtCinemaName.setText(cinema);

            GridLayout gridShowtimes = cinemaView.findViewById(R.id.grid_showtimes);
            gridShowtimes.removeAllViews();

            for (Showtime showtime : futureShowtimes) {
                View chipView = inflater.inflate(R.layout.item_showtime_chip, gridShowtimes, false);
                TextView txtTime = chipView.findViewById(R.id.txt_time);
                txtTime.setText(showtime.getTime());

                chipView.setOnClickListener(v -> {
                    Intent intent = new Intent(BookingActivity.this, SeatSelectionActivity.class);
                    intent.putExtra("movie_id", movieId);
                    intent.putExtra("showtime_id", showtime.getId());
                    startActivity(intent);
                });

                gridShowtimes.addView(chipView);
            }

            layoutCinemaContainer.addView(cinemaView);
        }

        if (!hasAnyShowtime) {
            txtNoShowtimes.setVisibility(View.VISIBLE);
        } else {
            txtNoShowtimes.setVisibility(View.GONE);
        }
    }
}
