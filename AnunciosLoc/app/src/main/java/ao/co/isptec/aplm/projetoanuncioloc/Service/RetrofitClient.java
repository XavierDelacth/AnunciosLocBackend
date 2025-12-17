package ao.co.isptec.aplm.projetoanuncioloc.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.time.LocalDateTime;

import ao.co.isptec.aplm.projetoanuncioloc.Adapters.LocalDateTimeAdapter;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;






public class RetrofitClient {

    public static final String BASE_URL = "http://192.168.110.157:8081";
    private static Retrofit retrofit;

    public static ApiService getApiService(Context context) {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(context))
                .build();

        // Configurar Gson com adaptador para LocalDateTime
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();

        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit.create(ApiService.class);
    }
}

class AuthInterceptor implements Interceptor {
    private Context context;

    public AuthInterceptor(Context context) {
        this.context = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        SharedPreferences prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        String jwt = prefs.getString("jwt", null);
        Log.d("AuthInterceptor", "JWT encontrado: " + (jwt != null ? jwt.substring(0, 20) + "..." : "NENHUM JWT SALVO!"));  // Log parcial para segurança
        if (jwt != null) {
            request = request.newBuilder()
                    .addHeader("Authorization", "Bearer " + jwt)
                    .build();
            Log.d("AuthInterceptor", "Header adicionado: Authorization Bearer [token]");
        } else {
            Log.e("AuthInterceptor", "Sem JWT - Request sem auth! Vai dar 403.");
        }
        return chain.proceed(request);
    }

}