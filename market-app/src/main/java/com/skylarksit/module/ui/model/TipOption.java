package com.skylarksit.module.ui.model;

public class TipOption {

    private String tipLabel;
    private double tipValue;

    private boolean isSelected;

    public String getTipLabel() {
        return tipLabel;
    }

    public void setTipLabel(String tipLabel) {
        this.tipLabel = tipLabel;
    }

    public double getTipValue() {
        return tipValue;
    }

    public void setTipValue(double tipValue) {
        this.tipValue = tipValue;
    }


    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

}
