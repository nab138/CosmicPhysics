package me.nabdev.physicsmod.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.OrderedMap;
import com.github.puzzle.game.engine.blocks.models.PuzzleBlockModel;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Vector3f;
import finalforeach.cosmicreach.GameAssetLoader;
import finalforeach.cosmicreach.blocks.Block;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.rendering.blockmodels.BlockModelJsonTexture;
import finalforeach.cosmicreach.savelib.blockdata.IBlockData;
import finalforeach.cosmicreach.world.Chunk;
import finalforeach.cosmicreach.world.Zone;
import me.nabdev.physicsmod.entities.Cube;

import java.util.ArrayList;
import java.util.HashMap;

public class PhysicsWorld {
    private static class ChunkBodyData {
        public PhysicsRigidBody body;
        public boolean isValid;

        public ChunkBodyData(){
            this.body = null;
            this.isValid = false;
        }
    }
    public static ArrayList<IPhysicsEntity> allObjects = new ArrayList<>();
    public static ArrayList<Cube> cubes = new ArrayList<>();
    public static PhysicsSpace space;
    public static final HashMap<Integer, String> blocks = new HashMap<>();
    public static final HashMap<Integer, PhysicsRigidBody> blockBodies = new HashMap<>();
    private static final HashMap<Chunk, ChunkBodyData> chunkBodies = new HashMap<>();
    private static final HashMap<BlockState, Texture> blockTextures = new HashMap<>();
    private static final ArrayList<PhysicsRigidBody> queuedBodies = new ArrayList<>();
    public static IPhysicsEntity magnetEntity = null;
    // public static PhysicsRigidBody playerBody;

    public static boolean isMagneting = false;
    public static boolean isRunning = false;

    public static boolean readyToInitialize = false;

    public static void initialize(){
        readyToInitialize = true;
    }

    private static void runInit(){
        if(space != null || isRunning) return;
        readyToInitialize = false;
        isRunning = true;
        initializeWorld();
    }

    private static void initializeWorld() {
        space = new PhysicsSpace(PhysicsSpace.BroadphaseType.DBVT);
        space.setGravity(new Vector3f(0, -9.81f, 0));
    }

    public static Vector3 getPlayerPos(){
        return InGame.getLocalPlayer().getPosition().cpy();
    }

    private static void addRigidBody(PhysicsRigidBody body){
        if(space == null){
            queuedBodies.add(body);
            return;
        }
        space.addCollisionObject(body);
    }

    public static void addEntity(IPhysicsEntity entity){
        allObjects.add(entity);

        addRigidBody(entity.getBody());
    }

    public static void removeEntity(IPhysicsEntity entity){
        allObjects.remove(entity);
        space.removeCollisionObject(entity.getBody());
    }

    public static void removeCube(Cube cube){
        cubes.remove(cube);
        removeEntity(cube);
    }

    public static void addCube(Cube cube){
        cubes.add(cube);
        addEntity(cube);
    }

    public static boolean isEmpty(BlockState b){
        return b == null || b.walkThrough;
    }

    public static void dropMagnet() {
        if(magnetEntity != null) {
            magnetEntity.setMagneted(false);
            magnetEntity = null;
        }
    }

    public static void magnet(IPhysicsEntity entity) {
        dropMagnet();
        isMagneting = true;
        magnetEntity = entity;
        magnetEntity.setMagneted(true);
    }

    public static void reset() {
        space = null;
        blockBodies.clear();
        allObjects.clear();
        blocks.clear();
        cubes.clear();
        chunkBodies.clear();
        blockTextures.clear();
        queuedBodies.clear();
        isRunning = false;

        if(magnetEntity != null) {
            magnetEntity.setMagneted(false);
            magnetEntity = null;
        }
    }

    public static void tick(double delta){
        if(!isRunning && readyToInitialize) runInit();
        if(!isRunning || space == null) return;
        if(!queuedBodies.isEmpty()){
            for(PhysicsRigidBody body : queuedBodies){
                space.addCollisionObject(body);
            }
            queuedBodies.clear();
        }
        space.update((float) delta);
    }

    public static void alertChunk(Zone zone, Chunk chunk){
        if(chunk == null) return;
        addChunk(chunk);
        Array<Chunk> adjacentChunks = new Array<>();
        chunk.getAdjacentChunks(zone, adjacentChunks);
        for(Chunk c : adjacentChunks){
            addChunk(c);
        }
    }

