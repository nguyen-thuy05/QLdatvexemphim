package com.example.codeappdatvexemphim;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import com.bumptech.glide.Glide;
import java.util.Locale;

public class MovieDetailsActivity extends AppCompatActivity {

    private ImageView imgBackdrop, imgPoster;
    private ImageButton btnBack;
    private TextView txtTitle, txtGenre, txtRating, txtDuration, txtReleaseDate, txtSynopsis;
    private AppCompatButton btnBookNow;

    private DatabaseHelper dbHelper;
    private int movieId;
    private Movie movie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        dbHelper = DatabaseHelper.getInstance(this);

        // Retrieve Movie ID from Intent
        movieId = getIntent().getIntExtra("movie_id", -1);
        movie = dbHelper.getMovieById(movieId);

        if (movie == null) {
            Toast.makeText(this, "Không tìm thấy thông tin phim!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        displayMovieDetails();
    }

    private void initViews() {
        imgBackdrop = findViewById(R.id.img_backdrop);
        imgPoster = findViewById(R.id.img_poster);
        btnBack = findViewById(R.id.btn_back);
        txtTitle = findViewById(R.id.txt_title);
        txtGenre = findViewById(R.id.txt_genre);
        txtRating = findViewById(R.id.txt_rating);
        txtDuration = findViewById(R.id.txt_duration);
        txtReleaseDate = findViewById(R.id.txt_release_date);
        txtSynopsis = findViewById(R.id.txt_synopsis);
        btnBookNow = findViewById(R.id.btn_book_now);

        btnBack.setOnClickListener(v -> finish());
        
        btnBookNow.setOnClickListener(v -> {
            if ("coming_soon".equals(movie.getStatus())) {
                Toast.makeText(this, "Phim sắp chiếu chưa mở bán vé! Vui lòng chọn phim khác.", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(MovieDetailsActivity.this, BookingActivity.class);
                intent.putExtra("movie_id", movie.getId());
                startActivity(intent);
            }
        });
    }

    private void displayMovieDetails() {
        txtTitle.setText(movie.getTitle());
        txtGenre.setText(movie.getGenre());
        txtRating.setText(String.format(Locale.getDefault(), "★ %.1f", movie.getRating()));
        txtDuration.setText(movie.getDuration() + " phút");
        txtReleaseDate.setText(movie.getReleaseDate());
        txtSynopsis.setText(movie.getSynopsis());

        // Load images using Glide
        Glide.with(this)
                .load(DatabaseHelper.getGlideUrl(movie.getCoverUri()))
                .placeholder(R.drawable.ic_placeholder_movie)
                .error(R.drawable.ic_placeholder_movie)
                .skipMemoryCache(true)
                .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                    @Override
                    public boolean onLoadFailed(com.bumptech.glide.load.engine.GlideException e, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                        android.util.Log.e("GLIDE_ERROR", "Cover load failed for URL: " + movie.getCoverUri(), e);
                        if (e != null) {
                            for (Throwable t : e.getRootCauses()) {
                                android.util.Log.e("GLIDE_ERROR", "Cover root cause: " + t.toString(), t);
                            }
                        }
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                        android.util.Log.d("GLIDE_SUCCESS", "Cover loaded successfully!");
                        return false;
                    }
                })
                .into(imgBackdrop);

        Glide.with(this)
                .load(DatabaseHelper.getGlideUrl(movie.getPosterUri()))
                .placeholder(R.drawable.ic_placeholder_movie)
                .error(R.drawable.ic_placeholder_movie)
                .skipMemoryCache(true)
                .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                    @Override
                    public boolean onLoadFailed(com.bumptech.glide.load.engine.GlideException e, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                        android.util.Log.e("GLIDE_ERROR", "Poster load failed for URL: " + movie.getPosterUri(), e);
                        if (e != null) {
                            for (Throwable t : e.getRootCauses()) {
                                android.util.Log.e("GLIDE_ERROR", "Poster root cause: " + t.toString(), t);
                            }
                        }
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                        android.util.Log.d("GLIDE_SUCCESS", "Poster loaded successfully!");
                        return false;
                    }
                })
                .into(imgPoster);

        // Style the CTA button if it's coming soon
        if ("coming_soon".equals(movie.getStatus())) {
            btnBookNow.setText("Phim Sắp Chiếu");
            btnBookNow.setBackgroundResource(R.drawable.bg_chip_unselected);
            btnBookNow.setTextColor(getResources().getColor(R.color.text_secondary));
        }
    }
}
