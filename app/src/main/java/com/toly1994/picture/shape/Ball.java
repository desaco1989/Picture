package com.toly1994.picture.shape;

import android.content.Context;
import android.opengl.GLES20;

import com.toly1994.picture.utils.GLState;
import com.toly1994.picture.utils.GLUtil;
import com.toly1994.picture.utils.MatrixStack;
import com.toly1994.picture.world.abs.RendererAble;
import com.toly1994.picture.world.base.Cons;

import java.nio.FloatBuffer;
import java.util.ArrayList;

/**
 * 作者：张风捷特烈<br/>
 * 时间：2019/1/9 0009:20:09<br/>
 * 邮箱：1981462002@qq.com<br/>
 * 说明：矩形
 */
public class Ball extends RendererAble {
    private static final String TAG = "Triangle";
    private int mProgram;//OpenGL ES 程序
    private int mPositionHandle;//位置句柄
    private int muMVPMatrixHandle;//顶点变换矩阵句柄
    private int muRHandle;//半径的句柄
    private int muAmbientHandle;//环境光句柄

    private FloatBuffer vertexBuffer;//顶点缓冲
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 3*4=12
    static final int COORDS_PER_VERTEX = 3;//数组中每个顶点的坐标数

    //顶点坐标个数
    private int verticeCount;

    private float mR = 0.8f;

    private int maNormalHandle;//顶点法向量句柄
    private int maLightLocationHandle;//光源位置句柄
    private int muMMatrixHandle;//操作矩阵句柄
    private int maCameraHandle;//相机句柄
    private int muShininessHandle;//粗糙度句柄

    private FloatBuffer mNormalBuffer;

    public Ball(Context context) {
        super(context);
        initVertex(mR * Cons.UNIT_SIZE, 36);
        initProgram();//初始化OpenGL ES 程序
    }


    /**
     * 初始化顶点坐标数据的方法
     *
     * @param r          半径
     * @param splitCount 赤道分割点数
     */
    public void initVertex(float r, int splitCount) {
        // 顶点坐标数据的初始化================begin============================
        ArrayList<Float> vertixs = new ArrayList<>();// 存放顶点坐标的ArrayList
        final float dθ = 360.f / splitCount;// 将球进行单位切分的角度
        //垂直方向angleSpan度一份
        for (float α = -90; α < 90; α = α + dθ) {
            // 水平方向angleSpan度一份
            for (float β = 0; β <= 360; β = β + dθ) {
                // 纵向横向各到一个角度后计算对应的此点在球面上的坐标
                float x0 = r * cos(α) * cos(β);
                float y0 = r * cos(α) * sin(β);
                float z0 = r * sin(α);

                float x1 = r * cos(α) * cos(β + dθ);
                float y1 = r * cos(α) * sin(β + dθ);
                float z1 = r * sin(α);

                float x2 = r * cos(α + dθ) * cos(β + dθ);
                float y2 = r * cos(α + dθ) * sin(β + dθ);
                float z2 = r * sin(α + dθ);

                float x3 = r * cos(α + dθ) * cos(β);
                float y3 = r * cos(α + dθ) * sin(β);
                float z3 = r * sin(α + dθ);

                // 将计算出来的XYZ坐标加入存放顶点坐标的ArrayList
                vertixs.add(x1);
                vertixs.add(y1);
                vertixs.add(z1);//p1
                vertixs.add(x3);
                vertixs.add(y3);
                vertixs.add(z3);//p3
                vertixs.add(x0);
                vertixs.add(y0);
                vertixs.add(z0);//p0
                vertixs.add(x1);
                vertixs.add(y1);
                vertixs.add(z1);//p1
                vertixs.add(x2);
                vertixs.add(y2);
                vertixs.add(z2);//p2
                vertixs.add(x3);
                vertixs.add(y3);
                vertixs.add(z3);//p3
            }
        }

        verticeCount = vertixs.size() / 3;// 顶点的数量为坐标值数量的1/3，因为一个顶点有3个坐标
        // 将vertices中的坐标值转存到一个float数组中
        float vertices[] = new float[verticeCount * 3];
        for (int i = 0; i < vertixs.size(); i++) {
            vertices[i] = vertixs.get(i);
        }
        vertexBuffer = GLUtil.getFloatBuffer(vertices);
//        mNormalBuffer = GLUtil.getFloatBuffer(vertices);
    }

