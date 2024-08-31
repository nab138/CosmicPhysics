package me.nabdev.physicsmod.utils;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector3;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Vector3f;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.gamestates.GameState;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.world.Zone;
import me.nabdev.physicsmod.entities.Cube;

public class PhysicsUtils {
    public static void applyMagnetForce(Vector3 position, PhysicsRigidBody body) {
        Player player = InGame.getLocalPlayer();
        Vector3 playerPos = player.getPosition().cpy().add(0, 2, 0);
        PerspectiveCamera cam = ((ICameraOwner) GameState.IN_GAME).browserMod$getCamera();
        playerPos.add(cam.direction.cpy().scl(2f));
        Vector3f playerPosF = new Vector3f(playerPos.x, playerPos.y, playerPos.z);

        Vector3f myPos = new Vector3f(position.x, position.y, position.z);
        Vector3f dir = new Vector3f(playerPosF);
        dir = dir.subtract(myPos).mult(3);

        body.setLinearVelocity(dir);
        body.activate(true);
    }

    public static void createBlockAt(Vector3 pos, BlockState state, Zone zone) {
        PhysicsWorld.initialize();
        if (isEmpty(state)) return;
        Texture stitchedTexture = TextureUtils.getTextureForBlock(state);
        Cube e = new Cube(new Vector3f(pos.x, pos.y, pos.z), state);
        zone.addEntity(e);
        e.setTexture(stitchedTexture);
        e.setMass(0);
    }

    public static boolean isEmpty(BlockState b) {
        return b == null || b.walkThrough;
    }
}
