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
import android.os.Vibrator;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.bson.Document;

import java.util.ArrayList;
import java.util.List;


public class H7_Sensor_Connection extends ActionBarActivity {
    public boolean isConnected;
    public BluetoothAdapter mBluetoothAdapter;
    public int REQUEST_ENABLE_BT = 1;
    public Handler mHandler;
    public static final long SCAN_PERIOD = 10000;
    public BluetoothLeScanner mLEScanner;
    public ScanSettings settings;
    public List<ScanFilter> filters;
    public BluetoothGatt mGatt;
    public TextView device;
    public TextView data1;
    public TextView data2;
    public TextView data3;
    public TextView data4;
    String usersname="";
    public LineChart lineChart;
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
//        nbalgo=new NaiveBayesian(usersname);
        new Naive().execute();
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

    public void scanLeDevice(final boolean enable) {
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


    public ScanCallback mScanCallback = new ScanCallback() {
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
                //Log.d("Main data", value.toString());
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
            //Log.d("Size of a data", new String(data_size + ""));
            //Log.i("callbackType", String.valueOf(callbackType));
            //Log.i("result", result.toString());
            BluetoothDevice btDevice = result.getDevice();
            //Log.d("DeviceName:", btDevice.getName());
            device.setText(btDevice.getName());//device name
            connectToDevice(btDevice);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult sr : results) {
                //Log.i("ScanResult - Results", sr.toString());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            //Log.e("Scan Failed", "Error Code: " + errorCode);
        }
    };


    public BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //Log.i("onLeScan", device.toString());
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

    String hr,br,bp,sp,p;
    class GetProductDetails extends AsyncTask<String, String, String> {
        ProgressDialog pDialog;
        int success = 0;

        @Override
        protected void onPreExecute() {


        }

        protected String doInBackground(String... args) {

            MongoClient mongoClient = new MongoClient( "192.168.0.6" , 27017 );
//            MongoClient mongoClient = new MongoClient( "DELL" , 27017 );
            MongoDatabase database = mongoClient.getDatabase("mydb");

            MongoCollection<Document> collection = database.getCollection("userDetails");

            MongoClient mongoClient2 = new MongoClient( "192.168.0.6" , 27017 );
            MongoDatabase database2 = mongoClient.getDatabase("mydb");

            int hrint = Integer.parseInt(data4.getText().toString().trim());
            String res="";
            if(hrint <60 || hrint > 100)
                res = "abnormal";
            else
                res = "normal";
            Document doc = new Document("name", "MongoDB")
                    .append("username", usersname)
                    .append("data1", data1.getText().toString())
                    .append("data2", data2.getText().toString())
                    .append("data3", data3.getText().toString())
                    .append("data4", data4.getText().toString())
                    .append("class", res);

            collection.insertOne(doc);

            //MongoCollection<Document> collection2 = database.getCollection("userDetails");
            //MongoCursor<Document> cursorsend = collection.find().iterator();
            //healthcare.bayesian(cursorsend);
            //nbalgo.insertToDB(data1.getText().toString(),data2.getText().toString(),data3.getText().toString(),data4.getText().toString(),"-1");
            hr=data1.getText().toString();
            br=data2.getText().toString();
            bp=data3.getText().toString();
            sp=data4.getText().toString();
            p="-1";

            return res+" "+hrint;
        }

