package co.in.divi.kids.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import co.in.divi.kids.HomeActivity;
import co.in.divi.kids.R;

/**
 * Created by indraneel on 15-12-2014.
 */
public class HelpDialogFragment extends DialogFragment {
    private static final String TAG = HelpDialogFragment.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View helpDialogView = inflater.inflate(R.layout.dialog_help_def_launcher, null);
        helpDialogView.findViewById(R.id.gotit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                ((HomeActivity) getActivity()).startSession();
            }
        });
        builder.setView(helpDialogView);
        return builder.create();
    }
}
