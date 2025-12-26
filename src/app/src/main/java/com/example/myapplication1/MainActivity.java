package com.example.myapplication1;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences; 
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.transition.platform.MaterialSharedAxis;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private RecyclerView categoryRecyclerView;
    private CategoryAdapter categoryAdapter;
    private List<Category> categoryList;
    private Button viewFavoritesButton;
    private MaterialToolbar toolbarMain;
    private TextView qodText;
    private TextView qodAuthor;
    private MaterialButton explainButton;

    private static final String[][] sampleQuotes = {
            {"The only way to do great work is to love what you do.", "Steve Jobs"},
            {"Strive not to be a success, but rather to be of value.", "Albert Einstein"},
            {"The mind is everything. What you think you become.", "Buddha"},
            {"Your time is limited, so don’t waste it living someone else’s life.", "Steve Jobs"},
            {"Life is what happens when you’re busy making other plans.", "John Lennon"}
    };
    private Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SettingsActivity.applyTheme(this);
        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MaterialSharedAxis exitTransition = new MaterialSharedAxis(MaterialSharedAxis.X, true); 
        getWindow().setExitTransition(exitTransition);
        getWindow().setReenterTransition(exitTransition); 

        toolbarMain = findViewById(R.id.toolbar_main);
        toolbarMain.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_settings) {
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
                return true;
            }
            return false;
        });

        qodText = findViewById(R.id.qod_text);
        qodAuthor = findViewById(R.id.qod_author);
        explainButton = findViewById(R.id.explain_button);

        categoryRecyclerView = findViewById(R.id.categoryRecyclerView);
        viewFavoritesButton = findViewById(R.id.view_favorites_button);

        displayQuoteOfTheDay();

        categoryList = new ArrayList<>();
        categoryList.add(new Category("Motivation", R.drawable.motivation));
        categoryList.add(new Category("Love", R.drawable.love));
        categoryList.add(new Category("Cool", R.drawable.cool));
        categoryList.add(new Category("Fitness", R.drawable.fitness));
        categoryList.add(new Category("Inspiration", R.drawable.inspiration));
        categoryList.add(new Category("Friendship", R.drawable.friendship));
        categoryList.add(new Category("Wisdom", R.drawable.wisdom));
        categoryList.add(new Category("Humor", R.drawable.humor));
        categoryList.add(new Category("Science", R.drawable.science));
        categoryList.add(new Category("Art", R.drawable.art));
        categoryList.add(new Category("Technology", R.drawable.technology));
        categoryList.add(new Category("Nature", R.drawable.natura));
        categoryList.add(new Category("Dreams", R.drawable.dreams));
        categoryList.add(new Category("Future", R.drawable.future));

        categoryAdapter = new CategoryAdapter(this, categoryList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        categoryRecyclerView.setLayoutManager(layoutManager);
        categoryRecyclerView.setAdapter(categoryAdapter);

        viewFavoritesButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FavoritesActivity.class);
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
        });

        explainButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, QuoteExplanationActivity.class);
            intent.putExtra("quote", qodText.getText().toString());
            intent.putExtra("author", qodAuthor.getText().toString().substring(2)); 
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
        });
    }

    private void displayQuoteOfTheDay() {
        if (sampleQuotes.length > 0) {
            int randomIndex = random.nextInt(sampleQuotes.length);
            qodText.setText(sampleQuotes[randomIndex][0]);
            qodAuthor.setText(String.format("- %s", sampleQuotes[randomIndex][1]));
        } else {
            qodText.setText("No quote available right now.");
            qodAuthor.setText("");
        }
    }
}
