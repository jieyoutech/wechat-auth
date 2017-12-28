package com.alvin.wechat.authtool.util;

import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;

public class MiscUtil {
	private final static Logger logger = LogManager.getLogger(MiscUtil.class);
	private static final char RANDOM_STR_BASE[] = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
			.toCharArray();
	private static final ObjectMapper mapper = new ObjectMapper()
			.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);

	static {
		mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	public static String getRandomString(Integer length) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < length; i++) {
			// Don't need use SecureRandom. We just use its based function.
			sb.append(RANDOM_STR_BASE[(int) Math.round(Math.random() * 61)]);
		}
		return sb.toString();
	}

	public static boolean isBlank(String s) {
		return s == null || s.length() == 0;
	}

	public static boolean isNotBlank(String s) {
		return s != null && s.length() > 0;
	}

	public static byte[] inputStreamToByte(InputStream inStream) {
		ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
		byte[] buff = new byte[100];
		int rc = 0;
		byte[] byteData = null;
		try {
			while ((rc = inStream.read(buff, 0, 100)) > 0) {
				swapStream.write(buff, 0, rc);
			}
			byteData = swapStream.toByteArray();
		} catch (IOException e) {
			logger.error("InputStreamToByte got error:{}", e.getMessage());
		}
		return byteData;
	}

	public static byte[] getImageFromURL(String url, RequestConfig config) throws ClientProtocolException, IOException {
		HttpGet httpget = new HttpGet(url);
		if (config != null) {
			httpget.setConfig(config);
		}
		CloseableHttpClient httpClient = null;
		CloseableHttpResponse httpResponse = null;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			httpClient = HttpClients.createDefault();
			httpResponse = httpClient.execute(httpget);
			httpResponse.getEntity().writeTo(bos);
			return bos.toByteArray();
		} finally {
			if (null != httpClient) {
				httpClient.close();
			}
			if (null != httpResponse) {
				httpResponse.close();
			}
			bos.close();
		}
	}

	public static String getImageFormat(byte[] bytes) {
		try {
			final ByteArrayInputStream input = new ByteArrayInputStream(bytes);
			final ImageInputStream imgInput = ImageIO.createImageInputStream(input);
			final Iterator<ImageReader> it = ImageIO.getImageReaders(imgInput);
			return it.next().getFormatName().toLowerCase();
		} catch (Exception e) {
			logger.error("MiscUtil getImageFormat got Exception: ", e);
			return "jpg";
		}
	}

	public static String getImageName(String url, byte[] data) {
		if (url == null) {
			String ext = getImageFormat(data);
			return "noname." + ext;
		} else {
			String fileName = url.substring(url.lastIndexOf("/") + 1, url.length());

			fileName = fileName.toLowerCase();
			if (fileName.length() < 5 || !(fileName.endsWith(".jpg") || fileName.endsWith(".bmp")
					|| fileName.endsWith(".png") || fileName.endsWith(".gif") || fileName.endsWith(".jpeg"))) {
				String ext = getImageFormat(data);
				fileName = "noname." + ext;
			}
			return fileName;
		}

	}

	public static BufferedImage resizeImage(BufferedImage source, double ratio) {
		int w = (int) (source.getWidth() * ratio);
		int h = (int) (source.getHeight() * ratio);
		BufferedImage bi = getCompatibleImage(w, h);
		Graphics2D g2d = bi.createGraphics();
		double xScale = (double) w / source.getWidth();
		double yScale = (double) h / source.getHeight();
		AffineTransform at = AffineTransform.getScaleInstance(xScale, yScale);
		g2d.drawRenderedImage(source, at);
		g2d.dispose();
		return bi;
	}

	public static BufferedImage resizeImage(BufferedImage source, int w, int h) {
		BufferedImage bi = getCompatibleImage(w, h);
		Graphics2D g2d = bi.createGraphics();
		// double xScale = (double) w / source.getWidth();
		double yScale = (double) h / source.getHeight();
		double xScale = yScale;
		AffineTransform at = AffineTransform.getScaleInstance(xScale, yScale);
		g2d.drawRenderedImage(source, at);
		g2d.dispose();
		return bi;
	}

	public static BufferedImage getCompatibleImage(int w, int h) {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice gd = ge.getDefaultScreenDevice();
		GraphicsConfiguration gc = gd.getDefaultConfiguration();
		BufferedImage image = gc.createCompatibleImage(w, h);
		return image;
	}

	public static String getParameterValueFromURL(String key, String URL) {

		Matcher keyMatcher = Pattern.compile(key + "=([^&]+)").matcher(URL);
		if (keyMatcher.find()) {
			return keyMatcher.group(1);
		} else {
			return null;
		}

	}

	public static boolean isEquals(Object obj1, Object obj2) {
		return obj1 == obj2 || (obj1 != null && obj1.equals(obj2));
	}

	/**
	 * 
	 * @param url
	 * @param parameter
	 *            !!!This parameter must not have first '&'
	 * @return
	 */
	public static String appendParameter(String url, String parameter) {
		int index = url.indexOf('&');
		if (index > 0) {
			url = url + "&" + parameter;
		} else {
			url = url + "?" + parameter;
		}
		return url;
	}

	public static String getHostFromURL(String url) {
		if (StringUtils.isBlank(url)) {
			return null;
		}
		try {
			URI uri = new URI(url);
			return uri.getHost();
		} catch (URISyntaxException e) {
			// eat it
		}
		return null;
	}

	public static String getPathFromURL(String url) {
		if (StringUtils.isBlank(url)) {
			return null;
		}
		try {
			URI uri = new URI(url);
			return uri.getPath();
		} catch (URISyntaxException e) {
			// eat it
		}
		return null;
	}

}
