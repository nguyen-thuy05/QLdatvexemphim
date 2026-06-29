package com.example.codeappdatvexemphim;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "movie_booking.db";
    private static final int DATABASE_VERSION = 6;

    // Table names
    public static final String TABLE_USERS = "users";
    public static final String TABLE_MOVIES = "movies";
    public static final String TABLE_SHOWTIMES = "showtimes";
    public static final String TABLE_BOOKINGS = "bookings";
    public static final String TABLE_VOUCHERS = "vouchers";

    public static final String FALLBACK_POSTER = "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?q=80&w=400";
    public static final String FALLBACK_COVER = "https://images.unsplash.com/photo-1517604931442-7e0c8ed2963c?q=80&w=1200";

    public static Object getGlideUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return android.R.drawable.ic_menu_gallery;
        }
        if (url.startsWith("http://") || url.startsWith("https://")) {
            com.bumptech.glide.load.model.LazyHeaders.Builder headersBuilder = new com.bumptech.glide.load.model.LazyHeaders.Builder()
                    .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

            try {
                java.net.URI uri = new java.net.URI(url);
                String host = uri.getHost();
                if (host != null) {
                    host = host.toLowerCase();
                    if (host.contains("cdn.com")) {
                        int cdnIdx = host.indexOf("cdn.com");
                        String prefix = host.substring(0, cdnIdx);
                        if (prefix.endsWith(".")) {
                            prefix = prefix.substring(0, prefix.length() - 1);
                        }
                        int lastDot = prefix.lastIndexOf('.');
                        String domainName = (lastDot != -1) ? prefix.substring(lastDot + 1) : prefix;
                        if (!domainName.isEmpty()) {
                            headersBuilder.addHeader("Referer", "https://" + domainName + ".vn/");
                        }
                    } else if (host.contains("vcmedia.vn")) {
                        headersBuilder.addHeader("Referer", "https://kenh14.vn/");
                    } else if (host.contains("vcdn.cloud")) {
                        headersBuilder.addHeader("Referer", "https://www.cgv.vn/");
                    } else if (host.contains("vccloud.vn")) {
                        if (host.contains("divineshop")) {
                            headersBuilder.addHeader("Referer", "https://divineshop.vn/");
                        } else {
                            headersBuilder.addHeader("Referer", "https://vccloud.vn/");
                        }
                    } else if (host.contains("behance.net")) {
                        headersBuilder.addHeader("Referer", "https://www.behance.net/");
                    }
                }
            } catch (Exception e) {
                // Ignore parsing exceptions and fallback to default headers
            }

            return new com.bumptech.glide.load.model.GlideUrl(url, headersBuilder.build());
        }
        return url;
    }

    // Common column names
    public static final String KEY_ID = "id";

    // Users Table columns
    public static final String USER_USERNAME = "username";
    public static final String USER_EMAIL = "email";
    public static final String USER_PASSWORD = "password";
    public static final String USER_ROLE = "role";
    public static final String USER_FULLNAME = "fullname";

    // Movies Table columns
    public static final String MOVIE_TITLE = "title";
    public static final String MOVIE_GENRE = "genre";
    public static final String MOVIE_DURATION = "duration";
    public static final String MOVIE_RATING = "rating";
    public static final String MOVIE_RELEASE = "release_date";
    public static final String MOVIE_SYNOPSIS = "synopsis";
    public static final String MOVIE_POSTER = "poster_uri";
    public static final String MOVIE_COVER = "cover_uri";
    public static final String MOVIE_STATUS = "status"; // 'now_showing' or 'coming_soon'
    public static final String MOVIE_IS_HOT = "is_hot"; // 1 if hot, 0 otherwise

    // Showtimes Table columns
    public static final String SHOW_MOVIE_ID = "movie_id";
    public static final String SHOW_CINEMA = "cinema";
    public static final String SHOW_DATE = "date";
    public static final String SHOW_TIME = "time";
    public static final String SHOW_PRICE = "price";

    // Bookings Table columns
    public static final String BOOK_USER_ID = "user_id";
    public static final String BOOK_SHOWTIME_ID = "showtime_id";
    public static final String BOOK_SEATS = "seats"; // Comma-separated list (e.g. "A1,A2")
    public static final String BOOK_TOTAL = "total_price";
    public static final String BOOK_TIME = "booking_time";
    public static final String BOOK_PAYMENT = "payment_method";
    public static final String BOOK_CODE = "ticket_code";
    public static final String BOOK_STATUS = "status"; // 'active' or 'cancelled'

    // Vouchers Table columns
    public static final String VOUCHER_USER_ID = "user_id";
    public static final String VOUCHER_CODE = "code";
    public static final String VOUCHER_VALUE = "value";
    public static final String VOUCHER_STATUS = "status"; // 'active' or 'used'

    // Create Table Statements
    private static final String CREATE_TABLE_USERS = "CREATE TABLE " + TABLE_USERS + " ("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + USER_USERNAME + " TEXT UNIQUE, "
            + USER_EMAIL + " TEXT UNIQUE, "
            + USER_PASSWORD + " TEXT, "
            + USER_ROLE + " TEXT, "
            + USER_FULLNAME + " TEXT" + ")";

    private static final String CREATE_TABLE_MOVIES = "CREATE TABLE " + TABLE_MOVIES + " ("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + MOVIE_TITLE + " TEXT, "
            + MOVIE_GENRE + " TEXT, "
            + MOVIE_DURATION + " INTEGER, "
            + MOVIE_RATING + " REAL, "
            + MOVIE_RELEASE + " TEXT, "
            + MOVIE_SYNOPSIS + " TEXT, "
            + MOVIE_POSTER + " TEXT, "
            + MOVIE_COVER + " TEXT, "
            + MOVIE_STATUS + " TEXT, "
            + MOVIE_IS_HOT + " INTEGER DEFAULT 0" + ")";

    private static final String CREATE_TABLE_SHOWTIMES = "CREATE TABLE " + TABLE_SHOWTIMES + " ("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + SHOW_MOVIE_ID + " INTEGER, "
            + SHOW_CINEMA + " TEXT, "
            + SHOW_DATE + " TEXT, "
            + SHOW_TIME + " TEXT, "
            + SHOW_PRICE + " REAL, "
            + "FOREIGN KEY(" + SHOW_MOVIE_ID + ") REFERENCES " + TABLE_MOVIES + "(" + KEY_ID + ") ON DELETE CASCADE)";

    private static final String CREATE_TABLE_BOOKINGS = "CREATE TABLE " + TABLE_BOOKINGS + " ("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + BOOK_USER_ID + " INTEGER, "
            + BOOK_SHOWTIME_ID + " INTEGER, "
            + BOOK_SEATS + " TEXT, "
            + BOOK_TOTAL + " REAL, "
            + BOOK_TIME + " TEXT, "
            + BOOK_PAYMENT + " TEXT, "
            + BOOK_CODE + " TEXT, "
            + BOOK_STATUS + " TEXT DEFAULT 'active', "
            + "FOREIGN KEY(" + BOOK_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + KEY_ID + "), "
            + "FOREIGN KEY(" + BOOK_SHOWTIME_ID + ") REFERENCES " + TABLE_SHOWTIMES + "(" + KEY_ID + ") ON DELETE CASCADE)";

    private static final String CREATE_TABLE_VOUCHERS = "CREATE TABLE " + TABLE_VOUCHERS + " ("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + VOUCHER_USER_ID + " INTEGER, "
            + VOUCHER_CODE + " TEXT UNIQUE, "
            + VOUCHER_VALUE + " REAL, "
            + VOUCHER_STATUS + " TEXT DEFAULT 'active')";

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_MOVIES);
        db.execSQL(CREATE_TABLE_SHOWTIMES);
        db.execSQL(CREATE_TABLE_BOOKINGS);
        db.execSQL(CREATE_TABLE_VOUCHERS);

        // Seed initial data
        seedInitialData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_VOUCHERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKINGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SHOWTIMES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MOVIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    private void seedInitialData(SQLiteDatabase db) {
        // 1. Seed Users (Admin & User)
        ContentValues adminVal = new ContentValues();
        adminVal.put(USER_USERNAME, "admin");
        adminVal.put(USER_EMAIL, "admin@gmail.com");
        adminVal.put(USER_PASSWORD, "admin");
        adminVal.put(USER_ROLE, "admin");
        adminVal.put(USER_FULLNAME, "Quản Trị Viên");
        db.insert(TABLE_USERS, null, adminVal);

        ContentValues userVal = new ContentValues();
        userVal.put(USER_USERNAME, "user");
        userVal.put(USER_EMAIL, "user@gmail.com");
        userVal.put(USER_PASSWORD, "user");
        userVal.put(USER_ROLE, "user");
        userVal.put(USER_FULLNAME, "Nguyễn Thị Thùy");
        db.insert(TABLE_USERS, null, userVal);

        // 2. Seed Movies (5 movies)
        long movie1Id = insertMovieSeed(db, "Dune: Hành Tinh Cát 2", "Khoa học viễn tưởng, Hành động", 166, 4.9, "2026-03-01", 
            "Dune: Part Two sẽ khám phá hành trình tiếp theo của Paul Atreides khi anh hợp nhất với Chani và người Fremen trong khi tìm cách trả thù những kẻ âm mưu hủy hoại gia đình mình.",
            "https://posterspy.com/wp-content/uploads/2024/04/Dune-Finished.jpg",
            "https://afamilycdn.com/150157425591193600/2024/2/26/01-170893464635147547634-1708935756534-1708935756888820663706.jpg", "now_showing", true);

        long movie2Id = insertMovieSeed(db, "COLONY: BẦY XÁC SỐNG(2026)", "Kinh dị, Hành động", 110, 4.8, "2026-06-02", 
            "Bộ phim lấy bối cảnh năm 2026 khi thế giới bị sụp đổ bởi đại dịch xác sống. Một nhóm người sống sót tại khu căn cứ Colony phải tìm cách sinh tồn trước bầy zombie khát máu.",
            "https://riocinemas.vn/Areas/Admin/Content/Fileuploads/images/poster%20web/2026/T6/colony-500_1780479019386%20(2).jpg",
            "https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/3/image/1800x/71252117777b696995f01934522c402d/6/4/640x396-colony.jpg", "now_showing", true);

        long movie3Id = insertMovieSeed(db, "Hố Đen Vũ Trụ (Interstellar)", "Viễn tưởng, Phiêu lưu, Kịch tính", 169, 4.9, "2014-11-07", 
            "Một nhóm các nhà thám hiện không gian sử dụng một hố đen mới được khám phá để vượt qua các giới hạn về du hành không gian của con người và chinh phục những khoảng không bao la.",
            "https://mir-s3-cdn-cf.behance.net/project_modules/hd_webp/297acd129204217.616629e21fe76.png",
            "https://tintuc-divineshop.cdn.vccloud.vn/wp-content/uploads/2025/02/interstellar-3.jpg", "now_showing", false);

        long movie4Id = insertMovieSeed(db, "Avatar: Dòng Chảy Của Nước", "Hành động, Phiêu lưu, Viễn tưởng", 192, 4.7, "2026-12-16", 
            "Jake Sully sống cùng gia đình mới được thành lập trên hành tinh Pandora. Khi một mối đe dọa quen thuộc quay trở lại để hoàn thành những gì đã bắt đầu trước đó, Jake phải làm việc với Neytiri.",
            "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?q=80&w=400",
            "https://images.unsplash.com/photo-1500485035595-cbe6f645feb1?q=80&w=1200", "coming_soon", false);

        long movie5Id = insertMovieSeed(db, "Oppenheimer: Kẻ Hủy Diệt Thế Giới", "Kịch tính, Lịch sử, Tiểu sử", 180, 4.8, "2026-07-21", 
            "Câu chuyện về nhà vật lý lý thuyết J. Robert Oppenheimer, người lãnh đạo Dự án Manhattan để tạo ra những quả bom nguyên tử đầu tiên cho Hoa Kỳ trong Thế chiến thứ hai.",
            "https://images.unsplash.com/photo-1440404653325-ab127d49abc1?q=80&w=400",
            "https://images.unsplash.com/photo-1451187580459-43490279c0fa?q=80&w=1200", "coming_soon", false);

        // 3. Seed Showtimes for Now Showing movies
        // Standard Dates: dynamically generate 4 days starting from today
        java.util.Calendar cal = java.util.Calendar.getInstance();
        java.text.SimpleDateFormat dbFormat = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        String[] dates = new String[4];
        for (int i = 0; i < 4; i++) {
            dates[i] = dbFormat.format(cal.getTime());
            cal.add(java.util.Calendar.DAY_OF_MONTH, 1);
        }
        String[] cinemas = {"CGV Vincom Center", "Lotte Cinema Cantavil", "BHD Star Thảo Điền"};
        String[] times = {"10:00", "13:30", "16:45", "19:30", "22:15"};

        // Seed showtimes for Movie 1 (Dune 2) and Movie 2 (Spider-Man) and Movie 3 (Interstellar)
        long[] activeMovieIds = {movie1Id, movie2Id, movie3Id};
        for (long mId : activeMovieIds) {
            for (String date : dates) {
                for (int c = 0; c < cinemas.length; c++) {
                    // Seed some times for each cinema
                    for (int t = 0; t < times.length; t++) {
                        // Skip some showtimes to make it realistic
                        if ((c + t) % 2 == 0) {
                            ContentValues stVal = new ContentValues();
                            stVal.put(SHOW_MOVIE_ID, mId);
                            stVal.put(SHOW_CINEMA, cinemas[c]);
                            stVal.put(SHOW_DATE, date);
                            stVal.put(SHOW_TIME, times[t]);
                            double basePrice = 85000.0;
                            if (times[t].equals("19:30") || times[t].equals("22:15")) {
                                basePrice = 110000.0; // VIP hours
                            }
                            stVal.put(SHOW_PRICE, basePrice);
                            db.insert(TABLE_SHOWTIMES, null, stVal);
                        }
                    }
                }
            }
        }
    }

    private long insertMovieSeed(SQLiteDatabase db, String title, String genre, int duration, 
                                 double rating, String release, String synopsis, String poster, String cover, String status, boolean isHot) {
        ContentValues val = new ContentValues();
        val.put(MOVIE_TITLE, title);
        val.put(MOVIE_GENRE, genre);
        val.put(MOVIE_DURATION, duration);
        val.put(MOVIE_RATING, rating);
        val.put(MOVIE_RELEASE, release);
        val.put(MOVIE_SYNOPSIS, synopsis);
        val.put(MOVIE_POSTER, poster);
        val.put(MOVIE_COVER, cover);
        val.put(MOVIE_STATUS, status);
        val.put(MOVIE_IS_HOT, isHot ? 1 : 0);
        return db.insert(TABLE_MOVIES, null, val);
    }

    // --- USER METHODS ---
    public boolean registerUser(String username, String email, String password, String fullname) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(USER_USERNAME, username);
        values.put(USER_EMAIL, email);
        values.put(USER_PASSWORD, password);
        values.put(USER_ROLE, "user"); // default role
        values.put(USER_FULLNAME, fullname);

        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    public User checkLogin(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE " 
                + USER_USERNAME + " = ? AND " + USER_PASSWORD + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{username, password});
        
        User user = null;
        if (cursor.moveToFirst()) {
            user = new User(
                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(USER_USERNAME)),
                cursor.getString(cursor.getColumnIndexOrThrow(USER_EMAIL)),
                cursor.getString(cursor.getColumnIndexOrThrow(USER_PASSWORD)),
                cursor.getString(cursor.getColumnIndexOrThrow(USER_ROLE)),
                cursor.getString(cursor.getColumnIndexOrThrow(USER_FULLNAME))
            );
        }
        cursor.close();
        return user;
    }

    public boolean isUsernameExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT 1 FROM " + TABLE_USERS + " WHERE " + USER_USERNAME + " = ?", new String[]{username});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    // --- MOVIE METHODS ---
    public List<Movie> getMoviesByStatus(String status) {
        List<Movie> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_MOVIES + " WHERE " + MOVIE_STATUS + " = ?", new String[]{status});
        
        if (cursor.moveToFirst()) {
            do {
                list.add(new Movie(
                    cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(MOVIE_TITLE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(MOVIE_GENRE)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(MOVIE_DURATION)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(MOVIE_RATING)),
                    cursor.getString(cursor.getColumnIndexOrThrow(MOVIE_RELEASE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(MOVIE_SYNOPSIS)),
                    cursor.getString(cursor.getColumnIndexOrThrow(MOVIE_POSTER)),
                    cursor.getString(cursor.getColumnIndexOrThrow(MOVIE_COVER)),
                    cursor.getString(cursor.getColumnIndexOrThrow(MOVIE_STATUS)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(MOVIE_IS_HOT)) == 1
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public List<Movie> searchMovies(String query) {
        List<Movie> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_MOVIES + " WHERE " + MOVIE_TITLE + " LIKE ?", new String[]{"%" + query + "%"});
        
        if (cursor.moveToFirst()) {
            do {
                list.add(new Movie(
                    cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(MOVIE_TITLE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(MOVIE_GENRE)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(MOVIE_DURATION)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(MOVIE_RATING)),
                    cursor.getString(cursor.getColumnIndexOrThrow(MOVIE_RELEASE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(MOVIE_SYNOPSIS)),
                    cursor.getString(cursor.getColumnIndexOrThrow(MOVIE_POSTER)),
                    cursor.getString(cursor.getColumnIndexOrThrow(MOVIE_COVER)),
                    cursor.getString(cursor.getColumnIndexOrThrow(MOVIE_STATUS)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(MOVIE_IS_HOT)) == 1
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public Movie getMovieById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_MOVIES + " WHERE " + KEY_ID + " = ?", new String[]{String.valueOf(id)});
        Movie movie = null;
        if (cursor.moveToFirst()) {
            movie = new Movie(
                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(MOVIE_TITLE)),
                cursor.getString(cursor.getColumnIndexOrThrow(MOVIE_GENRE)),
                cursor.getInt(cursor.getColumnIndexOrThrow(MOVIE_DURATION)),
                cursor.getDouble(cursor.getColumnIndexOrThrow(MOVIE_RATING)),
                cursor.getString(cursor.getColumnIndexOrThrow(MOVIE_RELEASE)),
                cursor.getString(cursor.getColumnIndexOrThrow(MOVIE_SYNOPSIS)),
                cursor.getString(cursor.getColumnIndexOrThrow(MOVIE_POSTER)),
                cursor.getString(cursor.getColumnIndexOrThrow(MOVIE_COVER)),
                cursor.getString(cursor.getColumnIndexOrThrow(MOVIE_STATUS)),
                cursor.getInt(cursor.getColumnIndexOrThrow(MOVIE_IS_HOT)) == 1
            );
        }
        cursor.close();
        return movie;
    }

    public long addMovie(String title, String genre, int duration, double rating, String releaseDate, 
                         String synopsis, String poster, String cover, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues val = new ContentValues();
        val.put(MOVIE_TITLE, title);
        val.put(MOVIE_GENRE, genre);
        val.put(MOVIE_DURATION, duration);
        val.put(MOVIE_RATING, rating);
        val.put(MOVIE_RELEASE, releaseDate);
        val.put(MOVIE_SYNOPSIS, synopsis);
        val.put(MOVIE_POSTER, poster);
        val.put(MOVIE_COVER, cover);
        val.put(MOVIE_STATUS, status);
        val.put(MOVIE_IS_HOT, 0); // added movies default to non-hot
        
        long movieId = db.insert(TABLE_MOVIES, null, val);
        
        // Auto-generate some mock showtimes for a newly added movie
        if (movieId != -1 && status.equals("now_showing")) {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            java.text.SimpleDateFormat dbFormat = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            String[] dates = new String[4];
            for (int i = 0; i < 4; i++) {
                dates[i] = dbFormat.format(cal.getTime());
                cal.add(java.util.Calendar.DAY_OF_MONTH, 1);
            }
            String[] cinemas = {"CGV Vincom Center", "Lotte Cinema Cantavil", "BHD Star Thảo Điền"};
            String[] times = {"11:00", "14:30", "18:00", "20:30"};
            
            for (String date : dates) {
                for (String cinema : cinemas) {
                    for (String time : times) {
                        ContentValues stVal = new ContentValues();
                        stVal.put(SHOW_MOVIE_ID, movieId);
                        stVal.put(SHOW_CINEMA, cinema);
                        stVal.put(SHOW_DATE, date);
                        stVal.put(SHOW_TIME, time);
                        stVal.put(SHOW_PRICE, 90000.0);
                        db.insert(TABLE_SHOWTIMES, null, stVal);
                    }
                }
            }
        }
        return movieId;
    }

    // --- SHOWTIME METHODS ---
    public List<String> getCinemasForMovieAndDate(int movieId, String date) {
        List<String> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT DISTINCT " + SHOW_CINEMA + " FROM " + TABLE_SHOWTIMES 
                + " WHERE " + SHOW_MOVIE_ID + " = ? AND " + SHOW_DATE + " = ?", 
                new String[]{String.valueOf(movieId), date});
        
        if (cursor.moveToFirst()) {
            do {
                list.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public List<Showtime> getShowtimes(int movieId, String date, String cinema) {
        List<Showtime> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_SHOWTIMES 
                + " WHERE " + SHOW_MOVIE_ID + " = ? AND " + SHOW_DATE + " = ? AND " + SHOW_CINEMA + " = ? ORDER BY " + SHOW_TIME + " ASC", 
                new String[]{String.valueOf(movieId), date, cinema});
        
        if (cursor.moveToFirst()) {
            do {
                list.add(new Showtime(
                    cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(SHOW_MOVIE_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(SHOW_CINEMA)),
                    cursor.getString(cursor.getColumnIndexOrThrow(SHOW_DATE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(SHOW_TIME)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(SHOW_PRICE))
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public Showtime getShowtimeById(int showtimeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_SHOWTIMES + " WHERE " + KEY_ID + " = ?", new String[]{String.valueOf(showtimeId)});
        Showtime showtime = null;
        if (cursor.moveToFirst()) {
            showtime = new Showtime(
                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)),
                cursor.getInt(cursor.getColumnIndexOrThrow(SHOW_MOVIE_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(SHOW_CINEMA)),
                cursor.getString(cursor.getColumnIndexOrThrow(SHOW_DATE)),
                cursor.getString(cursor.getColumnIndexOrThrow(SHOW_TIME)),
                cursor.getDouble(cursor.getColumnIndexOrThrow(SHOW_PRICE))
            );
        }
        cursor.close();
        return showtime;
    }

    // --- BOOKING METHODS ---
    public List<String> getBookedSeatsForShowtime(int showtimeId) {
        List<String> bookedSeats = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + BOOK_SEATS + " FROM " + TABLE_BOOKINGS + " WHERE " + BOOK_SHOWTIME_ID + " = ? AND " + BOOK_STATUS + " = 'active'", new String[]{String.valueOf(showtimeId)});
        
        if (cursor.moveToFirst()) {
            do {
                String seatsString = cursor.getString(0);
                if (seatsString != null && !seatsString.trim().isEmpty()) {
                    String[] seatsArr = seatsString.split(",");
                    for (String seat : seatsArr) {
                        bookedSeats.add(seat.trim());
                    }
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return bookedSeats;
    }

    public boolean insertBooking(int userId, int showtimeId, String seats, double totalPrice, 
                                 String bookingTime, String paymentMethod, String ticketCode) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues val = new ContentValues();
        val.put(BOOK_USER_ID, userId);
        val.put(BOOK_SHOWTIME_ID, showtimeId);
        val.put(BOOK_SEATS, seats);
        val.put(BOOK_TOTAL, totalPrice);
        val.put(BOOK_TIME, bookingTime);
        val.put(BOOK_PAYMENT, paymentMethod);
        val.put(BOOK_CODE, ticketCode);

        long result = db.insert(TABLE_BOOKINGS, null, val);
        return result != -1;
    }

    public List<Booking> getBookingsForUser(int userId) {
        List<Booking> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        // Joined query to fetch movie and showtime details
        String query = "SELECT b.*, m." + MOVIE_TITLE + ", m." + MOVIE_POSTER + ", s." + SHOW_CINEMA 
                + ", s." + SHOW_DATE + ", s." + SHOW_TIME 
                + " FROM " + TABLE_BOOKINGS + " b"
                + " JOIN " + TABLE_SHOWTIMES + " s ON b." + BOOK_SHOWTIME_ID + " = s." + KEY_ID
                + " JOIN " + TABLE_MOVIES + " m ON s." + SHOW_MOVIE_ID + " = m." + KEY_ID
                + " WHERE b." + BOOK_USER_ID + " = ?"
                + " ORDER BY b." + KEY_ID + " DESC";
                
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        
        if (cursor.moveToFirst()) {
            do {
                list.add(new Booking(
                    cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(BOOK_USER_ID)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(BOOK_SHOWTIME_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(BOOK_SEATS)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(BOOK_TOTAL)),
                    cursor.getString(cursor.getColumnIndexOrThrow(BOOK_TIME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(BOOK_PAYMENT)),
                    cursor.getString(cursor.getColumnIndexOrThrow(BOOK_CODE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(MOVIE_TITLE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(MOVIE_POSTER)),
                    cursor.getString(cursor.getColumnIndexOrThrow(SHOW_CINEMA)),
                    cursor.getString(cursor.getColumnIndexOrThrow(SHOW_DATE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(SHOW_TIME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(BOOK_STATUS))
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public List<Booking> getAllBookings() {
        List<Booking> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        String query = "SELECT b.*, m." + MOVIE_TITLE + ", m." + MOVIE_POSTER + ", s." + SHOW_CINEMA 
                + ", s." + SHOW_DATE + ", s." + SHOW_TIME 
                + " FROM " + TABLE_BOOKINGS + " b"
                + " JOIN " + TABLE_SHOWTIMES + " s ON b." + BOOK_SHOWTIME_ID + " = s." + KEY_ID
                + " JOIN " + TABLE_MOVIES + " m ON s." + SHOW_MOVIE_ID + " = m." + KEY_ID
                + " ORDER BY b." + KEY_ID + " DESC";
                
        Cursor cursor = db.rawQuery(query, null);
        
        if (cursor.moveToFirst()) {
            do {
                list.add(new Booking(
                    cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(BOOK_USER_ID)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(BOOK_SHOWTIME_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(BOOK_SEATS)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(BOOK_TOTAL)),
                    cursor.getString(cursor.getColumnIndexOrThrow(BOOK_TIME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(BOOK_PAYMENT)),
                    cursor.getString(cursor.getColumnIndexOrThrow(BOOK_CODE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(MOVIE_TITLE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(MOVIE_POSTER)),
                    cursor.getString(cursor.getColumnIndexOrThrow(SHOW_CINEMA)),
                    cursor.getString(cursor.getColumnIndexOrThrow(SHOW_DATE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(SHOW_TIME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(BOOK_STATUS))
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public Booking getBookingByCode(String code) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT b.*, m." + MOVIE_TITLE + ", m." + MOVIE_POSTER + ", s." + SHOW_CINEMA 
                + ", s." + SHOW_DATE + ", s." + SHOW_TIME 
                + " FROM " + TABLE_BOOKINGS + " b"
                + " JOIN " + TABLE_SHOWTIMES + " s ON b." + BOOK_SHOWTIME_ID + " = s." + KEY_ID
                + " JOIN " + TABLE_MOVIES + " m ON s." + SHOW_MOVIE_ID + " = m." + KEY_ID
                + " WHERE b." + BOOK_CODE + " = ?";
                
        Cursor cursor = db.rawQuery(query, new String[]{code});
        Booking booking = null;
        if (cursor.moveToFirst()) {
            booking = new Booking(
                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)),
                cursor.getInt(cursor.getColumnIndexOrThrow(BOOK_USER_ID)),
                cursor.getInt(cursor.getColumnIndexOrThrow(BOOK_SHOWTIME_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(BOOK_SEATS)),
                cursor.getDouble(cursor.getColumnIndexOrThrow(BOOK_TOTAL)),
                cursor.getString(cursor.getColumnIndexOrThrow(BOOK_TIME)),
                cursor.getString(cursor.getColumnIndexOrThrow(BOOK_PAYMENT)),
                cursor.getString(cursor.getColumnIndexOrThrow(BOOK_CODE)),
                cursor.getString(cursor.getColumnIndexOrThrow(MOVIE_TITLE)),
                cursor.getString(cursor.getColumnIndexOrThrow(MOVIE_POSTER)),
                cursor.getString(cursor.getColumnIndexOrThrow(SHOW_CINEMA)),
                cursor.getString(cursor.getColumnIndexOrThrow(SHOW_DATE)),
                cursor.getString(cursor.getColumnIndexOrThrow(SHOW_TIME)),
                cursor.getString(cursor.getColumnIndexOrThrow(BOOK_STATUS))
            );
        }
        cursor.close();
        return booking;
    }

    public boolean insertVoucher(int userId, String code, double value) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues val = new ContentValues();
        val.put(VOUCHER_USER_ID, userId);
        val.put(VOUCHER_CODE, code);
        val.put(VOUCHER_VALUE, value);
        val.put(VOUCHER_STATUS, "active");
        long result = db.insert(TABLE_VOUCHERS, null, val);
        return result != -1;
    }

    public List<Voucher> getActiveVouchersForUser(int userId) {
        List<Voucher> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_VOUCHERS + " WHERE " + VOUCHER_USER_ID + " = ? AND " + VOUCHER_STATUS + " = 'active'", new String[]{String.valueOf(userId)});
        if (cursor.moveToFirst()) {
            do {
                list.add(new Voucher(
                    cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(VOUCHER_USER_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(VOUCHER_CODE)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(VOUCHER_VALUE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(VOUCHER_STATUS))
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public Voucher getVoucherByCode(String code) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_VOUCHERS + " WHERE " + VOUCHER_CODE + " = ?", new String[]{code});
        Voucher voucher = null;
        if (cursor.moveToFirst()) {
            voucher = new Voucher(
                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)),
                cursor.getInt(cursor.getColumnIndexOrThrow(VOUCHER_USER_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(VOUCHER_CODE)),
                cursor.getDouble(cursor.getColumnIndexOrThrow(VOUCHER_VALUE)),
                cursor.getString(cursor.getColumnIndexOrThrow(VOUCHER_STATUS))
            );
        }
        cursor.close();
        return voucher;
    }

    public boolean markVoucherAsUsed(String code) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues val = new ContentValues();
        val.put(VOUCHER_STATUS, "used");
        int rows = db.update(TABLE_VOUCHERS, val, VOUCHER_CODE + " = ?", new String[]{code});
        return rows > 0;
    }

    public String cancelBooking(int bookingId, String reason) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        Cursor cursor = db.rawQuery("SELECT " + BOOK_USER_ID + ", " + BOOK_TOTAL + ", " + BOOK_CODE + " FROM " + TABLE_BOOKINGS + " WHERE " + KEY_ID + " = ?", new String[]{String.valueOf(bookingId)});
        int userId = -1;
        double total = 0.0;
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(0);
            total = cursor.getDouble(1);
        }
        cursor.close();
        
        if (userId == -1) return null;
        
        db.beginTransaction();
        try {
            ContentValues bookingVal = new ContentValues();
            bookingVal.put(BOOK_STATUS, "cancelled");
            int rowsAffected = db.update(TABLE_BOOKINGS, bookingVal, KEY_ID + " = ?", new String[]{String.valueOf(bookingId)});
            if (rowsAffected <= 0) {
                db.endTransaction();
                return null;
            }
            
            java.util.Random r = new java.util.Random();
            int rand = 100000 + r.nextInt(900000);
            String voucherCode = "REF-" + rand;
            
            ContentValues voucherVal = new ContentValues();
            voucherVal.put(VOUCHER_USER_ID, userId);
            voucherVal.put(VOUCHER_CODE, voucherCode);
            voucherVal.put(VOUCHER_VALUE, total);
            voucherVal.put(VOUCHER_STATUS, "active");
            long vId = db.insert(TABLE_VOUCHERS, null, voucherVal);
            if (vId == -1) {
                db.endTransaction();
                return null;
            }
            
            db.setTransactionSuccessful();
            db.endTransaction();
            return voucherCode;
        } catch (Exception e) {
            db.endTransaction();
            return null;
        }
    }

    public double getTotalRevenue() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(" + BOOK_TOTAL + ") FROM " + TABLE_BOOKINGS, null);
        double total = 0.0;
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        return total;
    }

    public List<Movie> getAllMovies() {
        List<Movie> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_MOVIES + " ORDER BY " + MOVIE_TITLE + " ASC", null);
        
        if (cursor.moveToFirst()) {
            do {
                list.add(new Movie(
                    cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(MOVIE_TITLE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(MOVIE_GENRE)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(MOVIE_DURATION)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(MOVIE_RATING)),
                    cursor.getString(cursor.getColumnIndexOrThrow(MOVIE_RELEASE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(MOVIE_SYNOPSIS)),
                    cursor.getString(cursor.getColumnIndexOrThrow(MOVIE_POSTER)),
                    cursor.getString(cursor.getColumnIndexOrThrow(MOVIE_COVER)),
                    cursor.getString(cursor.getColumnIndexOrThrow(MOVIE_STATUS)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(MOVIE_IS_HOT)) == 1
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public List<Movie> getHotMovies() {
        List<Movie> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_MOVIES + " WHERE " + MOVIE_IS_HOT + " = 1 ORDER BY " + MOVIE_TITLE + " ASC", null);
        
        if (cursor.moveToFirst()) {
            do {
                list.add(new Movie(
                    cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(MOVIE_TITLE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(MOVIE_GENRE)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(MOVIE_DURATION)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(MOVIE_RATING)),
                    cursor.getString(cursor.getColumnIndexOrThrow(MOVIE_RELEASE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(MOVIE_SYNOPSIS)),
                    cursor.getString(cursor.getColumnIndexOrThrow(MOVIE_POSTER)),
                    cursor.getString(cursor.getColumnIndexOrThrow(MOVIE_COVER)),
                    cursor.getString(cursor.getColumnIndexOrThrow(MOVIE_STATUS)),
                    true
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public boolean updateMovieHotStatus(int movieId, boolean isHot) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(MOVIE_IS_HOT, isHot ? 1 : 0);
        int rows = db.update(TABLE_MOVIES, values, KEY_ID + " = ?", new String[]{String.valueOf(movieId)});
        return rows > 0;
    }

    public boolean deleteMovie(int movieId) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Since TABLE_SHOWTIMES foreign key references TABLE_MOVIES with ON DELETE CASCADE,
        // deleting a movie will automatically clean up its associated showtimes in SQLite!
        int rows = db.delete(TABLE_MOVIES, KEY_ID + " = ?", new String[]{String.valueOf(movieId)});
        return rows > 0;
    }

    public List<Showtime> getAllShowtimes() {
        List<Showtime> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_SHOWTIMES + " ORDER BY " + SHOW_DATE + " DESC, " + SHOW_TIME + " ASC", null);
        
        if (cursor.moveToFirst()) {
            do {
                list.add(new Showtime(
                    cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(SHOW_MOVIE_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(SHOW_CINEMA)),
                    cursor.getString(cursor.getColumnIndexOrThrow(SHOW_DATE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(SHOW_TIME)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(SHOW_PRICE))
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public boolean addShowtime(int movieId, String cinema, String date, String time, double price) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues val = new ContentValues();
        val.put(SHOW_MOVIE_ID, movieId);
        val.put(SHOW_CINEMA, cinema);
        val.put(SHOW_DATE, date);
        val.put(SHOW_TIME, time);
        val.put(SHOW_PRICE, price);
        long result = db.insert(TABLE_SHOWTIMES, null, val);
        return result != -1;
    }

    public boolean deleteShowtime(int showtimeId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_SHOWTIMES, KEY_ID + " = ?", new String[]{String.valueOf(showtimeId)});
        return rows > 0;
    }

    public boolean updateMovie(int id, String title, String genre, int duration, double rating, 
                               String releaseDate, String synopsis, String poster, String cover, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(MOVIE_TITLE, title);
        values.put(MOVIE_GENRE, genre);
        values.put(MOVIE_DURATION, duration);
        values.put(MOVIE_RATING, rating);
        values.put(MOVIE_RELEASE, releaseDate);
        values.put(MOVIE_SYNOPSIS, synopsis);
        values.put(MOVIE_POSTER, poster);
        values.put(MOVIE_COVER, cover);
        values.put(MOVIE_STATUS, status);
        int rows = db.update(TABLE_MOVIES, values, KEY_ID + " = ?", new String[]{String.valueOf(id)});
        return rows > 0;
    }
}
