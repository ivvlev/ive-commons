package com.ivvlev.util.tuple;

import java.util.Arrays;
import java.util.Iterator;

public final class Tuple3<T1, T2, T3> implements Product {
    public final T1 _1;
    public final T2 _2;
    public final T3 _3;

    public Tuple3(T1 _1, T2 _2, T3 _3) {
        this._1 = _1;
        this._2 = _2;
        this._3 = _3;
    }

    @Override
    public Object productElement(int n) {
        switch (n) {
            case 0:
                return this._1;
            case 1:
                return this._2;
            case 2:
                return this._3;
            default:
                throw new IndexOutOfBoundsException(n);
        }
    }

    @Override
    public int productArity() {
        return 3;
    }

    @Override
    public Iterator<Object> iterator() {
        return Arrays.stream(new Object[]{_1, _2, _3}).iterator();
    }
}