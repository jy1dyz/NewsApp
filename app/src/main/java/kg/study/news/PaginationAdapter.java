package kg.study.news;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import kg.study.news.models.Article;

public class PaginationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Article> articles;
    private Context context;
    private OnItemClickListener onItemClickListener;
    private static final int LOADING = 0;
    private static final int ITEM = 1;
    private boolean isLoadingAdded = false;

    public PaginationAdapter(Context context) {
        this.context = context;
        articles = new ArrayList<>();
    }

    public void setArticles(List<Article> articles) {
        this.articles = articles;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case ITEM:
                View viewItem = inflater.inflate(R.layout.item, parent, false);
                viewHolder = new ArticleViewHolder(viewItem, onItemClickListener);
                break;
            case LOADING:
                View viewLoading = inflater.inflate(R.layout.loading, parent, false);
                viewHolder = new LoadingViewHolder(viewLoading);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        switch (getItemViewType(position)) {
            case ITEM:
                ArticleViewHolder mHolder = (ArticleViewHolder) holder;
                Article article = articles.get(position);
                RequestOptions requestOptions = new RequestOptions();
                requestOptions.placeholder(Utils.getRandomDrawbleColor());
                requestOptions.error(Utils.getRandomDrawbleColor());
                requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL);
                requestOptions.centerCrop();

                Glide.with(context)
                        .load(article.getUrlToImage())
                        .apply(requestOptions)
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                mHolder.progressBar.setVisibility(View.GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                mHolder.progressBar.setVisibility(View.GONE);
                                return false;
                            }
                        })
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(mHolder.imageView);
                mHolder.title.setText(article.getTitle());
                mHolder.desc.setText(article.getDescription());
                mHolder.source.setText(article.getSource().getName());
                mHolder.time.setText(" \u2022" + Utils.DateToTimeFormat(article.getPublishedAt()));
                mHolder.publishedAt.setText(Utils.DateFormat(article.getPublishedAt()));
                mHolder.author.setText(article.getAuthor());
                break;

            case LOADING:
                LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
                loadingViewHolder.progressBar.setVisibility(View.VISIBLE);
                break;
        }
    }

    public void clear() {
        articles.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return articles == null ? 0 : articles.size();
    }

    @Override
    public int getItemViewType(int position) {
        return (position == articles.size() - 1 && isLoadingAdded) ? LOADING : ITEM;
    }

    public void addLoadingFooter() {
        isLoadingAdded = true;
        add(new Article());
    }

    public void removeLoadingFooter() {
        isLoadingAdded = false;

        int position = articles.size() - 1;
        Article result = getItem(position);

        if (result != null) {
            articles.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void add(Article article) {
        articles.add(article);
        notifyItemInserted(articles.size() - 1);
    }

    public void addAll(List<Article> articlesResults) {
        for (Article result : articlesResults) {
            add(result);
        }
    }

    public Article getItem(int position) {
        return articles.get(position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }


    public class ArticleViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView title, desc, author, publishedAt, source, time;
        ImageView imageView;
        ProgressBar progressBar;
        OnItemClickListener onItemClickListener;

        public ArticleViewHolder(View itemView, OnItemClickListener onItemClickListener) {
            super(itemView);

            itemView.setOnClickListener(this);
            title = itemView.findViewById(R.id.title);
            desc = itemView.findViewById(R.id.desc);
            source = itemView.findViewById(R.id.source);
            author = itemView.findViewById(R.id.tv_author);
            publishedAt = itemView.findViewById(R.id.publishedAt);
            time = itemView.findViewById(R.id.time);
            imageView = itemView.findViewById(R.id.image);
            progressBar = itemView.findViewById(R.id.progress_load_photo);

            this.onItemClickListener = onItemClickListener;

        }

        @Override
        public void onClick(View v) {
            onItemClickListener.onItemClick(v, getAdapterPosition());
        }
    }

    public class LoadingViewHolder extends RecyclerView.ViewHolder {

        private ProgressBar progressBar;

        public LoadingViewHolder(View itemView) {
            super(itemView);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar);

        }
    }
}
