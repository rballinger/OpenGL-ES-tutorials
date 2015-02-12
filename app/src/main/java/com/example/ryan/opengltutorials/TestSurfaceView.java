package com.example.ryan.opengltutorials;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.MotionEvent;

/**
 * Created by Ryan on 1/10/2015.
 */
public class TestSurfaceView extends GLSurfaceView{
    private TestRenderer mRenderer;

    public TestSurfaceView(Context context)
    {
        super(context);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        int action = MotionEventCompat.getActionMasked(event);
        final float x = event.getX();
        final float y = event.getY();

        switch(action){
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                if (mRenderer != null)
                {
                    queueEvent(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            mRenderer.fingerOn(x, y);
                        }
                    });
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mRenderer != null)
                {
                    queueEvent(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            mRenderer.fingerOff();
                        }
                    });
                    return true;
                }
                break;
        }

        return super.onTouchEvent(event);
    }

    // Hides superclass method.
    public void setRenderer(TestRenderer renderer)
    {
        mRenderer = renderer;
        super.setRenderer(renderer);
    }
}
