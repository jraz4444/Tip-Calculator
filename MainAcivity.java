//make sure to look over code and give comments if needed
//take out any repeated code
package com.example.tipcalculatorupdated;

import static com.example.tipcalculatorupdated.R.id.editTextNumberDecimalTipAmount;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MainActivity extends AppCompatActivity {
    private static final double MAX_TIP_PERCENT = 100.0; // Maximum tip percent
    private static final double MAX_BILL_AMOUNT = 9999999999.99;

    // Declare UI components
    private Button buttonPlusTip;
    private Button buttonMinusTip;
    private Button buttonCalculateTip;
    private Button buttonRoundTip;
    private Button buttonRoundTotalBill;
    private Button buttonSplitBill;
    private EditText editTextNumberDecimalBillAmount;
    private EditText editTextNumberTipPercent;
    private EditText editTextNumberDecimalTipAmount;
    private EditText editTextNumberDecimalTotalDue;
    private EditText editTextNumberPeople;
    private TextView textViewPersonContribution;

    // Declare variables
    private double billAmount = 0.0;   // Bill amount in USD initially
    private double myTipPercent = 0.0; // Tip percentage
    private double tipAmount = 0.0;    // Calculated tip
    private double totalDue = 0.0;     // Total bill
    private final Lock calculationLock = new ReentrantLock();

    @SuppressLint({"WrongViewCast", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        buttonPlusTip = findViewById(R.id.buttonPlusTip);
        buttonMinusTip = findViewById(R.id.buttonMinusTip);
        buttonCalculateTip = findViewById(R.id.buttonCalculateTip);
        buttonRoundTip = findViewById(R.id.buttonRoundTip);
        buttonRoundTotalBill = findViewById(R.id.buttonRoundBill);
        buttonSplitBill = findViewById(R.id.buttonSplitBill);
        editTextNumberDecimalBillAmount = findViewById(R.id.editTextNumberDecimalBillAmount);
        editTextNumberTipPercent = findViewById(R.id.editTextNumberTipPercent);
        editTextNumberDecimalTipAmount = findViewById(R.id.editTextNumberDecimalTipAmount);
        editTextNumberDecimalTotalDue = findViewById(R.id.editTextNumberDecimalTotalDue);
        editTextNumberPeople = findViewById(R.id.editTextNumberPeople);
        textViewPersonContribution = findViewById(R.id.textViewPersonContribution);

        // Set initial values
        editTextNumberTipPercent.setText(String.format("%.1f", myTipPercent));
        editTextNumberDecimalBillAmount.setText("$0.00");

        // Add TextWatchers
        setupTextWatchers();

        // Set click listeners
        buttonPlusTip.setOnClickListener(view -> adjustTipPercent(0.1));
        buttonMinusTip.setOnClickListener(view -> adjustTipPercent(-0.1));
        buttonCalculateTip.setOnClickListener(view -> calculateTip());
        buttonRoundTip.setOnClickListener(view -> roundTipAmount());
        buttonRoundTotalBill.setOnClickListener(view -> roundTotalBill());
        buttonSplitBill.setOnClickListener(view -> splitBill());
    }

    private void setupTextWatchers() {
        // TextWatcher for bill amount
        editTextNumberDecimalBillAmount.addTextChangedListener(new TextWatcher() {
            private String current = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals(current)) {
                    if (!s.toString().equals(current)) {
                        current = s.toString();
                        try {


                            String billAmountString = s.toString().replace("$", "").replace("€", "").replace(",", "").replace("%", "");
                            if (!billAmountString.isEmpty()) {
                                double billAmount = Double.parseDouble(billAmountString);
                                if (billAmount > MAX_BILL_AMOUNT) {
                                    Toast.makeText(MainActivity.this, "Bill amount cannot exceed $9,999,999,999.99", Toast.LENGTH_SHORT).show();
                                    editTextNumberDecimalBillAmount.setText(String.format("%,.2f", MAX_BILL_AMOUNT));
                                } else {
                                    calculateTip();
                                }
                            }
                        } catch (NumberFormatException e) {
                            Toast.makeText(MainActivity.this, "Invalid bill amount", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
            });



            // TextWatcher for tip percent
        editTextNumberTipPercent.addTextChangedListener(new TextWatcher() {
            private String current = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals(current)) {
                    current = s.toString();
                    String tipPercentString = current.replace("%", "");

                    try {
                        double inputTip = Double.parseDouble(s.toString());
                        if (inputTip >= 0) {
                            myTipPercent = inputTip;
                            myTipPercent = Math.round(inputTip * 10) / 10.0;
                            editTextNumberTipPercent.setText(String.format("%.1f", myTipPercent));
                            calculateTip();
                        } else if (inputTip > MAX_TIP_PERCENT) {
                            Toast.makeText(MainActivity.this, "Tip percent cannot exceed 100%", Toast.LENGTH_SHORT).show();
                            editTextNumberTipPercent.setText(String.format("%.1f", MAX_TIP_PERCENT));
                            myTipPercent = MAX_TIP_PERCENT;
                            calculateTip();
                        } else {
                            Toast.makeText(MainActivity.this, "Tip percent must be positive", Toast.LENGTH_SHORT).show();
                        }
                    } catch (NumberFormatException e) {
                        if(!tipPercentString.isEmpty())
                        {
                        Toast.makeText(MainActivity.this, "", Toast.LENGTH_SHORT).show();

                        }
                }
                }
            }
        });
    }

    private void calculateTip() {
        calculationLock.lock();
        try {
            String billAmountString = editTextNumberDecimalBillAmount.getText().toString().replace(",", "").replace("$", "");
            if (!billAmountString.isEmpty()) {
                try {
                    double billAmount = Double.parseDouble(billAmountString);

                    // Round bill amount to two decimal places
                    billAmount = Math.round(billAmount * 100.0) / 100.0;

                    // Check if billAmount exceeds maximum value
                    if (billAmount > MAX_BILL_AMOUNT) {
                        Toast.makeText(this, "Bill amount cannot exceed $9,999,999,999.99", Toast.LENGTH_SHORT).show();
                        return; // Exit if the bill amount exceeds the limit
                    }

                    double tipAmount = (billAmount * myTipPercent) / 100.0;
                    double totalDue = billAmount + tipAmount;

                    // Update UI with the formatted values
                    editTextNumberDecimalBillAmount.setText(String.format("$%.2f", billAmount));
                    editTextNumberDecimalTipAmount.setText(String.format("$%.2f", tipAmount));
                    editTextNumberDecimalTotalDue.setText(String.format("$%.2f", totalDue));
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Invalid bill amount", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Please enter a valid bill amount", Toast.LENGTH_SHORT).show();
            }
        } finally {
            calculationLock.unlock();
        }
    }

    private void roundTipAmount() {
        calculationLock.lock();
        try {
            String tipAmountString = editTextNumberDecimalTipAmount.getText().toString().replace("$", "").replace("€", "").replace(",", "");
            if (!tipAmountString.isEmpty()) {
                double tipAmount = Double.parseDouble(tipAmountString);
                tipAmount = Math.round(tipAmount);
                editTextNumberDecimalTipAmount.setText(String.format("%,.2f", tipAmount));
            } else {
                Toast.makeText(this, "No tip amount to round", Toast.LENGTH_SHORT).show();
            }
        } finally {
            calculationLock.unlock();
        }
    }


    private void roundTotalBill() {
        calculationLock.lock();
        try {
            String totalDueString = editTextNumberDecimalTotalDue.getText().toString().replace("$", "").replace("€", "").replace(",", "");
            if (!totalDueString.isEmpty()) {
                double totalDue = Double.parseDouble(totalDueString);
                totalDue = Math.round(totalDue);
                editTextNumberDecimalTotalDue.setText(String.format("%,.2f", totalDue));
            } else {
                Toast.makeText(this, "No total bill amount to round", Toast.LENGTH_SHORT).show();
            }
        } finally {
            calculationLock.unlock();
        }
    }


    private void splitBill() {
        calculationLock.lock();
        try {
            String totalDueString = editTextNumberDecimalTotalDue.getText().toString().replace("$", "").replace("€", "").replace(",", "");
            String numberOfPeopleString = editTextNumberPeople.getText().toString().replace(",", "");


            if (!totalDueString.isEmpty() && !numberOfPeopleString.isEmpty()) {
                double totalDue = Double.parseDouble(totalDueString);
                int numberOfPeople = Integer.parseInt(numberOfPeopleString);


                if (numberOfPeople <= 0) {
                    Toast.makeText(this, "Number of people must be greater than zero", Toast.LENGTH_SHORT).show();
                    return;
                }


                double contributionPerPerson = totalDue / numberOfPeople;
                textViewPersonContribution.setText(String.format("%,.2f", contributionPerPerson));
            } else {
                Toast.makeText(this, "Please enter a valid number of people", Toast.LENGTH_SHORT).show();
            }
        } finally {
            calculationLock.unlock();
        }
    }


    private void adjustTipPercent(double increment) {
        calculationLock.lock();
        try {
            double currentTipPercent = Double.parseDouble(editTextNumberTipPercent.getText().toString());
            double newTipPercent = Math.max(0, Math.min(100, currentTipPercent + increment)); // Ensure tip percentage is between 0 and 100
            myTipPercent = newTipPercent;
            editTextNumberTipPercent.setText(String.format("%.1f", myTipPercent));
            calculateTip();
        } finally {
            calculationLock.unlock();
        }
    }
}
