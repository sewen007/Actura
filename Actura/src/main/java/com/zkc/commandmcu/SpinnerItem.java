package com.zkc.commandmcu;

/**
 * Created by leoxu on 2017/8/28.
 */

public class SpinnerItem {
    private int ID;
    private double Para;
    private String Value = "";

    public SpinnerItem() {
        ID = 0;
        Value = "";
    }
    public SpinnerItem(int _ID,String _Value) {
        ID = _ID;
        Value = _Value;
    }

    public SpinnerItem(int _ID,double _para, String _Value) {
        ID = _ID;
        Para=_para;
        Value = _Value;
    }

    @Override
    public String toString() {
        // 为什么要重写toString()呢？因为适配器在显示数据的时候，如果传入适配器的对象不是字符串的情况下，直接就使用对象.toString()
        // TODO Auto-generated method stub
        return Value;
    }

    public int getID() {
        return ID;
    }
    public double getPara() {
        return Para;
    }

    public String getValue() {
        return Value;
    }
}
