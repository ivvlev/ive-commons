package com.ivvlev.util.event;




/**
 *Абстрактное базовое событие
 * Неизменный
 */
abstract public class BaseEventAbst implements BaseEvent {
    /**Источник сообщения*/
    protected final Object sourceObj_;
    protected BaseEventAbst(
        Object sourceObj
    ){
        sourceObj_ =  sourceObj;
    }
    /**
     * Гетер
     * @return источник
     */
    @Override
    public Object getSourceObj() {
        return sourceObj_;
    }

}
