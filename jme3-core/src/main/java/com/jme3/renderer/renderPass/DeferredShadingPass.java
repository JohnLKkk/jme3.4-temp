package com.jme3.renderer.renderPass;

import com.jme3.light.Light;
import com.jme3.light.LightList;
import com.jme3.material.Material;
import com.jme3.material.MaterialDef;
import com.jme3.material.RenderState;
import com.jme3.renderer.framegraph.*;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.shader.VarType;
import com.jme3.ui.Picture;

/**
 * @author JohnKkk
 */
public class DeferredShadingPass extends ScreenPass{
    public final static String S_RT_0 = "Context_InGBuff0";
    public final static String S_RT_1 = "Context_InGBuff1";
    public final static String S_RT_2 = "Context_InGBuff2";
    public final static String S_RT_3 = "Context_InGBuff3";
    public final static String S_RT_4 = "Context_InGBuff4";
    public final static String S_LIGHT_DATA = "LIGHT_DATA";
    private final static String _S_DEFERRED_PASS = "DeferredPass";
    private static final String _S_DEFERRED_SHADING_PASS_MAT_DEF = "Common/MatDefs/ShadingCommon/DeferredShading.j3md";
    public DeferredShadingPass() {
        super("DeferredShadingPass", RenderQueue.Bucket.Opaque);
    }

    @Override
    public void init() {
        MaterialDef def = (MaterialDef) assetManager.loadAsset(_S_DEFERRED_SHADING_PASS_MAT_DEF);
        screenMat = new Material(def);
        screenRect = new Picture(getName() + "_rect");
        screenRect.setWidth(1);
        screenRect.setHeight(1);
        screenRect.setMaterial(screenMat);

        // register Sinks
        registerSink(new FGTextureBindableSink<FGRenderTargetSource.RenderTargetSourceProxy>(S_RT_0, binds, binds.size() - 1, screenMat, VarType.Texture2D));
        registerSink(new FGTextureBindableSink<FGRenderTargetSource.RenderTargetSourceProxy>(S_RT_1, binds, binds.size() - 1, screenMat, VarType.Texture2D));
        registerSink(new FGTextureBindableSink<FGRenderTargetSource.RenderTargetSourceProxy>(S_RT_2, binds, binds.size() - 1, screenMat, VarType.Texture2D));
        registerSink(new FGTextureBindableSink<FGRenderTargetSource.RenderTargetSourceProxy>(S_RT_3, binds, binds.size() - 1, screenMat, VarType.Texture2D));
        registerSink(new FGTextureBindableSink<FGRenderTargetSource.RenderTargetSourceProxy>(S_RT_4, binds, binds.size() - 1, screenMat, VarType.Texture2D));
        registerSink(new DeferredLightDataSink<DeferredLightDataSource.DeferredLightDataProxy>(S_LIGHT_DATA, binds, binds.size() - 1));
        registerSink(new FGFramebufferCopyBindableSink<FGFramebufferSource.FrameBufferSourceProxy>(FGGlobal.S_DEFAULT_FB, null, false, true, true, binds, binds.size() - 1));
    }

    @Override
    public void executeDrawCommandList(FGRenderContext renderContext) {
        screenMat.selectTechnique(_S_DEFERRED_PASS, renderContext.renderManager);
        DeferredLightDataSink deferredLightDataSink = (DeferredLightDataSink) getSink(S_LIGHT_DATA);
        DeferredLightDataSource.DeferredLightDataProxy deferredLightDataProxy = (DeferredLightDataSource.DeferredLightDataProxy) deferredLightDataSink.getBind();
        LightList lights = deferredLightDataProxy.getLightData();
        boolean depthWrite = screenMat.getAdditionalRenderState().isDepthWrite();
        RenderState.FaceCullMode faceCullMode = screenMat.getAdditionalRenderState().getFaceCullMode();
        screenMat.getAdditionalRenderState().setDepthWrite(false);
        screenMat.setBoolean("UseLightsCullMode", false);
        screenRect.updateGeometricState();
        screenMat.render(screenRect, lights, renderContext.renderManager);
        screenMat.getAdditionalRenderState().setDepthWrite(depthWrite);
    }

    @Override
    public void dispatchPassSetup(RenderQueue renderQueue) {
        canExecute = getSink(S_LIGHT_DATA).isLinkValidate();
    }
}