        protected void onPostExecute(String file_url) {
            final String res = file_url;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(H7_Sensor_Connection.this, res, Toast.LENGTH_LONG).show();
                    if(res.equals("abnormal")) {
                        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

// Vibrate for 400 milliseconds
                        v.vibrate(400);
                    }

                }
            });
            new Classify().execute();
        }
    }


    public final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            //Log.i("onConnectionStateChange", "Status: " + status);
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    //Log.i("gattCallback", "STATE_CONNECTED");
                    gatt.discoverServices();
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    //Log.e("gattCallback", "STATE_DISCONNECTED");
                    break;
                default:
                    //Log.e("gattCallback", "STATE_OTHER");
            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                List<BluetoothGattService> services = gatt.getServices();
                //Log.i("onServicesDiscovered", services.toString());
                gatt.readCharacteristic(services.get(1).getCharacteristics().get
                        (0));
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic
                                                 characteristic, int status) {
              //Log.i("onCharacteristicRead", characteristic.toString());
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
    public String pid;
    public int count_n,count_a;

    public int count_bp_n = 0, count_bp_a = 0;
    public int count_hr_n = 0, count_hr_a = 0;
    public int count_br_n = 0, count_br_a = 0;
    public int count_spo2_n = 0, count_spo2_a = 0;
    public int count_p_n = 0, count_p_a = 0;

    public int counter = 0;

    public ArrayList<Integer> BPa, BRa, HRa, pa, spo2a;
    public ArrayList<String> classna;

    class Naive extends AsyncTask<String, String, String> {
        ProgressDialog pDialog;
        int success = 0;

        @Override
        protected void onPreExecute() {


        }

        protected String doInBackground(String... args) {

            pid = usersname;
            HRa = new ArrayList<Integer>();
            BRa = new ArrayList<Integer>();
            BPa = new ArrayList<Integer>();
            pa = new ArrayList<Integer>();
            spo2a = new ArrayList<Integer>();
            classna = new ArrayList<String>();

            count_n = count_a = 0;

            //connect to mongo and build classifier
            MongoClient mongoClient = new MongoClient( "192.168.0.6" , 27017 );
            DB db = mongoClient.getDB( "mydb" );
            //boolean auth = db.authenticate(myUserName, myPassword);
            DBCollection coll = db.getCollection("userDetails");

            BasicDBObject query = new BasicDBObject();
            query.put("pid", "" + pid);
            DBCursor cursor = coll.find(query);

            while(cursor.hasNext()) {
                DBObject rec = cursor.next();
                String classn = rec.get("class").toString();
                if(classn.equals("abnormal"))
                    count_a++;
                else
                    count_n++;
            }

            //move the cursor to first record
            cursor = coll.find(query);
            int countc = 0;
            while(cursor.hasNext())
            {
                countc++;
                DBObject rec = cursor.next();
                int tempbp, temphr, tempbr, tempspo2, tempp;
                if(rec.get("BP") != null)
                    tempbp = Integer.parseInt(rec.get("BP").toString());
                else
                    tempbp = -1;
                if(rec.get("BP") != null)
                    temphr = Integer.parseInt(rec.get("HR").toString());
                else
                    temphr = -1;
                if(rec.get("BP") != null)
                    tempbr = Integer.parseInt(rec.get("BR").toString());
                else
                    tempbr = -1;
                if(rec.get("BP") != null)
                    tempspo2 = Integer.parseInt(rec.get("spo2").toString());
                else
                    tempspo2 = -1;
                if(rec.get("BP") != null)
                    tempp = Integer.parseInt(rec.get("pulse").toString());
                else
                    tempp = -1;
                String classn = rec.get("BP").toString();

                if(checkBP(tempbp) == true && classn.equals("abnormal"))
                    count_bp_a++;
                else if(checkBP(tempbp) == false && classn.equals("normal"))
                    count_bp_n++;

                if(checkHR(temphr) == true && classn.equals("abnormal"))
                    count_hr_a++;
                else if(checkHR(temphr) == false && classn.equals("normal"))
                    count_hr_n++;

                if(checkBR(tempbr) == true && classn.equals("abnormal"))
                    count_br_a++;
                else if(checkBR(tempbr) == false && classn.equals("normal"))
                    count_br_n++;

                if(checkSPo2(tempspo2) == true && classn.equals("abnormal"))
                    count_spo2_a++;
                else if(checkSPo2(tempspo2) == false && classn.equals("normal"))
                    count_spo2_n++;

                if(checkPulse(tempp) == true && classn.equals("abnormal"))
                    count_p_a++;
                else if(checkPulse(tempp) == false && classn.equals("normal"))
                    count_p_n++;
            }
            return "Naive size:"+countc;
        }
        public boolean checkBP(int x) {
            if(x == -1)
                return false;
            if(x < 60 || x > 110)
                return true;
            return false;
        }

        public boolean checkHR(int x) {
            if(x == -1)
                return false;
            if(x < 60 || x > 100)
                return true;
            return false;
        }
        public boolean checkBR(int x) {
            if(x == -1)
                return false;
            if(x < 12 || x > 30)
                return true;
            return false;
        }
        public boolean checkSPo2(int x) {
            if(x == -1)
                return false;
            if(x < 90 || x > 100)
                return true;
            return false;
        }
        public boolean checkPulse(int x) {
            if(x == -1)
                return false;
            if(x < 60 || x > 100)
                return true;
            return false;
        }

        protected void onPostExecute(String file_url) {
            final String res = file_url;
            /*runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(H7_Sensor_Connection.this, res, Toast.LENGTH_SHORT).show();
                }
            });*/
        }
    }



    public boolean checkBP(int x) {
        if(x == -1)
            return false;
        if(x < 60 || x > 110)
            return true;
        return false;
    }

    public boolean checkHR(int x) {
        if(x == -1)
            return false;
        if(x < 60 || x > 100)
            return true;
        return false;
    }
    public boolean checkBR(int x) {
        if(x == -1)
            return false;
        if(x < 12 || x > 30)
            return true;
        return false;
    }
    public boolean checkSPo2(int x) {
        if(x == -1)
            return false;
        if(x < 90 || x > 100)
            return true;
        return false;
    }
    public boolean checkPulse(int x) {
        if(x == -1)
            return false;
        if(x < 60 || x > 100)
            return true;
        return false;
    }
    String result22="check";
    class Classify extends AsyncTask<String, String, String> {
        ProgressDialog pDialog;
        int success = 0;

        @Override
        protected void onPreExecute() {


        }

        protected String doInBackground(String... args) {
            String ccc="empty";
            //Log.d("Charizard","doInbackground");
            int HR = Integer.parseInt(hr.trim());
            int BR = Integer.parseInt(br.trim());
            int BP = Integer.parseInt(bp.trim());
            int pulse = Integer.parseInt(p.trim());
            int spo2 = Integer.parseInt(sp.trim());
            //db table schema: pid, HR, BP, BR, pulse, spo2, class
            if(counter < 20) {
                HRa.add(HR);
                BRa.add(BR);
                BPa.add(BP);
                pa.add(pulse);
                spo2a.add(spo2);
                if(!checkBP(BP) && !checkBR(BR) && !checkHR(HR) && !checkPulse(pulse) && !checkSPo2(spo2)) {
                    classna.add("normal");
                    ccc= "normal";
                }
                else {
                    classna.add("abnormal");
                    ccc = "normal";
                }
            }
            else {
                counter = 0;
                HRa.add(HR);
                BRa.add(BR);
                BPa.add(BP);
                pa.add(pulse);
                spo2a.add(spo2);
                if(!checkBP(BP) && !checkBR(BR) && !checkHR(HR) && !checkPulse(pulse) && !checkSPo2(spo2)) {
                    classna.add("normal");
                    ccc= "normal";
                }
                else {
                    classna.add("abnormal");
                    ccc= "abnormal";
                }

                String result = classify();
                result22="size "+classna.size();
                //now insert all the records in arraylist with class as result in mongodb
                try{
                    MongoClient mongoClient = new MongoClient( "192.168.0.6" , 27017 );
                    DB db = mongoClient.getDB( "test" );
                    //boolean auth = db.authenticate(myUserName, myPassword);
                    DBCollection coll = db.getCollection("mycol");

                    for(int i = 0 ; i < 20 ; i++) {
                        BasicDBObject doc = new BasicDBObject();
                        doc.append("pid", ""+pid);
                        if(HRa.get(i) != -1)
                            doc.append("HR", ""+HRa.get(i));
                        if(BPa.get(i) != -1)
                            doc.append("BP", ""+BPa.get(i));
                        if(BRa.get(i) != -1)
                            doc.append("BR", ""+BRa.get(i));
                        if(pa.get(i) != -1)
                            doc.append("pulse", ""+pa.get(i));
                        if(spo2a.get(i) != -1)
                            doc.append("spo2", ""+spo2a.get(i));
                        doc.append("class", result);

                        coll.insert(doc);
                    }
                }catch(Exception e){

                }
                HRa.clear();
                BRa.clear();
                BPa.clear();
                pa.clear();
                spo2a.clear();
                classna.clear();
            }
            return "size "+classna.size()+" "+ccc;
        }

        public String classify() {
            //Log.d("Charizard","start");
            String result = null;
            try
            {
                //get the result
                for(int i = 0 ; i < 20 ; i++) {
                    if(classna.get(i).equals("abnormal"))
                        count_a++;
                    else
                        count_n++;
                }

                for(int i = 0 ; i < 20 ; i++) {
                    if(checkBP(BPa.get(i)) == true && classna.get(i).equals("abnormal"))
                        count_bp_a++;
                    else if(checkBP(BPa.get(i)) == false && classna.get(i).equals("normal"))
                        count_bp_n++;

                    if(checkHR(HRa.get(i)) == true && classna.get(i).equals("abnormal"))
                        count_hr_a++;
                    else if(checkHR(HRa.get(i)) == false && classna.get(i).equals("normal"))
                        count_hr_n++;

                    if(checkBR(BRa.get(i)) == true && classna.get(i).equals("abnormal"))
                        count_br_a++;
                    else if(checkBR(BRa.get(i)) == false && classna.get(i).equals("normal"))
                        count_br_n++;

                    if(checkSPo2(spo2a.get(i)) == true && classna.get(i).equals("abnormal"))
                        count_spo2_a++;
                    else if(checkSPo2(spo2a.get(i)) == false && classna.get(i).equals("normal"))
                        count_spo2_n++;

                    if(checkPulse(pa.get(i)) == true && classna.get(i).equals("abnormal"))
                        count_p_a++;
                    else if(checkPulse(pa.get(i)) == false && classna.get(i).equals("normal"))
                        count_p_n++;

                }

                double prob_n,prob_a;

                prob_n = count_n/((count_n+count_a)*1.00);
                prob_a = count_a/((count_n+count_a)*1.00);

                double final_prob_n = 0, final_prob_a = 0;

                final_prob_a = prob_a * count_bp_a * count_br_a * count_hr_a * count_p_a * count_spo2_a;
                final_prob_a = final_prob_a / (Math.pow(count_a, 5));

                final_prob_n = prob_n * count_bp_n * count_br_n * count_hr_n * count_p_n * count_spo2_n;
                final_prob_n = final_prob_n / (Math.pow(count_n, 5));

                if(final_prob_a > final_prob_n) {
                    //result is abnormal
                    result = "abnormal";
                    //Log.d("Charizard",result);
                }
                else {
                    //result is normal
                    result = "normal";
                    //Log.d("Charizard",result);
                }

            }catch (Exception e) {
                System.out.println(e);
            }
            //result22=result;
            return result;
        }
        protected void onPostExecute(String file_url) {
            //Log.e("Mewtwo",result22);
            final String res = file_url;
/*            runOnUiThread(new Runnable() {
                @Override
                public void run() {
 //                   Toast.makeText(H7_Sensor_Connection.this,res,Toast.LENGTH_LONG).show();
                }
            });*/
        }
    }

}