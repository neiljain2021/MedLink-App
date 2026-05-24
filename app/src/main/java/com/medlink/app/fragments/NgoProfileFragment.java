package com.medlink.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.medlink.app.R;
import com.medlink.app.activities.NgoActivity;

public class NgoProfileFragment extends Fragment {

    private TextView tvName, tvEmail;

    public NgoProfileFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ngo_profile, container, false);

        tvName = view.findViewById(R.id.tv_ngo_profile_name);
        tvEmail = view.findViewById(R.id.tv_ngo_profile_email);

        if (getActivity() instanceof NgoActivity) {
            String ngoName = ((NgoActivity) getActivity()).getCurrentNgoName();
            if (ngoName != null && !ngoName.isEmpty()) {
                tvName.setText(ngoName);
            }
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getEmail() != null) {
            tvEmail.setText(user.getEmail());
        }

        return view;
    }
}
