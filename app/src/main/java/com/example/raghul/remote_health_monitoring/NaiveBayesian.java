package com.example.raghul.remote_health_monitoring;

/**
 * Created by REMO on 13-09-2016.
 */

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

import java.util.ArrayList;

class NaiveBayesian
{
    private String pid;
    private int count_n,count_a;

    private int count_bp_n = 0, count_bp_a = 0;
    private int count_hr_n = 0, count_hr_a = 0;
    private int count_br_n = 0, count_br_a = 0;
    private int count_spo2_n = 0, count_spo2_a = 0;
    private int count_p_n = 0, count_p_a = 0;

    private int counter = 0;

    private ArrayList<Integer> BPa, BRa, HRa, pa, spo2a;
    private ArrayList<String> classna;

    public NaiveBayesian(String id) {
        pid = id;
        HRa = new ArrayList<Integer>();
        BRa = new ArrayList<Integer>();
        BPa = new ArrayList<Integer>();
        pa = new ArrayList<Integer>();
        spo2a = new ArrayList<Integer>();
        classna = new ArrayList<String>();

        count_n = count_a = 0;

        //connect to mongo and build classifier
        MongoClient mongoClient = new MongoClient( "localhost" , 27017 );
        DB db = mongoClient.getDB( "test" );
        //boolean auth = db.authenticate(myUserName, myPassword);
        DBCollection coll = db.getCollection("mycol");

        BasicDBObject query = new BasicDBObject();
        query.put("pid", ""+pid);
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
        while(cursor.hasNext())
        {
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

    public String classify() {
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
            }
            else {
                //result is normal
                result = "normal";
            }

        }catch (Exception e) {
            System.out.println(e);
        }
        return result;
    }
    public void insertToDB(int HR, int BP, int BR, int pulse, int spo2)
    {
        //db table schema: pid, HR, BP, BR, pulse, spo2, class
        if(counter < 20) {
            HRa.add(HR);
            BRa.add(BR);
            BPa.add(BP);
            pa.add(pulse);
            spo2a.add(spo2);
            if(!checkBP(BP) && !checkBR(BR) && !checkHR(HR) && !checkPulse(pulse) && !checkSPo2(spo2))
                classna.add("normal");
            else
                classna.add("abnormal");
        }
        else {
            counter = 0;
            HRa.add(HR);
            BRa.add(BR);
            BPa.add(BP);
            pa.add(pulse);
            spo2a.add(spo2);
            if(!checkBP(BP) && !checkBR(BR) && !checkHR(HR) && !checkPulse(pulse) && !checkSPo2(spo2))
                classna.add("normal");
            else
                classna.add("abnormal");

            String result = classify();
            //now insert all the records in arraylist with class as result in mongodb
            try{
                MongoClient mongoClient = new MongoClient( "localhost" , 27017 );
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
    }
}
