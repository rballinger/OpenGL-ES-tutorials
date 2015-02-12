/**
 * Copyright (C) 2015 Ryan Ballinger.
 *
 * Application: OpenGL Tutorials
 * Author: Ryan Ballinger
 * Date: 1/2/2015
 * Modified: 2/12/2015
 */

package com.example.ryan.openglestutorials;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;
import android.os.SystemClock;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Ryan on 1/2/2015.
 */
public class TriangleRenderer implements Renderer {

    // store model data in float buffer
    private final FloatBuffer mTriangle1Vertices;
    // private final FloatBuffer mTriangle2Vertices;
    // private final FloatBuffer mTriangle3Vertices;

    // bytes per float
    private final int mBytesPerFloat = 4;

    // store view matrix, thought of as the camera, matrix positions things relative to our eye
    private float[] mViewMatrix = new float[16];

    // this will be used to pass in the transformation matrix
    private int mMVPMatrixHandle;

    // this will be used to pass in model position info
    private int mPositionHandle;

    // this will be used to pass in model color info
    private int mColorHandle;

    // store the projection matrix, this is used to project the scene onto a 2D viewport
    private float[] mProjectionMatrix = new float[16];

    // store model matrix, this is used to move models from object space to world space
    private float[] mModelMatrix = new float[16];

    // allocate storage for final combined matrix, this will be passed into shader program
    private float[] mMVPMatrix = new float[16];

    // elements per vertex
    private final int mStrideBytes = 7 * mBytesPerFloat;

    // offset of the position data
    private final int mPositionOffset = 0;

    // size of the position data in elements
    private final int mPositionDataSize = 3;

    // offset of the color data
    private final int mColorOffset = 3;

    // size of the color data in elements
    private final int mColorDataSize = 4;

    public TriangleRenderer(){
        // this triangle is red, green, blue
        final float[] triangle1VerticesData = {
            // X, Y, Z
            // R, G, B, Alpha
            -0.5f, -0.25f, 0.0f,
            1.0f, 0.0f, 0.0f, 1.0f,

            0.5f, -0.25f, 0.0f,
            0.0f, 0.0f, 1.0f, 1.0f,

            0.0f, 0.559016994f, 0.0f,
            0.0f, 1.0f, 0.0f, 1.0f
        };

        // Initialize the buffers
        mTriangle1Vertices = ByteBuffer.allocateDirect(triangle1VerticesData.length
                * mBytesPerFloat).order(ByteOrder.nativeOrder()).asFloatBuffer();
        // copy over into buffer
        mTriangle1Vertices.put(triangle1VerticesData).position(0);
    }

