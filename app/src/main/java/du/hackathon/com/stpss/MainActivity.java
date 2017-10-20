package du.hackathon.com.stpss;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";
    private ServerSocket mServerSocket;
    private String ip;
    private String buffer;
    private TextView ip_tv;
    private TextView content_tv;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0x11) {
                Bundle bundle = msg.getData();
                Log.d(TAG, "接受到来自客户端的消息了");
                content_tv.append("client:" + bundle.getString("msg") + "\n");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ip_tv = (TextView) findViewById(R.id.sever_ip);
        content_tv = (TextView) findViewById(R.id.content_tv);
        ip = getLocalIp();
        ip_tv.setText("Ip :" + ip);
        new Thread() {
            @Override
            public void run() {
                Bundle bundle = new Bundle();
                bundle.clear();
                OutputStream out;
                String str = "sever is ok";
                try {
                    mServerSocket = new ServerSocket(30000);
                    while (true) {
                        Message msg = new Message();
                        msg.what = 0x11;
                        Socket socket = mServerSocket.accept();
                        out = socket.getOutputStream();
                        out.write(str.getBytes("utf-8"));
                        out.flush();
                        socket.shutdownOutput();
                        buffer = "";
                        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        String line;
                        while ((line = br.readLine()) != null) {
                            buffer = line + buffer;
                        }
                        bundle.putString("msg", buffer.toString());
                        msg.setData(bundle);
                        mHandler.sendMessage(msg);
                        br.close();
                        out.close();
                        socket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private String getLocalIp() {
        WifiManager wifiM = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiI = wifiM.getConnectionInfo();
        int ipAddress = wifiI.getIpAddress();
        if (ipAddress == 0) {
            return null;
        }
        return ((ipAddress & 0xff) + "." + (ipAddress >> 8 & 0xff) + "."
                + (ipAddress >> 16 & 0xff) + "." + (ipAddress >> 24 & 0xff));
    }


}
