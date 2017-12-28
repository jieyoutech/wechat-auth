package com.alvin.wechat.authtool.wechat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alvin.wechat.authtool.util.JsonDB;
import com.qq.weixin.mp.aes.AesException;
import com.qq.weixin.mp.aes.WXBizMsgCrypt;

public class EncryptUtil {
	private static final Logger logger = LogManager.getLogger(EncryptUtil.class);

	public static String decryptWeChatMessage(String encryptedText) throws AesException {
		WXBizMsgCrypt wechatDecrypter = null;
		try {
			wechatDecrypter = getWeChatDecrypter();
			if (wechatDecrypter == null)
				return null;
			else
				return wechatDecrypter.decrypt(encryptedText);
		} catch (AesException e) {
			logger.error("Cannot decrypt WeChat message, message={}, error={}", encryptedText, e.getMessage());
		}
		return null;
	}

	public static String encryptWeChatMessage(String replyMsg, String timeStamp, String nonce) throws AesException {
		WXBizMsgCrypt wechatDecrypter = null;
		wechatDecrypter = getWeChatDecrypter();
		if (wechatDecrypter == null) {
			return null;
		} else {
			return wechatDecrypter.encryptMsg(replyMsg, timeStamp, nonce);
		}
	}

	private static WXBizMsgCrypt getWeChatDecrypter() {
		WXBizMsgCrypt wechatDecrypter = null;
		try {
			wechatDecrypter = new WXBizMsgCrypt(JsonDB.get("component_access_token"), JsonDB.get("aes_key"),
					JsonDB.get("component_app_id"));
		} catch (AesException e) {
			logger.error("Cannot initalize WeChat decrypter, error={}", e.getMessage());
			return null;
		}
		return wechatDecrypter;
	}
}
