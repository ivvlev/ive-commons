package com.ivvlev.util.tuple;

public interface Product extends Iterable<Object>{

    Object productElement(int n);

    int productArity();

}
