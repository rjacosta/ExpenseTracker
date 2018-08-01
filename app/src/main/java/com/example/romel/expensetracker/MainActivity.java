package com.example.romel.expensetracker;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.icu.text.DecimalFormat;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.DigestException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {


    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final DatabaseReference myDatabaseRef = database.getReference();

    private TextView titleText;

    private double totalAmt = 0.0;
    private double dadAmt = 0.0;
    private double prevMonthAmt = 0.0;
    private double foodAmt = 0.0;
    private double gasAmt = 0.0;
    private double miscAmt = 0.0;

    private Button addButton;
    private Spinner expenseTypeSpinner;

    private EditText amountEditText;
    private TextView totalTextView;
    private TextView dadTextView;
    private TextView prevMonthTextView;
    private TextView foodTextView;
    private TextView gasTextView;
    private TextView miscTextView;

    private String[] months = {"January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"};
    private String[] expenseTypes = {"Dad", "Prev Month", "Food", "Gas", "Misc"};
    private String currentMonthString;
    private String prevMonthString;

    private DecimalFormat df;

    private Button expenseHistoryButton;
    private int expenseHistoryRecentIndex;
    private int expenseHistorySize;

    private Button resetExpensesButton;
    private int daysSinceLastReset = 0;

    //ExpenseHistoryList eHL = new ExpenseHistoryList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        expenseHistoryButton = (Button) findViewById(R.id.ExpenseHistoryButton);
        expenseHistoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ExpenseHistoryActivity.class);
                //startActivityForResult(intent,0);
                startActivity(intent);
            }
        });

        df = new DecimalFormat("#.##");
        df.setRoundingMode(0);
        Calendar calendar = Calendar.getInstance();

        titleText = (TextView) findViewById(R.id.TitleText);
        currentMonthString = months[calendar.get(Calendar.MONTH)];
        titleText.setText(currentMonthString + " Expenses");

        expenseTypeSpinner = (Spinner) findViewById(R.id.ExpenseSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.expense_type_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        expenseTypeSpinner.setAdapter(adapter);

        totalTextView = (TextView) findViewById(R.id.ExpenseTotal);
        amountEditText = (EditText) findViewById(R.id.AmountText);
        dadTextView = (TextView) findViewById(R.id.DadText);
        prevMonthTextView = (TextView) findViewById(R.id.PrevMonthText);
        foodTextView = (TextView) findViewById(R.id.FoodText);
        gasTextView = (TextView) findViewById(R.id.GasText);
        miscTextView = (TextView) findViewById(R.id.MiscText);

        prevMonthString = months[calendar.get(Calendar.MONTH) - 1];
        prevMonthTextView.setText(prevMonthString + ": $ 0.00");

        initializeExpenses();

        setUpExpenseEditing();

        resetExpensesButton = (Button) findViewById(R.id.ResetButton);
        resetExpensesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Reset Expenses")
                        .setMessage("Are you sure you want to reset your expenses?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //do the reset
                                dialog.dismiss();
                                AlertDialog.Builder builder2 = new AlertDialog.Builder(MainActivity.this);
                                builder2.setTitle("ResetExpenses")
                                        .setMessage("Is it a new month?")
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                resetExpenses();
                                            }
                                        })
                                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Toast.makeText(getApplicationContext(),
                                                        "Only reset your expenses if its a new month!" ,
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .show();
                                /*
                                resetExpensesButton.setVisibility(View.GONE);
                                FileOutputStream outputStream;
                                String fileContents = "T";
                                String filename = "resetExpensesData";
                                try {
                                    outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                                    outputStream.write(fileContents.getBytes());
                                    outputStream.close();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }*/
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //do nothing
                            }
                        })
                        .show();


            }
        });
        //resetExpensesButton.setVisibility(View.GONE);
        //setUpResetExpenseData();

    }

    private void resetExpenses() {
        myDatabaseRef.child("Month Archive").child("test").setValue("data");
        Map<String, Double> expenseData = new HashMap<String, Double>();
        expenseData.put("Dad", dadAmt);
        expenseData.put("Prev Month", prevMonthAmt);
        expenseData.put("Food", foodAmt);
        expenseData.put("Gas", gasAmt);
        expenseData.put("Misc", miscAmt);
        totalTextView.setText("$ 0.00");
        dadTextView.setText("Dad $ 0.00");
        prevMonthTextView.setText(prevMonthString + ": $ 0.00");
        foodTextView.setText("Food: $ 0.00");
        gasTextView.setText("Gas: $ 0.00");
        miscTextView.setText("Misc: $ 0.00");
    }

    private void initializeExpenses() {

        DatabaseReference currDBRef = myDatabaseRef.child("Dad");
        currDBRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Double currDBRefVal = dataSnapshot.getValue(Double.class);
                currDBRefVal = Double.valueOf(df.format(currDBRefVal));
                dadTextView.setText("Dad: $ " + currDBRefVal);
                dadAmt = currDBRefVal;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        currDBRef = myDatabaseRef.child("Prev Month");
        currDBRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Double currDBRefVal = dataSnapshot.getValue(Double.class);
                currDBRefVal = Double.valueOf(df.format(currDBRefVal));
                prevMonthTextView.setText(prevMonthString + ": $ " + currDBRefVal);
                prevMonthAmt = currDBRefVal;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        currDBRef = myDatabaseRef.child("Food");
        currDBRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Double currDBRefVal = dataSnapshot.getValue(Double.class);
                currDBRefVal = Double.valueOf(df.format(currDBRefVal));
                foodTextView.setText("Food: $ " + currDBRefVal);
                foodAmt = currDBRefVal;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        currDBRef = myDatabaseRef.child("Gas");
        currDBRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Double currDBRefVal = dataSnapshot.getValue(Double.class);
                currDBRefVal = Double.valueOf(df.format(currDBRefVal));
                gasTextView.setText("Gas: $ " + currDBRefVal);
                gasAmt = currDBRefVal;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        currDBRef = myDatabaseRef.child("Misc");
        currDBRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Double currDBRefVal = dataSnapshot.getValue(Double.class);
                currDBRefVal = Double.valueOf(df.format(currDBRefVal));
                miscTextView.setText("Misc: $ " + currDBRefVal);
                miscAmt = currDBRefVal;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        currDBRef = myDatabaseRef.child("Total");
        currDBRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Double currDBRefVal = dataSnapshot.getValue(Double.class);
                currDBRefVal = Double.valueOf(df.format(currDBRefVal));
                totalTextView.setText("$ " + currDBRefVal);
                totalAmt = currDBRefVal;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        currDBRef = myDatabaseRef.child("ExpenseHistory").child("RecentIndex");
        currDBRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                expenseHistoryRecentIndex = dataSnapshot.getValue(Integer.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        currDBRef = myDatabaseRef.child("ExpenseHistory").child("size");
        currDBRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                expenseHistorySize = dataSnapshot.getValue(Integer.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void setUpExpenseEditing() {
        addButton = (Button) findViewById(R.id.AddButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String selectedExpenseString = expenseTypeSpinner.getSelectedItem().toString();

                String newExpenseAmtString = amountEditText.getText().toString();
                int i = newExpenseAmtString.length();
                int counter = 0;
                if (newExpenseAmtString.contains(".")) {
                    while (newExpenseAmtString.charAt(--i) != '.') {
                        counter++;
                    }
                    if (counter > 2) {
                        newExpenseAmtString = newExpenseAmtString.substring(0, i + 3);
                    }
                }

                double newExpenseAmtDouble = Double.valueOf(df.format(Double.parseDouble(newExpenseAmtString)));
                totalAmt = Double.valueOf(df.format(totalAmt + newExpenseAmtDouble));
                totalTextView.setText("$ " + totalAmt);
                myDatabaseRef.child("Total").setValue(totalAmt);
                Calendar cal = Calendar.getInstance();
                String time = cal.getTime().toString();

                if (selectedExpenseString.equals("Dad")) {
                    dadAmt = Double.valueOf(df.format(dadAmt + newExpenseAmtDouble));
                    dadTextView.setText("Dad: $ " + dadAmt);
                    myDatabaseRef.child("Dad").setValue(dadAmt);
                }
                else if (selectedExpenseString.equals("Prev Month")) {
                    prevMonthAmt = Double.valueOf(df.format(prevMonthAmt + newExpenseAmtDouble));
                    prevMonthTextView.setText(prevMonthString + ": $ " + prevMonthAmt);
                    myDatabaseRef.child("Prev Month").setValue(prevMonthAmt);
                }
                else if (selectedExpenseString.equals("Food")) {
                    foodAmt = Double.valueOf(df.format(foodAmt + newExpenseAmtDouble));
                    foodTextView.setText("Food: $ " + foodAmt);
                    myDatabaseRef.child("Food").setValue(foodAmt);
                }
                else if (selectedExpenseString.equals("Gas")) {
                    gasAmt = Double.valueOf(df.format(gasAmt + newExpenseAmtDouble));
                    gasTextView.setText("Gas: $ " + gasAmt);
                    myDatabaseRef.child("Gas").setValue(gasAmt);
                }
                else if (selectedExpenseString.equals("Misc")) {
                    miscAmt = Double.valueOf(df.format(miscAmt + newExpenseAmtDouble));
                    miscTextView.setText("Misc: $ " + miscAmt);
                    myDatabaseRef.child("Misc").setValue(miscAmt);
                }

                if (selectedExpenseString.equals("Prev Month")) {
                    Toast.makeText(getApplicationContext(), "Added $ " + newExpenseAmtDouble + " to " + prevMonthString,
                            Toast.LENGTH_SHORT).show();
                }
                else Toast.makeText(getApplicationContext(), "Added $ " + newExpenseAmtDouble + " to " + selectedExpenseString,
                        Toast.LENGTH_SHORT).show();

                Expense recentExpense = new Expense(newExpenseAmtDouble, selectedExpenseString, time);
                expenseHistoryRecentIndex++;
                if (expenseHistoryRecentIndex == 10) expenseHistoryRecentIndex = 0;
                myDatabaseRef.child("ExpenseHistory").child(expenseHistoryRecentIndex + "").setValue(recentExpense.toString());
                if (expenseHistorySize == 10) expenseHistorySize--;
                expenseHistorySize++;
                myDatabaseRef.child("ExpenseHistory").child("RecentIndex").setValue(expenseHistoryRecentIndex);
                myDatabaseRef.child("ExpenseHistory").child("size").setValue(expenseHistorySize);


            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0 && resultCode == RESULT_CANCELED) {
            /*int eraseCount = data.getIntExtra("eraseCount", 0);
            System.out.println("Erase Count " + eraseCount);
            for (int i = 0; i < eraseCount; i++) {
                Expense e = Expense.parseString(data.getStringExtra("" + i));
                System.out.println("Expense to erase: " + e.toString());
            }*/
        }
    }

    private void setUpResetExpenseData() {
        String filename = "resetExpensesData";
        File file = getFileStreamPath(filename);
        String fileContents = "F";
        /*
        Calendar calendar = Calendar.getInstance();


        if (file == null || !file.exists()) {
            System.out.println("Creating new file");
            FileOutputStream outputStream;
            try {
                outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                outputStream.write(fileContents.getBytes());
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            System.out.println("file exists");
            FileInputStream inputStream;
            byte[] buffer = new byte[1];
            try {
                inputStream = openFileInput(filename);
                inputStream.read(buffer);
                String s = new String(buffer);
                //"F" if we need a reset
                if (s.equals("F")) {
                    System.out.println("Read F!!");
                    resetExpensesButton.setVisibility(View.VISIBLE);
                }
                //"T" if we don't need a reset
                else if (s.equals("T")) {
                    System.out.println("Read T!!");
                    resetExpensesButton.setVisibility(View.GONE);
                }
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        */
    }

}
