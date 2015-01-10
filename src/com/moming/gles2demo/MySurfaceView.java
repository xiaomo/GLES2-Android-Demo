package com.moming.gles2demo;
import java.io.IOException;
import java.io.InputStream;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.view.MotionEvent;

import com.bn.Sample7_1.R;
import com.moming.gesturerecognizer.GestureRecognizer;
import com.moming.gesturerecognizer.GestureRecognizer.Listener;

class MySurfaceView extends GLSurfaceView implements Listener
{
	static{
		System.loadLibrary("hello-jni");
	}
	
    private SceneRenderer mRenderer;//������Ⱦ��
    
    int textureId;//ϵͳ���������id
	
    private MatrixManager mMatrixManager;
    private GestureRecognizer mGestureRecognizer;
    
    boolean zoomed=false;
	public MySurfaceView(Context context) {
        super(context);
        this.setEGLContextClientVersion(2); //����ʹ��OPENGL ES2.0
        mRenderer = new SceneRenderer();	//����������Ⱦ��
        setRenderer(mRenderer);				//������Ⱦ��		        
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);//������ȾģʽΪ������Ⱦ   
        //mMatrixManager=new MatrixManager();
        mGestureRecognizer=new GestureRecognizer(getContext(), this);
        //setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }
	
	@Override
	public boolean onTouchEvent(MotionEvent event){
		mGestureRecognizer.onTouchEvent(event);
		return true;
	}
	private class SceneRenderer implements GLSurfaceView.Renderer 
    {   
    	Triangle texRect;//�������
    	
        public void onDrawFrame(GL10 gl) 
        { 
        	//�����Ȼ�������ɫ����
            GLES20.glClear( GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
            //�����������
            //texRect.drawSelf(textureId,mMatrixManager.getTransformMatrix());
            draw(mMatrixManager.width,mMatrixManager.height);
        }  

        public void onSurfaceChanged(GL10 gl, int width, int height) {
            //�����Ӵ���С��λ�� 
        	GLES20.glViewport(0, 0, width, height); 
        	//����GLSurfaceView�Ŀ�߱�
            float ratio = (float) width / height;
            //���ô˷����������͸��ͶӰ����
            //mMatrixManager.setProjectFrustum(-ratio, ratio, -1, 1, 1, 10);
            //���ô˷������������9����λ�þ���
            //mMatrixManager.setCamera(0,0,2,0f,0f,0f,0f,1.0f,0.0f);
            
            if(mMatrixManager==null){
            	mMatrixManager=new MatrixManager(width, height);
            }
            if(texRect==null){
            	//���������ζԶ��� 
                texRect=new Triangle(MySurfaceView.this,width, height);     
            }
        }

        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            //������Ļ����ɫRGBA
            GLES20.glClearColor(0.5f,0.5f,0.5f, 1.0f);  
            
            //����ȼ��
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
            //��ʼ������
            initTexture();
            //�رձ������   
            GLES20.glDisable(GLES20.GL_CULL_FACE);
        }
    }
	
	public void initTexture()//textureId
	{
		//��������ID
		int[] textures = new int[1];
		GLES20.glGenTextures
		(
				1,          //����������id������
				textures,   //����id������
				0           //ƫ����
		);    
		textureId=textures[0];    
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_NEAREST);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_LINEAR);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_CLAMP_TO_EDGE);
        
     
        //ͨ������������ͼƬ===============begin===================
        InputStream is = this.getResources().openRawResource(R.drawable.wall);
        Bitmap bitmapTmp;
        try 
        {
        	bitmapTmp = BitmapFactory.decodeStream(is);
        } 
        finally 
        {
            try 
            {
                is.close();
            } 
            catch(IOException e) 
            {
                e.printStackTrace();
            }
        }
        //ͨ������������ͼƬ===============end=====================  
        
        //ʵ�ʼ�������
        GLUtils.texImage2D
        (
        		GLES20.GL_TEXTURE_2D,   //�������ͣ���OpenGL ES�б���ΪGL10.GL_TEXTURE_2D
        		0, 					  //����Ĳ�Σ�0��ʾ����ͼ��㣬�������Ϊֱ����ͼ
        		bitmapTmp, 			  //����ͼ��
        		0					  //����߿�ߴ�
        );
        bitmapTmp.recycle(); 		  //������سɹ����ͷ�ͼƬ
	}

	@Override
	public boolean onSingleTapUp(float x, float y) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onDoubleTap(float x, float y) {
		if(zoomed){
			//��ԭ
			mMatrixManager.reset();	
		}else {
			//�Ŵ�
			mMatrixManager.reset();
			mMatrixManager.scale(3,3, x, y);
		}
		zoomed=!zoomed;
		requestRender();
		return false;
	}

	@Override
	public boolean onScroll(float dx, float dy, float totalX, float totalY) {
		zoomed=true;
		mMatrixManager.translate(-dx, -dy);
		requestRender();
		return true;
	}

	@Override
	public boolean onFling(float velocityX, float velocityY) {
		return false;
	}

	@Override
	public boolean onScaleBegin(float focusX, float focusY) {
		return true;
	}

	@Override
	public boolean onScale(float focusX, float focusY, float scale) {
		zoomed=true;
		mMatrixManager.scale(scale, scale, focusX, focusY);
		requestRender();
		return true;
	}

	@Override
	public void onScaleEnd() {
		
	}

	@Override
	public void onDown(float x, float y) {
	}

	@Override
	public void onUp() {
	}
	public native void draw(int w,int h);
}