    private static void addChunk(Chunk chunk){
        if(chunk == null) return;
        boolean seenBefore = chunkBodies.containsKey(chunk);
        ChunkBodyData chunkData = chunkBodies.get(chunk);
        if(seenBefore && chunkData != null && chunkData.isValid && chunkData.body != null) return;
        else if(chunkData == null) chunkData = new ChunkBodyData();

        IBlockData<BlockState>  blockData = (IBlockData<BlockState>) chunk.getBlockData();
        if(blockData == null || blockData.isEntirely(Block.AIR.getDefaultBlockState()) || blockData.isEntirely(Block.WATER.getDefaultBlockState())) return;

        CompoundCollisionShape chunkShape = new CompoundCollisionShape();
        for(int x = 0; x < 16; x++){
            for(int y = 0; y < 16; y++){
                for(int z = 0; z < 16; z++){
                    BlockState state = blockData.getBlockValue(x, y, z);
                    if(isEmpty(state)) continue;
                    Vector3 pos = new Vector3(x, y, z);
                    BoxCollisionShape boxShape = new BoxCollisionShape(new Vector3f(0.5f, 0.5f, 0.5f));
                    chunkShape.addChildShape(boxShape, new Vector3f(pos.x + 0.5f, pos.y + 0.5f, pos.z + 0.5f));
                }
            }
        }

        chunkData.isValid = true;
        if(!seenBefore) {
            PhysicsRigidBody body = new PhysicsRigidBody(chunkShape, 0);
            body.setPhysicsLocation(new Vector3f(chunk.getBlockX(), chunk.getBlockY(), chunk.getBlockZ()));
            addRigidBody(body);
            chunkData.body = body;
            chunkBodies.put(chunk, chunkData);
        } else {
            chunkData.body.setCollisionShape(chunkShape);
        }
        for(Cube cube : cubes){
            cube.setMass(2.5f);
        }
    }

    public static void invalidateChunk(Chunk chunk){
        if(chunk == null || !chunkBodies.containsKey(chunk)) return;
        chunkBodies.get(chunk).isValid = false;
        for(IPhysicsEntity entity : allObjects){
            entity.forceActivate();
        }
    }

    public static void createBlockAt(Vector3 pos, BlockState state, Zone zone){
        initialize();
        if(isEmpty(state)) return;
        if(state.getModel() instanceof PuzzleBlockModel blockModelJson){
            OrderedMap<String, BlockModelJsonTexture> textures = blockModelJson.getTextures();
            Texture stitchedTexture;
            if(blockTextures.containsKey(state)){
                stitchedTexture = blockTextures.get(state);
            } else {
                stitchedTexture = createStitchedTexture(textures);
                blockTextures.put(state, stitchedTexture);
            }
            Cube e = new Cube(new Vector3f(pos.x, pos.y, pos.z), state);
            zone.addEntity(e);
            e.setTexture(stitchedTexture);
            e.setMass(0);
        } else {
            System.out.println("Block model is not PuzzleBlockModel, it is: " + state.getModel().getClass());
        }
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
                if(textures.orderedKeys().first().equals("all")) {
                    // Draw the texture 8 times to fill the entire pixmap
                    String key = textures.orderedKeys().first();
                    BlockModelJsonTexture tex = textures.get(key);
                    Texture blockTex = new Texture(GameAssetLoader.loadAsset("textures/blocks/" + tex.fileName));
                    TextureData data = blockTex.getTextureData();
                    data.prepare();
                    Pixmap blockPixmap = data.consumePixmap();
                    for (int i = 0; i < 8; i++) {
                        pixmap.drawPixmap(blockPixmap, positions[i][0], positions[i][1]);
                    }
                    blockPixmap.dispose();
                } else {
                    for (int i = 0; i < order.length; i++) {
                        String key = order[i];
                        if (!key.equals("BLANK") && textures.containsKey(key)) {
                            BlockModelJsonTexture tex = textures.get(key);
                            Texture blockTex = new Texture(GameAssetLoader.loadAsset("textures/blocks/" + tex.fileName));
                            TextureData data = blockTex.getTextureData();
                            data.prepare();
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
}
