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

package org.safris.demo.classworlds;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLStreamHandler;
import java.util.HashMap;

/**
 * A {@link URLStreamHandler} that implements the "memory" protocol. This class
 * can be used to create {@code memory:/...} URLs that are resolvable in the JVM
 * in which they are created.
 */
public abstract class MemoryURLStreamHandler extends URLStreamHandler {
  private static final String PROPERTY = "java.protocol.handler.pkgs";

  static {
    final String pkgs = System.getProperty(PROPERTY);
    if (pkgs == null || !pkgs.contains("org.safris.demo.classworlds"))
      System.setProperty(PROPERTY, pkgs != null && pkgs.length() > 0 ? pkgs + "|" + "org.safris.demo.classworlds" : "org.safris.demo.classworlds");
  }

  protected static final HashMap<String,byte[]> idToData = new HashMap<>();

  /**
   * Creates a "memory" protocol {@link URL} for the specified {@code data}.
   *
   * @param data The data {@code byte} array.
   * @return A "memory" protocol {@link URL} for the specified {@code data}.
   */
  public static URL createURL(final byte[] data) {
    try {
      final String path = "/" + Integer.toHexString(System.identityHashCode(data));
      final URL url = new URL("memory", null, path);

      idToData.put(path, data);
      return url;
    }
    catch (final MalformedURLException e) {
      throw new UnsupportedOperationException(e);
    }
  }

  /**
   * Returns the data for the provided {@code URL}.
   * <p>
   * <i><b>Note</b>: This method only supports URLs with {@code "memory"}
   * protocol, and a {@code null} or empty host.</i>
   *
   * @param url The {@code URL}.
   * @return The data for the provided {@code URL}.
   * @throws IllegalArgumentException If the provided {@code URL} specifies a
   *           protocol that is not {@code "memory"}, or a host that is not
   *           {@code null} or empty.
   * @throws NullPointerException If {@code url} is null.
   */
  public static byte[] getData(final URL url) {
    if (!"memory".equals(url.getProtocol()))
      throw new IllegalArgumentException("Illegal protocol: " + url.getProtocol());

    if (url.getHost() != null && url.getHost().length() > 0)
      throw new IllegalArgumentException("Illegal host: " + url.getHost());

    return idToData.get(url.getPath());
  }
}