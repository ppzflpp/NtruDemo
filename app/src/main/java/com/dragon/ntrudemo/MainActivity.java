package com.dragon.ntrudemo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dragon.ntrudemo.wordCheck.RubbishManager;

public class MainActivity extends AppCompatActivity {


    private static final String MSG = "I am a programmer!";

    private LinearLayout mContainer;
    private EditText mInputText;
    private Button mSubmitButton;
    private Button mCheckButton;

    private final static int MSG_NTRU_ENCRYPT = 0;
    private final static int MSG_NTRU_DECRYPT = 1;
    private final static int MSG_NTRU_INIT = 2;

    private final static int MSG_ENCRYPT = 10;
    private final static int MSG_DECRYPT = 11;

    private HandlerThread mHandlerThread;
    private Handler mNtruHandler;

    private AlertDialog mAlertDialog;

    private RubbishManager mRubbishManager;

    private Handler mMainHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            TextView textView = new TextView(MainActivity.this);
            mContainer.addView(textView);
            switch (msg.what) {
                case MSG_ENCRYPT:
                    textView.setTextColor(Color.RED);
                    textView.setText("encrypted:\n " + msg.obj);
                    break;
                case MSG_DECRYPT:
                    textView.setTextColor(Color.GREEN);
                    textView.setText("dencrypt:\n " + msg.obj);
                    break;
            }
        }
    };


    private NtruManager mNtruManager;

    private String mContent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRubbishManager = new RubbishManager(getApplicationContext());

        mInputText = (EditText) findViewById(R.id.input_text);

        mSubmitButton = (Button) findViewById(R.id.submit);
        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(mInputText.getText())) {
                    return;
                }
                /*
                Message msg = Message.obtain();
                msg.what = MSG_NTRU_ENCRYPT;
                msg.obj = mInputText.getText().toString();
                mNtruHandler.sendMessage(msg);

                mInputText.setText("");
                */
            }
        });

        mCheckButton = (Button) findViewById(R.id.check);
        mCheckButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(mInputText.getText())) {
                    return;
                }
                checkContent(mInputText.getText().toString());
            }
        });

        mContainer = (LinearLayout) findViewById(R.id.parent);
        TextView textView = new TextView(MainActivity.this);
        textView.setTextColor(Color.GRAY);
        //textView.setText("before encrypt: " + MSG);
        mContainer.addView(textView);

        mHandlerThread = new HandlerThread("ntru");
        mHandlerThread.start();
        mNtruHandler = new Handler(mHandlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case MSG_NTRU_ENCRYPT:
                        //update ui
                        Log.d("TAG", "msg is " + msg.obj);
                        String result = mNtruManager.encrypt((String) msg.obj);
                        Message message = Message.obtain();
                        message.what = MSG_ENCRYPT;
                        message.obj = result;
                        mMainHandler.sendMessage(message);

                        //decrypt
                        mNtruHandler.sendEmptyMessage(MSG_NTRU_DECRYPT);
                        break;
                    case MSG_NTRU_DECRYPT:
                        //update ui
                        String result1 = mNtruManager.decrypt();
                        Message message1 = Message.obtain();
                        message1.what = MSG_DECRYPT;
                        message1.obj = result1;
                        mMainHandler.sendMessage(message1);

                        break;
                    case MSG_NTRU_INIT:
                        mNtruManager = new NtruManager();
                        break;
                }
            }
        };
        mNtruHandler.sendEmptyMessage(MSG_NTRU_INIT);


    }

    private void checkContent(String content) {
        boolean result = mRubbishManager.justifyRubbish(mRubbishManager.cutWord(content));
        if(!result){
            if(mAlertDialog != null && mAlertDialog.isShowing()){
                mAlertDialog.dismiss();;
                mAlertDialog = null;
            }

            mAlertDialog = new AlertDialog.Builder(this).setTitle(R.string.warning)
                    .setMessage(R.string.contain_error_word)
                    .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mAlertDialog.dismiss();
                        }
                    })
                    .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Message msg = Message.obtain();
                            msg.what = MSG_NTRU_ENCRYPT;
                            msg.obj = mInputText.getText().toString();
                            mNtruHandler.sendMessage(msg);
                        }
                    })
                    .show();
        }else{
            Message msg = Message.obtain();
            msg.what = MSG_NTRU_ENCRYPT;
            msg.obj = mInputText.getText().toString();
            mNtruHandler.sendMessage(msg);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mHandlerThread != null) {
            mHandlerThread.quitSafely();
        }
    }
}
