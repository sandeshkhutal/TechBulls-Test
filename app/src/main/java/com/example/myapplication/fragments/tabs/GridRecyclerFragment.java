package com.example.myapplication.fragments.tabs;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.HomeActivity;
import com.example.myapplication.R;
import com.example.myapplication.adapters.GridRecyclerAdapter;
import com.example.myapplication.models.MovieModel;
import com.example.myapplication.utilities.GridSpacingItemDecoration;
import com.example.myapplication.utilities.OnLoadMoreListener;

import java.util.ArrayList;

public class GridRecyclerFragment extends Fragment {

    private final int visibleThreshold = 5;
    public RecyclerView movieGridRecycler;
    public GridRecyclerAdapter gridRecyclerAdapter;
    public TextView message;
    private ArrayList<MovieModel> movies;
    private GridLayoutManager gridLayoutManager;
    private OnLoadMoreListener mOnLoadMoreListener;
    private boolean isLoading;
    private int lastVisibleItem, totalItemCount;

    public GridRecyclerFragment() {
        // Required empty public constructor
    }

    public void setOnLoadMoreListener(OnLoadMoreListener mOnLoadMoreListener) {
        this.mOnLoadMoreListener = mOnLoadMoreListener;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        movies = HomeActivity.movies;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_recycler_grid, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        movieGridRecycler = view.findViewById(R.id.grid_recycler);
        message = view.findViewById(R.id.message);
        if (movies != null) {
            gridLayoutManager = new GridLayoutManager(getContext(), 2);
            movieGridRecycler.setLayoutManager(gridLayoutManager);

            gridRecyclerAdapter = new GridRecyclerAdapter(getContext(), movies);
            movieGridRecycler.setAdapter(gridRecyclerAdapter);

            movieGridRecycler.setItemAnimator(new DefaultItemAnimator());
            movieGridRecycler.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(), true));

            movieGridRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    totalItemCount = gridLayoutManager.getItemCount();
                    lastVisibleItem = gridLayoutManager.findLastVisibleItemPosition();

                    if (!isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                        if (mOnLoadMoreListener != null) {
                            mOnLoadMoreListener.onLoadMore();
                        }
                        isLoading = true;
                    }
                }
            });


        }
        movieGridRecycler.setVisibility(View.GONE);
        message.setVisibility(View.VISIBLE);
    }

    private int dpToPx() {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, r.getDisplayMetrics()));
    }

    public void setLoaded() {
        isLoading = false;
    }
}
