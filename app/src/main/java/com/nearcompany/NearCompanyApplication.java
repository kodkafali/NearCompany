package com.nearcompany;

import com.nearcompany.model.Company;
import com.nearcompany.model.User;

import java.util.List;

/**
 * Created by Emre on 4/19/2020.
 */

public class NearCompanyApplication {

    private static final NearCompanyApplication instance = new NearCompanyApplication();

    private User mUser;

    private List<Company> mCompanies;

    private NearCompanyApplication() {
    }

    public static NearCompanyApplication getInstance() {
        return NearCompanyApplication.instance;
    }

    public User getUser() {
        return mUser;
    }

    public void setUser(User user) {
        this.mUser = user;
    }

    public List<Company> getCompanies() {
        return mCompanies;
    }

    public void setCompanies(List<Company> pCompanies) {
        this.mCompanies = pCompanies;
    }
}
