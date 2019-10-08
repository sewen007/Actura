package com.zkc.commandmcu.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.zkc.commandmcu.R;


/**
 * Created by Sir Sheyi on 16/09/2014.
 *
 */
public class ToastDialog extends Dialog {
    private String mMessage;
    private Context mContext;
    private int mNumber = 0;

    public ToastDialog(Context context, String message) {
        super(context);
        mContext = context;
        mMessage = message;
    }

    public ToastDialog(Context context, String message, int number) {
        super(context);
        mContext = context;
        mMessage = message;
        mNumber = number;
    }

    public void onCreate(Bundle bundle){
        super.onCreate(bundle);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.toast_dialog);

        TextView messageTextView = (TextView)findViewById(R.id.toast_message);
        messageTextView.setText(mMessage);

        Button cancelCall = (Button)findViewById(R.id.remove_dialog);
        cancelCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ToastDialog.this.hide();
            }
        });
    }
}
