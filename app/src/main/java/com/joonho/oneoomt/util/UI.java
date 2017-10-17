package com.joonho.oneoomt.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 * Created by joonhopark on 2017. 9. 12..
 */

public class UI {
    public void errDlg (Context ctx, String hd, String msg) {

        final AlertDialog errDlg = new AlertDialog.Builder(ctx).create();

        errDlg.setTitle(hd);
        errDlg.setMessage(msg);
        errDlg.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        errDlg.dismiss();
                    }
                });
        errDlg.show();
    }
}
