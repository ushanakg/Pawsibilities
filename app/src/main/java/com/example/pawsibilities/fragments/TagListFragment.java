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
import android.widget.Toast;

import com.codepath.asynchttpclient.AsyncHttpClient;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.example.pawsibilities.EndlessRecyclerViewScrollListener;
import com.example.pawsibilities.Tag;
import com.example.pawsibilities.TagAdapter;
import com.example.pawsibilities.databinding.FragmentTagListBinding;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;
import okhttp3.Headers;

public class TagListFragment extends Fragment {

    private static final String TAG = "TagListFragment";
    private FragmentTagListBinding binding;
    private List<Tag> tagList;
    private TagAdapter adapter;
    private EndlessRecyclerViewScrollListener scrollListener;

    private static final String DISTANCE_MATRIX_URL = "https://maps.googleapis.com/maps/api/distancematrix/json?units=imperial&mode=walking&origins=%f,%f&destinations=%s&key=AIzaSyDuVh9ZaszyhzGn4_AwfClrzt-iEjg1Xeo";

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

        // refresh timeline
        binding.swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                adapter.clear();
                queryTags(0);
                binding.swipeContainer.setRefreshing(false);
            }
        });
        // Configure the refreshing colors
        binding.swipeContainer.setColorSchemeResources(android.R.color.holo_orange_light);

        // infinite scroll
        scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                queryTags(page);
            }
        };
        binding.rvTags.addOnScrollListener(scrollListener);

        queryTags(0);
    }

    // TODO increment radius (by chunk unit) when furthest tag is past 75% of current radius
    private void queryTags(int page) {
        binding.rvShimmer.showShimmerAdapter();

        ParseQuery<Tag> query = ParseQuery.getQuery(Tag.class);
        query.include(Tag.KEY_UPDATED_AT);
        query.setLimit(10);
        query.setSkip(page * 10);
        query.whereNear(Tag.KEY_LOCATION,
                ParseUser.getCurrentUser().getParseGeoPoint(MapsFragment.KEY_LOCATION));
        query.whereEqualTo(Tag.KEY_ACTIVE, true);
        query.findInBackground(new FindCallback<Tag>() {
            @Override
            public void done(List<Tag> queried, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "issue with getting Tags", e);
                    return;
                }

                getWalkingTime(queried);
            }
        });
    }

    private void getWalkingTime(final List<Tag> lst) {
        if (lst.size() > 0) {
            // format request url
            ParseGeoPoint userLocation = ParseUser.getCurrentUser().getParseGeoPoint(MapsFragment.KEY_LOCATION);
            String destinations = "";
            for (Tag t : lst) {
                destinations += t.getLocation().getLatitude() + "," + t.getLocation().getLongitude() + "|";
            }
            destinations = destinations.substring(0, destinations.length() - 1);
            String url = String.format(DISTANCE_MATRIX_URL, userLocation.getLatitude(), userLocation.getLongitude(), destinations);

            // use distance matrix api to get walking duration to each queried tag
            AsyncHttpClient client = new AsyncHttpClient();
            client.get(url, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Headers headers, JSON json) {
                    JSONObject jsonObject = json.jsonObject;
                    try {
                        JSONArray elements = jsonObject.getJSONArray("rows")
                                .getJSONObject(0).getJSONArray("elements");
                        for (int i = 0; i < elements.length(); i++) {
                            JSONObject duration = ((JSONObject) elements.get(i)).getJSONObject("duration");
                            lst.get(i).setWalkingTime(duration.getString("text"));
                            lst.get(i).setWalkingTimeValue(duration.getInt("value"));
                        }

                        adapter.addAll(lst);
                        binding.rvShimmer.hideShimmerAdapter();

                    } catch (JSONException e) {
                        Log.e(TAG, "Distance matrix api request failed", e);
                        Toasty.error(getContext(), "Tags unavailable", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                    Toasty.error(getContext(), "Tags unavailable", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}