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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.math.BigInteger;

/**
 * This class is used to generate globally unique IDs. The requirements for
 * global uniqueness are as follows:
 * <p/>
 * <pre>
 *     1) The time on any machine is never set back.
 *     2) Each machine has a unique IP address.
 *     3) Each process has the 'org.apache.ode.uid.port' property set to the
 *        same non-zero value.
 *
 *    byte:    0   1   2   3   4  5  6  7  8  9  10  11   12  13
 *             [ IPADDRESS ]  [   START TIME IN MS     ] [ count]
 *    This format allow more compact string representation.
 *    Persistence mechanism maps 6 bits to a number-char mapping.  Byte
 *    0-5 (48 bits, 6 bits per char =&gt; 8 chars)
 *    Since the current time typically has zeros for many of its most significant
 *    digits, all leading zeros are truncated from the string representation.
 *    The following 6 bit to char mapping is used:
 *    0-9   -&gt; 0-9
 *    10-35 -&gt; A-Z
 *    36-60 -&gt; a-y
 *    61    -&gt; za
 *    62    -&gt; zb
 *    63    -&gt; zc
 * </pre>
 */
public final class GUID implements Cloneable, Comparable, java.io.Serializable {
    static final long serialVersionUID = -7977671257884186039L;

    private static Log log = LogFactory.getLog(GUID.class);

    private static String propertyPort = "org.wso2.carbon.humantask.uid.port";

    private static int port = Integer.getInteger(propertyPort, 33666);

    // 32 bits
    private static final byte[] ipadd = {127, 0, 0, 1};

    // 64 bits
    private static byte[] baseId = getSystemUniqId();

    // 32 bits
    private static short cnt = Short.MIN_VALUE;

//    private static GUID vmGuid;
//
//    static {
//        vmGuid = new GUID();
//    }

    private final byte[] id;

    private transient String guidstring = null;

    /**
     * Create a new unique GUID
     */
    public GUID() {
        short c;
        byte[] b;

        synchronized (GUID.class) {
            c = ++cnt;
            b = baseId;

            if (cnt == Short.MAX_VALUE) {
                cnt = Short.MIN_VALUE;
                baseId = getSystemUniqId();
            }
        }

        id = new byte[]{ipadd[0], ipadd[1], ipadd[2], ipadd[3], b[7], b[6],
                b[5], b[4], b[3], b[2], b[1], b[0], (byte) ((c >>> 8) & 0xff),
                (byte) (c & 0xff)};
    }

//    /**
//     * Reconstitute a GUID from it's string representation
//     *
//     * @param str DOCUMENTME
//     * @throws MalformedGuidException DOCUMENTME
//     */
//    public GUID(String str) throws MalformedGuidException {
//        if (str == null) {
//            throw new MalformedGuidException("String is null");
//        }
//
//        id = new byte[14];
//        stringToBytes(str);
//    }

//    /**
//     * Get the GUID bytes.
//     *
//     * @return byte[]
//     */
//    public byte[] getGuid() {
//        return id.clone();
//    }
//
//    public static GUID getVMGUID() {
//        return vmGuid;
//    }

    public int compareTo(Object o) {
        if (o == this) {

            return 0;
        }

        GUID o1 = (GUID) o;

        for (short i = 0; i < id.length; ++i) {
            if (o1.id[i] < id[i]) {

                return -1;
            } else if (o1.id[i] > id[i]) {

                return 1;
            }
        }

        return 0;
    }

    public boolean equals(Object o) {
        return o instanceof GUID && compareTo(o) == 0;
    }

    public int hashCode() {
        int ret = 0;

        for (short i = 0; i < id.length; ++i) {
            ret ^= (id[i] << (i % 4));
        }

        return ret;
    }

//    public static void main(String[] argv) throws Exception {
//        Set<GUID> set = new HashSet<GUID>();
//
//        for (int i = 0; i < 100000; ++i) {
//            GUID g = new GUID();
//
//            if (set.contains(g)) {
//                System.out.println("CONFLICT>>>");
//            }
//
//            set.add(g);
//
//            GUID ng = new GUID(g.toString());
//
//            if (!ng.toString().equals(g.toString()) || !ng.equals(g)) {
//                System.out.println("INEQUALITY>>>");
//                System.out.println(ng.toString());
//                System.out.println(g.toString());
//            } else {
//                System.out.println(g.toString());
//            }
//        }
//    }

    /**
     * Convert a GUID to it's string representation. This will return a string
     * of at most 32 bytes.
     *
     * @return DOCUMENTME
     */
    public String toString() {
        if (guidstring == null) {
            guidstring = mapBytesToChars();
        }

        return guidstring;
    }

    private static byte[] getSystemUniqId() {
        ProcessMutex pm = new ProcessMutex(port);

        try {
            pm.lock();
        } catch (InterruptedException ie) {
            log.error("ERROR: Could not establish unique starttime using\n"
                    + "       TCP port "
                    + port
                    + " for synchronization. \n"
                    + "       Perhaps this port is used by anotherprocess? \n"
                    + "       Check the '"
                    + propertyPort
                    + "' JAVA system property. \n", ie);
            throw new RuntimeException("GUID.getSystemUniqId() FAILED!!!", ie);
        }

        long uid = System.currentTimeMillis();
        pm.unlock();

        return new byte[]{(byte) (uid & 0xff), (byte) ((uid >>> 8) & 0xff),
                (byte) ((uid >>> 16) & 0xff), (byte) ((uid >>> 24) & 0xff),
                (byte) ((uid >>> 32) & 0xff), (byte) ((uid >>> 40) & 0xff),
                (byte) ((uid >>> 48) & 0xff), (byte) ((uid >>> 56) & 0xff)};
    }

    private String mapBytesToChars() {
        BigInteger bigInt = new BigInteger(id);
        return bigInt.toString(34);
    }

//    private void stringToBytes(String s) {
//        BigInteger bigInt = new BigInteger(s, 34);
//        byte[] bytes = bigInt.toByteArray();
//        System.arraycopy(bytes, 0, id, 0, id.length);
//    }
//
//    public static class MalformedGuidException extends Exception {
//
//        private static final long serialVersionUID = -8922336058603571809L;
//
//        public MalformedGuidException(String guid) {
//            super("Malformed guid: " + guid);
//        }
//    }

//    public static String makeGUID(String digest) {
//        String val = "0";
//        int maxlen = 32;
//        int base = 34;
//        int prime = 31;
//        for (int i = 0; i < digest.length(); i++) {
//            char c = digest.charAt(i);
//            val = new BigInteger(val, base).add(BigInteger.valueOf((long) c)).
//                    multiply(BigInteger.valueOf(prime)).toString(base);
//            if (val.length() > maxlen) {
//                val = val.substring(0, maxlen);
//            }
//        }
//
//        return val;
//    }
}
