package me.nabdev.physicsmod.utils;

import com.jme3.math.Vector3f;

public class Vec3Int {
    public int x;
    public int y;
    public int z;

    public Vec3Int(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3f toVector3f() {
        return new Vector3f(x, y, z);
    }

    public Vec3Int copy() {
        return new Vec3Int(x, y, z);
    }

    public Vec3Int add(int x, int y, int z) {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Vec3Int vec) {
            return vec.x == x && vec.y == y && vec.z == z;
        }
        return false;
    }
}
