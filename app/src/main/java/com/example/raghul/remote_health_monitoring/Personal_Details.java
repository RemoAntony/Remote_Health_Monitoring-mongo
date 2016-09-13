package com.example.raghul.remote_health_monitoring;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.bson.Document;

import java.util.Calendar;


public class Personal_Details extends ActionBarActivity {

    private TextView male;
    private TextView female;
    private int gender=1;//1 for male and 0 for female
    private TextView dob;
    private static final int DATE_DIALOG_ID = 1;
    private int year;
    private int month;
    private int day;
    private String currentDate;
    private TextView height;
    private TextView weight;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal__details);
        male=(TextView)findViewById(R.id.male);
        male.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                male.setBackgroundColor(Color.parseColor("#0080ff"));
                female.setBackgroundColor(Color.parseColor("#FFFFFF"));
                gender=1;
            }
        });
        female=(TextView)findViewById(R.id.female);
        female.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                female.setBackgroundColor(Color.parseColor("#0080ff"));
                male.setBackgroundColor(Color.parseColor("#FFFFFF"));
                gender=0;
            }
        });
        dob=(TextView)findViewById(R.id.dob);
        View.OnClickListener listenerDate = new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                final Calendar c = Calendar.getInstance();
                year = c.get(Calendar.YEAR);
                month = c.get(Calendar.MONTH);
                day = c.get(Calendar.DAY_OF_MONTH);
                showDialog(DATE_DIALOG_ID);
            }
        };
        dob.setOnClickListener(listenerDate);
        height=(TextView)findViewById(R.id.height);
        height.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog d = new Dialog(Personal_Details.this);
                d.setTitle("NumberPicker");
                d.setContentView(R.layout.number_picker_dialog);
                Button b1 = (Button) d.findViewById(R.id.button1);
                TextView dimen=(TextView)d.findViewById(R.id.dimen_name);
                dimen.setText("cm");
                final NumberPicker np = (NumberPicker) d.findViewById(R.id.numberPicker1);
                np.setMaxValue(240);
                np.setMinValue(90);
                np.setValue(160);
                np.setWrapSelectorWheel(false);
                b1.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v) {
                        height.setText(String.valueOf(np.getValue()));
                        d.dismiss();
                    }
                });
                d.show();
            }
        });
        weight=(TextView)findViewById(R.id.weight);
        weight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog d = new Dialog(Personal_Details.this);
                d.setTitle("NumberPicker");
                d.setContentView(R.layout.number_picker_dialog);
                Button b1 = (Button) d.findViewById(R.id.button1);
                TextView dimen=(TextView)d.findViewById(R.id.dimen_name);
                dimen.setText("Kg");
                final NumberPicker np = (NumberPicker) d.findViewById(R.id.numberPicker1);
                np.setMaxValue(130);
                np.setMinValue(15);
                np.setValue(60);
                np.setWrapSelectorWheel(false);
                b1.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v) {
                        weight.setText(String.valueOf(np.getValue()));
                        d.dismiss();
                    }
                });
                d.show();
            }
        });
    }
    private void updateDisplay() {
        currentDate = new StringBuilder().append(day).append(".")
                .append(month + 1).append(".").append(year).toString();
    }
    DatePickerDialog.OnDateSetListener myDateSetListener = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker datePicker, int i, int j, int k) {

            year = i;
            month = j;
            day = k;
            updateDisplay();
            dob.setText(currentDate);
        }
    };
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DATE_DIALOG_ID:
                return new DatePickerDialog(this, myDateSetListener, year, month, day);
        }
        return null;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_personal__details, menu);
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


    public void sign_up(View v)
    {
        EditText name=(EditText)findViewById(R.id.name);
        EditText user=(EditText)findViewById(R.id.user);
        EditText pass=(EditText)findViewById(R.id.pass);
        EditText cnfm=(EditText)findViewById(R.id.cnfm_pass);

        sname = name.getText().toString();
        username = user.getText().toString();
        pwd1 = pass.getText().toString();
        pwd2 = cnfm.getText().toString();
        mWeight = weight.getText().toString();
        mHeight = height.getText().toString();
        mDOB = dob.getText().toString();
        sex = null;
        if(gender == 1)
            sex = "Male";
        else
            sex = "Female";

        if(sname.isEmpty() || username.isEmpty() || pwd1.isEmpty() || pwd2.isEmpty() ||
                mWeight.isEmpty() || mHeight.isEmpty() || mDOB.isEmpty() || sex.isEmpty())
            Toast.makeText(this, "Please fill all details", Toast.LENGTH_LONG).show();

        if(!pwd1.equals(pwd2))
            Toast.makeText(this, "Enter the password correctly", Toast.LENGTH_LONG).show();

        //TODO: Upload all the above details in userReg collection in mongoDB
        if(!sname.isEmpty() && !username.isEmpty() && !pwd1.isEmpty() && !pwd2.isEmpty() &&
                !mWeight.isEmpty() && !mHeight.isEmpty() && !mDOB.isEmpty() && !sex.isEmpty() && pwd1.equals(pwd2)) {

            new  GetProductDetails().execute();

        }
    }

    String username,sname,pwd1,pwd2,mWeight,mHeight,mDOB,sex;
    class GetProductDetails extends AsyncTask<String, String, String> {
        ProgressDialog pDialog;
        int success = 0;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(Personal_Details.this);
            pDialog.setMessage("Signing You Up");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();

        }

        protected String doInBackground(String... args) {
try {
    MongoClient mongoClient = new MongoClient("192.168.43.89", 27017);
//            MongoClient mongoClient = new MongoClient( "DELL" , 27017 );
    MongoDatabase database = mongoClient.getDatabase("mydb");

    MongoCollection<Document> collection = database.getCollection("userReg");

    Document doc = new Document("name", "MongoDB")
            .append("username", username)
            .append("password", pwd1)
            .append("name", sname)
            .append("weight", mWeight)
            .append("height", mHeight)
            .append("dob", mDOB)
            .append("sex", sex);


    collection.insertOne(doc);
}catch(Exception e){ex=e.toString(); }
            return null;
        }
String ex="";
        protected void onPostExecute(String file_url) {
            pDialog.dismiss();
            Toast.makeText(Personal_Details.this,ex+"Successfully Signed You Up",Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Personal_Details.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }

}
