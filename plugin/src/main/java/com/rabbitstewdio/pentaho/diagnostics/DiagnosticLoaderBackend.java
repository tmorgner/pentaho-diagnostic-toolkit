package com.rabbitstewdio.pentaho.diagnostics;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.reporting.libraries.base.util.IOUtils;
import org.pentaho.reporting.libraries.base.util.LFUMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

public class DiagnosticLoaderBackend implements IDiagnosticLoaderBackend {
  private static class Tuple {
    long date;
    DiagnosticTestInfo info;
  }

  private static final Log logger = LogFactory.getLog(DiagnosticLoaderBackend.class);
  private LFUMap<String, Tuple> cachedClassLoaders;

  public DiagnosticLoaderBackend() {
    this.cachedClassLoaders = new LFUMap<>(5);
  }

  @Override
  public synchronized DiagnosticTestInfo load(RepositoryFile file) throws IOException {
    String dirPath = file.getId().toString();
    logger.debug("Loading test info for " + dirPath + " - " + file.getName());

    long date = file.getLastModifiedDate().getTime();
    Tuple tuple = cachedClassLoaders.get(dirPath);
    if (tuple != null){
      if (tuple.date != date) {
        logger.debug("Entry dates do not match for " + dirPath);
        cachedClassLoaders.remove(dirPath);
      }
      else if (tuple.info != null ){
        logger.debug("Found cached info: " + dirPath);
        return tuple.info;
      }
    }

    tuple = new Tuple();
    tuple.date = date;
    tuple.info = loadTestData(dirPath, file);
    cachedClassLoaders.put(dirPath, tuple);
    return tuple.info;
  }

  private DiagnosticTestInfo loadTestData(String path, RepositoryFile file) throws IOException {
    final File basePath = new File(getSystemTmp(), path);
    basePath.mkdirs();
    if (! basePath.isDirectory()) {
      throw new IllegalStateException();
    }

    File jar = new File(basePath, "diagnostic.jar");
    Properties p = loadProperties(basePath);
    final String lastModified = p.getProperty("lastModified");
    final String ftime = String.valueOf(file.getLastModifiedDate().getTime());
    if (ftime.equals(lastModified)) {
      // maybe this is a valid but old cache ..
      if (jar.isFile() && jar.canRead()) {
        JarFile jarArchive = new JarFile(jar);
        final Object o = jarArchive.getManifest().getMainAttributes().getValue("Diagnostic-Main");
        if (o != null) {
          ClassLoader l = new URLClassLoader(new URL[] {jar.toURI().toURL()}, getClass().getClassLoader());
          logger.info("Found previously cached entry.");
          return new DiagnosticTestInfo(l, String.valueOf(o));
        }
      }
    }

    p.setProperty("lastModified", ftime);

    storeProperties(basePath, p);
    copyJar(file, jar);
    JarFile jarArchive = new JarFile(jar);
    logger.info("Processing jar " + jar.getAbsolutePath());
    final Attributes entries = jarArchive.getManifest().getMainAttributes();
    for (Map.Entry<Object, Object> e: entries.entrySet()) {
      logger.info(" -> " + e.getKey() + "=" + e.getValue());
    }
    final Object o = entries.getValue("Diagnostic-Main");
    if (o != null) {
      ClassLoader l = new URLClassLoader(new URL[] {jar.toURI().toURL()}, getClass().getClassLoader());
      return new DiagnosticTestInfo(l, String.valueOf(o));
    }

    throw new IllegalStateException("Unable to find Diagnostic-Main entry in manifest");
  }

  private void copyJar(RepositoryFile file, File jar) throws IOException {
    final IUnifiedRepository unifiedRepository = PentahoSystem.get( IUnifiedRepository.class, null );

    final FileOutputStream jarFileOut = new FileOutputStream(jar);

    final SimpleRepositoryFileData fileData =
        unifiedRepository.getDataForRead( file.getId(), SimpleRepositoryFileData.class );
    final InputStream is = fileData.getInputStream();
    IOUtils.getInstance().copyStreams(is, jarFileOut);
    jarFileOut.close();
  }

  private void storeProperties(File basePath, Properties p) {
    final File lastMod = new File(basePath, "metadata.properties");
    try {
      try (FileOutputStream fin = new FileOutputStream(lastMod)){
        p.store(fin, "Do not modify");
      }
    } catch (IOException e) {
      // ignored ..
    }
  }

  private Properties loadProperties(File basePath) {
    final File lastMod = new File(basePath, "metadata.properties");
    Properties p = new Properties();
    try {
      try (FileInputStream fin = new FileInputStream(lastMod)){
        p.load(fin);
      }
    } catch (IOException e) {
      // ignored ..
    }
    return p;
  }

  private String getSystemTmp() {
    String s = System.getProperty( "java.io.tmpdir" ); //$NON-NLS-1$
    final char c = s.charAt( s.length() - 1 );
    if ( ( c != '/' ) && ( c != '\\' ) ) {
      s += File.separator;
      System.setProperty( "java.io.tmpdir", s ); //$NON-NLS-1$//$NON-NLS-2$
    }
    return s;
  }

}
