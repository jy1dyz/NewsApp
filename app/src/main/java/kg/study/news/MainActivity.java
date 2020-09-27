package kg.study.news;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.appcompat.widget.SearchView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import kg.study.news.Adapter;
import kg.study.news.PaginationAdapter;
import kg.study.news.PaginationListener;
import kg.study.news.R;
import kg.study.news.api.ApiInterface;
import kg.study.news.api.Service;
import kg.study.news.models.Article;
import kg.study.news.models.News;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import static kg.study.news.PaginationListener.PAGE_START;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    public static final String API_KEY = "3125f0e16a2e44babcdd15b80775d54c";
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private List<Article> articles = new ArrayList<>();
    private Adapter adapter;
    private String TAG = MainActivity.class.getSimpleName();
    private TextView topHeadLine;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RelativeLayout errorLayout;
    private ImageView errorImageView;
    private TextView errorTitle, errorMessage;
    private Button btnRetry;

    private PaginationAdapter paginationAdapter;
    private ProgressBar progressBar;
    private static final int PAGE_START = 1;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private int TOTAL_PAGES = 10;
    private int currentPage = PAGE_START;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        topHeadLine = findViewById(R.id.tv_headlines);
        recyclerView = findViewById(R.id.recycler_view);
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setNestedScrollingEnabled(false);

        errorLayout = findViewById(R.id.errorLayout);
        errorImageView = findViewById(R.id.errorImage);
        errorTitle = findViewById(R.id.errorTitle);
        errorMessage = findViewById(R.id.errorMessage);
        btnRetry = findViewById(R.id.btn_retry);

        progressBar = findViewById(R.id.progressBar);
