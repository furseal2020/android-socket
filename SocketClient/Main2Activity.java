package com.example.socketclient;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.SocketAddress;
import java.net.UnknownHostException;


public class Main2Activity extends AppCompatActivity {

    private EditText m_editText_txt_msg;
    private TextView m_txtview;
    private Button m_btn_back, m_btn_send;

    private int port_int;
    private Socket client;
    private String display_txt = "";
    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        m_txtview = (TextView)findViewById(R.id.txtview);
        m_editText_txt_msg = (EditText) findViewById(R.id.txt_msg2);
        m_btn_back = (Button)findViewById(R.id.btn_back);
        m_btn_send = (Button)findViewById(R.id.btn_send);

        Bundle bundle = getIntent().getExtras();
        name = bundle.getString("name");
        final String ip_address = bundle.getString("ip_address");
        String port = bundle.getString("port");

        port_int = Integer.valueOf(port);


        Thread clientThread =  new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    client = new Socket(ip_address, port_int);
                    if(client.isConnected()){
                        Log.d("connect","success");
                        display_txt+="Connection setup.\n";
                        runOnUiThread(new Runnable(){
                            @Override
                            public void run() {
                                m_txtview.setText(display_txt);
                            }
                        });
                        new Thread(new ReceiveMessage()).start();

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        clientThread.start();


        }


    class ReceiveMessage implements Runnable {
        @Override
        public void run() {

            while(!Thread.currentThread().isInterrupted())
            {
                try {
                    BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
                    String json_str;
                    if((json_str = br.readLine())!=null) //如果回傳NULL，代表socket已經出現問題 (被關閉，網路斷線…)
                    {
                        JSONObject jsonObject = new JSONObject(json_str);
                        String username = jsonObject.getString("Username");
                        String msg = jsonObject.getString("Msg");
                        if(username.equals("server_request_username")) //just for c#_server and android_client
                        {
                            Thread sendThread = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        JSONObject outData = new JSONObject();
                                        outData.put("Username", "client_response_username");
                                        outData.put("Msg", name);
                                        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
                                        bw.write(String.valueOf(outData)); //BufferedReader的readLine方法是一次讀一行的，這個方法是阻塞的，直到它讀到了一行數據為止程序才會繼續往下執行，直到程序遇到了換行符或者是對應流的結束符readLine方法才會認為讀到了一行，才會結束其阻塞，讓程序繼續往下執行
                                        bw.flush(); //清空緩衝區並送出資料
                                        //Closing either the input or the output stream of a Socket closes the other stream and the Socket.
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            sendThread.start();
                        }
                        else if(username.equals("server_welcome")) //just for c#_server and android_client
                        {
                            display_txt+=msg;
                            runOnUiThread(new Runnable(){
                                @Override
                                public void run() {
                                    m_txtview.setText(display_txt);
                                }
                            });
                        }
                        else if(username.equals("server_reply")) //just for c#_server and android_client
                        {
                            msg = msg.replaceFirst("You send : ", "");
                            display_txt += name + " : " + msg;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    m_txtview.setText(display_txt);
                                }
                            });
                        }
                        else
                        {
                            display_txt += username + " : " + msg;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    m_txtview.setText(display_txt);
                                }
                            });
                        }
                    }
                    else
                    {
                        display_txt+="Read line failed, socket close.\n";
                        runOnUiThread(new Runnable(){
                            @Override
                            public void run() {
                                m_txtview.setText(display_txt);
                            }
                        });
                        client.close();
                        Thread.currentThread().interrupt();
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }


    public void sendOnClick(View view)
    {
            Thread sendThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String str = m_editText_txt_msg.getText().toString()+"\n";
                        JSONObject outData = new JSONObject();
                        outData.put("Username", name);
                        outData.put("Msg", str);
                        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
                        bw.write(String.valueOf(outData)+"\n"); //BufferedReader的readLine方法是一次讀一行的，這個方法是阻塞的，直到它讀到了一行數據為止程序才會繼續往下執行，直到程序遇到了換行符或者是對應流的結束符readLine方法才會認為讀到了一行，才會結束其阻塞，讓程序繼續往下執行
                        bw.flush(); //清空緩衝區並送出資料
                        //Closing either the input or the output stream of a Socket closes the other stream and the Socket.
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            m_editText_txt_msg.getText().clear();
                        }
                    });
                }
            });
        sendThread.start();
    }

    public void backOnClick(View view) throws IOException {
        client.close();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

}





