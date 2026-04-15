package com.suryakusuma.novelin;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class AccountFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        TextView tvUsername = view.findViewById(R.id.tvAccountUsername);
        Button btnLogout = view.findViewById(R.id.btnLogout);

        SharedPreferences sharedPref = getActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String username = sharedPref.getString("username", "User");
        tvUsername.setText(username);

        btnLogout.setOnClickListener(v -> {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.clear();
            editor.apply();

            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            getActivity().finish();
        });

        return view;
    }
}
