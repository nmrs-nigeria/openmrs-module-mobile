
package pbsprocessor.api.retrofit;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.migcomponents.migbase64.Base64;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import pbsprocessor.OpenMRS;
import pbsprocessor.util.ApplicationConstants;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RestServiceBuilder {

    protected static final OpenMRS mOpenMRS = OpenMRS.getInstance();

    private static String API_BASE_URL = OpenMRS.getInstance().getUrlBase()+ ApplicationConstants.REST_ENDPOINT;

    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

    private static Retrofit.Builder builder;

    static {
        builder =
                new Retrofit.Builder()
                        .baseUrl(API_BASE_URL)
                        .addConverterFactory(buildGsonConverter())
                        .client((httpClient).build());
    }

    public static <S> S createService(Class<S> serviceClass, String username, String password){
        if (username != null && password != null) {
            String credentials = username + ":" + password;
            final String basic =
                    "Basic " + Base64.encodeToString(credentials.getBytes() , false );
            httpClient.addInterceptor(chain -> {
                Request original = chain.request();

                Request.Builder requestBuilder = original.newBuilder()
                        .header("Authorization", basic)
                        .header("Accept", "application/json")
                        .method(original.method(), original.body());


                Request request = requestBuilder.build();
                return chain.proceed(request);
            });
//            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
//            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
//            httpClient.addInterceptor(new SnooperInterceptor());
//            httpClient.addInterceptor(logging);
        }
        OkHttpClient client = httpClient.build();
        Retrofit retrofit = builder.client(client).build();
        return retrofit.create(serviceClass);
    }

    public static <S> S createService(Class<S> serviceClass) {
        String username=OpenMRS.getInstance().getUser();
        String password=OpenMRS.getInstance().getPassword();
        return createService(serviceClass, username, password);
    }

    private static GsonConverterFactory buildGsonConverter() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson myGson = gsonBuilder
               // .excludeFieldsWithoutExposeAnnotation()
               // .registerTypeHierarchyAdapter(Resource.class, new ResourceSerializer())
               // .registerTypeHierarchyAdapter(Observation.class, new ObservationDeserializer())
                .create();

        return GsonConverterFactory.create(myGson);
    }

    public static <S> S createServiceForPatientIdentifier(Class<S> clazz){
        return new Retrofit.Builder()
                .baseUrl(OpenMRS.getInstance().getUrlBase() + '/')
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(clazz);
    }



}