package com.medlink.app.fragments;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * Generic placeholder fragment used for tabs that aren't yet implemented.
 * Shows a centered message with the tab name — easy to replace later.
 */
public class PlaceholderFragment extends Fragment {

    private static final String ARG_TITLE = "title";

    public PlaceholderFragment(String title) {
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        setArguments(args);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        String title = getArguments() != null
                ? getArguments().getString(ARG_TITLE, "Coming Soon")
                : "Coming Soon";

        // Build a simple centered screen
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setBackgroundColor(0xFFF8FAFC);

        TextView icon = new TextView(requireContext());
        icon.setText("🚧");
        icon.setTextSize(48);
        icon.setGravity(Gravity.CENTER);

        TextView tv = new TextView(requireContext());
        tv.setText(title + "\nComing Soon");
        tv.setTextSize(18);
        tv.setTextColor(0xFF0F172A);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(0, 16, 0, 0);

        layout.addView(icon);
        layout.addView(tv);
        return layout;
    }
}
