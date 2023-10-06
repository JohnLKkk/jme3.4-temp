package jme3test.gi;

import com.jme3.app.SimpleApplication;
import com.jme3.environment.generation.JobProgressAdapter;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.gi.LightProbeVolume;
import com.jme3.light.gi.LightProbeVolumeBake;
import com.jme3.light.gi.LightProbeVolumeVisualize;
import com.jme3.material.MatParamTexture;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.math.Vector4f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.FXAAFilter;
import com.jme3.post.filters.ToneMapFilter;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.shader.VarType;
import com.jme3.shadow.DirectionalLightShadowFilter;
import com.jme3.shadow.EdgeFilteringMode;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture2D;
import com.jme3.texture.image.ColorSpace;
import com.jme3.ui.Picture;
import com.sun.management.HotSpotDiagnosticMXBean;

import java.lang.management.ManagementFactory;

public class TestLightProbeVolume extends SimpleApplication {
    private int frame = 0;
    LightProbeVolume lightProbeVolume;
    private Material octahedralDebugMat;
    private Spatial skyGeo;
    private int probeIndex = 0;
    DirectionalLightShadowFilter dlsf;
    Node sponza;
    @Override
    public void simpleInitApp() {
        sponza = (Node) assetManager.loadModel("Models/sponza/sponza.j3o");
        for(Spatial child : sponza.getChildren()){
            if(child.getName().equals("Sky")){
                skyGeo = child;
            }
            else if(child instanceof Geometry){
                Geometry geo = (Geometry)child;
//                geo.getMaterial().setVector4("Diffuse", new Vector4f(2.2f, 2.2f, 2.2f, 1.0f));
//                geo.getMaterial().setBoolean("UseMaterialColors", true);
//                MatParamTexture texture2D = geo.getMaterial().getTextureParam("DiffuseMap");
//                texture2D.setColorSpace(ColorSpace.Linear);
                geo.getMaterial().setBoolean("UseMaterialColors", false);
//                geo.getMaterial().setParam("Ambient", VarType.Vector4, new ColorRGBA(0.15f, 0.15f, 0.15f, 1));
            }
        }
        if(skyGeo != null){
            sponza.detachChild(skyGeo);
        }
        AmbientLight ambientLight = (AmbientLight)sponza.getLocalLightList().get(1);
        ambientLight.setColor(new ColorRGBA(0.15f, 0.15f, 0.15f, 1.0f));
        DirectionalLight directionalLight = (DirectionalLight) sponza.getLocalLightList().get(0);
        directionalLight.setColor(new ColorRGBA(13, 13, 13, 1));
        rootNode.attachChild(sponza);
        cam.setLocation(new Vector3f(-15.0f, 3.0f, 0.0f));
        cam.setRotation(new Quaternion(new float[]{(float)Math.toRadians(-15), (float)Math.toRadians(90), 0}));
        flyCam.setMoveSpeed(10.0f);

        lightProbeVolume = new LightProbeVolume();
        lightProbeVolume.setProbeOrigin(new Vector3f(-22.0f, 1.8f, -8.0f));
        lightProbeVolume.setProbeCount(new Vector3f(8, 8, 4));
        lightProbeVolume.setProbeStep(new Vector3f(15.6f / 2.0f, 8.0f / 2.0f, 5.35f));
        lightProbeVolume.setIndirectMultiplier(0.24f);
        lightProbeVolume.placeProbes();
        rootNode.addLight(lightProbeVolume);
//        Spatial debugLightProbeVolume = LightProbeVolumeVisualize.generateLightProbeVolumeDebugGeometry(lightProbeVolume);
//        rootNode.attachChild(debugLightProbeVolume);

        /* this shadow needs a directional light */
        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
        dlsf = new DirectionalLightShadowFilter(assetManager, 2048, 1);
        dlsf.setLight(directionalLight);
        dlsf.setShadowIntensity(0.7f);
//        dlsf.setEdgeFilteringMode(EdgeFilteringMode.PCFPOISSON);
//        dlsf.setShadowZExtend(100.0f);
        fpp.addFilter(dlsf);
        FXAAFilter fxaaFilter = new FXAAFilter();
        fpp.addFilter(fxaaFilter);

        ToneMapFilter toneMapFilter = new ToneMapFilter();
//        toneMapFilter.setToneMapModel(ToneMapFilter.ToneMapMode.ACES_FILMIC, renderManager);
//        fpp.addFilter(toneMapFilter);
        viewPort.addProcessor(fpp);

        LightProbeVolumeBake lightProbeVolumeBake = new LightProbeVolumeBake();
        stateManager.attach(lightProbeVolumeBake);


        Picture debugOctahedral = new Picture("debugOctahedral");
        debugOctahedral.move(0,0,1.001f);
        debugOctahedral.setPosition(150, 150);
        debugOctahedral.setWidth(600);
        debugOctahedral.setHeight(400);
        octahedralDebugMat = new Material(assetManager, "Common/MatDefs/Gi/LightProbeVolumeDebug.j3md");
        octahedralDebugMat.selectTechnique("DebugOctahedral", renderManager);
        debugOctahedral.setMaterial(octahedralDebugMat);
//        debugOctahedral.setCullHint(Spatial.CullHint.Always);
        guiNode.attachChild(debugOctahedral);

        inputManager.addMapping("up", new KeyTrigger(KeyInput.KEY_1));
        inputManager.addMapping("down", new KeyTrigger(KeyInput.KEY_2));
        inputManager.addMapping("addMultiplier", new KeyTrigger(KeyInput.KEY_3));
        inputManager.addMapping("subMultiplier", new KeyTrigger(KeyInput.KEY_4));
        inputManager.addMapping("toggle", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("addHdrExposure", new KeyTrigger(KeyInput.KEY_5));
        inputManager.addMapping("subHdrExposure", new KeyTrigger(KeyInput.KEY_6));
//        probeIndex = 131;
        inputManager.addListener(new ActionListener() {
            @Override
            public void onAction(String name, boolean isPressed, float tpf) {
                if(name.equals("addHdrExposure")){
                    toneMapFilter.setExposure(toneMapFilter.getExposure() + 0.1f);
                }
                else if(name.equals("subHdrExposure")){
                    toneMapFilter.setExposure(toneMapFilter.getExposure() - 0.1f);
                }
                if(name.equals("toggle") && !isPressed){
                    lightProbeVolume.setEnabled(!lightProbeVolume.isEnabled());
                }
                if(name.equals("addMultiplier") && isPressed){
                    lightProbeVolume.setIndirectMultiplier(lightProbeVolume.getIndirectMultiplier() + 0.1f);
                    System.out.println("lightProbeVolume:" + lightProbeVolume.getIndirectMultiplier());
                }
                if(name.equals("subMultiplier") && isPressed){
                    lightProbeVolume.setIndirectMultiplier(lightProbeVolume.getIndirectMultiplier() - 0.1f);
                    System.out.println("lightProbeVolume:" + lightProbeVolume.getIndirectMultiplier());
                }
                if(name.equals("up") && !isPressed){
                    probeIndex++;
                }
                else if(name.equals("down") && !isPressed){
                    probeIndex--;
                }
                if(probeIndex < 0){
                    probeIndex = 0;
                }
                else if(probeIndex > 256){
                    probeIndex = 255;
                }
                octahedralDebugMat.setInt("ProbeIndex", probeIndex);
            }
        }, "up", "down", "addMultiplier", "subMultiplier", "toggle", "addHdrExposure", "subHdrExposure");
    }

    @Override
    public void simpleUpdate(float tpf) {
        super.simpleUpdate(tpf);
        if(frame == 2){
            // d
            LightProbeVolumeBake lightProbeVolumeBake = stateManager.getState(LightProbeVolumeBake.class);
            if(lightProbeVolumeBake != null){
                lightProbeVolumeBake.bakeLightProbeVolume(rootNode, lightProbeVolume, new JobProgressAdapter<LightProbeVolume>() {
                    @Override
                    public void done(LightProbeVolume result) {
                        System.out.println("bake done!");
                        octahedralDebugMat.setTexture("OctahedralData", result.getProbeOctahedralIrradiances());
                        Spatial debugLightProbeVolume = LightProbeVolumeVisualize.generateLightProbeVolumeDebugGeometry(lightProbeVolume);
                        rootNode.attachChild(debugLightProbeVolume);
                        rootNode.attachChild(skyGeo);
                        rootNode.updateGeometricState();
//                        dlsf.setShadowIntensity(0.9f);
                        dlsf.setEnabled(false);
                    }
                });
            }
        }
        frame++;
    }

    public static void main(String[] args) {
        final HotSpotDiagnosticMXBean hsdiag = ManagementFactory
                .getPlatformMXBean(HotSpotDiagnosticMXBean.class);
        if (hsdiag != null) {
            System.out.println("MaxDirectMemorySize:" + hsdiag.getVMOption("MaxDirectMemorySize"));
        }
        TestLightProbeVolume testLightProbeVolume = new TestLightProbeVolume();
//        AppSettings settings = new AppSettings(true);
//        settings.setRenderer(AppSettings.LWJGL_OPENGL45);
//        testLightProbeVolume.setSettings(settings);
        testLightProbeVolume.start();
    }
}
