package com.example.codeappdatvexemphim;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class TicketConfirmationActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView txtMovieTitle, txtCinema, txtDateTime, txtSeats;
    private TextView txtTicketBreakdown, txtTicketSubtotal, txtTotalPrice;
    private RadioGroup rgPaymentMethods;
    private AppCompatButton btnConfirmPayment;

    // Voucher views
    private android.widget.EditText edtVoucherCode;
    private androidx.appcompat.widget.AppCompatButton btnApplyVoucher;
    private android.view.View layoutVoucherDiscount;
    private TextView txtVoucherDiscount;
    private TextView txtVoucherStatus;

    private DatabaseHelper dbHelper;
    private SharedPreferences sharedPreferences;

    private int movieId;
    private int showtimeId;
    private String selectedSeats;
    private double totalPrice; // acts as final price
    private double originalPrice;
    private double voucherDiscount = 0.0;
    private String appliedVoucherCode = null;

    private Movie movie;
    private Showtime showtime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_ticket_confirmation);

            dbHelper = DatabaseHelper.getInstance(this);
            sharedPreferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE);

            // Retrieve Intent data
            movieId = getIntent().getIntExtra("movie_id", -1);
            showtimeId = getIntent().getIntExtra("showtime_id", -1);
            selectedSeats = getIntent().getStringExtra("selected_seats");
            totalPrice = getIntent().getDoubleExtra("total_price", 0.0);
            originalPrice = totalPrice;

            android.util.Log.d("TicketConfirm", "Received data: movie_id=" + movieId + 
                    ", showtime_id=" + showtimeId + ", selected_seats=" + selectedSeats + 
                    ", total_price=" + totalPrice);

            movie = dbHelper.getMovieById(movieId);
            showtime = dbHelper.getShowtimeById(showtimeId);

            if (movie == null || showtime == null || selectedSeats == null) {
                String errorMsg = "Lỗi nạp hóa đơn: " +
                        (movie == null ? "Movie is null (id=" + movieId + ") " : "") +
                        (showtime == null ? "Showtime is null (id=" + showtimeId + ") " : "") +
                        (selectedSeats == null ? "SelectedSeats is null" : "");
                android.util.Log.e("TicketConfirm", errorMsg);
                Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            initViews();
            displayInvoice();
        } catch (Exception e) {
            android.util.Log.e("TicketConfirm", "Crash in onCreate", e);
            Toast.makeText(this, "Crash in TicketConfirmationActivity: " + e.toString(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        txtMovieTitle = findViewById(R.id.txt_movie_title);
        txtCinema = findViewById(R.id.txt_cinema);
        txtDateTime = findViewById(R.id.txt_date_time);
        txtSeats = findViewById(R.id.txt_seats);
        txtTicketBreakdown = findViewById(R.id.txt_ticket_breakdown);
        txtTicketSubtotal = findViewById(R.id.txt_ticket_subtotal);
        txtTotalPrice = findViewById(R.id.txt_total_price);
        rgPaymentMethods = findViewById(R.id.rg_payment_methods);
        btnConfirmPayment = findViewById(R.id.btn_confirm_payment);

        // Voucher views
        edtVoucherCode = findViewById(R.id.edt_voucher_code);
        btnApplyVoucher = findViewById(R.id.btn_apply_voucher);
        layoutVoucherDiscount = findViewById(R.id.layout_voucher_discount);
        txtVoucherDiscount = findViewById(R.id.txt_voucher_discount);
        txtVoucherStatus = findViewById(R.id.txt_voucher_status);

        // Make voucher code input field non-editable and act as a selector
        edtVoucherCode.setFocusable(false);
        edtVoucherCode.setClickable(true);
        btnApplyVoucher.setText("Chọn Mã");

        btnBack.setOnClickListener(v -> finish());
        btnConfirmPayment.setOnClickListener(v -> handleBookingConfirmation());
        btnApplyVoucher.setOnClickListener(v -> showVoucherSelectionDialog());
        edtVoucherCode.setOnClickListener(v -> showVoucherSelectionDialog());
    }

    private void displayInvoice() {
        txtMovieTitle.setText(movie.getTitle());
        txtCinema.setText(showtime.getCinema());
        txtDateTime.setText(showtime.getDate() + " | " + showtime.getTime());
        txtSeats.setText("Ghế: " + selectedSeats);

        // Split seats to get count
        int seatCount = selectedSeats.split(",").length;
        txtTicketBreakdown.setText("Vé tiêu chuẩn (x" + seatCount + ")");

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        txtTicketSubtotal.setText(currencyFormat.format(originalPrice));
        txtTotalPrice.setText(currencyFormat.format(originalPrice));
    }

    private void showVoucherSelectionDialog() {
        int userId = sharedPreferences.getInt("user_id", -1);
        if (userId == -1) {
            Toast.makeText(this, "Phiên đăng nhập hết hạn! Vui lòng đăng nhập lại.", Toast.LENGTH_SHORT).show();
            return;
        }

        java.util.List<Voucher> activeVouchers = dbHelper.getActiveVouchersForUser(userId);
        if (activeVouchers == null || activeVouchers.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle("Chọn Voucher")
                    .setMessage("Tài khoản của bạn hiện không có mã voucher hoàn tiền nào khả dụng.")
                    .setPositiveButton("Đóng", null)
                    .show();
            return;
        }

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        String[] voucherOptions = new String[activeVouchers.size() + 1];
        for (int i = 0; i < activeVouchers.size(); i++) {
            Voucher v = activeVouchers.get(i);
            voucherOptions[i] = v.getCode() + " (Giảm: " + currencyFormat.format(v.getValue()) + ")";
        }
        voucherOptions[activeVouchers.size()] = "Không áp dụng voucher";

        new AlertDialog.Builder(this)
                .setTitle("Chọn Voucher Hoàn Vé")
                .setItems(voucherOptions, (dialog, which) -> {
                    if (which == activeVouchers.size()) {
                        // User selected "No voucher"
                        edtVoucherCode.setText("");
                        resetVoucherDeduction();
                        txtVoucherStatus.setVisibility(android.view.View.GONE);
                    } else {
                        Voucher selected = activeVouchers.get(which);
                        edtVoucherCode.setText(selected.getCode());
                        applySelectedVoucher(selected);
                    }
                })
                .show();
    }

    private void applySelectedVoucher(Voucher voucher) {
        appliedVoucherCode = voucher.getCode();
        voucherDiscount = voucher.getValue();
        double displayDiscount = Math.min(originalPrice, voucherDiscount);
        double finalPrice = Math.max(0.0, originalPrice - displayDiscount);

        layoutVoucherDiscount.setVisibility(android.view.View.VISIBLE);
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        txtVoucherDiscount.setText("-" + currencyFormat.format(displayDiscount));
        txtTotalPrice.setText(currencyFormat.format(finalPrice));

        txtVoucherStatus.setVisibility(android.view.View.VISIBLE);
        txtVoucherStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        txtVoucherStatus.setText("Đã áp dụng: " + voucher.getCode() + " (-" + currencyFormat.format(displayDiscount) + ")");
    }

    private void resetVoucherDeduction() {
        appliedVoucherCode = null;
        voucherDiscount = 0.0;
        layoutVoucherDiscount.setVisibility(android.view.View.GONE);
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        txtTotalPrice.setText(currencyFormat.format(originalPrice));
    }

    private void handleBookingConfirmation() {
        int userId = sharedPreferences.getInt("user_id", -1);
        if (userId == -1) {
            Toast.makeText(this, "Hết phiên đăng nhập! Vui lòng đăng nhập lại.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(TicketConfirmationActivity.this, LoginActivity.class));
            finishAffinity();
            return;
        }

        // Determine Payment Method
        int checkedId = rgPaymentMethods.getCheckedRadioButtonId();
        String method = "Momo";
        if (checkedId == R.id.rb_zalopay) {
            method = "ZaloPay";
        } else if (checkedId == R.id.rb_card) {
            method = "ATM/Banking";
        }
        final String paymentMethod = method;

        // Generate Ticket Code (e.g. BKG-839120)
        Random r = new Random();
        int randomNum = 100000 + r.nextInt(900000);
        String ticketCode = "BKG-" + randomNum;

        // Current Timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        String bookingTime = sdf.format(new Date());

        // Price to pay
        double finalPrice = Math.max(0.0, originalPrice - voucherDiscount);

        // Insert into database
        boolean success = dbHelper.insertBooking(userId, showtimeId, selectedSeats, finalPrice, bookingTime, paymentMethod, ticketCode);

        if (success) {
            // Handle voucher validation and surplus refunds
            if (appliedVoucherCode != null) {
                dbHelper.markVoucherAsUsed(appliedVoucherCode);

                if (voucherDiscount > originalPrice) {
                    double surplus = voucherDiscount - originalPrice;
                    int rand = 100000 + r.nextInt(900000);
                    String newVoucherCode = "REF-REM-" + rand;
                    
                    dbHelper.insertVoucher(userId, newVoucherCode, surplus);

                    NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
                    String surplusStr = currencyFormat.format(surplus);

                    // Show surplus voucher popup first
                    new AlertDialog.Builder(this)
                            .setTitle("Nhận Voucher Dư Thừa")
                            .setMessage("Mã Voucher hoàn tiền của bạn có giá trị lớn hơn hóa đơn vé.\n\n" +
                                       "Hệ thống đã tự động tạo cho bạn một Mã Voucher Mới chứa số dư thặng dư:\n" +
                                       "👉 " + newVoucherCode + " 👈\n\n" +
                                       "Số dư còn lại: " + surplusStr + "\n" +
                                       "Vui lòng lưu lại mã này để sử dụng thanh toán cho lần mua vé sau.")
                            .setPositiveButton("Đồng Ý", (dialog, which) -> navigateToTicketDetails(ticketCode, finalPrice, paymentMethod))
                            .setCancelable(false)
                            .show();
                    return; // Prevent immediate navigation
                }
            }

            Toast.makeText(this, "Đặt vé thành công!", Toast.LENGTH_LONG).show();
            navigateToTicketDetails(ticketCode, finalPrice, paymentMethod);
        } else {
            Toast.makeText(this, "Đặt vé thất bại! Lỗi cơ sở dữ liệu.", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToTicketDetails(String ticketCode, double finalPrice, String paymentMethod) {
        // Open TicketDetailsActivity
        Intent intent = new Intent(TicketConfirmationActivity.this, TicketDetailsActivity.class);
        intent.putExtra("ticket_code", ticketCode);
        intent.putExtra("movie_title", movie.getTitle());
        intent.putExtra("movie_genre", movie.getGenre());
        intent.putExtra("cinema", showtime.getCinema());
        intent.putExtra("date", showtime.getDate());
        intent.putExtra("time", showtime.getTime());
        intent.putExtra("seats", selectedSeats);
        intent.putExtra("payment_method", appliedVoucherCode != null ? "Voucher + " + paymentMethod : paymentMethod);
        intent.putExtra("total_price", finalPrice);
        intent.putExtra("from_booking", true);
        
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
