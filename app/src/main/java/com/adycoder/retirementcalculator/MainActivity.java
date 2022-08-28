package com.adycoder.retirementcalculator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.adycoder.retirementcalculator.databinding.ActivityMainBinding;
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;
import com.microsoft.appcenter.utils.async.AppCenterConsumer;
import com.microsoft.appcenter.utils.async.AppCenterFuture;

import java.util.HashMap;


public class MainActivity extends AppCompatActivity {

    public static final String TAG = "RCTag";
    private ActivityMainBinding mAMBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAMBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        AppCenter.start(getApplication(), getString(R.string.app_secret), Analytics.class, Crashes.class);

        AppCenterFuture<Boolean> future = Crashes.hasCrashedInLastSession();

        future.thenAccept(new AppCenterConsumer<Boolean>() {
            @Override
            public void accept(Boolean aBoolean) {
                if (aBoolean) {
                    Toast.makeText(MainActivity.this,
                            "Oops! Sorry about that!",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        mAMBinding.calculateButton.setOnClickListener(view -> {
            Crashes.generateTestCrash();
            analyticsTrackEvent();
        });


    }

    @SuppressLint("SetTextI18n")
    private void analyticsTrackEvent() {
        try {

            float interestRate = Float.parseFloat(mAMBinding.interestEditText.getText().toString());
            int currentAge = Integer.parseInt(mAMBinding.ageEditText.getText().toString());
            int retirementAge = Integer.parseInt(mAMBinding.retirementEditText.getText().toString());

            float monthlySavings = Float.parseFloat(mAMBinding.monthlySavingsEditText.getText().toString());
            float currentSavings = Float.parseFloat(mAMBinding.currentEditText.getText().toString());

            HashMap<String, String> properties = new HashMap<>();
            properties.put("interest_rate", String.valueOf(interestRate));
            properties.put("current_Age", String.valueOf(currentAge));
            properties.put("retirement_Age", String.valueOf(retirementAge));
            properties.put("monthly_Savings", String.valueOf(monthlySavings));
            properties.put("current_Savings", String.valueOf(currentSavings));

            if (interestRate <= 0) {
                Analytics.trackEvent("wrong_interest_rate", properties);
            }
            if (retirementAge <= currentAge) {
                Analytics.trackEvent("wrong_age", properties);
            }

            float futureSavings = calculateRetirement(interestRate, currentSavings, monthlySavings,
                    (retirementAge - currentAge) * 12);

            mAMBinding.resultTextView.setText("At the current rate of " + interestRate +
                    "%, saving $" + monthlySavings + " a month you will have $" + futureSavings +
                    " by " + retirementAge + ".");

        } catch (Exception e) {
            Analytics.trackEvent(e.toString());
        }
    }

    private float calculateRetirement(float interestRate, float currentSavings,
                                      float monthlySavings, int numMonths) {

        float calcFloatVal = (float) 1 + interestRate / (float) 100 / (float) 12;
        float futureSavings = currentSavings * (float) Math.pow(calcFloatVal, numMonths);

        int i = 1;
        if (i <= numMonths) {
            while (true) {
                futureSavings += monthlySavings * (float) Math.pow(calcFloatVal, i);
                if (i == numMonths) {
                    break;
                }
                ++i;
            }
        }

        return futureSavings;
    }

}








































