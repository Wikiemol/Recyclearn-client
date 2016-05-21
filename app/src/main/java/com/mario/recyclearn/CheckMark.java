package com.mario.recyclearn;

import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import org.json.JSONObject;

/**
 * Created by williamoliver on 5/21/16.
 */
public class CheckMark extends DialogFragment {

    private JSONObject results = null;
    private Button yesButton;
    private Button noButton;

    public CheckMark() {
        setStyle(STYLE_NO_FRAME, 0);
    }

    public static CheckMark newInstance(JSONObject results) {

        CheckMark c = new CheckMark();
        c.results = results;
        return c;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState) {

        getDialog().getWindow().setGravity(Gravity.BOTTOM| Gravity.CENTER_HORIZONTAL);

        View v = inflater.inflate(R.layout.check_mark, container, false);
        yesButton = (Button) v.findViewById(R.id.yesButton);
        noButton = (Button) v.findViewById(R.id.cancel);

        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ((MainActivity)getActivity()).resetAlpha();
                Log.d("CheckMark", "yes");
                dismiss();

            }
        });

        noButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d("CheckMark", "no");
                ((MainActivity)getActivity()).resetAlpha();
                dismiss();
            }
        });

        return v;
    }
}
