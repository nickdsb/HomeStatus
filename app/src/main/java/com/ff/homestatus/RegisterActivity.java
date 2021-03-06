package com.ff.homestatus;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.LogInCallback;
import com.avos.avoscloud.SaveCallback;
import com.avos.avoscloud.SignUpCallback;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText username,password,password2;
    private Button submit;
    private String ip="192.168.0.105";
    private String requestPath="http://"+ip+":8080/HomeStatus/servlet/";

    @Override
    protected void onCreate(Bundle save){
        super.onCreate(save);
        setContentView(R.layout.activity_register);
        submit=(Button)findViewById(R.id.submit);
        username= (EditText) findViewById(R.id.username);
        password= (EditText) findViewById(R.id.password);
        password2= (EditText) findViewById(R.id.password2);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(password.getText().toString().equals(password2.getText().toString())){
                    sendRequestWithHttpURLConnection();
                }else{
                    Toast.makeText(RegisterActivity.this,"两次密码不一致",Toast.LENGTH_SHORT).show();
                }

            }
        });
    }


    public void Register(String username,String password){
            AVUser user = new AVUser();// 新建 AVUser 对象实例
            user.setUsername(username);// 设置用户名
            user.setPassword(password);// 设置密码
            user.signUpInBackground(new SignUpCallback() {
                @Override
                public void done(AVException e) {
                    if (e == null) {
                        // 注册成功
                        Toast.makeText(RegisterActivity.this,"注册成功",Toast.LENGTH_SHORT).show();
                    } else {
                        // 失败的原因可能有多种，常见的是用户名已经存在。
                        Toast.makeText(RegisterActivity.this,"注册失败",Toast.LENGTH_SHORT).show();
                        //从数据库中删除一条数据
                    }
                }
            });

    }
    private void sendRequestWithHttpURLConnection() {
        final String urlStr = requestPath+"addUser?username="+username.getText().toString();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(urlStr)
                            .build();
                    final Response response = client.newCall(request).execute();
                    final String responseData = response.body().string();
                    if("addSuccess".equals(responseData)){
                        Register(username.getText().toString(),password.getText().toString());
                    }else{
                        Log.e("upToMyServer","error:"+responseData);
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // 在这里进行 UI 操作，将结果显示到界面上
                        }
                    });



                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}

