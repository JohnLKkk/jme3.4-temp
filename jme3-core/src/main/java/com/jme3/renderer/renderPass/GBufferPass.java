package com.jme3.renderer.renderPass;

import com.jme3.asset.AssetManager;
import com.jme3.asset.plugins.ClasspathLocator;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.light.Light;
import com.jme3.light.LightList;
import com.jme3.material.Material;
import com.jme3.material.MaterialDef;
import com.jme3.material.plugins.J3MLoader;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.Renderer;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.framegraph.*;
import com.jme3.scene.Geometry;
import com.jme3.shader.plugins.GLSLLoader;
import com.jme3.system.JmeSystem;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image;
import com.jme3.texture.Texture2D;
import com.jme3.ui.Picture;

import java.util.ArrayList;
import java.util.List;

/**
 * @author JohnKkk
 */
public class GBufferPass extends OpaquePass{
    private final static String S_GBUFFER_PASS = "GBufferPass";
    public final static String S_RT_0 = "RT_0";
    public final static String S_RT_1 = "RT_1";
    public final static String S_RT_2 = "RT_2";
    public final static String S_RT_3 = "RT_3";
    public final static String S_RT_4 = "RT_4";
    public final static String S_FB = "GBufferFramebuffer";
    public final static String S_LIGHT_DATA = "LIGHT_DATA";
    public final static String S_EXECUTE_STATE = "EXECUTE_STATE";
    private final LightList lightData = new LightList(null);
    private final List<Light> tempLights = new ArrayList<Light>();
    private boolean bHasDraw;
    // gBuffer
    private FrameBuffer gBuffer;
    private Texture2D gBufferData0 = null;
    private Texture2D gBufferData1 = null;
    private Texture2D gBufferData2 = null;
    private Texture2D gBufferData3 = null;
    private Texture2D gBufferData4 = null;
    private ColorRGBA gBufferMask = new ColorRGBA(0, 0, 0, 0);
    private int frameBufferWidth, frameBufferHeight;

    public GBufferPass() {
        super("GBufferPass");
    }

    @Override
    public void executeDrawCommandList(FGRenderContext renderContext) {
        if(canExecute){
            bHasDraw = false;
            tempLights.clear();
            lightData.clear();
            ViewPort vp = null;
            if(forceViewPort != null){
                vp = forceViewPort;
            }
            else{
                vp = renderContext.viewPort;
            }
            reshape(renderContext.renderManager.getRenderer(), vp, vp.getCamera().getWidth(), vp.getCamera().getHeight());
            FrameBuffer opfb = vp.getOutputFrameBuffer();
            vp.setOutputFrameBuffer(gBuffer);
            ColorRGBA opClearColor = vp.getBackgroundColor();
            gBufferMask.set(opClearColor);
            gBufferMask.a = 0.0f;
            renderContext.renderManager.getRenderer().setFrameBuffer(gBuffer);
            renderContext.renderManager.getRenderer().setBackgroundColor(gBufferMask);
            renderContext.renderManager.getRenderer().clearBuffers(vp.isClearColor(), vp.isClearDepth(), vp.isClearStencil());
            String techOrig = renderContext.renderManager.getForcedTechnique();
            renderContext.renderManager.setForcedTechnique(S_GBUFFER_PASS);
            super.executeDrawCommandList(renderContext);
            renderContext.renderManager.setForcedTechnique(techOrig);
            vp.setOutputFrameBuffer(opfb);
            renderContext.renderManager.getRenderer().setBackgroundColor(opClearColor);
            renderContext.renderManager.getRenderer().setFrameBuffer(vp.getOutputFrameBuffer());
            if(bHasDraw){
                for(Light light : tempLights){
                    lightData.add(light);
                }
//                renderContext.renderManager.getRenderer().copyFrameBuffer(gBuffer, vp.getOutputFrameBuffer(), false, true);
            }
        }
    }

    @Override
    public void reset() {
        super.reset();
        tempLights.clear();
        lightData.clear();
    }

