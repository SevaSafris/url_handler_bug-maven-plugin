package org.safris.demo.classworlds;

import java.net.URL;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name="demo", defaultPhase=LifecyclePhase.COMPILE)
@Execute(goal="demo")
public class DemoMojo extends AbstractMojo {
  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    final URL memUrl = MemoryURLStreamHandler.createURL("hello".getBytes());
  }
}