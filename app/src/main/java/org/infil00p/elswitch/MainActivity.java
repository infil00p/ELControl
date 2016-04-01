


package org.infil00p.elswitch;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import ioio.lib.api.DigitalOutput;
import ioio.lib.api.Uart;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.IOIOLooperProvider;
import ioio.lib.util.android.IOIOAndroidApplicationHelper;


public class MainActivity extends AppCompatActivity implements IOIOLooperProvider {

    private final IOIOAndroidApplicationHelper helper = new IOIOAndroidApplicationHelper(this, this);

    private static int TX_PIN = 45;
    private static int RX_PIN = 46;
    private static String TAG = "EL Switch";
    private int lightEnabled;
    Button[] buttons;
    private DigitalOutput led;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        helper.create();
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        lightEnabled = -1;
        wireButtons();
    }

    private void wireButtons() {
        buttons = new Button[4];
        buttons[0] = (Button) findViewById(R.id.panel_1);
        buttons[1] = (Button) findViewById(R.id.panel_2);
        buttons[2] = (Button) findViewById(R.id.panel_3);
        buttons[3] = (Button) findViewById(R.id.panel_4);

        buttons[0].setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                refreshState(0);
            }
        });
        buttons[1].setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                refreshState(1);
            }
        });
        buttons[2].setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                refreshState(2);
            }
        });
        buttons[3].setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                refreshState(3);
            }
        });
    }

    private void refreshState(final int index) {
        lightEnabled = index;
        Log.d(TAG, "Flipping State to: " + Integer.toString(lightEnabled));
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                toggleUi(true);
                buttons[index].setEnabled(false);
            }
        });
    }

    private void toggleUi(final boolean enabled) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for(int i = 0; i < 4; ++i) {
                    buttons[i].setEnabled(enabled);
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            Intent about = new Intent(MainActivity.this,AboutActivity.class);
            startActivity(about);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    class Looper extends BaseIOIOLooper {

        Uart uart;
        OutputStream out;
        InputStream in;

        @Override
        public void setup() throws ConnectionLostException {
            uart = ioio_.openUart(RX_PIN, TX_PIN, 9600, Uart.Parity.NONE, Uart.StopBits.ONE);
            out = uart.getOutputStream();
            in = uart.getInputStream();

            led = ioio_.openDigitalOutput(0, true);

            toggleUi(true);
            toast("IOIO Connected");
        }

        @Override
        public void loop() throws ConnectionLostException, InterruptedException  {
            // Do all our writing here based on whether the state changed in the app
            // between the last state and the next.
            //out.write();
            Log.d(TAG, "Inside run loop.  Current state: " + Integer.toString(lightEnabled));
            if(lightEnabled > -1) {
                byte buffer[] = new byte[1];
                buffer[0] = (byte) Character.forDigit(lightEnabled, 10);

                //Apparently one of the exceptions above extends IOException
                try {
                    Log.d(TAG, "Writing byte to device");
                    out.write(buffer);
                } catch (IOException e) {
                    Log.e(TAG, "EL Command Lost");
                }
                led.write(true);
                Thread.sleep(1000);
                led.write(false);
            }
        }


        @Override
        public void disconnected() {
            try {
                out.close();
            } catch (IOException e) {
                Log.e(TAG, "Output Stream did not close, uart is closing");
            }
            uart.close();
            toggleUi(false);
            toast("IOIO disconnected");
        }

    }

    private void toast(final String message) {
        final Context context = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    /*
        The following are methods yanked from the IOIOActivity.  These are required so that
        we can do things like start the run loop, which is a concept stolen from Arduino.  Since
        Java doesn't implement dual inheritance, we have to copy/paste code to keep the AppCompat
        code for Material Design goodness!
     */

    @Override
    protected void onStart() {
        super.onStart();
        helper.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        helper.stop();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if((intent.getFlags() & Intent.FLAG_ACTIVITY_NEW_TASK) != 0) {
            helper.restart();
        }
    }

    @Override
    public IOIOLooper createIOIOLooper(String connectionType, Object extra) {
        return new Looper();
    }
}
