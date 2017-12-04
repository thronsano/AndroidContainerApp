package com.example.szaman.androidkonteneryprojekt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Szaman on 04.12.2017.
 */

public class Util {
    public static <T extends Comparable<? super T>> List<T> asSortedList(Collection<T> c) {
        if(c == null)
            return new ArrayList<>();

        List<T> list = new ArrayList<T>(c);
        java.util.Collections.sort(list);
        return list;
    }
}
