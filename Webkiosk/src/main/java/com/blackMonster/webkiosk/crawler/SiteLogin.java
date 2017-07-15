package com.blackMonster.webkiosk.crawler;

import android.content.Context;

import com.blackMonster.webkiosk.utils.M;
import com.blackMonster.webkiosk.utils.NetworkUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.impl.client.CloseableHttpClient;
import cz.msebera.android.httpclient.impl.client.HttpClientBuilder;
import cz.msebera.android.httpclient.impl.client.LaxRedirectStrategy;
import cz.msebera.android.httpclient.protocol.HTTP;

/**
 * Manages logging in to Webkiosk website.
 */
class SiteLogin {
	private static final String TAG = "SiteLogin";

	private CloseableHttpClient httpclient=null;


	HttpClient getConnection() {
		return httpclient;
	}

	int login(String colg,String enroll, String pass, Context context)  {

		if (!NetworkUtils.isInternetAvailable(context)) return LoginStatus.CONN_ERROR;

		ArrayList<NameValuePair> formparams = new ArrayList<NameValuePair>();
		WebkioskWebsite.initiliseLoginDetails(formparams, colg, enroll, pass);

        httpclient = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy()).build();
		HttpPost httppost = new HttpPost(WebkioskWebsite.getLoginUrl(colg));
		BufferedReader reader=null;
		Integer status = null;
		try {
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams,
					HTTP.UTF_8);
			httppost.setEntity(entity);
			HttpResponse response = httpclient.execute(httppost);
				reader = new BufferedReader(new InputStreamReader(
						response.getEntity().getContent()));
				status = responseStatus(reader);
		} catch (IOException e) {
			status = LoginStatus.UNKNOWN_ERROR;
			httppost.abort();
			e.printStackTrace();
		} 
		
		if (status != LoginStatus.LOGIN_DONE) {
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
		
		if (reader!= null)
			try {
				reader.close();
			} catch (IOException e) {
				status = LoginStatus.UNKNOWN_ERROR;
				e.printStackTrace();
			}
		
		return status;
		
	}
	
	private int responseStatus(BufferedReader reader) throws IOException {
		String tmp;
		
		while (true) {
			tmp = reader.readLine();
			if (tmp == null)
				return LoginStatus.UNKNOWN_ERROR;
			M.log(TAG, tmp);
			if (tmp.contains("PersonalFiles/ShowAlertMessageSTUD.jsp") || tmp.contains("StudentPageFinal.jsp"))
				return LoginStatus.LOGIN_DONE;
			if (tmp.contains("Invalid Password"))
				return LoginStatus.INVALID_PASS;
			if (tmp.contains("Login Account Locked"))
				return LoginStatus.ACCOUNT_LOCKED;
			if (tmp.contains("Wrong Member Type or Code") || tmp.toLowerCase().contains("correct institute name and enrollment"))
				return LoginStatus.INVALID_ENROLL;
				
		}
	}


	void close() throws IOException {
		if (httpclient !=null)
		httpclient.close();

	}



}
