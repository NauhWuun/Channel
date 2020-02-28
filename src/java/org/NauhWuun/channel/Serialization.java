package org.NauhWuun.channel;

import java.util.*;

class Serialization
{
    private int _rpos = 0, _wpos = 0, ctr = 0;
    private Vector<Object> _storage;

    public Serialization() {
        _storage = new Vector<>();
    }

    public void Clear() {
        this._storage.clear();
        this._rpos = this._wpos = 0;
    }

    private <T> void Append(Class clazz, T value) {
        long size = sizeof(clazz, value);

        _storage.add(value);
        _wpos += size;
    }
    
    public Serialization Add(boolean value) {
        Append(boolean.class, value);
        return this;
    }

    public Serialization Add(char value) {
        Append(char.class, value);
        return this;
    }

    public Serialization Add(short value) {
        Append(short.class, value);
        return this;
    }

	public Serialization Add(int value) {
        Append(int.class, value);
		return this;
	}

    public Serialization Add(long value) {
        Append(long.class, value);
        return this;
    }

    public Serialization Add(float value) {
        Append(float.class, value);
        return this;
    }

    public Serialization Add(double value) {
        Append(double.class, value);
        return this;
    }

    public Serialization Add(String value) {
        Append(String.class, value);
        return this;
    }

    public Serialization Add(char[] value) {
        Append(value.getClass(), value);
        return this;
    }

    public Serialization Add(byte[] value) {
        Append(value.getClass(), value);
        return this;
    }

    public Serialization Add(byte value) {
        Append(Byte.class, value);
        return this;
    }

    public Serialization Add(List list) {
        Append(List.class, list);
        return this;
    }

    public Serialization Add(Map map) {
        Append(Map.class, map);
        return this;
    }

    public Serialization Add(Class classic) {
        Append(Class.class, classic);
        return this;
    }

    public Serialization Add(Vector vector) {
        Append(Vector.class, vector);
        return this;
    }

    public Serialization of(boolean value) {
        value = Read(value);
        return this;
    }

    public Serialization of(char value) {
        value = Read(value);
        return this;
    }

    public Serialization of(short value) {
        value = Read(value);
        return this;
    }

	public Serialization of(int value) {
		value = Read(value);
		return this;
	}

    public Serialization of(long value) {
        value = Read(value);
		return this;
    }

    public Serialization of(float value) {
        value = Read(value);
		return this;
    }

    public Serialization of(double value) {
        value = Read(value);
		return this;
    }

    public Serialization of(String value) {
        value = Read(value);
		return this;
    }

    public byte[] BinaryEncrypt(byte[] binary) {
        byte[] encr = new byte[binary.length + 1];

		ctr = 0;
 		int counter = 0;

 		for (int i = 0; i < binary.length; i++) {
 			if (i == 0) {
 				if (binary[i] == '1') {
 					encr[ctr] = (byte) counter;

 					ctr++;
 					counter++;
 				} else {
 					counter++;
 				}
 			} else {
 				if (binary[i] == binary[i - 1]) {
 					counter++;
 				} else {
 					encr[ctr] = (byte) counter;
 					ctr++;

 					counter = 1;
 				}
 			}
 		}

 		encr[ctr] = (byte) counter;
        return encr;
    }

 	public byte[] BinaryDecrypt(byte[] binary) {
        byte[] decr = new byte[binary.length + 1];
		int val = 0;
 
		for (int i = 0; i < ctr; i++) {
 			for (int k = 0; k < binary[i]; k++) {
 				 decr[k] = (byte) val;
 			}

			val = (val == 0) ? 1 : 0;
         }
         
		return decr;
    }

    public String toStrings() {
        Iterator<Object> it = _storage.iterator();
        if (! it.hasNext())
            return "";

        StringBuilder sb = new StringBuilder();
        while (it.hasNext()) {
            Object e = it.next();
            sb.append(e).append("\t\n");
        }

        return sb.toString();
    }

    private int Ros() {
        return _rpos;
    }

    private int Rpos(int rpos_) {
        _rpos = rpos_;
        return _rpos;
    }

    private int Wpos() {
        return _wpos;
    }

    private int Wpos(int wpos_) {
        _wpos = wpos_;
        return _wpos;
    }
 
	private <T> T Read(T type) {
        long size = OffHeap.sizeof(type.getClass(), type);
        type = (T) _storage.get((int) size);
        _rpos += size;
		
        return type;
    }

	public <T> T[] ReadArray(int pos) {
        return (T[]) _storage.toArray()[pos];
    }

    public void Read(char[] dest, int len) {
        Read(dest, len, 0);
    }

    private void Read(char[] dest, int len, int pos) {
        assert(_rpos + len  <= Size());

        System.arraycopy(_storage, pos, dest, 0, len);
        _rpos += len;
    }

    public int Size() { 
		return _storage.size(); 
	}
	
    public boolean Empty() { 
		return _storage.isEmpty();
	}

    public static long sizeof(Class clazz, Object o) {
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
                    return 4;
                case "[L":
                    return 1 * ((long[]) o).length;
                case "[D":
                    return 1 * ((double[]) o).length;
                case "[I":
                    return 1 * ((int[]) o).length;
                case "[F":
                    return 1 * ((float[]) o).length;
                case "[C":
                    return 1 * ((char[]) o).length;
                case "[S":
                    return 1 * ((short[]) o).length;
                case "[B":
                    return 1 * ((byte[]) o).length;
                case "[Z":
                    return 1 * ((boolean[]) o).length;
            }
        }

        if (clazz.isEnum())
            return 4;

        if (clazz.isLocalClass() || clazz.isInterface() || clazz.isMemberClass())
            return clazz.getClasses().length;

        if (clazz.isInstance(Class.class)) {
            return 0;
        }

        return 0;
    }
}