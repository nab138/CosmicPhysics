package me.nabdev.physicsmod.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.utils.OrderedMap;
import com.github.puzzle.game.engine.blocks.models.PuzzleBlockModel;
import finalforeach.cosmicreach.GameAssetLoader;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.rendering.blockmodels.BlockModelJsonTexture;

import java.util.HashMap;

public class TextureUtils {
    private static final HashMap<BlockState, Texture> blockTextures = new HashMap<>();

    public static Texture getTextureForBlock(BlockState state) {
        if (state.getModel() instanceof PuzzleBlockModel blockModelJson) {
            OrderedMap<String, BlockModelJsonTexture> textures = blockModelJson.getTextures();
            Texture stitchedTexture;
            if (blockTextures.containsKey(state)) {
                stitchedTexture = blockTextures.get(state);
            } else {
                stitchedTexture = TextureUtils.createStitchedTexture(textures);
                blockTextures.put(state, stitchedTexture);
            }
            return stitchedTexture;
        } else {
            System.out.println("Block model is not PuzzleBlockModel, it is: " + state.getModel().getClass());
        }
        return null;
    }

    public static Texture createStitchedTexture(OrderedMap<String, BlockModelJsonTexture> textures) {
        final Pixmap pixmap = new Pixmap(64, 32, Pixmap.Format.RGBA8888);

        // Define positions for each texture
        int[][] positions = {
                {0, 0}, {16, 0}, {32, 0}, {48, 0},
                {0, 16}, {16, 16}, {32, 16}, {48, 16}
        };

        // Define the order of textures
        String[] order = {"BLANK", "top", "bottom", "BLANK", "side", "side", "side", "side"};

        final Texture[] stitchedTexture = new Texture[1];
        Gdx.app.postRunnable(() -> {
            // Iterate through the order and draw each texture
            if (textures.orderedKeys().first().equals("all")) {
                // Draw the texture 8 times to fill the entire pixmap
                String key = textures.orderedKeys().first();
                BlockModelJsonTexture tex = textures.get(key);

                for (int i = 0; i < 8; i++) {
                    Texture blockTex = new Texture(GameAssetLoader.loadAsset("textures/blocks/" + tex.fileName));
                    Texture correctTex;
                    if (i < 4) {
                        correctTex = flipY(blockTex);
                    } else if (i == 4 || i == 6) {
                        correctTex = blockTex;
                    } else {
                        correctTex = flipX(blockTex);
                    }

                    TextureData data = correctTex.getTextureData();
                    try {
                        data.prepare();
                    } catch (Exception ignored) {
                    }
                    Pixmap blockPixmap = data.consumePixmap();
                    pixmap.drawPixmap(blockPixmap, positions[i][0], positions[i][1]);
                    blockPixmap.dispose();
                }
            } else {
                for (int i = 0; i < order.length; i++) {
                    String key = order[i];
                    if (!key.equals("BLANK") && textures.containsKey(key)) {
                        BlockModelJsonTexture tex = textures.get(key);
                        Texture blockTex = new Texture(GameAssetLoader.loadAsset("textures/blocks/" + tex.fileName));
                        Texture correctTex;

                        switch (key) {
                            case "top":
                                correctTex = flipY(blockTex);
                                break;
                            case "side":
                                if (i == 4 || i == 6) {
                                    correctTex = blockTex;
                                } else {
                                    correctTex = flipX(blockTex);
                                }
                                break;
                            default:
                                correctTex = blockTex;
                                break;
                        }
                        TextureData data = correctTex.getTextureData();
                        try {
                            data.prepare();
                        } catch (Exception ignored) {
                        }
                        Pixmap blockPixmap = data.consumePixmap();

                        pixmap.drawPixmap(blockPixmap, positions[i][0], positions[i][1]);
                        blockPixmap.dispose();
                    }
                }
            }

            // Create a new Texture from the Pixmap
            stitchedTexture[0] = new Texture(pixmap);
            pixmap.dispose();
        });

        // Wait for the runnable to complete
        while (stitchedTexture[0] == null) {
            Thread.yield();
        }

        return stitchedTexture[0];
    }

    public static Texture flipY(Texture texture) {
        TextureData data = texture.getTextureData();
        data.prepare();
        Pixmap donorPixmap = data.consumePixmap();
        Pixmap newPixmap = new Pixmap(donorPixmap.getWidth(), donorPixmap.getHeight(), donorPixmap.getFormat());

        for (int x = 0; x < donorPixmap.getWidth(); ++x) {
            for (int y = 0; y < donorPixmap.getHeight(); ++y) {
                newPixmap.drawPixel(x, y, donorPixmap.getPixel(x, donorPixmap.getHeight() - 1 - y));
            }
        }

        return new Texture(newPixmap);
    }

    public static Texture flipX(Texture texture) {
        TextureData data = texture.getTextureData();
        data.prepare();
        Pixmap donorPixmap = data.consumePixmap();
        Pixmap newPixmap = new Pixmap(donorPixmap.getWidth(), donorPixmap.getHeight(), donorPixmap.getFormat());

        for (int x = 0; x < donorPixmap.getWidth(); ++x) {
            for (int y = 0; y < donorPixmap.getHeight(); ++y) {
                newPixmap.drawPixel(x, y, donorPixmap.getPixel(donorPixmap.getWidth() - 1 - x, y));
            }
        }

        return new Texture(newPixmap);
    }
}