//        scrollListener();

        onLoadingSwipeRefresh("");
    }

    private void scrollListener() {
        recyclerView.addOnScrollListener(new PaginationListener(layoutManager) {

            @Override
            protected void loadMoreItems() {
                isLoading = true;
                currentPage += 1;

                loadNextNews("");
            }

            @Override
            public boolean isLastPage() {
                return isLastPage;
            }

            @Override
            public boolean isLoading() {
                return isLoading;
            }
        });
        loadFirstNews("");
//        onLoadingSwipeRefresh("");

    }

    private void loadFirstNews(final String keyword) {
        errorLayout.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(true);
        currentPage = PAGE_START;
        ApiInterface apiInterface = Service.getApi();
        String country = Utils.getCountry();
        Call<News> call;
        if (keyword.length() > 0) {
            call = apiInterface.getNewssearch(keyword, API_KEY);
        } else {
            call = apiInterface.getNews(country, API_KEY);
        }
        call.enqueue(new Callback<News>() {
            @Override
            public void onResponse(Call<News> call, Response<News> response) {
                if (response.isSuccessful() && response.body().getArticle() != null) {
                    if (!articles.isEmpty()) {
                        articles.clear();
                    }
                    List<Article> results = response.body().getArticle();
//                    progressBar.setVisibility(View.GONE);
                    errorLayout.setVisibility(View.GONE);
                    paginationAdapter = new PaginationAdapter(MainActivity.this);
                    recyclerView.setAdapter(paginationAdapter);
                    paginationAdapter.addAll(results);
                    paginationAdapter.notifyDataSetChanged();
                    if (currentPage <= TOTAL_PAGES) paginationAdapter.addLoadingFooter();
                    else isLastPage = true;

                    initPaginationListener();

                    topHeadLine.setVisibility(View.VISIBLE);
                    swipeRefreshLayout.setRefreshing(false);
                } else if (response.raw().cacheResponse() != null &&
                        response.raw().networkResponse() == null) {
                    Log.d(TAG, "onResponse: response is from CACHE");
                } else {
                    topHeadLine.setVisibility(View.INVISIBLE);
                    swipeRefreshLayout.setRefreshing(false);

                    String errorCode;
                    switch (response.code()) {
                        case 404:
                            errorCode = "404 not found";
                            break;
                        case 500:
                            errorCode = "500 server broken";
                            break;
                        default:
                            errorCode = "unknown error";
                            break;
                    }
                    showErrorMessage(
                            R.drawable.ic_mood_bad,
                            "No Result",
                            "Please try again!\n" +
                                    errorCode);
                }

            }
            @Override
            public void onFailure(Call<News> call, Throwable t) {
                Log.d(TAG, "onFailure");
                topHeadLine.setVisibility(View.INVISIBLE);
                swipeRefreshLayout.setRefreshing(false);

                showErrorMessage(
                        R.drawable.ic_mood_bad,
                        "Oops...",
                        "Network failure, please try again!\n" +
                                t.toString());
            }
        });
    }

    private void loadNextNews(final String keyword) {
        Log.d(TAG, "loadNextNews");
        errorLayout.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(true);

        ApiInterface apiInterface = Service.getApi();
        String country = Utils.getCountry();
        Call<News> call;

        if (keyword.length() > 0) {
            call = apiInterface.getNewssearch(keyword, API_KEY);
        } else {
            call = apiInterface.getNews(country, API_KEY);
        }
        call.enqueue(new Callback<News>() {
            @Override
            public void onResponse(Call<News> call, Response<News> response) {
                if (response.isSuccessful() && response.body().getArticle() != null) {

                    paginationAdapter.removeLoadingFooter();
                    isLoading = false;

                    List<Article> results = response.body().getArticle();
                    paginationAdapter.addAll(results);
                    paginationAdapter.notifyDataSetChanged();
                    if (currentPage != TOTAL_PAGES) paginationAdapter.addLoadingFooter();
                    else isLastPage = true;

                    initPaginationListener();

                    topHeadLine.setVisibility(View.VISIBLE);
                    swipeRefreshLayout.setRefreshing(false);
                } else if (response.raw().cacheResponse() != null &&
                        response.raw().networkResponse() == null) {
                    Log.d(TAG, "onResponse: response is from CACHE");
                } else {
                    topHeadLine.setVisibility(View.INVISIBLE);
                    swipeRefreshLayout.setRefreshing(false);

                    String errorCode;
                    switch (response.code()) {
                        case 404:
                            errorCode = "404 not found";
                            break;
                        case 500:
                            errorCode = "500 server broken";
                            break;
                        default:
                            errorCode = "unknown error";
                            break;
                    }
                    showErrorMessage(
                            R.drawable.ic_mood_bad,
                            "No Result",
                            "Please try again!\n" +
                                    errorCode);
                }
            }
            @Override
            public void onFailure(Call<News> call, Throwable t) {
                Log.d(TAG, "onFailure");
                topHeadLine.setVisibility(View.INVISIBLE);
                swipeRefreshLayout.setRefreshing(false);

                showErrorMessage(
                        R.drawable.ic_mood_bad,
                        "Oops...",
                        "Network failure, please try again!\n" +
                                t.toString());
            }
        });

    }

    public void loadJson(final String keyword) {

        errorLayout.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(true);

//        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        ApiInterface apiInterface = Service.getApi();
        String country = Utils.getCountry();

        Call<News> call;

        if (keyword.length() > 0) {
            call = apiInterface.getNewssearch(keyword, API_KEY);
        } else {
            call = apiInterface.getNews(country, API_KEY);
        }

        call.enqueue(new Callback<News>() {
            @Override
            public void onResponse(Call<News> call, Response<News> response) {
                if (response.isSuccessful() && response.body().getArticle() != null) {
                    Log.d(TAG, "onResponse is from Network");
                    if (!articles.isEmpty()) {
                        articles.clear();
                    }
                    articles = response.body().getArticle();
                    adapter = new Adapter(articles, MainActivity.this);
                    recyclerView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();

                    initListener();

                    topHeadLine.setVisibility(View.VISIBLE);
                    swipeRefreshLayout.setRefreshing(false);

                } else if (response.raw().cacheResponse() != null &&
                        response.raw().networkResponse() == null) {
                    Log.d(TAG, "onResponse: response is from CACHE");
                } else {
                    topHeadLine.setVisibility(View.INVISIBLE);
                    swipeRefreshLayout.setRefreshing(false);

                    String errorCode;
                    switch (response.code()) {
                        case 404:
                            errorCode = "404 not found";
                            break;
                        case 500:
                            errorCode = "500 server broken";
                            break;
                        default:
                            errorCode = "unknown error";
                            break;
                    }
                    showErrorMessage(
                            R.drawable.ic_mood_bad,
                            "No Result",
                            "Please try again!\n" +
                                    errorCode);
                }
            }

            @Override
            public void onFailure(Call<News> call, Throwable t) {
                Log.d(TAG, "onFailure");
                topHeadLine.setVisibility(View.INVISIBLE);
                swipeRefreshLayout.setRefreshing(false);

                showErrorMessage(
                        R.drawable.ic_mood_bad,
                        "Oops...",
                        "Network failure, please try again!\n" +
                                t.toString());
            }
        });
    }


    private void initListener() {
        Log.d(TAG, "initListener");
        adapter.setOnItemClickListener(new Adapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                ImageView imageView = view.findViewById(R.id.image);
                Intent intent = new Intent(MainActivity.this, NewsDetailActivity.class);

                Article article = articles.get(position);
                intent.putExtra("url", article.getUrl());
                intent.putExtra("title", article.getTitle());
                intent.putExtra("img", article.getUrlToImage());
                intent.putExtra("date", article.getPublishedAt());
                intent.putExtra("source", article.getSource().getName());
                intent.putExtra("author", article.getAuthor());

                Pair<View, String> pair = Pair.create(imageView, ViewCompat.getTransitionName(imageView));
                ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        MainActivity.this, pair
                );

                startActivity(intent, optionsCompat.toBundle());
            }
        });
    }

    private void initPaginationListener() {
        Log.d(TAG, "initPaginationListener");
        paginationAdapter.setOnItemClickListener(new PaginationAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                ImageView imageView = view.findViewById(R.id.image);
                Intent intent = new Intent(MainActivity.this, NewsDetailActivity.class);

                Article article = articles.get(position);
                intent.putExtra("url", article.getUrl());
                intent.putExtra("title", article.getTitle());
                intent.putExtra("img", article.getUrlToImage());
                intent.putExtra("date", article.getPublishedAt());
                intent.putExtra("source", article.getSource().getName());
                intent.putExtra("author", article.getAuthor());

                Pair<View, String> pair = Pair.create(imageView, ViewCompat.getTransitionName(imageView));
                ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        MainActivity.this, pair
                );

                startActivity(intent, optionsCompat.toBundle());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        MenuItem searchMenuItem = menu.findItem(R.id.action_search);

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setQueryHint("Search latest News...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query.length() > 2) {
                    onLoadingSwipeRefresh(query);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        searchMenuItem.getIcon().setVisible(false, false);

        return true;
    }

    @Override
    public void onRefresh() {
        Log.d(TAG, "onRefresh");
        currentPage = PAGE_START;
        isLastPage = false;
        adapter.clear();
        loadJson("");
//        paginationAdapter.clear();
//        loadFirstNews("");

    }

    private void onLoadingSwipeRefresh(final String keyword) {
        Log.d(TAG, "onLoadingSwipeRefresh");
        swipeRefreshLayout.post(
                new Runnable() {
                    @Override
                    public void run() {
                        loadJson(keyword);
//                        loadFirstNews(keyword);
                    }
                }
        );
    }

    private void showErrorMessage(int imageView, String title, String message) {
        Log.d(TAG, "showErrorMessage");
        if (errorLayout.getVisibility() == View.GONE) {
            errorLayout.setVisibility(View.VISIBLE);
        }

        errorImageView.setImageResource(imageView);
        errorTitle.setText(title);
        errorMessage.setText(message);

        btnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLoadingSwipeRefresh("");
            }
        });
    }
}
