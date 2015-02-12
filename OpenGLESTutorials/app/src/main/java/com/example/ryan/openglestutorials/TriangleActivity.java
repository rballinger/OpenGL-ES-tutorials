/**
 * Copyright (C) 2015 Ryan Ballinger.
 *
 * Application: OpenGL Tutorials
 * Author: Ryan Ballinger
 * Date: 1/2/2015
 * Modified: 2/12/2015
 */

package com.example.ryan.openglestutorials;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class TriangleActivity extends ActionBarActivity {

    private GLSurfaceView mGLSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // check if device supports OpenGL ES2
        final ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final ConfigurationInfo configInfo = am.getDeviceConfigurationInfo();
        // first 16 bits = major build number, last 16 bits = minor build number
        final boolean supportEs2 = configInfo.reqGlEsVersion >= 0x20000;
        if(supportEs2){
            // create renderer
            TriangleRenderer mainRend = new TriangleRenderer();
            // create surface view
            mGLSurfaceView = new GLSurfaceView(this);
            // set version
            mGLSurfaceView.setEGLContextClientVersion(2);
            // add renderer to surface view
            mGLSurfaceView.setRenderer(mainRend);
            // set surface view in content view
            setContentView(mGLSurfaceView);
        }else{
            Log.e("OpenGL ES2", "Your device doesn't support ES2. ("
                    + configInfo.reqGlEsVersion + ")");
            return;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume(){
        // activity must call GL surface view's onResume()
        super.onResume();
        mGLSurfaceView.onResume();
    }

    @Override
    protected void onPause(){
        // activity must call GL surface view's on Pause()
        super.onPause();
        mGLSurfaceView.onPause();
    }
}