package zgas.supervisor.retrofit;



import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import zgas.supervisor.models.FCMBody;
import zgas.supervisor.models.FCMResponse;

public interface IFCMApi {

    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAG0gEj1U:APA91bEKTLJ3GPV8mxeXO4We4yyW7qWqD9iIsuuLXYyT0rYPkxdMhPR4u87RPWqJEy4GG5-BkxsU3wUdhW_Y7VF0vPIHUvbNWg1x6PU3jJnL2ahozH5R7PmLVhqk3RiHUy38HZejsPOC"
    })
    @POST("fcm/send")
    Call<FCMResponse> send(@Body FCMBody body);

}
