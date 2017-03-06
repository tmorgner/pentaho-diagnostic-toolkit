package com.rabbitstewdio.pentaho.diagnostics.sample.spring;

import com.rabbitstewdio.pentaho.diagnostics.IDiagnosticTest;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;

import java.io.PrintWriter;

public class HelloSpringTest implements IDiagnosticTest {

  private IUnifiedRepository repository;

  public HelloSpringTest(IUnifiedRepository repository) {
    this.repository = repository;
  }

  @Override
  public void run(PrintWriter writer) {
    writer.println("Hello World with Spring");
    writer.println();
    writer.println("SolutionRepository injected: " + repository);
  }
}
