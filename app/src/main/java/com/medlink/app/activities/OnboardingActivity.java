package com.medlink.app.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.medlink.app.adapters.OnboardingAdapter;
import com.medlink.app.R;

import java.util.ArrayList;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {

    private OnboardingAdapter onboardingAdapter;
    private LinearLayout layoutOnboardingIndicators;
    private TextView buttonOnboardingAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        layoutOnboardingIndicators = findViewById(R.id.dots_container);
        buttonOnboardingAction = findViewById(R.id.btn_next);
        TextView textSkip = findViewById(R.id.tv_skip);

        setupOnboardingItems();

        ViewPager2 onboardingViewPager = findViewById(R.id.viewPager);
        onboardingViewPager.setAdapter(onboardingAdapter);

        setupOnboardingIndicators();
        setCurrentOnboardingIndicator(0);

        onboardingViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                setCurrentOnboardingIndicator(position);

                if (position == onboardingAdapter.getItemCount() - 1) {
                    buttonOnboardingAction.setText("Get Started");
                } else {
                    buttonOnboardingAction.setText("Next Step →");
                }
            }
        });

        buttonOnboardingAction.setOnClickListener(v -> {
            if (onboardingViewPager.getCurrentItem() + 1 < onboardingAdapter.getItemCount()) {
                onboardingViewPager.setCurrentItem(onboardingViewPager.getCurrentItem() + 1);
            } else {
                finishOnboarding();
            }
        });

        textSkip.setOnClickListener(v -> finishOnboarding());
    }

    private void setupOnboardingItems() {
        List<OnboardingAdapter.OnboardingItem> onboardingItems = new ArrayList<>();

        OnboardingAdapter.OnboardingItem item1 = new OnboardingAdapter.OnboardingItem(
                "Donate Medicine Easily",
                "Give your unused, unexpired medicines a second life and help those in need through our verified NGO partners.",
                R.drawable.ic_heart // Placeholder
        );

        OnboardingAdapter.OnboardingItem item2 = new OnboardingAdapter.OnboardingItem(
                "Hassle-Free Pickups",
                "We'll come to your doorstep to collect your donations at your convenience.",
                R.drawable.ic_home // Placeholder
        );

        OnboardingAdapter.OnboardingItem item3 = new OnboardingAdapter.OnboardingItem(
                "Verified & Trusted",
                "We work with registered NGOs to ensure medicines reach the right hands safely and efficiently.",
                R.drawable.ic_verified_badge // Placeholder
        );

        onboardingItems.add(item1);
        onboardingItems.add(item2);
        onboardingItems.add(item3);

        onboardingAdapter = new OnboardingAdapter(onboardingItems);
    }

    private void setupOnboardingIndicators() {
        ImageView[] indicators = new ImageView[onboardingAdapter.getItemCount()];
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(8, 0, 8, 0);
        for (int i = 0; i < indicators.length; i++) {
            indicators[i] = new ImageView(getApplicationContext());
            indicators[i].setImageDrawable(ContextCompat.getDrawable(
                    getApplicationContext(),
                    R.drawable.bg_dot_inactive // Need to create this
            ));
            indicators[i].setLayoutParams(layoutParams);
            layoutOnboardingIndicators.addView(indicators[i]);
        }
    }

    private void setCurrentOnboardingIndicator(int index) {
        int childCount = layoutOnboardingIndicators.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ImageView imageView = (ImageView) layoutOnboardingIndicators.getChildAt(i);
            if (i == index) {
                imageView.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_dot_active)); // Need to create
            } else {
                imageView.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_dot_inactive)); // Need to create
            }
        }
    }

    private void finishOnboarding() {
        SharedPreferences prefs = getSharedPreferences("MedLinkPrefs", MODE_PRIVATE);
        prefs.edit().putBoolean("isFirstRun", false).apply();
        
        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        finish();
    }
}
