/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.bpel.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * An <code>ErrorHandler</code> implementation that dumps all of the error
 * messages onto a logging channel at the debug level.
 */
public class LoggingErrorHandler implements ErrorHandler {

  private static final Log log = LogFactory.getLog(LoggingErrorHandler.class);
  private Log l;

  private static final String WARNING = "WARNING";
  private static final String ERROR = "ERROR";
  private static final String FATAL = "FATAL";

  /**
   * Construct a new instance that dumps messages onto the default logging
   * channel defined by this class.
   */
  public LoggingErrorHandler() {
    l = log;
  }

  /**
   * @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
   */
  public void warning(SAXParseException exception) throws SAXException {
    if (l.isDebugEnabled()) {
      l.debug(formatMessage(WARNING, exception));
    }
  }

  /**
   * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
   */
  public void error(SAXParseException exception) throws SAXException {
    if (l.isDebugEnabled()) {
      l.debug(formatMessage(ERROR, exception));
    }
  }

  /**
   * @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
   */
  public void fatalError(SAXParseException exception) throws SAXException {
    if (l.isDebugEnabled()) {
      l.debug(formatMessage(FATAL, exception));
    }
  }

  private String formatMessage(String level, SAXParseException spe) {
      StringBuilder sb = new StringBuilder(64);

    if (spe.getSystemId() != null) {
      sb.append(spe.getSystemId());
    }

    sb.append(':');
    sb.append(spe.getLineNumber());
    sb.append(':');
    sb.append(spe.getColumnNumber());
    sb.append(':');
    sb.append(level);
    sb.append(':');
    sb.append(spe.getMessage());

    return sb.toString();
  }

}
