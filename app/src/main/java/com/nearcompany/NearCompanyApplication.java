package com.nearcompany;

/**
 * Created by Emre on 4/19/2020.
 */

public class NearCompanyApplication {

    private static final NearCompanyApplication instance = new NearCompanyApplication();

    private NearCompanyApplication() {
    }

    public static NearCompanyApplication getInstance() {
        return NearCompanyApplication.instance;
    }
}
