package com.suryakusuma.novelin;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GoogleSignInHelper {
    private static final String TAG = "GoogleSignInHelper";
    private static final String PREF_NAME = "google_session";
    private static final String SUPABASE_URL = "https://ubdqjyojnzzdqhlmmkil.supabase.co";
    private static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InViZHFqeW9qbnp6ZHFobG1ta2lsIiwicm9sZSI6ImFub24iLCJpYXQiOjE3Nzg0NzgyNzksImV4cCI6MjA5NDA1NDI3OX0.jp9Tj-hfQBTpzUtq8RFzhzUr4My73EHw-VY6HhYSKyk";

    private final Context context;
    private GoogleSignInClient googleSignInClient;
    private final OkHttpClient okHttpClient;
    private AuthCallback callback;

    public interface AuthCallback {
        void onSuccess(GoogleSignInAccount account, String supabaseToken);
        void onError(String error);
    }

    public GoogleSignInHelper(Context context) {
        this.context = context;
        this.okHttpClient = new OkHttpClient();
        buildGoogleSignInClient();
    }

    private void buildGoogleSignInClient() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.google_web_client_id))
                .requestEmail()
                .requestProfile()
                .build();

        googleSignInClient = GoogleSignIn.getClient(context, gso);
    }

    public Intent getSignInIntent() {
        signOutSilently();
        buildGoogleSignInClient();
        return googleSignInClient.getSignInIntent();
    }

    private void signOutSilently() {
        if (googleSignInClient != null) {
            googleSignInClient.signOut();
        }
    }

    public void handleSignInResult(Intent data, AuthCallback callback) {
        this.callback = callback;
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            Log.d(TAG, "Google sign in success: " + account.getEmail());
            authenticateWithSupabase(account);
        } catch (ApiException e) {
            Log.e(TAG, "Google sign in failed", e);
            callback.onError("Google sign in failed: " + e.getStatusCode());
        }
    }

    private void authenticateWithSupabase(GoogleSignInAccount account) {
        String idToken = account.getIdToken();

        String url = SUPABASE_URL + "/auth/v1/token?grant_type=id_token";

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("id_token", idToken);
            jsonBody.put("provider", "google");
        } catch (Exception e) {
            callback.onError("JSON error: " + e.getMessage());
            return;
        }

        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"),
                jsonBody.toString()
        );

        Request request = new Request.Builder()
                .url(url)
                .header("apikey", SUPABASE_ANON_KEY)
                .header("Content-Type", "application/json")
                .post(body)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Supabase auth network error", e);
                callback.onError("Network error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : "";
                Log.d(TAG, "Supabase response code: " + response.code());

                if (response.isSuccessful()) {
                    try {
                        JSONObject json = new JSONObject(responseBody);
                        String accessToken = json.getString("access_token");
                        String userId = json.getJSONObject("user").getString("id");

                        saveUserSession(account, accessToken, userId);
                        callback.onSuccess(account, accessToken);
                    } catch (Exception e) {
                        callback.onError("Parse error: " + e.getMessage());
                    }
                } else {
                    callback.onError("Supabase auth failed: " + response.code());
                }
            }
        });
    }

    // 🔥 INI METHOD YANG DIUBAH
    private void saveUserSession(GoogleSignInAccount account, String accessToken, String userId) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        // Hapus data lama terlebih dahulu
        prefs.edit().clear().apply();

        // Simpan data baru
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("user_id", userId);
        editor.putString("email", account.getEmail());
        editor.putString("name", account.getDisplayName());
        if (account.getPhotoUrl() != null) {
            editor.putString("photo_url", account.getPhotoUrl().toString());
        }
        editor.putString("access_token", accessToken);
        editor.putString("login_type", "google");
        editor.putBoolean("is_logged_in", true);
        editor.apply();

        Log.d(TAG, "User session saved: " + account.getEmail());
    }

    public void signOut() {
        if (googleSignInClient != null) {
            googleSignInClient.revokeAccess().addOnCompleteListener(task -> {
                Log.d(TAG, "Access revoked: " + task.isSuccessful());
                googleSignInClient.signOut().addOnCompleteListener(task2 -> {
                    SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                    prefs.edit().clear().apply();
                    Log.d(TAG, "User signed out and access revoked");
                });
            });
        } else {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            prefs.edit().clear().apply();
        }
    }

    public boolean isLoggedIn() {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean("is_logged_in", false);
    }
}