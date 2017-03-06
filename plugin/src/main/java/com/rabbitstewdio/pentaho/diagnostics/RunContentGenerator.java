package com.rabbitstewdio.pentaho.diagnostics;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.solution.SimpleContentGenerator;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class RunContentGenerator extends SimpleContentGenerator {
  private static final Log logger = LogFactory.getLog(RunContentGenerator.class);
  private IDiagnosticLoaderBackend classLoaderBackend;

  public RunContentGenerator() {
    this.classLoaderBackend = new DiagnosticLoaderBackend();
  }

  public void createContent(OutputStream outputStream) throws Exception {
    RepositoryFile file = resolveFile();
    if (file == null) {
      throw new IllegalStateException("FileNotFound");
    }
    final PrintWriter w = new PrintWriter(outputStream);
    try {
      DiagnosticTestInfo l = classLoaderBackend.load(file);
      if (l == null) {
        throw new IllegalStateException("No valid content");
      }
      l.run(w);
    }
    finally {
      w.flush();
    }
  }

  protected RepositoryFile resolveFile() throws UnsupportedEncodingException {
    final IUnifiedRepository unifiedRepository = PentahoSystem.get( IUnifiedRepository.class, null );
    final IParameterProvider pathParams = getPathParameters();
    final IParameterProvider requestParams = getRequestParameters();

    String path = null;
    if ( requestParams != null && requestParams.getStringParameter( "path", null ) != null ) {
      path = requestParams.getStringParameter( "path", "" ); //$NON-NLS-1$ //$NON-NLS-2$
    }
    else if ( pathParams != null && pathParams.getStringParameter( "path", null ) != null ) {
      path = pathParams.getStringParameter( "path", "" ); //$NON-NLS-1$ //$NON-NLS-2$
    }
    else {
      throw new IllegalStateException();
    }

    path = idTopath( URLDecoder.decode( path, "UTF-8" ) );

    return unifiedRepository.getFile( path );
  }

  public IParameterProvider getPathParameters() {
    if ( parameterProviders == null ) {
      return new SimpleParameterProvider();
    }

    return parameterProviders.get( "path" );
  }

  public IParameterProvider getRequestParameters() {
    if ( parameterProviders == null ) {
      return new SimpleParameterProvider();
    }

    return parameterProviders.get( IParameterProvider.SCOPE_REQUEST );
  }

  protected String idTopath( String path ) {
    if ( path != null && path.length() > 0 && path.charAt( 0 ) != '/' ) {
      path = "/" + path;
    }
    return path;
  }

  public String getMimeType() {
    return "text/plain";
  }

  public Log getLogger() {
    return logger;
  }
}
