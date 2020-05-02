package com.nearcompany.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Emre on 4/29/2020.
 */

public class Company {

    @SerializedName("id")
    @Expose
    public Integer id;

    @SerializedName("companyClass")
    @Expose
    public String companyClass;

    @SerializedName("companyName")
    @Expose
    public String companyName;

    @SerializedName("scope")
    @Expose
    public String scope;

    @SerializedName("stafCount")
    @Expose
    public Integer stafCount;

    @SerializedName("description")
    @Expose
    public String description;

    @SerializedName("logo")
    @Expose
    public String logo;

    @SerializedName("webLink")
    @Expose
    public String webLink;

    @SerializedName("foundationYear")
    @Expose
    public String foundationYear;

    @SerializedName("phone")
    @Expose
    public String phone;

    @SerializedName("email")
    @Expose
    public String email;

    @SerializedName("projects")
    @Expose
    public List<Project> projects;
}
