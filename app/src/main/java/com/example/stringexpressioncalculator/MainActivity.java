package com.example.stringexpressioncalculator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.TypedValue;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import java.util.ArrayList;

/**
 * The type Main activity.
 */
public class MainActivity extends AppCompatActivity {

    private InterstitialAd mInterstitialAd;

    private AdRequest adRequest;

    /**
     * The Input items.
     */
    ArrayList<String> inputItems = new ArrayList<>();

    /**
     * The constant SPACE.
     */
    public static final int SPACE = 15;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        onWindowFocusChanged(true);

        if (Build.VERSION.SDK_INT >= 28) {
            getWindow().getAttributes().layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }

        MobileAds.initialize(this, initializationStatus -> {
        });

        adRequest = new AdRequest.Builder().build();

        loadAd();


        LinearLayout first = findViewById(R.id.first);
        LinearLayout second = findViewById(R.id.second);
        LinearLayout third = findViewById(R.id.third);
        LinearLayout fourth = findViewById(R.id.fourth);
        LinearLayout fifth = findViewById(R.id.fifth);
        LinearLayout add = findViewById(R.id.AddFun2);

        Animation animation = AnimationUtils.loadAnimation(this, R.anim.alpha);
        Animation animation1 = AnimationUtils.loadAnimation(this, R.anim.trans5);
        Animation animation2 = AnimationUtils.loadAnimation(this, R.anim.trans4);
        Animation animation3 = AnimationUtils.loadAnimation(this, R.anim.trans3);
        Animation animation4 = AnimationUtils.loadAnimation(this, R.anim.trans2);
        Animation animation5 = AnimationUtils.loadAnimation(this, R.anim.trans1);

        final Boolean[] IsClick = {false};

        Button button = findViewById(R.id.button);
        button.setOnClickListener(v -> {
            if (!IsClick[0]) {
                add.startAnimation(animation);
                first.startAnimation(animation5);
                second.startAnimation(animation4);
                third.startAnimation(animation3);
                fourth.startAnimation(animation2);
                fifth.startAnimation(animation1);
                IsClick[0] = true;
            } else {

                IsClick[0] = false;
            }
        });
    }

    private void loadAd() {
        InterstitialAd.load(this,"\n" +
                        "ca-app-pub-6240815819353431/1683112214", adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        mInterstitialAd = interstitialAd;

                        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdShowedFullScreenContent() {
                                mInterstitialAd = null;

                                loadAd();
                            }
                        });
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        mInterstitialAd = null;
                    }
                });
    }

    /**
     * On click start.
     *
     * @param view the view
     */
    @SuppressLint({"NonConstantResourceId", "SetTextI18n"})
    public void onClickStart(View view) {
        int id = view.getId();

        final Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        final VibrationEffect vibrationEffect;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrationEffect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK);
            vibrator.cancel();
            vibrator.vibrate(vibrationEffect);
        }

        IExpressionChecker expressionChecker = new ExpressionChecker();

        TextView input = findViewById(R.id.input);
        TextView output = findViewById(R.id.output);
        HorizontalScrollView scroll = findViewById(R.id.scroll_inp);

        output.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.baseText));

        switch (id) {
            case R.id.deleteAll: {
                inputItems.clear();
                output.setText("");
                break;
            }
            case R.id.delete: {
                if (!inputItems.isEmpty()) {
                    inputItems.remove(inputItems.size() - 1);
                }
                break;
            }
            case R.id.solve: {
                output.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.largeText));
                break;
            }
            default: {
                String currentItem = ((TextView) findViewById(id)).getText().toString();

                if (expressionChecker.check(inputItems, currentItem)) {
                    inputItems.add(currentItem);
                }
            }
        }

        StringBuilder items = new StringBuilder();

        for (String inputItem : inputItems) {
            items.append(inputItem);
        }

        input.setText(items.toString());

        scroll.post(() -> scroll.fullScroll(View.FOCUS_RIGHT));

        ICalculator calculator = new Calculator();

        AnswerFormat answerFormat = new AnswerFormat();

        String outputResult;

        if (items.length() > 0) {
            try {
                double result = calculator.calculate(items.toString()
                        .replace("e", "" + Math.E)
                        .replace("π", "" + Math.PI)
                        .replace(",", "."));

                if (Double.isInfinite(result)) {
                    outputResult = "Infinity";
                } else if (Double.isNaN(result)) {
                    outputResult = "NaN";
                } else {
                    outputResult = answerFormat.getFormattedAnswer(result, SPACE);
                }
            } catch (CalculatorException e) {
                outputResult = e.getMessage();
            }
        } else {
            outputResult = "";
        }

        output.setText(outputResult);

        if (id == R.id.solve && mInterstitialAd != null) {
            mInterstitialAd.show(MainActivity.this);
        }
    }
}