    /**
     * 求sin值
     *
     * @param θ 角度值
     * @return sinθ
     */
    private float sin(float θ) {
        return (float) Math.sin(Math.toRadians(θ));
    }

    /**
     * 求cos值
     *
     * @param θ 角度值
     * @return cosθ
     */
    private float cos(float θ) {
        return (float) Math.cos(Math.toRadians(θ));
    }


    /**
     * 初始化OpenGL ES 程序
     */
    private void initProgram() {
        //顶点着色
        int vertexShader = GLUtil.loadShaderAssets(mContext,
                GLES20.GL_VERTEX_SHADER, "ball.vert");
        //片元着色
        int fragmentShader = GLUtil.loadShaderAssets(mContext,
                GLES20.GL_FRAGMENT_SHADER, "ball.frag");

        mProgram = GLES20.glCreateProgram();//创建空的OpenGL ES 程序
        GLES20.glAttachShader(mProgram, vertexShader);//加入顶点着色器
        GLES20.glAttachShader(mProgram, fragmentShader);//加入片元着色器
        GLES20.glLinkProgram(mProgram);//创建可执行的OpenGL ES项目

        //获取顶点着色器的vPosition成员的句柄
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        //获取顶点法向量的句柄
        maNormalHandle = GLES20.glGetAttribLocation(mProgram, "aNormal");
        //获取程序中总变换矩阵uMVPMatrix成员的句柄
        muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        // 获取程序中球半径引用
        muRHandle = GLES20.glGetUniformLocation(mProgram, "uR");
        //获取环境光句柄
        muAmbientHandle = GLES20.glGetUniformLocation(mProgram, "uAmbient");
        //获取程序中光源位置引用
        maLightLocationHandle = GLES20.glGetUniformLocation(mProgram, "uLightLocation");
        //获取位置、旋转变换矩阵引用
        muMMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMMatrix");
        maCameraHandle = GLES20.glGetUniformLocation(mProgram, "uCamera");
        //粗糙度
        muShininessHandle = GLES20.glGetUniformLocation(mProgram, "uShininess");
    }

    public void draw(float[] mvpMatrix) {
        // 将程序添加到OpenGL ES环境中
        GLES20.glUseProgram(mProgram);
        //启用三角形顶点的句柄
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        // 启用顶点法向量数据
        GLES20.glEnableVertexAttribArray(maNormalHandle);
        //顶点矩阵变换
        GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mvpMatrix, 0);
        //准备三角顶点坐标数据
        GLES20.glVertexAttribPointer(
                mPositionHandle,//int indx, 索引
                COORDS_PER_VERTEX,//int size,大小
                GLES20.GL_FLOAT,//int type,类型
                false,//boolean normalized,//是否标准化
                vertexStride,// int stride,//跨度
                vertexBuffer);// java.nio.Buffer ptr//缓冲
        // 将半径尺寸传入shader程序
        GLES20.glUniform1f(muRHandle, mR * Cons.UNIT_SIZE);
        GLES20.glUniform1f(muShininessHandle, 30);

//        GLES20.glUniform4f(muAmbientHandle, 0.5f,0.5f,0.5f,1f);

        //将位置、旋转变换矩阵传入着色器程序
        GLES20.glUniformMatrix4fv(muMMatrixHandle, 1, false, MatrixStack.getOpMatrix(), 0);
        //将光源位置传入着色器程序
        GLES20.glUniform3fv(maLightLocationHandle, 1, GLState.lightPositionFB);
        //将顶点法向量数据传入渲染管线
        GLES20.glVertexAttribPointer(maNormalHandle, 3, GLES20.GL_FLOAT, false,
                3 * 4, vertexBuffer);

        //将摄像机位置传入着色器程序
        GLES20.glUniform3fv(maCameraHandle, 1, GLState.cameraFB);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, verticeCount);
        //禁用顶点数组:
        //禁用index指定的通用顶点属性数组。
        // 默认情况下，禁用所有客户端功能，包括所有通用顶点属性数组。
        // 如果启用，将访问通用顶点属性数组中的值，
        // 并在调用顶点数组命令（如glDrawArrays或glDrawElements）时用于呈现
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }
}

