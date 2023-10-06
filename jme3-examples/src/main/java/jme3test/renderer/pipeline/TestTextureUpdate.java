package jme3test.renderer.pipeline;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.texture.image.ColorSpace;
import com.jme3.texture.image.ImageRaster;
import com.jme3.ui.Picture;
import com.jme3.util.BufferUtils;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class TestTextureUpdate extends SimpleApplication {
    private Texture2D texture2D;
    private ImageRaster textureUpdate;

    public static void main(String[] args){
        TestTextureUpdate app = new TestTextureUpdate();
        app.start();
    }

    @Override
    public void simpleUpdate(float tpf) {
        super.simpleUpdate(tpf); //To change body of generated methods, choose Tools | Templates.
        for(int i = 0;i < 256;i++){
            for(int j = 0;j < 256;j++){
                textureUpdate.setPixel(i, j, Math.random() > 0.5f ? ColorRGBA.Blue : ColorRGBA.Red);
            }
        }
        texture2D.getImage().setWidth(1);
        texture2D.getImage().setHeight(1);
        texture2D.getImage().setUpdateNeeded();
    }

    public void simpleInitApp() {
        texture2D = new Texture2D(256, 256, Image.Format.RGBA32F);
        texture2D.setMinFilter(Texture.MinFilter.NearestNoMipMaps);
        texture2D.setMagFilter(Texture.MagFilter.Nearest);
        texture2D.setWrap(Texture.WrapMode.EdgeClamp);
        ByteBuffer data = BufferUtils.createByteBuffer( (int)Math.ceil(Image.Format.RGBA32F.getBitsPerPixel() / 8.0) * 256 * 256);
        Image convertedImage = new Image(Image.Format.RGBA32F, 256, 256, data, null, ColorSpace.Linear);
        texture2D.setImage(convertedImage);
        textureUpdate = ImageRaster.create(texture2D.getImage());
        Picture p = new Picture("Picture1");
        p.move(0,0,-1);
        p.setPosition(cam.getWidth() / 2 - (256.0f / 2), cam.getHeight() / 2 - (256.0f / 2));
        p.setWidth(256);
        p.setHeight(256);
        Material mat = new Material(assetManager, "Common/MatDefs/Gui/Gui.j3md");
        mat.setColor("Color", ColorRGBA.White);
        p.setMaterial(mat);
        p.getMaterial().setTexture("Texture", texture2D);
        guiNode.attachChild(p);
    }

}
