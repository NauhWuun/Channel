package org.NauhWuun.channel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Channel
{
    public int ssss = 10;

    public static void main(String[] args) {
        Serialization ser = new Serialization();

        int a = 10;
        boolean b = true;
        short c = 2;
        char d = 't';
        long e = 999999999;
        float f = (float) 0.1;
        double g = 0.000001;
        String h = "test";
        char[] i = {'i', 'a', 'b'};

        List<Integer> list = new ArrayList<>();
        list.add(1234567890);
        list.add(1234567890);
        list.add(1234567890);
        list.add(1234567890);

        Map<String, String> map = new HashMap<>();
        map.put("a", "b");

        ser.Add(a);
        ser.Add(b);
        ser.Add(c);
        ser.Add(d);
        ser.Add(e);
        ser.Add(f);
        ser.Add(g);
        ser.Add(h);
        ser.Add(i);
        ser.Add(list);
        ser.Add(map);
//        ser.Add(AAA.class);

        System.out.println( ser.toStrings() );

        // Serialization der = new Serialization();
        // System.out.println(der.of());
    }

    class AAA
    {

    }
}