package com.szagurskii.githubsearch.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;

import com.szagurskii.githubsearch.R;

/**
 * @author Savelii Zagurskii
 */
public class ConfirmAlertDialog extends AlertDialog.Builder {

    private String mLink;

    public ConfirmAlertDialog(Context context, String link) {
        super(context);
        mLink = link;

        setCancelable(true);
        setMessage(String.format(getContext().getString(R.string.dialog_confirm_description), mLink));
        setTitle(R.string.dialog_confirm_title);
        setPositiveButton(R.string.dialog_confirm_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mLink));
                getContext().startActivity(browserIntent);
            }
        });
        setNegativeButton(R.string.dialog_confirm_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
    }
}