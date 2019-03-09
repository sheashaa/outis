package wtf.sheashaa.outis.controller.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import wtf.sheashaa.outis.controller.R;
import wtf.sheashaa.outis.controller.activities.MainActivity;
import wtf.sheashaa.outis.controller.interfaces.ClientStatus;
import wtf.sheashaa.outis.controller.utilities.Utility;

import static wtf.sheashaa.outis.controller.BuildConfig.DEBUG;

public class GamepadFragment extends Fragment implements MainActivity.GamepadListener, ESP8266ClientFragment.ESP8266ClientListener {

    private final String TAG = "myTag";

    private static final String SSID_NAME = "OUTISDarkLord";
    private static final String IP_ADDRESS = "192.168.4.1";
    private static final int PORT = 4210;
    private static final String TAG_TASK_FRAGMENT = "task_fragment";

    private Toast toast;
    private ClientStatus clientStatus;
    private ESP8266ClientFragment esp8266ClientFragment;

    private Unbinder unbinder;

    //Connect to WiFi button
    @BindView(R.id.wifi_connect_fab)
    FloatingActionButton mConnectFab;
    //Toggle button for people who don't have a controller, but just want to test out the app
    @BindView(R.id.toggle_led)
    ImageView mToggleBtn;
    //Used to show if we are connected to esp8266
    @BindView(R.id.Toggle_background)
    ImageView mStatusWiFi;

    //right joystick
    @BindView(R.id.right_outer_edge_joystick)
    ImageView mJoystickRight;
    @BindView(R.id.right_inner_edge_joystick)
    ImageView mJoystickRight_inner;
    @BindView(R.id.right_outline_joystick)
    ImageView mJoystickRight_outline;

    //Left joystick
    @BindView(R.id.left_outer_edge_joystick)
    ImageView mJoystickLeft;
    @BindView(R.id.left_inner_edge_joystick)
    ImageView mJoystickLeft_inner;
    @BindView(R.id.left_outline_joystick)
    ImageView mJoystickLeft_outline;

    //Back Button
    @BindView(R.id.button_back)
    ImageView mBackButton;

    //Select/Menu Button
    @BindView(R.id.button_menu)
    ImageView mMenuButton;

    //X Button
    @BindView(R.id.button_left)
    ImageView mButtonX;
    //Y Button
    @BindView(R.id.button_up)
    ImageView mButtonY;
    //B Button
    @BindView(R.id.button_right)
    ImageView mButtonB;
    //A Button
    @BindView(R.id.button_down)
    ImageView mButtonA;

    //D-Pad left
    @BindView(R.id.dPad_left)
    ImageView mDpadLeft;
    //D-Pad Up
    @BindView(R.id.dPad_up)
    ImageView mDpadUp;
    //D-Pad Right
    @BindView(R.id.dPad_right)
    ImageView mDpadRight;
    //D-Pad Down
    @BindView(R.id.dPad_down)
    ImageView mDpadDown;

    //Left Trigger
    @BindView(R.id.trigger_left)
    ImageView mTriggerLeft;
    //Right Trigger
    @BindView(R.id.trigger_right)
    ImageView mTriggerRight;
    //Analog Left trigger
    @BindView(R.id.analog_trigger_left)
    ImageView mAnalogTriggerLeft;
    //Analog Right trigger
    @BindView(R.id.analog_trigger_right)
    ImageView mAnalogTriggerRight;

