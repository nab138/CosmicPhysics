package me.nabdev.physicsmod.items;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector3;
import com.github.puzzle.core.Identifier;
import com.github.puzzle.core.resources.ResourceLocation;
import com.github.puzzle.game.items.IModItem;
import com.github.puzzle.game.items.data.DataTagManifest;
import finalforeach.cosmicreach.entities.EntityCreator;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.gamestates.GameState;
import finalforeach.cosmicreach.items.ItemSlot;
import me.nabdev.physicsmod.Constants;
import me.nabdev.physicsmod.ICameraOwner;
import me.nabdev.physicsmod.entities.Cube;

import static finalforeach.cosmicreach.gamestates.InGame.world;

public class Launcher implements IModItem {
    DataTagManifest tagManifest = new DataTagManifest();
    public static Identifier id = new Identifier(Constants.MOD_ID, "launcher");
    public static boolean isMagneting = false;

    public Launcher() {
        addTexture(IModItem.MODEL_2_5D_ITEM, new ResourceLocation(Constants.MOD_ID, "textures/items/launcher.png"));
    }

    @Override
    public void use(ItemSlot slot, Player player) {
        PerspectiveCamera cam = ((ICameraOwner) GameState.IN_GAME).browserMod$getCamera();
        if(Cube.magnetCube != null){
            if(!isMagneting) isMagneting = true;
            else {
                isMagneting = false;
                Cube.magnetCube.isMagnet = false;
                Cube.magnetCube = null;
            }
            return;
        }
        Cube e = (Cube)EntityCreator.get(Cube.id.toString());
        e.setPosition(new Vector3(player.getPosition()).add(0, 1.5f,0).add(cam.direction.cpy().scl(2f)));
        player.getZone(world).addEntity(e);
        e.setVelocity(cam.direction.cpy().scl(6));
    }

    @Override
    public String toString() {
        return id.toString();
    }

    @Override
    public Identifier getIdentifier() {
        return id;
    }

    @Override
    public DataTagManifest getTagManifest() {
        return tagManifest;
    }

    @Override
    public boolean isCatalogHidden() {
        return false;
    }
}
