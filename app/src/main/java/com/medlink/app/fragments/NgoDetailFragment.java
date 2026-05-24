package com.medlink.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.medlink.app.activities.MainActivity;
import com.medlink.app.models.Ngo;
import com.medlink.app.R;

import java.util.Map;

public class NgoDetailFragment extends Fragment {

    private static final String ARG_NGO = "arg_ngo";
    private Ngo ngo;

    public static NgoDetailFragment newInstance(Ngo ngo) {
        NgoDetailFragment fragment = new NgoDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_NGO, ngo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            ngo = (Ngo) getArguments().getSerializable(ARG_NGO);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ngo_detail, container, false);

        if (ngo != null) {
            TextView tvName = view.findViewById(R.id.tv_detail_name);

            tvName.setText(ngo.getName());


            view.findViewById(R.id.btn_request_medicine).setOnClickListener(v -> {
                // Navigate to request form
                Fragment form = new RequestFormFragment();
                // We could pass NGO name here if needed
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).loadFragment(form, true);
                }
            });
            
            androidx.appcompat.widget.Toolbar toolbar = view.findViewById(R.id.toolbar);
            toolbar.setNavigationOnClickListener(v -> {
                if (getParentFragmentManager() != null) {
                    getParentFragmentManager().popBackStack();
                }
            });
        }

        return view;
    }
}