    private int dPadCurrentLocation = KeyEvent.KEYCODE_DPAD_CENTER;
    private int brightStandOutColor = Utility.getComplimentColor(0xD81B60);
    private static final String CONNECTED_TO_WIFI = "current_wifi_status";

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            clientStatus = (ClientStatus) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement MyInterface");
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_gamepad, container, false);
        unbinder = ButterKnife.bind(this, v);
        v.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                    @Override
                    public void onGlobalLayout() {
                        // Layout has happened here.

                        // Don't forget to remove your listener when you are done with it.
                        if (Build.VERSION.SDK_INT < 16) {
                            v.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        } else {
                            v.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                    }
                });

        //Setup up our interface listeners
        ((MainActivity) getActivity()).setGamepadListener(this);

        mConnectFab.setOnClickListener(v1 -> {
            toggleWifi();
        });

        mToggleBtn.setOnTouchListener((v12, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                mToggleBtn.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_radio_button_checked_black_24dp));
                if (esp8266ClientFragment != null) {
                    esp8266ClientFragment.sendMessage("a=1", 0);
                }
                return true;
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                mToggleBtn.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_album_black_24dp));
                if (esp8266ClientFragment != null) {
                    esp8266ClientFragment.sendMessage("a=0", 0);
                }
                return true;
            }
            return false;
        });

        if (savedInstanceState != null) {
            boolean connectedToWiFi = savedInstanceState.getBoolean(CONNECTED_TO_WIFI, false);
            if (connectedToWiFi) {
                onConnectedListener(false);
            } else {
                onDisconnectListener(false);
            }
        }

        FragmentManager fm = getActivity().getSupportFragmentManager();
        esp8266ClientFragment = (ESP8266ClientFragment) fm.findFragmentByTag(TAG_TASK_FRAGMENT);

        if (esp8266ClientFragment == null) {
            Log.d(TAG, "New async created");
            esp8266ClientFragment = new ESP8266ClientFragment();
            esp8266ClientFragment.setTargetFragment(this, 0);
            clientStatus.onConnecting();
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    esp8266ClientFragment.start(IP_ADDRESS, PORT, SSID_NAME);
                    mConnectFab.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_wifi_black_24dp));
                }
            }, 500);
            fm.beginTransaction().add(esp8266ClientFragment, TAG_TASK_FRAGMENT).commit();
        } else {
            Log.d(TAG, "Using old Async");
        }

        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (DEBUG) Log.i(TAG, "onSaveInstanceState(Bundle)");
        super.onSaveInstanceState(outState);
        outState.putBoolean(CONNECTED_TO_WIFI, esp8266ClientFragment.isConnected());
    }

    private void toggleWifi() {
        if (!esp8266ClientFragment.isRunning()) {
            esp8266ClientFragment.start(IP_ADDRESS, PORT, SSID_NAME);
            mConnectFab.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_wifi_black_24dp));
        } else if (esp8266ClientFragment.isConnected()) {
            esp8266ClientFragment.disconnect();
            mConnectFab.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_signal_wifi_off_black_24dp));
        }
    }

    private void sayToast(String message) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public boolean onKey(int keyPress, boolean isPressed) {
        boolean handled = true;
        switch (keyPress) {
            case KeyEvent.KEYCODE_BACK:
                changeImageViewColor(mBackButton, isPressed);
                sendToClient((isPressed) ? "y1" : "y0");
                break;
            case KeyEvent.KEYCODE_BUTTON_START:
                changeImageViewColor(mMenuButton, isPressed);
                sendToClient((isPressed) ? "u1" : "u0");
                break;
            case KeyEvent.KEYCODE_BUTTON_X:
                changeImageViewColor(mButtonX, isPressed);
                sendToClient((isPressed) ? "i1" : "i0");
                break;
            case KeyEvent.KEYCODE_BUTTON_Y:
                changeImageViewColor(mButtonY, isPressed);
                sendToClient((isPressed) ? "o1" : "o0");
                break;
            case KeyEvent.KEYCODE_BUTTON_B:
                changeImageViewColor(mButtonB, isPressed);
                sendToClient((isPressed) ? "p1" : "p0");
                break;
            case KeyEvent.KEYCODE_BUTTON_A:
                changeImageViewColor(mButtonA, isPressed);
                sendToClient((isPressed) ? "a1" : "a0");
                break;
            case KeyEvent.KEYCODE_BUTTON_L1:
                changeImageViewColor(mTriggerLeft, isPressed);
                sendToClient((isPressed) ? "w1" : "w0");
                break;
            case KeyEvent.KEYCODE_BUTTON_R1:
                changeImageViewColor(mTriggerRight, isPressed);
                sendToClient((isPressed) ? "e1" : "e0");
                break;
            case KeyEvent.KEYCODE_BUTTON_L2:
                changeImageViewColor(mAnalogTriggerLeft, isPressed);
                sendToClient((isPressed) ? "q255" : "q0");
                break;
            case KeyEvent.KEYCODE_BUTTON_R2:
                changeImageViewColor(mAnalogTriggerRight, isPressed);
                sendToClient((isPressed) ? "r255" : "r0");
                break;
            case KeyEvent.KEYCODE_DPAD_CENTER:
                clearPreviousDpad();
                dPadCurrentLocation = KeyEvent.KEYCODE_DPAD_CENTER;
                dPadCurrentLocation = KeyEvent.KEYCODE_DPAD_CENTER;
                sendToClient("d0");
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                clearPreviousDpad();
                changeImageViewColor(mDpadLeft, isPressed);
                dPadCurrentLocation = KeyEvent.KEYCODE_DPAD_LEFT;
                sendToClient("d1");
                break;
            case KeyEvent.KEYCODE_DPAD_UP:
                clearPreviousDpad();
                changeImageViewColor(mDpadUp, isPressed);
                dPadCurrentLocation = KeyEvent.KEYCODE_DPAD_UP;
                sendToClient("d2");
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                clearPreviousDpad();
                changeImageViewColor(mDpadRight, isPressed);
                dPadCurrentLocation = KeyEvent.KEYCODE_DPAD_RIGHT;
                sendToClient("d3");
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                clearPreviousDpad();
                changeImageViewColor(mDpadDown, isPressed);
                dPadCurrentLocation = KeyEvent.KEYCODE_DPAD_DOWN;
                sendToClient("d4");
                break;
            default:
                handled = false;
        }
        return handled;
    }

    private void sendToClient(String message) {
        if (esp8266ClientFragment != null) {
            esp8266ClientFragment.sendMessage(message, 0);
        }
    }

    private void clearPreviousDpad() {
        switch (dPadCurrentLocation) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
                changeImageViewColor(mDpadLeft, false);
                break;
            case KeyEvent.KEYCODE_DPAD_UP:
                changeImageViewColor(mDpadUp, false);
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                changeImageViewColor(mDpadRight, false);
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                changeImageViewColor(mDpadDown, false);
                break;
        }
    }

    public void changeImageViewColor(ImageView img, boolean isPressed) {
        ImageViewCompat.setImageTintList(img, ColorStateList.valueOf((isPressed) ? brightStandOutColor : getResources().getColor(R.color.colorPrimary)));
    }

    @Override
    public void onReceiveListener(String message) {

    }

    @Override
    public void onDisconnectListener(boolean toast) {
        if (toast) sayToast("Disconnected.");
        mConnectFab.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_signal_wifi_off_black_24dp));
        ImageViewCompat.setImageTintList(mConnectFab, ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
        ImageViewCompat.setImageTintList(mStatusWiFi, ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
        clientStatus.onDisconnected();
    }

    @Override
    public void onConnectedListener(boolean toast) {
        if (toast) sayToast("Connected!");
        mConnectFab.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_wifi_black_24dp));
        ImageViewCompat.setImageTintList(mConnectFab, ColorStateList.valueOf(Utility.getComplimentColor(getResources().getColor(R.color.colorAccent))));
        ImageViewCompat.setImageTintList(mStatusWiFi, ColorStateList.valueOf(Utility.getComplimentColor(getResources().getColor(R.color.colorAccent))));
        clientStatus.onConnected();
    }

    @Override
    public void onConnectingListener(boolean toast) {
        clientStatus.onConnecting();
    }

    public void forceDisconnect() {
        if (esp8266ClientFragment != null)
            esp8266ClientFragment.disconnect();
    }
}
