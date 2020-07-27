package com.example.pawsibilities;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pawsibilities.databinding.ItemTagBinding;
import com.example.pawsibilities.fragments.MapsFragment;
import com.parse.ParseUser;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class TagAdapter extends RecyclerView.Adapter<TagAdapter.ViewHolder> {

    private ItemTagBinding binding;
    private List<Tag> tagList;
    private Context context;

    public TagAdapter(Context context, List<Tag> list) {
        this.context = context;
        tagList = list;
        setHasStableIds(true);
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
        Log.i("TagAdapter", position + ": " + t.getWalkingTimeValue());
    }

    @Override
    public int getItemCount() {
        return tagList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public void clear() {
        int size = tagList.size();
        tagList.clear();
        notifyItemRangeRemoved(0, size);
    }

    public void addAll(List<Tag> lst) {
        int start = tagList.size();
        tagList.addAll(lst);
        quickSort(0, tagList.size() - 1);
        notifyDataSetChanged();
        //notifyItemRangeInserted(start, tagList.size() - start);
    }

    // quicksort tagList by walking distance (in seconds)
    private void quickSort(int left, int right) {
        if (left >= right) {
            return;
        }

        int partition = partition(left, right);
        quickSort(left, partition - 1);
        quickSort(partition + 1, right);
    }

    // partition range of tagList with tagList.get(high) as pivot
    private int partition(int low, int high) {
        int pivot_index = high;
        Tag pivot = tagList.get(high);
        high--;

        while (true) {
            while (tagList.get(low).getWalkingTimeValue() < pivot.getWalkingTimeValue()) {
                low++;
            }

            while (tagList.get(high).getWalkingTimeValue() > pivot.getWalkingTimeValue()) {
                high--;
            }

            if (low >= high) {
                break;
            }

            Collections.swap(tagList, low, high);
        }
        Collections.swap(tagList, low, pivot_index);
        return low;
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
            binding.tvDistance.setText(t.distanceFrom(ParseUser.getCurrentUser()
                    .getParseGeoPoint(MapsFragment.KEY_LOCATION)) + " miles away");
            binding.tvWalk.setText(t.getWalkingTime());
        }
    }
}
