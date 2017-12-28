package com.qq.weixin.mp.aes;

@SuppressWarnings("serial")
public class AesException extends Exception {

	public final static int OK = 0;
	public final static int ValidateSignatureError = -40001;
	public final static int ParseXmlError = -40002;
	public final static int ComputeSignatureError = -40003;
	public final static int IllegalAesKey = -40004;
	public final static int ValidateAppidError = -40005;
	public final static int EncryptAESError = -40006;
	public final static int DecryptAESError = -40007;
	public final static int IllegalBuffer = -40008;
	//public final static int EncodeBase64Error = -40009;
	//public final static int DecodeBase64Error = -40010;
	//public final static int GenReturnXmlError = -40011;

	private int code;

	private static String getMessage(int code) {
		switch (code) {
		case ValidateSignatureError:
			return "Signature Error";
		case ParseXmlError:
			return "xml parse error";
		case ComputeSignatureError:
			return "sha signature error";
		case IllegalAesKey:
			return "SymmetricKey illegal";
		case ValidateAppidError:
			return "appid incorrect";
		case EncryptAESError:
			return "aes encrypt error";
		case DecryptAESError:
			return "aes decrypt error";
		case IllegalBuffer:
			return "illegal buffer";
		default:
			return null; // cannot be
		}
	}

	public int getCode() {
		return code;
	}

	AesException(int code) {
		super(getMessage(code));
		this.code = code;
	}

}
