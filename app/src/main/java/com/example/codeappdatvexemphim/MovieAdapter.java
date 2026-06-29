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
import java.util.Locale;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    private List<Movie> movieList;
    private boolean isHorizontal;
    private OnMovieClickListener listener;

    public interface OnMovieClickListener {
        void onMovieClick(Movie movie);
    }

    public MovieAdapter(List<Movie> movieList, boolean isHorizontal, OnMovieClickListener listener) {
        this.movieList = movieList;
        this.isHorizontal = isHorizontal;
        this.listener = listener;
    }

    public void updateData(List<Movie> newList) {
        this.movieList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId = isHorizontal ? R.layout.item_movie_horizontal : R.layout.item_movie_grid;
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Movie movie = movieList.get(position);
        holder.txtTitle.setText(movie.getTitle());
        holder.txtGenre.setText(movie.getGenre());
        
        if (holder.txtRating != null) {
            holder.txtRating.setText(String.format(Locale.getDefault(), "%.1f", movie.getRating()));
        }

        // Load image using Glide
        Glide.with(holder.itemView.getContext())
                .load(DatabaseHelper.getGlideUrl(movie.getPosterUri()))
                .placeholder(R.drawable.ic_placeholder_movie)
                .error(R.drawable.ic_placeholder_movie)
                .into(holder.imgPoster);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMovieClick(movie);
            }
        });
    }

    @Override
    public int getItemCount() {
        return movieList != null ? movieList.size() : 0;
    }

    static class MovieViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPoster;
        TextView txtTitle;
        TextView txtGenre;
        TextView txtRating;

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPoster = itemView.findViewById(R.id.img_poster);
            txtTitle = itemView.findViewById(R.id.txt_title);
            txtGenre = itemView.findViewById(R.id.txt_genre);
            txtRating = itemView.findViewById(R.id.txt_rating);
        }
    }
}
