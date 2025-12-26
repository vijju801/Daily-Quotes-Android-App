package com.example.myapplication1;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface FavoriteQuoteDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(FavoriteQuote favoriteQuote);

    @Delete
    void delete(FavoriteQuote favoriteQuote);

    @Query("SELECT * FROM favorite_quotes ORDER BY quote_category, quote_text ASC")
    LiveData<List<FavoriteQuote>> getAllFavoriteQuotes();

    // To check if a specific quote is already a favorite
    @Query("SELECT * FROM favorite_quotes WHERE quote_text = :text AND quote_category = :category LIMIT 1")
    FavoriteQuote getFavoriteQuoteByTextAndCategory(String text, String category);

    // Alternative: query to get LiveData for a single quote, to observe its favorite status
    @Query("SELECT * FROM favorite_quotes WHERE quote_text = :text AND quote_category = :category LIMIT 1")
    LiveData<FavoriteQuote> getFavoriteQuoteLiveData(String text, String category);

}
