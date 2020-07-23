package com.example.pawsibilities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pawsibilities.databinding.ItemTagBinding;
import com.example.pawsibilities.fragments.MapsFragment;
import com.parse.ParseUser;

import java.util.List;

public class TagAdapter extends RecyclerView.Adapter<TagAdapter.ViewHolder> {

    private ItemTagBinding binding;
    private List<Tag> tagList;
    private Context context;

    public TagAdapter(Context context, List<Tag> list) {
        this.context = context;
        tagList = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        binding = ItemTagBinding.inflate(LayoutInflater.from(context), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Tag t = tagList.get(position);
        holder.bind(t);
    }

    @Override
    public int getItemCount() {
        return tagList.size();
    }

    public void clear() {
        tagList.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<Tag> lst) {
        tagList.addAll(lst);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ItemTagBinding itemBinding;

        public ViewHolder(@NonNull ItemTagBinding b) {
            super(b.getRoot());
            itemBinding = b;
        }

        private void bind(Tag t) {
            binding.tvName.setText(t.getName());
            // TODO make text resource string
            binding.tvDistance.setText(t.distanceFrom(ParseUser.getCurrentUser().getParseGeoPoint(MapsFragment.KEY_LOCATION)) + " miles away");
        }
    }
}
