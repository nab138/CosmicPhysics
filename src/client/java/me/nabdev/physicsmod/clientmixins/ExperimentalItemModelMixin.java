package me.nabdev.physicsmod.clientmixins;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.github.puzzle.game.engine.items.ExperimentalItemModel;
import com.github.puzzle.game.items.IModItem;
import com.github.puzzle.game.items.data.DataTagManifest;
import com.github.puzzle.game.util.DataTagUtil;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.items.ItemStack;
import finalforeach.cosmicreach.rendering.shaders.GameShader;
import finalforeach.cosmicreach.world.Sky;
import finalforeach.cosmicreach.world.Zone;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

// Temporary because puzzle est broken
@Mixin(ExperimentalItemModel.class)
public abstract class ExperimentalItemModelMixin {
    @Shadow
    IModItem item;

    @Shadow
    GameShader program;

    @Final
    @Shadow
    static Color tintColor;

    @Final
    @Shadow
    static BlockPosition tmpBlockPos;

    @Shadow
    public abstract Mesh getMeshFromIndex(int index);

    @Shadow
    public abstract Texture getTextureFromIndex(int index);

    /**
     * @author nab138
     * @reason not updated for latest cosmicreach
     */
    @Overwrite
    public void renderGeneric(Vector3 pos, ItemStack stack, Camera cam, Matrix4 tmpMatrix, boolean isSlot) {
        DataTagManifest stackManifest;
        try {
            stackManifest = DataTagUtil.getManifestFromStack(stack);
        } catch (Exception var11) {
            stackManifest = null;
        }

        int currentEntry;
        if (stackManifest != null) {
            currentEntry = stackManifest.hasTag("currentEntry") ? stackManifest.getTag("currentEntry").getTagAsType(Integer.class).getValue() : 0;
            currentEntry = currentEntry >= this.item.getTextures().size() ? 0 : currentEntry;
        } else {
            currentEntry = 0;
        }

        if (isSlot) {
            tintColor.set(Color.WHITE);
        } else {
            Zone zone = InGame.getLocalPlayer().getZone();

            try {
                Entity.setLightingColor(zone, pos, Sky.currentSky.currentAmbientColor, tintColor, tmpBlockPos, tmpBlockPos);
            } catch (Exception var10) {
                tintColor.set(Color.WHITE);
            }
        }

        this.program.bind(cam);
        this.program.bindOptionalMatrix4("u_projViewTrans", cam.combined);
        this.program.bindOptionalMatrix4("u_modelMat", tmpMatrix);
        this.program.bindOptionalUniform4f("tintColor", tintColor);
        this.program.bindOptionalInt("isInSlot", isSlot ? 1 : 0);
        this.program.bindOptionalTexture("texDiffuse", this.getTextureFromIndex(currentEntry), 0);
        if (getMeshFromIndex(currentEntry) != null) {
            this.getMeshFromIndex(currentEntry).render(this.program.shader, 4);
        }

        this.program.unbind();
    }
}
