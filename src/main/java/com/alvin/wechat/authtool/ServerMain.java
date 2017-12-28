package com.alvin.wechat.authtool;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;

import com.alvin.wechat.authtool.util.JsonDB;
import com.sun.jersey.spi.container.servlet.ServletContainer;

public class ServerMain {

	public static void main(String[] args) throws Exception {
		JsonDB.initDB("wechat_authtool_db.json");

		int port = 8080;
		Server server = new Server(port);
		WebAppContext ctx = new WebAppContext();
		
		//JSP lookup base
		ctx.setResourceBase("src/main/webapp");
		
		//URL lookup base
		ctx.setContextPath("/");
		
		//Support compile JSP
		ctx.setAttribute("org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern", ".*/[^/]*jstl.*\\.jar$");
		
		//Support compile JSP
		org.eclipse.jetty.webapp.Configuration.ClassList classlist = org.eclipse.jetty.webapp.Configuration.ClassList.setServerDefault(server);
		classlist.addAfter("org.eclipse.jetty.webapp.FragmentConfiguration", "org.eclipse.jetty.plus.webapp.EnvConfiguration", "org.eclipse.jetty.plus.webapp.PlusConfiguration");
		classlist.addBefore("org.eclipse.jetty.webapp.JettyWebXmlConfiguration", "org.eclipse.jetty.annotations.AnnotationConfiguration");
		
		server.setHandler(ctx);
		
		//Support Jersey
		ServletHolder sh = new ServletHolder(ServletContainer.class);
		sh.setInitParameter("com.sun.jersey.config.property.resourceConfigClass",
				"com.sun.jersey.api.core.PackagesResourceConfig");

		// Define Jersey Servlet Controller
		sh.setInitParameter("com.sun.jersey.config.property.packages", "com.alvin.wechat.authtool");
		ctx.addServlet(sh, "/oardc/wechat/*");
		//Support Jersey end
				
		server.start();
	}

}
