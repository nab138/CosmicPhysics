package me.nabdev.physicsmod;

import com.nikrasoff.seamlessportals.api.IPortalEntityRendererInitialiser;
import finalforeach.cosmicreach.entities.ItemEntity;
import me.nabdev.physicsmod.entities.Cube;
import me.nabdev.physicsmod.entities.CubePortalRenderer;

@SuppressWarnings("unused")
public class PortalInitializer implements IPortalEntityRendererInitialiser {
    @Override
    public void initPortalEntityRenderers() {
        IPortalEntityRendererInitialiser.registerPortalEntityRenderer(Cube.class, new CubePortalRenderer());
        IPortalEntityRendererInitialiser.registerPortalEntityRenderer(ItemEntity.class, new CubePortalRenderer());
    }
}
