package me.nabdev.physicsmod.utils;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Vector3f;
import finalforeach.cosmicreach.blocks.Block;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.gamestates.InGame;
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
    public static IPhysicsEntity magnetEntity = null;
    // public static PhysicsRigidBody playerBody;

    public static boolean isMagneting = false;
    public static boolean isRunning = false;



    public static void initialize(){
        if(space != null || isRunning) return;
        isRunning = true;
        initializeWorld();
    }

    private static void initializeWorld() {
        space = new PhysicsSpace(PhysicsSpace.BroadphaseType.DBVT);
        space.setGravity(new Vector3f(0, -9.81f, 0));

        // Create a 2 block tall, 0.5 block wide, 0.5 block deep box for the player
//        BoxCollisionShape boxShape = new BoxCollisionShape(new Vector3f(0.255f, 1f, 0.255f));
//        Transform startTransform = new Transform();
//        startTransform.loadIdentity();
//        float mass = 50.0f;
        //playerBody = new PhysicsRigidBody(boxShape, mass);
        //playerBody.setGravity(new Vector3f(0, 0f, 0));

        //addRigidBody(playerBody);
    }

    public static Vector3 getPlayerPos(){
        return InGame.getLocalPlayer().getPosition().cpy();
    }

    private static void addRigidBody(PhysicsRigidBody body){
        space.addCollisionObject(body);
    }

    private static void addEntity(IPhysicsEntity entity){
        allObjects.add(entity);
        addRigidBody(entity.getBody());
    }

    public static void removeCube(Cube cube){
        cubes.remove(cube);
        allObjects.remove(cube);
        space.removeCollisionObject(cube.getBody());
    }

    public static void addCube(Cube cube){
        cubes.add(cube);
        addEntity(cube);
    }

    public static boolean isSolid(BlockState b){
        return b != null && !b.walkThrough;
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
        //playerBody = null;
        isRunning = false;
        if(magnetEntity != null) {
            magnetEntity.setMagneted(false);
            magnetEntity = null;
        }
    }

    public static void tick(double delta){
        if(!isRunning || space == null) return;
        space.update((float) delta);
    }

    public static void alertChunk(Zone zone, Chunk chunk){
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
                    if(!isSolid(state)) continue;
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
    }

    public static void invalidateChunk(Chunk chunk){
        if(chunk == null || !chunkBodies.containsKey(chunk)) return;
        chunkBodies.get(chunk).isValid = false;
        for(IPhysicsEntity entity : allObjects){
            entity.forceActivate();
        }
    }
}
