package com.alvin.wechat.authtool;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alvin.wechat.authtool.util.JsonDB;
import com.alvin.wechat.authtool.util.XmlData;
import com.alvin.wechat.authtool.wechat.EncryptUtil;
import com.alvin.wechat.authtool.wechat.WeChatClient;
import com.qq.weixin.mp.aes.XMLParse;
import com.sun.jersey.api.view.Viewable;

@Path("/")
public class WeChatController {
	private static final Logger logger = LogManager.getLogger(WeChatController.class);
	private static WeChatClient client = new WeChatClient();

	//Http Get
	@GET
	@Path("/")
	public String hello() {
		return "hello";
	}

	//JSP Get
	@GET
	@Path("/redirect_page")
	public Viewable redirectPage(@Context HttpServletRequest request, @Context ServletContext servletContext) throws URISyntaxException, MalformedURLException {
		String calbackURL = request.getScheme() + "://" + request.getServerName() + "oardc/wechat/auth_callback";
		String code = client.getPreAuthCode();
		String param = "?component_appid=" + JsonDB.get("component_app_id") + "&pre_auth_code=" + code
				+ "&redirect_uri=" + calbackURL;
		request.setAttribute("param", param);
		return new Viewable("/jsp/redirect.jsp");
	}
	
	//Display auth result
	@GET
	@Path("auth_callback")
	public String redirectPage(@QueryParam("auth_code") String authCode) throws URISyntaxException, MalformedURLException {
		return client.getAuthAccessToken(authCode);
	}

	//Rest Post
	@POST
	@Path("/authorize/{componentAppId}/callback/trigger")
	public String authorize(@PathParam("componentAppId") String componentAppId, String content) {
		System.out.println("SYS: got auth request");
		logger.info("Logger: got auth request");

		try {
			String[] extracted = XMLParse.extract(content);
			String decryptedXml = EncryptUtil.decryptWeChatMessage(extracted[1]);
			XmlData xml = XmlData.parseXML(decryptedXml);
			String ticket = xml.getStringByTag("ComponentVerifyTicket");
			String accessToken = client.refreshComponentAccessToken(JsonDB.get("component_app_id"),
					JsonDB.get("component_app_secret"), ticket);
			if (StringUtils.isNotBlank(accessToken)) {
				JsonDB.put("component_access_token", accessToken);
			} else {
				return "failure";
			}
		} catch (Exception e) {
			logger.error("authorize got exception, error = {}", e.getMessage());
			return "failure";
		}

		return "success";
	}

}
