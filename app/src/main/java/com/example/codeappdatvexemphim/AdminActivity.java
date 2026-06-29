package com.example.codeappdatvexemphim;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.Spinner;
import android.widget.LinearLayout;
import androidx.cardview.widget.CardView;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminActivity extends AppCompatActivity implements TicketAdapter.OnTicketClickListener {

    private ImageButton btnBack;
    private TextView txtAdminTitle;
    private TextView txtStatRevenue, txtStatTickets, txtAdminNoBookings, btnAdminLogout;
    private RecyclerView rvAdminBookings;

    // Panels
    private View layoutAdminMenu;
    private View layoutPanelStats;
    private View layoutPanelNowShowing;
    private View layoutPanelComingSoon;

    // Menu Cards
    private CardView btnMenuStats;
    private CardView btnMenuNowShowing;
    private CardView btnMenuComingSoon;

    // Now Showing Add Form Fields
    private EditText edtTitle, edtGenre, edtDuration, edtRating, edtRelease, edtSynopsis, edtPoster, edtCover;
    private AppCompatButton btnAddMovie;

    // Coming Soon Add Form Fields
    private EditText edtTitleComing, edtGenreComing, edtDurationComing, edtRatingComing, edtReleaseComing, edtSynopsisComing, edtPosterComing, edtCoverComing;
    private AppCompatButton btnAddMovieComing;

    // Edit Movie state & views
    private int editingMovieId = -1;
    private int editingComingMovieId = -1;
    private AppCompatButton btnCancelEdit;
    private AppCompatButton btnCancelEditComing;
    private TextView txtFormTitle;
    private TextView txtFormTitleComing;
    private androidx.core.widget.NestedScrollView scrollDashboard;

    private DatabaseHelper dbHelper;
    private TicketAdapter adapter;
    private List<Booking> allBookings = new ArrayList<>();

    // Fallbacks in case poster or cover inputs are empty
    private final String FALLBACK_POSTER = DatabaseHelper.FALLBACK_POSTER;
    private final String FALLBACK_COVER = DatabaseHelper.FALLBACK_COVER;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        dbHelper = DatabaseHelper.getInstance(this);
        initViews();
        setupRecyclerView();
        loadSystemData();
    }

    private void initViews() {
        // Main action bar
        btnBack = findViewById(R.id.btn_back);
        txtAdminTitle = findViewById(R.id.txt_admin_title);
        btnAdminLogout = findViewById(R.id.btn_admin_logout);

        // Panels
        layoutAdminMenu = findViewById(R.id.layout_admin_menu);
        layoutPanelStats = findViewById(R.id.layout_panel_stats);
        layoutPanelNowShowing = findViewById(R.id.layout_panel_now_showing);
        layoutPanelComingSoon = findViewById(R.id.layout_panel_coming_soon);

        // Menu Cards
        btnMenuStats = findViewById(R.id.btn_menu_stats);
        btnMenuNowShowing = findViewById(R.id.btn_menu_now_showing);
        btnMenuComingSoon = findViewById(R.id.btn_menu_coming_soon);

        // Bottom list inside menu
        rvAdminBookings = findViewById(R.id.rv_admin_bookings);
        txtAdminNoBookings = findViewById(R.id.txt_admin_no_bookings);

        // Overall stats textviews (inside Stats panel)
        txtStatRevenue = findViewById(R.id.txt_stat_revenue);
        txtStatTickets = findViewById(R.id.txt_stat_tickets);

        // Now Showing Add Form Fields
        edtTitle = findViewById(R.id.edt_admin_title);
        edtGenre = findViewById(R.id.edt_admin_genre);
        edtDuration = findViewById(R.id.edt_admin_duration);
        edtRating = findViewById(R.id.edt_admin_rating);
        edtRelease = findViewById(R.id.edt_admin_release);
        edtSynopsis = findViewById(R.id.edt_admin_synopsis);
        edtPoster = findViewById(R.id.edt_admin_poster);
        edtCover = findViewById(R.id.edt_admin_cover);
        btnAddMovie = findViewById(R.id.btn_admin_add_movie);

        // Coming Soon Add Form Fields
        edtTitleComing = findViewById(R.id.edt_admin_title_coming);
        edtGenreComing = findViewById(R.id.edt_admin_genre_coming);
        edtDurationComing = findViewById(R.id.edt_admin_duration_coming);
        edtRatingComing = findViewById(R.id.edt_admin_rating_coming);
        edtReleaseComing = findViewById(R.id.edt_admin_release_coming);
        edtSynopsisComing = findViewById(R.id.edt_admin_synopsis_coming);
        edtPosterComing = findViewById(R.id.edt_admin_poster_coming);
        edtCoverComing = findViewById(R.id.edt_admin_cover_coming);
        btnAddMovieComing = findViewById(R.id.btn_admin_add_movie_coming);

        // Bind Edit Mode views
        btnCancelEdit = findViewById(R.id.btn_admin_cancel_edit);
        btnCancelEditComing = findViewById(R.id.btn_admin_cancel_edit_coming);
        txtFormTitle = findViewById(R.id.txt_admin_form_title);
        txtFormTitleComing = findViewById(R.id.txt_admin_form_title_coming);
        scrollDashboard = findViewById(R.id.scroll_dashboard);

        // Card clicks
        btnMenuStats.setOnClickListener(v -> {
            showPanel(layoutPanelStats, "Thống Kê Doanh Thu");
            loadStatistics();
        });

        btnMenuNowShowing.setOnClickListener(v -> {
            showPanel(layoutPanelNowShowing, "Quản Lý Phim Đang Chiếu");
            loadNowShowingMovies();
        });

        btnMenuComingSoon.setOnClickListener(v -> {
            showPanel(layoutPanelComingSoon, "Quản Lý Phim Sắp Chiếu");
            loadComingSoonMovies();
        });

        // Add actions
        btnAddMovie.setOnClickListener(v -> handleAddNowShowing());
        btnAddMovieComing.setOnClickListener(v -> handleAddComingSoon());
        btnCancelEdit.setOnClickListener(v -> resetNowShowingForm());
        btnCancelEditComing.setOnClickListener(v -> resetComingSoonForm());

        // Date Picker Actions
        edtRelease.setOnClickListener(v -> showDatePickerDialog(edtRelease));
        edtReleaseComing.setOnClickListener(v -> showDatePickerDialog(edtReleaseComing));

        // Header actions
        btnBack.setOnClickListener(v -> handleBackAction());
        btnAdminLogout.setOnClickListener(v -> {
            SharedPreferences sharedPreferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();
            Toast.makeText(AdminActivity.this, "Đã đăng xuất tài khoản Admin!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(AdminActivity.this, LoginActivity.class));
            finishAffinity();
        });
    }

    private void showPanel(View activePanel, String title) {
        layoutAdminMenu.setVisibility(View.GONE);
        layoutPanelStats.setVisibility(View.GONE);
        layoutPanelNowShowing.setVisibility(View.GONE);
        layoutPanelComingSoon.setVisibility(View.GONE);

        activePanel.setVisibility(View.VISIBLE);
        txtAdminTitle.setText(title);

        if (activePanel == layoutAdminMenu) {
            btnAdminLogout.setVisibility(View.VISIBLE);
        } else {
            btnAdminLogout.setVisibility(View.GONE);
        }
    }

    private void handleBackAction() {
        if (layoutAdminMenu.getVisibility() != View.VISIBLE) {
            showPanel(layoutAdminMenu, "Quản Trị Hệ Thống");
        } else {
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        handleBackAction();
    }

    private void setupRecyclerView() {
        rvAdminBookings.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TicketAdapter(allBookings, this);
        rvAdminBookings.setAdapter(adapter);
    }

    private void loadSystemData() {
        // Load Revenue & Ticket summaries
        double totalRevenue = dbHelper.getTotalRevenue();
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        txtStatRevenue.setText(currencyFormat.format(totalRevenue));

        allBookings = dbHelper.getAllBookings();
        int totalTickets = 0;
        for (Booking b : allBookings) {
            if (b.getSeats() != null && !b.getSeats().trim().isEmpty()) {
                totalTickets += b.getSeats().split(",").length;
            }
        }
        txtStatTickets.setText(totalTickets + " vé");

        // Load Bookings History list
        if (allBookings == null || allBookings.isEmpty()) {
            rvAdminBookings.setVisibility(View.GONE);
            txtAdminNoBookings.setVisibility(View.VISIBLE);
        } else {
            txtAdminNoBookings.setVisibility(View.GONE);
            rvAdminBookings.setVisibility(View.VISIBLE);
            adapter.updateData(allBookings);
        }
    }

    private void loadStatistics() {
        // Aggregate ticket counts
        List<Booking> bookings = dbHelper.getAllBookings();
        List<Movie> allMovies = dbHelper.getAllMovies();
        java.util.Map<String, Integer> movieTicketCounts = new java.util.HashMap<>();
        
        for (Movie m : allMovies) {
            movieTicketCounts.put(m.getTitle(), 0);
        }
        for (Booking b : bookings) {
            String title = b.getMovieTitle();
            String seatsStr = b.getSeats();
            int count = 0;
            if (seatsStr != null && !seatsStr.trim().isEmpty()) {
                count = seatsStr.split(",").length;
            }
            movieTicketCounts.put(title, movieTicketCounts.getOrDefault(title, 0) + count);
        }

        LinearLayout layoutStatsList = findViewById(R.id.layout_stats_list);
        layoutStatsList.removeAllViews();

        for (Movie m : allMovies) {
            View row = getLayoutInflater().inflate(R.layout.item_admin_stat_row, layoutStatsList, false);
            TextView txtMovieTitle = row.findViewById(R.id.txt_stat_movie_title);
            TextView txtMovieTickets = row.findViewById(R.id.txt_stat_movie_tickets);

            txtMovieTitle.setText(m.getTitle());
            int count = movieTicketCounts.getOrDefault(m.getTitle(), 0);
            txtMovieTickets.setText(count + " vé");

            layoutStatsList.addView(row);
        }
    }

    private void loadNowShowingMovies() {
        LinearLayout layoutList = findViewById(R.id.layout_now_showing_list);
        layoutList.removeAllViews();

        List<Movie> movies = dbHelper.getMoviesByStatus("now_showing");
        for (Movie m : movies) {
            View row = getLayoutInflater().inflate(R.layout.item_admin_movie_row, layoutList, false);
            TextView txtTitle = row.findViewById(R.id.txt_row_movie_title);
            TextView txtInfo = row.findViewById(R.id.txt_row_movie_info);
            ImageButton btnDelete = row.findViewById(R.id.btn_row_movie_delete);
            android.widget.CheckBox cbHot = row.findViewById(R.id.cb_row_movie_hot);

            txtTitle.setText(m.getTitle());
            txtInfo.setText(m.getGenre() + " • " + m.getDuration() + " phút • " + m.getRating() + "★");
            cbHot.setChecked(m.isHot());

            cbHot.setOnCheckedChangeListener((buttonView, isChecked) -> {
                boolean success = dbHelper.updateMovieHotStatus(m.getId(), isChecked);
                if (success) {
                    m.setHot(isChecked);
                    Toast.makeText(this, "Đã cập nhật trạng thái Phim Hot cho '" + m.getTitle() + "'!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Lỗi khi cập nhật trạng thái!", Toast.LENGTH_SHORT).show();
                    // Temporary remove listener to prevent infinite loop on reset
                    cbHot.setOnCheckedChangeListener(null);
                    cbHot.setChecked(!isChecked);
                    cbHot.setOnCheckedChangeListener((bv, val) -> updateHotStatus(m, cbHot, val));
                }
            });

            btnDelete.setOnClickListener(v -> {
                new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Xóa phim")
                    .setMessage("Bạn có chắc chắn muốn xóa phim '" + m.getTitle() + "'? Tất cả suất chiếu liên quan cũng sẽ bị xóa.")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        boolean success = dbHelper.deleteMovie(m.getId());
                        if (success) {
                            Toast.makeText(this, "Đã xóa phim thành công!", Toast.LENGTH_SHORT).show();
                            loadNowShowingMovies();
                            loadSystemData();
                            if (editingMovieId == m.getId()) {
                                resetNowShowingForm();
                            }
                        } else {
                            Toast.makeText(this, "Lỗi khi xóa phim!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
            });

            row.setOnClickListener(v -> enterEditNowShowing(m));

            layoutList.addView(row);
        }
    }

    private void updateHotStatus(Movie m, android.widget.CheckBox cb, boolean isChecked) {
        boolean success = dbHelper.updateMovieHotStatus(m.getId(), isChecked);
        if (success) {
            m.setHot(isChecked);
            Toast.makeText(this, "Đã cập nhật trạng thái Phim Hot cho '" + m.getTitle() + "'!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Lỗi khi cập nhật trạng thái!", Toast.LENGTH_SHORT).show();
            cb.setOnCheckedChangeListener(null);
            cb.setChecked(!isChecked);
            cb.setOnCheckedChangeListener((bv, val) -> updateHotStatus(m, cb, val));
        }
    }

    private void handleAddNowShowing() {
        String title = edtTitle.getText().toString().trim();
        String genre = edtGenre.getText().toString().trim();
        String durationStr = edtDuration.getText().toString().trim();
        String ratingStr = edtRating.getText().toString().trim();
        String release = edtRelease.getText().toString().trim();
        String synopsis = edtSynopsis.getText().toString().trim();
        String poster = edtPoster.getText().toString().trim();
        String cover = edtCover.getText().toString().trim();

        if (title.isEmpty() || genre.isEmpty() || durationStr.isEmpty() || ratingStr.isEmpty() 
                || release.isEmpty() || synopsis.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ các thông tin phim chính!", Toast.LENGTH_SHORT).show();
            return;
        }

        int duration;
        double rating;
        try {
            duration = Integer.parseInt(durationStr);
            rating = Double.parseDouble(ratingStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Thời lượng hoặc Điểm đánh giá không hợp lệ!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (poster.isEmpty()) poster = FALLBACK_POSTER;
        if (cover.isEmpty()) cover = FALLBACK_COVER;

        if (editingMovieId == -1) {
            long result = dbHelper.addMovie(title, genre, duration, rating, release, synopsis, poster, cover, "now_showing");
            if (result != -1) {
                Toast.makeText(this, "Thêm phim đang chiếu thành công! Suất chiếu mẫu tự động đã được tạo.", Toast.LENGTH_LONG).show();
                resetNowShowingForm();
                loadNowShowingMovies();
                loadSystemData();
            } else {
                Toast.makeText(this, "Lỗi thêm phim vào cơ sở dữ liệu!", Toast.LENGTH_SHORT).show();
            }
        } else {
            boolean success = dbHelper.updateMovie(editingMovieId, title, genre, duration, rating, release, synopsis, poster, cover, "now_showing");
            if (success) {
                Toast.makeText(this, "Cập nhật thông tin phim thành công!", Toast.LENGTH_SHORT).show();
                resetNowShowingForm();
                loadNowShowingMovies();
                loadSystemData();
            } else {
                Toast.makeText(this, "Lỗi khi cập nhật thông tin phim!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadComingSoonMovies() {
        LinearLayout layoutList = findViewById(R.id.layout_coming_soon_list);
        layoutList.removeAllViews();

        List<Movie> movies = dbHelper.getMoviesByStatus("coming_soon");
        for (Movie m : movies) {
            View row = getLayoutInflater().inflate(R.layout.item_admin_movie_row, layoutList, false);
            TextView txtTitle = row.findViewById(R.id.txt_row_movie_title);
            TextView txtInfo = row.findViewById(R.id.txt_row_movie_info);
            ImageButton btnDelete = row.findViewById(R.id.btn_row_movie_delete);
            android.widget.CheckBox cbHot = row.findViewById(R.id.cb_row_movie_hot);

            txtTitle.setText(m.getTitle());
            txtInfo.setText(m.getGenre() + " • " + m.getDuration() + " phút • " + m.getRating() + "★");
            cbHot.setChecked(m.isHot());

            cbHot.setOnCheckedChangeListener((buttonView, isChecked) -> {
                boolean success = dbHelper.updateMovieHotStatus(m.getId(), isChecked);
                if (success) {
                    m.setHot(isChecked);
                    Toast.makeText(this, "Đã cập nhật trạng thái Phim Hot cho '" + m.getTitle() + "'!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Lỗi khi cập nhật trạng thái!", Toast.LENGTH_SHORT).show();
                    cbHot.setOnCheckedChangeListener(null);
                    cbHot.setChecked(!isChecked);
                    cbHot.setOnCheckedChangeListener((bv, val) -> updateHotStatus(m, cbHot, val));
                }
            });

            btnDelete.setOnClickListener(v -> {
                new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Xóa phim")
                    .setMessage("Bạn có chắc chắn muốn xóa phim '" + m.getTitle() + "'? Tất cả suất chiếu liên quan cũng sẽ bị xóa.")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        boolean success = dbHelper.deleteMovie(m.getId());
                        if (success) {
                            Toast.makeText(this, "Đã xóa phim thành công!", Toast.LENGTH_SHORT).show();
                            loadComingSoonMovies();
                            loadSystemData();
                            if (editingComingMovieId == m.getId()) {
                                resetComingSoonForm();
                            }
                        } else {
                            Toast.makeText(this, "Lỗi khi xóa phim!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
            });

            row.setOnClickListener(v -> enterEditComingSoon(m));

            layoutList.addView(row);
        }
    }

    private void handleAddComingSoon() {
        String title = edtTitleComing.getText().toString().trim();
        String genre = edtGenreComing.getText().toString().trim();
        String durationStr = edtDurationComing.getText().toString().trim();
        String ratingStr = edtRatingComing.getText().toString().trim();
        String release = edtReleaseComing.getText().toString().trim();
        String synopsis = edtSynopsisComing.getText().toString().trim();
        String poster = edtPosterComing.getText().toString().trim();
        String cover = edtCoverComing.getText().toString().trim();

        if (title.isEmpty() || genre.isEmpty() || durationStr.isEmpty() || ratingStr.isEmpty() 
                || release.isEmpty() || synopsis.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ các thông tin phim chính!", Toast.LENGTH_SHORT).show();
            return;
        }

        int duration;
        double rating;
        try {
            duration = Integer.parseInt(durationStr);
            rating = Double.parseDouble(ratingStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Thời lượng hoặc Điểm đánh giá không hợp lệ!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (poster.isEmpty()) poster = FALLBACK_POSTER;
        if (cover.isEmpty()) cover = FALLBACK_COVER;

        if (editingComingMovieId == -1) {
            long result = dbHelper.addMovie(title, genre, duration, rating, release, synopsis, poster, cover, "coming_soon");
            if (result != -1) {
                Toast.makeText(this, "Thêm phim sắp chiếu thành công!", Toast.LENGTH_SHORT).show();
                resetComingSoonForm();
                loadComingSoonMovies();
                loadSystemData();
            } else {
                Toast.makeText(this, "Lỗi thêm phim vào cơ sở dữ liệu!", Toast.LENGTH_SHORT).show();
            }
        } else {
            boolean success = dbHelper.updateMovie(editingComingMovieId, title, genre, duration, rating, release, synopsis, poster, cover, "coming_soon");
            if (success) {
                Toast.makeText(this, "Cập nhật thông tin phim thành công!", Toast.LENGTH_SHORT).show();
                resetComingSoonForm();
                loadComingSoonMovies();
                loadSystemData();
            } else {
                Toast.makeText(this, "Lỗi khi cập nhật thông tin phim!", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void showDatePickerDialog(EditText targetEditText) {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        int year = calendar.get(java.util.Calendar.YEAR);
        int month = calendar.get(java.util.Calendar.MONTH);
        int day = calendar.get(java.util.Calendar.DAY_OF_MONTH);

        android.app.DatePickerDialog datePickerDialog = new android.app.DatePickerDialog(
            this,
            (view, selectedYear, selectedMonth, selectedDay) -> {
                String formattedDate = String.format(Locale.US, "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                targetEditText.setText(formattedDate);
            },
            year, month, day
        );
        datePickerDialog.show();
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

        Intent intent = new Intent(AdminActivity.this, TicketDetailsActivity.class);
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

    private void enterEditNowShowing(Movie m) {
        editingMovieId = m.getId();
        edtTitle.setText(m.getTitle());
        edtGenre.setText(m.getGenre());
        edtDuration.setText(String.valueOf(m.getDuration()));
        edtRating.setText(String.valueOf(m.getRating()));
        edtRelease.setText(m.getReleaseDate());
        edtSynopsis.setText(m.getSynopsis());
        edtPoster.setText(m.getPosterUri());
        edtCover.setText(m.getCoverUri());

        if (txtFormTitle != null) {
            txtFormTitle.setText("Chỉnh Sửa Phim: " + m.getTitle());
        }
        btnAddMovie.setText("Lưu Thay Đổi");
        btnCancelEdit.setVisibility(View.VISIBLE);

        if (scrollDashboard != null) {
            scrollDashboard.post(() -> {
                View formCard = findViewById(R.id.card_admin_now_showing_form);
                if (formCard != null) {
                    scrollDashboard.smoothScrollTo(0, formCard.getTop());
                }
            });
        }
    }

    private void enterEditComingSoon(Movie m) {
        editingComingMovieId = m.getId();
        edtTitleComing.setText(m.getTitle());
        edtGenreComing.setText(m.getGenre());
        edtDurationComing.setText(String.valueOf(m.getDuration()));
        edtRatingComing.setText(String.valueOf(m.getRating()));
        edtReleaseComing.setText(m.getReleaseDate());
        edtSynopsisComing.setText(m.getSynopsis());
        edtPosterComing.setText(m.getPosterUri());
        edtCoverComing.setText(m.getCoverUri());

        if (txtFormTitleComing != null) {
            txtFormTitleComing.setText("Chỉnh Sửa Phim: " + m.getTitle());
        }
        btnAddMovieComing.setText("Lưu Thay Đổi");
        btnCancelEditComing.setVisibility(View.VISIBLE);

        if (scrollDashboard != null) {
            scrollDashboard.post(() -> {
                View formCard = findViewById(R.id.card_admin_coming_soon_form);
                if (formCard != null) {
                    scrollDashboard.smoothScrollTo(0, formCard.getTop());
                }
            });
        }
    }

    private void resetNowShowingForm() {
        editingMovieId = -1;
        edtTitle.setText("");
        edtGenre.setText("");
        edtDuration.setText("");
        edtRating.setText("");
        edtRelease.setText("");
        edtSynopsis.setText("");
        edtPoster.setText("");
        edtCover.setText("");

        if (txtFormTitle != null) {
            txtFormTitle.setText("Thêm Phim Đang Chiếu Mới");
        }
        btnAddMovie.setText("Thêm Phim Đang Chiếu");
        btnCancelEdit.setVisibility(View.GONE);
    }

    private void resetComingSoonForm() {
        editingComingMovieId = -1;
        edtTitleComing.setText("");
        edtGenreComing.setText("");
        edtDurationComing.setText("");
        edtRatingComing.setText("");
        edtReleaseComing.setText("");
        edtSynopsisComing.setText("");
        edtPosterComing.setText("");
        edtCoverComing.setText("");

        if (txtFormTitleComing != null) {
            txtFormTitleComing.setText("Thêm Phim Sắp Chiếu Mới");
        }
        btnAddMovieComing.setText("Thêm Phim Sắp Chiếu");
        btnCancelEditComing.setVisibility(View.GONE);
    }
}
