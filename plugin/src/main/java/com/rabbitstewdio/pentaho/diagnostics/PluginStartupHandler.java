package com.rabbitstewdio.pentaho.diagnostics;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPluginLifecycleListener;
import org.pentaho.platform.api.engine.PluginLifecycleException;
import org.pentaho.platform.api.mimetype.IMimeType;
import org.pentaho.platform.api.mimetype.IPlatformMimeResolver;
import org.pentaho.platform.api.repository2.unified.IRepositoryContentConverterHandler;
import org.pentaho.platform.api.repository2.unified.IRepositoryDefaultAclHandler;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.core.mimetype.MimeType;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.importer.IPlatformImporter;
import org.pentaho.platform.plugin.services.importer.RepositoryFileImportFileHandler;
import org.pentaho.platform.plugin.services.importer.SolutionImportHandler;
import org.pentaho.platform.plugin.services.importexport.StreamConverter;

import java.util.Arrays;
import java.util.List;

public class PluginStartupHandler implements IPluginLifecycleListener {
  private static final Log logger = LogFactory.getLog(PluginStartupHandler.class);

  public PluginStartupHandler() {
    logger.info("Init called");
    System.out.println("Init called! SysOut");
  }

  @Override
  public void init() throws PluginLifecycleException {
    logger.info("Init called");
    System.out.println("Init called! SysOut");
  }

  @Override
  public void loaded() throws PluginLifecycleException {
    logger.info("Registering exporter");

    final StreamConverter sc = PentahoSystem.get(StreamConverter.class, "streamConverter", null);
    if (sc == null) {
      throw new IllegalStateException();
    }
    final IRepositoryContentConverterHandler handler = PentahoSystem.get(IRepositoryContentConverterHandler.class);
    if (handler == null) {
      throw new IllegalStateException();
    }
    handler.addConverter("jar", sc);

    logger.info("Registering importer");
    final RepositoryFileImportFileHandler importFileHandler = safelyBuildImportHandler(sc);

    final IPlatformImporter importer = PentahoSystem.get(IPlatformImporter.class);
    importer.addHandler(importFileHandler);

    logger.info("Diagnostic Plugin loaded");

    IPlatformMimeResolver mr = PentahoSystem.get(IPlatformMimeResolver.class);
    logger.info("MimeType registered: " + mr.resolveMimeForFileName("upload.jar"));
    logger.info("Stream Converter active");
  }

  private RepositoryFileImportFileHandler safelyBuildImportHandler(StreamConverter sc) {
    final RepositoryFileImportFileHandler importFileHandler = new RepositoryFileImportFileHandler(getMimeTypes(sc));
    importFileHandler.setRepository(PentahoSystem.get(IUnifiedRepository.class, "unifiedRepository", null));
    final IRepositoryDefaultAclHandler defaultAclHandler =
        PentahoSystem.get(IRepositoryDefaultAclHandler.class, "defaultAclHandler", null);
    importFileHandler.setDefaultAclHandler(defaultAclHandler);

    if (importFileHandler.getRepository() == null) {
      throw new IllegalStateException("No repository");
    }
    if (defaultAclHandler == null) {
      throw new IllegalStateException("No ACL handler");
    }
    return importFileHandler;
  }

  private List<IMimeType> getMimeTypes(StreamConverter sc) {

    final MimeType mimeType = new MimeType();
    mimeType.setExtensions(Arrays.asList("jar"));
    mimeType.setHidden(false);
    mimeType.setLocale(false);
    mimeType.setName("x-application/diagnostic-jar");
    mimeType.setConverter(sc);
    return Arrays.asList(mimeType);
  }


  @Override
  public void unLoaded() throws PluginLifecycleException {

  }
}
