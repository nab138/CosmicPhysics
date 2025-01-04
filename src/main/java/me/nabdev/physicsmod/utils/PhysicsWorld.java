
package me.nabdev.physicsmod.utils;

import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.github.puzzle.core.loader.util.ModLocator;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Vector3f;
import com.nikrasoff.seamlessportals.SeamlessPortals;
import com.nikrasoff.seamlessportals.portals.HPGPortal;
import com.nikrasoff.seamlessportals.portals.Portal;
import finalforeach.cosmicreach.blocks.Block;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.networking.server.ServerSingletons;
import finalforeach.cosmicreach.savelib.blockdata.IBlockData;
import finalforeach.cosmicreach.world.Chunk;
import finalforeach.cosmicreach.world.Zone;
import me.nabdev.physicsmod.Constants;
import me.nabdev.physicsmod.entities.Cube;
import me.nabdev.physicsmod.items.GravityGun;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static me.nabdev.physicsmod.utils.PhysicsUtils.isEmpty;

public class PhysicsWorld {
    public static final boolean portalsLoaded = ModLocator.isModLoaded("seamlessportals");

    public static boolean paused = false;
    private static class ChunkBodyData {
        public PhysicsRigidBody body = null;
        public PhysicsRigidBody iceBody = null;
        public boolean isValid = false;
    }

    public static final ArrayList<IPhysicsEntity> allObjects = new ArrayList<>();
    public static final ArrayList<Cube> cubes = new ArrayList<>();
    public static PhysicsSpace space;
    public static final HashMap<Integer, String> blocks = new HashMap<>();
    private static final HashMap<Chunk, ChunkBodyData> chunkBodies = new HashMap<>();
    private static final ArrayList<PhysicsRigidBody> queuedBodies = new ArrayList<>();
    public static HashMap<String, IPhysicsEntity> magnetEntities = new HashMap<>();

    public static boolean isRunning = false;

    public static boolean readyToInitialize = false;
    public static HashMap<Player, IPhysicsEntity> queuedMagnetEntities = new HashMap<>();

    public static MagnetPacket magnetPacket = new MagnetPacket();

    public static float iceFriction = (float)Cube.frictionInterpolation(Block.getInstance("ice").getDefaultBlockState().friction) * 0.5f;

