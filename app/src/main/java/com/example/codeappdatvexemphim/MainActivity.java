package com.example.codeappdatvexemphim;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MovieAdapter.OnMovieClickListener {

    private TextView txtWelcomeUser, txtProfileFullname, txtProfileUsername, txtProfileEmail, txtProfileRole;
    private EditText edtSearchMovie;
    private NestedScrollView scrollDashboard;
    private RecyclerView rvNowShowing, rvComingSoon, rvSearchResults;
    private LinearLayout layoutProfile, layoutSearch;
    private BottomNavigationView bottomNavigation;
    private androidx.appcompat.widget.SwitchCompat switchTheme;

    // Hot Movies Banner Views & Variables
    private androidx.viewpager2.widget.ViewPager2 vpHotMovies;
    private LinearLayout layoutBannerIndicators;
    private android.widget.RelativeLayout layoutHotBanner;
    private List<Movie> hotMoviesList = new ArrayList<>();
    private HotMoviesAdapter hotMoviesAdapter;
    private final android.os.Handler bannerHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private Runnable bannerRunnable;
    private int currentBannerItem = 0;

    private DatabaseHelper dbHelper;
    private SharedPreferences sharedPreferences;

    private MovieAdapter nowShowingAdapter;
    private MovieAdapter comingSoonAdapter;
    private MovieAdapter searchResultsAdapter;

    private List<Movie> nowShowingList = new ArrayList<>();
    private List<Movie> comingSoonList = new ArrayList<>();

    private String selectedGenre = "Tất cả";
    private boolean isProgrammaticChange = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = DatabaseHelper.getInstance(this);
        sharedPreferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE);

        // Redirect if not logged in
        if (!sharedPreferences.contains("user_id")) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }

        initViews();
        setupUserSession();
        setupRecyclerViews();
        loadMovies();
        setupNavigation();
        setupSearch();
        setupChips();
        restoreTabState();
    }

    private void initViews() {
        txtWelcomeUser = findViewById(R.id.txt_welcome_user);
        edtSearchMovie = findViewById(R.id.edt_search_movie);
        scrollDashboard = findViewById(R.id.scroll_dashboard);
        rvNowShowing = findViewById(R.id.rv_now_showing);
        rvComingSoon = findViewById(R.id.rv_coming_soon);
        rvSearchResults = findViewById(R.id.rv_search_results);
        layoutProfile = findViewById(R.id.layout_profile);
        layoutSearch = findViewById(R.id.layout_search);
        bottomNavigation = findViewById(R.id.bottom_navigation);
        switchTheme = findViewById(R.id.switch_theme);

        vpHotMovies = findViewById(R.id.vp_hot_movies);
        layoutBannerIndicators = findViewById(R.id.layout_banner_indicators);
        layoutHotBanner = findViewById(R.id.layout_hot_banner);

        // Profile views
        txtProfileFullname = findViewById(R.id.txt_profile_fullname);
        txtProfileUsername = findViewById(R.id.txt_profile_username);
        txtProfileEmail = findViewById(R.id.txt_profile_email);
        txtProfileRole = findViewById(R.id.txt_profile_role);

    }

    private void setupUserSession() {
        String fullname = sharedPreferences.getString("fullname", "Người dùng");
        txtWelcomeUser.setText(fullname);

        // Populate Profile
        txtProfileFullname.setText(fullname);
        txtProfileUsername.setText(sharedPreferences.getString("username", ""));
        txtProfileEmail.setText(sharedPreferences.getString("email", ""));
        
        String role = sharedPreferences.getString("role", "user");
        if (role.equals("admin")) {
            txtProfileRole.setText("QUẢN TRỊ VIÊN");
            // Show Admin Option in Bottom Nav
            bottomNavigation.getMenu().findItem(R.id.nav_admin).setVisible(true);
        } else {
            txtProfileRole.setText("KHÁCH HÀNG");
        }

        findViewById(R.id.btn_logout).setOnClickListener(v -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();
            Toast.makeText(MainActivity.this, "Đã đăng xuất!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        });
        setupThemeSwitch();
    }

    private void setupRecyclerViews() {
        // Now Showing (Horizontal)
        rvNowShowing.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        nowShowingAdapter = new MovieAdapter(nowShowingList, true, this);
        rvNowShowing.setAdapter(nowShowingAdapter);

        // Coming Soon (Horizontal)
        rvComingSoon.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        comingSoonAdapter = new MovieAdapter(comingSoonList, true, this);
        rvComingSoon.setAdapter(comingSoonAdapter);

        // Search Results (Grid of 1 column, using our list item format)
        rvSearchResults.setLayoutManager(new LinearLayoutManager(this));
        searchResultsAdapter = new MovieAdapter(new ArrayList<>(), false, this);
        rvSearchResults.setAdapter(searchResultsAdapter);
    }

    private void loadMovies() {
        nowShowingList = dbHelper.getMoviesByStatus("now_showing");
        comingSoonList = dbHelper.getMoviesByStatus("coming_soon");

        nowShowingAdapter.updateData(nowShowingList);
        comingSoonAdapter.updateData(comingSoonList);

        setupHotMoviesBanner();
    }

    private void setupNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                // Show Home
                scrollDashboard.setVisibility(View.VISIBLE);
                layoutProfile.setVisibility(View.GONE);
                layoutSearch.setVisibility(View.VISIBLE);
                rvSearchResults.setVisibility(View.GONE);
                isProgrammaticChange = true;
                edtSearchMovie.setText(""); // clear search
                isProgrammaticChange = false;
                return true;
            } else if (itemId == R.id.nav_tickets) {
                // Open Tickets history activity
                startActivity(new Intent(MainActivity.this, TicketHistoryActivity.class));
                // Keep the selection on Home tab so returning doesn't bug out
                return false;
            } else if (itemId == R.id.nav_profile) {
                // Show Profile
                scrollDashboard.setVisibility(View.GONE);
                layoutProfile.setVisibility(View.VISIBLE);
                layoutSearch.setVisibility(View.GONE);
                rvSearchResults.setVisibility(View.GONE);
                return true;
            } else if (itemId == R.id.nav_admin) {
                // Open Admin activity
                startActivity(new Intent(MainActivity.this, AdminActivity.class));
                return false;
            }
            return false;
        });
    }

    private void setupSearch() {
        edtSearchMovie.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isProgrammaticChange) return;
                String query = s.toString().trim();
                if (!query.isEmpty()) {
                    scrollDashboard.setVisibility(View.GONE);
                    layoutProfile.setVisibility(View.GONE);
                    rvSearchResults.setVisibility(View.VISIBLE);

                    List<Movie> results = dbHelper.searchMovies(query);
                    searchResultsAdapter.updateData(results);
                } else {
                    // Reset to dashboard if search cleared
                    rvSearchResults.setVisibility(View.GONE);
                    if (bottomNavigation.getSelectedItemId() == R.id.nav_profile) {
                        layoutProfile.setVisibility(View.VISIBLE);
                    } else {
                        scrollDashboard.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupChips() {
        LinearLayout layoutCategories = findViewById(R.id.layout_categories);
        if (layoutCategories == null) return;
        layoutCategories.removeAllViews();

        // 1. Get all unique genres from database
        List<Movie> allMovies = dbHelper.getAllMovies();
        java.util.Set<String> uniqueGenres = new java.util.LinkedHashSet<>();
        
        // Add default/standard genres first to preserve a nice order
        uniqueGenres.add("Hành động");
        uniqueGenres.add("Viễn tưởng");
        uniqueGenres.add("Hoạt hình");
        
        for (Movie m : allMovies) {
            String genreStr = m.getGenre();
            if (genreStr != null && !genreStr.trim().isEmpty()) {
                String[] parts = genreStr.split(",");
                for (String part : parts) {
                    String clean = part.trim();
                    if (!clean.isEmpty()) {
                        // Prevent duplicates case-insensitively
                        boolean alreadyExists = false;
                        for (String existing : uniqueGenres) {
                            if (existing.equalsIgnoreCase(clean)) {
                                alreadyExists = true;
                                break;
                            }
                        }
                        if (!alreadyExists) {
                            uniqueGenres.add(clean);
                        }
                    }
                }
            }
        }

        // Create chip list
        List<String> genreList = new ArrayList<>();
        genreList.add("Tất cả");
        genreList.addAll(uniqueGenres);

        // 2. Inflate/create TextView chips
        for (int i = 0; i < genreList.size(); i++) {
            String genreName = genreList.get(i);
            TextView chip = new TextView(this);
            
            // Layout params
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
            if (i < genreList.size() - 1) {
                params.rightMargin = (int) (8 * getResources().getDisplayMetrics().density);
            }
            chip.setLayoutParams(params);
            chip.setText(genreName);
            chip.setPadding(
                (int) (16 * getResources().getDisplayMetrics().density),
                (int) (8 * getResources().getDisplayMetrics().density),
                (int) (16 * getResources().getDisplayMetrics().density),
                (int) (8 * getResources().getDisplayMetrics().density)
            );
            chip.setTextSize(13);
            chip.setClickable(true);
            chip.setFocusable(true);

            // Style active/inactive
            if (genreName.equals(selectedGenre)) {
                chip.setBackgroundResource(R.drawable.bg_chip_selected);
                chip.setTextColor(getResources().getColor(R.color.black));
                chip.setTypeface(null, android.graphics.Typeface.BOLD);
            } else {
                chip.setBackgroundResource(R.drawable.bg_chip_unselected);
                chip.setTextColor(getResources().getColor(R.color.text_secondary));
            }

            // Click listener
            chip.setOnClickListener(v -> {
                selectedGenre = genreName;
                setupChips(); // Re-draw chips to update selected styling
                filterMoviesByGenre(genreName);
            });

            layoutCategories.addView(chip);
        }
    }

    private void filterMoviesByGenre(String genre) {
        if (genre.equals("Tất cả")) {
            nowShowingAdapter.updateData(nowShowingList);
        } else {
            List<Movie> filtered = new ArrayList<>();
            for (Movie m : nowShowingList) {
                if (m.getGenre() != null && m.getGenre().toLowerCase().contains(genre.toLowerCase())) {
                    filtered.add(m);
                }
            }
            nowShowingAdapter.updateData(filtered);
        }
    }

    @Override
    public void onMovieClick(Movie movie) {
        Intent intent = new Intent(MainActivity.this, MovieDetailsActivity.class);
        intent.putExtra("movie_id", movie.getId());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh movies list and category chips on return (useful if admin added something)
        loadMovies();
        setupChips();
        if (layoutHotBanner != null && layoutHotBanner.getVisibility() == View.VISIBLE) {
            startBannerAutoScroll();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopBannerAutoScroll();
    }

    private void setupHotMoviesBanner() {
        hotMoviesList = dbHelper.getHotMovies();
        if (hotMoviesList == null || hotMoviesList.isEmpty()) {
            layoutHotBanner.setVisibility(View.GONE);
            stopBannerAutoScroll();
            return;
        }

        layoutHotBanner.setVisibility(View.VISIBLE);
        hotMoviesAdapter = new HotMoviesAdapter(this, hotMoviesList);
        vpHotMovies.setAdapter(hotMoviesAdapter);

        setupBannerTransformer();
        setupBannerIndicators();
        startBannerAutoScroll();
    }

    private void setupBannerTransformer() {
        int pageMargin = (int) (8 * getResources().getDisplayMetrics().density);
        int offset = (int) (30 * getResources().getDisplayMetrics().density);
        vpHotMovies.setOffscreenPageLimit(3);
        
        vpHotMovies.setPadding(offset, 0, offset, 0);
        vpHotMovies.setClipToPadding(false);
        vpHotMovies.setClipChildren(false);

        androidx.viewpager2.widget.CompositePageTransformer compositePageTransformer = new androidx.viewpager2.widget.CompositePageTransformer();
        compositePageTransformer.addTransformer(new androidx.viewpager2.widget.MarginPageTransformer(pageMargin));
        compositePageTransformer.addTransformer((page, position) -> {
            float r = 1 - Math.abs(position);
            page.setScaleY(0.85f + r * 0.15f);
            page.setAlpha(0.5f + r * 0.5f);
        });
        vpHotMovies.setPageTransformer(compositePageTransformer);

        vpHotMovies.registerOnPageChangeCallback(new androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentBannerItem = position;
                updateIndicators(position);
            }
        });
    }

    private void setupBannerIndicators() {
        layoutBannerIndicators.removeAllViews();
        int count = hotMoviesList.size();
        if (count <= 1) return;

        android.widget.ImageView[] indicators = new android.widget.ImageView[count];
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(8, 0, 8, 0);

        for (int i = 0; i < count; i++) {
            indicators[i] = new android.widget.ImageView(this);
            indicators[i].setImageDrawable(androidx.core.content.ContextCompat.getDrawable(this, 
                i == 0 ? R.drawable.bg_indicator_active : R.drawable.bg_indicator_inactive));
            layoutBannerIndicators.addView(indicators[i], params);
        }
    }

    private void updateIndicators(int position) {
        int count = layoutBannerIndicators.getChildCount();
        for (int i = 0; i < count; i++) {
            android.widget.ImageView img = (android.widget.ImageView) layoutBannerIndicators.getChildAt(i);
            if (img != null) {
                img.setImageDrawable(androidx.core.content.ContextCompat.getDrawable(this, 
                    i == position ? R.drawable.bg_indicator_active : R.drawable.bg_indicator_inactive));
            }
        }
    }

    private void startBannerAutoScroll() {
        if (hotMoviesList == null || hotMoviesList.size() <= 1) return;
        
        stopBannerAutoScroll();
        bannerRunnable = new Runnable() {
            @Override
            public void run() {
                if (hotMoviesList == null || hotMoviesList.isEmpty()) return;
                currentBannerItem = (currentBannerItem + 1) % hotMoviesList.size();
                vpHotMovies.setCurrentItem(currentBannerItem, true);
                bannerHandler.postDelayed(this, 4000);
            }
        };
        bannerHandler.postDelayed(bannerRunnable, 4000);
    }

    private void stopBannerAutoScroll() {
        if (bannerRunnable != null) {
            bannerHandler.removeCallbacks(bannerRunnable);
        }
    }

    private void setupThemeSwitch() {
        SharedPreferences themePrefs = getSharedPreferences("ThemePrefs", MODE_PRIVATE);
        boolean isDarkMode = themePrefs.getBoolean("is_dark_mode", true);
        switchTheme.setChecked(isDarkMode);

        switchTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = themePrefs.edit();
            editor.putBoolean("is_dark_mode", isChecked);
            editor.apply();

            if (isChecked) {
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
            }
        });
    }

    private void restoreTabState() {
        int selectedId = bottomNavigation.getSelectedItemId();
        if (selectedId == R.id.nav_profile) {
            scrollDashboard.setVisibility(View.GONE);
            layoutProfile.setVisibility(View.VISIBLE);
            layoutSearch.setVisibility(View.GONE);
            rvSearchResults.setVisibility(View.GONE);
        } else {
            scrollDashboard.setVisibility(View.VISIBLE);
            layoutProfile.setVisibility(View.GONE);
            layoutSearch.setVisibility(View.VISIBLE);
            rvSearchResults.setVisibility(View.GONE);
        }
    }
}