package com.example.romel.expensetracker;

import android.content.Intent;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ExpenseHistoryActivity extends AppCompatActivity {

    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final DatabaseReference myDatabaseRef = database.getReference();

    ArrayList<Expense> expenseAL;
    ArrayList<String> expenseStringAL;
    ListView expenseListView;
    ArrayAdapter<String> expAdapter;
    Intent erasedExpenseData;
    int eraseCount = 0;
    int expenseHistoryListSize = 0;
    int expenseHistoryListRecentIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_history);

        erasedExpenseData = new Intent();
        expenseAL = new ArrayList<Expense>();
        expenseStringAL =  new ArrayList<String>();
        expenseListView = (ListView) findViewById(R.id.ExpenseListView);
        expAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, expenseStringAL);
        expenseListView.setAdapter(expAdapter);

        DatabaseReference expenseHistoryListSizeDR = myDatabaseRef.child("ExpenseHistory").child("size");
        DatabaseReference expenseHistoryListRecentIndexDR = myDatabaseRef.child("ExpenseHistory").child("RecentIndex");

        expenseHistoryListSizeDR.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                expenseHistoryListSize = dataSnapshot.getValue(Integer.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        expenseHistoryListRecentIndexDR.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                expenseHistoryListRecentIndex = dataSnapshot.getValue(Integer.class);
                setUpExpenseAL();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        /*expenseListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Expense e = expenseAL.get(position);
                System.out.println(expenseAL.get(position).toString());
                expenseStringAL.remove(position);
                expenseAL.remove(position);
                erasedExpenseData.putExtra("" + eraseCount++, e.toString());
                expAdapter.notifyDataSetChanged();
                //myDatabaseRef.child("ExpenseHistory").child(position + "").removeValue();
                //myDatabaseRef.child("ExpenseHistory").child("size").setValue(--expenseHistoryListSize);
            }
        });
        */
    }

    private int j = 0;
    public void setUpExpenseAL() {
        System.out.println(expenseHistoryListSize);
        while (j < expenseHistoryListSize) {
            int currIndex = expenseHistoryListRecentIndex - j;
            if (currIndex < 0) currIndex = 10 + currIndex;
            DatabaseReference dr = myDatabaseRef.child("ExpenseHistory").child("" + currIndex);
            //System.out.println(j);
            dr.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //System.out.println(dataSnapshot.getValue(String.class));
                    expenseAL.add(Expense.parseString(dataSnapshot.getValue(String.class)));
                    expenseStringAL.add(dataSnapshot.getValue(String.class));
                    expAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            j++;
        }
        //expenseListView.setAdapter(expAdapter);
        /*System.out.println(expenseHistoryListSize);
        for (int i = 0; i < expenseHistoryListSize; i++) {
            k = i;
            DatabaseReference dr = myDatabaseRef.child("ExpenseHistory").child("" + i);
            dr.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    System.out.println(dataSnapshot.getValue(String.class));
                    expenseAL.add(Expense.parseString(dataSnapshot.getValue(String.class)));
                    expenseStringAL.add(dataSnapshot.getValue(String.class));
                    if (k == expenseHistoryListSize - 1) {
                        expenseListView.setAdapter(expAdapter);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }*/


    }

    @Override
    public void onBackPressed() {
        //erasedExpenseData.putExtra("eraseCount", eraseCount);
        //setResult(RESULT_CANCELED, erasedExpenseData);
        finish();
    }

}
