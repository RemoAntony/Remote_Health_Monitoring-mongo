package com.example.raghul.remote_health_monitoring;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import org.bson.Document;


public class MainActivity extends ActionBarActivity {

    TextView join;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        join=(TextView)findViewById(R.id.join);
        SpannableString content = new SpannableString("Join Now");
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        join.setText(content);
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
String loginid,passwordlogin;
    public void sign_in(View v)
    {
        EditText login=(EditText)findViewById(R.id.login_id);
        EditText password=(EditText)findViewById(R.id.password);
        if(login.getText().toString().isEmpty())
            Toast.makeText(getApplicationContext(),"Please enter the login id",Toast.LENGTH_SHORT).show();
        else if(password.getText().toString().isEmpty())
            Toast.makeText(getApplicationContext(),"Please enter the password",Toast.LENGTH_SHORT).show();

        //TODO: Check the login credentials by getting data from mongoDB
        else
        {
            loginid=login.getText().toString();
            passwordlogin=password.getText().toString();
            new GetProductDetails().execute();
        }
    }

    class GetProductDetails extends AsyncTask<String, String, String> {
        ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Validating...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }
        int success=0;
        protected String doInBackground(String... args) {

            MongoClient mongoClient = new MongoClient( "192.168.43.89" , 27017 );
            MongoDatabase database = mongoClient.getDatabase("mydb");
            MongoCollection<Document> collection = database.getCollection("userReg");
            MongoCursor<Document> cursor = collection.find().iterator();
            try {
                while (cursor.hasNext()) {
                   String checking=cursor.next().toJson().toString();
                    if(checking.contains(loginid)&&checking.contains(passwordlogin)) {
                        success = 1;
                        break;
                    }
                }
            } finally {
                cursor.close();
            }

            return null;
        }

        protected void onPostExecute(String file_url) {

            pDialog.dismiss();
            if(success==1) {
                Toast.makeText(MainActivity.this,"Welcome "+loginid,Toast.LENGTH_LONG).show();
                Intent intent = new Intent(MainActivity.this, H7_Sensor_Connection.class);
                intent.putExtra("usernamee",loginid);
                finish();
                startActivity(intent);
            }
            else Toast.makeText(MainActivity.this,"Username or password doesn't exist. Try Again!",Toast.LENGTH_LONG).show();
        }
    }




    public void sign_up(View v)
    {
        Intent signup=new Intent(MainActivity.this,Personal_Details.class);
        startActivity(signup);
        finish();
    }

}
