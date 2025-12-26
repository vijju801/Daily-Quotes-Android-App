package com.example.myapplication1;

import android.app.ActivityOptions;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.google.android.material.appbar.MaterialToolbar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class QuotesActivity extends AppCompatActivity {

    private static final String TAG = "QuotesActivity";
    private static final int QUOTE_BATCH_SIZE = 5;

    private MaterialToolbar toolbarQuotes;
    private TextView categoryTitleTextView;
    private TextView quoteTextArea;
    private ImageButton copyButton;
    private ImageButton shareButton;
    private ImageButton favoriteButton;
    private ImageButton explainButton;
    private Button prevButton;
    private Button nextButton;

    private String currentCategory;
    private String currentQuoteText;
    private Map<String, String[]> quotesMap = new HashMap<>();
    private Map<String, Integer> currentQuoteIndices = new HashMap<>();

    private boolean isFetching = false;
    private String[] preFetchedQuotes = null;
    private boolean isPreFetching = false;
    private String preFetchingCategory = null;

    private FavoriteQuoteDao favoriteQuoteDao;
    private LiveData<FavoriteQuote> currentFavoriteStatusLiveData;
    private Observer<FavoriteQuote> favoriteStatusObserver;

    private static final String GEMINI_API_KEY = "AIzaSyAakiSSSKRIHR4q2BQ1IiOmKKqlqEaaPOE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SettingsActivity.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quotes);

        toolbarQuotes = findViewById(R.id.toolbar_quotes);
        toolbarQuotes.setNavigationOnClickListener(v -> finish());

        categoryTitleTextView = findViewById(R.id.quotes_category_title);
        quoteTextArea = findViewById(R.id.quote_text_area);
        copyButton = findViewById(R.id.copy_button);
        shareButton = findViewById(R.id.share_button);
        favoriteButton = findViewById(R.id.favorite_button);
        explainButton = findViewById(R.id.explain_button_quotes);
        prevButton = findViewById(R.id.prev_button);
        nextButton = findViewById(R.id.next_button);

        favoriteQuoteDao = AppDatabase.getDatabase(getApplicationContext()).favoriteQuoteDao();

        Intent intent = getIntent();
        currentCategory = "Unknown Category";

        if (intent != null && intent.hasExtra("categoryName")) {
            String receivedCategoryName = intent.getStringExtra("categoryName");
            if (!TextUtils.isEmpty(receivedCategoryName)) {
                currentCategory = receivedCategoryName;
            }
        }

        categoryTitleTextView.setText(currentCategory);
        toolbarQuotes.setTitle(currentCategory);

        initializeQuotes();
        currentQuoteIndices.putIfAbsent(currentCategory, 0);

        if (!"Unknown Category".equals(currentCategory) && !GEMINI_API_KEY.equals("YOUR_API_KEY_HERE") && !GEMINI_API_KEY.isEmpty()) {
            fetchFreshQuotesForCategory(currentCategory, false);
        } else {
            if (GEMINI_API_KEY.equals("YOUR_API_KEY_HERE") || GEMINI_API_KEY.isEmpty()) {
                Log.w(TAG, "Gemini API key is not set. Using local quotes.");
            }
            displayCurrentQuoteForCategory();
        }

        favoriteStatusObserver = favoriteQuote -> {
            if (favoriteQuote != null) {
                favoriteButton.setImageResource(R.drawable.ic_futuristic_star_filled);
            } else {
                favoriteButton.setImageResource(R.drawable.ic_futuristic_star_outline);
            }
        };

        prevButton.setOnClickListener(v -> {
            if (isFetching) return;
            int currentIndex = currentQuoteIndices.getOrDefault(currentCategory, 0);
            if (currentIndex > 0) {
                currentQuoteIndices.put(currentCategory, currentIndex - 1);
                displayCurrentQuoteForCategory();
            } else {
                Toast.makeText(QuotesActivity.this, "Already at the first quote.", Toast.LENGTH_SHORT).show();
            }
        });

        nextButton.setOnClickListener(v -> {
            String[] currentBatch = quotesMap.get(currentCategory);
            int currentIndex = currentQuoteIndices.getOrDefault(currentCategory, 0);
            int newIndex = currentIndex + 1;

            if (currentBatch != null && newIndex < currentBatch.length) {
                currentQuoteIndices.put(currentCategory, newIndex);
                displayCurrentQuoteForCategory();
                if (newIndex >= currentBatch.length - 2) {
                    preFetchNextBatchQuotes(currentCategory);
                }
            } else {
                if (preFetchedQuotes != null) {
                    quotesMap.put(currentCategory, preFetchedQuotes);
                    currentQuoteIndices.put(currentCategory, 0);
                    preFetchedQuotes = null;
                    displayCurrentQuoteForCategory();
                } else if (!isFetching) {
                    fetchFreshQuotesForCategory(currentCategory, true);
                } 
            }
        });

        copyButton.setOnClickListener(v -> {
             if (!TextUtils.isEmpty(currentQuoteText) && !currentQuoteText.startsWith("No quotes")) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("quote", currentQuoteText);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(QuotesActivity.this, "Quote copied!", Toast.LENGTH_SHORT).show();
            }
        });

        shareButton.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(currentQuoteText) && !currentQuoteText.startsWith("No quotes")) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, currentQuoteText);
                startActivity(Intent.createChooser(shareIntent, "Share quote via"));
            }
        });

        favoriteButton.setOnClickListener(v -> {
            if (TextUtils.isEmpty(currentQuoteText) || currentQuoteText.startsWith("No quotes")) return;
            AppDatabase.databaseWriteExecutor.execute(() -> {
                FavoriteQuote existingQuote = favoriteQuoteDao.getFavoriteQuoteByTextAndCategory(currentQuoteText, currentCategory);
                if (existingQuote != null) {
                    favoriteQuoteDao.delete(existingQuote);
                } else {
                    favoriteQuoteDao.insert(new FavoriteQuote(currentQuoteText, currentCategory));
                }
            });
        });

        explainButton.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(currentQuoteText) && !currentQuoteText.startsWith("No quotes")) {
                Intent explanationIntent = new Intent(QuotesActivity.this, QuoteExplanationActivity.class);
                explanationIntent.putExtra("quote", currentQuoteText);
                startActivity(explanationIntent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
            }
        });
    }

    private void displayCurrentQuoteForCategory() {
        String[] categoryQuotes = quotesMap.get(currentCategory);
        int currentIndex = currentQuoteIndices.getOrDefault(currentCategory, 0);

        if (categoryQuotes != null && currentIndex < categoryQuotes.length) {
            currentQuoteText = categoryQuotes[currentIndex];
            quoteTextArea.setText(currentQuoteText);
        } else {
            currentQuoteText = "No quotes available for this category.";
            quoteTextArea.setText(currentQuoteText);
        }

        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        quoteTextArea.startAnimation(fadeIn);

        if (currentFavoriteStatusLiveData != null) {
            currentFavoriteStatusLiveData.removeObservers(this);
        }
        if (!TextUtils.isEmpty(currentQuoteText) && !currentQuoteText.startsWith("No quotes")) {
            currentFavoriteStatusLiveData = favoriteQuoteDao.getFavoriteQuoteLiveData(currentQuoteText, currentCategory);
            currentFavoriteStatusLiveData.observe(this, favoriteStatusObserver);
        } else {
            favoriteButton.setImageResource(R.drawable.ic_futuristic_star_outline);
        }
    }

    private void preFetchNextBatchQuotes(String categoryName) {
        if (isPreFetching || isFetching || GEMINI_API_KEY.equals("YOUR_API_KEY_HERE")) return;
        isPreFetching = true;

        AppDatabase.databaseWriteExecutor.execute(() -> {
            String[] fetchedQuotes = fetchQuotesFromApi(categoryName);
            runOnUiThread(() -> {
                if (fetchedQuotes != null) {
                    preFetchedQuotes = fetchedQuotes;
                }
                isPreFetching = false;
            });
        });
    }

    private void fetchFreshQuotesForCategory(String categoryName, boolean isNextBatch) {
        if (isFetching || GEMINI_API_KEY.equals("YOUR_API_KEY_HERE")) return;
        isFetching = true;

        AppDatabase.databaseWriteExecutor.execute(() -> {
            String[] fetchedQuotes = fetchQuotesFromApi(categoryName);
            runOnUiThread(() -> {
                if (fetchedQuotes != null) {
                    quotesMap.put(categoryName, fetchedQuotes);
                    currentQuoteIndices.put(categoryName, 0);
                } else if (!isNextBatch) {
                    quotesMap.put(categoryName, getLocalFallbackQuotes(categoryName));
                }
                displayCurrentQuoteForCategory();
                isFetching = false;
            });
        });
    }

    private String[] parseQuotesFromApiResponse(String rawResponse) {
        if (rawResponse == null || rawResponse.trim().isEmpty()) {
            return new String[0];
        }

        // Remove common conversational preambles
        String cleanedResponse = rawResponse.replaceAll("(?im)^.*?quotes.*?:\\s*\\n?", "");

        String[] lines = cleanedResponse.split("\n");
        List<String> quoteList = new ArrayList<>();

        for (String line : lines) {
            // Remove list formatting (numbers, bullets)
            String trimmedLine = line.trim().replaceAll("^\\d+\\.\\s*", "").replaceAll("^[-*•–]\\s*", "");
            if (!trimmedLine.isEmpty()) {
                quoteList.add(trimmedLine);
            }
        }
        return quoteList.toArray(new String[0]);
    }


    private String[] fetchQuotesFromApi(String categoryName) {
         HttpURLConnection urlConnection = null;
        try {
            URL url = new URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent");
            JSONObject requestBody = new JSONObject();
            requestBody.put("contents", new JSONArray().put(new JSONObject().put("parts", new JSONArray().put(new JSONObject().put("text", "Provide " + QUOTE_BATCH_SIZE + " new, distinct, very short quotes (ideally under 15 words) about \"" + categoryName + "\".")))));

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("X-goog-api-key", GEMINI_API_KEY);
            urlConnection.setDoOutput(true);

            try (OutputStream os = urlConnection.getOutputStream()) {
                os.write(requestBody.toString().getBytes(StandardCharsets.UTF_8));
            }

            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    String text = jsonResponse.getJSONArray("candidates").getJSONObject(0).getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text");
                    return parseQuotesFromApiResponse(text);
                }
            } else {
                Log.e(TAG, "API request failed with code: " + urlConnection.getResponseCode());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching quotes from API", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return null;
    }


    private String[] getLocalFallbackQuotes(String categoryName) {
        return quotesMap.getOrDefault(categoryName, new String[]{"Local quotes for " + categoryName + " could not be loaded."});
    }

    private void initializeQuotes() {
        // This method is now primarily for fallback, API is preferred.
        quotesMap.put("Motivation", new String[]{"Love your work. - Steve J.", "Keep going. - Sam L."});
        quotesMap.put("Love", new String[]{"Sun from both sides. - David V.", "Love finds you. - Loretta Y."});
        // Add other categories as needed
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (currentFavoriteStatusLiveData != null) {
            currentFavoriteStatusLiveData.removeObservers(this);
        }
    }
}
