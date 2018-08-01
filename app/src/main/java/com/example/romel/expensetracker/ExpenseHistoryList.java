package com.example.romel.expensetracker;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by romel on 7/26/2018.
 */

public class ExpenseHistoryList {

    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final DatabaseReference myDatabaseRef = database.getReference();


    static String[] expenses = new String[10];
    static boolean initializeList = true;
    int index = 0;

    int expenseHistoryListSize = 0;


    public ExpenseHistoryList() {
        if (initializeList) {

            initializeList = false;
        }
    }

    public void addExpense(Expense e) {
        if (index == 10) {
            index = 0;
            expenseHistoryListSize--;
        }
        expenses[index++] = e.toString();
        expenseHistoryListSize++;

    }
    public void addExpense(String s) {
        if (index == 10) {
            index = 0;
            expenseHistoryListSize--;
        }
        expenses[index++] = s;
        expenseHistoryListSize++;
    }

    public ArrayList<String> getArrayList() {
        return new ArrayList<String>(Arrays.asList(expenses));
    }


    public void setUpExpenseAL() {
        DatabaseReference expenseHistoryListSizeDR = myDatabaseRef.child("ExpenseHistory").child("size");
        expenseHistoryListSizeDR.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                expenseHistoryListSize = dataSnapshot.getValue(Integer.class);
                setUpExpenseAL();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        System.out.println(expenseHistoryListSize);
        for (int i = 0; i < expenseHistoryListSize; i++) {

            DatabaseReference dr = myDatabaseRef.child("ExpenseHistory").child("" + i);
            dr.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    System.out.println(dataSnapshot.getValue(String.class));
                    //expenseAL.add(Expense.parseString(dataSnapshot.getValue(String.class)));
                    //expenseStringAL.add(dataSnapshot.getValue(String.class));

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

}
