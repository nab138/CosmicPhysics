package me.nabdev.physicsmod.mixins;

import com.badlogic.gdx.utils.ObjectMap;
import finalforeach.cosmicreach.sounds.GameSound;
import finalforeach.cosmicreach.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

@Mixin(GameSound.class)
public class GameSoundMixin {
    @Shadow
    private static ObjectMap<String, GameSound> allSounds = new ObjectMap<>();

    @Unique
    private static final Constructor<GameSound> physics$constructor;

    static {
        try {
            physics$constructor = GameSound.class.getDeclaredConstructor(Identifier.class);
            physics$constructor.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @author nab138
     * @reason puzzle sucks
     */
    @Overwrite
    public static GameSound of(String id) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        System.out.println("Hi from PhysicsMod!");
        GameSound s = allSounds.get(id);
        if (s != null) {
            return s;
        } else {
            s = physics$constructor.newInstance(Identifier.of(id));
            allSounds.put(id, s);
            return s;
        }
    }

}
