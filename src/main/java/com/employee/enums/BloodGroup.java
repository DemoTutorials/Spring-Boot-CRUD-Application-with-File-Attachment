package com.employee.enums;

public enum BloodGroup {
    A_POSITIVE,A_NEGATIVE,
    AB_POSITIVE,AB_NEGATIVE,
    B_POSITIVE,B_NEGATIVE,
    O_POSITIVE,O_NEGATIVE;

    public static BloodGroup fromString(String key){

        if(key==null){
            return null;
        }
        try{
           return BloodGroup.valueOf(key);
        }
        catch(IllegalArgumentException e){
           return null;
        }
    }
}
