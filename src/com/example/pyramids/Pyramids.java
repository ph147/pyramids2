package com.example.pyramids;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.content.res.Configuration;
import android.content.SharedPreferences;

public class Pyramids extends Activity
{
    Deck deck;

    /*
    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        // ignore
        super.onConfigurationChanged(newConfig);
    }
    */

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int height = displaymetrics.heightPixels;
        int width = displaymetrics.widthPixels;

        SharedPreferences saveState = getPreferences(0);

        deck = new Deck(this, width, height, saveState);
        deck.setBackgroundColor(0xff999966);

        setContentView(deck);
        //setContentView(R.layout.main);
    }
}
