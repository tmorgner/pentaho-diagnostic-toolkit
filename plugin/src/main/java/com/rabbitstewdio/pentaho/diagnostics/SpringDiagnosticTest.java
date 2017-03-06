package com.rabbitstewdio.pentaho.diagnostics;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.UrlResource;

import java.io.PrintWriter;
import java.net.URL;

public abstract class SpringDiagnosticTest implements IDiagnosticTest {
  private static final Log logger = LogFactory.getLog(SpringDiagnosticTest.class);
  private GenericApplicationContext beanFactory;

  public SpringDiagnosticTest() {
  }

  @Override
  public final void run(PrintWriter writer) {
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    beanFactory = getNativeBeanFactory(cl);
    if (beanFactory == null) {
      throw new IllegalStateException
          ("A spring-based diagnostic test requires a diagnostics.spring.xml file in the classpath");
    }
    try {
      beanFactory.start();
      runWithSpring(writer);
    }
    finally {
      beanFactory.stop();
    }
  }

  public BeanFactory getBeanFactory() {
    return beanFactory;
  }

  /**
   * The native bean factory is the bean factory that has had all of its bean definitions loaded natively. In other
   * words, the plugin manager will not add any further bean definitions (i.e. from a plugin.xml file) into this
   * factory. This factory represents the one responsible for holding bean definitions for plugin.spring.xml or, if in a
   * unit test environment, the unit test pre-loaded bean factory.
   *
   * @return a bean factory will preconfigured bean definitions or <code>null</code> if no bean definition source is
   * available
   */
  protected GenericApplicationContext getNativeBeanFactory(final ClassLoader loader) {
    final URL resource = loader.getResource("diagnostics.spring.xml");
    if ( resource != null ) {
      logger.debug( "Found plugin spring file in classpath @ " + resource ); //$NON-NLS-1$
      UrlResource fsr = new UrlResource(resource);
      GenericApplicationContext appCtx = new GenericApplicationContext() {

        @Override
        protected void prepareBeanFactory( ConfigurableListableBeanFactory clBeanFactory ) {
          super.prepareBeanFactory( clBeanFactory );
          clBeanFactory.setBeanClassLoader( loader );
        }

        @Override
        public ClassLoader getClassLoader() {
          return loader;
        }

      };

      XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader( appCtx );
      xmlReader.setBeanClassLoader( loader );
      xmlReader.loadBeanDefinitions( fsr );

      return appCtx;
    }
    return null;
  }

  public void runWithSpring(PrintWriter writer) {
    final IDiagnosticTest bean = beanFactory.getBean(IDiagnosticTest.class);
    if (bean != null) {
      throw new IllegalStateException();
    }
    bean.run(writer);
  }
}
