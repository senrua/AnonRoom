package com.example.myapplication;

import static com.example.myapplication.util.ImageUtil.saveAvatarImage;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Pair;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.example.myapplication.entity.Contact;
import com.example.myapplication.entity.ContactMsg;
import com.example.myapplication.entity.Location;
import com.example.myapplication.entity.NewFriend;
import com.example.myapplication.entity.NewFriendMsg;
import com.example.myapplication.entity.Room;
import com.example.myapplication.entity.UserInfo;
import com.example.myapplication.util.ImageUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class NetworkService extends Service {
    private final IBinder binder = new LocalBinder();
    private final HttpRequest httpRequest = new HttpRequest();
    private Handler handler = new Handler(Looper.getMainLooper());

    public class LocalBinder extends Binder {
        public NetworkService getService() {
            return NetworkService.this;
        }
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public String getAvatar(String username) {
        String avatarImagePath = ImageUtil.getAvatarImagePath(this, username);
        if (avatarImagePath == null)
            try {
                String requestUrl = "/user/avatar";
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                // 创建请求参数
                Map<String, String> params = new HashMap<>();
                params.put("username", username);
                // 将请求参数转换为JSON字符串
                String jsonParams = new Gson().toJson(params);
                // 发送请求
                final String result = httpRequest.sendRequest(requestUrl, "POST", headers, jsonParams);

                JsonObject jsonObject = JsonParser.parseString(result).getAsJsonObject();
                JsonObject dataObject = jsonObject.getAsJsonObject("data");
                String avatarPicture = dataObject.get("avatar_picture").getAsString();

                Bitmap avatarBitmap = ImageUtil.base64ToImage(avatarPicture);
                saveAvatarImage(this, avatarBitmap, username);
                return ImageUtil.getAvatarImagePath(this, username);
            } catch (final IOException e) {
                //加载失败
                return "";
            }
        return avatarImagePath;
    }

    /***
     * 获取个人信息
     *
     * @param username
     */
    public void getPersonalInfo(String username) {
        new Thread(() -> {
            try {
                String requestUrl = "/personal/info";
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Bearer your_token");

                // 创建请求参数
                Map<String, String> params = new HashMap<>();
                params.put("username", username);

                // 将请求参数转换为JSON字符串
                String jsonParams = new Gson().toJson(params);

                // 发送请求
                final String result = httpRequest.sendRequest(requestUrl, "POST", headers, jsonParams);

                JsonObject jsonObject = JsonParser.parseString(result).getAsJsonObject();
                JsonObject dataObject = jsonObject.getAsJsonObject("data");
                Gson gson = new Gson();
                UserInfo userInfo = gson.fromJson(dataObject, UserInfo.class);
                String avatarPath = getAvatar(username);
                // 在主线程中返回结果
                Message message = handler.obtainMessage();
                message.what = 1; // 设置消息的标识
                message.obj = new Pair<>(userInfo, avatarPath); // 设置消息的内容
                handler.sendMessage(message);
            } catch (final IOException e) {
                // 在主线程中返回错误
                Message message = handler.obtainMessage();
                message.what = 2; // 设置消息的标识
                message.obj = e; // 设置消息的内容
                handler.sendMessage(message);
            }
        }).start();
    }

    /***
     * 编辑个人信息
     *
     * @param username
     * @param nickname
     * @param avatar_picture
     * @param summary
     */
    public void changePersonalInfo(String username, String nickname, String avatar_picture, String summary) {
        new Thread(() -> {
            try {
                String requestUrl = "/personal/change";
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");

                // 创建请求参数
                Map<String, String> params = new HashMap<>();
                params.put("username", username);
                params.put("nickname", nickname);
                params.put("avatar_picture", avatar_picture);
                params.put("summary", summary);

                // 将请求参数转换为JSON字符串
                String jsonParams = new Gson().toJson(params);

                // 发送请求
                final String result = httpRequest.sendRequest(requestUrl, "POST", headers, jsonParams);

                // 在主线程中返回结果
                Message message = handler.obtainMessage();
                message.what = 1; // 设置消息的标识
                message.obj = result; // 设置消息的内容
                handler.sendMessage(message);
            } catch (final IOException e) {
                // 在主线程中返回错误
                Message message = handler.obtainMessage();
                message.what = 2; // 设置消息的标识
                message.obj = e; // 设置消息的内容
                handler.sendMessage(message);
            }
        }).start();
    }
    public void logout(String username) {
        new Thread(() -> {
            try {
                String requestUrl = "/personal/logout";
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");

                // 创建请求参数
                Map<String, String> params = new HashMap<>();
                params.put("username", username);

                // 将请求参数转换为JSON字符串
                String jsonParams = new Gson().toJson(params);

                // 发送请求
                final String result = httpRequest.sendRequest(requestUrl, "POST", headers, jsonParams);

                // 在主线程中返回结果
                Message message = handler.obtainMessage();
                message.what = 3; // 设置消息的标识
                message.obj = result; // 设置消息的内容
                handler.sendMessage(message);
            } catch (final IOException e) {
                // 在主线程中返回错误
                Message message = handler.obtainMessage();
                message.what = 4; // 设置消息的标识
                message.obj = e; // 设置消息的内容
                handler.sendMessage(message);
            }
        }).start();
    }

    /***
     * 登录
     *
     * @param username
     * @param password
     */
    public void login(String username, String password,String email,String code) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String requestUrl = "/user/login";
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");

                    // 创建请求参数
                    Map<String, String> params = new HashMap<>();
                    params.put("username", username);
                    params.put("password", password);
                    params.put("mailbox",email);
                    params.put("mailbox_check",code);


                    // 将请求参数转换为JSON字符串
                    String jsonParams = new Gson().toJson(params);

                    // 发送请求
                    final String result = httpRequest.sendRequest(requestUrl, "POST", headers, jsonParams);
                    // 保存用户的登录状态
                    SharedPreferences sharedPreferences = getSharedPreferences("MyApp", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("isLogin", true);
                    editor.putString("username", username);
                    editor.putString("password", password);
                    editor.apply();

                    // 在主线程中返回结果
                    Message message = handler.obtainMessage();
                    message.what = 3; // 设置消息的标识
                    // 创建一个 HashMap 来存储用户名和密码
                    HashMap<String, String> userData = new HashMap<>();
                    userData.put("username", username);
                    userData.put("password", password);
                    message.obj = userData; // 设置消息的内容
                    handler.sendMessage(message);

                    // 登录成功后，启动心跳包
                    if (scheduledExecutorService == null) {
                        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
                        scheduledExecutorService.scheduleAtFixedRate(checkLoginStatusTask, 0, 5, TimeUnit.MINUTES);
                    }
                } catch (final IOException e) {
                    // 在主线程中返回错误
                    Message message = handler.obtainMessage();
                    message.what = 4; // 设置消息的标识
                    message.obj = e; // 设置消息的内容
                    handler.sendMessage(message);
                }
            }
        }).start();
    }

    /***
     * 请求邮箱验证码
     *
     * @param email
     */
    public void requestEmailVerificationCode(String email) {
        new Thread(() -> {
            try {
                String requestUrl = "/mailbox/appear";
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");

                // 创建请求参数
                Map<String, String> params = new HashMap<>();
                params.put("mailbox", email);

                // 将请求参数转换为JSON字符串
                String jsonParams = new Gson().toJson(params);

                // 发送请求
                final String result = httpRequest.sendRequest(requestUrl, "POST", headers, jsonParams);

                // 在主线程中返回结果
                Message message = handler.obtainMessage();
                message.what = 1; // 设置消息的标识
                message.obj = email; // 设置消息的内容
                handler.sendMessage(message);
            } catch (final IOException e) {
                // 在主线程中返回错误
                Message message = handler.obtainMessage();
                message.what = 2; // 设置消息的标识
                message.obj = e; // 设置消息的内容
                handler.sendMessage(message);
            }
        }).start();
    }

    /***
     * 注册
     *
     * @param created_time
     * @param username
     * @param password
     * @param nickname
     * @param avatar_picture
     * @param mailbox
     * @param mailbox_check
     */
    public void register(String created_time, String username, String password, String nickname, String avatar_picture, String mailbox, String mailbox_check) {
        new Thread(() -> {
            try {
                String requestUrl = "/user/register";
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");

                // 创建请求参数
                Map<String, String> params = new HashMap<>();
                params.put("created_time", created_time);
                params.put("username", username);
                params.put("password", password);
                params.put("nickname", nickname);
                params.put("avatar_picture", avatar_picture);
                params.put("mailbox", mailbox);
                params.put("mailbox_check", mailbox_check);

                // 将请求参数转换为JSON字符串
                String jsonParams = new Gson().toJson(params);

                // 发送请求
                final String result = httpRequest.sendRequest(requestUrl, "POST", headers, jsonParams);

                // 在主线程中返回结果
                Message message = handler.obtainMessage();
                message.what = 1; // 设置消息的标识
                message.obj = result; // 设置消息的内容
                handler.sendMessage(message);
            } catch (final IOException e) {
                // 在主线程中返回错误
                Message message = handler.obtainMessage();
                message.what = 2; // 设置消息的标识
                message.obj = e; // 设置消息的内容
                handler.sendMessage(message);
            }
        }).start();
    }

    /***
     * 获取好友信息
     *
     * @param friendUsername
     */
    public void getFriendInfo(String friendUsername) {
        new Thread(() -> {
            try {
                String requestUrl = "/friend/info"; // 你的服务器 API 地址
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");

                // 创建请求参数
                Map<String, String> params = new HashMap<>();
                params.put("username", friendUsername);

                // 将请求参数转换为JSON字符串
                String jsonParams = new Gson().toJson(params);

                // 发送请求
                final String result = httpRequest.sendRequest(requestUrl, "POST", headers, jsonParams);

                JsonObject jsonObject = JsonParser.parseString(result).getAsJsonObject();
                JsonObject dataObject = jsonObject.getAsJsonObject("data");
                Gson gson = new Gson();
                UserInfo userInfo = gson.fromJson(dataObject, UserInfo.class);
                String avatarPath = getAvatar(friendUsername);
                // 在主线程中返回结果
                Message message = handler.obtainMessage();
                message.what = 1; // 设置消息的标识
                message.obj = new Pair<>(userInfo, avatarPath); // 设置消息的内容
                handler.sendMessage(message);
            } catch (final IOException e) {
                // 在主线程中返回错误
                Message message = handler.obtainMessage();
                message.what = 2; // 设置消息的标识
                message.obj = e; // 设置消息的内容
                handler.sendMessage(message);
            }
        }).start();
    }

    /***
     * 删除好友
     * @param username
     * @param friendUsername
     */
    public void deleteFriend(String username, String friendUsername) {
        new Thread(() -> {
            try {
                String requestUrl = "/friend/delete";
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");

                // 创建请求参数
                Map<String, String> params = new HashMap<>();
                params.put("username", username);
                params.put("target_username", friendUsername);

                // 将请求参数转换为JSON字符串
                String jsonParams = new Gson().toJson(params);

                // 发送请求
                final String result = httpRequest.sendRequest(requestUrl, "POST", headers, jsonParams);

                // 在主线程中返回结果
                Message message = handler.obtainMessage();
                message.what = 3;
                message.obj = result;
                handler.sendMessage(message);
            } catch (final IOException e) {
                Message message = handler.obtainMessage();
                message.what = 4;
                message.obj = e;
                handler.sendMessage(message);
            }
        }).start();
    }

    /***
     * 获取消息列表
     * @param username
     */
    public void getUserHomeList(String username) {
        new Thread(() -> {
            try {
                String requestUrl = "/user/home/list";
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");

                // 创建请求参数
                Map<String, String> params = new HashMap<>();
                params.put("username", username);

                // 将请求参数转换为JSON字符串
                String jsonParams = new Gson().toJson(params);

                // 发送请求
                final String result = httpRequest.sendRequest(requestUrl, "POST", headers, jsonParams);

                JsonObject jsonObject = JsonParser.parseString(result).getAsJsonObject();
                JsonArray dataArray = jsonObject.getAsJsonArray("data");
                Gson gson = new Gson();
                Type listType = new TypeToken<List<Room>>() {
                }.getType();
                List<Room> rooms = gson.fromJson(dataArray, listType);
                // 在主线程中返回结果
                Message message = handler.obtainMessage();
                message.what = 1; // 设置消息的标识
                message.obj = rooms; // 设置消息的内容
                handler.sendMessage(message);
            } catch (final IOException e) {
                // 在主线程中返回错误
                Message message = handler.obtainMessage();
                message.what = 2; // 设置消息的标识
                message.obj = e; // 设置消息的内容
                handler.sendMessage(message);
            }
        }).start();
    }

    /***
     * 获得好友列表
     * @param username
     */
    public void getUserFriendList(String username) {
        new Thread(() -> {
            try {
                String requestUrl = "/user/connection/list";
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");

                // 创建请求参数
                Map<String, String> params = new HashMap<>();
                params.put("username", username);

                // 将请求参数转换为JSON字符串
                String jsonParams = new Gson().toJson(params);

                // 发送请求
                final String result = httpRequest.sendRequest(requestUrl, "POST", headers, jsonParams);

                JsonObject jsonObject = JsonParser.parseString(result).getAsJsonObject();
                JsonArray dataArray = jsonObject.getAsJsonArray("data");
                Gson gson = new Gson();
                Type listType = new TypeToken<List<ContactMsg>>() {
                }.getType();
                List<ContactMsg> contactMsgs = gson.fromJson(dataArray, listType);
                List<Contact> contacts=new ArrayList<>();
                // 在主线程中返回结果
                for(ContactMsg contactMsg:contactMsgs){
                    Contact contact=new Contact();
                    contact.setNickname(contactMsg.getNickname());
                    contact.setUsername(contactMsg.getUsername());
                    String avatarPath = getAvatar(contactMsg.getUsername());
                    contact.setAvatarPath(avatarPath);
                    contacts.add(contact);
                }
                Message message = handler.obtainMessage();
                message.what = 3; // 设置消息的标识
                message.obj = contacts; // 设置消息的内容
                handler.sendMessage(message);
            } catch (final IOException e) {
                // 在主线程中返回错误
                Message message = handler.obtainMessage();
                message.what = 4; // 设置消息的标识
                message.obj = e; // 设置消息的内容
                handler.sendMessage(message);
            }
        }).start();
    }

    /***
     * 添加好友
     *
     * @param username
     * @param targetUsername
     */
    public void addFriend(String username, String targetUsername) {
        new Thread(() -> {
            try {
                String requestUrl = "/user/connection/add";
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Bearer your_token");
                headers.put("username", username);


                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
                String createdTime = sdf.format(new Date());

                // 创建请求参数
                Map<String, String> params = new HashMap<>();
                params.put("username", username);
                params.put("target_username", targetUsername);
                params.put("created_time", createdTime);

                // 将请求参数转换为JSON字符串
                String jsonParams = new Gson().toJson(params);

                // 发送请求
                final String result = httpRequest.sendRequest(requestUrl, "POST", headers, jsonParams);

                // 在主线程中返回结果
                Message message = handler.obtainMessage();
                message.what = 1; // 设置消息的标识
                message.obj = result; // 设置消息的内容
                handler.sendMessage(message);
            } catch (final IOException e) {
                // 在主线程中返回错误
                Message message = handler.obtainMessage();
                message.what = 2; // 设置消息的标识
                message.obj = e; // 设置消息的内容
                handler.sendMessage(message);
            }
        }).start();
    }
    /***
     * 同意好友
     * @param username
     * @param isAgree
     * @param targetUsername
     */
    public void ensureFriend(String username, String isAgree, String targetUsername) {
        new Thread(() -> {
            try {
                String requestUrl = "/user/connection/ensure";
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Bearer your_token");
                headers.put("username", username);

                // 创建请求参数
                Map<String, String> params = new HashMap<>();
                params.put("username", username);
                params.put("isAgree", isAgree);
                params.put("target_username", targetUsername);

                // 将请求参数转换为JSON字符串
                String jsonParams = new Gson().toJson(params);

                // 发送请求
                final String result = httpRequest.sendRequest(requestUrl, "POST", headers, jsonParams);

                // 在主线程中返回结果
                Message message = handler.obtainMessage();
                message.what = 3; // 设置消息的标识
                message.obj = result; // 设置消息的内容
                handler.sendMessage(message);
            } catch (final IOException e) {
                // 在主线程中返回错误
                Message message = handler.obtainMessage();
                message.what = 4; // 设置消息的标识
                message.obj = e; // 设置消息的内容
                handler.sendMessage(message);
            }
        }).start();
    }

    /***
     * 获取请求好友列表
     * @param username
     */
    public void getRequestFriendList(String username) {
        new Thread(() -> {
            try {
                String requestUrl = "/user/connection/request/list";
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Bearer your_token");
                headers.put("username", username);

                // 创建请求参数
                Map<String, String> params = new HashMap<>();
                params.put("username", username);

                // 将请求参数转换为JSON字符串
                String jsonParams = new Gson().toJson(params);

                // 发送请求
                final String result = httpRequest.sendRequest(requestUrl, "POST", headers, jsonParams);

                JsonObject jsonObject = JsonParser.parseString(result).getAsJsonObject();
                JsonArray dataArray = jsonObject.getAsJsonArray("data");
                Gson gson = new Gson();
                Type listType = new TypeToken<List<NewFriendMsg>>() {}.getType();
                List<NewFriendMsg> newFriendMsgs = gson.fromJson(dataArray, listType);
                List<NewFriend> newFriends=new ArrayList<>();
                for (NewFriendMsg newFriendMsg : newFriendMsgs) {
                    if (newFriendMsg.getAgreeStatus() == 0) {
                        NewFriend newFriend = new NewFriend();
                        newFriend.setUsername(newFriendMsg.getRequestName());
                        newFriend.setNickname(newFriendMsg.getRequestNickname());
                        String avatarPath = getAvatar(newFriendMsg.getRequestName());
                        newFriend.setAvatarPath(avatarPath);
                        newFriends.add(newFriend);
                    }
                }                // 在主线程中返回结果
                Message message = handler.obtainMessage();
                message.what = 5; // 设置消息的标识
                message.obj = newFriends; // 设置消息的内容
                handler.sendMessage(message);
            } catch (final IOException e) {
                // 在主线程中返回错误
                Message message = handler.obtainMessage();
                message.what = 6; // 设置消息的标识
                message.obj = e; // 设置消息的内容
                handler.sendMessage(message);
            }
        }).start();
    }
    public void addLocation(String username) {
        new Thread(() -> {
            try {
                AMapLocationClientOption locationOption = new AMapLocationClientOption();
                locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Battery_Saving);
                locationOption.setNeedAddress(true);
                AMapLocationClient locationClient = new AMapLocationClient(getApplicationContext());
                locationClient.setLocationOption(locationOption);
                locationClient.setLocationListener(aMapLocation -> {
                    if (aMapLocation != null) {
                        if (aMapLocation.getErrorCode() == 0) {
                            String location = aMapLocation.getLatitude() + "_" + aMapLocation.getLongitude();
                            String requestUrl = "/user/connection/addLocation";
                            Map<String, String> headers = new HashMap<>();
                            headers.put("Content-Type", "application/json");
                            headers.put("Authorization", "Bearer your_token");
                            headers.put("username", username);

                            Map<String, String> params = new HashMap<>();
                            params.put("username", username);
                            params.put("location", location);

                            String jsonParams = new Gson().toJson(params);

                            try {
                                final String result = httpRequest.sendRequest(requestUrl, "POST", headers, jsonParams);
                                JsonObject jsonObject = JsonParser.parseString(result).getAsJsonObject();
                                JsonArray dataArray = jsonObject.getAsJsonArray("data");
                                Gson gson = new Gson();
                                Type listType = new TypeToken<List<Location>>() {}.getType();
                                List<Location> locations = gson.fromJson(dataArray, listType);
                                Message message = handler.obtainMessage();
                                message.what = 3;
                                message.obj = locations;
                                handler.sendMessage(message);
                            } catch (final IOException e) {
                                Message message = handler.obtainMessage();
                                message.what = 4;
                                message.obj = e;
                                handler.sendMessage(message);
                            }
                        }
                    }
                });
                // 启动定位
                locationClient.startLocation();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private ScheduledExecutorService scheduledExecutorService;
    private Runnable checkLoginStatusTask = () -> {
        try {
            SharedPreferences sharedPreferences = getSharedPreferences("MyApp", MODE_PRIVATE);
            String username = sharedPreferences.getString("username", null);
            String requestUrl = "/heartbeat";
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            headers.put("Authorization", "Bearer your_token");
            headers.put("username",username);
            // 发送请求
            final String result = httpRequest.sendRequest(requestUrl, "GET", headers, null);

        } catch (IOException e) {
            e.printStackTrace();
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdown();
            scheduledExecutorService = null;
        }
    }
}