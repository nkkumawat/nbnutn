package me.nkkumawat.chatzzz.Connection;

import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by sonu on 20/1/18.
 */

public class Connection {
    private static final MediaType jsonType = MediaType.parse("application/json; charset=utf-8");
    private static final OkHttpClient client = new OkHttpClient();
    public static void post(final String Url ,final String JsonParameters ,final boolean isJsonResponse ,final ConnectionResponse connectionResponse) {
        new Thread() {
            @Override
            public void run() {
                try {
                    JSONObject jsonObject = new JSONObject(JsonParameters);
                    RequestBody body = RequestBody.create(jsonType, jsonObject.toString());
                    Request request = new Request.Builder()
                            .url(Url)
                            .post(body)
                            .build();
                    Response response = client.newCall(request).execute();
                    if (isJsonResponse) {
                        connectionResponse.JsonResponse(new JSONObject(response.body().string()), true);
                    } else {
                        connectionResponse.StringResponse(response.body().string(), true);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (isJsonResponse) {
                        connectionResponse.JsonResponse(null, false);
                    } else {
                        connectionResponse.StringResponse(null, false);
                    }
                }
            }
        }.start();
    }

    public static abstract class ConnectionResponse {
        public void JsonResponse(JSONObject object, boolean Success) {
        }
        public void StringResponse(String object, boolean Success) {
        }
    }
}
