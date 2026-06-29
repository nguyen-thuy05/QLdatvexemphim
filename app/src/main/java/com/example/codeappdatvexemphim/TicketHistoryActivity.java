package com.example.codeappdatvexemphim;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class TicketHistoryActivity extends AppCompatActivity implements TicketAdapter.OnTicketClickListener {

    private ImageButton btnBack;
    private RecyclerView rvTicketHistory;
    private TextView txtEmptyHistory;

    private DatabaseHelper dbHelper;
    private SharedPreferences sharedPreferences;
    private TicketAdapter adapter;
    private List<Booking> bookingList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_history);

        dbHelper = DatabaseHelper.getInstance(this);
        sharedPreferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE);

        initViews();
        setupRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBookingHistory();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        rvTicketHistory = findViewById(R.id.rv_ticket_history);
        txtEmptyHistory = findViewById(R.id.txt_empty_history);

        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        rvTicketHistory.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TicketAdapter(bookingList, this);
        rvTicketHistory.setAdapter(adapter);
    }

    private void loadBookingHistory() {
        int userId = sharedPreferences.getInt("user_id", -1);
        if (userId == -1) {
            Toast.makeText(this, "Phiên đăng nhập hết hạn!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        bookingList = dbHelper.getBookingsForUser(userId);
        if (bookingList == null || bookingList.isEmpty()) {
            rvTicketHistory.setVisibility(View.GONE);
            txtEmptyHistory.setVisibility(View.VISIBLE);
        } else {
            txtEmptyHistory.setVisibility(View.GONE);
            rvTicketHistory.setVisibility(View.VISIBLE);
            adapter.updateData(bookingList);
        }
    }

    @Override
    public void onTicketClick(Booking booking) {
        // Find movie genre for this booking to display in Ticket Details
        String genre = "Hành động / Viễn tưởng";
        Showtime st = dbHelper.getShowtimeById(booking.getShowtimeId());
        if (st != null) {
            Movie mv = dbHelper.getMovieById(st.getMovieId());
            if (mv != null) {
                genre = mv.getGenre();
            }
        }

        Intent intent = new Intent(TicketHistoryActivity.this, TicketDetailsActivity.class);
        intent.putExtra("ticket_code", booking.getTicketCode());
        intent.putExtra("movie_title", booking.getMovieTitle());
        intent.putExtra("movie_genre", genre);
        intent.putExtra("cinema", booking.getCinema());
        intent.putExtra("date", booking.getShowDate());
        intent.putExtra("time", booking.getShowTime());
        intent.putExtra("seats", booking.getSeats());
        intent.putExtra("payment_method", booking.getPaymentMethod());
        intent.putExtra("total_price", booking.getTotalPrice());
        startActivity(intent);
    }
}
