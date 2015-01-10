package com.moming.gles2demo;

import android.annotation.SuppressLint;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

@SuppressLint("NewApi")
public class MatrixManager {

	private float[] mProjectionMatrix;
	private float[] mTransformMatrix;
	private float[] mFinalMatrix;
	
	private android.graphics.Matrix mTransformAssistMatrix;
	private float[] mTransformAssistMatrixTempValuse;

	public int width;
	public int height;
	
	public MatrixManager(int width,int height) {
		this.width=width;
		this.height=height;
		init();
	}

	private void init() {
		mProjectionMatrix = new float[16];
		mTransformMatrix= new float[16];
		mFinalMatrix = new float[16];

		Matrix.setRotateM(mTransformMatrix, 0, 0, 1, 0, 0);
		
		mTransformAssistMatrix=new android.graphics.Matrix();
		mTransformAssistMatrixTempValuse= new float[9];
		
        setProjectOrtho( 
        		0, width,
        		0, height, 
        		0, 1);
        
        float[] aa0=convertCoor(0,0,0);
        
        Log.e("hehe", "(0,0)->"+aa0[0]+","+aa0[1]);
        String glExtension =GLES20.glGetString(GLES20.GL_EXTENSIONS);
        if(glExtension.contains("GL_AMD_compressed_ATC_texture"))
        {
        	Log.e("hehe", "has GL_AMD_compressed_ATC_texture");
        }
        else
        {
        	Log.e("hehe", "no GL_AMD_compressed_ATC_texture");
        }
        
        Log.e("hehe",GLES20.glGetString(GLES20.GL_EXTENSIONS));
	}
	public void setProjectOrtho(
			float left,
			float right,
			float bottom,
			float top,
			float near,
			float far
	) {
		Matrix.orthoM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
		Matrix.translateM(mProjectionMatrix,0, 0, height, 0);
        Matrix.scaleM(mProjectionMatrix,0,1, -1, 1);
	}

	
	public float[] convertCoor(float winx, float winy, float winz){
		float[] win=new float[]{winx,winy,winz,1};
		float[] gl=new float[]{0,0,0,1};
		Matrix.multiplyMV(gl,0,mProjectionMatrix,0,win,0);
		return gl;
	}
	
	public void translate(float dx, float dy){
		float[] aa0=convertCoor(0,0,0);
		float[] aa=convertCoor(dx,dy,0);
		mTransformAssistMatrix.postTranslate(aa[0]-aa0[0], aa[1]-aa0[1]);
		
	}

	public void scale(float sx, float sy, float px,float py){
		float[] aa=convertCoor(px,py,0);
		mTransformAssistMatrix.postScale(sx, sy, aa[0], aa[1]);
		
	}
	
	public void rotate(float degrees, float px, float py){
		float[] aa=convertCoor(px,py,0);
		mTransformAssistMatrix.postRotate(degrees,aa[0], aa[1]);
	}
	
	public float[] getProjectionMatrix(){
		return mProjectionMatrix;
	}
	
	public float[] getTransformMatrix(){
		mTransformAssistMatrix.getValues(mTransformAssistMatrixTempValuse);
		mTransformMatrix[0]=mTransformAssistMatrixTempValuse[0];
		mTransformMatrix[12]=mTransformAssistMatrixTempValuse[2];
		mTransformMatrix[5]=mTransformAssistMatrixTempValuse[4];
		mTransformMatrix[13]=mTransformAssistMatrixTempValuse[5];
		return mTransformMatrix;
	}
	
	public float[] getFinalMatrix() {
		getTransformMatrix();
		Matrix.multiplyMM(mFinalMatrix, 0, mProjectionMatrix, 0, mTransformMatrix, 0);
		return mFinalMatrix;
	}

	public void reset() {
		mTransformAssistMatrix.reset();
	}
}
