package kg.study.news.api;

import kg.study.news.models.News;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiInterface {

    @GET("top-headlines")
    Call<News> getNews(
            @Query("country") String country,
            @Query("apiKey") String apiKey
    );

    @GET("everything")
    Call<News> getNewssearch(
            @Query("q") String keyword,
//            @Query("sortBy") String sortBy,
            @Query("apiKey") String apiKey
    );


}
