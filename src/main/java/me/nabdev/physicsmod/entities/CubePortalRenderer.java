package me.nabdev.physicsmod.entities;

import com.badlogic.gdx.graphics.Camera;
import com.nikrasoff.seamlessportals.api.IPortalEntityRenderer;
import finalforeach.cosmicreach.entities.Entity;

public class CubePortalRenderer implements IPortalEntityRenderer {

    @Override
    public void render(Entity entity, Camera camera) {
        entity.render(camera);
    }
}
