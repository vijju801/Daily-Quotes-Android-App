package com.example.myapplication1;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

@Entity(tableName = "favorite_quotes")
public class FavoriteQuote {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "quote_text")
    public String text;

    @ColumnInfo(name = "quote_category")
    public String category;

    // Constructor, getters, and setters can be added if needed,
    // but Room can work with public fields.
    // For simplicity, we'll use public fields for now.

    public FavoriteQuote(String text, String category) {
        this.text = text;
        this.category = category;
    }

    // It's good practice to have a no-arg constructor if you have other constructors,
    // though Room can handle it if there's only one.
    // However, if we make fields public, Room might not need this explicitly
    // if it can directly access them. For robust-ness, especially if fields become private:
    // public FavoriteQuote() {}

    // Getters are useful if fields are private
    // public int getId() { return id; }
    // public String getText() { return text; }
    // public String getCategory() { return category; }
    // public void setId(int id) { this.id = id; } // Usually not needed for autogen PK
}
