package com.example.myapplication1;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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

public class QuoteExplanationActivity extends AppCompatActivity {

    private TextView quoteTextExplanation;
    private TextView quoteAuthorExplanation;
    private TextView explanationText;

    private static final String GEMINI_API_KEY = "AIzaSyAakiSSSKRIHR4q2BQ1IiOmKKqlqEaaPOE";
    private static final String TAG = "QuoteExplanation";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SettingsActivity.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quote_explanation);

        MaterialToolbar toolbar = findViewById(R.id.toolbar_explanation);
        toolbar.setNavigationOnClickListener(v -> finish());

        quoteTextExplanation = findViewById(R.id.quote_text_explanation);
        quoteAuthorExplanation = findViewById(R.id.quote_author_explanation);
        explanationText = findViewById(R.id.explanation_text);

        String quote = getIntent().getStringExtra("quote");
        String author = getIntent().getStringExtra("author");

        quoteTextExplanation.setText(String.format("\"%s\"", quote));
        if (author != null && !author.isEmpty()) {
            quoteAuthorExplanation.setText(String.format("- %s", author));
        } else {
            quoteAuthorExplanation.setText("");
        }

        if (quote != null && !quote.isEmpty()) {
            fetchExplanation(quote);
        } else {
            explanationText.setText("No quote provided to explain.");
        }
    }

    private void fetchExplanation(String quote) {
        explanationText.setText("Fetching explanation...");

        AppDatabase.databaseWriteExecutor.execute(() -> {
            HttpURLConnection urlConnection = null;
            String rawApiResponse = null;
            String errorMessage = null;

            try {
                URL url = new URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent");
                JSONObject requestBody = new JSONObject();
                JSONObject content = new JSONObject();
                JSONArray partsArray = new JSONArray();
                JSONObject part = new JSONObject();
                part.put("text", "Explain the following quote in a thoughtful and detailed way: \"" + quote + "\"");
                partsArray.put(part);
                content.put("parts", partsArray);
                JSONArray contentsArray = new JSONArray();
                contentsArray.put(content);
                requestBody.put("contents", contentsArray);

                JSONObject generationConfig = new JSONObject();
                generationConfig.put("temperature", 0.7);
                generationConfig.put("maxOutputTokens", 512);
                requestBody.put("generationConfig", generationConfig);

                String jsonInputString = requestBody.toString();
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("X-goog-api-key", GEMINI_API_KEY);
                urlConnection.setDoOutput(true);
                urlConnection.setConnectTimeout(15000);
                urlConnection.setReadTimeout(15000);

                try (OutputStream os = urlConnection.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int responseCode = urlConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    StringBuilder responseBuilder = new StringBuilder();
                    try (InputStream is = urlConnection.getInputStream();
                         BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            responseBuilder.append(line.trim());
                        }
                    }
                    JSONObject jsonResponse = new JSONObject(responseBuilder.toString());
                    JSONArray candidates = jsonResponse.getJSONArray("candidates");
                    if (candidates.length() > 0) {
                        rawApiResponse = candidates.getJSONObject(0).getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text").trim();
                    } else {
                        errorMessage = "No explanation found in response.";
                    }
                } else {
                    errorMessage = "API request failed with code: " + responseCode;
                }
            } catch (Exception e) {
                errorMessage = "Network/other error: " + e.getMessage();
                Log.e(TAG, "Error fetching explanation", e);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }

            final String finalExplanation = rawApiResponse;
            final String finalErrorMessage = errorMessage;

            runOnUiThread(() -> {
                if (finalExplanation != null && !finalExplanation.isEmpty()) {
                    explanationText.setText(finalExplanation);
                } else {
                    explanationText.setText("Failed to get explanation. " + (finalErrorMessage != null ? finalErrorMessage : "Unknown error."));
                    Toast.makeText(QuoteExplanationActivity.this, "Could not fetch explanation.", Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}