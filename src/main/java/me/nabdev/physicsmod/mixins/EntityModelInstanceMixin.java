package me.nabdev.physicsmod.mixins;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import finalforeach.cosmicreach.rendering.entities.EntityModelInstance;
import me.nabdev.physicsmod.utils.IPhysicsModelInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityModelInstance.class)
public abstract class EntityModelInstanceMixin implements IPhysicsModelInstance {

    @Unique public Color physicsMod$tint = Color.WHITE;

    @Unique
    private final float[] physicsMod$tmpVec4 = new float[4];

    @Unique
    private void physicsMod$setTintColor(ShaderProgram shader, Color color) {
        int u = shader.getUniformLocation("tintColor");
        if (u != -1) {
            this.physicsMod$tmpVec4[0] = color.r;
            this.physicsMod$tmpVec4[1] = color.g;
            this.physicsMod$tmpVec4[2] = color.b;
            this.physicsMod$tmpVec4[3] = color.a;
            shader.setUniform4fv(u, this.physicsMod$tmpVec4, 0, 4);
        }
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lcom/badlogic/gdx/graphics/Mesh;render(Lcom/badlogic/gdx/graphics/glutils/ShaderProgram;I)V"))
    private void render(Mesh instance, ShaderProgram shader, int primitiveType) {
        physicsMod$setTintColor(shader, physicsMod$tint);
        instance.render(shader, primitiveType);
        physicsMod$setTintColor(shader, Color.WHITE);
    }

    @SuppressWarnings("all")
    @Override
    public void tintSet(Color color) {
        this.physicsMod$tint = color;
    }
}
