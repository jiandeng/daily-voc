package play.doudou.daily_voc.app;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class VocDevice {
	private Context mContext;
	private String mDeviceName;
	private String mServiceUUID;
	private BluetoothAdapter mAdapter;
	private BluetoothDevice mDevice;
	private BluetoothReceiver mReceiver;
	private BluetoothSocket mSocket;
	private EventListener mEventListener;
	
	private float mVoc;
	private float mTemperature;
	private float mHumidity;
	
	public float getVoc() {
		return mVoc;
	}	
	
	public float getTempearture() {
		return mTemperature;
	}	

	public float getHumidity() {
		return mHumidity;
	}	
	
	public interface EventListener {

		public void onAdapterEnabled();

		public void onDiscoveryStarted();

		public void onDiscoveryFinished();

		public void onDeviceDiscovered();

		public void onReadSucceed();

		public void onReadFail();
	}

	public void setEventListener(EventListener listener) {
		mEventListener = listener;
	}

	public VocDevice(Context context, String deviceName,
                     String serviceUUID) {
		
		mContext = context;
		mDeviceName = deviceName;
		mServiceUUID = serviceUUID;
		mDevice = null;

		mReceiver = new BluetoothReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		filter.addAction(BluetoothDevice.ACTION_FOUND);
		mContext.registerReceiver(mReceiver, filter);
	}

	public void ready() {
		mAdapter = BluetoothAdapter.getDefaultAdapter();
		if (!mAdapter.isEnabled()) {
			Intent intent = new Intent();
			intent.setAction(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			mContext.startActivity(intent);
		} else {
			mEventListener.onAdapterEnabled();
		}
	}

	public void discover() {
		Set<BluetoothDevice> pairedDevices = mAdapter.getBondedDevices();
		for (BluetoothDevice device : pairedDevices) {
			if (device.getName().equals(mDeviceName)) {
				mDevice = device;
				mEventListener.onDeviceDiscovered();
				return;
			}
		}
		
		mAdapter.startDiscovery();
	}

	public void cancel() {
		if (mAdapter.isDiscovering()) {
			mAdapter.cancelDiscovery();
		}
		mContext.unregisterReceiver(mReceiver);
	}

	public void read() {
		UUID uuid = UUID.fromString(mServiceUUID);
		try {
			mSocket = mDevice.createInsecureRfcommSocketToServiceRecord(uuid);			
			mSocket.connect();

			byte[] command = new byte[] {0x01, 0x04, 0x00, 0x00, 0x00, 0x06, 0x70, 0x08};	
			OutputStream oStream = mSocket.getOutputStream();
			oStream.write(command);
			
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			byte[] buffer = new byte[17];
			InputStream iStream = mSocket.getInputStream();
			iStream.read(buffer);

			if(buffer[0] == command[0] && buffer[1] == command[1] && buffer[2] == 0x0C){
				mVoc = bytes2float(buffer, 3);				
				mTemperature = bytes2float(buffer, 7);				
				mHumidity = bytes2float(buffer, 11);
				
				mEventListener.onReadSucceed();
			} else if(buffer[1] == command[0] && buffer[2] == command[1] && buffer[3] == 0x0C){
                mVoc = bytes2float(buffer, 4);
                mTemperature = bytes2float(buffer, 8);
                mHumidity = bytes2float(buffer, 12);

                mEventListener.onReadSucceed();
            } else {
				mEventListener.onReadFail();
			}

			iStream.close();
			oStream.close();
			mSocket.close();			
		} catch(IOException e){
			mEventListener.onReadFail();
		}

	}
	
	protected float bytes2float(byte[] buffer, int offset) {
		int bits = 0xFF000000 & (buffer[offset++] << 24) | 0xFF0000 & (buffer[offset++] << 16) | 0xFF00 & (buffer[offset++] << 8) | 0xFF & (buffer[offset++]);		
		return Float.intBitsToFloat(bits);
	}

	public class BluetoothReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
				if (mAdapter.isEnabled()) {
					mEventListener.onAdapterEnabled();
				}
			} else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)) {
				mEventListener.onDiscoveryStarted();
			} else if (action
					.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
				mEventListener.onDiscoveryFinished();
			} else if (action.equals(BluetoothDevice.ACTION_FOUND)) {
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

				Log.i("MBT", device.getName() + " " + device.getAddress());

				if (device.getName().equals(mDeviceName)) {
					Log.i("MBT", "Got it");
					mAdapter.cancelDiscovery();
					mDevice = device;
					mEventListener.onDeviceDiscovered();
				}

			}
		}

	}
}