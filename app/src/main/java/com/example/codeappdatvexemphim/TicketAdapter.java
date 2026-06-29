package com.example.codeappdatvexemphim;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.TicketViewHolder> {

    private List<Booking> bookingList;
    private OnTicketClickListener listener;

    public interface OnTicketClickListener {
        void onTicketClick(Booking booking);
    }

    public TicketAdapter(List<Booking> bookingList, OnTicketClickListener listener) {
        this.bookingList = bookingList;
        this.listener = listener;
    }

    public void updateData(List<Booking> newList) {
        this.bookingList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ticket, parent, false);
        return new TicketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
        Booking booking = bookingList.get(position);
        holder.txtMovieTitle.setText(booking.getMovieTitle());
        holder.txtCinema.setText(booking.getCinema());
        holder.txtDateTime.setText(booking.getShowDate() + " | " + booking.getShowTime());
        holder.txtSeats.setText("Ghế: " + booking.getSeats());

        // Format currency
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        holder.txtPrice.setText(currencyFormat.format(booking.getTotalPrice()));
        
        if ("cancelled".equals(booking.getStatus())) {
            holder.itemView.setAlpha(0.55f);
            holder.txtTicketCode.setText("Mã vé: " + booking.getTicketCode() + " (ĐÃ HỦY)");
            holder.txtTicketCode.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_red_light));
        } else {
            holder.itemView.setAlpha(1.0f);
            holder.txtTicketCode.setText("Mã vé: " + booking.getTicketCode());
            // Restore default gray color for active tickets
            holder.txtTicketCode.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.darker_gray));
        }

        Glide.with(holder.itemView.getContext())
                .load(DatabaseHelper.getGlideUrl(booking.getMoviePoster()))
                .placeholder(R.drawable.ic_placeholder_movie)
                .error(R.drawable.ic_placeholder_movie)
                .into(holder.imgPoster);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTicketClick(booking);
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookingList != null ? bookingList.size() : 0;
    }

    static class TicketViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPoster;
        TextView txtMovieTitle;
        TextView txtCinema;
        TextView txtDateTime;
        TextView txtSeats;
        TextView txtPrice;
        TextView txtTicketCode;

        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPoster = itemView.findViewById(R.id.img_poster);
            txtMovieTitle = itemView.findViewById(R.id.txt_movie_title);
            txtCinema = itemView.findViewById(R.id.txt_cinema);
            txtDateTime = itemView.findViewById(R.id.txt_date_time);
            txtSeats = itemView.findViewById(R.id.txt_seats);
            txtPrice = itemView.findViewById(R.id.txt_price);
            txtTicketCode = itemView.findViewById(R.id.txt_ticket_code);
        }
    }
}
