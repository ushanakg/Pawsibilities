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
    private static final int QUERY_RADIUS_INCREMENT = 50; // in miles
    private static int QUERY_RADIUS = 50;

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
                QUERY_RADIUS = QUERY_RADIUS_INCREMENT; // reset radius since we're loading from beginning
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

    private void incrementQueryRadius(List<Tag> lst) {
        Log.i(TAG, "radius: " + QUERY_RADIUS);

        double furthestTagDistance = Double.parseDouble(lst.get(lst.size() - 1)
                .distanceFrom(ParseUser.getCurrentUser().getParseGeoPoint(MapsFragment.KEY_LOCATION)));
        Log.i(TAG, "furthest tag: " + furthestTagDistance);
        if (furthestTagDistance > QUERY_RADIUS * 0.75) {
            QUERY_RADIUS += QUERY_RADIUS_INCREMENT;
        }
        Log.i(TAG, "new radius: " + QUERY_RADIUS);

    }

    // TODO increment radius (by chunk unit) when furthest tag is past 75% of current radius
    private void queryTags(int page) {
        if (page == 0) {
            binding.rvShimmer.showShimmerAdapter();
        }

        ParseQuery<Tag> query = ParseQuery.getQuery(Tag.class);
        query.include(Tag.KEY_UPDATED_AT);
        query.setLimit(10);
        query.setSkip(tagList.size());
        query.whereWithinMiles(Tag.KEY_LOCATION,
                ParseUser.getCurrentUser().getParseGeoPoint(MapsFragment.KEY_LOCATION),
                QUERY_RADIUS);
        query.whereEqualTo(Tag.KEY_ACTIVE, true);
        query.findInBackground(new FindCallback<Tag>() {
            @Override
            public void done(List<Tag> queried, ParseException e) {
                if (e != null) {
                    Toasty.error(getContext(), "Tags unavailable", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "issue with getting Tags", e);
                    return;
                }
                Log.i(TAG, "num of tags + queried: " + (tagList.size() + queried.size()));

                if (queried.size() > 0) {
                    for (int i = 0; i < queried.size(); i++) {
                        Log.i(TAG, "queried(" + i + "): " + queried.get(i).getName());
                    }

                    incrementQueryRadius(queried);
                    getWalkingTime(queried);
                } else {
                    binding.rvShimmer.hideShimmerAdapter();
                }
            }
        });
    }

    private void getWalkingTime(final List<Tag> lst) {
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

                    int lastValue = 0;
                    for (int i = 0; i < elements.length(); i++) {
                        JSONObject elem = (JSONObject) elements.get(i);

                        if (elem.get("status").equals("OK")) {
                            JSONObject duration = elem.getJSONObject("duration");
                            lst.get(i).setWalkingTime(duration.getString("text"));
                            int value = duration.getInt("value");
                            lastValue = value;
                            lst.get(i).setWalkingTimeValue(value);

                        } else {
                            lst.get(i).setWalkingTime("Unavailable");
                            lst.get(i).setWalkingTimeValue(lastValue); // assign prev value so can be sorted
                        }
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