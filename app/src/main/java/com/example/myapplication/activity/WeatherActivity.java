package com.example.myapplication.activity;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.example.myapplication.R;
import com.example.myapplication.entity.Weather;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private AMapLocationClient locationClient = null;
    private AMapLocationClientOption locationOption = null;
    private static final String API_KEY = "dec352e11860f2d040cb613b4de6d80b";
    private static  String CITY = "110101";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        // 初始化定位
        try {
            locationClient = new AMapLocationClient(this.getApplicationContext());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        locationOption = new AMapLocationClientOption();

        // 设置定位模式为低功耗模式
        locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Battery_Saving);
        locationOption.setOnceLocation(true);

        // 设置定位参数
        locationClient.setLocationOption(locationOption);

        // 启动定位
        locationClient.startLocation();
        // 设置定位监听
        locationClient.setLocationListener(new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
                if (aMapLocation != null) {
                    if (aMapLocation.getErrorCode() == 0) {
                        // 定位成功回调信息，设置相关消息
                        CITY = aMapLocation.getCityCode(); // 获取城市编码
                        getWeatherInfo(CITY);
                    }
                }
            }
        });
    }
    private void getWeatherInfo(String cityCode) {
        OkHttpClient client = new OkHttpClient();

        String url = "https://restapi.amap.com/v3/weather/weatherInfo?city=" + cityCode + "&key=" + API_KEY;

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    parseJSONWithGSON(responseData);
                }
            }
        });
    }
    private void parseJSONWithGSON(String jsonData) {
        Gson gson = new Gson();
        Weather weather = gson.fromJson(jsonData, Weather.class);

        TextView tvCity = findViewById(R.id.tv_city);
        TextView tvWeather = findViewById(R.id.tv_weather);
        TextView tvTemperature = findViewById(R.id.tv_temperature);
        TextView tvWind = findViewById(R.id.tv_wind);
        TextView tvHumidity = findViewById(R.id.tv_humidity);
        TextView tvReportTime = findViewById(R.id.tv_report_time);

        tvCity.setText("城市: " +  weather.getLives().get(0).getProvince() +" "+weather.getLives().get(0).getCity());
        tvWeather.setText("天气: " + weather.getLives().get(0).getWeather());
        tvTemperature.setText("温度: " + weather.getLives().get(0).getTemperature());
        tvWind.setText("风向和风力: " + weather.getLives().get(0).getWinddirection() + " " + weather.getLives().get(0).getWindpower());
        tvHumidity.setText("湿度: " + weather.getLives().get(0).getHumidity());
        tvReportTime.setText("预测时间: " + weather.getLives().get(0).getReporttime());
    }

}