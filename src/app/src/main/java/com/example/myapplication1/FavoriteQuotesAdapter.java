package com.example.myapplication1;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class FavoriteQuotesAdapter extends RecyclerView.Adapter<FavoriteQuotesAdapter.FavoriteQuoteViewHolder> {

    private List<FavoriteQuote> favoriteQuotesList = new ArrayList<>();
    private final Context context;
    private final FavoriteQuoteDao favoriteQuoteDao;

    public FavoriteQuotesAdapter(Context context, FavoriteQuoteDao favoriteQuoteDao) {
        this.context = context;
        this.favoriteQuoteDao = favoriteQuoteDao;
    }

    @NonNull
    @Override
    public FavoriteQuoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.favorite_quote_item, parent, false);
        return new FavoriteQuoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteQuoteViewHolder holder, int position) {
        FavoriteQuote currentQuote = favoriteQuotesList.get(position);
        holder.quoteTextView.setText(currentQuote.text);
        holder.categoryTextView.setText("Category: " + currentQuote.category);

        holder.deleteButton.setOnClickListener(v -> {
            AppDatabase.databaseWriteExecutor.execute(() -> {
                favoriteQuoteDao.delete(currentQuote);
                // The LiveData in FavoritesActivity will handle UI updates.
                // We can show a Toast for feedback if needed, ensuring it runs on the UI thread.
                // For example, by using ((Activity)context).runOnUiThread(...) or Handler.
            });
            Toast.makeText(context, "Removed from favorites", Toast.LENGTH_SHORT).show();

        });
    }

    @Override
    public int getItemCount() {
        return favoriteQuotesList.size();
    }

    public void setFavoriteQuotes(List<FavoriteQuote> favoriteQuotes) {
        this.favoriteQuotesList = favoriteQuotes;
        notifyDataSetChanged(); // Or use DiffUtil for better performance
    }

    static class FavoriteQuoteViewHolder extends RecyclerView.ViewHolder {
        TextView quoteTextView;
        TextView categoryTextView;
        ImageButton deleteButton;

        public FavoriteQuoteViewHolder(@NonNull View itemView) {
            super(itemView);
            quoteTextView = itemView.findViewById(R.id.favorite_quote_text);
            categoryTextView = itemView.findViewById(R.id.favorite_quote_category);
            deleteButton = itemView.findViewById(R.id.delete_favorite_button);
        }
    }
}
