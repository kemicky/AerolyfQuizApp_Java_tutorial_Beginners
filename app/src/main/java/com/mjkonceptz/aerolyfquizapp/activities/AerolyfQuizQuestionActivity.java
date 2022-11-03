package com.mjkonceptz.aerolyfquizapp.activities;import android.annotation.SuppressLint;import android.content.DialogInterface;import android.content.Intent;import android.content.res.ColorStateList;import android.graphics.Color;import android.os.Bundle;import android.os.CountDownTimer;import android.view.View;import android.widget.Button;import android.widget.RadioButton;import android.widget.RadioGroup;import android.widget.TextView;import android.widget.Toast;import androidx.annotation.NonNull;import androidx.appcompat.app.AlertDialog;import androidx.appcompat.app.AppCompatActivity;import androidx.core.content.ContextCompat;import androidx.core.view.ViewCompat;import androidx.core.view.WindowCompat;import androidx.core.view.WindowInsetsCompat;import androidx.core.view.WindowInsetsControllerCompat;import com.mjkonceptz.aerolyfquizapp.R;import com.mjkonceptz.aerolyfquizapp.model.Question;import com.mjkonceptz.aerolyfquizapp.sqlitedatabase.QuizDbHelper;import com.mjkonceptz.aerolyfquizapp.utils.Constants;import java.util.ArrayList;import java.util.Collections;import java.util.Locale;/** * Author: Kemmy MO Jones * Project: AerolyfQuizApp * Date: 2022/07/21 * Email: mjkonceptz@gmail.com * UI/UX Design: Kemmy MO Jones ~ (Mjkonceptz) * Copyright (c) 2022 MJKonceptz. All rights reserved. */public class AerolyfQuizQuestionActivity extends AppCompatActivity {    /* CONSTANTS: Quiz Result Data. */    public static final String EXTRA_PLAYERS_NAME = "extraNamePlayer";    public static final String EXTRA_TOTAL_QUESTION = "extraTotalQuestion";    public static final String EXTRA_TOTAL_SCORE = "extraScores";    /* VIEW: Timer CountDown */    private static final long COUNTDOWN_IN_MILLISECS = 31000;    /* VIEWS: UI Global Definition */    private TextView   txtQuestion;    private TextView   txtScore;    private TextView   txtQuestionNum;    private TextView    mtTxtTimer;    private RadioGroup  radGroup;    private RadioButton radBtn1;    private RadioButton radBtn2;    private RadioButton radBtn3;    private RadioButton radBtn4;    private Button btnSubmit;    /*VARIABLES: Global Variables */    private ColorStateList textColorDefaultTxtTv;    private ColorStateList textColorDefaultRadBtn;    private ColorStateList textColorDefaultCd;    private CountDownTimer countDownTimer;    private long timeLeftInMilliSecs;    private ArrayList<Question> questionList;    private int questionCounter;    private int questionCountTotal;    private Question currentQuestion;    private String playerName;    private int score;    private boolean answered;    /* VIEW LIFECYCLE ~ onCreate */    @Override    protected void onCreate(Bundle savedInstanceState) {        super.onCreate(savedInstanceState);        /* CODE DESCRIPTION: UI Design: Set The Window To Extend Behind The Status Bar. */        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);        hideSystemBars();        setContentView(R.layout.activity_aerolyf_quiz_question);        /*VIEWS: Initializing The Layout Views */        txtQuestion = findViewById(R.id.txtQuestion);        txtScore = findViewById(R.id.txtScore);        mtTxtTimer = findViewById(R.id.mtTxtTimer);        TextView txtQuizCategory = findViewById(R.id.txtQuizCategory);        TextView txtDifficultyLevel = findViewById(R.id.txtDfficultyLevel);        txtQuestionNum = findViewById(R.id.txtQuestionNum);        radGroup = findViewById(R.id.radGroup);        radBtn1 = findViewById(R.id.radBtn1);        radBtn2 = findViewById(R.id.radBtn2);        radBtn3 = findViewById(R.id.radBtn3);        radBtn4 = findViewById(R.id.radBtn4);        btnSubmit = findViewById(R.id.btnSubmit);        /*SETTINGS: Radio Button & Timer UI Color Rendering. */        textColorDefaultTxtTv = txtQuestion.getTextColors();        textColorDefaultRadBtn = radBtn1.getTextColors();        textColorDefaultCd = mtTxtTimer.getTextColors();        /* CODE DESCRIPTION: Retrieving Data From The Quiz Category Dashboard Using Intent Extra */        Intent intent = getIntent();        playerName = intent.getStringExtra(AerolyfQuestionTopicCategoryActivity.EXTRA_PLAYER);        int categoryID = intent.getIntExtra(AerolyfQuestionTopicCategoryActivity.EXTRA_CATEGORY_ID, 0);        String categoryName = intent.getStringExtra(AerolyfQuestionTopicCategoryActivity.EXTRA_CATEGORY_NAME);        String difficulty = intent.getStringExtra(AerolyfQuestionTopicCategoryActivity.EXTRA_DIFFICULTY);        /* COMMENT: Displaying TextView Labels */        txtQuizCategory.setText(String.format("Quiz Category: %s", categoryName));        txtDifficultyLevel.setText(String.format("Difficulty: %s", difficulty));        /*COMMENT: Set Content to be rendered again after it is destroyed on exit of the app.*/        if (savedInstanceState == null){            /*SETTINGS: Retrieving Quiz Questions From The Database Using The Database QuizDBHelper */            QuizDbHelper dbHelper = QuizDbHelper.getInstance(this);            questionList = dbHelper.getQuestions(categoryID, difficulty);            questionCountTotal = questionList.size();            Collections.shuffle(questionList);            showNextQuestion();        } else{            /*CODE DESCRIPTION: Device Layout Orientation Change Code.*/            questionList = savedInstanceState.getParcelableArrayList(Constants.KEY_QUESTION_LIST);            questionCountTotal = questionList.size();            questionCounter = savedInstanceState.getInt(Constants.KEY_QUESTION_NUM);            currentQuestion = questionList.get(questionCounter - 1);            score = savedInstanceState.getInt(Constants.KEY_SCORE);            timeLeftInMilliSecs = savedInstanceState.getLong(Constants.KEY_MILLISECS_LEFT);            answered = savedInstanceState.getBoolean(Constants.KEY_ANSWERED);            if(!answered){                startCountDown();            }            else{                updateCountDownText();                showSolution();            } /*end: if_else */        } /*end: if_else */        /* CODE DESCRIPTION: Firing up the submit button to move to the next question */        // noinspection Convert2Lambda        btnSubmit.setOnClickListener(new View.OnClickListener() {            @Override            public void onClick(View view) {                if (!answered) {                    if (radBtn1.isChecked() || radBtn2.isChecked()                            || radBtn3.isChecked() || radBtn4.isChecked()) {                        checkAnswer();                    } else {                        Toast.makeText(AerolyfQuizQuestionActivity.this, "Select An Answer To Continue Quiz", Toast.LENGTH_SHORT).show();                    }//if-else_isChecked()                } else {                    showNextQuestion();                } /*end: if-else_answered*/            }        }); /*end: btnSubmit*/    } /*end: onCreate*/    /* SETTINGS: Configure The Device Back Button Behaviour.*/    @SuppressWarnings("deprecation")    private void hideSystemBars() {        /*Comment: Calling The Device Systems Bars Configuration From The WindowInsetController Library */        WindowInsetsControllerCompat windowInsetsController =                ViewCompat.getWindowInsetsController(getWindow().getDecorView());        if (windowInsetsController == null) {            return;        }        /*Comment: Configure the behavior of the hidden system bars */        windowInsetsController.setSystemBarsBehavior(                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE        );        /*Comment: Hide the navigation bar */        windowInsetsController.hide(WindowInsetsCompat.Type.navigationBars());    } /*end: hideSystemBars() */    /* CODE DESCRIPTION: App Shows The Next Question.*/    @SuppressLint({"DefaultLocale", "SetTextI18n"})    private void showNextQuestion(){        txtQuestion.setTextColor(textColorDefaultTxtTv);        radBtn1.setTextColor(textColorDefaultRadBtn);        radBtn2.setTextColor(textColorDefaultRadBtn);        radBtn3.setTextColor(textColorDefaultRadBtn);        radBtn4.setTextColor(textColorDefaultRadBtn);        radGroup.clearCheck();        //if-statement        if(questionCounter < questionCountTotal){            currentQuestion = questionList.get(questionCounter);            txtQuestion.setText(currentQuestion.getQuestion());            radBtn1.setText(currentQuestion.getOption1());            radBtn2.setText(currentQuestion.getOption2());            radBtn3.setText(currentQuestion.getOption3());            radBtn4.setText(currentQuestion.getOption4());            questionCounter++;            txtQuestionNum.setText(String.format("Question: %d/%d ",                    questionCounter, questionCountTotal));            answered = false;            btnSubmit.setText("Submit Answer");            timeLeftInMilliSecs = COUNTDOWN_IN_MILLISECS;            startCountDown();        } else{            finishQuiz();        } //if_else    } /*end: showNextQuestion() */    /* CODE DESCRIPTION: Timer To Start The Questions Count Down Timer.*/    private void startCountDown(){        countDownTimer = new CountDownTimer(timeLeftInMilliSecs, 1000) {            @Override            public void onTick(long millisUntilFinished) {                timeLeftInMilliSecs = millisUntilFinished;                updateCountDownText();            }            @Override            public void onFinish() {                timeLeftInMilliSecs = 0;                updateCountDownText();                checkAnswer();            }        }.start();    } /*end: startCountDown() */    /* CODE DESCRIPTION: Timer To Update The Questions Count Down Timer.*/    private void updateCountDownText(){        int minutes = (int) (timeLeftInMilliSecs / 1000) / 60;        int seconds = (int) (timeLeftInMilliSecs / 1000) % 60;        String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);        mtTxtTimer.setText(timeFormatted);        if(timeLeftInMilliSecs < 10000){            mtTxtTimer.setTextColor(Color.RED);        }else{            mtTxtTimer.setTextColor(textColorDefaultCd);        }//else_if    } /*end: updateCountDownText() */    /* CODE DESCRIPTION: Validating The Quiz Answers & Updating the Answer.*/    @SuppressLint("DefaultLocale")    private void checkAnswer(){        answered = true;        countDownTimer.cancel();        RadioButton radBtnSelected = findViewById(radGroup.getCheckedRadioButtonId());        int answerNum = radGroup.indexOfChild(radBtnSelected) + 1;        if(answerNum == currentQuestion.getAnswerNum()){            score++;            txtScore.setText(String.format("Score: %d ", score));        } //if        showSolution();    } /*end: checkAnswer() */    /* CODE DESCRIPTION: The Quiz Interface Displays The Answer To The Question.*/    @SuppressLint("SetTextI18n")    private void showSolution(){        radBtn1.setTextColor(Color.RED);        radBtn2.setTextColor(Color.RED);        radBtn3.setTextColor(Color.RED);        radBtn4.setTextColor(Color.RED);        switch(currentQuestion.getAnswerNum()){            case 1:                radBtn1.setTextColor(Color.rgb(0, 121, 107));                txtQuestion.setTextColor(Color.CYAN);                txtQuestion.setText(String.format("The correct answer is: %s", currentQuestion.getOption1()));                break;            case 2:                radBtn2.setTextColor(Color.rgb(0, 121, 107));                txtQuestion.setTextColor(Color.CYAN);                txtQuestion.setText(String.format("The correct answer is: %s", currentQuestion.getOption2()));                break;            case 3:                radBtn3.setTextColor(Color.rgb(0, 121, 107));                txtQuestion.setTextColor(Color.CYAN);                txtQuestion.setText(String.format("The correct answer is: %s", currentQuestion.getOption3()));                break;            case 4:                radBtn4.setTextColor(Color.rgb(0, 121, 107));                txtQuestion.setTextColor(Color.CYAN);                txtQuestion.setText(String.format("The correct answer is: %s", currentQuestion.getOption4()));                break;        }//switch_case        if(questionCounter < questionCountTotal){            btnSubmit.setText("Next Question");        } else {            btnSubmit.setText("End of Quiz");        } /* if-else */    } /*end: showSolution() */    /* CODE DESCRIPTION: Sending Requested Quiz Results To ScoreActivity And Close The Question Activity.*/    private void finishQuiz(){        Intent intentScore = new Intent(AerolyfQuizQuestionActivity.this, ScoreActivity.class);        intentScore.putExtra(EXTRA_PLAYERS_NAME, playerName);        intentScore.putExtra(EXTRA_TOTAL_QUESTION, questionCountTotal);        intentScore.putExtra(EXTRA_TOTAL_SCORE, score);        intentScore.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);        startActivity(intentScore);        finish();    } /*end: finishQuiz() */    /* SETTINGS: Configure The Device Back Button Behaviour.*/    @Override    public void onBackPressed() {        /* VIEW : Creating An Alert DialogBox To Display An Error Message */        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(AerolyfQuizQuestionActivity.this, R.style.customQuizAlertDialog);        /* CODE DESCRIPTION: Set Alert Dialog Title */        alertDialogBuilder.setTitle(R.string.submit_dialog_title);        /* CODE DESCRIPTION: Set  Alert Dialog Icon */        alertDialogBuilder.setIcon(R.drawable.ic_info_warning);        /* CODE DESCRIPTION: Set Alert Dialog Message */        alertDialogBuilder.setMessage(R.string.submit_dialog_message);        /* CODE DESCRIPTION: Set Alert Cancel Dialog Button */        alertDialogBuilder.setCancelable(false);        /* COMMENT: Positive Alert Button  */        alertDialogBuilder.setPositiveButton(R.string.dialog_ok_button_text, null);        /* CODE DESCRIPTION:  Create The Alert Dialog Box */        AlertDialog alertDialog = alertDialogBuilder.create();        alertDialog.show();        /* CODE DESCRIPTION - UI: Set Alert Dialog Positive Button */        Button buttonPositive = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);        buttonPositive.setTextColor(ContextCompat.getColor(AerolyfQuizQuestionActivity.this, R.color.dialogNegativeButtonColor1));        buttonPositive.setTextSize(16F);    } /*end: onBackPressed() */    /* SETTINGS: Configure The Timer Task To Stop After Closing The Activity.*/    @Override    protected void onDestroy() {        super.onDestroy();        if(countDownTimer != null){            countDownTimer.cancel();        }//if    } /*end: onDestroy() */    /* SETTINGS: Configure The Device Layout Orientation.*/    @Override    protected void onSaveInstanceState(@NonNull Bundle outState) {        super.onSaveInstanceState(outState);        outState.putInt(Constants.KEY_SCORE, score);        outState.putInt(Constants.KEY_QUESTION_NUM, questionCounter);        outState.putLong(Constants.KEY_MILLISECS_LEFT, timeLeftInMilliSecs);        outState.putBoolean(Constants.KEY_ANSWERED, answered);        outState.putParcelableArrayList(Constants.KEY_QUESTION_LIST, questionList);    } /*end: onSaveInstanceState */} /*end: CLASS ~ AerolyfQuizQuestionActivity */