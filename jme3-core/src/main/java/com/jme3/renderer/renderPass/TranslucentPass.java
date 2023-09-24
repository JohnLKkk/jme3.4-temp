package com.jme3.renderer.renderPass;

import com.jme3.renderer.framegraph.FGRenderContext;
import com.jme3.renderer.queue.RenderQueue;

public class TranslucentPass extends ForwardPass{
    @Override
    public void executeDrawCommandList(FGRenderContext renderContext) {
        if(canExecute){
            renderContext.setDepthRange(0, 1);
        }
        super.executeDrawCommandList(renderContext);
    }

    public TranslucentPass() {
        super("TranslucentPass", RenderQueue.Bucket.Translucent);
    }
}
