package com.ivvlev.util;

/**
 * Вспомогательный класс для органицации передачи скалярных переменных по ссылке.
 */
public class Ref<C> {
    private C var_;

    public C get() {
        return var_;
    }

    public void set(C var) {
        this.var_ = var;
    }

    public Ref() {
        this.var_ = null;
    }

    public Ref(C var) {
        this.var_ = var;
    }
}
