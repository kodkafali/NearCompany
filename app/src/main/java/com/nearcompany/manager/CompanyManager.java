package com.nearcompany.manager;

import android.content.Context;
import android.os.AsyncTask;

import com.nearcompany.db.DatabaseHelper;
import com.nearcompany.model.Company;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Created by Emre on 5/2/2020.
 */

public class CompanyManager {

    private String TEKNOPARK_ISTANBUL_URL = "https://www.teknoparkistanbul.com.tr/firmalar";
    private DatabaseHelper mDbHelper;

    public CompanyManager(String pURL, Context pContext) {

        if (pURL == null)
            pURL = TEKNOPARK_ISTANBUL_URL;

        mDbHelper = new DatabaseHelper(pContext);
        //List<Company> list = mDbHelper.getCompaniesQuery();
        //new AsyncCompanyBaseContent(pURL).execute();
    }

    private class AsyncCompanyBaseContent extends AsyncTask<Void, Void, Void> {

        private String mURL = "";

        private AsyncCompanyBaseContent(String pURL) {
            this.mURL = pURL;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Document doc = Jsoup.connect(mURL).ignoreContentType(true).get();

                String tmpLink = "";
                Integer companyId = 0;

                for (Element row : doc.select("div.firma-list")) {
                    for (Element row2 : row.select("div.item")) {
                        Company company = new Company();
                        company.id = companyId;
                        company.logo = row2.getElementsByTag("img").attr("src");
                        company.companyName = row2.getElementsByTag("span").text();
                        //String scope = row2.getElementsByTag("i").text(); çekme yöntemi doğru data yanlış..
                        tmpLink = row2.getElementsByTag("a").attr("href");

                        new AsyncCompanyDetailContent(company, tmpLink).execute();

                        companyId++;
                    }
                }
            } catch (Exception e) {
                e.getStackTrace();
            }
            return null;
        }
    }

    private class AsyncCompanyDetailContent extends AsyncTask<Void, Void, Void> {

        private Company mCompany;
        private String mTmpLink;

        private AsyncCompanyDetailContent(Company pCompany, String pTmpLink) {
            this.mCompany = pCompany;
            this.mTmpLink = pTmpLink;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Document doc = Jsoup.connect(mTmpLink).ignoreContentType(true).get();

                for (Element row : doc.select("div.firma-detail")) {
                    int size = row.getElementsByClass("item").size();
                    for (int i = 0; i < size; i++) {
                        Element item = row.getElementsByClass("item").get(i);
                        if (size == 6) {
                            switch (i) {
                                case 0: {
                                    break;
                                }
                                case 1: {
                                    this.mCompany.foundationYear = item.getElementsByTag("b").text();
                                    break;
                                }
                                case 2: {
                                    this.mCompany.stafCount = Integer.parseInt(item.getElementsByTag("b").text());
                                    if (this.mCompany.stafCount < 10) {
                                        this.mCompany.companyClass = "small";
                                    } else if (this.mCompany.stafCount >= 10 && this.mCompany.stafCount < 50) {
                                        this.mCompany.companyClass = "medium";
                                    } else {
                                        this.mCompany.companyClass = "large";
                                    }
                                    break;
                                }
                                case 3: {
                                    this.mCompany.phone = item.getElementsByTag("b").text();
                                    break;
                                }
                                case 4: {
                                    this.mCompany.webLink = item.getElementsByTag("a").attr("href");
                                    break;
                                }
                                case 5: {
                                    this.mCompany.email = item.getElementsByTag("a").attr("href");
                                    break;
                                }
                            }
                        } else {
                            this.mCompany.stafCount = 1;
                            this.mCompany.companyClass = "small";
                            switch (i) {
                                case 0: {
                                    break;
                                }
                                case 1: {
                                    this.mCompany.foundationYear = item.getElementsByTag("b").text();
                                    break;
                                }
                                case 2: {
                                    this.mCompany.phone = item.getElementsByTag("b").text();
                                    break;
                                }
                                case 3: {
                                    this.mCompany.webLink = item.getElementsByTag("a").attr("href").split(":")[1];
                                    break;
                                }
                                case 4: {
                                    this.mCompany.email = item.getElementsByTag("a").attr("href").split(":")[1];
                                    break;
                                }
                            }
                        }
                    }
                }

                this.mCompany.scope = doc.getElementsByClass("title").get(0).getElementsByTag("i").text();
                this.mCompany.description = doc.getElementsByClass("content").get(0).getElementsByTag("p").text();

                mDbHelper.setCompanyQuery(mCompany);

                this.cancel(true);
            } catch (Exception e) {
                e.getStackTrace();
            }

            return null;
        }
    }
}
