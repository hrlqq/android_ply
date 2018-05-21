package com.android.openglply;

import android.opengl.*;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.*;
import android.graphics.PointF;
import android.util.*;
import android.view.*;
import android.widget.ImageView;
import android.view.View.OnTouchListener;

public class GLSurface extends GLSurfaceView implements OnTouchListener
{

	private GLRenderer renderer;
	
	public GLSurface(Context context, AttributeSet attrs){
		super(context, attrs);
		setEGLContextClientVersion(2);
		renderer = new GLRenderer();
		renderer.setContext(context);
		setRenderer(renderer);
		setRenderMode(RENDERMODE_CONTINUOUSLY);
		setOnTouchListener(this);
	}

	public GLRenderer getRenderer() {
		return renderer;
	}
	
	// 縮放控制
    @SuppressWarnings("deprecation")
	private Matrix matrix = new Matrix();
    private Matrix savedMatrix = new Matrix();
    
    // 不同状态的表示：
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;

    // 定义第一个按下的点，两只接触点的重点，以及出事的两指按下的距离：
    private PointF startPoint = new PointF();
    private PointF midPoint = new PointF();
    private float oriDis = 1f;
    private float twodx = 0f;
    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        //ImageView view = (ImageView) v;
        
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
        // 单指
        case MotionEvent.ACTION_DOWN:

            startPoint.set(event.getX(), event.getY());
            mode = DRAG;
            break;
        // 双指
        case MotionEvent.ACTION_POINTER_DOWN:
            oriDis = distance(event);
            if (oriDis > 10f) {

                midPoint = middle(event);
                mode = ZOOM;
                twodx = Math.abs(event.getY(0) - event.getY(1));

            }
            break;
        // 手指放开
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_POINTER_UP:
            mode = NONE;
            break;
        // 单指滑动事件
        case MotionEvent.ACTION_MOVE:
            if (mode == DRAG) {
            	
            	float tv = renderer.getAngle();
            	float tvy = renderer.getAngley();
                // 是一个手指拖动
            	float mx = event.getX() - startPoint.x;
            	float my = event.getY() - startPoint.y;
            	
            	if(Math.abs(mx) > Math.abs(my)){
                	tv += mx/5;
                	renderer.setAngle(tv);
            	}
            	else
            	{
            		tvy += -my/5;
                	renderer.setAngley(tvy);
            	}
            	
            	
            } else if (mode == ZOOM) {
                // 两个手指滑动
                float newDist = distance(event);
                if (newDist > 10f) {

                	float old_scale = renderer.getScale();
                	float new_twodx = Math.abs(event.getY(0) - event.getY(1));
                	
                	float dddx = Math.abs(new_twodx - twodx);
                	if(dddx > 10f)
                	{
	                	if(new_twodx > twodx)
	                		old_scale += 0.05f;
	                	else
	                		old_scale -= 0.05f;
	                	twodx = new_twodx;
	                    renderer.setScale(old_scale);
                	}
                }
            }
            break;
        }
        return true;
    }

    // 计算两个触摸点之间的距离
    private float distance(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt(x * x + y * y);
    }

    // 计算两个触摸点的中点
    private PointF middle(MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        return new PointF(x / 2, y / 2);
    }


}
