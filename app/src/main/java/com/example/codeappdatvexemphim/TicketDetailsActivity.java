package com.example.codeappdatvexemphim;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TicketDetailsActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private androidx.appcompat.widget.AppCompatButton btnHome;
    private TextView txtMovieTitle, txtGenre, txtCinema, txtShowtime, txtDate, txtSeats, txtPaymentMethod, txtPrice, txtTicketCode;
    private BarcodeView barcodeView;

    // Cancellation views
    private android.view.View layoutCancelledInfo;
    private TextView txtCancellationReason;
    private androidx.appcompat.widget.AppCompatButton btnCancelTicket;

    private DatabaseHelper dbHelper;
    private int bookingId = -1;
    private double ticketPrice = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_details);

        dbHelper = DatabaseHelper.getInstance(this);

        initViews();
        displayTicketDetails();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        btnHome = findViewById(R.id.btn_home);
        txtMovieTitle = findViewById(R.id.txt_movie_title);
        txtGenre = findViewById(R.id.txt_genre);
        txtCinema = findViewById(R.id.txt_cinema);
        txtShowtime = findViewById(R.id.txt_showtime);
        txtDate = findViewById(R.id.txt_date);
        txtSeats = findViewById(R.id.txt_seats);
        txtPaymentMethod = findViewById(R.id.txt_payment_method);
        txtPrice = findViewById(R.id.txt_price);
        txtTicketCode = findViewById(R.id.txt_ticket_code);
        barcodeView = findViewById(R.id.barcode_view);

        // Cancellation views
        layoutCancelledInfo = findViewById(R.id.layout_cancelled_info);
        txtCancellationReason = findViewById(R.id.txt_cancellation_reason);
        btnCancelTicket = findViewById(R.id.btn_cancel_ticket);

        btnBack.setOnClickListener(v -> finish());
        btnCancelTicket.setOnClickListener(v -> showCancelReasonDialog());
    }

    private void displayTicketDetails() {
        // Retrieve values from Intent
        String ticketCode = getIntent().getStringExtra("ticket_code");
        String movieTitle = getIntent().getStringExtra("movie_title");
        String movieGenre = getIntent().getStringExtra("movie_genre");
        String cinema = getIntent().getStringExtra("cinema");
        String date = getIntent().getStringExtra("date");
        String time = getIntent().getStringExtra("time");
        String seats = getIntent().getStringExtra("seats");
        String paymentMethod = getIntent().getStringExtra("payment_method");
        double totalPrice = getIntent().getDoubleExtra("total_price", 0.0);
        boolean fromBooking = getIntent().getBooleanExtra("from_booking", false);

        ticketPrice = totalPrice;

        // Toggle buttons based on context
        if (fromBooking) {
            btnBack.setVisibility(android.view.View.GONE);
            btnHome.setVisibility(android.view.View.VISIBLE);
            btnHome.setOnClickListener(v -> {
                android.content.Intent homeIntent = new android.content.Intent(TicketDetailsActivity.this, MainActivity.class);
                homeIntent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP | android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(homeIntent);
                finish();
            });
        } else {
            btnBack.setVisibility(android.view.View.VISIBLE);
            btnHome.setVisibility(android.view.View.GONE);
        }

        // Populate fields
        txtMovieTitle.setText(movieTitle);
        txtGenre.setText(movieGenre);
        txtCinema.setText(cinema);
        txtShowtime.setText(time);
        txtDate.setText(date);
        txtSeats.setText(seats);
        txtPaymentMethod.setText(paymentMethod);
        txtTicketCode.setText(ticketCode);

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        txtPrice.setText(currencyFormat.format(totalPrice));

        // Pass ticket code to dynamic custom barcode view
        if (barcodeView != null && ticketCode != null) {
            barcodeView.setTicketCode(ticketCode);
        }

        // Load dynamic status from Database
        if (ticketCode != null) {
            Booking booking = dbHelper.getBookingByCode(ticketCode);
            if (booking != null) {
                bookingId = booking.getId();
                
                // If already cancelled
                if ("cancelled".equals(booking.getStatus())) {
                    if (barcodeView != null) barcodeView.setVisibility(android.view.View.GONE);
                    btnCancelTicket.setVisibility(android.view.View.GONE);
                    layoutCancelledInfo.setVisibility(android.view.View.VISIBLE);
                    txtCancellationReason.setText("Lý do: Khách hàng yêu cầu hủy vé (Hoàn voucher)");
                } else {
                    // Check if showtime has already passed
                    try {
                        String showDateTimeStr = booking.getShowDate() + " " + booking.getShowTime(); // e.g. "2026-06-12 19:30"
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                        Date showDateTime = format.parse(showDateTimeStr);
                        if (showDateTime != null && showDateTime.before(new Date())) {
                            // Showtime has passed, hide cancel button
                            btnCancelTicket.setVisibility(android.view.View.GONE);
                        } else {
                            btnCancelTicket.setVisibility(android.view.View.VISIBLE);
                        }
                    } catch (Exception e) {
                        btnCancelTicket.setVisibility(android.view.View.VISIBLE);
                    }
                    layoutCancelledInfo.setVisibility(android.view.View.GONE);
                }
            } else {
                btnCancelTicket.setVisibility(android.view.View.GONE);
            }
        } else {
            btnCancelTicket.setVisibility(android.view.View.GONE);
        }
    }

    private void showCancelReasonDialog() {
        if (bookingId == -1) return;

        String[] reasons = {
            "Thay đổi lịch trình cá nhân",
            "Đặt nhầm suất chiếu / rạp chiếu",
            "Muốn chọn vị trí ghế khác",
            "Lý do khác"
        };

        final int[] selectedIndex = {0};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn lý do hủy vé")
               .setSingleChoiceItems(reasons, 0, (dialog, which) -> selectedIndex[0] = which)
               .setPositiveButton("Xác Nhận Hủy", (dialog, which) -> {
                   String reason = reasons[selectedIndex[0]];
                   performCancellation(reason);
               })
               .setNegativeButton("Hủy Bỏ", null)
               .show();
    }

    private void performCancellation(String reason) {
        String voucherCode = dbHelper.cancelBooking(bookingId, reason);
        if (voucherCode != null) {
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            String valueStr = currencyFormat.format(ticketPrice);

            // Show success dialog with new voucher code
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Hủy Vé Thành Công")
                   .setMessage("Vé của bạn đã được hủy thành công và ghế ngồi đã được giải phóng.\n\n" +
                               "Mã Voucher hoàn tiền của bạn là:\n" +
                               "👉 " + voucherCode + " 👈\n\n" +
                               "Trị giá: " + valueStr + "\n" +
                               "Vui lòng lưu lại mã này để sử dụng thanh toán cho lần đặt vé tiếp theo.")
                   .setPositiveButton("Đồng Ý", (dialog, which) -> {
                       // Refresh status
                       displayTicketDetails();
                   })
                   .setCancelable(false)
                   .show();
        } else {
            Toast.makeText(this, "Hủy vé thất bại! Vui lòng thử lại sau.", Toast.LENGTH_SHORT).show();
        }
    }
}
