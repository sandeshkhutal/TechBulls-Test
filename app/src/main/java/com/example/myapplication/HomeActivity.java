package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.myapplication.fragments.tabs.GridRecyclerFragment;
import com.example.myapplication.fragments.tabs.ListRecyclerFragment;
import com.example.myapplication.models.MovieModel;
import com.example.myapplication.models.SearchResultModel;
import com.example.myapplication.retrofit.ApiCall;
import com.example.myapplication.utilities.ConnectivityChangeReceiver;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends MainActivity implements
        ConnectivityChangeReceiver.ConnectivityReceiverListener {

    public static ArrayList<MovieModel> movies;
    private final String TAG = "HomeActivity";
    public SearchResultModel searchResult;
    private int pagesLoaded;
    private String latestQuery;
    private boolean reachedEnd;

    private SearchView searchView;
    private ProgressDialog progressDialog;

    private ListRecyclerFragment listFragment;
    private GridRecyclerFragment gridFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Checking();
        movies = new ArrayList<>();

        listFragment = new ListRecyclerFragment();
        gridFragment = new GridRecyclerFragment();

        setupLazyLoad();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Fetching...");
        progressDialog.setCancelable(false);

        Toolbar toolbar = this.findViewById(R.id.toolbar);
        this.setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(false);

        ViewPager viewPager = findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);


        getData("spiderman", true);
        if (progressDialog != null && !progressDialog.isShowing()) {
            progressDialog.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                HomeActivity.this.latestQuery = query;
                getData(query, true);
                if (progressDialog != null && !progressDialog.isShowing()) {
                    progressDialog.show();
                }
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                HomeActivity.this.latestQuery = query;
                getData(query, true);
                return true;

            }
        };
        searchView.setOnQueryTextListener(queryTextListener);

        return super.onCreateOptionsMenu(menu);
    }

    public void getData(final String query, boolean newQuery) {
        if (newQuery) {
            movies.clear();
//            listFragment.listRecyclerAdapter.notifyDataSetChanged();
            //  gridFragment.gridRecyclerAdapter.notifyDataSetChanged();
            pagesLoaded = 0;
            reachedEnd = false;
            ApiCall.Factory.getInstance().search(query, "movie", 1).enqueue(new Callback<SearchResultModel>() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onResponse(@NonNull Call<SearchResultModel> call, @NonNull Response<SearchResultModel> response) {
                    searchResult = response.body();
                    if (searchResult != null) {
                        if (searchResult.getResponse().equals("True")) {
                            //Movie Found
                            pagesLoaded = 1;
                            getMovies();
                            listFragment.message.setVisibility(View.GONE);
                            gridFragment.message.setVisibility(View.GONE);
                            listFragment.movieListRecycler.setVisibility(View.VISIBLE);
                            gridFragment.movieGridRecycler.setVisibility(View.VISIBLE);
                        } else {
                            //Movie not found
                            progressDialog.dismiss();
                            listFragment.message.setText("No movies found. Try again.");
                            gridFragment.message.setText("No movies found. Try again.");
                            listFragment.message.setVisibility(View.VISIBLE);
                            gridFragment.message.setVisibility(View.VISIBLE);
                            listFragment.movieListRecycler.setVisibility(View.GONE);
                            gridFragment.movieGridRecycler.setVisibility(View.GONE);
                        }
                    }
                }

                @SuppressLint("SetTextI18n")
                @Override
                public void onFailure(@NonNull Call<SearchResultModel> call, @NonNull Throwable t) {
                    Log.e(TAG, "Failure : " + t.getMessage());
                    progressDialog.dismiss();
                    listFragment.message.setText("Query request failed. Try again");
                    gridFragment.message.setText("Query request failed. Try again");
                    listFragment.message.setVisibility(View.VISIBLE);
                    gridFragment.message.setVisibility(View.VISIBLE);
                    listFragment.movieListRecycler.setVisibility(View.GONE);
                    gridFragment.movieGridRecycler.setVisibility(View.GONE);
                }
            });
        } else {
            if (!reachedEnd) {
                pagesLoaded++;
                ApiCall.Factory.getInstance().search(query, "movie", pagesLoaded).enqueue(new Callback<SearchResultModel>() {
                    @Override
                    public void onResponse(@NonNull Call<SearchResultModel> call, @NonNull Response<SearchResultModel> response) {
                        searchResult = response.body();
                        if (searchResult != null) {
                            if (searchResult.getResponse().equals("True")) {
                                //Movie Found
                                getMovies();
                            } else {
                                //Reached End
                                movies.remove(movies.size() - 1);
                                reachedEnd = true;
                                listFragment.listRecyclerAdapter.notifyItemRemoved(movies.size());
                                gridFragment.gridRecyclerAdapter.notifyItemRemoved(movies.size());
                                listFragment.listRecyclerAdapter.notifyDataSetChanged();
                                gridFragment.gridRecyclerAdapter.notifyDataSetChanged();
                                listFragment.setLoaded();
                                gridFragment.setLoaded();
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<SearchResultModel> call, @NonNull Throwable t) {
                        movies.remove(movies.size() - 1);
                        listFragment.listRecyclerAdapter.notifyItemRemoved(movies.size());
                        gridFragment.gridRecyclerAdapter.notifyItemRemoved(movies.size());
                        listFragment.listRecyclerAdapter.notifyDataSetChanged();
                        gridFragment.gridRecyclerAdapter.notifyDataSetChanged();
                        listFragment.setLoaded();
                        gridFragment.setLoaded();
                    }
                });
            }
        }
    }

    public void getMovies() {
        final int[] count = {0};
        for (int i = 0; i < searchResult.getSearch().size(); i++) {
            String imdbId = searchResult.getSearch().get(i).getImdbID();
            ApiCall.Factory.getInstance().getMovie(imdbId).enqueue(new Callback<MovieModel>() {
                @Override
                public void onResponse(@NonNull Call<MovieModel> call, @NonNull Response<MovieModel> response) {
                    movies.add(response.body());
                    Log.d(TAG, movies.get(movies.size() - 1).getTitle());
                    count[0]++;
                    isDataFetchComplete(count[0]);
                }

                @Override
                public void onFailure(@NonNull Call<MovieModel> call, @NonNull Throwable t) {
                    Log.e(TAG, "Failure : " + t.getMessage());
                    count[0]++;
                    isDataFetchComplete(count[0]);
                }
            });
        }
    }

    private void isDataFetchComplete(int count) {
        if (searchResult.getResponse().equals("True") && count == searchResult.getSearch().size()) {
            progressDialog.dismiss();
            for (int i = 0; i < movies.size(); i++) {
                if (movies.get(i) == null) {
                    movies.remove(i);
                    listFragment.listRecyclerAdapter.notifyItemRemoved(i);
                    gridFragment.gridRecyclerAdapter.notifyItemRemoved(i);
                }
            }
            listFragment.listRecyclerAdapter.notifyDataSetChanged();
            listFragment.setLoaded();
            gridFragment.gridRecyclerAdapter.notifyDataSetChanged();
            gridFragment.setLoaded();
        }
    }

    private void setupLazyLoad() {
        listFragment.setOnLoadMoreListener(() -> {
            if (!reachedEnd) {
                movies.add(null);
                listFragment.listRecyclerAdapter.notifyItemInserted(movies.size() - 1);
                getData(latestQuery, false);
            }
        });
        gridFragment.setOnLoadMoreListener(() -> {
            if (!reachedEnd) {
                movies.add(null);
                gridFragment.gridRecyclerAdapter.notifyItemInserted(movies.size() - 1);
                getData(latestQuery, false);
            }
        });
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(listFragment, "LIST");
        adapter.addFragment(gridFragment, "GRID");
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        App.getInstance().setConnectivityListener(this);
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        showNetworkMessage(isConnected);
    }

    static class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

}
