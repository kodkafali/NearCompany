package nearcompany.com.nearcompany.model;

import android.content.Intent;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Emre on 4/19/2020.
 */

public class User {

    @SerializedName("id")
    @Expose
    public Intent id;

    @SerializedName("name")
    @Expose
    public String name;

    @SerializedName("surname")
    @Expose
    public String surname;

}
