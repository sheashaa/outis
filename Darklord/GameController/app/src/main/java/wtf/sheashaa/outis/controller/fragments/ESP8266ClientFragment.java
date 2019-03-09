package wtf.sheashaa.outis.controller.fragments;

import android.content.Context;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import androidx.fragment.app.Fragment;

import static android.content.Context.WIFI_SERVICE;
import static wtf.sheashaa.outis.controller.BuildConfig.DEBUG;

public class ESP8266ClientFragment extends Fragment {

    private final String TAG = "myTag";
    private boolean running;

    private ESP8266ClientListener client;
    private ESP8266Async clientAsync;

    private String address;
    private int port;
    private String SSIDName = "";

    private static final String CONNECTING_MSG = "CONNECTING_MSG";
    private static final String CONNECTED_MSG = "CONNECTED_MSG";
    private static final String DISCONNECTED_MSG = "DISCONNECTED_MSG";

    private boolean disconnected;
    private boolean[] sendSignal = new boolean[7];
    private String[] dataToSend = new String[7];
    private int channel = 0;

    @Override
    public void onAttach(Context context) {
        if (DEBUG) Log.i(TAG, "onAttach(Activity)");
        super.onAttach(context);
        if (!(getTargetFragment() instanceof ESP8266ClientListener)) {
            throw new IllegalStateException("Target fragment must implement the ESP8266ClientAsync interface.");
        }
        client = (ESP8266ClientListener) getTargetFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate(Bundle)");
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy()");
        super.onDestroy();
        disconnect();
    }

    public void start(String address, int port, String SSIDName) {
        if (!running) {
            this.address = address;
            this.port = port;
            this.SSIDName = SSIDName;
            disconnected = false;
            clientAsync = new ESP8266Async();
            clientAsync.execute();
            running = true;
        }
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isConnected() {
        return !disconnected;
    }

    private class ESP8266Async extends AsyncTask<Void, String, Void> {

        @Override
        protected void onPreExecute() {
            running = true;
        }

        @Override
        protected Void doInBackground(Void... arg) {

            try {
                if (!isAdded())
                    return null;

                connectToWIFI();

                InetAddress addr = InetAddress.getByName(address);
                DatagramSocket ds = new DatagramSocket();
                ds.setReuseAddress(true);
                ds.setBroadcast(true);
                ds.connect(addr, port);

                for (int i = 0; i < 3; i++) {
                    String testPacket = "m=0";
                    DatagramPacket datagramPacket = new DatagramPacket(testPacket.getBytes(), testPacket.length(), addr, port);
                    ds.send(datagramPacket);
                }

                Log.d(TAG, "Connected Successfully!");
                publishProgress(CONNECTED_MSG);

                DatagramPacket[] dp = new DatagramPacket[7];

                while (!disconnected) {
                    if (sendSignal[0]) {
                        sendSignal[0] = false;
                        dp[0] = new DatagramPacket(dataToSend[0].getBytes(), dataToSend[0].length(), addr, port);
                        ds.send(dp[0]);
                    }

                    Thread thread = new Thread() {
                        @Override
                        public void run() {
                            for (int i = 1; i < sendSignal.length; i++) {
                                if (sendSignal[i]) {
                                    sendSignal[i] = false;
                                    dp[i] = new DatagramPacket(dataToSend[i].getBytes(), dataToSend[i].length(), addr, port);
                                    try {
                                        ds.send(dp[i]);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    };

                    thread.start();
                }

            } catch (UnknownHostException e) {
                e.printStackTrace();
                Log.d(TAG, "UnknownHostException: " + e.toString());
                publishProgress(DISCONNECTED_MSG);
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "IOException: " + e.toString());
                publishProgress(DISCONNECTED_MSG);
            }

            publishProgress(DISCONNECTED_MSG);
            Log.d(TAG, "Successfully Disconnected...Ending Session");
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            String msg = values[0];
            if (msg == null) return;
            if (msg.equals(CONNECTED_MSG)) {
                running = true;
                client.onConnectedListener(true);
            } else if (msg.equals(DISCONNECTED_MSG)) {
                running = false;
                client.onDisconnectListener(true);
            } else if (msg.equals(CONNECTING_MSG)) {
                running = true;
                client.onConnectingListener(true);
            } else
                client.onReceiveListener(msg);
        }

        private boolean connectToWIFI() {
            publishProgress(CONNECTING_MSG);
            WifiManager wifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(WIFI_SERVICE);
            boolean wifiWasOn = true;
            assert wifiManager != null;
            while (!wifiManager.isWifiEnabled()) {
                wifiWasOn = false;
                wifiManager.setWifiEnabled(true); //turn on wifi if it isn't already;
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            Log.d(TAG, "Checking if already connected to Esp8266");
            if (!connectToEsp(wifiInfo, wifiWasOn)) {  //if false, then search for it and connect
                Log.d(TAG, "Not connected to Esp8266...");
                String passwordEsp8266 = "";
                WifiConfiguration wifiConfig = new WifiConfiguration();
                wifiConfig.SSID = String.format("\"%s\"", SSIDName);
                wifiConfig.preSharedKey = String.format("\"%s\"", passwordEsp8266);

                //remember id
                int netId = wifiManager.addNetwork(wifiConfig);
                wifiManager.disconnect();
                wifiManager.enableNetwork(netId, true);
                wifiManager.reconnect();
                Log.d(TAG, "auto-connecting");
                wifiInfo = wifiManager.getConnectionInfo();
                return !connectToEsp(wifiInfo, false);
            }
            return true;
        }

        private boolean connectToEsp(WifiInfo wifiInfo, boolean wifiWasOn) {
            publishProgress(CONNECTING_MSG);
            long start = System.currentTimeMillis();
            boolean scanComplete = true;
            while (wifiInfo.getSupplicantState() != SupplicantState.COMPLETED) {
                long now = System.currentTimeMillis();
                if (now - start > 5E3) {
                    scanComplete = false;
                    publishProgress(DISCONNECTED_MSG);
                    break;
                }
            }

            if (scanComplete) {
                Log.d(TAG, "Yay, found a device...");
                String requiredSSID = String.format("\"%s\"", SSIDName);
                start = System.currentTimeMillis();
                while (!wifiInfo.getSSID().equalsIgnoreCase(requiredSSID)) {
                    long now = System.currentTimeMillis();
                    if (now - start > 12E3) {
                        Log.d(TAG, "Unknown device: " + wifiInfo.getSSID());
                        publishProgress(DISCONNECTED_MSG);
                        scanComplete = false;
                        break;
                    }
                }
            }
            return scanComplete;
        }

        @Override
        protected void onCancelled() {
            running = false;
        }

        @Override
        protected void onPostExecute(Void ignore) {
            running = false;
        }
    }

    public void sendMessage(String dataToSend, int channel) {
        this.dataToSend[channel] = dataToSend;
        this.channel = channel;
        sendSignal[channel] = true;
    }

    public void disconnect() {
        if (running) {
            clientAsync.cancel(false);
            client.onDisconnectListener(true);
            disconnected = true;
            clientAsync = null;
            running = false;
        }
    }

    public interface ESP8266ClientListener {
        void onReceiveListener(String message);
        void onDisconnectListener(boolean toast);
        void onConnectedListener(boolean toast);
        void onConnectingListener(boolean toast);
    }
}
