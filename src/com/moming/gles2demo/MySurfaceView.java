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
	
    private SceneRenderer mRenderer;//场景渲染器
    
    int textureId;//系统分配的纹理id
	
    private MatrixManager mMatrixManager;
    private GestureRecognizer mGestureRecognizer;
    
    boolean zoomed=false;
	public MySurfaceView(Context context) {
        super(context);
        this.setEGLContextClientVersion(2); //设置使用OPENGL ES2.0
        mRenderer = new SceneRenderer();	//创建场景渲染器
        setRenderer(mRenderer);				//设置渲染器		        
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);//设置渲染模式为主动渲染   
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
    	Triangle texRect;//纹理矩形
    	
        public void onDrawFrame(GL10 gl) 
        { 
        	//清除深度缓冲与颜色缓冲
            GLES20.glClear( GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
            //绘制纹理矩形
            //texRect.drawSelf(textureId,mMatrixManager.getTransformMatrix());
            draw(mMatrixManager.width,mMatrixManager.height);
        }  

        public void onSurfaceChanged(GL10 gl, int width, int height) {
            //设置视窗大小及位置 
        	GLES20.glViewport(0, 0, width, height); 
        	//计算GLSurfaceView的宽高比
            float ratio = (float) width / height;
            //调用此方法计算产生透视投影矩阵
            //mMatrixManager.setProjectFrustum(-ratio, ratio, -1, 1, 1, 10);
            //调用此方法产生摄像机9参数位置矩阵
            //mMatrixManager.setCamera(0,0,2,0f,0f,0f,0f,1.0f,0.0f);
            
            if(mMatrixManager==null){
            	mMatrixManager=new MatrixManager(width, height);
            }
            if(texRect==null){
            	//创建三角形对对象 
                texRect=new Triangle(MySurfaceView.this,width, height);     
            }
        }

        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            //设置屏幕背景色RGBA
            GLES20.glClearColor(0.5f,0.5f,0.5f, 1.0f);  
            
            //打开深度检测
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
            //初始化纹理
            initTexture();
            //关闭背面剪裁   
            GLES20.glDisable(GLES20.GL_CULL_FACE);
        }
    }
	
	public void initTexture()//textureId
	{
		//生成纹理ID
		int[] textures = new int[1];
		GLES20.glGenTextures
		(
				1,          //产生的纹理id的数量
				textures,   //纹理id的数组
				0           //偏移量
		);    
		textureId=textures[0];    
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_NEAREST);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_LINEAR);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_CLAMP_TO_EDGE);
        
     
        //通过输入流加载图片===============begin===================
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
        //通过输入流加载图片===============end=====================  
        
        //实际加载纹理
        GLUtils.texImage2D
        (
        		GLES20.GL_TEXTURE_2D,   //纹理类型，在OpenGL ES中必须为GL10.GL_TEXTURE_2D
        		0, 					  //纹理的层次，0表示基本图像层，可以理解为直接贴图
        		bitmapTmp, 			  //纹理图像
        		0					  //纹理边框尺寸
        );
        bitmapTmp.recycle(); 		  //纹理加载成功后释放图片
	}

	@Override
	public boolean onSingleTapUp(float x, float y) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onDoubleTap(float x, float y) {
		if(zoomed){
			//还原
			mMatrixManager.reset();	
		}else {
			//放大
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
