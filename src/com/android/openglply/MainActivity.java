package com.android.openglply;

import android.app.*;
import android.graphics.PointF;
import android.opengl.Matrix;
import android.os.*;
import android.util.FloatMath;
import android.view.*;
import android.view.View.OnTouchListener;
import android.widget.*;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

public class MainActivity extends Activity 
{

	private GLSurface surface;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		
		surface = (GLSurface) findViewById(R.id.surface);
    }

	@Override
	protected void onPause()
	{
		surface.onPause();
		super.onPause();
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		surface.onResume();
	}
	
}
