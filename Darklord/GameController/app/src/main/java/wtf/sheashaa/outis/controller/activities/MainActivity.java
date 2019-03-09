package wtf.sheashaa.outis.controller.activities;

import android.os.Bundle;
import android.renderscript.ScriptGroup;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import wtf.sheashaa.outis.controller.fragments.GamepadFragment;
import wtf.sheashaa.outis.controller.input.DPadAction;
import wtf.sheashaa.outis.controller.R;
import wtf.sheashaa.outis.controller.interfaces.ClientStatus;

public class MainActivity extends AppCompatActivity implements ClientStatus {

    ProgressBar progressBar;
    FragmentTransaction fragmentTransaction;

    GamepadFragment gamepadFragment = new GamepadFragment();
    DPadAction dPadAction = new DPadAction();

    boolean isGamepad = false;
    GamepadListener gamepadListener;
    int previousDPadKey = KeyEvent.KEYCODE_DPAD_CENTER;
    int previousKey = KeyEvent.KEYCODE_DPAD_CENTER;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = findViewById(R.id.connection_progress_bar);

        if (savedInstanceState == null) {
            loadFragment(gamepadFragment);
        } else {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (fragment.getClass().equals(gamepadFragment.getClass())) {
                gamepadFragment = (GamepadFragment) fragment;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getGameControllerIds();
    }

    private boolean loadFragment(Fragment currentFragment) {
        fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out);
        fragmentTransaction.replace(R.id.fragment_container, currentFragment);
        fragmentTransaction.commit();
        return true;
    }

    @Override
    public boolean onGenericMotionEvent(android.view.MotionEvent motionEvent) {
        int press = dPadAction.getDirectionPressed(motionEvent);
        if (isGamepad && (previousDPadKey != press)) {
            previousDPadKey = press;
            gamepadListener.onKey(press, true);
        }
        return true;
    }

    @Override
    public boolean dispatchKeyEvent(android.view.KeyEvent event) {
        if ((event.getSource() & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (previousKey != event.getKeyCode() && !gamepadListener.onKey(event.getKeyCode(), true)) {
                    Log.d("myTag", "Unhandled KeyCode: " + event.getKeyCode());
                    return false;
                } else {
                    previousKey = event.getKeyCode();
                    return true;
                }
            } else if (event.getAction() == KeyEvent.ACTION_UP) {
                if (!gamepadListener.onKey(event.getKeyCode(), false)) {
                    Log.d("myTag", "Unhandled KeyCode: " + event.getKeyCode());
                    return false;
                } else {
                    previousKey = -1;
                    return true;
                }
            }
        } else {
            if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                onBackPressed();
                return true;
            }
            Log.d("myTag", "Not a Joystick, KeyCode: " + event.getKeyCode());
        }
        return false;
    }

    public void getGameControllerIds() {
        ArrayList gameControllerDeviceIds = new ArrayList();
        int[] deviceIds = InputDevice.getDeviceIds();

        for (int deviceId : deviceIds) {
            InputDevice dev = InputDevice.getDevice(deviceId);
            int sources = dev.getSources();
            if ((sources & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD) {
                if (!gameControllerDeviceIds.contains(deviceId)) {
                    gameControllerDeviceIds.add(deviceId);
                }
                //possible both maybe true.
                if ((sources & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD)
                    isGamepad = true;
            }
            Toast.makeText(this, "Device ID: " + deviceId + " ,Description: " + dev.getName(), Toast.LENGTH_LONG);
        }
    }

    @Override
    public void onDisconnected() {
        progressBar.setIndeterminate(false);
        progressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onConnected() {
        progressBar.setIndeterminate(false);
        progressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onConnecting() {
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.VISIBLE);
    }

    public interface GamepadListener {
        boolean onKey(int keyPress, boolean isPressed);
    }

    public void setGamepadListener(GamepadListener interFace) {
        gamepadListener = interFace;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (fragment.getClass().equals(gamepadFragment.getClass())) {
            gamepadFragment.forceDisconnect();
            super.onBackPressed();
        } else {
            loadFragment(gamepadFragment);
        }
    }

}
