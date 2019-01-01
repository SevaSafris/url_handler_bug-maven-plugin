/* Copyright (c) 2018 OpenJAX
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * You should have received a copy of The MIT License (MIT) along with this
 * program. If not, see <http://opensource.org/licenses/MIT/>.
 */

package org.safris.demo.classworlds.memory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.spi.URLStreamHandlerProvider;

import org.safris.demo.classworlds.MemoryURLConnection;
import org.safris.demo.classworlds.MemoryURLStreamHandler;

/**
 * Handler class extending {@link MemoryURLStreamHandler}. This class is used
 * for handler registration with the {@code "java.protocol.handler.pkgs"} system
 * property.
 */
public class Handler extends MemoryURLStreamHandler {
  public static class Provider extends URLStreamHandlerProvider {
    @Override
    public URLStreamHandler createURLStreamHandler(final String protocol) {
      return "memory".equals(protocol) ? new Handler() : null;
    }
  }

  /**
   * {@inheritDoc}
   *
   * @throws MalformedURLException If the provided {@code URL} specifies a
   *           protocol that is not {@code "memory"}, or a host that is not
   *           {@code null} or empty.
   * @throws IOException If no data is registered for the provided {@code URL},
   *           or if an I/O error occurs while opening the connection.
   */
  @Override
  protected URLConnection openConnection(final URL url) throws IOException {
    if (!"memory".equals(url.getProtocol()))
      throw new MalformedURLException("Unsupported protocol: " + url.getProtocol());

    if (url.getHost() != null && url.getHost().length() > 0)
      throw new MalformedURLException("Unsupported host: " + url.getHost());

    final byte[] data = idToData.get(url.getPath());
    if (data == null)
      throw new IOException("URL not registered: " + url);

    return new MemoryURLConnection(url, data);
  }
}