package org.NauhWuun.channel;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public final class OffHeap 
{
    private static final Unsafe   UNSAFE;
    private static final boolean  JVM_64;

    private static final long     B  = 1    * 1;
    private static final long     KB = 1024 * B;
    private static final long     M  = 1024 * KB;
    private static final long     G  = 1024 * M;
    private static final long     T  = 1024 * G;
    private static final long     P  = 1024 * T;
    private static final long     E  = 1024 * P;
    private static final long     Z  = 1024 * E;

    static {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);

            UNSAFE = (Unsafe) field.get(null);
            JVM_64 = UNSAFE.addressSize() == 8;
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private OffHeap() {}

    public static long malloc(long bytes) {
        assert bytes >= 0;
        return UNSAFE.allocateMemory(bytes);
    }

    public static long calloc(long bytes) {
        assert bytes >= 0;

        long pointer = UNSAFE.allocateMemory(bytes);
        UNSAFE.setMemory(pointer, bytes, (byte) 0);

        return pointer;
    }

    public static long realloc(long pointer, long bytes) {
        assert pointer != 0 && bytes >= 0;
        return UNSAFE.reallocateMemory(pointer, bytes);
    }

    public static void free(long pointer) {
        if (pointer != 0)
            UNSAFE.freeMemory(pointer);
    }

    public static long sizeof(Object obj) {
        return sizeof(obj.getClass(), obj);
    }

    public static long sizeof(Class clazz, Object o) {
        long mem_offset = header_size(clazz);

        if (o == null)
            return -1;

        if (clazz.isPrimitive()) {
            switch (clazz.getName()) {
                default:
                    return 8;
                case "long":
                case "double":
                    return 8;
                case "int":
                case "float":
                    return 4;
                case "char":
                case "short":
                    return 2;
                case "byte":
                case "boolean":
                    return 1;
            }
        }

        if (clazz.isArray()) {
            switch (clazz.getName()) {
                default:
                    return 8;
                case "[L":
                    return 8 * ((long[]) o).length;
                case "[D":
                    return 8 * ((double[]) o).length;
                case "[I":
                    return 4 * ((int[]) o).length;
                case "[F":
                    return 4 * ((float[]) o).length;
                case "[C":
                    return 2 * ((char[]) o).length;
                case "[S":
                    return 2 * ((short[]) o).length;
                case "[B":
                    return 1 * ((byte[]) o).length;
                case "[Z":
                    return 1 * ((boolean[]) o).length;
            }
        }

        if (clazz.isEnum())
            return 4;

        if (clazz.isLocalClass() || clazz.isInterface())
            return clazz.getClasses().length;

        long obj_offset = 0;
        Field[] fields  = clazz.getDeclaredFields();

        for (int i = 0; i < fields.length; i++) {
            if ((fields[i].getModifiers() & Modifier.STATIC) == 0) {
                obj_offset = UNSAFE.objectFieldOffset(fields[i]);

                switch (fields[i].getType().getName()) {
                    default: mem_offset += sizeof(fields[i].getType(), UNSAFE.getObject(o, obj_offset)) - header_size(fields[i].getType());
                    break;

                    case "long":  case "double" : mem_offset += 8;  break;
                    case "int" :  case "float"  : mem_offset += 4;  break;
                    case "char":  case "short"  : mem_offset += 2;  break;
                    case "byte":  case "boolean": mem_offset += 1;  break;

                    case "[L":    mem_offset += 8 * ((long[])    UNSAFE.getObject(o, obj_offset)).length;   break;
                    case "[D":    mem_offset += 8 * ((double[])  UNSAFE.getObject(o, obj_offset)).length;   break;
                    case "[I":    mem_offset += 4 * ((int[])     UNSAFE.getObject(o, obj_offset)).length;   break;
                    case "[F":    mem_offset += 4 * ((float[])   UNSAFE.getObject(o, obj_offset)).length;   break;
                    case "[C":    mem_offset += 2 * ((char[])    UNSAFE.getObject(o, obj_offset)).length;   break;
                    case "[S":    mem_offset += 2 * ((short[])   UNSAFE.getObject(o, obj_offset)).length;   break;
                    case "[B":    mem_offset += 1 * ((byte[])    UNSAFE.getObject(o, obj_offset)).length;   break;
                    case "[Z":    mem_offset += 1 * ((boolean[]) UNSAFE.getObject(o, obj_offset)).length;   break;
                }
            }
        }

        return round(mem_offset);
    }

    public static long memchr(long pointer, byte val, int len) {
        assert pointer != 0;

        for (int i = 0; i <= len; i++)
            if ((memget_byte(pointer + i) ^ val) == 0)
                return pointer + i;

        return 0;
    }

    public static void memcpy(Object src, int srcPos, Object dest, int dstPos, int len) {
        assert dest != null || src  != null;

        if (len == 0) {
            return;
        }

        UNSAFE.copyMemory(src, srcPos, dest, dstPos, len);
    }

    public static void memset(long pointer, byte val, int len) {
        assert pointer != 0;
        UNSAFE.setMemory(pointer, len, val);
    }

    public static void memset(long pointer, long[] buffer) {
        assert pointer != 0 || buffer != null;

        for (int i = 0; i < buffer.length << 3; i += 8)
            UNSAFE.putLong(pointer + i, buffer[i >> 3]);
    }

    public static void memset(long pointer, double[] buffer) {
        assert pointer != 0 || buffer != null;

        for (int i = 0; i < buffer.length << 3; i += 8)
            UNSAFE.putDouble(pointer + i, buffer[i >> 3]);
    }

    public static void memset(long pointer, int[] buffer) {
        assert pointer != 0 || buffer != null;

        for (int i = 0; i < buffer.length << 2; i += 4)
            UNSAFE.putInt(pointer + i, buffer[i >> 2]);
    }

    public static void memset(long pointer, float[] buffer) {
        assert pointer != 0 || buffer != null;

        for (int i = 0; i < buffer.length << 2; i += 4)
            UNSAFE.putFloat(pointer + i, buffer[i >> 2]);
    }

    public static void memset(long pointer, short[] buffer) {
        assert pointer != 0 || buffer != null;

        for (int i = 0; i < buffer.length << 1; i += 2)
            UNSAFE.putShort(pointer + i, buffer[i >> 1]);
    }

    public static void memset(long pointer, byte[] buffer) {
        assert pointer != 0 || buffer != null;

        for (int i = 0; i < buffer.length; i++)
            UNSAFE.putByte(pointer + i, buffer[i]);
    }

    public static void memset(long pointer, char[] buffer) {
        assert pointer != 0 || buffer != null;

        for (int i = 0; i < buffer.length; i++)
            UNSAFE.putChar(pointer + i, buffer[i]);
    }

    public static void memset(long pointer, boolean[] buffer) {
        assert pointer != 0 || buffer != null;

        for (int i = 0; i < buffer.length; i++)
            UNSAFE.putByte(pointer + i, (buffer[i]) ? (byte) 1 : (byte) 0);
    }

    public static void memput(long pointer, byte val) {
        assert pointer != 0;
        UNSAFE.putByte(pointer, val);
    }

    public static void memput(long pointer, short val) {
        assert pointer != 0;
        UNSAFE.putShort(pointer, val);
    }

    public static void memput(long pointer, int val) {
        assert pointer != 0;
        UNSAFE.putInt(pointer, val);
    }

    public static void memput(long pointer, long val) {
        assert pointer != 0;

        UNSAFE.putLong(pointer, val);
    }

    public static byte memget_byte(long pointer) {
        assert pointer != 0;
        return UNSAFE.getByte(pointer);
    }

    public static short memget_short(long pointer) {
        assert pointer != 0;
        return UNSAFE.getShort(pointer);
    }

    public static int memget_int(long pointer) {
        assert pointer != 0;
        return UNSAFE.getInt(pointer);
    }

    public static long memget_long(long pointer) {
        assert pointer != 0;
        return UNSAFE.getLong(pointer);
    }

    public static boolean memcmp(long pointer1, long pointer2, int len) {
        assert pointer1 != 0 && pointer2 != 0 && len >= 0;

        if ((len & 8) == 0) return memcmp_l(pointer1, pointer2, len);
        if ((len & 4) == 0) return memcmp_i(pointer1, pointer2, len);
        if ((len & 2) == 0) return memcmp_s(pointer1, pointer2, len);
        else                return memcmp_b(pointer1, pointer2, len);
    }

    private static boolean memcmp_b(long pointer1, long pointer2, int len) {
        for (int i = 0; i < len; i++)
            if ((memget_byte(pointer1 + i) ^ memget_byte(pointer2 + i)) != 0)
                return false;

        return true;
    }

    private static boolean memcmp_s(long pointer1, long pointer2, int len) {
        for (int i = 0; i < len; i += 2)
            if ((memget_short(pointer1 + i) ^ memget_short(pointer2 + i)) != 0)
                return false;

        return true;
    }

    private static boolean memcmp_i(long pointer1, long pointer2, int len) {
        for (int i = 0; i < len; i += 4)
            if ((memget_int(pointer1 + i) ^ memget_int(pointer2 + i)) != 0)
                return false;

        return true;
    }

    private static boolean memcmp_l(long pointer1, long pointer2, int len) {
        for (int i = 0; i < len; i += 8)
            if ((memget_long(pointer1 + i) ^ memget_long(pointer2 + i)) != 0)
                return false;

        return true;
    }

    private static long round(final long num) {
        return (num + 7) / 8 * 8;
    }

    private static long header_size(Class c) {
        long len = (JVM_64) ? 12 : 8;

        if (c.isArray()) 
            len += 4;
        
            return len;
    }
}