    @Override
    public void onDrawFrame(GL10 glUnused){
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        // do a complete rotation every 10 seconds
        long time = SystemClock.uptimeMillis() % 10000L;
        float angleInDegrees = (360.0f / 10000.0f) * ((int) time);

        // draw the triangle facing straight on
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, 0.0f, 1.0f);
        drawTriangle(mTriangle1Vertices);
    }

    @Override
    public void onSurfaceChanged(GL10 glUnused, int width, int height){
        // set the OpenGL viewport to the same size as the surface
        GLES20.glViewport(0, 0, width, height);

        // create new perspective projection matrix. The height will stay the same while
        // the width will vary as per aspect ratio.
        final float ratio = (float) width / height;
        final float left = -ratio;
        final float right = ratio;
        final float bottom = -1.0f;
        final float top = 1.0f;
        final float near = 1.0f;
        final float far = 10.0f;

        Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
    }

    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config){
        // set the background clear color to gray
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 0.5f);

        // position the eye behind the origin
        final float eyeX = 0.0f;
        final float eyeY = 0.0f;
        final float eyeZ = 1.5f;

        // we are looking toward the distance
        final float lookX = 0.0f;
        final float lookY = 0.0f;
        final float lookZ = -5.0f;

        // set our up vector, this is where our head would be pointing if we were holding a camera
        final float upX = 0.0f;
        final float upY = 1.0f;
        final float upZ = 0.0f;

        // set view matrix. Matrix represents camera position
        // **In OpenGL 1 a ModelView matrix is used which is both a model and view matrix.
        // In OpenGL 2, these can be separate
        Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);

        final String vertexShader =
            "uniform mat4 u_MVPMatrix;      \n"     // represents combined model/view/projection
                + "attribute vec4 a_Position;     \n"     // per vertex position info we will pass in

                + "attribute vec4 a_Color;        \n"     // per vertex color info we will pass in
                + "varying vec4 v_Color;          \n"     // will be passed in to the fragment shader

                + "void main(){                   \n"     // entry point for our vertex shader
                + "    v_Color = a_Color;         \n"     // pass the color to the fragment shader
                + "    gl_Position = u_MVPMatrix  \n"     // special variable to store final position
                + "                * a_Position;  \n"     // multiply vertex by matrix to get final
                + "}                              \n";    // point normalized screen coordinates

        final String fragmentShader =
            "precision mediump float;       \n"     // set default precision to medium, we don't
                + "varying vec4 v_Color;          \n"     // need as high precision in fragment shader

                + "void main(){                   \n"     // entry point for fragment shader
                + "    gl_FragColor = v_Color;    \n"     // pass color directly through the pipeline
                + "}                              \n";

        // load in vertex shader
        int vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);

        if(vertexShaderHandle != 0){
            // pass in the shader source
            GLES20.glShaderSource(vertexShaderHandle, vertexShader);

            // compile the shader
            GLES20.glCompileShader(vertexShaderHandle);

            // get the compilation status
            final int[] compileStatus = new int[1];
            GLES20.glGetShaderiv(vertexShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

            // if compilation failed, delete the shader
            if(compileStatus[0] == 0){
                GLES20.glDeleteShader(vertexShaderHandle);
                vertexShaderHandle = 0;
            }
        }

        if(vertexShaderHandle == 0){
            throw new RuntimeException("Error creating vertex shader.");
        }

        // load in vertex shader
        int fragmentShaderHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);

        if(fragmentShaderHandle != 0){
            // pass in the shader source
            GLES20.glShaderSource(fragmentShaderHandle, fragmentShader);

            // compile the shader
            GLES20.glCompileShader(fragmentShaderHandle);

            // get the compilation status
            final int[] compileStatus = new int[1];
            GLES20.glGetShaderiv(fragmentShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

            // if compilation failed, delete the shader
            if(compileStatus[0] == 0){
                GLES20.glDeleteShader(fragmentShaderHandle);
                fragmentShaderHandle = 0;
            }
        }

        if(fragmentShaderHandle == 0){
            throw new RuntimeException("Error creating fragment shader.");
        }

        // create a program object and store the handle to it
        int programHandle = GLES20.glCreateProgram();

        if(programHandle != 0){
            // bind the vertex shader to the program
            GLES20.glAttachShader(programHandle, vertexShaderHandle);

            // bind the fragment shader to the program
            GLES20.glAttachShader(programHandle, fragmentShaderHandle);

            // bind attributes
            GLES20.glBindAttribLocation(programHandle, 0, "a_Position");
            GLES20.glBindAttribLocation(programHandle, 1, "a_Color");

            // link the two shaders together into a program
            GLES20.glLinkProgram(programHandle);

            // get the link status
            final int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);

            // if the link failed, delete the program
            if(linkStatus[0] == 0){
                GLES20.glDeleteProgram(programHandle);
                programHandle = 0;
            }
        }

        if(programHandle == 0){
            throw new RuntimeException("Error creating program.");
        }

        // set program handles. These will later be used to pass in values to the program.
        mMVPMatrixHandle = GLES20.glGetUniformLocation(programHandle, "u_MVPMatrix");
        mPositionHandle = GLES20.glGetAttribLocation(programHandle, "a_Position");
        mColorHandle = GLES20.glGetAttribLocation(programHandle, "a_Color");

        // tell OpenGL to use this program when rendering
        GLES20.glUseProgram(programHandle);
    }

    /**
     * Draws a triangle from the given vertex data.
     *
     * @param aTriangleBuffer The buffer containing the vertex data.
     */
    private void drawTriangle(final FloatBuffer aTriangleBuffer){
        // pass in the position info
        aTriangleBuffer.position(mPositionOffset);
        GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false,
                mStrideBytes, aTriangleBuffer);

        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // pass in the color info
        aTriangleBuffer.position(mColorOffset);
        GLES20.glVertexAttribPointer(mColorHandle, mColorDataSize, GLES20.GL_FLOAT, false,
                mStrideBytes, aTriangleBuffer);

        GLES20.glEnableVertexAttribArray(mColorHandle);

        // this multiplies the view matrix by the model matrix, and stores result in the MVP matrix
        // which currently contains model * view
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);

        // this multiplies the modelview matrix by the projection matrix, and stores the result in
        // the MVP matrix which now contains model * view * projection
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
    }
}