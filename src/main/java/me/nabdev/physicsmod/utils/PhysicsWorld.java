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
import me.nabdev.physicsmod.items.GravityGun;
import me.nabdev.physicsmod.items.Linker;

import java.util.ArrayList;
import java.util.HashMap;

import static me.nabdev.physicsmod.utils.PhysicsUtils.isEmpty;

public class PhysicsWorld {
    private static class ChunkBodyData {
        public PhysicsRigidBody body;
        public boolean isValid;

        public ChunkBodyData() {
            this.body = null;
            this.isValid = false;
        }
    }

    public static final ArrayList<IPhysicsEntity> allObjects = new ArrayList<>();
    public static final ArrayList<Cube> cubes = new ArrayList<>();
    public static PhysicsSpace space;
    public static final HashMap<Integer, String> blocks = new HashMap<>();
    public static final HashMap<Integer, PhysicsRigidBody> blockBodies = new HashMap<>();
    private static final HashMap<Chunk, ChunkBodyData> chunkBodies = new HashMap<>();
    private static final ArrayList<PhysicsRigidBody> queuedBodies = new ArrayList<>();
    public static IPhysicsEntity magnetEntity = null;

    public static boolean isRunning = false;

    public static boolean readyToInitialize = false;

    public static final ArrayList<IPhysicsEntity[]> queuedLinks = new ArrayList<>();

    public static IPhysicsEntity queuedMagnetEntity = null;

    public static void initialize() {
        readyToInitialize = true;
    }

    private static void runInit() {
        if (space != null || isRunning) return;
        readyToInitialize = false;
        isRunning = true;
        initializeWorld();
    }

    private static void initializeWorld() {
        space = new PhysicsSpace(PhysicsSpace.BroadphaseType.DBVT);
        space.setGravity(new Vector3f(0, -9.81f, 0));
    }

    public static Vector3 getPlayerPos() {
        return InGame.getLocalPlayer().getPosition().cpy();
    }

    private static void addRigidBody(PhysicsRigidBody body) {
        if (space == null) {
            queuedBodies.add(body);
            return;
        }
        space.addCollisionObject(body);
    }

    public static void addEntity(IPhysicsEntity entity) {
        allObjects.add(entity);
        addRigidBody(entity.getBody());
    }

    public static void removeEntity(IPhysicsEntity entity) {
        allObjects.remove(entity);
        if (space != null) {
            space.removeCollisionObject(entity.getBody());
        }
    }

    public static void removeCube(Cube cube) {
        cubes.remove(cube);
        removeEntity(cube);
    }

    public static void addCube(Cube cube) {
        cubes.add(cube);
        addEntity(cube);
    }

    public static void dropMagnet() {
        if (magnetEntity != null) {
            magnetEntity.setMagnetised(false);
            magnetEntity = null;
            GravityGun.isMag = false;
        }
    }

    public static void magnet(IPhysicsEntity entity) {
        queuedMagnetEntity = entity;
    }

    private static void setMagnet(IPhysicsEntity entity) {
        dropMagnet();
        magnetEntity = entity;
        magnetEntity.setMagnetised(true);
        GravityGun.isMag = true;
    }

    public static void reset() {
        space = null;
        blockBodies.clear();
        IPhysicsEntity[] entities = allObjects.toArray(new IPhysicsEntity[0]);
        for (IPhysicsEntity entity : entities) {
            entity.kill();
        }
        allObjects.clear();
        blocks.clear();
        cubes.clear();
        chunkBodies.clear();
        queuedBodies.clear();
        isRunning = false;

        if (magnetEntity != null) {
            magnetEntity.setMagnetised(false);
            magnetEntity = null;
        }
    }

    public static void tick(double delta) {
        if (!isRunning && readyToInitialize) runInit();
        if (!isRunning || space == null) return;
        if (!queuedBodies.isEmpty()) {
            for (PhysicsRigidBody body : queuedBodies) {
                space.addCollisionObject(body);
            }
            queuedBodies.clear();
        }
        for (IPhysicsEntity[] link : queuedLinks) {
            if (link[0] != null && link[1] != null) {
                Linker.entityOne = link[0];
                Linker.entityTwo = link[1];
                Linker.link();
                Linker.entityOne = null;
                Linker.entityTwo = null;
            }
        }
        if (queuedMagnetEntity != null) {
            setMagnet(queuedMagnetEntity);
            queuedMagnetEntity = null;
        }
        queuedLinks.clear();
        space.update((float) delta);
    }

    public static void alertChunk(Zone zone, Chunk chunk) {
        if (chunk == null) return;
        addChunk(chunk);
        Array<Chunk> adjacentChunks = new Array<>();
        chunk.getAdjacentChunks(zone, adjacentChunks);
        for (Chunk c : adjacentChunks) {
            addChunk(c);
        }
    }

    private static void addChunk(Chunk chunk) {
        if (chunk == null) return;
        boolean seenBefore = chunkBodies.containsKey(chunk);
        ChunkBodyData chunkData = chunkBodies.get(chunk);
        if (seenBefore && chunkData != null && chunkData.isValid && chunkData.body != null) return;
        else if (chunkData == null) chunkData = new ChunkBodyData();

        //noinspection ALL
        IBlockData<BlockState> blockData = (IBlockData<BlockState>) chunk.getBlockData();
        if (blockData == null || blockData.isEntirely(Block.AIR.getDefaultBlockState()) || blockData.isEntirely(Block.WATER.getDefaultBlockState()))
            return;

        CompoundCollisionShape chunkShape = new CompoundCollisionShape();
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    BlockState state = blockData.getBlockValue(x, y, z);
                    if (isEmpty(state)) continue;
                    Vector3 pos = new Vector3(x, y, z);
                    BoxCollisionShape boxShape = new BoxCollisionShape(new Vector3f(0.5f, 0.5f, 0.5f));
                    chunkShape.addChildShape(boxShape, new Vector3f(pos.x + 0.5f, pos.y + 0.5f, pos.z + 0.5f));
                }
            }
        }

        chunkData.isValid = true;
        if (!seenBefore) {
            PhysicsRigidBody body = new PhysicsRigidBody(chunkShape, 0);
            body.setPhysicsLocation(new Vector3f(chunk.getBlockX(), chunk.getBlockY(), chunk.getBlockZ()));
            addRigidBody(body);
            chunkData.body = body;
            chunkBodies.put(chunk, chunkData);
        } else {
            chunkData.body.setCollisionShape(chunkShape);
        }
        for (Cube cube : cubes) {
            cube.setMass(2.5f);
        }
    }

    public static void invalidateChunk(Chunk chunk) {
        if (chunk == null || !chunkBodies.containsKey(chunk)) return;
        chunkBodies.get(chunk).isValid = false;
        for (IPhysicsEntity entity : allObjects) {
            entity.forceActivate();
        }
    }
}
