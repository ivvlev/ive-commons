package com.ivvlev.util.tuple;

import java.util.Arrays;
import java.util.Iterator;

public final class Tuple2<T1, T2> implements Product {
    public final T1 _1;
    public final T2 _2;

    public Tuple2(T1 _1, T2 _2) {
        this._1 = _1;
        this._2 = _2;
    }

    @Override
    public Object productElement(int n) {
        switch (n) {
            case 0:
                return this._1;
            case 1:
                return this._2;
            default:
                throw new IndexOutOfBoundsException(n);
        }
    }

    @Override
    public int productArity() {
        return 2;
    }

    @Override
    public Iterator<Object> iterator() {
        return Arrays.stream(new Object[]{_1, _2}).iterator();
    }
}
