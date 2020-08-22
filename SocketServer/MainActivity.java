package com.example.socketserver;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Enumeration;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private TextView m_txtview;
    private Button m_btnConnect;

    private ServerSocket server;
    private String display_txt;
    String serverIPAddr;
    HashMap<Socket, String> hashMap; //<client_socket,client_ip>
    boolean flag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m_txtview = (TextView)findViewById(R.id.txtview);
        m_btnConnect = (Button)findViewById(R.id.btnConnect);

        hashMap = new HashMap<Socket, String>();

        display_txt="";
        flag=false; //表示畫面上目前為connect btn
    }

    public void connectOnClick(View view) throws IOException {
        if(flag==false)
        {
            flag=true;
            m_btnConnect.setText("DISCONNECT");
            server = new ServerSocket(8888);
            new Thread(new GetDeviceIP()).start();
            while(serverIPAddr==null)
            {
                //stuck here.
            }
            display_txt+="Server Connect : IP="+serverIPAddr+" Port=8888\n";
            runOnUiThread(new Runnable(){
                @Override
                public void run() {
                    m_txtview.setText(display_txt);
                }
            });
            new Thread(new Listen()).start();
        }
        else
        {
            server.close();
            flag=false;
            for (Socket socket : hashMap.keySet())
            {
                socket.close();
            }
            m_btnConnect.setText("CONNECT");
            display_txt+="Server shut down\n";
            hashMap.clear();
            runOnUiThread(new Runnable(){
                @Override
                public void run() {
                    m_txtview.setText(display_txt);
                }
            });
        }
    }

    class GetDeviceIP implements Runnable {

        @Override
        public void run() {
            try {
                Enumeration<NetworkInterface> enumNetworkInterface = NetworkInterface.getNetworkInterfaces();
                while (enumNetworkInterface.hasMoreElements()) {
                    NetworkInterface networkInterface = enumNetworkInterface.nextElement();
                    Enumeration<InetAddress> enumInetAddress = networkInterface.getInetAddresses();
                    while (enumInetAddress.hasMoreElements()) {
                        InetAddress inetAddress = enumInetAddress.nextElement();
                        if (inetAddress.isSiteLocalAddress())
                            serverIPAddr = inetAddress.getHostAddress();
                    }
                }

            } catch (Exception e) {
                Log.e("ERROR:", "cannot get IP");
                Log.e("ERROR", e.toString());
            }
        }
    }

    class Listen implements Runnable {

        @Override
        public void run() {
            while(!Thread.currentThread().isInterrupted())
            {
                try {
                    Socket client = server.accept();
                    String client_addr = client.getRemoteSocketAddress().toString();
                    client_addr = client_addr.substring(1, client_addr.length());
                    hashMap.put(client, client_addr);
                    display_txt+="User IP="+client_addr+" is connected.\n";
                    runOnUiThread(new Runnable(){
                        @Override
                        public void run() {
                            m_txtview.setText(display_txt);
                        }
                    });

                    ReceiveMessage thread = new ReceiveMessage(client);
                    Thread mythread = new Thread(thread);
                    mythread.start();
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }

            }
        }
    }

    class ReceiveMessage implements Runnable {

        private Socket client;
        private BufferedReader br;

        public ReceiveMessage(Socket client) {

            this.client = client;

            try {
                this.br = new BufferedReader(new InputStreamReader(this.client.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        @Override
        public void run() {
            while(!Thread.currentThread().isInterrupted())
            {
                String json_str;
                try {
                    json_str = br.readLine();
                    String username;
                    String msg;
                    if(json_str !=null) //如果回傳NULL，代表socket已經出現問題 (被關閉，網路斷線…)
                    {
                        JSONObject jsonObject = new JSONObject(json_str);
                        username = jsonObject.getString("Username");
                        msg = jsonObject.getString("Msg");
                        display_txt+=hashMap.get(client) + " where name = " + username + " : "+msg;
                        runOnUiThread(new Runnable(){
                            @Override
                            public void run() {
                                m_txtview.setText(display_txt);

                            }
                        });
                        SendMessage thread = new SendMessage(client, json_str);
                        Thread mythread = new Thread(thread);
                        mythread.start();
                    }
                    else
                    {
                        client.close();
                        display_txt+=hashMap.get(client) + " disconnect\n";
                        runOnUiThread(new Runnable(){
                            @Override
                            public void run() {
                                m_txtview.setText(display_txt);

                            }
                        });
                        Thread.currentThread().interrupt();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    class SendMessage implements Runnable {

        private Socket client;
        private BufferedWriter bw;
        private String json_str;


        public SendMessage(Socket client, String json_str) {

            this.client = client;
            this.json_str = json_str;


            try {
                this.bw = new BufferedWriter(new OutputStreamWriter(this.client.getOutputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        @Override
        public void run() {
                try {
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));

                bw.write(json_str+"\n"); //BufferedReader的readLine方法是一次讀一行的，這個方法是阻塞的，直到它讀到了一行數據為止程序才會繼續往下執行，直到程序遇到了換行符或者是對應流的結束符readLine方法才會認為讀到了一行，才會結束其阻塞，讓程序繼續往下執行
                bw.flush(); //清空緩衝區並送出資料
                //Closing either the input or the output stream of a Socket closes the other stream and the Socket.
            } catch (Exception e) {
                e.printStackTrace();
            }

            }

    }


}





