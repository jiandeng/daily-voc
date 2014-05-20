package play.doudou.daily_voc.app;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class MainActivity extends Activity implements VocDevice.EventListener {
    VocDevice mVocDevice = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button refreshButton = (Button) findViewById(R.id.refresh);
        refreshButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mVocDevice = new VocDevice(MainActivity.this, "ACMM", "00001101-0000-1000-8000-00805F9B34FB");
                mVocDevice.setEventListener(MainActivity.this);
                mVocDevice.ready();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onStop() {
        if(mVocDevice != null){
            mVocDevice.cancel();
        }

        super.onStop();
    }


    @Override
    public void onAdapterEnabled() {
        Log.i("MBT", "Adapter enabled.");
        mVocDevice.discover();

    }


    @Override
    public void onDiscoveryStarted() {
        Log.i("MBT", "Discovery started.");

    }


    @Override
    public void onDiscoveryFinished() {
        Log.i("MBT", "Discovery finished.");

    }


    @Override
    public void onDeviceDiscovered() {
        Log.i("MBT", "Device Discovered.");
        mVocDevice.read();

    }


    @Override
    public void onReadSucceed() {
        Log.i("MBT", "Read succeed.");
        TextView textView =  (TextView)findViewById(R.id.voc_value);
        textView.setText(String.valueOf(mVocDevice.getVoc()));

        textView =  (TextView)findViewById(R.id.temperature_value);
        textView.setText(String.valueOf(mVocDevice.getTempearture()));

        textView =  (TextView)findViewById(R.id.humidity_value);
        textView.setText(String.valueOf(mVocDevice.getHumidity()));
    }


    @Override
    public void onReadFail() {
        Log.i("MBT", "Read failed.");
    }

}
