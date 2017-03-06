package com.rabbitstewdio.pentaho.diagnostics;

import org.pentaho.platform.api.repository2.unified.RepositoryFile;

import java.io.IOException;

public interface IDiagnosticLoaderBackend {
  DiagnosticTestInfo load(RepositoryFile file) throws IOException;
}