    static {

        Thread physicsThread = new Thread(() -> {
            final long frameTime = 1_000_000_000 / 100; // 100 updates per second
            long lastTime = System.nanoTime();

            while (true) {
                long currentTime = System.nanoTime();
                long elapsedTime = currentTime - lastTime;

                if (elapsedTime >= frameTime) {
                    double delta = elapsedTime / 1_000_000_000.0; // Convert nanoseconds to seconds
                    lastTime = currentTime;

                    if(!paused) PhysicsWorld.tick(delta);
                } else {
                    try {
                        // Sleep to save CPU if the update is ahead of schedule
                        Thread.sleep((frameTime - elapsedTime) / 1_000_000); // Convert nanoseconds to milliseconds
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        });

        physicsThread.setDaemon(true); // Ensure the thread does not prevent JVM shutdown
        physicsThread.start();
    }


    public static void initialize() {
        readyToInitialize = true;
    }

    public static void runInit() {
        if (space != null || isRunning) return;
        readyToInitialize = false;
        isRunning = true;
        initializeWorld();
    }

    private static void initializeWorld() {
        Constants.LOGGER.info("Initializing Physics World");
        space = new PhysicsSpace(PhysicsSpace.BroadphaseType.DBVT);

        space.setGravity(new Vector3f(0, -9.81f, 0));
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

    public static void dropMagnet(Player p) {
        if (magnetEntities.containsKey(p.getAccount().getUniqueId())) {
            IPhysicsEntity magnetEntity = magnetEntities.get(p.getAccount().getUniqueId());
            magnetEntity.setMagnetised(null);
            magnetEntities.remove(p.getAccount().getUniqueId());
            if (ServerSingletons.SERVER != null) {
                magnetPacket.setPlayer(p, false);
                ServerSingletons.SERVER.broadcast(p.getZone(), magnetPacket);
            }
        }
    }

    public static void magnet(Player player, IPhysicsEntity entity) {
        queuedMagnetEntities.put(player, entity);
    }

    private static void setMagnet(Player player, IPhysicsEntity entity) {
        dropMagnet(player);
        magnetEntities.put(player.getAccount().getUniqueId(), entity);
        entity.setMagnetised(player);
        GravityGun.isPlayerMag.put(player.getAccount().getUniqueId(), true);
        if (ServerSingletons.SERVER != null) {
            magnetPacket.setPlayer(player, true);
            ServerSingletons.SERVER.broadcast(player.getZone(), magnetPacket);
        }
    }

    public static void reset() {
        space = null;
        IPhysicsEntity[] entities = allObjects.toArray(new IPhysicsEntity[0]);
        for (IPhysicsEntity entity : entities) {
            entity.kill();
        }
        allObjects.clear();
        cubes.clear();
        chunkBodies.clear();
        queuedBodies.clear();
        isRunning = false;

        for(IPhysicsEntity e : magnetEntities.values()) {
            e.setMagnetised(null);
        }
        magnetEntities.clear();
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
        if(!queuedMagnetEntities.isEmpty()) {
            for (Player queuedMagnetPlayer : queuedMagnetEntities.keySet()) {
                IPhysicsEntity queuedMagnetEntity = queuedMagnetEntities.get(queuedMagnetPlayer);
                setMagnet(queuedMagnetPlayer, queuedMagnetEntity);
            }
            queuedMagnetEntities.clear();
        }
        PhysicsUtils.applyQueuedLinks();
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
        CompoundCollisionShape iceShape = new CompoundCollisionShape();
        AtomicBoolean hasIce = new AtomicBoolean(false);

        Array<Vec3Int> ignoreBlocks = new Array<>();
        if(portalsLoaded){
            for(Portal p : SeamlessPortals.portalManager.createdPortals.values()){
                if(p instanceof HPGPortal hpg){
                    ignoreBlocks.addAll(PortalUtils.getBlocksOn(hpg, chunk.getZone()));
                }
            }
        }

        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    Vec3Int pos = new Vec3Int(x, y, z);
                    Vec3Int globalPos = pos.copy().add(chunk.getBlockX(), chunk.getBlockY(), chunk.getBlockZ());
                    if(ignoreBlocks.contains(globalPos, false)){
                        System.out.println("Ignoring block at " + globalPos);
                        //BlockUtil.setBlockAt(chunk.getZone(), Block.WATER.getDefaultBlockState(), globalPos.x, globalPos.y, globalPos.z);
                        continue;
                    }
                    BlockState state = blockData.getBlockValue(x, y, z);
                    if (isEmpty(state)) continue;
                    Array<BoundingBox> boxes = new Array<>();
                    state.getModel().getAllBoundingBoxes(boxes, 0, 0, 0);
                    boxes.forEach(box -> {
                        Vector3f halfExtents = new Vector3f((box.max.x - box.min.x) / 2, (box.max.y - box.min.y) / 2, (box.max.z - box.min.z) / 2);
                        Vector3f center = new Vector3f(box.getCenterX(), box.getCenterY(), box.getCenterZ());
                        BoxCollisionShape boxShape = new BoxCollisionShape(halfExtents);

                        if(state.friction >=1) chunkShape.addChildShape(boxShape, center.add(pos.toVector3f()));
                        else {
                            iceShape.addChildShape(boxShape, center.add(pos.toVector3f()));
                            hasIce.set(true);
                        }
                    });
                }
            }
        }

        chunkData.isValid = true;
        if (!seenBefore) {
            PhysicsRigidBody body = new PhysicsRigidBody(chunkShape, 0);
            body.setFriction(0.7f);
            body.setPhysicsLocation(new Vector3f(chunk.getBlockX(), chunk.getBlockY(), chunk.getBlockZ()));
            addRigidBody(body);
            chunkData.body = body;


            if (hasIce.get()) {
                PhysicsRigidBody iceBody = new PhysicsRigidBody(iceShape, 0);
                iceBody.setPhysicsLocation(new Vector3f(chunk.getBlockX(), chunk.getBlockY(), chunk.getBlockZ()));
                iceBody.setFriction(iceFriction);
                addRigidBody(iceBody);
                chunkData.iceBody = iceBody;
            }

            chunkBodies.put(chunk, chunkData);
        } else {
            chunkData.body.setCollisionShape(chunkShape);
            if (hasIce.get()) {
                if(chunkData.iceBody != null) {
                    chunkData.iceBody.setCollisionShape(iceShape);
                } else {
                    PhysicsRigidBody iceBody = new PhysicsRigidBody(iceShape, 0);
                    iceBody.setPhysicsLocation(new Vector3f(chunk.getBlockX(), chunk.getBlockY(), chunk.getBlockZ()));
                    iceBody.setFriction(iceFriction);
                    addRigidBody(iceBody);
                    chunkData.iceBody = iceBody;
                }
            } else if (chunkData.iceBody != null) {
                space.removeCollisionObject(chunkData.iceBody);
                chunkData.iceBody = null;
            }
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
