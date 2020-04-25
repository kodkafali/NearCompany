package com.nearcompany.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Emre on 4/19/2020.
 */

public class User {

    @SerializedName("id")
    @Expose
    public String id;

    @SerializedName("name")
    @Expose
    public String name;

    @SerializedName("surname")
    @Expose
    public String surname;

    @SerializedName("email")
    @Expose
    public String email;

    @SerializedName("token")
    @Expose
    public String token;

    @SerializedName("usrImage")
    @Expose
    public String usrImage;
}
