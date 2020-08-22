package com.example.socketclient;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

    private EditText m_editText_name;
    private EditText m_editText_ip_address;
    private EditText m_editText_port;
    private Button m_btnConnect;
    private ImageView m_img_client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m_editText_name = (EditText)findViewById(R.id.name);
        m_editText_ip_address = (EditText)findViewById(R.id.ip_address);
        m_editText_port = (EditText)findViewById(R.id.port);
        m_img_client = (ImageView)findViewById(R.id.img_client);
        m_btnConnect = (Button)findViewById(R.id.btn_connect);

        Picasso.with(this)
                .load("https://i.imgur.com/4AiXzf8.jpg")
                .resize(200,200)
                .into(m_img_client) ;

    }

    public void connectOnClick(View view)
    {
        if (TextUtils.isEmpty( m_editText_name.getText().toString())||TextUtils.isEmpty( m_editText_ip_address.getText().toString())||TextUtils.isEmpty( m_editText_port.getText().toString()))
        {
            Toast.makeText(MainActivity.this,"Please fill in all of the blanks above.",Toast.LENGTH_SHORT).show();
        }
        else
        {
            String name = m_editText_name.getText().toString();
            String ip_address = m_editText_ip_address.getText().toString();
            String port = m_editText_port.getText().toString();

            Intent intent = new Intent(this, Main2Activity.class);
            Bundle bundle = new Bundle();
            bundle.putString("name",name);
            bundle.putString("ip_address",ip_address);
            bundle.putString("port",port);
            intent.putExtras(bundle);
            startActivity(intent);

        }
    }
}
