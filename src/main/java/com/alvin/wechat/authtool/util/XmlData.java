package com.alvin.wechat.authtool.util;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;

public class XmlData {

	private final Element root;

	public XmlData(Element root) {
		this.root = root;
	}

	public String getStringByTag(String tag) {
		try {
			return root.getElementsByTagName(tag).item(0).getTextContent();
		} catch (Exception e) {
			return null;
		}
	}

	public static XmlData parseXML(String xml) {
		return parseXML(new StringReader(xml));
	}

	public static XmlData parseXML(HttpServletRequest req) {
		try {
			return parseXML(new InputStreamReader(req.getInputStream(), "UTF-8"));
		} catch (IOException e) {
		}
		return null;
	}

	public static XmlData parseXML(Reader input) {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db;
			db = dbf.newDocumentBuilder();
			InputSource is = new InputSource(input);
			Document document = db.parse(is);

			return new XmlData(document.getDocumentElement());
		} catch (Exception e) {
		}

		return null;
	}

	@Override
	public String toString() {
		Document document = root.getOwnerDocument();
		DOMImplementationLS domImplLS = (DOMImplementationLS) document.getImplementation();
		LSSerializer serializer = domImplLS.createLSSerializer();
		return serializer.writeToString(root);
	}
}
