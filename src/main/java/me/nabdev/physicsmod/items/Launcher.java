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

    public Launcher() {
        addTexture(IModItem.MODEL_2_5D_ITEM, new ResourceLocation(Constants.MOD_ID, "textures/items/launcher.png"));
    }

    @Override
    public void use(ItemSlot slot, Player player) {
        PerspectiveCamera cam = ((ICameraOwner) GameState.IN_GAME).browserMod$getCamera();
        if(Cube.magnetCube != null){
            Cube.magnetCube.isMagnet = false;
            Cube.magnetCube.setVelocity(cam.direction.cpy().scl(15));
            Cube.magnetCube = null;
            return;
        }
        Cube e = (Cube)EntityCreator.get(Cube.id.toString());
        e.setPosition(new Vector3(player.getPosition()).add(0, 1,0));
        player.getZone(world).addEntity(e);
        e.setVelocity(cam.direction.cpy().scl(15));
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
