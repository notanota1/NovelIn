package com.suryakusuma.novelin;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.SignInButton;

public class LoginActivity extends AppCompatActivity {

    DatabaseHelper db;
    EditText etUsername, etPassword;
    Button btnLogin;
    TextView tvRegister;

    private static final int RC_SIGN_IN = 1001;
    private GoogleSignInHelper googleSignInHelper;
    private SignInButton btnGoogleSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        googleSignInHelper = new GoogleSignInHelper(this);

        if (googleSignInHelper.isLoggedIn()) {
            startMainActivity();
            return;
        }

        db = new DatabaseHelper(this);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);

        if (btnGoogleSignIn != null) {
            btnGoogleSignIn.setSize(SignInButton.SIZE_WIDE);
            btnGoogleSignIn.setOnClickListener(v -> {
                Intent signInIntent = googleSignInHelper.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            });
        }

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = etUsername.getText().toString();
                String pwd = etPassword.getText().toString();

                if (user.isEmpty() || pwd.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Isi username dan password", Toast.LENGTH_SHORT).show();
                    return;
                }

                boolean res = db.checkUser(user, pwd);
                if (res) {
                    // Save session manual
                    SharedPreferences sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("username", user);
                    editor.putString("login_type", "manual");
                    editor.apply();

                    Toast.makeText(LoginActivity.this, "Successfully Logged In", Toast.LENGTH_SHORT).show();
                    Intent contentIntent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(contentIntent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Login Error - Username atau Password salah", Toast.LENGTH_SHORT).show();
                }
            }
        });

        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(registerIntent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            googleSignInHelper.handleSignInResult(data, new GoogleSignInHelper.AuthCallback() {
                @Override
                public void onSuccess(GoogleSignInAccount account, String supabaseToken) {
                    runOnUiThread(() -> {
                        Toast.makeText(LoginActivity.this,
                                "Welcome " + account.getDisplayName() + "!",
                                Toast.LENGTH_LONG).show();
                        startMainActivity();
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        Toast.makeText(LoginActivity.this, "Google Login Error: " + error, Toast.LENGTH_LONG).show();
                    });
                }
            });
        }
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}