package com.campusgo.data.remote;

import androidx.annotation.NonNull;

import com.campusgo.core.config.FeatureFlags;
import com.campusgo.core.session.SessionManager;
import com.campusgo.data.remote.api.AddressApi;
import com.campusgo.data.remote.api.AiApi;
import com.campusgo.data.remote.api.AuthApi;
import com.campusgo.data.remote.api.ChatApi;
import com.campusgo.data.remote.api.DashboardApi;
import com.campusgo.data.remote.api.HeatmapApi;
import com.campusgo.data.remote.api.NotificationApi;
import com.campusgo.data.remote.api.PointsApi;
import com.campusgo.data.remote.api.TaskApi;
import com.campusgo.data.remote.api.UserApi;
import com.campusgo.data.remote.api.VoucherApi;
import com.campusgo.data.remote.api.WalletApi;
import com.campusgo.domain.model.TaskCategory;
import com.campusgo.domain.model.TaskMode;
import com.campusgo.domain.model.TaskStatus;
import com.campusgo.domain.model.UserRole;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Retrofit 单例入口。
 */
public final class RetrofitClient {

    private static RetrofitClient instance;

    private final AuthApi authApi;
    private final UserApi userApi;
    private final WalletApi walletApi;
    private final TaskApi taskApi;
    private final AddressApi addressApi;
    private final ChatApi chatApi;
    private final NotificationApi notificationApi;
    private final AuthRemoteDataSource authRemote;
    private final TaskRemoteDataSource taskRemote;
    private final UserRemoteDataSource userRemote;
    private final WalletRemoteDataSource walletRemote;
    private final AddressRemoteDataSource addressRemote;
    private final ChatRemoteDataSource chatRemote;
    private final NotificationRemoteDataSource notificationRemote;
    private final PointsApi pointsApi;
    private final DashboardApi dashboardApi;
    private final HeatmapApi heatmapApi;
    private final VoucherApi voucherApi;
    private final AiApi aiApi;
    private final PointsRemoteDataSource pointsRemote;
    private final DashboardRemoteDataSource dashboardRemote;
    private final HeatmapRemoteDataSource heatmapRemote;
    private final VoucherRemoteDataSource voucherRemote;

    private RetrofitClient(@NonNull SessionManager sessionManager) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(FeatureFlags.DEBUG
                ? HttpLoggingInterceptor.Level.BODY
                : HttpLoggingInterceptor.Level.NONE);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(new AuthInterceptor(sessionManager))
                .addInterceptor(logging)
                .build();

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(TaskStatus.class, new EnumTypeAdapter<>(TaskStatus.class))
                .registerTypeAdapter(TaskMode.class, new EnumTypeAdapter<>(TaskMode.class))
                .registerTypeAdapter(TaskCategory.class, new EnumTypeAdapter<>(TaskCategory.class))
                .registerTypeAdapter(UserRole.class, new EnumTypeAdapter<>(UserRole.class))
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(FeatureFlags.API_BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        authApi = retrofit.create(AuthApi.class);
        userApi = retrofit.create(UserApi.class);
        walletApi = retrofit.create(WalletApi.class);
        taskApi = retrofit.create(TaskApi.class);
        addressApi = retrofit.create(AddressApi.class);
        chatApi = retrofit.create(ChatApi.class);
        notificationApi = retrofit.create(NotificationApi.class);
        pointsApi = retrofit.create(PointsApi.class);
        dashboardApi = retrofit.create(DashboardApi.class);
        heatmapApi = retrofit.create(HeatmapApi.class);
        voucherApi = retrofit.create(VoucherApi.class);
        aiApi = retrofit.create(AiApi.class);
        authRemote = new AuthRemoteDataSource(authApi, pointsApi, sessionManager);
        taskRemote = new TaskRemoteDataSource(taskApi);
        userRemote = new UserRemoteDataSource(userApi, sessionManager);
        walletRemote = new WalletRemoteDataSource(walletApi, sessionManager);
        addressRemote = new AddressRemoteDataSource(addressApi);
        chatRemote = new ChatRemoteDataSource(chatApi);
        notificationRemote = new NotificationRemoteDataSource(notificationApi);
        pointsRemote = new PointsRemoteDataSource(pointsApi, sessionManager);
        dashboardRemote = new DashboardRemoteDataSource(dashboardApi);
        heatmapRemote = new HeatmapRemoteDataSource(heatmapApi);
        voucherRemote = new VoucherRemoteDataSource(voucherApi);
    }

    public static void init(@NonNull SessionManager sessionManager) {
        if (instance == null) {
            instance = new RetrofitClient(sessionManager);
        }
    }

    @NonNull
    public static RetrofitClient get() {
        if (instance == null) {
            throw new IllegalStateException("RetrofitClient not initialized");
        }
        return instance;
    }

    @NonNull
    public AuthApi authApi() {
        return authApi;
    }

    @NonNull
    public UserApi userApi() {
        return userApi;
    }

    @NonNull
    public WalletApi walletApi() {
        return walletApi;
    }

    @NonNull
    public TaskApi taskApi() {
        return taskApi;
    }

    @NonNull
    public AddressApi addressApi() {
        return addressApi;
    }

    @NonNull
    public ChatApi chatApi() {
        return chatApi;
    }

    @NonNull
    public AuthRemoteDataSource authRemote() {
        return authRemote;
    }

    @NonNull
    public TaskRemoteDataSource taskRemote() {
        return taskRemote;
    }

    @NonNull
    public UserRemoteDataSource userRemote() {
        return userRemote;
    }

    @NonNull
    public WalletRemoteDataSource walletRemote() {
        return walletRemote;
    }

    @NonNull
    public AddressRemoteDataSource addressRemote() {
        return addressRemote;
    }

    @NonNull
    public ChatRemoteDataSource chatRemote() {
        return chatRemote;
    }

    @NonNull
    public NotificationRemoteDataSource notificationRemote() {
        return notificationRemote;
    }

    @NonNull
    public PointsApi pointsApi() {
        return pointsApi;
    }

    @NonNull
    public DashboardApi dashboardApi() {
        return dashboardApi;
    }

    @NonNull
    public HeatmapApi heatmapApi() {
        return heatmapApi;
    }

    @NonNull
    public PointsRemoteDataSource pointsRemote() {
        return pointsRemote;
    }

    @NonNull
    public DashboardRemoteDataSource dashboardRemote() {
        return dashboardRemote;
    }

    @NonNull
    public HeatmapRemoteDataSource heatmapRemote() {
        return heatmapRemote;
    }

    @NonNull
    public VoucherApi voucherApi() {
        return voucherApi;
    }

    @NonNull
    public VoucherRemoteDataSource voucherRemote() {
        return voucherRemote;
    }

    @NonNull
    public AiApi aiApi() {
        return aiApi;
    }
}
