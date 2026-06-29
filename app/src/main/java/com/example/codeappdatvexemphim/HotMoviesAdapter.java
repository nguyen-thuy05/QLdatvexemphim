package com.example.codeappdatvexemphim;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class HotMoviesAdapter extends RecyclerView.Adapter<HotMoviesAdapter.BannerViewHolder> {

    private final List<Movie> movies;
    private final Context context;

    public HotMoviesAdapter(Context context, List<Movie> movies) {
        this.context = context;
        this.movies = movies;
    }

    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_hot_movie_banner, parent, false);
        return new BannerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        Movie movie = movies.get(position);
        holder.txtTitle.setText(movie.getTitle());

        // Use cover uri if available, otherwise poster uri
        String imgUrl = movie.getCoverUri();
        if (imgUrl == null || imgUrl.trim().isEmpty()) {
            imgUrl = movie.getPosterUri();
        }

        Glide.with(context)
                .load(DatabaseHelper.getGlideUrl(imgUrl))
                .placeholder(R.drawable.ic_placeholder_movie)
                .error(R.drawable.ic_placeholder_movie)
                .into(holder.imgBackdrop);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MovieDetailsActivity.class);
            intent.putExtra("movie_id", movie.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    static class BannerViewHolder extends RecyclerView.ViewHolder {
        ImageView imgBackdrop;
        TextView txtTitle;

        public BannerViewHolder(@NonNull View itemView) {
            super(itemView);
            imgBackdrop = itemView.findViewById(R.id.img_banner_backdrop);
            txtTitle = itemView.findViewById(R.id.txt_banner_title);
        }
    }
}
