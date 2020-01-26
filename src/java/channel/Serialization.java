package java.channel;

import java.util.Iterator;
import java.util.Vector;

class Serialization
{
    private static final int DEFAULT_SIZE = 0x400;
    private int _rpos = 0, _wpos = 0, ctr = 0;

    private Vector<Object> _storage = new Vector<Object>();

    public Serialization() {
        this(DEFAULT_SIZE + 1);
    }

    public Serialization(int res) {
        _storage = new Vector<Object>(res + 1);
    }
 	
	/**
     * return 
     *  true(BigEndian)
     *  false(LittleEndian)
     */
	public static boolean MemoryEndian() {
		char encode_chars = 0x1122;
		return (String.valueOf(encode_chars).equals(0x11));
	}

    public static int ChangeEndianSwap(int value) {
        char[] bytes = String.valueOf(value).toCharArray();
        return ((int)(bytes[0]) << 3) |
			   ((int)(bytes[1]) << 2) |
               ((int)(bytes[2]) << 1) |
               (int)(bytes[3])		  ;
    }

    public static long ChangeEndianSwap(long value) {
        char[] bytes = String.valueOf(value).toCharArray();
        return ((long)(bytes[0]) << 7) |
			   ((long)(bytes[1]) << 6) |
               ((long)(bytes[2]) << 5) |
               ((long)(bytes[3]) << 4) |
               ((long)(bytes[4]) << 3) |
               ((long)(bytes[5]) << 2) |
               ((long)(bytes[6]) << 1) |
               (long)(bytes[7])		   ;
    }
	
	/**
     * Toggle the 16 bit unsigned integer pointed by *p from little endian to big endian
     * @param p
     */
	public static void memrev16(char[] p) {
		char[] x = (char[]) p;
        char t;

		t 	 = x[0];
		x[0] = x[1];
		x[1] = t;
	}

	public static void memrev32(char[] p) {
		char[] x = (char[]) p;
        char t;

		t 	 = x[0];
		x[0] = x[3];
		x[3] = t;
		
		t 	 = x[1];
		x[1] = x[2];
		x[2] = t;
	}
 
	public static void memrev64(char[] p) {
		char[] x = (char[]) p;
        char t;

		t 	 = x[0];
		x[0] = x[7];
		x[7] = t;
		
		t 	 = x[1];
		x[1] = x[6];
		x[6] = t;
		
		t 	 = x[2];
		x[2] = x[5];
		x[5] = t;
		
		t 	 = x[3];
		x[3] = x[4];
		x[4] = t;
	}

    public void Clear() {
        this._storage.clear();
        this._rpos = this._wpos = 0;
    }

    public <T> void Append(T value) {
        _storage.add(value);
    }
    
    public Serialization Add(boolean value) {
        Append(value);
        return this;
    }

    public Serialization Add(char value) {
        Append(value);
        return this;
    }

    public Serialization Add(short value) {
        Append(value);
        return this;
    }

	public Serialization Add(int value) {
		Append(value);
		return this;
	}

    public Serialization Add(long value) {
        Append(value);
        return this;
    }

    public Serialization Add(float value) {
        Append(value);
        return this;
    }

    public Serialization Add(double value) {
        Append(value);
        return this;
    }

    public Serialization Add(String value) {
        Append(value);
        return this;
    }

    public Serialization Add(char[] str) {
        Append(String.valueOf(str));		
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

    public byte[] BinaryEncrypt(char[] binary) {
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
        ctr++;
        
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
    
    public void reSize(int size) {
        //
        // alloc size must less (Integer.MAX_VALUE - 1000), careful [OOM] event.
        //
        if (_storage.size() + size > (Integer.MAX_VALUE - 1000) && size < DEFAULT_SIZE)
            throw new IllegalArgumentException("reSize Value Size Is To Bigger...");

        //
        // copied src to new dest object
        //
        Vector<Object> _storage_ = new Vector<Object>(_storage.size() + size);
        OffHeap.memcpy(_storage, 0, _storage_, 0, _storage.size());
        
        //
        // review meta object to alloc new size
        // copied new object to this object
        //
        _storage = new Vector<Object>(_storage.size() + size);
        OffHeap.memcpy(_storage_, 0, _storage, 0, _storage.size() + size);
    }

    public String toString() {
        Iterator<Object> it = _storage.iterator();
        if (! it.hasNext())
            return "";

        StringBuilder sb = new StringBuilder();
        for (;;) {
            Object e = it.next();
            sb.append(e);
        }
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

	private <T> T[] ReadArray(long pos) {
        return (T[]) _storage.toArray()[pos];
    }

    private void Read(char[] dest, int len) {
        Read(dest, len, 0);
    }

    private void Read(char[] dest, int len, int pos) {
        assert(_rpos + len  <= Size());

        OffHeap.memcpy(_storage, pos, dest, len, len);
        _rpos += len;
    }

    public int Size() { 
		return _storage.size(); 
	}
	
    public boolean Empty() { 
		return _storage.isEmpty();
	}
}