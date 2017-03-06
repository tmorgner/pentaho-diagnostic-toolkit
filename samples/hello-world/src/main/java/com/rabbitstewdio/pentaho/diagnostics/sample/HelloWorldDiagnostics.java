package com.rabbitstewdio.pentaho.diagnostics.sample;

import com.rabbitstewdio.pentaho.diagnostics.IDiagnosticTest;
import org.pentaho.platform.util.VersionHelper;

import java.io.PrintWriter;

public class HelloWorldDiagnostics implements IDiagnosticTest {
  @Override
  public void run(PrintWriter writer) {
    writer.println("Hello World Sample");
    writer.println();
    writer.println("Running on " + VersionHelper.getVersionInfo());
  }
}
