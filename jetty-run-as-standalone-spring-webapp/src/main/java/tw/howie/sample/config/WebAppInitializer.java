/**
 * 
 */
package tw.howie.sample.config;

import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

/**
 * 同等於web.xml 的存在
 * 
 * @author howie_yu
 * 
 */
public class WebAppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer implements ServletContextListener {

	final Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * See {@link org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer}.
	 */
	@Override
	protected Class<?>[] getRootConfigClasses() {
		return null;
	}

	/**
	 * Set the application context for the Spring MVC web tier.
	 * 
	 * @See {@link org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer}
	 */
	@Override
	protected Class<?>[] getServletConfigClasses() {
		return new Class<?>[]{MvcConfiguration.class};
	}

	/**
	 * Map the Spring MVC servlet as the root.
	 * 
	 * @See {@link org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer}
	 */
	@Override
	protected String[] getServletMappings() {
		return new String[]{"/"};
	}

	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		try {
			onStartup(servletContextEvent.getServletContext());
		}
		catch(ServletException e) {
			logger.error("Failed to initialize web application", e);
			System.exit(0);
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent) {
	}

	/**
	 * Overrided to squelch a meaningless log message when embedded.
	 */
	@Override
	protected void registerContextLoaderListener(ServletContext servletContext) {
		servletContext.addFilter("HiddenHttpMethodFilter", org.springframework.web.filter.HiddenHttpMethodFilter.class);
	}

	private void configureEncodingFilter(ServletContext servletContext) {

		CharacterEncodingFilter encodingFilter = new CharacterEncodingFilter();
		encodingFilter.setEncoding("UTF-8");
		encodingFilter.setForceEncoding(false);
		FilterRegistration.Dynamic encodingFilterDinamic = servletContext.addFilter("charEncodingFilter",
																					encodingFilter);
		encodingFilterDinamic.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");

	}

	/**
	 * Register Spring Security
	 * 
	 * @param servletContext
	 */
	private void configureSpringSecurityFilter(ServletContext servletContext) {

		DelegatingFilterProxy delegatingFilterProxy = new DelegatingFilterProxy("springSecurityFilterChain");

		FilterRegistration.Dynamic securityFilterDynamic = servletContext.addFilter("securityFilter",
																					delegatingFilterProxy);
		securityFilterDynamic.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");
	}

	/**
	 * Register JerseyServlet Config
	 * 
	 * @param servletContext
	 */
	private void configurationJersey(ServletContext servletContext) {

		FilterRegistration.Dynamic filterDinamic = servletContext.addFilter("jersey",
																			new org.glassfish.jersey.servlet.ServletContainer());
		filterDinamic.setInitParameter("javax.ws.rs.Application", "tw.howie.sample.config.JerseyApplicationConfig");
		filterDinamic.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/api/*");
	}

	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {

		logger.info("On Startup WebAppInitializer.....");

		/* Let super do its thing... , include add RootConfig and ServletConfig */
		super.onStartup(servletContext);

		configureEncodingFilter(servletContext);

		configureSpringSecurityFilter(servletContext);

		configurationJersey(servletContext);
	}

}
