package com.alvin.wechat.authtool.wechat;

import java.io.IOException;

import javax.naming.AuthenticationException;

import org.apache.http.HttpException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.util.StringUtil;
import org.json.JSONException;
import org.json.JSONObject;

import com.alvin.wechat.authtool.util.HttpClientUtil;
import com.alvin.wechat.authtool.util.JsonDB;
import com.alvin.wechat.authtool.util.JsonData;

public class WeChatClient {
	private static final Logger logger = LogManager.getLogger(EncryptUtil.class);

	public String refreshComponentAccessToken(String clientId, String clientSecret, String verifyTicket)
			throws HttpException, IOException, AuthenticationException, JSONException {

		JSONObject parameters = new JSONObject();
		parameters.put("component_appid", clientId);
		parameters.put("component_appsecret", clientSecret);
		parameters.put("component_verify_ticket", verifyTicket);

		String response = HttpClientUtil.getInstance()
				.sendHttpPost("https://api.weixin.qq.com/cgi-bin/component/api_component_token", parameters.toString());
		JsonData jData = JsonData.parseJson(response);
		String aToken = jData.getString("component_access_token");
		if (StringUtil.isNotBlank(aToken)) {
			return aToken;
		} else {
			logger.error(response);
		}
		return null;
	}

	public String getPreAuthCode() {
		JSONObject parameters = new JSONObject();
		parameters.put("component_appid", (String) JsonDB.get("component_app_id"));

		String response = HttpClientUtil.getInstance().sendHttpPost(
				"https://api.weixin.qq.com/cgi-bin/component/api_create_preauthcode?component_access_token="
						+ JsonDB.get("component_access_token"),
				parameters.toString());
		JsonData jData = JsonData.parseJson(response);
		String code = jData.getString("pre_auth_code");
		if (StringUtil.isNotBlank(code)) {
			return code;
		} else {
			logger.error(response);
		}
		return null;
	}
	
	public String getAuthAccessToken(String authCode) {
		JSONObject parameters = new JSONObject();
		parameters.put("component_appid", (String) JsonDB.get("component_app_id"));
		parameters.put("authorization_code", authCode);

		String response = HttpClientUtil.getInstance().sendHttpPost(
				"https://api.weixin.qq.com/cgi-bin/component/api_query_auth?component_access_token="
						+ JsonDB.get("component_access_token"),
				parameters.toString());
		System.out.println("Auth info:");
		System.out.println(response);
		return response;
	}
}
