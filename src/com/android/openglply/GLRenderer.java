package com.android.openglply;

import android.content.*;
import android.graphics.*;
import android.opengl.*;
import android.util.*;

import javax.microedition.khronos.egl.*;
import javax.microedition.khronos.opengles.*;

import android.opengl.Matrix;
import javax.microedition.khronos.egl.EGLConfig;

public class GLRenderer implements GLSurfaceView.Renderer {
	private float[] mProjectionMatrix = new float[16];

	private float[] mViewMatrix = new float[16];

	private float[] mvMatrix = new float[16];

	private Context context;

	private float[] mLightModelMatrix = new float[16];

	private float[] mLightPos = { 1f, 0f, -1f, 1f };

	private float[] mLightPosInModelSpace = new float[4];

	private float[] mLightPosInEyeSpace = new float[4];

	private int mProgram;

	private int startedCount = 0;

	private int completedCount = 0;

	private PLYModel cube;

	private LoadingAnimation anim;

	private boolean allLoaded = false;

	private float angle = 0;
	
	private float angley = 0;
	
	private float scale = 0.4f;
	
	public void setContext(Context context) {
		this.context = context;
	}
	
	public Context getContext() {
		return this.context;
	}
	
	public float getAngle()
	{
		return this.angle;
	}
	
	public void setAngle(float v)
	{
		this.angle = v;
	}
	
	public float getAngley()
	{
		return this.angley;
	}
	
	public void setAngley(float v)
	{
		this.angley = v;
	}
	
	public float getScale()
	{
		return this.scale;
	}
	
	public void setScale(float v)
	{
		if(v < 5 && v > 0.1)
			this.scale = v;
	}
	
	@Override
	public void onSurfaceCreated(GL10 unused, EGLConfig config) {
		GLES20.glClearColor(0.3f, 0.3f, 0.3f, 1f);
		anim = new LoadingAnimation();
		cube = new PLYModel(context, "tree_leaves_test.ply", mLoadStatusListener);

	}

	@Override
	public void onSurfaceChanged(GL10 unused, int width, int height) {
		float ratio = (float) width / height;
		GLES20.glViewport(0, 0, width, height);
		float zoom = 2.0f;
		Matrix.frustumM(mProjectionMatrix, 0, -ratio / zoom, ratio / zoom, -1
				/ zoom, 1 / zoom, 1, 100);
		// set STLModel projectionMatrix
		anim.setProjection(mProjectionMatrix);
		cube.setProjectionMatrix(mProjectionMatrix);
		cube.setLightStrength(4);

	}

	@Override
	public void onDrawFrame(GL10 unused) {
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

		if (allLoaded) {
			Matrix.setLookAtM(mViewMatrix, 0, 0.0f, 0.0f, -1.0f, 0f, 0f, 1f,
					0f, 1f, 0f);

			Matrix.setIdentityM(mLightModelMatrix, 0);
			Matrix.multiplyMV(mLightPosInModelSpace, 0, mLightModelMatrix, 0,
					mLightPos, 0);
			Matrix.multiplyMV(mLightPosInEyeSpace, 0, mViewMatrix, 0,
					mLightPosInModelSpace, 0);


			cube.setIdentity();
			Vector3 cv = cube.getCenterVec();
			cube.translate(cv.x,cv.y,cv.z);
			Vector3 cv2 = cube.getCenterVertical(); 

			cube.rotateAxis(angle*0.2f, cv.x,cv.y,cv.z);
			cube.rotateAxis(angley*0.2f, -cv.z,cv.y,cv.x);
			cube.scale(scale);
			cube.draw(mViewMatrix, mLightPosInEyeSpace);

		} else {
			anim.draw();
		}
	}

	public static int loadGLShader(int type, String code) {
		int shader = GLES20.glCreateShader(type);
		GLES20.glShaderSource(shader, code);
		GLES20.glCompileShader(shader);

		// Get the compilation status.
		final int[] compileStatus = new int[1];
		GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

		// If the compilation failed, delete the shader.
		if (compileStatus[0] == 0) {
			Log.e("GLRenderer",
					"Error compiling shader: "
							+ GLES20.glGetShaderInfoLog(shader));
			GLES20.glDeleteShader(shader);
			shader = 0;
		}

		if (shader == 0) {
			throw new RuntimeException("Error creating shader.");
		}

		return shader;
	}

	public static void checkGLError(String func) {
		int error;
		while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
			Log.e("GLRenderer", func + ": glError " + error);
			throw new RuntimeException(func + ": glError " + error);
		}
	}

	public static int loadTexture(final Context context, final int resourceId) {
		final int[] textureHandle = new int[1];

		GLES20.glGenTextures(1, textureHandle, 0);

		if (textureHandle[0] != 0) {
			final BitmapFactory.Options options = new BitmapFactory.Options();
			options.inScaled = false; // No pre-scaling

			// Read in the resource
			final Bitmap bitmap = BitmapFactory.decodeResource(
					context.getResources(), resourceId, options);

			// Bind to the texture in OpenGL
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

			// Set filtering
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
					GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
					GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

			// Load the bitmap into the bound texture.
			GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

			// Recycle the bitmap, since its data has been loaded into OpenGL.
			bitmap.recycle();
		}

		if (textureHandle[0] == 0) {
			throw new RuntimeException("Error loading texture.");
		}

		return textureHandle[0];
	}

	private PLYModel.LoadStatusListener mLoadStatusListener = new PLYModel.LoadStatusListener() {
		@Override
		public void started() {
			if (allLoaded)
				allLoaded = false;
			startedCount++;
			Log.d("started ", startedCount + " started");
		}

		@Override
		public void completed() {
			completedCount++;
			if (completedCount == startedCount) {
				allLoaded = true;
			}
			Log.d("completed ", completedCount + " completed");
		}
	};
}
