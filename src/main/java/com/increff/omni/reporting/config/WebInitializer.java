//package com.increff.omni.reporting.config;
//
//import com.nextscm.commons.spring.server.AbstractWebInitializer;
//import org.springframework.web.WebApplicationInitializer;
//
///**
// * This class is a hook for <b>Servlet 3.0</b> specification, to initialize
// * Spring configuration without any <code>web.xml</code> configuration. Note
// * that {@link #getServletConfigClasses} method returns {@link SpringConfig},
// * which is the starting point for Spring configuration <br>
// * <b>Note:</b> You can also implement the {@link WebApplicationInitializer }
// * interface for more control
// */
//
//public class WebInitializer extends AbstractWebInitializer {
//
//	@Override
//	protected Class<?>[] getServletConfigClasses() {
//		return new Class[] { SpringConfig.class };
//	}
//}