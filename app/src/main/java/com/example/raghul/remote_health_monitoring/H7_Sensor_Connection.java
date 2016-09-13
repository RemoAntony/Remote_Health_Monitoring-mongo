package com.example.raghul.remote_health_monitoring;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.bson.Document;

import java.util.ArrayList;
import java.util.List;


public class H7_Sensor_Connection extends ActionBarActivity {
    private boolean isConnected;
    private BluetoothAdapter mBluetoothAdapter;
    private int REQUEST_ENABLE_BT = 1;
    private Handler mHandler;
    private static final long SCAN_PERIOD = 10000;
    private BluetoothLeScanner mLEScanner;
    private ScanSettings settings;
    private List<ScanFilter> filters;
    private BluetoothGatt mGatt;
    private TextView device;
    private TextView data1;
    private TextView data2;
    private TextView data3;
    private TextView data4;
    String usersname="";
    private LineChart lineChart;
    ArrayList<String> labels;
    ArrayList<Entry> entries;
    int count=0;
    NaiveBayesian nbalgo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_h7__sensor__connection);mHandler = new Handler();
        Intent i=getIntent();
        usersname=i.getStringExtra("usernamee");
        nbalgo=new NaiveBayesian(usersname);
        device=(TextView)findViewById(R.id.name);
        data1=(TextView)findViewById(R.id.data1);
        data2=(TextView)findViewById(R.id.data2);
        data3=(TextView)findViewById(R.id.data3);
        data4=(TextView)findViewById(R.id.data4);
        lineChart = (LineChart) findViewById(R.id.chart);
        labels = new ArrayList<String>();
        entries = new ArrayList<>();
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE Not Supported",
                    Toast.LENGTH_SHORT).show();
            finish();
        }
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

    }
    @Override
    protected void onResume() {
        super.onResume();
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            if (Build.VERSION.SDK_INT >= 21) {
                mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
                settings = new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .build();
                filters = new ArrayList<ScanFilter>();
            }
            scanLeDevice(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            scanLeDevice(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mGatt == null) {
            return;
        }
        mGatt.close();
        mGatt = null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_CANCELED) {
                finish();
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (Build.VERSION.SDK_INT < 21) {
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    } else {
                        mLEScanner.stopScan(mScanCallback);

                    }
                }
            }, SCAN_PERIOD);
            if (Build.VERSION.SDK_INT < 21) {
                mBluetoothAdapter.startLeScan(mLeScanCallback);
            } else {
                mLEScanner.startScan(filters, settings, mScanCallback);
            }
        } else {
            if (Build.VERSION.SDK_INT < 21) {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            } else {
                mLEScanner.stopScan(mScanCallback);
            }
        }
    }


    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            ArrayList<String> value=new ArrayList<String>();
            int a=result.describeContents();
            int data_size=result.getScanRecord().getManufacturerSpecificData().size();
            final byte[] data = result.getScanRecord().getManufacturerSpecificData().get(107);
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for(byte byteChar : data)
                {    stringBuilder.append(String.format("%d ", byteChar));
                value.add(String.format("%d ", byteChar));}
                Log.d("Main data", value.toString());
                data1.setText(value.get(0));//data1
                data2.setText(value.get(1));//data2
                data3.setText(value.get(2));//data3
                data4.setText(value.get(3));//data4
                entries.add(new Entry(Float.parseFloat(value.get(2)), count));
                LineDataSet dataset = new LineDataSet(entries, "# of Calls");
                labels.add("Data" + count);
                LineData data1 = new LineData(labels, dataset);
                lineChart.setData(data1);
                lineChart.setDescription("Description");
                count++;

                //TODO: load data in mongodb
                new GetProductDetails().execute();
            }
            Log.d("Size of a data", new String(data_size + ""));
            Log.i("callbackType", String.valueOf(callbackType));
            Log.i("result", result.toString());
            BluetoothDevice btDevice = result.getDevice();
            Log.d("DeviceName:", btDevice.getName());
            device.setText(btDevice.getName());//device name
            connectToDevice(btDevice);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult sr : results) {
                Log.i("ScanResult - Results", sr.toString());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e("Scan Failed", "Error Code: " + errorCode);
        }
    };


    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.i("onLeScan", device.toString());
                            connectToDevice(device);
                        }
                    });
                }
            };

    public void connectToDevice(BluetoothDevice device) {
        if (mGatt == null) {
            mGatt = device.connectGatt(this, false, gattCallback);
            scanLeDevice(true);// will stop after first device detection
        }
    }

    class GetProductDetails extends AsyncTask<String, String, String> {
        ProgressDialog pDialog;
        int success = 0;

        @Override
        protected void onPreExecute() {


        }

        protected String doInBackground(String... args) {

            MongoClient mongoClient = new MongoClient( "192.168.43.89" , 27017 );
//            MongoClient mongoClient = new MongoClient( "DELL" , 27017 );
            MongoDatabase database = mongoClient.getDatabase("mydb");

            MongoCollection<Document> collection = database.getCollection("userDetails");

            MongoClient mongoClient2 = new MongoClient( "192.168.43.89" , 27017 );
            MongoDatabase database2 = mongoClient.getDatabase("mydb");


            Document doc = new Document("name", "MongoDB")
                    .append("username", usersname)
                    .append("data1", data1.getText().toString())
                    .append("data2", data2.getText().toString())
                    .append("data3", data3.getText().toString())
                    .append("data4", data4.getText().toString());


            collection.insertOne(doc);

            //MongoCollection<Document> collection2 = database.getCollection("userDetails");
            //MongoCursor<Document> cursorsend = collection.find().iterator();
            //healthcare.bayesian(cursorsend);
            nbalgo.insertToDB(data1.getText().toString(),data1.getText().toString(),data1.getText().toString(),data1.getText().toString());
            return null;
        }

        protected void onPostExecute(String file_url) {

        }
    }


    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i("onConnectionStateChange", "Status: " + status);
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.i("gattCallback", "STATE_CONNECTED");
                    gatt.discoverServices();
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.e("gattCallback", "STATE_DISCONNECTED");
                    break;
                default:
                    Log.e("gattCallback", "STATE_OTHER");
            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                List<BluetoothGattService> services = gatt.getServices();
                Log.i("onServicesDiscovered", services.toString());
                gatt.readCharacteristic(services.get(1).getCharacteristics().get
                        (0));
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic
                                                 characteristic, int status) {
              Log.i("onCharacteristicRead", characteristic.toString());
            //gatt.disconnect();
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_h7__sensor__connection, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
