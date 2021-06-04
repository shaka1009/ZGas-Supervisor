package zgas.supervisor.providers;


import retrofit2.Call;
import zgas.supervisor.models.FCMBody;
import zgas.supervisor.models.FCMResponse;
import zgas.supervisor.retrofit.IFCMApi;
import zgas.supervisor.retrofit.RetrofitClient;

public class NotificationProvider {

    private String url = "https://fcm.googleapis.com";

    public NotificationProvider() {
    }

    public Call<FCMResponse> sendNotification(FCMBody body) {
        return RetrofitClient.getClientObject(url).create(IFCMApi.class).send(body);
    }
}
