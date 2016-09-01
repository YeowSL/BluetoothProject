package com.example.prn763.bluetoothproject;

import android.content.Context;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by PRN763 on 1/17/2016.
 */
public class UIManager implements MessagesCode{
    private Context mContext;

    public UIManager(Context context){
        mContext = context;
    }

    //how to change the Textview into generic type
    public synchronized <T> void dialogUpdate(int dialogType, String dialog, T viewId){

            switch (dialogType) {
                case TOAST_DIALOG_DISPLAY:
                    Toast.makeText(mContext, dialog, Toast.LENGTH_SHORT).show();
                    break;
                case TEXT_DIALOG_DISPLAY:
                    ((TextView)viewId).setText(dialog);
                    break;
                case BTN_DIALOG_DISPLAY:
                    ((Button)viewId).setText(dialog);
                    break;

            }

    }


}
