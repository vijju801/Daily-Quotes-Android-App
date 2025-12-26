package com.example.myapplication1;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.transition.platform.MaterialSharedAxis;

import java.util.List;

public class FavoritesActivity extends AppCompatActivity {

    private RecyclerView favoritesRecyclerView;
    private FavoriteQuotesAdapter favoriteQuotesAdapter;
    private FavoriteQuoteDao favoriteQuoteDao;
    private TextView emptyFavoritesTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SettingsActivity.applyTheme(this);
        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);

        super.onCreate(savedInstanceState);

        MaterialSharedAxis enterTransition = new MaterialSharedAxis(MaterialSharedAxis.X, true);
        getWindow().setEnterTransition(enterTransition);

        MaterialSharedAxis returnTransition = new MaterialSharedAxis(MaterialSharedAxis.X, false);
        getWindow().setReturnTransition(returnTransition);

        setContentView(R.layout.activity_favorites);

        MaterialToolbar toolbar = findViewById(R.id.toolbar_favorites);
        toolbar.setNavigationOnClickListener(v -> finish());

        favoritesRecyclerView = findViewById(R.id.favorites_recycler_view);
        emptyFavoritesTextView = findViewById(R.id.empty_favorites_text);

        favoriteQuoteDao = AppDatabase.getDatabase(getApplicationContext()).favoriteQuoteDao();

        favoritesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        favoriteQuotesAdapter = new FavoriteQuotesAdapter(this, favoriteQuoteDao);
        favoritesRecyclerView.setAdapter(favoriteQuotesAdapter);

        favoriteQuoteDao.getAllFavoriteQuotes().observe(this, new Observer<List<FavoriteQuote>>() {
            @Override
            public void onChanged(List<FavoriteQuote> favoriteQuotes) {
                if (favoriteQuotes == null || favoriteQuotes.isEmpty()) {
                    emptyFavoritesTextView.setVisibility(View.VISIBLE);
                    favoritesRecyclerView.setVisibility(View.GONE);
                } else {
                    emptyFavoritesTextView.setVisibility(View.GONE);
                    favoritesRecyclerView.setVisibility(View.VISIBLE);
                    favoriteQuotesAdapter.setFavoriteQuotes(favoriteQuotes);
                }
            }
        });
    }
}
