package com.example.pawsibilities.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.example.pawsibilities.EndlessRecyclerViewScrollListener;
import com.example.pawsibilities.R;
import com.example.pawsibilities.Tag;
import com.example.pawsibilities.TagAdapter;
import com.example.pawsibilities.databinding.FragmentTagListBinding;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

public class TagListFragment extends Fragment {

    private static final String TAG = "TagListFragment";
    private FragmentTagListBinding binding;
    private List<Tag> tagList;
    private TagAdapter adapter;
    private EndlessRecyclerViewScrollListener scrollListener;

    public TagListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentTagListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tagList = new ArrayList<>();
        adapter = new TagAdapter(getContext(), tagList);
        binding.rvTags.setAdapter(adapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.rvTags.setLayoutManager(layoutManager);
        scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                queryTags(page);
            }
        };
        binding.rvTags.addOnScrollListener(scrollListener);

        binding.swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //refresh timeline
                adapter.clear();
                queryTags(0);
                binding.swipeContainer.setRefreshing(false);
            }
        });
        // Configure the refreshing colors
        binding.swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        queryTags(0);
    }

    private void queryTags(int page) {
        ParseQuery<Tag> query = ParseQuery.getQuery(Tag.class);
        query.include(Tag.KEY_UPDATED_AT);
        query.setLimit(15);
        query.setSkip(page * 15);
        query.whereEqualTo(Tag.KEY_ACTIVE, true);
        query.findInBackground(new FindCallback<Tag>() {
            @Override
            public void done(List<Tag> queried, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "issue with getting Tags", e);
                    return;
                }
                adapter.addAll(queried);
            }
        });
    }
}