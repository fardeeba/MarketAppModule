package com.skylarksit.module.lib;

import android.app.Activity;

import com.skylarksit.module.utils.Utilities;

import java.util.List;

public class ErrorResource {

    public String status;
    public String message;
    public String description;
    public String title;

    public List<FieldError> errors;
    public Object data;

    public void show(Activity activity) {
        Utilities.Error(title,description);
    }

    public static class FieldError{
        public String field;
        public String message;

        public FieldError(String field, String message){
            this.field = field;
            this.message = message;
        }
    }

}
