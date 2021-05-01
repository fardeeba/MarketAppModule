package com.skylarksit.module.pojos.services;

public enum PriceTag {


    CHEAP(0),
    AVERAGE(1),
    EXPENSIVE(2),
    VERY_EXPENSIVE(3);

    private final Integer value;

    PriceTag(final Integer newValue) {
        value = newValue;
    }

    public Integer getValue() { return value; }
}
