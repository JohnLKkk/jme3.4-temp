package com.jme3.renderer.renderPass;

import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.Renderer;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.framegraph.FGRenderContext;
import com.jme3.renderer.framegraph.FGRenderQueuePass;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;

/**
 * @author JohnKkk
 */
public class OpaquePass extends ForwardPass {
    public OpaquePass(String name) {
        super(name, RenderQueue.Bucket.Opaque);
    }

    @Override
    public void executeDrawCommandList(FGRenderContext renderContext) {
        if(canExecute){
            renderContext.setDepthRange(0, 1);
        }
        super.executeDrawCommandList(renderContext);
    }

    public OpaquePass() {
        super("ForwardOpaquePass", RenderQueue.Bucket.Opaque);
    }
}
