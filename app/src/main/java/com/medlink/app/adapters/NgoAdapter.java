package com.medlink.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.medlink.app.R;
import com.medlink.app.activities.MainActivity;
import com.medlink.app.fragments.NgoDetailFragment;
import com.medlink.app.models.Ngo;

import java.util.ArrayList;
import java.util.List;

public class NgoAdapter extends RecyclerView.Adapter<NgoAdapter.NgoViewHolder> {

    private List<Ngo> ngoList = new ArrayList<>();

    public void setNgos(List<Ngo> ngos) {
        this.ngoList = ngos;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NgoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ngo_card, parent, false);
        return new NgoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NgoViewHolder holder, int position) {
        Ngo ngo = ngoList.get(position);
        holder.tvName.setText(ngo.getName());
        holder.tvLocation.setText(ngo.getLocation());
        holder.tvSpecialization.setText("Specializes: " + ngo.getCategory());
        
        // Randomize icon color for variety
        int[] bgDrawables = {R.drawable.bg_medicine_icon_blue, R.drawable.bg_category_red, R.drawable.bg_category_green, R.drawable.bg_category_orange, R.drawable.bg_category_purple};
        int[] iconDrawables = {R.drawable.ic_globe, R.drawable.ic_heart, R.drawable.ic_leaf, R.drawable.ic_medicine_box, R.drawable.ic_drop};
        
        int index = Math.abs(ngo.getName().hashCode()) % bgDrawables.length;
        holder.iconContainer.setBackgroundResource(bgDrawables[index]);
        holder.ivLogo.setImageResource(iconDrawables[index]);
        
        holder.ivVerified.setVisibility(ngo.isVerified() ? View.VISIBLE : View.GONE);
        

        // Removed detail page navigation as requested
        /*
        holder.itemView.setOnClickListener(v -> {
            if (v.getContext() instanceof MainActivity) {
                ((MainActivity) v.getContext()).loadFragment(NgoDetailFragment.newInstance(ngo), true);
            }
        });
        */
    }

    @Override
    public int getItemCount() {
        return ngoList.size();
    }

    static class NgoViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvLocation, tvSpecialization;
        ImageView ivLogo, ivVerified;
        View iconContainer;

        public NgoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_ngo_name);
            tvLocation = itemView.findViewById(R.id.tv_ngo_location);
            tvSpecialization = itemView.findViewById(R.id.tv_specialization);
            ivLogo = itemView.findViewById(R.id.iv_ngo_logo);
            ivVerified = itemView.findViewById(R.id.iv_verified);
            iconContainer = itemView.findViewById(R.id.ngo_icon_container);
        }
    }
}
