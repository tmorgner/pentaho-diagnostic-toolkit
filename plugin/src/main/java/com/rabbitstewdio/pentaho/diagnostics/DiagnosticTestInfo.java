package com.rabbitstewdio.pentaho.diagnostics;

import org.pentaho.reporting.libraries.base.util.ObjectUtilities;

import java.io.PrintWriter;

public class DiagnosticTestInfo {
  private ClassLoader classLoader;
  private String entryPoint;

  public DiagnosticTestInfo(ClassLoader classLoader, String entryPoint) {
    this.classLoader = classLoader;
    this.entryPoint = entryPoint;
  }

  public void run(PrintWriter w) {
    ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(classLoader);
    try {
      final Class<?> loadClass = classLoader.loadClass(entryPoint);
      final IDiagnosticTest diagnosticTest = ObjectUtilities.instantiateSafe(loadClass, IDiagnosticTest.class);
      diagnosticTest.run(w);
    } catch (ClassNotFoundException e) {
      w.println("Error while loading diagnostic test classes");
      e.printStackTrace(w);
    }
    finally {
      Thread.currentThread().setContextClassLoader(oldClassLoader);
    }
  }
}
