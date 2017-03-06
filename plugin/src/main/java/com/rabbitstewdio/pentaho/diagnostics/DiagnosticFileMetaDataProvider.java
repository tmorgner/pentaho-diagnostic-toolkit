package com.rabbitstewdio.pentaho.diagnostics;

import org.pentaho.platform.api.engine.IFileInfo;
import org.pentaho.platform.api.engine.ISolutionFile;
import org.pentaho.platform.api.engine.SolutionFileMetaAdapter;
import org.pentaho.platform.engine.core.solution.FileInfo;

import java.io.InputStream;

public class DiagnosticFileMetaDataProvider extends SolutionFileMetaAdapter {
  @Override
  public IFileInfo getFileInfo(ISolutionFile iSolutionFile, InputStream inputStream) {
    return new FileInfo();
  }
}
