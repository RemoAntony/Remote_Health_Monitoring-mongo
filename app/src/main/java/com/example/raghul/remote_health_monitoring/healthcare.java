package com.example.raghul.remote_health_monitoring;

import com.mongodb.Cursor;
import com.mongodb.client.MongoCursor;

import org.bson.Document;


/**
 * Created by REMO on 13-09-2016.
 */
public class healthcare {


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

        public static void bayesian(MongoCursor<Document> c)
        {
            int HR, BP, SPo2, BR, pulse;
            HR = BP = SPo2 = BR = pulse = -1;
            //get the data from sensor
            String gclass,result;

            try
            {
                //connect to mongodb
                //insert the current tuple
                //if an attribute value is -1, don't insert in mongo

                int count_n,count_a;
                count_n = count_a = 0;

                //read the data for the respective patient id and get it in a cursor
                //change the syntax
                MongoCursor<Document> cursor=c;
                while(cursor.hasNext()) {
                    String classn = cursor.get("class");
                    if(class.equals("abnormal")
                    count_a++;
                    else
                    count_n++;
                }

                double prob_n,prob_a;

                prob_n = count_n/((count_n+count_a)*1.00);
                prob_a = count_a/((count_n+count_a)*1.00);

                //move the cursor to first record
                int count_bp_n = 0, count_bp_a = 0;
                int count_hr_n = 0, count_hr_a = 0;
                int count_br_n = 0, count_br_a = 0;
                int count_spo2_n = 0, count_spo2_a = 0;
                int count_p_n = 0, count_p_a = 0;
                while(cursor.hasNext())
                {
                    int tempbp = cursor.get("BP");
                    int temphp = cursor.get("HR");
                    int tempbr = cursor.get("BR");
                    int tempspo2 = cursor.get("Spo2");
                    int tempp = cursor.get("pulse");
                    String classn = cursor.get("class");
                    //if the mongo record does not have that attribute, set it's value as -1

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


                double final_prob_n = 0, final_prob_a = 0;

                final_prob_a = prob_a * count_bp_a * count_br_a * count_hr_a * count_p_a * count_spo2_a;
                final_prob_a = final_prob_a / (Math.pow(count_a, 5));

                final_prob_n = prob_n * count_bp_n * count_br_n * count_hr_n * count_p_n * count_spo2_n;
                final_prob_n = final_prob_n / (Math.pow(count_n, 5));

                if(final_prob_a > final_prob_n) {
                    //result is abnormal
                }
                else {
                    //result is normal
                }

            }catch (Exception e) {
                System.out.println(e);
            }
        }
    }

