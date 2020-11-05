package com.example.myapplication.fragments.tabs;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.HomeActivity;
import com.example.myapplication.R;
import com.example.myapplication.adapters.ListRecyclerAdapter;
import com.example.myapplication.models.MovieModel;
import com.example.myapplication.utilities.DividerItemDecoration;
import com.example.myapplication.utilities.OnLoadMoreListener;

import java.util.ArrayList;

public class ListRecyclerFragment extends Fragment {

    private final int visibleThreshold = 5;
    public RecyclerView movieListRecycler;
    public ListRecyclerAdapter listRecyclerAdapter;
    public TextView message;
    private ArrayList<MovieModel> movies;
    private LinearLayoutManager linearLayoutManager;
    private OnLoadMoreListener mOnLoadMoreListener;
    private boolean isLoading;
    private int lastVisibleItem, totalItemCount;

    public ListRecyclerFragment() {
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
        return inflater.inflate(R.layout.fragment_recycler_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        movieListRecycler = view.findViewById(R.id.list_recycler);
        message = view.findViewById(R.id.message);
        if (movies != null) {
            linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
            movieListRecycler.setLayoutManager(linearLayoutManager);

            listRecyclerAdapter = new ListRecyclerAdapter(getContext(), movies);
            movieListRecycler.setAdapter(listRecyclerAdapter);

            movieListRecycler.setItemAnimator(new DefaultItemAnimator());
            movieListRecycler.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));

            movieListRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    totalItemCount = linearLayoutManager.getItemCount();
                    lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();

                    if (!isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                        if (mOnLoadMoreListener != null) {
                            mOnLoadMoreListener.onLoadMore();
                        }
                        isLoading = true;
                    }
                }
            });

        }
        movieListRecycler.setVisibility(View.GONE);
        message.setVisibility(View.VISIBLE);
    }

    public void setLoaded() {
        isLoading = false;
    }
}
