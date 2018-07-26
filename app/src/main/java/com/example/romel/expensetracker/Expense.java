package com.example.romel.expensetracker;

import android.icu.text.DecimalFormat;

/**
 * Created by romel on 7/19/2018.
 */

public class Expense {

    double amount;
    String type;
    String time;



    public Expense(double amount, String type, String time) {
        this.amount = amount;
        this.type = type;
        this.time = time;
    }

    public String toString() {
        return "$" + amount + ", " + type + ", " + time;
    }

    public static Expense parseString(String s) {

        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(0);
        int r = 1;
        int l = 1;
        double amt = 0.001;
        String eType = "";
        String eTime;
        while (true) {
            System.out.println(r);
            if (s.charAt(r) == ',' && amt == 0.001) {
                amt = Double.valueOf(df.format(Double.parseDouble(s.substring(l,r))));
                r += 2;
                l = r;
            }
            else if (s.charAt(r) == ',' && amt != 0.001){
                eType = s.substring(l,r);
                l = r + 2;
                break;
            }
            r++;
        }
        eTime = s.substring(l);
        return new Expense(amt, eType, eTime);
    }

}
