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
package org.wso2.carbon.humantask.core.utils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;


/**
 * An inter-process synchronization mechanism. This class uses a well known
 * TCP port to synchronize multiple processes. The method of operation is
 * simple: by opening a port, a process prevents other processes on the local
 * machine from opening the same port.
 *
 * @author Maciej Szefler  ( m s z e f l e r @ g m a i l . c o m)
 */
public class ProcessMutex {
  private int port;
  private ServerSocket ss = null;

  /**
   * Constructor.
   *
   * @param port synchronization TCP port
   */
  public ProcessMutex(int port) {
    this.port = port;
  }

  /**
   * Acquire the lock. Will throw InterruptedException after timeout (15
   * seconds).
   *
   * @throws InterruptedException in case of timeout
   * @throws IllegalStateException in case lock is already acquired
   */
  public void lock()
            throws InterruptedException {
    synchronized (this) {
      if (ss != null) {
        throw new IllegalStateException("ProcessMutex: Bad mutex state exception.");
      }

      long startTime = System.currentTimeMillis();

      while ((startTime + 15000) > System.currentTimeMillis()) {
        try {
          ss = new ServerSocket();
          // Per Dan Kearns suggestion (jira ODE-100), prevents excessive hanging on to socket.
          ss.setReuseAddress(true);
          ss.bind(new InetSocketAddress(port));

          break;
        } catch (IOException ioe) {
          Thread.sleep(2);
        }
      }

      if (ss == null) {
        throw new InterruptedException("ProcessMutex: lock() timed out!");
      }
    }
  }

  /**
   * Release the lock
   *
   * @throws IllegalStateException DOCUMENTME
   */
  public void unlock() {
    synchronized (this) {
      if (ss == null) {
        throw new IllegalStateException("ProcessMutex: Bad mutex state exception.");
      }

      try {
        ss.close();
      } catch (IOException ioe) {
        throw new IllegalStateException("ProcessMutex: Error closing socket.");
      } finally {
        ss = null;
        this.notify();
      }
    }
  }
}
