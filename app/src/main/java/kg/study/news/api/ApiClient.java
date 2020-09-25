package kg.study.news.api;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    public static final String BASE_URL = "https://newsapi.org/v2/";
    public static Retrofit retrofit;

    public static Retrofit getApiClient() {
        if(retrofit == null) {
           retrofit = new Retrofit.Builder().baseUrl(BASE_URL)
//                   .client(getUnsafeOkHttpClient().build())
                   .addConverterFactory(GsonConverterFactory.create())
                   .build();
        }
        return retrofit;
    }

}