    public void reshape(Renderer renderer, ViewPort vp, int w, int h){
        boolean recreate = false;
        if(gBuffer != null){
            if(frameBufferWidth != w || frameBufferHeight != h){
                gBuffer.deleteObject(renderer);

                frameBufferWidth = w;
                frameBufferHeight = h;

                recreate = true;
            }
        }
        else{
            recreate = true;
            frameBufferWidth = w;
            frameBufferHeight = h;
        }

        if(recreate){
            // recreate
            // To ensure accurate results, 32bit is used here for generalization.
            gBufferData0 = new Texture2D(w, h, Image.Format.RGBA32F);
            gBufferData1 = new Texture2D(w, h, Image.Format.RGBA32F);
            gBufferData2 = new Texture2D(w, h, Image.Format.RGBA32F);
            gBufferData3 = new Texture2D(w, h, Image.Format.RGBA32F);   // The third buffer provides 32-bit floating point to store high-precision information, such as normals
            // todo:后续调整为Depth24Stencil8,然后使用一个SceneColorFBO用于渲染所有3D部分,然后将其color_attach_0复制到BackBuffer中
            // todo:然后开启DepthTest绘制最后的所有GUI
            this.getSinks().clear();
            // Depth16/Depth32/Depth32F provide higher precision to prevent clipping when camera gets close, but it seems some devices do not support copying Depth16/Depth32/Depth32F to default FrameBuffer.
            gBufferData4 = new Texture2D(w, h, Image.Format.Depth);
            gBuffer = new FrameBuffer(w, h, 1);
            FrameBuffer.FrameBufferTextureTarget rt0 = FrameBuffer.FrameBufferTarget.newTarget(gBufferData0);
            FrameBuffer.FrameBufferTextureTarget rt1 = FrameBuffer.FrameBufferTarget.newTarget(gBufferData1);
            FrameBuffer.FrameBufferTextureTarget rt2 = FrameBuffer.FrameBufferTarget.newTarget(gBufferData2);
            FrameBuffer.FrameBufferTextureTarget rt3 = FrameBuffer.FrameBufferTarget.newTarget(gBufferData3);
            FrameBuffer.FrameBufferTextureTarget rt4 = FrameBuffer.FrameBufferTarget.newTarget(gBufferData4);
            gBuffer.addColorTarget(rt0);
            gBuffer.addColorTarget(rt1);
            gBuffer.addColorTarget(rt2);
            gBuffer.addColorTarget(rt3);
            gBuffer.setDepthTarget(rt4);
            gBuffer.setMultiTarget(true);
            registerSource(new FGRenderTargetSource(S_RT_0, rt0));
            registerSource(new FGRenderTargetSource(S_RT_1, rt1));
            registerSource(new FGRenderTargetSource(S_RT_2, rt2));
            registerSource(new FGRenderTargetSource(S_RT_3, rt3));
            registerSource(new FGRenderTargetSource(S_RT_4, rt4));
            registerSource(new DeferredLightDataSource(S_LIGHT_DATA, lightData));
            registerSource(new FGVarSource<Boolean>(S_EXECUTE_STATE, bHasDraw));
            registerSource(new FGFramebufferSource(S_FB, gBuffer));
        }
    }

    @Override
    public boolean drawGeometry(RenderManager rm, Geometry geom) {
        Material material = geom.getMaterial();
        if(material.getMaterialDef().getTechniqueDefs(rm.getForcedTechnique()) == null)return false;
        rm.renderGeometry(geom);
        if(material.getActiveTechnique() != null){
            // todo:应该使用一个统一的材质材质,其中根据shadingModeId分开着色
            if(material.getMaterialDef().getTechniqueDefs(S_GBUFFER_PASS) != null){
                LightList lights = geom.getFilterWorldLights();
                for(Light light : lights){
                    if(!tempLights.contains(light)){
                        tempLights.add(light);
                    }
                }
                // todo:无论是否拥有lights,只要包含GBufferPass的材质物体都会执行DeferredShading,根据shadingModelId着色
                bHasDraw = true;
                return true;
            }
        }
        return false;
    }

    @Override
    public void prepare(FGRenderContext renderContext) {
        super.prepare(renderContext);
        ViewPort vp = null;
        if(forceViewPort != null){
            vp = forceViewPort;
        }
        else{
            vp = renderContext.viewPort;
        }
        reshape(renderContext.renderManager.getRenderer(), vp, vp.getCamera().getWidth(), vp.getCamera().getHeight());
    }
}
