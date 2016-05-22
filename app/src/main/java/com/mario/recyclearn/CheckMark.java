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
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by williamoliver on 5/21/16.
 */
public class CheckMark extends DialogFragment {

    private JSONObject results = null;
    private Button yesButton;
    private Button noButton;
    private TextView questionText;
    private final List<String> hints = new ArrayList<>();
    private boolean errored = false;
    public CheckMark() {
        setStyle(STYLE_NO_FRAME, 0);
    }

    public static CheckMark newInstance(JSONObject results) {

        CheckMark c = new CheckMark();
        c.results = results;
        JSONArray array = null;

        try {
            if (results.has("error")) {
                c.errored = results.getBoolean("error");
            }
            array = results.getJSONArray("hints");
            for (int i = 0; i < array.length(); i++) {
                c.hints.add(array.getString(i));
            }
        } catch (Exception e) {
            Log.e(MainActivity.TAG, e.getMessage());
            Log.e(MainActivity.TAG, e.getStackTrace().toString());
        }
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
        questionText = (TextView) v.findViewById(R.id.questionText);

        StringBuilder text = new StringBuilder("");
        for (String hint : hints) {
            text.append(hint + "\n");
        }

        questionText.setText(text.toString());

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

        if (errored) {
            noButton.setVisibility(View.GONE);
            yesButton.setText("OK");
        } else {
            noButton.setVisibility(View.VISIBLE);
            yesButton.setText("Yes");
        }

        return v;
    }
}
