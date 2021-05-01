package com.skylarksit.module.libs.alertdialog;

import android.content.Context;
import android.view.View;
import android.widget.EditText;

import com.skylarksit.module.R;
import com.skylarksit.module.pojos.Address;
import com.skylarksit.module.ui.utils.IClickListener;
import com.skylarksit.module.ui.utils.OnOneOffClickListener;

public class AddressDetailsPopup<Y> extends NokAlertDialogBuilder<AddressDetailsPopup> {

    private final View confirmButton;

    private final EditText buildingField;
    private final EditText localityField;
    private final EditText unitField;
    private final EditText notesField;

    {
        buildingField = findView(R.id.buildingField);
        localityField = findView(R.id.localityField);
        unitField = findView(R.id.unitField);
        notesField = findView(R.id.notesField);
        confirmButton = findView(R.id.confirmButton);
        View cancelButton = findView(R.id.cancelButton);
        cancelButton.setOnClickListener(new OnOneOffClickListener() {
            @Override
            public void onSingleClick(View v) {
                hide();
            }
        });
    }


    private Address address;

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
        if (address.getLocality()!=null)
            localityField.setText(address.getLocality());
        if (address.getBuilding()!=null)
            buildingField.setText(address.getBuilding());
    }


    public AddressDetailsPopup(Context context) {
        super(context);
    }

    public AddressDetailsPopup setConfirmClickListener(final IClickListener<Address> iClickListener){
        if (iClickListener!=null)
        {
            confirmButton.setOnClickListener(new OnOneOffClickListener() {
                @Override
                public void onSingleClick(View v) {
                    address.setLocality(localityField.getText().toString());
                    address.setBuilding(buildingField.getText().toString());
                    address.setUnit(unitField.getText().toString());
                    address.setNotes(notesField.getText().toString());
                    iClickListener.click(address);
                }
            });
        }

        return this;
    }

    @Override
    protected int getLayout() {
        return R.layout.address_details_popup;
    }


}
