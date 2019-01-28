package com.example.demos.demo_automl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public class Network {

    private static long READ_TIMEOUT = 1L;
    private static long WRITE_TIMEOUT = 1L;
    private static long CONNECTION_TIMEOUT = 1L;

    private String baseUrl;
    private Boolean enableLog;

    private Gson gson;
    private OkHttpClient okHttpClient;

    public Network(String baseUrl, Boolean enableLog) {
        this.baseUrl = baseUrl;
        this.enableLog = enableLog;
        gson = new GsonBuilder().setLenient().create();
        okHttpClient = new OkHttpClient();
        init();
    }

    private void init() {
        OkHttpClient.Builder okHttpBuilder = new OkHttpClient.Builder()
                .readTimeout(READ_TIMEOUT, TimeUnit.MINUTES)
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.MINUTES)
                .connectTimeout(CONNECTION_TIMEOUT, TimeUnit.MINUTES);

        if (enableLog) {
            HttpLoggingInterceptor logger = new HttpLoggingInterceptor();
            logger.setLevel(HttpLoggingInterceptor.Level.BODY);
            okHttpBuilder.addInterceptor(logger);
        }

        Interceptor headerInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                okhttp3.Headers headers = request.headers().newBuilder().add("Authorization", "Bearer $(gcloud auth application-default print-access-token)").build();
                request = request.newBuilder().headers(headers).build();
                return chain.proceed(request);
            }
        };
        okHttpBuilder.addInterceptor(headerInterceptor);

        okHttpClient = okHttpBuilder.build();
    }

    Retrofit getRetroFitClient() {
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

    static class ModelImage {
        @SerializedName("imageBytes")
        String imageBytes;

        public ModelImage(String imageBytes) {
            this.imageBytes = imageBytes;
        }
    }

    static class ModelRequestBody {
        @SerializedName("payload")
        PayloadRequest payload;

        public ModelRequestBody(PayloadRequest payload) {
            this.payload = payload;
        }

    }

    static class PayloadRequest {
        @SerializedName("image")
        ModelImage modelImage;

        public PayloadRequest(ModelImage modelImage) {
            this.modelImage = modelImage;
        }
    }


    public interface Endpoint {
        @Headers("Content-Type: application/json")
        @POST("projects/doggzamapp/locations/us-central1/models/ICN2428774777069952171:predict")
        Call<PayloadRequest> classifyImage(@Body ModelRequestBody modelRequestBody);
    }

}
