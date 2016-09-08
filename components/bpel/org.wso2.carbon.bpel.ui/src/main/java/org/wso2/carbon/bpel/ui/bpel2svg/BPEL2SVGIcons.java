/**
 *  Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
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

package org.wso2.carbon.bpel.ui.bpel2svg;

/**
* Manage the icons/images of the activities
* The icons are in Base64 format and are given as String constants.
* */
public final class BPEL2SVGIcons {

    public static final String TRANSFORMATION_MATRIX;
    public static final String ASSIGN_ICON;
    public static final String CATCH_ICON;
    public static final String CATCHALL_ICON;
    public static final String COMPENSATESCOPE_ICON;
    public static final String COMPENSATE_ICON;
    public static final String COMPENSATIONHANDLER_ICON;
    public static final String ELSE_ICON;
    public static final String ELSEIF_ICON;
    public static final String EVENTHANDLER_ICON;
    public static final String EXIT_ICON;
    public static final String FAULTHANDLER_ICON;
    public static final String FLOW_ICON;
    public static final String FOREACH_ICON;
    public static final String ENDFOREACH_ICON;
    public static final String IF_ICON;
    public static final String ENDIF_ICON;
    public static final String INVOKE_ICON;
    public static final String ONALARM_ICON;
    public static final String ONEVENT_ICON;
    public static final String ONMESSAGE_ICON;
    public static final String PICK_ICON;
    public static final String RECEIVE_ICON;
    public static final String REPEATUNTIL_ICON;
    public static final String ENDREPEATUNTIL_ICON;
    public static final String REPLY_ICON;
    public static final String RETHROW_ICON;
    public static final String SCOPE_ICON;
    public static final String SEQUENCE_ICON;
    public static final String TERMINATIONHANDLER_ICON;
    public static final String THROW_ICON;
    public static final String WAIT_ICON;
    public static final String WHILE_ICON;
    public static final String ENDWHILE_ICON;
    public static final String START_ICON;
    public static final String END_ICON;
    public static final String EMPTY_ICON;


    static {

        TRANSFORMATION_MATRIX = "matrix(1.0124 0 0 1.0124 0 0)";

        ASSIGN_ICON = "data:image/jpg;base64," +
                "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAABGdBTUEAAK/INwWK6QAAABl0RVh0U29mdHdhcmUAQWRvYmUgSW1h" +
                "Z2VSZWFkeXHJZTwAAACgSURBVHjaYvz//z8DNQETA5XBCDSQBZmz6+Kj/8cevCHJACsFEQY3fTlGrAZO2nmZoTHZgSQD6+ceABmI" +
                "3YX///1nYGZkJMlAkB6cXgaZxUKaeQzo9qMYmOWkwxBZtJ4kAzuzrFEtoHZOwYjlS0/fk2SAnrQg7lietecqQ2WCHfVi+S/VY5mB" +
                "yrEcaa3KUD/nAEkGxtuq0zaWR2DxBRBgAIrzNXMyn6FVAAAAAElFTkSuQmCC";

        CATCH_ICON = "data:image/png;base64," +
                "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAACXBIWXMAAAsTAAALEwEAmpwYAAAKTWlDQ1BQaG90b3Nob3AgSUND" +
                "IHByb2ZpbGUAAHjanVN3WJP3Fj7f92UPVkLY8LGXbIEAIiOsCMgQWaIQkgBhhBASQMWFiApWFBURnEhVxILVCkidiOKgKLhnQYqI" +
                "WotVXDjuH9yntX167+3t+9f7vOec5/zOec8PgBESJpHmomoAOVKFPDrYH49PSMTJvYACFUjgBCAQ5svCZwXFAADwA3l4fnSwP/wB" +
                "r28AAgBw1S4kEsfh/4O6UCZXACCRAOAiEucLAZBSAMguVMgUAMgYALBTs2QKAJQAAGx5fEIiAKoNAOz0ST4FANipk9wXANiiHKkI" +
                "AI0BAJkoRyQCQLsAYFWBUiwCwMIAoKxAIi4EwK4BgFm2MkcCgL0FAHaOWJAPQGAAgJlCLMwAIDgCAEMeE80DIEwDoDDSv+CpX3CF" +
                "uEgBAMDLlc2XS9IzFLiV0Bp38vDg4iHiwmyxQmEXKRBmCeQinJebIxNI5wNMzgwAABr50cH+OD+Q5+bk4eZm52zv9MWi/mvwbyI+" +
                "IfHf/ryMAgQAEE7P79pf5eXWA3DHAbB1v2upWwDaVgBo3/ldM9sJoFoK0Hr5i3k4/EAenqFQyDwdHAoLC+0lYqG9MOOLPv8z4W/g" +
                "i372/EAe/tt68ABxmkCZrcCjg/1xYW52rlKO58sEQjFu9+cj/seFf/2OKdHiNLFcLBWK8ViJuFAiTcd5uVKRRCHJleIS6X8y8R+W" +
                "/QmTdw0ArIZPwE62B7XLbMB+7gECiw5Y0nYAQH7zLYwaC5EAEGc0Mnn3AACTv/mPQCsBAM2XpOMAALzoGFyolBdMxggAAESggSqw" +
                "QQcMwRSswA6cwR28wBcCYQZEQAwkwDwQQgbkgBwKoRiWQRlUwDrYBLWwAxqgEZrhELTBMTgN5+ASXIHrcBcGYBiewhi8hgkEQcgI" +
                "E2EhOogRYo7YIs4IF5mOBCJhSDSSgKQg6YgUUSLFyHKkAqlCapFdSCPyLXIUOY1cQPqQ28ggMor8irxHMZSBslED1AJ1QLmoHxqK" +
                "xqBz0XQ0D12AlqJr0Rq0Hj2AtqKn0UvodXQAfYqOY4DRMQ5mjNlhXIyHRWCJWBomxxZj5Vg1Vo81Yx1YN3YVG8CeYe8IJAKLgBPs" +
                "CF6EEMJsgpCQR1hMWEOoJewjtBK6CFcJg4Qxwicik6hPtCV6EvnEeGI6sZBYRqwm7iEeIZ4lXicOE1+TSCQOyZLkTgohJZAySQtJ" +
                "a0jbSC2kU6Q+0hBpnEwm65Btyd7kCLKArCCXkbeQD5BPkvvJw+S3FDrFiOJMCaIkUqSUEko1ZT/lBKWfMkKZoKpRzame1AiqiDqf" +
                "WkltoHZQL1OHqRM0dZolzZsWQ8ukLaPV0JppZ2n3aC/pdLoJ3YMeRZfQl9Jr6Afp5+mD9HcMDYYNg8dIYigZaxl7GacYtxkvmUym" +
                "BdOXmchUMNcyG5lnmA+Yb1VYKvYqfBWRyhKVOpVWlX6V56pUVXNVP9V5qgtUq1UPq15WfaZGVbNQ46kJ1Bar1akdVbupNq7OUndS" +
                "j1DPUV+jvl/9gvpjDbKGhUaghkijVGO3xhmNIRbGMmXxWELWclYD6yxrmE1iW7L57Ex2Bfsbdi97TFNDc6pmrGaRZp3mcc0BDsax" +
                "4PA52ZxKziHODc57LQMtPy2x1mqtZq1+rTfaetq+2mLtcu0W7eva73VwnUCdLJ31Om0693UJuja6UbqFutt1z+o+02PreekJ9cr1" +
                "Dund0Uf1bfSj9Rfq79bv0R83MDQINpAZbDE4Y/DMkGPoa5hpuNHwhOGoEctoupHEaKPRSaMnuCbuh2fjNXgXPmasbxxirDTeZdxr" +
                "PGFiaTLbpMSkxeS+Kc2Ua5pmutG003TMzMgs3KzYrMnsjjnVnGueYb7ZvNv8jYWlRZzFSos2i8eW2pZ8ywWWTZb3rJhWPlZ5VvVW" +
                "16xJ1lzrLOtt1ldsUBtXmwybOpvLtqitm63Edptt3xTiFI8p0in1U27aMez87ArsmuwG7Tn2YfYl9m32zx3MHBId1jt0O3xydHXM" +
                "dmxwvOuk4TTDqcSpw+lXZxtnoXOd8zUXpkuQyxKXdpcXU22niqdun3rLleUa7rrStdP1o5u7m9yt2W3U3cw9xX2r+00umxvJXcM9" +
                "70H08PdY4nHM452nm6fC85DnL152Xlle+70eT7OcJp7WMG3I28Rb4L3Le2A6Pj1l+s7pAz7GPgKfep+Hvqa+It89viN+1n6Zfgf8" +
                "nvs7+sv9j/i/4XnyFvFOBWABwQHlAb2BGoGzA2sDHwSZBKUHNQWNBbsGLww+FUIMCQ1ZH3KTb8AX8hv5YzPcZyya0RXKCJ0VWhv6" +
                "MMwmTB7WEY6GzwjfEH5vpvlM6cy2CIjgR2yIuB9pGZkX+X0UKSoyqi7qUbRTdHF09yzWrORZ+2e9jvGPqYy5O9tqtnJ2Z6xqbFJs" +
                "Y+ybuIC4qriBeIf4RfGXEnQTJAntieTE2MQ9ieNzAudsmjOc5JpUlnRjruXcorkX5unOy553PFk1WZB8OIWYEpeyP+WDIEJQLxhP" +
                "5aduTR0T8oSbhU9FvqKNolGxt7hKPJLmnVaV9jjdO31D+miGT0Z1xjMJT1IreZEZkrkj801WRNberM/ZcdktOZSclJyjUg1plrQr" +
                "1zC3KLdPZisrkw3keeZtyhuTh8r35CP5c/PbFWyFTNGjtFKuUA4WTC+oK3hbGFt4uEi9SFrUM99m/ur5IwuCFny9kLBQuLCz2Lh4" +
                "WfHgIr9FuxYji1MXdy4xXVK6ZHhp8NJ9y2jLspb9UOJYUlXyannc8o5Sg9KlpUMrglc0lamUycturvRauWMVYZVkVe9ql9VbVn8q" +
                "F5VfrHCsqK74sEa45uJXTl/VfPV5bdra3kq3yu3rSOuk626s91m/r0q9akHV0IbwDa0b8Y3lG19tSt50oXpq9Y7NtM3KzQM1YTXt" +
                "W8y2rNvyoTaj9nqdf13LVv2tq7e+2Sba1r/dd3vzDoMdFTve75TsvLUreFdrvUV99W7S7oLdjxpiG7q/5n7duEd3T8Wej3ulewf2" +
                "Re/ranRvbNyvv7+yCW1SNo0eSDpw5ZuAb9qb7Zp3tXBaKg7CQeXBJ9+mfHvjUOihzsPcw83fmX+39QjrSHkr0jq/dawto22gPaG9" +
                "7+iMo50dXh1Hvrf/fu8x42N1xzWPV56gnSg98fnkgpPjp2Snnp1OPz3Umdx590z8mWtdUV29Z0PPnj8XdO5Mt1/3yfPe549d8Lxw" +
                "9CL3Ytslt0utPa49R35w/eFIr1tv62X3y+1XPK509E3rO9Hv03/6asDVc9f41y5dn3m978bsG7duJt0cuCW69fh29u0XdwruTNxd" +
                "eo94r/y+2v3qB/oP6n+0/rFlwG3g+GDAYM/DWQ/vDgmHnv6U/9OH4dJHzEfVI0YjjY+dHx8bDRq98mTOk+GnsqcTz8p+Vv9563Or" +
                "59/94vtLz1j82PAL+YvPv655qfNy76uprzrHI8cfvM55PfGm/K3O233vuO+638e9H5ko/ED+UPPR+mPHp9BP9z7nfP78L/eE8/sl" +
                "0p8zAAAABGdBTUEAALGOfPtRkwAAACBjSFJNAAB6JQAAgIMAAPn/AACA6QAAdTAAAOpgAAA6mAAAF2+SX8VGAAACRklEQVR42qyV" +
                "TUhUURTHf3d0FD8aP568UBoIoqScbAgmocEiXAa6blPNNmgTrcKN+2jZQhfabFq4aNtGtJpClD4EaREUQRFpvnB03ryZ9969t8Vz" +
                "HEefA4YHDpz7cf73f8//XK7QWnOcFuGYrbES/H12QVsrG0dKNi710H3nswgFtFY2iGceHAnwx/QTug9jiNQoaeNZ3wFoT1ylsPru" +
                "AEhlPmqcBqnr11BLhXKKtPUPMHn5Pm39AyinuOt757VU9UXRWqNdl4jnI3yPlGkwNTROLJEk4vnEEkmmhsZJmQbC99CuS1iHVBkq" +
                "DdJFlUtsLeZIzmZImQaT6QnaLp5nMj1ByjRIzmbYWsyBdIOcwwEVyrFxtwO35nMMZkcBmLr+mJRpMJgdxZrP4W7bKMcGpQ5vGyT4" +
                "dh53s1A9o2DtxomnadZeLu+Oo3YeZJ0+lFojHRvfdgHoHY6zeu9tdWexugYgHRtZr4ZKKVAeogL28BPL6xaZ7Agp02D69hy9w3EE" +
                "IACUF+TsM1FRanNpRjd29QBQ/lLCKpU5c+0U5fUCzWY7m0t/sEpl+gY7AoZ5C600nVfuinDA5Rn9cf3Xf7/hGzcfiVpRgJ+ij999" +
                "I7QiKW7laY11cCLWEVxTBPWpiXe46W8LISprKHlNJE72cDbWwsKL56THbhGNQIMIPCKggT3xDuCHryGitHR24XhN1aLvMKgZh8SC" +
                "gMgBwOZzY6KyUEnyy6WDYPsOAXC8aMiVAddvZO3Na9aAaFM37+deHVkccdxfwL8BAF6qBTafR5X7AAAAAElFTkSuQmCC";

        CATCHALL_ICON = "data:image/png;base64," +
                "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAACXBIWXMAAAsTAAALEwEAmpwYAAAKTWlDQ1BQaG90b3Nob3AgSUND" +
                "IHByb2ZpbGUAAHjanVN3WJP3Fj7f92UPVkLY8LGXbIEAIiOsCMgQWaIQkgBhhBASQMWFiApWFBURnEhVxILVCkidiOKgKLhnQYqI" +
                "WotVXDjuH9yntX167+3t+9f7vOec5/zOec8PgBESJpHmomoAOVKFPDrYH49PSMTJvYACFUjgBCAQ5svCZwXFAADwA3l4fnSwP/wB" +
                "r28AAgBw1S4kEsfh/4O6UCZXACCRAOAiEucLAZBSAMguVMgUAMgYALBTs2QKAJQAAGx5fEIiAKoNAOz0ST4FANipk9wXANiiHKkI" +
                "AI0BAJkoRyQCQLsAYFWBUiwCwMIAoKxAIi4EwK4BgFm2MkcCgL0FAHaOWJAPQGAAgJlCLMwAIDgCAEMeE80DIEwDoDDSv+CpX3CF" +
                "uEgBAMDLlc2XS9IzFLiV0Bp38vDg4iHiwmyxQmEXKRBmCeQinJebIxNI5wNMzgwAABr50cH+OD+Q5+bk4eZm52zv9MWi/mvwbyI+" +
                "IfHf/ryMAgQAEE7P79pf5eXWA3DHAbB1v2upWwDaVgBo3/ldM9sJoFoK0Hr5i3k4/EAenqFQyDwdHAoLC+0lYqG9MOOLPv8z4W/g" +
                "i372/EAe/tt68ABxmkCZrcCjg/1xYW52rlKO58sEQjFu9+cj/seFf/2OKdHiNLFcLBWK8ViJuFAiTcd5uVKRRCHJleIS6X8y8R+W" +
                "/QmTdw0ArIZPwE62B7XLbMB+7gECiw5Y0nYAQH7zLYwaC5EAEGc0Mnn3AACTv/mPQCsBAM2XpOMAALzoGFyolBdMxggAAESggSqw" +
                "QQcMwRSswA6cwR28wBcCYQZEQAwkwDwQQgbkgBwKoRiWQRlUwDrYBLWwAxqgEZrhELTBMTgN5+ASXIHrcBcGYBiewhi8hgkEQcgI" +
                "E2EhOogRYo7YIs4IF5mOBCJhSDSSgKQg6YgUUSLFyHKkAqlCapFdSCPyLXIUOY1cQPqQ28ggMor8irxHMZSBslED1AJ1QLmoHxqK" +
                "xqBz0XQ0D12AlqJr0Rq0Hj2AtqKn0UvodXQAfYqOY4DRMQ5mjNlhXIyHRWCJWBomxxZj5Vg1Vo81Yx1YN3YVG8CeYe8IJAKLgBPs" +
                "CF6EEMJsgpCQR1hMWEOoJewjtBK6CFcJg4Qxwicik6hPtCV6EvnEeGI6sZBYRqwm7iEeIZ4lXicOE1+TSCQOyZLkTgohJZAySQtJ" +
                "a0jbSC2kU6Q+0hBpnEwm65Btyd7kCLKArCCXkbeQD5BPkvvJw+S3FDrFiOJMCaIkUqSUEko1ZT/lBKWfMkKZoKpRzame1AiqiDqf" +
                "WkltoHZQL1OHqRM0dZolzZsWQ8ukLaPV0JppZ2n3aC/pdLoJ3YMeRZfQl9Jr6Afp5+mD9HcMDYYNg8dIYigZaxl7GacYtxkvmUym" +
                "BdOXmchUMNcyG5lnmA+Yb1VYKvYqfBWRyhKVOpVWlX6V56pUVXNVP9V5qgtUq1UPq15WfaZGVbNQ46kJ1Bar1akdVbupNq7OUndS" +
                "j1DPUV+jvl/9gvpjDbKGhUaghkijVGO3xhmNIRbGMmXxWELWclYD6yxrmE1iW7L57Ex2Bfsbdi97TFNDc6pmrGaRZp3mcc0BDsax" +
                "4PA52ZxKziHODc57LQMtPy2x1mqtZq1+rTfaetq+2mLtcu0W7eva73VwnUCdLJ31Om0693UJuja6UbqFutt1z+o+02PreekJ9cr1" +
                "Dund0Uf1bfSj9Rfq79bv0R83MDQINpAZbDE4Y/DMkGPoa5hpuNHwhOGoEctoupHEaKPRSaMnuCbuh2fjNXgXPmasbxxirDTeZdxr" +
                "PGFiaTLbpMSkxeS+Kc2Ua5pmutG003TMzMgs3KzYrMnsjjnVnGueYb7ZvNv8jYWlRZzFSos2i8eW2pZ8ywWWTZb3rJhWPlZ5VvVW" +
                "16xJ1lzrLOtt1ldsUBtXmwybOpvLtqitm63Edptt3xTiFI8p0in1U27aMez87ArsmuwG7Tn2YfYl9m32zx3MHBId1jt0O3xydHXM" +
                "dmxwvOuk4TTDqcSpw+lXZxtnoXOd8zUXpkuQyxKXdpcXU22niqdun3rLleUa7rrStdP1o5u7m9yt2W3U3cw9xX2r+00umxvJXcM9" +
                "70H08PdY4nHM452nm6fC85DnL152Xlle+70eT7OcJp7WMG3I28Rb4L3Le2A6Pj1l+s7pAz7GPgKfep+Hvqa+It89viN+1n6Zfgf8" +
                "nvs7+sv9j/i/4XnyFvFOBWABwQHlAb2BGoGzA2sDHwSZBKUHNQWNBbsGLww+FUIMCQ1ZH3KTb8AX8hv5YzPcZyya0RXKCJ0VWhv6" +
                "MMwmTB7WEY6GzwjfEH5vpvlM6cy2CIjgR2yIuB9pGZkX+X0UKSoyqi7qUbRTdHF09yzWrORZ+2e9jvGPqYy5O9tqtnJ2Z6xqbFJs" +
                "Y+ybuIC4qriBeIf4RfGXEnQTJAntieTE2MQ9ieNzAudsmjOc5JpUlnRjruXcorkX5unOy553PFk1WZB8OIWYEpeyP+WDIEJQLxhP" +
                "5aduTR0T8oSbhU9FvqKNolGxt7hKPJLmnVaV9jjdO31D+miGT0Z1xjMJT1IreZEZkrkj801WRNberM/ZcdktOZSclJyjUg1plrQr" +
                "1zC3KLdPZisrkw3keeZtyhuTh8r35CP5c/PbFWyFTNGjtFKuUA4WTC+oK3hbGFt4uEi9SFrUM99m/ur5IwuCFny9kLBQuLCz2Lh4" +
                "WfHgIr9FuxYji1MXdy4xXVK6ZHhp8NJ9y2jLspb9UOJYUlXyannc8o5Sg9KlpUMrglc0lamUycturvRauWMVYZVkVe9ql9VbVn8q" +
                "F5VfrHCsqK74sEa45uJXTl/VfPV5bdra3kq3yu3rSOuk626s91m/r0q9akHV0IbwDa0b8Y3lG19tSt50oXpq9Y7NtM3KzQM1YTXt" +
                "W8y2rNvyoTaj9nqdf13LVv2tq7e+2Sba1r/dd3vzDoMdFTve75TsvLUreFdrvUV99W7S7oLdjxpiG7q/5n7duEd3T8Wej3ulewf2" +
                "Re/ranRvbNyvv7+yCW1SNo0eSDpw5ZuAb9qb7Zp3tXBaKg7CQeXBJ9+mfHvjUOihzsPcw83fmX+39QjrSHkr0jq/dawto22gPaG9" +
                "7+iMo50dXh1Hvrf/fu8x42N1xzWPV56gnSg98fnkgpPjp2Snnp1OPz3Umdx590z8mWtdUV29Z0PPnj8XdO5Mt1/3yfPe549d8Lxw" +
                "9CL3Ytslt0utPa49R35w/eFIr1tv62X3y+1XPK509E3rO9Hv03/6asDVc9f41y5dn3m978bsG7duJt0cuCW69fh29u0XdwruTNxd" +
                "eo94r/y+2v3qB/oP6n+0/rFlwG3g+GDAYM/DWQ/vDgmHnv6U/9OH4dJHzEfVI0YjjY+dHx8bDRq98mTOk+GnsqcTz8p+Vv9563Or" +
                "59/94vtLz1j82PAL+YvPv655qfNy76uprzrHI8cfvM55PfGm/K3O233vuO+638e9H5ko/ED+UPPR+mPHp9BP9z7nfP78L/eE8/sl" +
                "0p8zAAAABGdBTUEAALGOfPtRkwAAACBjSFJNAAB6JQAAgIMAAPn/AACA6QAAdTAAAOpgAAA6mAAAF2+SX8VGAAACjUlEQVR42pyU" +
                "UUhTYRSAvzu3Zduc0eSm0p5CpFwyiiE4RMSHgsCinnwoWC9J0Iv0ZL740FO9VqJQNB96EOwp8EXUWiFIUWFEhBHkg4ordbu72727" +
                "/9/D3J26KeqBA+c/5/+/c85/7n+RUnJUvXRnQO72KVJKDiuX+x7I6kAzdT4fa+k02eQPJocfKkAJ+PflOZn8snYgYDDWz424ipVa" +
                "pKrmDG8e3VaKMRv4s1+VwVj/gYDXnqQ4Ud+C71g16VyW9eVvdoVOe5clEZaGmfwNgC/UTnrhQxnMF2pnok8DvnN9WNigoji2L6Ql" +
                "EHoGb3MLIxfu4W1uQegZW4t+d50XaQkm+hxlCW2PlBJpGDjMPEreJKIGGG0bxB8K4zDz+ENhRtsGiagBlLyJNAyWnj/bG4iQYBmI" +
                "XJbNuQTh8RgRNcBIdAjv+bOMRIeIqAHC4zE25xJgGYUzewMFQtcwUgVNTidojfcAMNr5mIgaoDXeQ3I6gZHSELoGQpQBtw0F8toG" +
                "xnq6lCOdtO3Q0ygrk/P22qVtgMXeQEtKLF0jrxkANHQEWbj7vrQzU4oBWLqGJfdpWQgBwkQpwu5/Zn41SSzeTUQN8OLWFA0dQRRA" +
                "ARBm4cwusT/s5OyY9Jyux+lx8G9ulTU9R1NXkOxqimq1pszn9HnJLC0T6LypVAbOjMmv2h+OKl1XBna+FClhSWlkubEbDxaZzQ08" +
                "/lpq/LWFNpXC/eywt2qTv2YqTFlKsqab0Kk6mvzHmXn9iujVXlwOqFIK6lCgim32FvDTYoWhuGtd6Ka7dOlbFexYV7AVIGu6y4H+" +
                "i71KMVA8lM9ly2G7kgDopqtCy4CRd7Ly7i0rgMt9ko9Ts4cezpH+2PvJ/wEAmhxhP9dSJlcAAAAASUVORK5CYII=";

        COMPENSATESCOPE_ICON = "data:image/png;base64," +
                "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAACXBIWXMAAAsTAAALEwEAmpwYAAAKTWlDQ1BQaG90b3Nob3AgSUND" +
                "IHByb2ZpbGUAAHjanVN3WJP3Fj7f92UPVkLY8LGXbIEAIiOsCMgQWaIQkgBhhBASQMWFiApWFBURnEhVxILVCkidiOKgKLhnQYqI" +
                "WotVXDjuH9yntX167+3t+9f7vOec5/zOec8PgBESJpHmomoAOVKFPDrYH49PSMTJvYACFUjgBCAQ5svCZwXFAADwA3l4fnSwP/wB" +
                "r28AAgBw1S4kEsfh/4O6UCZXACCRAOAiEucLAZBSAMguVMgUAMgYALBTs2QKAJQAAGx5fEIiAKoNAOz0ST4FANipk9wXANiiHKkI" +
                "AI0BAJkoRyQCQLsAYFWBUiwCwMIAoKxAIi4EwK4BgFm2MkcCgL0FAHaOWJAPQGAAgJlCLMwAIDgCAEMeE80DIEwDoDDSv+CpX3CF" +
                "uEgBAMDLlc2XS9IzFLiV0Bp38vDg4iHiwmyxQmEXKRBmCeQinJebIxNI5wNMzgwAABr50cH+OD+Q5+bk4eZm52zv9MWi/mvwbyI+" +
                "IfHf/ryMAgQAEE7P79pf5eXWA3DHAbB1v2upWwDaVgBo3/ldM9sJoFoK0Hr5i3k4/EAenqFQyDwdHAoLC+0lYqG9MOOLPv8z4W/g" +
                "i372/EAe/tt68ABxmkCZrcCjg/1xYW52rlKO58sEQjFu9+cj/seFf/2OKdHiNLFcLBWK8ViJuFAiTcd5uVKRRCHJleIS6X8y8R+W" +
                "/QmTdw0ArIZPwE62B7XLbMB+7gECiw5Y0nYAQH7zLYwaC5EAEGc0Mnn3AACTv/mPQCsBAM2XpOMAALzoGFyolBdMxggAAESggSqw" +
                "QQcMwRSswA6cwR28wBcCYQZEQAwkwDwQQgbkgBwKoRiWQRlUwDrYBLWwAxqgEZrhELTBMTgN5+ASXIHrcBcGYBiewhi8hgkEQcgI" +
                "E2EhOogRYo7YIs4IF5mOBCJhSDSSgKQg6YgUUSLFyHKkAqlCapFdSCPyLXIUOY1cQPqQ28ggMor8irxHMZSBslED1AJ1QLmoHxqK" +
                "xqBz0XQ0D12AlqJr0Rq0Hj2AtqKn0UvodXQAfYqOY4DRMQ5mjNlhXIyHRWCJWBomxxZj5Vg1Vo81Yx1YN3YVG8CeYe8IJAKLgBPs" +
                "CF6EEMJsgpCQR1hMWEOoJewjtBK6CFcJg4Qxwicik6hPtCV6EvnEeGI6sZBYRqwm7iEeIZ4lXicOE1+TSCQOyZLkTgohJZAySQtJ" +
                "a0jbSC2kU6Q+0hBpnEwm65Btyd7kCLKArCCXkbeQD5BPkvvJw+S3FDrFiOJMCaIkUqSUEko1ZT/lBKWfMkKZoKpRzame1AiqiDqf" +
                "WkltoHZQL1OHqRM0dZolzZsWQ8ukLaPV0JppZ2n3aC/pdLoJ3YMeRZfQl9Jr6Afp5+mD9HcMDYYNg8dIYigZaxl7GacYtxkvmUym" +
                "BdOXmchUMNcyG5lnmA+Yb1VYKvYqfBWRyhKVOpVWlX6V56pUVXNVP9V5qgtUq1UPq15WfaZGVbNQ46kJ1Bar1akdVbupNq7OUndS" +
                "j1DPUV+jvl/9gvpjDbKGhUaghkijVGO3xhmNIRbGMmXxWELWclYD6yxrmE1iW7L57Ex2Bfsbdi97TFNDc6pmrGaRZp3mcc0BDsax" +
                "4PA52ZxKziHODc57LQMtPy2x1mqtZq1+rTfaetq+2mLtcu0W7eva73VwnUCdLJ31Om0693UJuja6UbqFutt1z+o+02PreekJ9cr1" +
                "Dund0Uf1bfSj9Rfq79bv0R83MDQINpAZbDE4Y/DMkGPoa5hpuNHwhOGoEctoupHEaKPRSaMnuCbuh2fjNXgXPmasbxxirDTeZdxr" +
                "PGFiaTLbpMSkxeS+Kc2Ua5pmutG003TMzMgs3KzYrMnsjjnVnGueYb7ZvNv8jYWlRZzFSos2i8eW2pZ8ywWWTZb3rJhWPlZ5VvVW" +
                "16xJ1lzrLOtt1ldsUBtXmwybOpvLtqitm63Edptt3xTiFI8p0in1U27aMez87ArsmuwG7Tn2YfYl9m32zx3MHBId1jt0O3xydHXM" +
                "dmxwvOuk4TTDqcSpw+lXZxtnoXOd8zUXpkuQyxKXdpcXU22niqdun3rLleUa7rrStdP1o5u7m9yt2W3U3cw9xX2r+00umxvJXcM9" +
                "70H08PdY4nHM452nm6fC85DnL152Xlle+70eT7OcJp7WMG3I28Rb4L3Le2A6Pj1l+s7pAz7GPgKfep+Hvqa+It89viN+1n6Zfgf8" +
                "nvs7+sv9j/i/4XnyFvFOBWABwQHlAb2BGoGzA2sDHwSZBKUHNQWNBbsGLww+FUIMCQ1ZH3KTb8AX8hv5YzPcZyya0RXKCJ0VWhv6" +
                "MMwmTB7WEY6GzwjfEH5vpvlM6cy2CIjgR2yIuB9pGZkX+X0UKSoyqi7qUbRTdHF09yzWrORZ+2e9jvGPqYy5O9tqtnJ2Z6xqbFJs" +
                "Y+ybuIC4qriBeIf4RfGXEnQTJAntieTE2MQ9ieNzAudsmjOc5JpUlnRjruXcorkX5unOy553PFk1WZB8OIWYEpeyP+WDIEJQLxhP" +
                "5aduTR0T8oSbhU9FvqKNolGxt7hKPJLmnVaV9jjdO31D+miGT0Z1xjMJT1IreZEZkrkj801WRNberM/ZcdktOZSclJyjUg1plrQr" +
                "1zC3KLdPZisrkw3keeZtyhuTh8r35CP5c/PbFWyFTNGjtFKuUA4WTC+oK3hbGFt4uEi9SFrUM99m/ur5IwuCFny9kLBQuLCz2Lh4" +
                "WfHgIr9FuxYji1MXdy4xXVK6ZHhp8NJ9y2jLspb9UOJYUlXyannc8o5Sg9KlpUMrglc0lamUycturvRauWMVYZVkVe9ql9VbVn8q" +
                "F5VfrHCsqK74sEa45uJXTl/VfPV5bdra3kq3yu3rSOuk626s91m/r0q9akHV0IbwDa0b8Y3lG19tSt50oXpq9Y7NtM3KzQM1YTXt" +
                "W8y2rNvyoTaj9nqdf13LVv2tq7e+2Sba1r/dd3vzDoMdFTve75TsvLUreFdrvUV99W7S7oLdjxpiG7q/5n7duEd3T8Wej3ulewf2" +
                "Re/ranRvbNyvv7+yCW1SNo0eSDpw5ZuAb9qb7Zp3tXBaKg7CQeXBJ9+mfHvjUOihzsPcw83fmX+39QjrSHkr0jq/dawto22gPaG9" +
                "7+iMo50dXh1Hvrf/fu8x42N1xzWPV56gnSg98fnkgpPjp2Snnp1OPz3Umdx590z8mWtdUV29Z0PPnj8XdO5Mt1/3yfPe549d8Lxw" +
                "9CL3Ytslt0utPa49R35w/eFIr1tv62X3y+1XPK509E3rO9Hv03/6asDVc9f41y5dn3m978bsG7duJt0cuCW69fh29u0XdwruTNxd" +
                "eo94r/y+2v3qB/oP6n+0/rFlwG3g+GDAYM/DWQ/vDgmHnv6U/9OH4dJHzEfVI0YjjY+dHx8bDRq98mTOk+GnsqcTz8p+Vv9563Or" +
                "59/94vtLz1j82PAL+YvPv655qfNy76uprzrHI8cfvM55PfGm/K3O233vuO+638e9H5ko/ED+UPPR+mPHp9BP9z7nfP78L/eE8/sl" +
                "0p8zAAAABGdBTUEAALGOfPtRkwAAACBjSFJNAAB6JQAAgIMAAPn/AACA6QAAdTAAAOpgAAA6mAAAF2+SX8VGAAABZ0lEQVR42tSU" +
                "vUtCYRTGn9e0uSxtavNPqKnNaGprCBrKrSmCgqgQyYRoagj6H0KiD7ugZV9bS0tLS0MgtISZNzT1Xn3fp0Gxr5ve0qEeOMs5h9/L" +
                "Oe85R5BEO+VAm2ULGA95eLDQY68Ukg0tEeql1DVKXePuvJvN8hsGj8MespxiLpvkfXKUUte4M9fNXwFPIl4q8465TJy5TILPN0tM" +
                "HQ5R6hqjs99DLZ2nES+VfGQ+HeNLOsZ8zfTraab2Byh1jdszXZZQ8Xlsztf66F+8euuxzKKo36KQvoS7f/hDbjQ4iYmtrHjvc1p9" +
                "1Nn6IAhAKWJk+QKiUoLDKAIA9sJTUKoas9IXoD/4UH8xueolRCeELAHlKhACGN98Ei3NoZAGnGah1oO/tykdgDTgkEa95NaAQkEo" +
                "EzCM9u2yUBU4VMUW0NkUhk64fAG4fAFbQNHsHh6teKgkYJgKlMDYRka0BPwfB/Yneh0AnkhMXpPkI5cAAAAASUVORK5CYII=";

        COMPENSATE_ICON = "data:image/png;base64," +
                "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAACXBIWXMAAAsTAAALEwEAmpwYAAAKTWlDQ1BQaG90b3Nob3AgSUND" +
                "IHByb2ZpbGUAAHjanVN3WJP3Fj7f92UPVkLY8LGXbIEAIiOsCMgQWaIQkgBhhBASQMWFiApWFBURnEhVxILVCkidiOKgKLhnQYqI" +
                "WotVXDjuH9yntX167+3t+9f7vOec5/zOec8PgBESJpHmomoAOVKFPDrYH49PSMTJvYACFUjgBCAQ5svCZwXFAADwA3l4fnSwP/wB" +
                "r28AAgBw1S4kEsfh/4O6UCZXACCRAOAiEucLAZBSAMguVMgUAMgYALBTs2QKAJQAAGx5fEIiAKoNAOz0ST4FANipk9wXANiiHKkI" +
                "AI0BAJkoRyQCQLsAYFWBUiwCwMIAoKxAIi4EwK4BgFm2MkcCgL0FAHaOWJAPQGAAgJlCLMwAIDgCAEMeE80DIEwDoDDSv+CpX3CF" +
                "uEgBAMDLlc2XS9IzFLiV0Bp38vDg4iHiwmyxQmEXKRBmCeQinJebIxNI5wNMzgwAABr50cH+OD+Q5+bk4eZm52zv9MWi/mvwbyI+" +
                "IfHf/ryMAgQAEE7P79pf5eXWA3DHAbB1v2upWwDaVgBo3/ldM9sJoFoK0Hr5i3k4/EAenqFQyDwdHAoLC+0lYqG9MOOLPv8z4W/g" +
                "i372/EAe/tt68ABxmkCZrcCjg/1xYW52rlKO58sEQjFu9+cj/seFf/2OKdHiNLFcLBWK8ViJuFAiTcd5uVKRRCHJleIS6X8y8R+W" +
                "/QmTdw0ArIZPwE62B7XLbMB+7gECiw5Y0nYAQH7zLYwaC5EAEGc0Mnn3AACTv/mPQCsBAM2XpOMAALzoGFyolBdMxggAAESggSqw" +
                "QQcMwRSswA6cwR28wBcCYQZEQAwkwDwQQgbkgBwKoRiWQRlUwDrYBLWwAxqgEZrhELTBMTgN5+ASXIHrcBcGYBiewhi8hgkEQcgI" +
                "E2EhOogRYo7YIs4IF5mOBCJhSDSSgKQg6YgUUSLFyHKkAqlCapFdSCPyLXIUOY1cQPqQ28ggMor8irxHMZSBslED1AJ1QLmoHxqK" +
                "xqBz0XQ0D12AlqJr0Rq0Hj2AtqKn0UvodXQAfYqOY4DRMQ5mjNlhXIyHRWCJWBomxxZj5Vg1Vo81Yx1YN3YVG8CeYe8IJAKLgBPs" +
                "CF6EEMJsgpCQR1hMWEOoJewjtBK6CFcJg4Qxwicik6hPtCV6EvnEeGI6sZBYRqwm7iEeIZ4lXicOE1+TSCQOyZLkTgohJZAySQtJ" +
                "a0jbSC2kU6Q+0hBpnEwm65Btyd7kCLKArCCXkbeQD5BPkvvJw+S3FDrFiOJMCaIkUqSUEko1ZT/lBKWfMkKZoKpRzame1AiqiDqf" +
                "WkltoHZQL1OHqRM0dZolzZsWQ8ukLaPV0JppZ2n3aC/pdLoJ3YMeRZfQl9Jr6Afp5+mD9HcMDYYNg8dIYigZaxl7GacYtxkvmUym" +
                "BdOXmchUMNcyG5lnmA+Yb1VYKvYqfBWRyhKVOpVWlX6V56pUVXNVP9V5qgtUq1UPq15WfaZGVbNQ46kJ1Bar1akdVbupNq7OUndS" +
                "j1DPUV+jvl/9gvpjDbKGhUaghkijVGO3xhmNIRbGMmXxWELWclYD6yxrmE1iW7L57Ex2Bfsbdi97TFNDc6pmrGaRZp3mcc0BDsax" +
                "4PA52ZxKziHODc57LQMtPy2x1mqtZq1+rTfaetq+2mLtcu0W7eva73VwnUCdLJ31Om0693UJuja6UbqFutt1z+o+02PreekJ9cr1" +
                "Dund0Uf1bfSj9Rfq79bv0R83MDQINpAZbDE4Y/DMkGPoa5hpuNHwhOGoEctoupHEaKPRSaMnuCbuh2fjNXgXPmasbxxirDTeZdxr" +
                "PGFiaTLbpMSkxeS+Kc2Ua5pmutG003TMzMgs3KzYrMnsjjnVnGueYb7ZvNv8jYWlRZzFSos2i8eW2pZ8ywWWTZb3rJhWPlZ5VvVW" +
                "16xJ1lzrLOtt1ldsUBtXmwybOpvLtqitm63Edptt3xTiFI8p0in1U27aMez87ArsmuwG7Tn2YfYl9m32zx3MHBId1jt0O3xydHXM" +
                "dmxwvOuk4TTDqcSpw+lXZxtnoXOd8zUXpkuQyxKXdpcXU22niqdun3rLleUa7rrStdP1o5u7m9yt2W3U3cw9xX2r+00umxvJXcM9" +
                "70H08PdY4nHM452nm6fC85DnL152Xlle+70eT7OcJp7WMG3I28Rb4L3Le2A6Pj1l+s7pAz7GPgKfep+Hvqa+It89viN+1n6Zfgf8" +
                "nvs7+sv9j/i/4XnyFvFOBWABwQHlAb2BGoGzA2sDHwSZBKUHNQWNBbsGLww+FUIMCQ1ZH3KTb8AX8hv5YzPcZyya0RXKCJ0VWhv6" +
                "MMwmTB7WEY6GzwjfEH5vpvlM6cy2CIjgR2yIuB9pGZkX+X0UKSoyqi7qUbRTdHF09yzWrORZ+2e9jvGPqYy5O9tqtnJ2Z6xqbFJs" +
                "Y+ybuIC4qriBeIf4RfGXEnQTJAntieTE2MQ9ieNzAudsmjOc5JpUlnRjruXcorkX5unOy553PFk1WZB8OIWYEpeyP+WDIEJQLxhP" +
                "5aduTR0T8oSbhU9FvqKNolGxt7hKPJLmnVaV9jjdO31D+miGT0Z1xjMJT1IreZEZkrkj801WRNberM/ZcdktOZSclJyjUg1plrQr" +
                "1zC3KLdPZisrkw3keeZtyhuTh8r35CP5c/PbFWyFTNGjtFKuUA4WTC+oK3hbGFt4uEi9SFrUM99m/ur5IwuCFny9kLBQuLCz2Lh4" +
                "WfHgIr9FuxYji1MXdy4xXVK6ZHhp8NJ9y2jLspb9UOJYUlXyannc8o5Sg9KlpUMrglc0lamUycturvRauWMVYZVkVe9ql9VbVn8q" +
                "F5VfrHCsqK74sEa45uJXTl/VfPV5bdra3kq3yu3rSOuk626s91m/r0q9akHV0IbwDa0b8Y3lG19tSt50oXpq9Y7NtM3KzQM1YTXt" +
                "W8y2rNvyoTaj9nqdf13LVv2tq7e+2Sba1r/dd3vzDoMdFTve75TsvLUreFdrvUV99W7S7oLdjxpiG7q/5n7duEd3T8Wej3ulewf2" +
                "Re/ranRvbNyvv7+yCW1SNo0eSDpw5ZuAb9qb7Zp3tXBaKg7CQeXBJ9+mfHvjUOihzsPcw83fmX+39QjrSHkr0jq/dawto22gPaG9" +
                "7+iMo50dXh1Hvrf/fu8x42N1xzWPV56gnSg98fnkgpPjp2Snnp1OPz3Umdx590z8mWtdUV29Z0PPnj8XdO5Mt1/3yfPe549d8Lxw" +
                "9CL3Ytslt0utPa49R35w/eFIr1tv62X3y+1XPK509E3rO9Hv03/6asDVc9f41y5dn3m978bsG7duJt0cuCW69fh29u0XdwruTNxd" +
                "eo94r/y+2v3qB/oP6n+0/rFlwG3g+GDAYM/DWQ/vDgmHnv6U/9OH4dJHzEfVI0YjjY+dHx8bDRq98mTOk+GnsqcTz8p+Vv9563Or" +
                "59/94vtLz1j82PAL+YvPv655qfNy76uprzrHI8cfvM55PfGm/K3O233vuO+638e9H5ko/ED+UPPR+mPHp9BP9z7nfP78L/eE8/sl" +
                "0p8zAAAABGdBTUEAALGOfPtRkwAAACBjSFJNAAB6JQAAgIMAAPn/AACA6QAAdTAAAOpgAAA6mAAAF2+SX8VGAAABZ0lEQVR42tSU" +
                "vUtCYRTGn9e0uSxtavNPqKnNaGprCBrKrSmCgqgQyYRoagj6H0KiD7ugZV9bS0tLS0MgtISZNzT1Xn3fp0Gxr5ve0qEeOMs5h9/L" +
                "Oe85R5BEO+VAm2ULGA95eLDQY68Ukg0tEeql1DVKXePuvJvN8hsGj8MespxiLpvkfXKUUte4M9fNXwFPIl4q8465TJy5TILPN0tM" +
                "HQ5R6hqjs99DLZ2nES+VfGQ+HeNLOsZ8zfTraab2Byh1jdszXZZQ8Xlsztf66F+8euuxzKKo36KQvoS7f/hDbjQ4iYmtrHjvc1p9" +
                "1Nn6IAhAKWJk+QKiUoLDKAIA9sJTUKoas9IXoD/4UH8xueolRCeELAHlKhACGN98Ei3NoZAGnGah1oO/tykdgDTgkEa95NaAQkEo" +
                "EzCM9u2yUBU4VMUW0NkUhk64fAG4fAFbQNHsHh6teKgkYJgKlMDYRka0BPwfB/Yneh0AnkhMXpPkI5cAAAAASUVORK5CYII=";

        COMPENSATIONHANDLER_ICON = "data:image/png;base64," +
                "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAACXBIWXMAAAsTAAALEwEAmpwYAAAKTWlDQ1BQaG90b3Nob3AgSUND" +
                "IHByb2ZpbGUAAHjanVN3WJP3Fj7f92UPVkLY8LGXbIEAIiOsCMgQWaIQkgBhhBASQMWFiApWFBURnEhVxILVCkidiOKgKLhnQYqI" +
                "WotVXDjuH9yntX167+3t+9f7vOec5/zOec8PgBESJpHmomoAOVKFPDrYH49PSMTJvYACFUjgBCAQ5svCZwXFAADwA3l4fnSwP/wB" +
                "r28AAgBw1S4kEsfh/4O6UCZXACCRAOAiEucLAZBSAMguVMgUAMgYALBTs2QKAJQAAGx5fEIiAKoNAOz0ST4FANipk9wXANiiHKkI" +
                "AI0BAJkoRyQCQLsAYFWBUiwCwMIAoKxAIi4EwK4BgFm2MkcCgL0FAHaOWJAPQGAAgJlCLMwAIDgCAEMeE80DIEwDoDDSv+CpX3CF" +
                "uEgBAMDLlc2XS9IzFLiV0Bp38vDg4iHiwmyxQmEXKRBmCeQinJebIxNI5wNMzgwAABr50cH+OD+Q5+bk4eZm52zv9MWi/mvwbyI+" +
                "IfHf/ryMAgQAEE7P79pf5eXWA3DHAbB1v2upWwDaVgBo3/ldM9sJoFoK0Hr5i3k4/EAenqFQyDwdHAoLC+0lYqG9MOOLPv8z4W/g" +
                "i372/EAe/tt68ABxmkCZrcCjg/1xYW52rlKO58sEQjFu9+cj/seFf/2OKdHiNLFcLBWK8ViJuFAiTcd5uVKRRCHJleIS6X8y8R+W" +
                "/QmTdw0ArIZPwE62B7XLbMB+7gECiw5Y0nYAQH7zLYwaC5EAEGc0Mnn3AACTv/mPQCsBAM2XpOMAALzoGFyolBdMxggAAESggSqw" +
                "QQcMwRSswA6cwR28wBcCYQZEQAwkwDwQQgbkgBwKoRiWQRlUwDrYBLWwAxqgEZrhELTBMTgN5+ASXIHrcBcGYBiewhi8hgkEQcgI" +
                "E2EhOogRYo7YIs4IF5mOBCJhSDSSgKQg6YgUUSLFyHKkAqlCapFdSCPyLXIUOY1cQPqQ28ggMor8irxHMZSBslED1AJ1QLmoHxqK" +
                "xqBz0XQ0D12AlqJr0Rq0Hj2AtqKn0UvodXQAfYqOY4DRMQ5mjNlhXIyHRWCJWBomxxZj5Vg1Vo81Yx1YN3YVG8CeYe8IJAKLgBPs" +
                "CF6EEMJsgpCQR1hMWEOoJewjtBK6CFcJg4Qxwicik6hPtCV6EvnEeGI6sZBYRqwm7iEeIZ4lXicOE1+TSCQOyZLkTgohJZAySQtJ" +
                "a0jbSC2kU6Q+0hBpnEwm65Btyd7kCLKArCCXkbeQD5BPkvvJw+S3FDrFiOJMCaIkUqSUEko1ZT/lBKWfMkKZoKpRzame1AiqiDqf" +
                "WkltoHZQL1OHqRM0dZolzZsWQ8ukLaPV0JppZ2n3aC/pdLoJ3YMeRZfQl9Jr6Afp5+mD9HcMDYYNg8dIYigZaxl7GacYtxkvmUym" +
                "BdOXmchUMNcyG5lnmA+Yb1VYKvYqfBWRyhKVOpVWlX6V56pUVXNVP9V5qgtUq1UPq15WfaZGVbNQ46kJ1Bar1akdVbupNq7OUndS" +
                "j1DPUV+jvl/9gvpjDbKGhUaghkijVGO3xhmNIRbGMmXxWELWclYD6yxrmE1iW7L57Ex2Bfsbdi97TFNDc6pmrGaRZp3mcc0BDsax" +
                "4PA52ZxKziHODc57LQMtPy2x1mqtZq1+rTfaetq+2mLtcu0W7eva73VwnUCdLJ31Om0693UJuja6UbqFutt1z+o+02PreekJ9cr1" +
                "Dund0Uf1bfSj9Rfq79bv0R83MDQINpAZbDE4Y/DMkGPoa5hpuNHwhOGoEctoupHEaKPRSaMnuCbuh2fjNXgXPmasbxxirDTeZdxr" +
                "PGFiaTLbpMSkxeS+Kc2Ua5pmutG003TMzMgs3KzYrMnsjjnVnGueYb7ZvNv8jYWlRZzFSos2i8eW2pZ8ywWWTZb3rJhWPlZ5VvVW" +
                "16xJ1lzrLOtt1ldsUBtXmwybOpvLtqitm63Edptt3xTiFI8p0in1U27aMez87ArsmuwG7Tn2YfYl9m32zx3MHBId1jt0O3xydHXM" +
                "dmxwvOuk4TTDqcSpw+lXZxtnoXOd8zUXpkuQyxKXdpcXU22niqdun3rLleUa7rrStdP1o5u7m9yt2W3U3cw9xX2r+00umxvJXcM9" +
                "70H08PdY4nHM452nm6fC85DnL152Xlle+70eT7OcJp7WMG3I28Rb4L3Le2A6Pj1l+s7pAz7GPgKfep+Hvqa+It89viN+1n6Zfgf8" +
                "nvs7+sv9j/i/4XnyFvFOBWABwQHlAb2BGoGzA2sDHwSZBKUHNQWNBbsGLww+FUIMCQ1ZH3KTb8AX8hv5YzPcZyya0RXKCJ0VWhv6" +
                "MMwmTB7WEY6GzwjfEH5vpvlM6cy2CIjgR2yIuB9pGZkX+X0UKSoyqi7qUbRTdHF09yzWrORZ+2e9jvGPqYy5O9tqtnJ2Z6xqbFJs" +
                "Y+ybuIC4qriBeIf4RfGXEnQTJAntieTE2MQ9ieNzAudsmjOc5JpUlnRjruXcorkX5unOy553PFk1WZB8OIWYEpeyP+WDIEJQLxhP" +
                "5aduTR0T8oSbhU9FvqKNolGxt7hKPJLmnVaV9jjdO31D+miGT0Z1xjMJT1IreZEZkrkj801WRNberM/ZcdktOZSclJyjUg1plrQr" +
                "1zC3KLdPZisrkw3keeZtyhuTh8r35CP5c/PbFWyFTNGjtFKuUA4WTC+oK3hbGFt4uEi9SFrUM99m/ur5IwuCFny9kLBQuLCz2Lh4" +
                "WfHgIr9FuxYji1MXdy4xXVK6ZHhp8NJ9y2jLspb9UOJYUlXyannc8o5Sg9KlpUMrglc0lamUycturvRauWMVYZVkVe9ql9VbVn8q" +
                "F5VfrHCsqK74sEa45uJXTl/VfPV5bdra3kq3yu3rSOuk626s91m/r0q9akHV0IbwDa0b8Y3lG19tSt50oXpq9Y7NtM3KzQM1YTXt" +
                "W8y2rNvyoTaj9nqdf13LVv2tq7e+2Sba1r/dd3vzDoMdFTve75TsvLUreFdrvUV99W7S7oLdjxpiG7q/5n7duEd3T8Wej3ulewf2" +
                "Re/ranRvbNyvv7+yCW1SNo0eSDpw5ZuAb9qb7Zp3tXBaKg7CQeXBJ9+mfHvjUOihzsPcw83fmX+39QjrSHkr0jq/dawto22gPaG9" +
                "7+iMo50dXh1Hvrf/fu8x42N1xzWPV56gnSg98fnkgpPjp2Snnp1OPz3Umdx590z8mWtdUV29Z0PPnj8XdO5Mt1/3yfPe549d8Lxw" +
                "9CL3Ytslt0utPa49R35w/eFIr1tv62X3y+1XPK509E3rO9Hv03/6asDVc9f41y5dn3m978bsG7duJt0cuCW69fh29u0XdwruTNxd" +
                "eo94r/y+2v3qB/oP6n+0/rFlwG3g+GDAYM/DWQ/vDgmHnv6U/9OH4dJHzEfVI0YjjY+dHx8bDRq98mTOk+GnsqcTz8p+Vv9563Or" +
                "59/94vtLz1j82PAL+YvPv655qfNy76uprzrHI8cfvM55PfGm/K3O233vuO+638e9H5ko/ED+UPPR+mPHp9BP9z7nfP78L/eE8/sl" +
                "0p8zAAAABGdBTUEAALGOfPtRkwAAACBjSFJNAAB6JQAAgIMAAPn/AACA6QAAdTAAAOpgAAA6mAAAF2+SX8VGAAAC4UlEQVR42qyV" +
                "z2tcVRTHP+fe92aazIy6MdXOTEYnKamVBjdSuhCRGuxGERWXduG/4MIqZFIp2BbUhQtFBLFuXIm4UlGDRSjxx8KWVrJoVExsRVxY" +
                "NJ375t1zXLzJm4lZpnf17uV7v+d7zvme+8TMuJUrAVi5uGEXfrqxK6Ij993G4fmmYGa88eEV2+0achQKzYwsi6yt/b4jcqt1J+fP" +
                "tMn6yqO9DdbX/9yB6Xb3YdgoZQxUjTzPASkOgE7nbr452+bYC+8B8HGvycLSBr/+cr3ASIFVNUyLOw5ATVE1QhbJskgIkVZrLxde" +
                "n+bY4g9sWpVr377Nk4vv81lvH632FCGLhCwnC7EgtDFCMyNGJesP6PczOp29fP/mvSy8tMI/Ny6DRhrNQ6yfP81TvXN83mvR6dxF" +
                "1h8QwoAYC0EjharE3AhhwMxMkx/fmuXoy1fY/PsSTgeIZvipI9w+fT/ryyd55pVzfHGyTbfbLAhzw0zHFKoRVZk7MM3ld/dz9MR3" +
                "iP5L7Y4HmGh0kBi4eX2Zxj2P03qkB8Czpz7gq1Md5g5ME1XLlJNCYbHZ8viXrz6IDc8XTiwjeR8XbgLw0dJzqI7uDPtX7oe2UZwT" +
                "nIODz6+CgSFcemc/SAWJfRgUhAg89OLPCAYIJoY4wVTHFSreO65evVaGnJtrlz6TGEiyzVJRmjpWV38r1R2an0HHCc2MNHXUatXR" +
                "TCZ++OUhBlwMpcIk8QVWiy6kqUPHaxhVSRJPvT4JKAhUKkNCUUQzCKEMVql46o0JMIegJIkrjZ1sNcN7oV7fUw7KSCGI5jjNt6lv" +
                "1CfABMPw3v0vZVVEhFqtUuQk4BM3zLBCOnucdPZ4Sei9Y3JyzyigyHbbmCkiUK1WSpATYWHxDz5dmkIjhEyxCE+/9hcA1Wo6Rsj2" +
                "Gm6Nnvdu5KtoOO94bGnn66JR8c4V5QFi1NLEYmasXNywT75e29UD+8TDXQ7PN0Vu9S/gvwEABzWu41yZrVcAAAAASUVORK5CYII=";

        ELSE_ICON = "data:image/png;base64," +
                "iVBORw0KGgoAAAANSUhEUgAAAEMAAABECAIAAABVrR+cAAAAhnpUWHRSYXcgcHJvZmlsZSB0eXBlIGV4aWYAAHjaXY7ZCYRQDEX/" +
                "U8WUkO1lKUdEwQ4sfxKe4oznI7lcwiGwnccOn4aQQYeHpRkWmpq8VAicCCIxUu+ak2sLVeKnBuEZLMNRn0O9+pshFra7utuw1VYu" +
                "O29CEjVb1FbsN/JHIvdHr17/5fAFQb0sH7LyzWAAAAoCaVRYdFhNTDpjb20uYWRvYmUueG1wAAAAAAA8P3hwYWNrZXQgYmVnaW49" +
                "Iu+7vyIgaWQ9Ilc1TTBNcENlaGlIenJlU3pOVGN6a2M5ZCI/Pgo8eDp4bXBtZXRhIHhtbG5zOng9ImFkb2JlOm5zOm1ldGEvIiB4" +
                "OnhtcHRrPSJYTVAgQ29yZSA0LjQuMC1FeGl2MiI+CiA8cmRmOlJERiB4bWxuczpyZGY9Imh0dHA6Ly93d3cudzMub3JnLzE5OTkv" +
                "MDIvMjItcmRmLXN5bnRheC1ucyMiPgogIDxyZGY6RGVzY3JpcHRpb24gcmRmOmFib3V0PSIiCiAgICB4bWxuczpleGlmPSJodHRw" +
                "Oi8vbnMuYWRvYmUuY29tL2V4aWYvMS4wLyIKICAgIHhtbG5zOnRpZmY9Imh0dHA6Ly9ucy5hZG9iZS5jb20vdGlmZi8xLjAvIgog" +
                "ICBleGlmOlBpeGVsWERpbWVuc2lvbj0iNjciCiAgIGV4aWY6UGl4ZWxZRGltZW5zaW9uPSI2OCIKICAgdGlmZjpJbWFnZVdpZHRo" +
                "PSI2NyIKICAgdGlmZjpJbWFnZUhlaWdodD0iNjgiCiAgIHRpZmY6T3JpZW50YXRpb249IjEiLz4KIDwvcmRmOlJERj4KPC94Onht" +
                "cG1ldGE+CiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAog" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "IAogICAgICAgICAgICAgICAgICAgICAgICAgICAKPD94cGFja2V0IGVuZD0idyI/PrVsZ8kAAAADc0JJVAgICNvhT+AAAAQDSURB" +
                "VGje7dtdSFNRHADw/+acq/xAqqVCI1plIYiQOaMeMs25cs7ayEKCnE9RoSNfgsgXLSrMCiFIBZ/cUxYKCxFKrWBO7IOiZDqL6qGM" +
                "wFkhcx+nh5XF3O7Ovfece6/medsO/O9+O/9zP/7nXBlCCJZFk8NyaSuS/1MyOTlZU1PjdDqXtmRiYqK4uDglJcVkMrlcLopHQjSb" +
                "2+3WaDSdnZ0IIYfDoVarR0ZGKB0LhGGEG1UMCMagjQEhGVQxIDCDHgaEZ1DCgCgMGhgQi0EcAyIyyGJAXAZBDIjOIIUBKTCIYEAi" +
                "DP4YkA6DJwYkxeCDAakxOGNAggxuGJAmgwMGJMtgiwEpM1hhQOIMfAxIn4GJAVaMwKf2/GgFmo3nxuYWeldV9s9GhvJ9cDQdL9yU" +
                "KgMAAFVGbsX5u545jJj4GAVDnaqkpKSxsdFqtS7qTN9VtjcjceFjoiYvPYGhEjX37GLRwSseWKcz1+5UB98P2h29l82vA6Ovruav" +
                "xo9pMBi6urqMRmNfX19BQQFWvStWUjH86wy98+5rOQCQedr5M/zFd1ezpeywtWngSyBeTPw0A1Zzg5sk+Lm7SAEAmurbw+++B1jF" +
                "xMcAqykeI6dzb0z5mX5T6MfL62Xr/9Rv12bvOXLm2v03s0GMmPiYyHlSW1vb0NAQbW7EnieqHdvWMNaXZWtybY6Px8Ye3OsbGHw8" +
                "9PBRT9vTnrabJ3vHOoxqbjENBkN7e3tVVZXH45HL5VHmic1m0+l0MzMzTGPCMrsiW3DG1ZwvB4CCjk8BLtmFEPL5fEaj0WKxBIPh" +
                "oUWR7paWlsLCQr1e7/V6CdXQ/VN3TNpUZUZ179dQOL9St+RtTgEAvy/AKeL8/LzZbE5KSrLb7b8HBCAyu2QyWWtrq81m0+v1/f39" +
                "aWlp0c6qQxeOVrT9PWOCcov11tXKDVF7k7aeOLsvy9f7pNukfb7/QF6mwusedox6QbH7ZGlWQpyYWYroDJVKZbfbFQpFnLNwKBSq" +
                "q6tbnGaxrmKQ3fTWF6t3+6XxOe+LLtuh3AzVb5w6p/TUbee3YPyYi5OqvLzcYrH4/X7ca3wsjIiNgRHnbkVSGGZG/DtIiWDiMrDu" +
                "6kXH4DBwn7RExGAyWDz9ioLBZ7CrSAiMYcVgXSUSDMOWwaVyJwCGA4NjNZUqhhuDe4WbEoYzA/FZdSCO4cNAPFeCCGJ4MhD/1Tki" +
                "GP4MRGTFlCeGCAORWsXmjCHFQAR3FnDAEGQgsrs9WGHIMhDxHTiYGOIMRGNXVFwMDQaitFONAUOJgejtHoyKocdAVHd0RmCoMuhK" +
                "/sVMT09TZVCXhDH19fXJyclUGQghGaL/1gZCaHx8XKvVKpVKekcRQrLyrsOKZKm3X0TBI/AGtSPZAAAAAElFTkSuQmCC";

        ELSEIF_ICON = "data:image/png;base64," +
                "iVBORw0KGgoAAAANSUhEUgAAAFYAAABaCAIAAACt9jbbAAAAhnpUWHRSYXcgcHJvZmlsZSB0eXBlIGV4aWYAAHjaXY7bCcRACEX/" +
                "rSIl+Bof5YSQwHaw5UeZhGH3fOjlIgfh/H4u2BpCBh0elmZYaGryXiFwIojESL1rTp4tVIlXDcIzWIajrkN9+pchFna5utuwww4u" +
                "O59CEjVb1FbsN3JJhr0f/fX7rxxuRT8sUeNsQK0AAAoCaVRYdFhNTDpjb20uYWRvYmUueG1wAAAAAAA8P3hwYWNrZXQgYmVnaW49" +
                "Iu+7vyIgaWQ9Ilc1TTBNcENlaGlIenJlU3pOVGN6a2M5ZCI/Pgo8eDp4bXBtZXRhIHhtbG5zOng9ImFkb2JlOm5zOm1ldGEvIiB4" +
                "OnhtcHRrPSJYTVAgQ29yZSA0LjQuMC1FeGl2MiI+CiA8cmRmOlJERiB4bWxuczpyZGY9Imh0dHA6Ly93d3cudzMub3JnLzE5OTkv" +
                "MDIvMjItcmRmLXN5bnRheC1ucyMiPgogIDxyZGY6RGVzY3JpcHRpb24gcmRmOmFib3V0PSIiCiAgICB4bWxuczpleGlmPSJodHRw" +
                "Oi8vbnMuYWRvYmUuY29tL2V4aWYvMS4wLyIKICAgIHhtbG5zOnRpZmY9Imh0dHA6Ly9ucy5hZG9iZS5jb20vdGlmZi8xLjAvIgog" +
                "ICBleGlmOlBpeGVsWERpbWVuc2lvbj0iODYiCiAgIGV4aWY6UGl4ZWxZRGltZW5zaW9uPSI5MCIKICAgdGlmZjpJbWFnZVdpZHRo" +
                "PSI4NiIKICAgdGlmZjpJbWFnZUhlaWdodD0iOTAiCiAgIHRpZmY6T3JpZW50YXRpb249IjEiLz4KIDwvcmRmOlJERj4KPC94Onht" +
                "cG1ldGE+CiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAog" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "IAogICAgICAgICAgICAgICAgICAgICAgICAgICAKPD94cGFja2V0IGVuZD0idyI/PsM5uM8AAAADc0JJVAgICNvhT+AAAAYFSURB" +
                "VHja7dx9TBNnHAfwXwURJpKYrXdXiMuGbm4xKQ0tAm3NXqRKICquy7JlWTSa2PJSpIAhm8v+Yi9gYvZiMrPNBJe4JZsFbOXaarLp" +
                "HIiU12wZw0Vj9hJjlrXXAiFg6bM/xIrY93uHe/6Du3vS7+ee9tp7fs/JEEKwstsqWPFNIpAIJAKJQCKQCCQCARAEg8FQKLRyCdxu" +
                "d15enslk4v3rKT8ELpdr3759Z86cmZiYMJvNPCsgzpvT6cRx/OrVqwihycnJbdu2HTp0KBQKIZ4acJ8fw7D+/v7wf3hXAH7zC0GB" +
                "OwKSJDEMu3btWsStPCqAEPLzqwACyc+jAnCQXy6XDwwMJLg/9wogqPy8KLBI0NPTI5fLPR5PCsdyqQACzM+xAisE58+fxzCMTn4u" +
                "FUCw+TlTACHn50aBSQKHw4Fh2ODgIOOvklUFEH5+thWYIbDb7TiOs5efVQUQS372FEBE+VlSoEVw7tw5HMeHhoY4/nnLrAKILj/j" +
                "CikSdHd3EwTBV35mFUCk+RlUAPHmZ0ohOYKuri6CIIaHh5GQGk0FEHt++gqJEnR2dioUCmHmp6kAyyM/HQVYNvlTVohDYLPZFArF" +
                "yMgIEk9LVgESyR/8+0tNpCnZDU1DMwgtbM2qcgeW9jD7J9n6RslTOTIAAMgklLvftt2YQQiFj4rS5+K2uP8oRxV33J5PWSE92oyz" +
                "zWarr68nSVKlUs3/MwgAAOuLyvXE6vAuq59UrU+LMWk9M/zeSxVtN+CJYuNBNTZ/69K3pP1D469Bzy/tmsfu75RknxGPyip4OlO2" +
                "eHN2djZJkhUVFWaz+eTJkzKZLOnJ9bNnz+bm5obHf4zzHGPr3PVjWwBAUds/vXBuBt5/tXzvgdaLd4Lx+owzChI4KvGxsCra+e/p" +
                "6VGpVHQqF9Jy8rB0gNuOz76+cmtqHiC76J3vnZ2njpZhaZyUTtwbC+Pj43GqOB49/wqFYnR0NMJ5WNqUH9+8G2uMhKbGjpfL75ez" +
                "PL5Z90rdse7fAvMJ9Bnvs2B9UfmucNtrOvXHbOpj4aHPgrGxMYvF4nQ6CwoK4r8DM59/dm3MOh3ZWqWV/Ov1IWeX4+KlK5d/+LHz" +
                "RG/niU/224e+2oWl2OdC83lcjgd/ERtajh/YlBFrLFRWVra3t7e0tDy6w0MEMpksFApNT09H7izrhdbvunasS2owytYoNFU1mqoa" +
                "gJDf81FZydHBjlZHa8VBIvU+AbKq3HeSOGpubm5qaiojIyN+uZVSqTx9+rTRaOzr66P9Trx784s9G3MyiDft/94rrFuVs0mVvw4A" +
                "7s4GOSym8nq9BoOhrKzMarVG3GHpRXHnzp0dHR1Go9Fms2m12ocvcpfffW33iQcXMMjYdODT9io84tY1z7xleTF31v7zN3s2jrxs" +
                "UCnS/dd/Ij1+SC/dvyM3LU6fuenM5Pf5fAaDYfv27W1tbcldFF0uF0EQvb29MT8OATa3js9G2/rcB7/P+Ec7rJVKInNBBduyo/rz" +
                "/v/m4/fJyEXR6/UWFhYeOXIkxW+HSxRE1xLMH+cLsngVvF6vWq1ubm5m4GeSGBV8Pp9arW5qamLsx7K4FJLNn+gtE7EopJA/iRtn" +
                "wlfw+XwajaaxsZHF26dCVqAoSqPRWK1W1m+iC1OBTv5UplKEpkAzP0ptQk04ChRFFRUVNTQ00OkkxWlVISgwkh/RmVznV4Gp/Ihm" +
                "iQVfChRFbd26taGhgecSC74UKIoqLi4+fPiwIAptwgo4jvf19YkxP2Kq6I4bBTbyIwZLL9lWoCiqpKSkvr5eoKWXbCtQFFVaWmqx" +
                "WARdgMueAqv5ERvF+MwqsJ0fsbQkgykFiqK0Wm1dXZ3IlmQwpcBNfsTq8iw6Cn6/X6vV1tbWinh5Fh0FLvMjDpZqJqvg9/t1Ol1N" +
                "Tc0yWaqZrAL3+RFny7YTUQgEAjqdrrq6ehku205Ega/8iONHOERTCAQCer3ebDYv80c4RFPgNz8PBEsUJicn9Xq9yWRaQY9zWaxw" +
                "4cIF3vPzRoAQcrvd+fn5PI7/cJMh6dmn0mPeJAKJQCKQCAAA/ge76rTpNcCIOQAAAABJRU5ErkJggg==";

        EVENTHANDLER_ICON = "data:image/png;base64," +
                "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAABGdBTUEAAK/INwWK6QAAABl0RVh0U29mdHdhcmUAQWRvYmUgSW1h" +
                "Z2VSZWFkeXHJZTwAAAJkSURBVHjarJW9axRBGMaf2ZnZLz8KMUK0ueIqP0BEC6/wI1UaiwPRFCqkDBKERAt7W4WgIPb+DSJYSFBE" +
                "VESIpDo0Vl6OcHDRu7DZ21nfmVn3dnNnIHhzzMLczPzmeZ/3nV2WpinG2YR+fF5dS79+XP4v0MlzF3HmeIUZoIbVarU9Q/7G1m63" +
                "DYOAVqFuv7tdVKtVu7Bgw3qzufeQ9fYgCNDpdAzswYebWH6/irPVI1i6Wrai1+sNFGYHu1Lmai1QKQKGcF0XURShu7FhJj81Wph6" +
                "eALCdczYkxwvbq/kwC5FpZvv+8RIBsAkScC5A0d3x8HhyaOYpv7yzRfMzz1CK2rix9Y3tLZ/ov7sNB0sMBH6eHL9rRHAhSCG1WiO" +
                "ViRdkuzWesuEfefyc4TSK5vDsg2uh/hUBZtx34w1TEemKMpcoUoURDZhPHEEHOaOTmnWojg1EMG5EZPsDFkDtRfGEwIKub+gLn/k" +
                "zZOM9nBjkVGYFIGqrFA4HIH0MXuljsdPF0pJCcIwh3JaxxjLFBY8tEnhBqj7ZtzD/Pkl8tHHscokZmdu4N3973h9rwG1HUGurFnh" +
                "BNQe6rBLChXRufHCKgwpDMFoMfNIgaDuglEF/Iq38GqxMVSHDoWuMoVZUpLMC5nJZpQUhoPhIXikIHCkGQfCRZzGNEsA+qGf5qGX" +
                "kqJSC9Td/mlvJJcBlYlL9embTQH14XtG3H7fVMqgDpPR93KfDE22BT+w+/2lKFRaSMrY34c2MWpocmpiBrVLdfJS/HPNSOCF6ToW" +
                "F+5SHbDCxUh3lPLu78Vrt+ayGzrmT8AfAQYAGSjcutqp62gAAAAASUVORK5CYII=";

        EXIT_ICON = "data:image/png;base64," +
                "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAABGdBTUEAAK/INwWK6QAAABl0RVh0U29mdHdhcmUAQWRvYmUgSW1h" +
                "Z2VSZWFkeXHJZTwAAAFpSURBVHjazFQtT8RAEH1zbXMYLAqFOBKS8/wACAkKwQ/AkCAJCoPhFAkCSzD8AASKhMAPQIE7icJwAsFd" +
                "Snrd7rCz/aAcXdKSCjbZzO7M6+ub3ZklZkabo4OWh1/ejO5u+Pn8zK7JzEJ7KQnrp2zNaWhpbx8L65uU+kopP2yt8crObmNVw8sL" +
                "rF7f0w+FnGhrX65ua5Mtbm8ASjvOMNZ/OjdWiYNQArohqeBjh0KeJuBINVNn8NqVskjnMGpGKPipqi4bvIYYPw4Rj95qEwoe75GD" +
                "MCCQ+SPHcT11UneisJSyP3vL2vOhP6K0eM0XZKwt1Xyf13lW3IKH4uoz1B4w3182lsAdM2csjLWxfG+m4NnjaoVspNNcF8l4kuWT" +
                "+TNlsGpTjWSkS5cJnn9LmboB1CT83suzvV3yCd5JKK1HQYD+4LBZcScOwt7JMZ4OjopnpLiQsiYTy9WmMELvdPCF+PcPbOuEnwIM" +
                "AEb/pzyAd5HvAAAAAElFTkSuQmCC";

        FAULTHANDLER_ICON = "data:image/png;base64," +
                "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAACXBIWXMAAAsTAAALEwEAmpwYAAAKTWlDQ1BQaG90b3Nob3AgSUND" +
                "IHByb2ZpbGUAAHjanVN3WJP3Fj7f92UPVkLY8LGXbIEAIiOsCMgQWaIQkgBhhBASQMWFiApWFBURnEhVxILVCkidiOKgKLhnQYqI" +
                "WotVXDjuH9yntX167+3t+9f7vOec5/zOec8PgBESJpHmomoAOVKFPDrYH49PSMTJvYACFUjgBCAQ5svCZwXFAADwA3l4fnSwP/wB" +
                "r28AAgBw1S4kEsfh/4O6UCZXACCRAOAiEucLAZBSAMguVMgUAMgYALBTs2QKAJQAAGx5fEIiAKoNAOz0ST4FANipk9wXANiiHKkI" +
                "AI0BAJkoRyQCQLsAYFWBUiwCwMIAoKxAIi4EwK4BgFm2MkcCgL0FAHaOWJAPQGAAgJlCLMwAIDgCAEMeE80DIEwDoDDSv+CpX3CF" +
                "uEgBAMDLlc2XS9IzFLiV0Bp38vDg4iHiwmyxQmEXKRBmCeQinJebIxNI5wNMzgwAABr50cH+OD+Q5+bk4eZm52zv9MWi/mvwbyI+" +
                "IfHf/ryMAgQAEE7P79pf5eXWA3DHAbB1v2upWwDaVgBo3/ldM9sJoFoK0Hr5i3k4/EAenqFQyDwdHAoLC+0lYqG9MOOLPv8z4W/g" +
                "i372/EAe/tt68ABxmkCZrcCjg/1xYW52rlKO58sEQjFu9+cj/seFf/2OKdHiNLFcLBWK8ViJuFAiTcd5uVKRRCHJleIS6X8y8R+W" +
                "/QmTdw0ArIZPwE62B7XLbMB+7gECiw5Y0nYAQH7zLYwaC5EAEGc0Mnn3AACTv/mPQCsBAM2XpOMAALzoGFyolBdMxggAAESggSqw" +
                "QQcMwRSswA6cwR28wBcCYQZEQAwkwDwQQgbkgBwKoRiWQRlUwDrYBLWwAxqgEZrhELTBMTgN5+ASXIHrcBcGYBiewhi8hgkEQcgI" +
                "E2EhOogRYo7YIs4IF5mOBCJhSDSSgKQg6YgUUSLFyHKkAqlCapFdSCPyLXIUOY1cQPqQ28ggMor8irxHMZSBslED1AJ1QLmoHxqK" +
                "xqBz0XQ0D12AlqJr0Rq0Hj2AtqKn0UvodXQAfYqOY4DRMQ5mjNlhXIyHRWCJWBomxxZj5Vg1Vo81Yx1YN3YVG8CeYe8IJAKLgBPs" +
                "CF6EEMJsgpCQR1hMWEOoJewjtBK6CFcJg4Qxwicik6hPtCV6EvnEeGI6sZBYRqwm7iEeIZ4lXicOE1+TSCQOyZLkTgohJZAySQtJ" +
                "a0jbSC2kU6Q+0hBpnEwm65Btyd7kCLKArCCXkbeQD5BPkvvJw+S3FDrFiOJMCaIkUqSUEko1ZT/lBKWfMkKZoKpRzame1AiqiDqf" +
                "WkltoHZQL1OHqRM0dZolzZsWQ8ukLaPV0JppZ2n3aC/pdLoJ3YMeRZfQl9Jr6Afp5+mD9HcMDYYNg8dIYigZaxl7GacYtxkvmUym" +
                "BdOXmchUMNcyG5lnmA+Yb1VYKvYqfBWRyhKVOpVWlX6V56pUVXNVP9V5qgtUq1UPq15WfaZGVbNQ46kJ1Bar1akdVbupNq7OUndS" +
                "j1DPUV+jvl/9gvpjDbKGhUaghkijVGO3xhmNIRbGMmXxWELWclYD6yxrmE1iW7L57Ex2Bfsbdi97TFNDc6pmrGaRZp3mcc0BDsax" +
                "4PA52ZxKziHODc57LQMtPy2x1mqtZq1+rTfaetq+2mLtcu0W7eva73VwnUCdLJ31Om0693UJuja6UbqFutt1z+o+02PreekJ9cr1" +
                "Dund0Uf1bfSj9Rfq79bv0R83MDQINpAZbDE4Y/DMkGPoa5hpuNHwhOGoEctoupHEaKPRSaMnuCbuh2fjNXgXPmasbxxirDTeZdxr" +
                "PGFiaTLbpMSkxeS+Kc2Ua5pmutG003TMzMgs3KzYrMnsjjnVnGueYb7ZvNv8jYWlRZzFSos2i8eW2pZ8ywWWTZb3rJhWPlZ5VvVW" +
                "16xJ1lzrLOtt1ldsUBtXmwybOpvLtqitm63Edptt3xTiFI8p0in1U27aMez87ArsmuwG7Tn2YfYl9m32zx3MHBId1jt0O3xydHXM" +
                "dmxwvOuk4TTDqcSpw+lXZxtnoXOd8zUXpkuQyxKXdpcXU22niqdun3rLleUa7rrStdP1o5u7m9yt2W3U3cw9xX2r+00umxvJXcM9" +
                "70H08PdY4nHM452nm6fC85DnL152Xlle+70eT7OcJp7WMG3I28Rb4L3Le2A6Pj1l+s7pAz7GPgKfep+Hvqa+It89viN+1n6Zfgf8" +
                "nvs7+sv9j/i/4XnyFvFOBWABwQHlAb2BGoGzA2sDHwSZBKUHNQWNBbsGLww+FUIMCQ1ZH3KTb8AX8hv5YzPcZyya0RXKCJ0VWhv6" +
                "MMwmTB7WEY6GzwjfEH5vpvlM6cy2CIjgR2yIuB9pGZkX+X0UKSoyqi7qUbRTdHF09yzWrORZ+2e9jvGPqYy5O9tqtnJ2Z6xqbFJs" +
                "Y+ybuIC4qriBeIf4RfGXEnQTJAntieTE2MQ9ieNzAudsmjOc5JpUlnRjruXcorkX5unOy553PFk1WZB8OIWYEpeyP+WDIEJQLxhP" +
                "5aduTR0T8oSbhU9FvqKNolGxt7hKPJLmnVaV9jjdO31D+miGT0Z1xjMJT1IreZEZkrkj801WRNberM/ZcdktOZSclJyjUg1plrQr" +
                "1zC3KLdPZisrkw3keeZtyhuTh8r35CP5c/PbFWyFTNGjtFKuUA4WTC+oK3hbGFt4uEi9SFrUM99m/ur5IwuCFny9kLBQuLCz2Lh4" +
                "WfHgIr9FuxYji1MXdy4xXVK6ZHhp8NJ9y2jLspb9UOJYUlXyannc8o5Sg9KlpUMrglc0lamUycturvRauWMVYZVkVe9ql9VbVn8q" +
                "F5VfrHCsqK74sEa45uJXTl/VfPV5bdra3kq3yu3rSOuk626s91m/r0q9akHV0IbwDa0b8Y3lG19tSt50oXpq9Y7NtM3KzQM1YTXt" +
                "W8y2rNvyoTaj9nqdf13LVv2tq7e+2Sba1r/dd3vzDoMdFTve75TsvLUreFdrvUV99W7S7oLdjxpiG7q/5n7duEd3T8Wej3ulewf2" +
                "Re/ranRvbNyvv7+yCW1SNo0eSDpw5ZuAb9qb7Zp3tXBaKg7CQeXBJ9+mfHvjUOihzsPcw83fmX+39QjrSHkr0jq/dawto22gPaG9" +
                "7+iMo50dXh1Hvrf/fu8x42N1xzWPV56gnSg98fnkgpPjp2Snnp1OPz3Umdx590z8mWtdUV29Z0PPnj8XdO5Mt1/3yfPe549d8Lxw" +
                "9CL3Ytslt0utPa49R35w/eFIr1tv62X3y+1XPK509E3rO9Hv03/6asDVc9f41y5dn3m978bsG7duJt0cuCW69fh29u0XdwruTNxd" +
                "eo94r/y+2v3qB/oP6n+0/rFlwG3g+GDAYM/DWQ/vDgmHnv6U/9OH4dJHzEfVI0YjjY+dHx8bDRq98mTOk+GnsqcTz8p+Vv9563Or" +
                "59/94vtLz1j82PAL+YvPv655qfNy76uprzrHI8cfvM55PfGm/K3O233vuO+638e9H5ko/ED+UPPR+mPHp9BP9z7nfP78L/eE8/sl" +
                "0p8zAAAABGdBTUEAALGOfPtRkwAAACBjSFJNAAB6JQAAgIMAAPn/AACA6QAAdTAAAOpgAAA6mAAAF2+SX8VGAAACTUlEQVR42qyU" +
                "P0gbYRTAf3fRIydJRFNCBwe3giCetkE6WBApCKWQRaFTkZrByUUKbqXQpVvHUDt0KAUJRbBFqCRD6CKddJCObqX1TyomJrnLfa/D" +
                "mS+5WIeKDx68e997v3vve4/PEBFuUnpaxsn7ETnePbo2KDl2i8Gn+4YGHu8eMbz8+trAgzfPGeyssCW1g9J/w+zhB9o2teULoqpI" +
                "09Macx6Hvq/0qyr40gUERClU7RxVOyc+PkNuZI74+Iz2XeUXpS5XKCKI52J6TUyvCb5HOpUkN7pI/+Qsptekf3KW3Ogi6VQSfE/H" +
                "iufS2pZ2hUrAd1GNOqpR57SQZ2JrNYA6S8TvT5NzlkinkkxsrXJayOtYfDfIDwMV0qjgnlW1Hm7mcTayAOTurpBOJXE2shxu5kNx" +
                "0qjARdvtKfvg18q4fyqhCUrtUNtjHzP8XP98acp+rQx+12L7Iqh6hWbV1YFDGYfdJxvtzPpZ6Fw3V6/gd9+hUgqUiwEYLdhCge+/" +
                "j1n8Mk86lWTt0TpDGUfHtBTlBvnhPYRIb5SePouh+SkNy24/49fXfZwPmTZ0foqePktrpDeqW26vjRKMaAwrYYM9EMC+rXBS2sNK" +
                "2JyU9nA+LZBOJXn78B2352axEjZWwsaIxpCLKes7FF8wLRvDjlIu7ZDdeUm5sI1hR3UT5dIOzuYyDoTOTMtG/C4gApgRMCJBcrGo" +
                "7U4pF4uB0XlmRoL8TqBSAlac+L3pC7rSQZcA/xDV3fLAnRg/Xry69vM1OBIP/nvTL7bJDcvfAQDSn0PT2oEIMQAAAABJRU5ErkJg" +
                "gg==";

        FLOW_ICON = "data:image/png;base64," +
                "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAABGdBTUEAAK/INwWK6QAAABl0RVh0U29mdHdhcmUAQWRvYmUgSW1h" +
                "Z2VSZWFkeXHJZTwAAAIeSURBVHjarFW/axVBEP5m8zA/CrHTIrxIIKmEFw0YAjYhJIKtjUQCqVRsJW3aIJhWSBWDYGcriIQ0KRKC" +
                "8PLQxkKS4N+Qlx/vbtzZnd27vUssJHPs3ezczrdz883sETPjOoUE8PfhH/7e+QnBJmtkexGRX+GMFd0+mOwae7EHwWTrHkbvDlND" +
                "1u0f/MCT2UeVrTy4c4bq4YVuCoUU7cvWjgDCAeY5YMhc8Q1lsLKZCo08hogHZHbGlx/28K19HJ3MWTfqef9gbS4yN9HE+tKUwygA" +
                "xdkYrDydxPz95qWBLm/s4O2ruZp9qI+crwboAbMcjoTmrX40HwwnDhwBgYWHI+j1MrVTvAeMIkKbAMcqUQ2JElg7N6TlQaEkEDAi" +
                "YI/Dwnr6iYRmg0AeBfJC2ZBH7XFCis0hUpaZAiA53abJjUJMLCeJNi8DZsrypZ8cyiaXiIwr6FA2VFqcJSzLxG5P5TbUXLXefMLQ" +
                "4A1Qdo7plc846Z7j4N1CqfBtYQvL6mtChIb854Vh9GVnzTrnGQYafe7ZWXuuUXsSJafim5UBPUHKcmUI+N7qM3RPL9yT4sYaod7S" +
                "TtGELb7fTjolEdsZd15s1szSKR9fz0QMT4ppuOm/OuUqkU5hxYiAnkFg7PZNN/5L8lKE7jDiolpCbljLiTmcfL7ZvK16EnFxwLZ/" +
                "HfHX3TaKXqIEPRy80ZsrupXH0y1MjI8QXfcv4K8AAwBnItulzrDa+QAAAABJRU5ErkJggg==";

        FOREACH_ICON = "data:image/png;base64," +
                "iVBORw0KGgoAAAANSUhEUgAAAGoAAABECAIAAADm5TeGAAAAhnpUWHRSYXcgcHJvZmlsZSB0eXBlIGV4aWYAAHjaVY7ZCYVADEX/" +
                "U4UlZJss5YgovA5e+SaMMng+ksslHALn/3fB1hAy6PCwNMNCU5P3CoETQSRG6l1z8myhSrxqEJ7BMhx1HerTvwyxsMvV3YYddnDZ" +
                "+RSSqNmitmK/kUti+/vRt1f9yuEGST8sT+6sdxIAAAoEaVRYdFhNTDpjb20uYWRvYmUueG1wAAAAAAA8P3hwYWNrZXQgYmVnaW49" +
                "Iu+7vyIgaWQ9Ilc1TTBNcENlaGlIenJlU3pOVGN6a2M5ZCI/Pgo8eDp4bXBtZXRhIHhtbG5zOng9ImFkb2JlOm5zOm1ldGEvIiB4" +
                "OnhtcHRrPSJYTVAgQ29yZSA0LjQuMC1FeGl2MiI+CiA8cmRmOlJERiB4bWxuczpyZGY9Imh0dHA6Ly93d3cudzMub3JnLzE5OTkv" +
                "MDIvMjItcmRmLXN5bnRheC1ucyMiPgogIDxyZGY6RGVzY3JpcHRpb24gcmRmOmFib3V0PSIiCiAgICB4bWxuczpleGlmPSJodHRw" +
                "Oi8vbnMuYWRvYmUuY29tL2V4aWYvMS4wLyIKICAgIHhtbG5zOnRpZmY9Imh0dHA6Ly9ucy5hZG9iZS5jb20vdGlmZi8xLjAvIgog" +
                "ICBleGlmOlBpeGVsWERpbWVuc2lvbj0iMTA2IgogICBleGlmOlBpeGVsWURpbWVuc2lvbj0iNjgiCiAgIHRpZmY6SW1hZ2VXaWR0" +
                "aD0iMTA2IgogICB0aWZmOkltYWdlSGVpZ2h0PSI2OCIKICAgdGlmZjpPcmllbnRhdGlvbj0iMSIvPgogPC9yZGY6UkRGPgo8L3g6" +
                "eG1wbWV0YT4KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "CiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgIAo8P3hwYWNrZXQgZW5kPSJ3Ij8+4abFYAAAAANzQklUCAgI2+FP4AAACDVJ" +
                "REFUeNrtmnlUU1cex7+P916CCcGEJWEVWUVkkYJKrdOIGdSKVUdbF3A5lqNTsKNVkWqxzpzqdHRcOx0RtS4dxdGxxaUqo+CCRXQA" +
                "sYgjRxTQEEQDAmEJiSR58wegCSUebZ3DnDn381eS9/L73Xzevfd3896lOI4D4ediQxQQfX0GY/5Gp9Pl5eURKS9m9OjRDNPljTKf" +
                "+1JSUrKyslxcXIgja6hUqtmzZ6empvbS+2pqalatWhUXF0c0WWPHjh2lpaVk7iOlg+gj+og+AtFH9BF9RB+B6CP6iD6ij0D0EX1E" +
                "H9FHIPqIPqLv/19fe9HHHlTvhG6rNAAATC1l362bHxPiKrShKIqiRJ5vTPzdVxdU+hcHocW+o+Z8kaXUv2JeykZxUmN2qr50tW/X" +
                "kTf+0tUkczrU+buX/2b4wP40RVEUxYi9R0xL2V/UYLRI1H/WxVazL2lOj2cpiqIG/+nO09fS+wT9JRbIhI+Krqt0+vuH5gYHvffZ" +
                "/pxbj7QcxdJAq+rG6b8uVvgMX5ZTb7IMwrcTiUQikchOwMKkqbxyMHVC6NS99zteIa9E4kZX5Jc86faku/23A5Vdr29s2/j9A53Z" +
                "lhNOW7p1gv9bC7ccL3zQbIINS8OouV+QuXH+sMBpeyrNzBjVJVfLmow9s2urCouU2hdtYmFeRp5wzB8Pr4m0Zy1c0wKu/MtpszOU" +
                "AOU5ITl1wdhACWvSPSw4vGnt/uK2m1unzAwoPvNhwPMg64+uiRAxNgA4vSp7feLn2Y2aM79fmzVux7vuPOpl8wIM36br7Pab+w4p" +
                "AXbQaI/yS1VVZ04XrBg/wKdf59H24s+nLstpBtiA91atmif3FtFce/WVAxvWHrpVd2JR3JfDLiz6pYOXecnzaGnwiMFi2vyj1ssJ" +
                "G4s5gA5eunF5rI/7oEAfmYilohVjhhhC3j1Y13b+z9tyJm+LNg8SEtUdJFJcuC97czVqS2/fqVW4eQmpl837nLbiPUceAuzQGXHy" +
                "47mbbiqzThXUxHr7CSgAzXmb0u8B4A9P+SJJMXDA4EAvJyFDyRVv+/Ln7dIEjBQp79Y79lnp0Ff882I9ADrsfbmLg39IgIuIpQCA" +
                "kcUkJ3gBQFXuDyW17b13fq5DZwAAnoD3VKMz/pwWtBTu/k4NsGETwn1j5kYAeHj25LVqrQkAdBXZ+RoA7NDJUVKnwBAfZyFDAaDY" +
                "ATP3nvt2w+K4X4e68qk+0/dUXa4GAEc/V4HE3dnWrCV8zwgvAECj6uGjlp/O5yat6tK2T/bVArANV/gKGJvef0bbhZVTo7ydHZ7j" +
                "9quNZd1zlubarhNPACZ0wlCxU+iUDyIBPM45cfWB1gSgo77iCQBIBjr3E7s5WE4OrIPfkCC/AVJ7pjvR0rFBEqa7PIknnjW8Rn09" +
                "olMU5TDvstam80ebTBwrtLUcXpyps2pQFNWhNXA9g9BCz+hPc7WA85jlS0Y6OkjtaGtdvLW5qdGM2opbRT9WazmAa7yy81QTwITG" +
                "hosdXB293pkXDqDuwvH8+20mAFRn8zgTWAH/v7U+e8m5j28nMr9+tIPu9g2tjwxoRUO5srXH2NMpi5QAQDl7SViqRxDO0NrazgH0" +
                "kMSdm6b7iWUB/o6stZK19ds14f16DnsDx4FrzE3PagFguJ2WNOcbWx4NrQoAGnKP51VODwrjywI7m1dZq+2xA5Qz6I00n6GsJmq7" +
                "tmLSykLj69JnXjS7ofj9/WNkGyoem24e+f52YkyAiNfduuaCXQeVADAoOtieL+gS+CyI/u7eBfP3Vhr/fTLn8eLpMYEi+oUlK2xU" +
                "L6XDpD6/45y281rV16rqzQ41Xc68fHdWcITPuNFO6yrqjSWZ5ysWKPxE3Y031Z2IC577Y9i8T7asn9V7Ik2rA/W6K+/zovlsSSr5" +
                "dMTuJf8y3tv52/nu9vtTot34XFtV9taFM9NrAUjeSVDIxFKRsWeQt8L/cScvcn15zeGVf5iiODjDg3nVMWN6fC79gh5A4LIj2yfJ" +
                "upplqD6UMGfXg5b8Y7nlc4YOG56cHLZnZYmhZPPCRe5H0hdFSVlT671T6+bMOqrW4eIPd6ur1XZcX5UOgPVNPPrNdHcA6uw1Cndb" +
                "ms+j7XzGf5bTBPQLW7D+o0gHV2+p7U8vpCDis4wUPwCPjixecljZYX3OTX1/VICbzAK/+HOND7PScw0AgiZGOYv9R7wtl8vlcrli" +
                "RnJSEIC2q8culjUZeIFLM7+eJAU45bGlb8p4tC2fFvlP3nBNB0jHpq4eK9SZaKrv9AGsZ/zfS/K/+nBcmGd/BtzTDjAityHyGSu2" +
                "H9ocH+TuE+In6b1nCSLXZCT7AlBnfvzRwSqrf420moY6tSUVZYVZX2+/YgKo4IkjnMSujs+WH+zAqR8EA9AVHrtY1mQAzychs/TS" +
                "loSYUA97Bib9U9gIXYOiZ6/es29ltIfb4MEuvF9YOix2l8bHx8fGxlpsjzS13CsortGDloa9aXX5atTW1yhrHjdotB0cQDH9RBIn" +
                "Fw9PF/uuac9KEGNLVXGxUgvwXEKHDbIU3f2V3hEHRThXX7/bAtgPihrqYr5849pV1wsq2mDjOCQq2IkFAGNbXbWyRt3Y3N7BAWBs" +
                "7SVSdy9PqZCx3jbDk9L8Ww0cBN7DIgcInsfv3B6ZlpbWnc6MuLi4jIwMjmCdtLS0xMTEZ2/JDStyv4/oI/qIPgLRR/QRfUQfgegj" +
                "+og+oo9A9BF9RB/RRyD6iL4+pueDMLVaXVlZSbxYo77e/JG85ZO2s2fPJiUlEUcv5sCBAyNHjuxFH4HMfUQf0Uf0EYg+ou9/mf8A" +
                "AcFyyh3oM2AAAAAASUVORK5CYII=";

        ENDFOREACH_ICON = "data:image/png;base64," +
                "iVBORw0KGgoAAAANSUhEUgAAAGwAAABECAIAAADr+0fBAAAAhnpUWHRSYXcgcHJvZmlsZSB0eXBlIGV4aWYAAHjaVY7bCcRACEX/" +
                "rWJL8DU+ylmGBNLBlr/KJAw5H3q5yEE4ftcJn4aQQYeHpRkWmpr8rRC4EERipN41F/cWqsS7BuEVLMNR96He/cMQCztd3W3YtMll" +
                "50NIomaL2or9Rm6Jzeejd6/6lsMfSY8sUbo/IBUAAAoEaVRYdFhNTDpjb20uYWRvYmUueG1wAAAAAAA8P3hwYWNrZXQgYmVnaW49" +
                "Iu+7vyIgaWQ9Ilc1TTBNcENlaGlIenJlU3pOVGN6a2M5ZCI/Pgo8eDp4bXBtZXRhIHhtbG5zOng9ImFkb2JlOm5zOm1ldGEvIiB4" +
                "OnhtcHRrPSJYTVAgQ29yZSA0LjQuMC1FeGl2MiI+CiA8cmRmOlJERiB4bWxuczpyZGY9Imh0dHA6Ly93d3cudzMub3JnLzE5OTkv" +
                "MDIvMjItcmRmLXN5bnRheC1ucyMiPgogIDxyZGY6RGVzY3JpcHRpb24gcmRmOmFib3V0PSIiCiAgICB4bWxuczpleGlmPSJodHRw" +
                "Oi8vbnMuYWRvYmUuY29tL2V4aWYvMS4wLyIKICAgIHhtbG5zOnRpZmY9Imh0dHA6Ly9ucy5hZG9iZS5jb20vdGlmZi8xLjAvIgog" +
                "ICBleGlmOlBpeGVsWERpbWVuc2lvbj0iMTA4IgogICBleGlmOlBpeGVsWURpbWVuc2lvbj0iNjgiCiAgIHRpZmY6SW1hZ2VXaWR0" +
                "aD0iMTA4IgogICB0aWZmOkltYWdlSGVpZ2h0PSI2OCIKICAgdGlmZjpPcmllbnRhdGlvbj0iMSIvPgogPC9yZGY6UkRGPgo8L3g6" +
                "eG1wbWV0YT4KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "CiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgIAo8P3hwYWNrZXQgZW5kPSJ3Ij8+ez094wAAAANzQklUCAgI2+FP4AAACmRJ" +
                "REFUeNrtm3lcVOUax39nZs4MDAwMwzZssq8iSKCQkQMRZmBWlhtoZly7oaapRBpq97aYpiTeEshcr8tVK5dKSSUUQ/ICYohXcgF0" +
                "2AcEhmWYYZbTH4DOjAzYzaQPnt9fcOY9z/vO97zv87zv88whKIoCrT8mBo2AhviXEEv7H7lcnpeXR0MZWBERESyWDjdC2ycmJydn" +
                "ZWUJhUKalCFVV1fPmjUrJSXF4EysqalZsWJFXFwcDcuQMjIySktLaZ9IBxYaIg2RFg2RhkhDpCHSoiHSEGmI96qr6C1HwoAYUd9K" +
                "tRrYJ+a23TnSa+p3hREEQfh+fLW7HyPGNj7jp7+zNa+u+9GaiVxzCz3ZM8vzS5rVfQ3qMl9derxBrXuXrLKwSCyjtIzwzU05kDde" +
                "/engJ/OedAlZeEz/lock1sPv0uSpj/avDjEj9Z4fi6N94eaulLS54R89Yc4YxIimq+r8wbQ1W863lG5+8TmHwtzlgcbE8IcIgGnj" +
                "H+rLZ96z3nuGZCskGuqVJZ+v3BF/bLE/d1Aj4RETQrnB49MqlYVpHx+duXO6ixEx/JfzIM/VPT4hkAV0nPn4n4dv3Y+nY1o8/tbS" +
                "QACQFJwtutWpGf4+sTNn+ZQwV2vBXdk/ub7sLiyl0urZVYvcAUi+fm9djuR+/BzL0tebBwDS2sbGVgWF4T8TFR1trS1aqiu/XPRL" +
                "1d2YQZCeCzNetQZQvi3l88L2+2BCaVRqAGAwCFWX8iFDHJLAsvHr1UHG+hRU2rVbwmz8mrSJB+N/kBVveu/AikFdnLLu4q8yABC4" +
                "CEjiUQksgeH9BJbb2k3spm56f4Nv0sW27PVbByn6UB3FWzKuAIDL+CCBEZdNPALL+f5Eer2RuXgEgLby+gEAKmpzU2dMSr0JwDTi" +
                "b7GO5tb8hw1xCGZiZ07K1PA1HJbON+U9vbt4q0B/4YeszJixI3Z/qyEjbEZ3W5NU0cPcO/6jZeOsbV3tuI/EcpZJm2V6lyRlFwp/" +
                "DdUPCAzBhE9SRYcTchUDGGEL3ALDY2bET35MaOns72VFDucTi3FImrhtYUFxjcKQaxmZKm57s6C4RnF3KrGcXjstjSktuNqiAUAa" +
                "MMJgm/AthY7O9hacofBPD3cmMngeYSKPAZvc24DgCAOe1A4tgxuhU2F0KoyGSIuGSEMcthAHSOgHpFWoAACa9rJvPpwbPcrOhEEQ" +
                "BEHwnB6b9OZnOdWKgY0w+e7hs9dkiRW/s9+eQoJWLqN0pXvvJ4/9q3dIOodqSf6Xy14c62LOJAiCIFh819CXkncW9SbR+zoyn3m6" +
                "Q+sm6bGJ5J2CxIObifoJfVuT+qIL1XLFzX2v+Pu9vGpn9uV6GUWQTKCj+uKxzxdFuY1dmt2kl9fjmPJ4PB6PZ8oloZFWnNuTEhMw" +
                "ZftN5e/ot7eQcLuPlvzKv3dX9P59MW39d7fkWvt1Sla6Mcbzidc/PVJ4q00DBsmEWnqz4ND6uWN8XtpWocVHLSn5uaz1npybXkHi" +
                "j+0T+03oM7nUtU0vzdorBginmKSUeRN8LEiNvLZg/4YPdhZ3Xtr4wgyv4uNveN01svar1cE8FgMApag+tTbx/VMt0uPvfZD1TMZz" +
                "Dv0edw0XEnpbd13asU8MkN4RjtfOVFYeP1bw9sQRbr3lga7i96cszW4DSK+XV6yYI3LlMamuqnO7132w73Lj0QVxm8bkLHi4m+1+" +
                "EvodZxPWF1MA03/J+mWxbg7ePm62PJKIjHpqpGrUc3saO3/8JC37+bRIbSOjwvqMhPALd5xKrUJd6ZWrdVH2zibE/fardYAu3nag" +
                "FiBHT48THcndcEmc9X1BTayrB5cA0Ja3IfMGAM7Y5DXzo1xG+Po4W5mwCFHUeHfOnC1Sr3E88fUmyyEOLIryH043AWAGThUJBZ6j" +
                "vIQ8kgAAlm10UoIzAFTm/lRS19X/cqCUchUAsLnsbqn8/6rStRd++Y0EIANjgtyjXwkGUHvi2/NVMg0AyMtP5UsBkKOfD7Ox8hnl" +
                "Zm3CIgAQ5IgZ209+vW5R3NMBdhxiiCF2S65JAMDSw45r4WCtXRviOAU7AwBaqmvr2+/19hpZ9Zm0d3bUATAKinLnshiEgXzPQIUE" +
                "6fktR28DrICY0XyrgBdeCwHQkH3051syDQBlU/ltALBwsTbm2wt03QUp8Bjp5zHCxozV19GSCX4WrL7gxZ90QvXAIer1QRCEYM5Z" +
                "GaPnq2s0FGlipLvgKE1PTCEIQilTUfpGmCZOke/mygDrp5YtHmcpsDFlGpruBgsJVMu5L75vBVgBsUF8gZ2l87NzggA05hzJv9mp" +
                "AUD0DI/SgOT+yWmJ+/SJHFOe9rNkCuRXLsrcbIEONF8Td+itRrm4SAwAhLWzBUnoGaFUHR1dFMAcmfjFhmkefFsvT0vSUEAzWEig" +
                "WnIzs9oBqK6kz5+9y4jNhKwaAJpzj+RVTPML5Nj69Ayvok6m99IYpVKomTr5TL2OOs+/PXl5ofrBQtQOrHdzK+ae0bbryhs0lw58" +
                "dyUx2ovH7htjW8GWPWIA8I70N+NwezHeMaK4vn3e3O0V6v99m92waFq0D485YEDrr5AAjeTHjJM96UR5U111k9ZHrWcPnb0+0z/Y" +
                "7ZkIqw/Lm9Qlh34snxflwesbvKbxaJz/K78Eznnn07Uz++9I2iEg/oSZqBNY72xkLd4N/XLxf9U3vvj7XAezncmR9hyqs/LUxtdn" +
                "ZNYBsHg2IcqWb8NT6xt5Iujg1byQtddq9i//xwtRe6Y7/u58nKbhZGaOAoDP0gObJ9v2DktVtS9h9pZb7fmHc6/NHj1mbFJS4Lbl" +
                "JaqS1NcXOBzIXBBmQ2o6bnz/4eyZX0nkOP3T9aoqiSk1tIEFIN0Tv9o1zQGA5NTqKAcjJofNNHWbuCq7FTAOnLd2YYjAztWmn18j" +
                "cINX7U32AFB/YNHi/WKlYV+cMjXcy95WRx7xJ1tqszJzVQD8JoVZ8z1Dx4tEIpFIFDU9ab4fgM6fD58ua1WxfZYc2jrZBqDEh5c8" +
                "bstmGnGYPM/n152XAzYTUlZOMJFrmMRQQwRIp/j/lOR/9sYzgU7mLFDdSrB49iNF09/evC813s/BbZSHRf+zjBuyem+SOwDJobcW" +
                "7qk0eLiSSZsbJboqLyvM2rr5nAYg/CeFWvHtLO9sVEiXKa/5A5AXHj5d1qoC2y3hUOmZTxOiAxzNWNAousEwsfOLnLVy247lkY72" +
                "vr5C9gMJLDqvpcXHx8fGxuq8UaVpv1FQXKMA0ybwcYObXrWsqUZc09AslSkpgGAZ8yyshI5OQrNed2jAiLq9srhYLAPYwoAx3rq4" +
                "+27pX3y/YOuqC9fbATPvsNFC7e0e1VV9oaC8EwzLkWH+PfUWdWdjlbhG0tLWU9VnGZlZ2Dg4O9mYsAyPTXW7NP9yMwWu65iQEVqV" +
                "r543qtLT03VDlZbi4uL27t1L0TKs9PT0xMREvYt0KozOJ9IQaYi0aIg0RBoiDZEWDZGG+NeVfn5AIpFUVFTQXAypqalpkATEiRMn" +
                "5s+fT5MaWLt37x43bpxBiLRon0hDpCHSEGnREGmIw0O/AZYji/zEiFmrAAAAAElFTkSuQmCC";

        IF_ICON = "data:image/png;base64," +
                "iVBORw0KGgoAAAANSUhEUgAAAEUAAABGCAIAAAAVe87QAAAAh3pUWHRSYXcgcHJvZmlsZSB0eXBlIGV4aWYAAHjaXY7LDcNACETv" +
                "VJES+O2wlBNZtpQOUn5Aa8tJ3gFGI/QE7e/XQY9GWMlHTCTAhaenPitMXhizKEvvmotzm1TSuybTFZAz2O9DP/uLYZg4wiMwsGHT" +
                "sutuYrNmi9rK/UZ+Scb10V+PXzl9AEItLCM/DLHyAAAKAmlUWHRYTUw6Y29tLmFkb2JlLnhtcAAAAAAAPD94cGFja2V0IGJlZ2lu" +
                "PSLvu78iIGlkPSJXNU0wTXBDZWhpSHpyZVN6TlRjemtjOWQiPz4KPHg6eG1wbWV0YSB4bWxuczp4PSJhZG9iZTpuczptZXRhLyIg" +
                "eDp4bXB0az0iWE1QIENvcmUgNC40LjAtRXhpdjIiPgogPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5" +
                "LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4KICA8cmRmOkRlc2NyaXB0aW9uIHJkZjphYm91dD0iIgogICAgeG1sbnM6ZXhpZj0iaHR0" +
                "cDovL25zLmFkb2JlLmNvbS9leGlmLzEuMC8iCiAgICB4bWxuczp0aWZmPSJodHRwOi8vbnMuYWRvYmUuY29tL3RpZmYvMS4wLyIK" +
                "ICAgZXhpZjpQaXhlbFhEaW1lbnNpb249IjY5IgogICBleGlmOlBpeGVsWURpbWVuc2lvbj0iNzAiCiAgIHRpZmY6SW1hZ2VXaWR0" +
                "aD0iNjkiCiAgIHRpZmY6SW1hZ2VIZWlnaHQ9IjcwIgogICB0aWZmOk9yaWVudGF0aW9uPSIxIi8+CiA8L3JkZjpSREY+CjwveDp4" +
                "bXBtZXRhPgogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAK" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgCjw/eHBhY2tldCBlbmQ9InciPz6xtaKZAAAAA3NCSVQICAjb4U/gAAACnUlE" +
                "QVRo3u3bMasaQRAH8DGYGMuAHI9AikA6C0ESbNLInSAcBCs/QAo/gCAp1EKxskgR0gUktW2uyBM5SK29YCEWD4IIHlxjkzApxITI" +
                "87zbndnbJzeVZyH82F1w/zOXQkS4onoE11WJJ/Fcm8f3/dVqdSUez/NM0yyVSp1O58F7PM+zLKtcLi8WC8dxVJCQrXa7XbFYbLVa" +
                "h8ftdlsoFNrtNnIWqMEoI4EyjBoSqMQoIIFiDDcJ1GNYSRALho8EcWGYSBAjhoME8WLISRA7hpYEOmAISaAJhooE+mBISKAVRp4E" +
                "umEkSaAhRoYEemKESaAtRowEOmMESKA5JioJ9MdEIgE55tfdl9cAkK3d+sfPp1X6+vM3EykdkJuZpjkcDilCsWdvqm9vHh+fsoWX" +
                "T1MCv5LL5abTqWVZADAYDMLmb5Lb7J71ydZufUUbL828MvQVvEr/efb7PQtm/6NTf/f5uN/Sz2vDT+9fPZElVSqVTCbT7XbPegBg" +
                "vV7X63XyHHv2/du/p5sXHz5KeQ6karW6XC4vnB/HcQzDmM1mwvub+/wcqtfr5fP5zWZz8v1pf8G27dFoZNv2fD7XtmnV7/fH47Hr" +
                "uoZhXO6XaE4KwJzt/2hLCsZc6P/InyU1ZybC/1F9SGEwoe4LOpBCYsLe5+IlhcdEuG/HRYqEiZaHqCdFxUTOq1SSBDAieaIakhhG" +
                "MO/lJgljxPN4PpIMBmX6JRwkSQxK9rNoSfIYlO83UpFIMEjSD5YnUWGQql8vQyLEIOE8hRiJFoO08y5RSeQYJJ9HCk/iwCDHvFgY" +
                "EhMGmeb5gkl8GOSbtzxHYsUweu4lcWN4PSckBRh2z19So9FQgEHEFPK//zOZTFzXbTabZ0NAulLhSd4vSTyJ52HUHzBKI9rqac8f" +
                "AAAAAElFTkSuQmCC";

        ENDIF_ICON = "data:image/png;base64," +
                "iVBORw0KGgoAAAANSUhEUgAAAIEAAAB2CAIAAABgVWemAAAAhnpUWHRSYXcgcHJvZmlsZSB0eXBlIGV4aWYAAHjaVY7ZCcQwDET/" +
                "VUVK0GUd5YSQQDrY8lfCCSbvQxoG82Q4f/cFW0PIoMPD0gwLTU3eKwROBJEYqXfNybOFKvGqQXgGy3DU9VCf/mWIhV2u7jbssIPL" +
                "zqeQRM0WtRX7G7kk8Z6Rb+/2lcMfQmQsJtTZtyAAAAoGaVRYdFhNTDpjb20uYWRvYmUueG1wAAAAAAA8P3hwYWNrZXQgYmVnaW49" +
                "Iu+7vyIgaWQ9Ilc1TTBNcENlaGlIenJlU3pOVGN6a2M5ZCI/Pgo8eDp4bXBtZXRhIHhtbG5zOng9ImFkb2JlOm5zOm1ldGEvIiB4" +
                "OnhtcHRrPSJYTVAgQ29yZSA0LjQuMC1FeGl2MiI+CiA8cmRmOlJERiB4bWxuczpyZGY9Imh0dHA6Ly93d3cudzMub3JnLzE5OTkv" +
                "MDIvMjItcmRmLXN5bnRheC1ucyMiPgogIDxyZGY6RGVzY3JpcHRpb24gcmRmOmFib3V0PSIiCiAgICB4bWxuczpleGlmPSJodHRw" +
                "Oi8vbnMuYWRvYmUuY29tL2V4aWYvMS4wLyIKICAgIHhtbG5zOnRpZmY9Imh0dHA6Ly9ucy5hZG9iZS5jb20vdGlmZi8xLjAvIgog" +
                "ICBleGlmOlBpeGVsWERpbWVuc2lvbj0iMTI5IgogICBleGlmOlBpeGVsWURpbWVuc2lvbj0iMTE4IgogICB0aWZmOkltYWdlV2lk" +
                "dGg9IjEyOSIKICAgdGlmZjpJbWFnZUhlaWdodD0iMTE4IgogICB0aWZmOk9yaWVudGF0aW9uPSIxIi8+CiA8L3JkZjpSREY+Cjwv" +
                "eDp4bXBtZXRhPgogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAK" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgCjw/eHBhY2tldCBlbmQ9InciPz68wetuAAAAA3NCSVQICAjb4U/gAAAN" +
                "E0lEQVR42u2de1QTVx7Hh1h89EFtbfMwQYIihKXIhlKWw7qICEEKHKRl10dWReGwFCriYnOErG5xYWMsNhU2paxUbMpBPUgVpCCH" +
                "IgRWWRalgAqKD0CRhzwNz5CQu39M8VgFSSaTycyY7195zNzc3M/MnXvv73d/PzMAAGSSUUUxNYGJgUkmBiYG+qi0tNTGxiY6Opro" +
                "jzSiMsjPz+fz+RKJpL6+fufOnVNTUwSGAAio7OxsGo1WW1sLABgZGfH29g4JCVEqlYCYIh6D9PR0JpN5/fr1J59MTEwEBQX5+fmN" +
                "jo6aGBhcYrHY2tr6zp07z3yuUqn4fL6Hh8fjx49NDAwooVDI4XAePHgw47dTU1ORkZEuLi59fX0mBuhLo9HExMRwudyenp4XHyYQ" +
                "CBwcHLq6ukwM0JRarQ4LC3N3dx8cHNTm+KSkpJUrV7a1tZkYoCOlUrlx40Zvb++RkRHtz0pNTbWysrp586aJgb4aGxvz9/cPCgqa" +
                "mJjQ9dysrKylS5fW19ebGCCXQqHw9PTk8/kqlQpZCbm5uTQarbq62sQAifr7+11dXSMjI6empvQpp6ioiEqllpWVmRjopu7ubkdH" +
                "R4FAoNFo9C9NLpdTqdTz58+bGGir9vZ2Ozu7pKQkFMusra1lMBinTp0yMZhbLS0tbDb7q6++Qr3ka9euMZnMzMxME4MXqbGx0aDN" +
                "BAOWSCQmBjOrpqaGTqcburuAO7rExEQTg2dVXl5OpVILCwuxeeA7OTnFxcWh8sAnCYMff/yRSqWWl5dj9osDAwNubm4RERF6DnxJ" +
                "wiA3N5dOp9fU1GD8u8PDw15eXvpMAEnCICsri8lkNjY2GuXXx8fHAwMDkS2EkIRBamoqm81uaWkx4p+fnJzcvHmzrguCJGGQlJRk" +
                "Z2fX3t5u9H5ArVZHRERovzBOBgawmcXJyam7uxsnwxKNRhMXFzengYgkDDQaTWRkpJub28DAAN4mSomJiRwOp6Ojg8wM1Gr11q1b" +
                "vby8hoeHAS4lkUiWL19+7949cjJQKpXBwcGBgYHj4+MAx8rMzLS0tGxqaiIbg9HRUR6Pt2nTpsnJSYB7nTp1isFgXL16lTwMhoaG" +
                "Vq9eHRYWplarAUFUUFBAo9GqqqrIwKC3t9fZ2Tk2NhY/izNaqqysjEqllpSUEJtBZ2envb39gQMHADFVXV1No9HOnj1LVAatra02" +
                "NjYpKSmAyKqvr2cymdnZ2cRj0NzcbGVllZGRAYivmzdvstns9PR0IjGoq6tjMpk5OTmALGpra7O1tRWLxcRgcOnSJTqdXlBQAMil" +
                "rq4uR0dHoVCIdwalpaU0Gg3nzjyIBbs87dq1C/UxHmoM8vPz6XQ6/p3a9BHs+rdjxw505zroMMjJyWGxWIRw7tRTsAvsxo0bUdx6" +
                "hQKDjIwMa2trojg56y/YFdzf339sbAwXDFJSUmxtbQnk7I+K4C0Rnp6eCoXCyAwOHDjg6OhIrE0vaEmj0cTGxrq6uvb39xuHAVwD" +
                "Im7+QldCoVD/qxBCdieGh4cTdBMk6hKLxba2tvrYxnVmAPsiEHczsCGUnp5ubW2N2EdENwawTw6hN8UbSNnZ2SwWC5mvlA4MYN+0" +
                "0NDQF85QlE0HbeeO0DDP5/zQswcv2Xph4JkZ6Oh/o+jwly7HOtQvKJ+y8K1lv/XZ8tcjZxsGtJg+PVUIc3ftmJbVZnxa86LB6Llz" +
                "5xgMBgKfQW1jhgwODvr4+Dg4OBw/fnzevHmGiJzR//2e1OsTyM7VTAzery/N+TIu2InmGHr8xqgRIrkEBQXJZLKgoKCKigqdTnxF" +
                "m4N6enp8fX0DAgKSkpJ0KZzu4vXe4pkom1m4082f/7j50N7c8KKtzHm6la9RKh61Xb/+YAyCIAhSNX8X9kHb49riPQ6LkDXmLNU2" +
                "t1z11hw18/b2zsvLCwkJOXbsmL+/P2pxW+7fv8/hcLReuX3+Nge6dFyMTyoVGi37ol+VP6Vozov3sHhSkENi4ziyvmjuas+huro6" +
                "Fot1+vRpdPqiu3fvenp67t69WyAQGPRGfsPyVQiCIKgrPfbft1VIAjG9wfnonyWXD7v+cmffSEksGzBObCkul1taWioQCE6cOKFv" +
                "DKkbN26sXbv24MGDkZGRhq73ip3CtQshCIKguoPxhY80yEpZ6BB19BMm/Hr4wrc1CmNFheJwOBUVFcnJyWlpacgZXLlyhcfjpaWl" +
                "8fl8DCo9Md9P/BncGyh+iEupG0NYzmurtgTS4Jej9VVtSuMF52Kz2ZWVlRkZGcnJyUieyZWVlZs2bZLJZN7e3shr8fD0JwE3Zni4" +
                "URZ7imRC5189MVUT5u/tlmz+l//JQQhqlez5Pqr8L+xXEPzoApazJQT1QBAE9d7pU0HQAuNhYDAYcrl8/fr1CoXi0KFDZmZm2jIo" +
                "Li4ODw8/c+aMu7u7fnXovnKxe8Z2WrhHBUHPjlooS3z+kfi70zE1Gkj9n7/9/eKfTvAQtB9lwevTZ6nGVUj6tIcnI9Y3vPncpUN5" +
                "2zslW8jVcay1ZMmSsrKywMDAqKgoqVRKoVDmZtDQ0BAeHl5YWMjlcjG/bsxX7JB8+oV76gMI6pPFpn32817dy5ga7Rv55eWiNxci" +
                "Cpn46OfKRzN8vGhxrxpJcRYWFsXFxR9//HF8fLxYLMY2ruOsg7yJ8+stZj7lddf4L/xfm54snOkC5rpOB8fvXm6bHuc7MOYTMbam" +
                "k5NTZmZmQEDA5cuXjVGfefQNh+Pfg1uzRCBqeEW3ex8MXjpW/Bh+/c7v/8Caj+KlM3aW9waSv6RQKPz8/Nhstkgk0nZc5Ofnd/Lk" +
                "yZCQkJ9++skIFBbYR38Z+g4EQRDU+e3nxX26EBj53+Hdp39BwPjjTu5rRr/G+/v7161b5+bm9vXXX8/4MJi1L/Lw8CgoKNi+fXt+" +
                "fj7m1TZbvPbzZA94KaO7aUjb9p9oL9rvt+5QC/x2/prPBa7GRtDV1bVmzZoNGzaIxeLZBkUvWi9ycXG5cOHChx9+ODIygnCKMNvY" +
                "FIIgaOFvPj32ZdDbs1TK6s9H9hz64HCrluWDyeGuW1eaep7E+n0z4JsTOxANbdFTW1ubj49PdHR0bGws8jU7R0fHixcv+vr6Dg8P" +
                "I5oqzzY2hSAI6vYb1UBvz/btq85xRz765qMfFLqXT7Hd9m3BN9vZ5sYEcOvWLV9f3/3794eFhem1VgFB0MqVKysqKo4ePXrkyBFs" +
                "xwpUf9F+XUbH5hZMJ98d+09cfnjtu1C7RWZGBNDQ0ODj4yMWi7UBAEHaxbvu7u5etWoVcXcSYKnq6moGg6FT/BNt7WiwtyURd9Rg" +
                "qbKyMjqdrqvHrQ62TNjbklg7y7BUQUEBg8FA4HGrm03fEN6W5JA+Hrc6+7YolcqQkJCAgACc7zTGUpmZmVZWVs3NzZj6eIWGhuJ5" +
                "xz2WkkgkNjY2ra2tRvB13LVrFz4jT2CpxMREBweHzs5OrH0dnyghIQFXEViwFBzt5f3339ff41Zf33eRSISTSERYCo56tHr16qGh" +
                "If1LQ2EPiFQqZbPZt2/ffkkAqFSqLVu28Hg8tDxu0dkLJZPJWCzW03mCyCo4/1FwcDC+9kLBysvLYzAYcL4ssgrOA8bn8/G4JxBW" +
                "UVERnU6Xy+WkBDA4OOju7q5/OgDDMgAAyOVyOp1eVFREMgA9PT1cLhetdACGZQCmA93n5uaSBkBHRweHw0E3HYBhGYDpCO4ymYwE" +
                "AO7du7d8+XKDRos3VOwcONC9VColNICmpiZLS0tDZ00wYAwpONC9SCQiKICrV68uXboUg+whho2lBhvg9u3bRzgAVVVVNBoNm3QA" +
                "Bo8pCBvgoqKiCGSAKykpwTKbFBaxNWED3LZt2whhgDt37hzGWdUwijELG+Dwv6k2OzubyWRiHIAGu1jLsAEOz5vL09PTly1bhtgc" +
                "RgAGYNoAh88gC2KxWE9zGDEYgGkDHN6CjQiFQv3NYYRhACshIcGI//mZayImJgYVcxjBGAAARCKR0TNNw6Gg0DKHEY8BAEAqlRox" +
                "0zQcEg1FcxghGQAAZDKZUTJNw2NldM1hRGUAAMjLy8N4TvQkOzZO5oy4yNWIZaZptLJjk40BmM40beg1MjhZpoHMYYRnAAyfadoQ" +
                "2bHJxgAYMoUybpMnAxzmcjdEYxk6OzbZGAC0M01jkx2bbAzAtAFu7969ej485XL5u+++i405jGwMUBlEYjnkJSeDpydTCDJN5+bm" +
                "Yjz1IyeDJ4sKumaazsrKMsoSCDkZgGkDnPaZplNTUy0tLbE3h5GZAZg2wGmTaTo5OXnFihVGMYeRnAGYNsA5Ozv39vbOdsy+fftw" +
                "YhoiJwNYCQkJ9vb2Dx8+fJ5QVFSUcc1hLwsDAIBIJLK2tn460zScHdvo5rCXiAEAQCqVMplMONM0nB0bD+awl4sBAEAmk8Epjnk8" +
                "Hk7MYS8dAwBAXl4elUrFjzkMscwAABBhpVarKRTKbLH6iCJiMyCHKKYmMDEwCfo/Vej/SIz8LfAAAAAASUVORK5CYII=";

        INVOKE_ICON = "data:image/png;base64," +
                "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAABGdBTUEAAK/INwWK6QAAABl0RVh0U29mdHdhcmUAQWRvYmUgSW1h" +
                "Z2VSZWFkeXHJZTwAAAHbSURBVHjaYvz//z8DKUC73guu4WrjNkZ0eSYGEsHffwwMGW5WQGzJoFnrheEaRkIuVK1C0wTkZXiaM/xn" +
                "BGlmYJix4yTD7TaES/EaqFDm9T/Ny4QBWQ0jI8xcRrDhjEBTZ28/y3C/E2IoXi//+cvI8OPPH4aff//C8Y8/EPwTKP4Dyo91M2CQ" +
                "KfIG28qCz8Bv3xgY3v/8CfEn2FkgfyK4DEhR8u0HhMZr4O8/DEBX/EEOcohn/8PMhFiwYstths+ztjISFSmcSd5oCv4zhPoowQ1f" +
                "veU+w/d5W+FuZSGUTJAVgwBrvPf/3//+gV24dstDht8LUOVxujCg+NR/sCuA8hv6zOCamGMRLv67eCtGwgYnCXTsm3/i/6Ibv/4v" +
                "uvnrv2/Bif/Y1ODCGC70yT3xPzTLCOp8BoZV08+C0x5yxG6eZMmIK4hQDPTKPvo/LMMY6lNoVviPSB3/oQaunnGWYetUa6yGwiPF" +
                "I+Pw/+BkY4bfP/6haCYVwA38A0x0X959IUrTr++/cMrBs96euY6MW5ZdJrLE+cdAVBiCgGPM7v9BQK/DwLq5ZxFBAA2D/YtdcYcG" +
                "tqi3j9r5f9K+t2BsF7mDpGSDNaccWOrGaBex4z8ZcUI4L5MKAAIMANTrPvhAO1k4AAAAAElFTkSuQmCC";

        ONALARM_ICON = "data:image/png;base64," +
                "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAABGdBTUEAAK/INwWK6QAAABl0RVh0U29mdHdhcmUAQWRvYmUgSW1h" +
                "Z2VSZWFkeXHJZTwAAAKSSURBVHjanFVtSFNRGH6O2wyXmx+1ZsuJium0FaUlikiFZIFQUCDkb0GI/oRB1O+yP5EQEQjRT6P+RFBB" +
                "ysglpH04M5brQ53kR1v5uSQtN0/n3Veeu7sgX3i4557zPs997jnvey/jnEMt3nyY5s53Xrz6NAnPxHf45n8gJ8uAUqsJlcW5OLin" +
                "APttFqbkMTXBji4X7+weRLU9H1VlVpTlm7F9ixFfZwMYHvejf3gCfe5xNB3Zh5b6cvZPwav3e7l/fglNdXthF0Kx4IySxZVIAm4h" +
                "3Ol4C3NWOi421sZFUyRnT11hscvNR9E95BWrLI5DrTcQ0qaAaRjaH/bDXpgTzqN84v19snBIGJyc4bXnb/O+MR8PiAklsk9d4U/c" +
                "ownzlE+8154pTjpxhw7XCKrEntkKzAiJeyVioZynfOLRAUqv/PL9BCpsVgRFlhoo1pKsEY+qgUIbE/SITS7KNWM1iKQRWoPqOvGo" +
                "tCSHvrkATEYD7jx6gVCQJyAiyBFUzFM+8ahOJYemjHS4vdM4VrkLfiGujGtnj6O57R662s9ECycSlE88KnrJYXHeNkzNLGKzfpMq" +
                "qncX4WbrSdSfu5WwRjzqIEmwvMSKoZEpaDQpSXGgtBAdFxpR03JdmicetaPUKc7P3/jC7BwqSvKQmZ6W8MrUKYiW7/peW1haxsDH" +
                "L9iRaQz3ttR6zx63cWwwDjdcYtKhUEwyC3yWOuhFyf4MLEJvzIBBgDIZi+yPNI5a5WM9cQ1JcGU1FXbzVuw0pqHnwV3UnDgNnWCK" +
                "9g2D2lqDdeOooGsU6oLLQpCt2yNyIN0nGZMR1a9NbCGWGPy1kiigeEjEiE7d4e+gFv7e5/CLsS41GwMO538fDkv2C9ho/BFgAAV9" +
                "WCkOhbgWAAAAAElFTkSuQmCC";

        ONEVENT_ICON = "data:image/png;base64," +
                "/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAAMCAgMCAgMDAwMEAwMEBQgFBQQEBQoHBwYIDAoMDAsKCwsNDhIQDQ4RDgsLEBYQERMU" +
                "FRUVDA8XGBYUGBIUFRT/2wBDAQMEBAUEBQkFBQkUDQsNFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQU" +
                "FBQUFBQUFBT/wAARCAAgACEDASIAAhEBAxEB/8QAHwAAAQUBAQEBAQEAAAAAAAAAAAECAwQFBgcICQoL/8QAtRAAAgEDAwIEAwUF" +
                "BAQAAAF9AQIDAAQRBRIhMUEGE1FhByJxFDKBkaEII0KxwRVS0fAkM2JyggkKFhcYGRolJicoKSo0NTY3ODk6Q0RFRkdISUpTVFVW" +
                "V1hZWmNkZWZnaGlqc3R1dnd4eXqDhIWGh4iJipKTlJWWl5iZmqKjpKWmp6ipqrKztLW2t7i5usLDxMXGx8jJytLT1NXW19jZ2uHi" +
                "4+Tl5ufo6erx8vP09fb3+Pn6/8QAHwEAAwEBAQEBAQEBAQAAAAAAAAECAwQFBgcICQoL/8QAtREAAgECBAQDBAcFBAQAAQJ3AAEC" +
                "AxEEBSExBhJBUQdhcRMiMoEIFEKRobHBCSMzUvAVYnLRChYkNOEl8RcYGRomJygpKjU2Nzg5OkNERUZHSElKU1RVVldYWVpjZGVm" +
                "Z2hpanN0dXZ3eHl6goOEhYaHiImKkpOUlZaXmJmaoqOkpaanqKmqsrO0tba3uLm6wsPExcbHyMnK0tPU1dbX2Nna4uPk5ebn6Onq" +
                "8vP09fb3+Pn6/9oADAMBAAIRAxEAPwD9EfE3irWvEHiWbwt4SeK2uLZFfUdXmTeloG+6iL0aQjnngfnjl/E/gnTNA1fw5ZavrPiT" +
                "WL/Wro2qXX9olBEwXduCjoPbmuk+CQWTSfEVw/N9Nrl39pY9d4YYB+gx+dZPx4vl8P6p4E8QXUM50nS9W8y9uYoy4t0ZdodsdBk/" +
                "5OK9f20MLUcG+WMU7vu7bt9r7LY4IYerjFH2cXOcmklvu9ku9izqVj4n+FMTanZ6pd+KfDkPzXdhqDB7qGPu8cn8WBzg9h+I9I0r" +
                "U7bWtNtb+zlE1rcxrLFIO6kZFSQXFtqllHNDJFdWlxGGSRCHSRGHBB6EEGuC+Arf8W8ijjYtaxXdzHbknOYxK2P61yykq9F1X8Sa" +
                "17p3/HT5mqi6NX2fRp6Po1b/ADPRKKKK4jqPLtaF38KvF1/4ghtZb3wtqzLJqMVupaSznHHnBe6sPve/4Ax/Fnx5outfB/xHcaRr" +
                "ltMzWnAgmXzCCw3KVPIJGRgjv0r1WvJPjj8N9Am+HXiS+tPD9r/aoti0cltbgSF8jkBRya7JVKdWP71PmStdde11+v4HNGFSlK9N" +
                "6b+nozz3wB8KtUls49ET4g65oIaLzbexhwIZInG7KAMB3OR9ffHnPxI1nV/hvqEnhfwR441zXbnS0MmorbyeTZ6cmclWZSRvJJ+U" +
                "dCfXIr0rUPiVoms+F7TTrix8Q2t1DbLEl1a6fKksLBACUYDIP/1q4e7ufDugfDPUfDPhrQ9aSS4iIaafT5PMuJCRl5HxyT+Q6DAr" +
                "ONNJcqdkXOpKUuaSbk+p2P8Ab/ir/oatS/77FFS/2Tff8+dx/wB+m/worE0P/9k="; //need a new one // onmessage

        ONMESSAGE_ICON = "data:image/png;base64," +
                "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAABGdBTUEAAK/INwWK6QAAABl0RVh0U29mdHdhcmUAQWRvYmUgSW1h" +
                "Z2VSZWFkeXHJZTwAAAJRSURBVHjarJVNaBNBFMf/s20WG7SCCIWKYivpxRaEBgr14Ec1lLQ2InjowYMiXrwIgpQKFioUPHjzpFDw" +
                "IAqCktD1IMTGeNAebA8VKylt0Qa0HgRra3aT2Yyzu7Nfya5F6cAL783s/N578x4vhDGG7VyNYQfZuVX2cnH1n2CJ2P5woAG7Nhh3" +
                "7MK6uiXwRf5DOPBcdztO3Tnr2K9G0ljaCsr0cCA/hV4Frvb3mvrxiRTyN9P4vKH95QrzA2OjSVbDRFnXwQhw+XQPesdTeDeWQXGz" +
                "HAysVl3gwRtJdiUZh7fqhIO0KuVcYsIvJroRvzWEudsZfC1V6oHcuWTrVCdQKYXGN21RqSUa31eFfSFxBJ0jKeyLypAl4hNw5w6w" +
                "wh2qOkWJX7KEWrbY04SuViiGE4cRu34GLTs4lEiO+IpiAI2LZm7m+3GPxDVNXaxHyics3E1jk38vNxBfyi6QWhG6y6IRZjMtB0+m" +
                "FlG8lw4uihf464FCmi4NsNrWOT/Y7sCfTq3gx/1MeNvwN/S1TWlSceKf+bjM5r8X0HZgp2mvfNnAw8n+eoZ3FthtM61MBE6IQ8bP" +
                "gqvnlNm6b04MjHqAIuUiacW31j5EoeP3+k9Em3djFxcielESSTu6yIMt52peSESoVmR0tuxFrLkJueePcTQ1jAi/aRTQEKPFGuDR" +
                "BXB2iT/NfAHPsm+taXPspAUscSDxdAYRHUM89Q7SjUD6ujpIT1eHE6RkH3hbjWpqPaDGiRVIJHjAlmkj1t7kscb1iLwH77Ov/3ti" +
                "k+3+C/gjwAAVKwRuFFvW9gAAAABJRU5ErkJggg==";

        PICK_ICON = "data:image/png;base64," +
                "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAABGdBTUEAAK/INwWK6QAAABl0RVh0U29mdHdhcmUAQWRvYmUgSW1h" +
                "Z2VSZWFkeXHJZTwAAAKbSURBVHjarJNdSJNRGMf/7zZtWrIx05TUciVBUhgZUtBdGM2uJIkuLC2q0VUUFdWFFEJgXdRFmBXVjdQw" +
                "g2CSXUisEDPyI5Vsrc1my/mZrobbej9O5z3vNl3mWuQDD5zP3/n/z3MORwjBUoYq0YWnbthI8ZmHZEmAMiw9NwUbVhuw5vh98l9A" +
                "GWbI0WJdph67NudhS35mXCgX7w47BrykIE+P1t5hjM4E2FiSWoW2vmG8+zwBd0M1lzCwsMYUnbCdfoL65wOKJRXHoC/fe9DvnlwA" +
                "VS2mTJQAc+kOmtux82o5zLsLEeRFzIYE+IM8SgqysTE3fYH9qMKCC6ZYqbRn3lMCwsmLgFutnWg/14y6pz1sWh1W+sYxCvvXb1Gl" +
                "DLj2rIkcMxVjvn2Oi3A5Buco9c6zLnScf4za5q4wVIVkjQo9rjE4RqYZlFkWRA5BQUBIFKMZFJQM0fFguF9ZWoTiyxW4WL4VIV5i" +
                "9n8EeGo9A8YsPbPPgDwPuklAgG5SUlD64bFQuB3kBRwoLURRzT5c2r+NHSJKBBk6LfJWroCGXoMmApQ3Mm/Mp3xxc13Mq2Njywf0" +
                "1zbh5IN2LEtSY5U+BYNfJvHa7oWrvopTgIKicF6tlNsjEaZywCOrA4NXLDhx9xW0FJatT4XTO43Ojwospsoph8t+e5AEFXuNUXiT" +
                "dQj2OguO3rZRmAb5mWkYmfqOt7TKEZgcmkgjcK8l5oEmHSojvCQxhc1WNz5ds+DgzRfQJquRY1gOz4QP3c6xGFjcn6KunFPsvm7B" +
                "kQYbfgoS1mfpMOXzo9c1vgD2178sh8vrI1X1bUhPS6XVTMX4jB99Q3+GxVheLIzZOs7nCRAhS6T/VIoLS0hhJDZVN5JZLQ9nHNg/" +
                "ARONXwIMAF3WXdmut/hNAAAAAElFTkSuQmCC";

        RECEIVE_ICON = "data:image/png;base64," +
                "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAABGdBTUEAAK/INwWK6QAAABl0RVh0U29mdHdhcmUAQWRvYmUgSW1h" +
                "Z2VSZWFkeXHJZTwAAAHKSURBVHjaYvz//z8DNQETA5UB1Q1kwSWx9/zj/7tuPybJMDdVWdwGggwr8DGB8299+kHQwG2HruA2MMhY" +
                "icGlMwDO31exkeEuIUP//8VtIFCW4e8/BoZsDysw26HNn+FQ9UaGh19+4tHyH9VA1Sqv/2hmMvz6+5fhPyMDQ4qrOYNVkz/DifpN" +
                "DE++/sJu4L9/CAMVyrz+p3mZMCCnS0agQT///QGaywg2PNHNmMGkzo/hfPMmhufff2MaCLQcnmz+/GVk+PHnD8NPoCAM//gDwT+B" +
                "4j+g/Fg3AwadCn8GaS42BjYmRhTMALQcbuBvoIU//v5h+A7UBMF/IHyo2E8o+8fvPwyRbtoMqsW+DOIcQEMZmeAYJVJABoI0gv0G" +
                "Dj+gjYwILpgNBUu33mC43ruR4StQPRszI4qXEQb+gbgQASCmMf6HmQmxYMWW2wxPpmzEHinIBn6evZWRM8n7P3rSCfVRghu+est9" +
                "hnezNuFONsAwREk23+dthbv/5LV7/206cxl+A5MCyJa1Wx4y/Ji/BX/CRk42mIn0H0ieYdXGhxDfLN5KODP/w5NT1u07SZwhaI5g" +
                "xFXAgkubg/tIK23snXAbOGgKWIAAAwDAL+l9EmbcTAAAAABJRU5ErkJggg==";

        REPEATUNTIL_ICON = "data:image/png;base64," +
                "iVBORw0KGgoAAAANSUhEUgAAAH8AAABBCAIAAAB3r+yZAAAAhnpUWHRSYXcgcHJvZmlsZSB0eXBlIGV4aWYAAHjaVY7bCcRACEX/" +
                "rWJL8DU+yllCBtLBlr/KJAw5H3q5yEE4f9eET0PIoMPD0gwLTU3+VghcCCIxUu+ai3sLVeJdg/AKluGo+1Dv/mGIhU1Xdxt22MFl" +
                "51NIomaL2or9Rm6Jz+ejd6/0lsMfSgAsUvIu4agAAAoEaVRYdFhNTDpjb20uYWRvYmUueG1wAAAAAAA8P3hwYWNrZXQgYmVnaW49" +
                "Iu+7vyIgaWQ9Ilc1TTBNcENlaGlIenJlU3pOVGN6a2M5ZCI/Pgo8eDp4bXBtZXRhIHhtbG5zOng9ImFkb2JlOm5zOm1ldGEvIiB4" +
                "OnhtcHRrPSJYTVAgQ29yZSA0LjQuMC1FeGl2MiI+CiA8cmRmOlJERiB4bWxuczpyZGY9Imh0dHA6Ly93d3cudzMub3JnLzE5OTkv" +
                "MDIvMjItcmRmLXN5bnRheC1ucyMiPgogIDxyZGY6RGVzY3JpcHRpb24gcmRmOmFib3V0PSIiCiAgICB4bWxuczpleGlmPSJodHRw" +
                "Oi8vbnMuYWRvYmUuY29tL2V4aWYvMS4wLyIKICAgIHhtbG5zOnRpZmY9Imh0dHA6Ly9ucy5hZG9iZS5jb20vdGlmZi8xLjAvIgog" +
                "ICBleGlmOlBpeGVsWERpbWVuc2lvbj0iMTI3IgogICBleGlmOlBpeGVsWURpbWVuc2lvbj0iNjUiCiAgIHRpZmY6SW1hZ2VXaWR0" +
                "aD0iMTI3IgogICB0aWZmOkltYWdlSGVpZ2h0PSI2NSIKICAgdGlmZjpPcmllbnRhdGlvbj0iMSIvPgogPC9yZGY6UkRGPgo8L3g6" +
                "eG1wbWV0YT4KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "CiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgIAo8P3hwYWNrZXQgZW5kPSJ3Ij8+TMIltwAAAANzQklUCAgI2+FP4AAABzlJ" +
                "REFUeNrtnHtQU1cawL+ThDwxkLe8ilRxIyUgmC4CBaxWptNO3ULT2pZWu7O6rsPWrlrdaXdxOl3dSrd22/XRSkdRi7a27sQp7NY+" +
                "3LXaLgUEJBJAxUeBVAgqERIgcHPv/pGQhJDwqNAy0+/31517zncev3vuOSd37g1hGAaQnwgWKkD7P1M47qPKysp9+/ahkSkiLy8v" +
                "MzPT5yRxz/sJCQk5OTkRERFoatIxmUx6vd5gMAQc+wCg0+k0Gg3KmnTOnz+v1+tx3sdVF0H7aB/tI2gf7SNoH+0jaB/tI2gf7SNo" +
                "H+0jaB/tI2gf7SOTYt96Ki+U+MIPS1y2+UiDlQmUwYm2qM3hNwNHpl6aX3TWQo8j3A11bbfWmSBa9sktBgDsxldiSSCCdV/2eEUz" +
                "nUfSCSFE/OR/rJ6z9PXiFEIIka48bXO3RJz7TzM9lMP2zSoFIWTOH4++NHpdQ+Gp73fQ3seTM/Z54lCJRCKRhAQHgb3dUPq3vAUP" +
                "vd1oH5nBTRjfVFV73c4MzxAq5oPj1oUv96xJySioso07nGor21/tPOz9orDofzcoIFxpVKTM2SqhTzFSFX2hoqHL4dsNR2ddeYPF" +
                "9zTVce7bizZXVT36/HUffTc4LENf2yWbImK0upp6nOHdTeW1Zgrcxx0D47fPCZQQt/HgrgeVPDYA0LamkvVr9hr7zxRu+yT3LfmI" +
                "DB7YAjYhviUw9tZ/bVn12tfW+l0FR3LXM+MKp9rKimsAuOpMVdPp1prSMxdWpaTHrjt58TFD3Xc2uq96y7J1J3th3voDux9S8tgE" +
                "AAg36AdOpB0fb931u+zti6Se5nAinyg5t6mnuTVgXbfufObhjJImnqtdoOICAKTOK9hf9OQ3jLnx0sWOYGZkhmFzV8PIEuZv+/e7" +
                "WYe6upsNxu8HRw93yW8tK64BCErULdd+cPqdy079cjlHEJG4MALA2i9nu4u5N2Ax40AUJba1dhv3bi157pfP3yP0nhcEEYkLowLV" +
                "ZT1FfqRVl+m3DjAAwBVxB3sGfkA1DE0zAABsLvRT43htl2ot218DwIrLTp6b/XgsQH9N6ZkLFmoKVj7V4ucWzQDoOfl64YnvB6fb" +
                "nocZ7DJ+WPBqFQDwEhfPEbJdIQ07Vi6Nj1S5CU9+saLXfwm9jSWFxy0AoExSh7LJ2OFUa1lxLQCos+dLFAt0upgp1E9xk/I3JrMA" +
                "TB+8uqfSQk8L+w1/zdHO5BFCWFxpfF5xC0DofS+8cJ9MqhC47jh7d9eNTrOb65fqq6svd3ta7/KrlM8Qxa09YQUQpa/WzZ4RHETG" +
                "Ch+SP3fpAmmIShn32KORUzn6OeHL31gbDkDV7dl2tNn+I9rnjLbnEbIJPWC5bWMAVLl/f+/3STJlbKysybWovqzflS0ZfvVYgzTj" +
                "uaD27i5XV9ghMQsf+XX+sxlhquhezujhAFRLaXEtAMCVQ39YcVzAY9O328Clf1WKXM4ZV88IiwAA0IMOxvtG7qecicNqFmr/tOPB" +
                "/U+dsHy6/c20dPZPb9+1J2H11e98Zs3R9o7Pjxs3PfviPTKOtcmzqKpT/a264O/ykCCRNGzW7Gh+xRjhQLWUHah1HnW1m7o8CRPS" +
                "T3iK8GAAa39r881Bt39r0+nLAACSqNAg70WTPTN3x8txJwoarh3ZSfOnxbwvnqtNS1+y+p2DKxQA1q9e23K0uX9iH7qI1alZLjLT" +
                "tPExcsHY68zQyFfo9n52ysV/S7dn8Cc4+Yg0T2TwAeBaydvvl5spAKa3+aNNaz+8DQB33Z82U8DnePvnznv+HytlALS5pXcarbpE" +
                "8sAb7+aIAbq/+POGQ55p0XfZVKlUs3X6zvFenkDhVEvpgXMAIM96YJY4MjkjKysrK2vRw7/dnC2cmH6W8pE3t6byAGwVOx4O5wqD" +
                "uaLY5e9dASB3P7358egQpWz4bUdCsv5SmBl0RzZ9OxWmyf/adoc7TpbiVzt3LhUAdH360obDVwfA76prNpuvNNZUGW+Mc9fmP9xQ" +
                "d9wpP3NJjFiuFLnaR0LTfrNEMMHRz1Nv+LyyaPX9apWQMH02iojC4haveOXg7tXxyuhfRAh99+ucqKff2nj3Hen36VR7s7G6+tJt" +
                "R8CR7f31xOHDhzUaDTgsjd/WmSkQq1OT3PMyYzcbqxpvOoA7MyFZ1X62zkwFmmySVGx/JXh+/LtSA4TPD+msqWi2AjciOWXODK/h" +
                "Qd2sL6+/SbNk8anxMg44LA3ldZ2OAJV4Hu303Wi52tJ+q8fuAAA2XyxVRc6KUgjZnpYIY+7V3uW6GLT16tnqlj4AEERrtbNcl99/" +
                "Xd6i5L0BOsWSJ6TRbQ15eXkjv54AZgiNRmMwGBhkCjAYDBqNZuR5fMKMz/fRPoL20T6C9tE+gvbRPoL20T6C9tE+gvbRPoL20T6C" +
                "9tE+gvanN8NeCzt27Fh5eTlKmXRMJpPf8543SvDfUKeUMf4NFcF5H+0jaP/nwP8BEHDk76672RQAAAAASUVORK5CYII=";

        ENDREPEATUNTIL_ICON = "data:image/png;base64," +
                "iVBORw0KGgoAAAANSUhEUgAAAIsAAABICAIAAABJB5GyAAAAhnpUWHRSYXcgcHJvZmlsZSB0eXBlIGV4aWYAAHjaVY7bCcRACEX/" +
                "rSIl+Bof5eyGBLaDLT/KJAw5H3q5yEE4/r8TtoaQQYeHpRkWmpr8qRA4EURipN41J/cWqsSrBuEZLMNR16He/cMQCztd3W3YbjuX" +
                "nQ8hiZotaiv2G7kk8X0+evcabzlcSfksVsUv88kAAAoEaVRYdFhNTDpjb20uYWRvYmUueG1wAAAAAAA8P3hwYWNrZXQgYmVnaW49" +
                "Iu+7vyIgaWQ9Ilc1TTBNcENlaGlIenJlU3pOVGN6a2M5ZCI/Pgo8eDp4bXBtZXRhIHhtbG5zOng9ImFkb2JlOm5zOm1ldGEvIiB4" +
                "OnhtcHRrPSJYTVAgQ29yZSA0LjQuMC1FeGl2MiI+CiA8cmRmOlJERiB4bWxuczpyZGY9Imh0dHA6Ly93d3cudzMub3JnLzE5OTkv" +
                "MDIvMjItcmRmLXN5bnRheC1ucyMiPgogIDxyZGY6RGVzY3JpcHRpb24gcmRmOmFib3V0PSIiCiAgICB4bWxuczpleGlmPSJodHRw" +
                "Oi8vbnMuYWRvYmUuY29tL2V4aWYvMS4wLyIKICAgIHhtbG5zOnRpZmY9Imh0dHA6Ly9ucy5hZG9iZS5jb20vdGlmZi8xLjAvIgog" +
                "ICBleGlmOlBpeGVsWERpbWVuc2lvbj0iMTM5IgogICBleGlmOlBpeGVsWURpbWVuc2lvbj0iNzIiCiAgIHRpZmY6SW1hZ2VXaWR0" +
                "aD0iMTM5IgogICB0aWZmOkltYWdlSGVpZ2h0PSI3MiIKICAgdGlmZjpPcmllbnRhdGlvbj0iMSIvPgogPC9yZGY6UkRGPgo8L3g6" +
                "eG1wbWV0YT4KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "CiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgIAo8P3hwYWNrZXQgZW5kPSJ3Ij8+O89GnAAAAANzQklUCAgI2+FP4AAACRtJ" +
                "REFUeNrtnXtQU1cex383N++EJCQh4f0oYsOrCKYiIAa1Mp06ta2ittpKO4W6Dq1dtdqxK2ynPulq11ZFpTug1tra2tER3Lq1rq62" +
                "CxQBEQgoiPKwQgB5JTxvkv0jCXmQQOKjBPd8/wr33vM7957PPff87u+bCZhWqwUkJxYJDQEihIQIPdEiW/x99uzZvr4+NC4TIolE" +
                "Eh4ePhahkydPZmRkhIWFocGaEBUWFsrlcjabbZNQW1tbQkJCVlYWGqwJEY/HU6vVaB1CmQISIoQIISFCSIgQIoSECCEhQogQEiKE" +
                "CCEhQkiTmpDy0goeZl3S7OZuw17Ooh8UGkMb1a8pbhiGTcmoHLQWgekeOjd5y8mqHg0i9ChF4/BczeRBv1tc3qEf5d5TaWu+axg2" +
                "a9HfcLX4jkpjFoHrQoP+VvnFoxlLwoKX5twamgyXTp4UgELWH9n3vIiGm23EKYqRz63fb933p8SdCXx8nAga5e2LOdu2nKj+/YfU" +
                "hX8NLdwe7YIhQo9EnKnS6WKqxTOQBADA8uGomnqqDm099uaM90KZ40WIjZsT53M/9K3zffJ/7Dr99tHXpzCcmtETkCmI576Z4ALQ" +
                "e+HTzHO/D49/PEbxX/zhAhcAaC8puNagdPIFaXIQku9Onh/mLR6RZ9QHRSNfdyGokWnro0gAd7/5JOu3LnsGnOYZ4QkA0H2vvaN7" +
                "UIsIPbwGezrb2xQjuldbWVJyq9cwtGTPZbtWewIQ5VnbTtQN2hFPQ2gAAEg4Ntw/7NyEJkmm8NGpfYmu5ncTaVhjHFqm9C+7n895" +
                "7VzXjzs/i43Dx4vXd7ukGQDA1deVgqFM4RFlCpKY0ZmCSWLnvmj3RyHn0uV3ju/V0McOpb575ovz/QDgMyuCT2dSnRvRk1NToAa/" +
                "90WyAECjaBzjG5navvq8TS+nnh8AoEevXODLEXKdnNDkmEPy3cnzs2kUkslYsuMPXkgzz9G4si2Zs4+nXB62GQEf7m7v1r2nei7Y" +
                "vHGeSOjvwURPuUeUKVgkAIrq0pI7FtvIPsv3rN8etbN+rAgMUfCMxKSVr80JFHgGP+3m5DPI2QmxE77u6NxcWK4grK9N797r3FJa" +
                "riAohi2MyB21ve9cLWnsBwCqrQg4zYUv9vbzFbFw5785nX4O4bzgOFnwGO+rlntJ7IAZsgAHIji5kPuACCEhQoiQDSeN7hGxcONx" +
                "uVIL41ltaqsHkAWS+WnZV7s0djQfEXFnv1S3g7XwzH0tAAxWfRyE2RI76ede07ehtuNxGIZhnFf/rTQpAd3LjcYwDOMnX1Ypx3QF" +
                "Pzyxaey+DM1jvmrVmH7+4+aQwUnjsikw2HI9728rpr/wefXg6APMrLaye8bqpO4AHocO6vs3fs5aFR2fXqyyuznRnJ9Toq/cnM/M" +
                "/m87ARiV7+Mt0J0V0yIMX6y5USTvVI+qKrSVF8i7LDcTrdcKb6r0XVl1BZtrVW5eY/VVo68V9tQUlBnSx56agrLWBzcLHcvlTJw0" +
                "jarm2NpVh6oGrmRuO7Noj3DUASbJFAMfKX6NHKAdbDqbkbLjF2XlvvTji9Zq7WpONOfnlgJQJbPFNZebSvOu3EiJjgtac+Hm4uvl" +
                "DSpNf0nGwjUX+iB47eH9L4hoOAYAGJXygE9yK64g2XvpsWsbeuuabPZ1f+KzbaOTFhOcnpP96q9aRXXtzVa2dvQBZs9J+egI07b9" +
                "86DsaGdP3fUqo69jvbkeUFN+bikAJSJpmfSbywdu6RAJhWSGV8RMLwDlgBAfCfOszTB2yLYrSGJ4Rcz0sdWX8hI2MeuQ9RLXgHJI" +
                "CwBUFnW490FmsVajq07jVBgg7LAAiKb8nFIAUkhi1NTEJUEAA6V5V250EY9heXbUFXS+XE473Fn1bfonxQBAi5g7hYmTjOUvm1ab" +
                "RQmz+ljm6S4AEEVKeDg2fnOiKT+3DAAkidNc3aYnJQU8RkQP4Ao6ByH59lek7jQMw0hUftiK3EYA3qz3358l4LsZvH6rVpvJN5/0" +
                "DERCF1bI6nNKAFZcalKgC5uCjdfcAGjq/Ol8rlgUsvhl78c5ixx3BZ2j6kPj8Jg4phnq6lZpAcSL/v7lu5ECUVCQoEafCNiy2kij" +
                "a6A4N2Dmi2+lvRHvIfbrI4/dHIBozMstAwCoP/rnlacZNFzT3Qx6RCnRQqF9F4LpyuOaYbXW9IEwQOh2mvXsoCvoHIT0uRapv3Lv" +
                "66tOtLT+dLpqwxsfhArIyhpjIjDaagMAw+uHOQOMwuJ7+Af60YvGaQ5EY/7hMt2nzpa7ncYdDiHCaG6ebADlQFNdh9H8VtZcvgUA" +
                "4OrDo5gu9I64gk60DnGmSmPj5qUeOLLSDUD5nx0ZJ+oGHDP6OZIYmV6zY6VhAULG+CdhmEFuSYf+dUmvi3k74+kOPuhY4Uvj6QBw" +
                "59jnXxUoCABtX913G1Z/2w0AvnNi3Rl0sikj+1xBp8wUMNfndh18hQPQc37zuqPGx7TlUi8WiwOTTrXZi9BWc6Ix7/A1ABDKnvPn" +
                "eEfFy2QymSxhwTsbE5mOISKJXvxsawwNQFW0e4EnlcmmsoKWfVkPgD21fOMSP65IYD59da4g5aGG2PKiPMLTflH9Adk2ye2lvXvn" +
                "MwA6f9y07uvbQ2A1U1AoFPXVpcVV7XZmrNabXy8/rQM0e14ARyhi6U8a48W+PY/h4CyiSdb99Ft26hyJmIlp+1UExvIImbvy4yP7" +
                "U8NEfk97jXJcyT7L96x/6qEQWVxUS11VSUltt9reyWD6C4AHDhyoqKiw/qsx6q7qwnIFARxJTOTIOqEdVFQVV3eoger+TJS45apN" +
                "q00SEynGrUUYFd9G82ncttKiOiVQvaKip7iY3FdER2VBZYeGJAiLCROQQd0lLyhvU9voxFiK629vvN3Ycr93UA0AOJ3DF3v7+7gx" +
                "ceOZMAOelfrqgWmUt/WuIMNPKvXX3yLW+zIdKGGfjYsiCZ+JDXW1zD54PF5DQwOXy30gQkiPX1YJIffhyXAfkBAhJEQIEUJChBAh" +
                "JEQICRFChJAQISRECBFCQoT+n2Tm7gcGBmZmZkqlUjQuEyJvb28KxdLQxSz+h1dFRcXQ0BAarAmRu7u7l5fXOISQ0DqEhAghQkiI" +
                "EJJR/wPmE4BT+xQnwQAAAABJRU5ErkJggg==";

        REPLY_ICON = "data:image/png;base64," +
                "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAABGdBTUEAAK/INwWK6QAAABl0RVh0U29mdHdhcmUAQWRvYmUgSW1h" +
                "Z2VSZWFkeXHJZTwAAAGwSURBVHjaYvz//z8DNQETA5UB1Q1kwSWx9/zj/7tuPybJMDdVWdwGggwr8DGB8299+kHQwG2HruA2MMhY" +
                "iSGz9gKQxcjAAIy4ma2GDHcJGfr/L24DWyc/ZAjNNAKbt3r6OQYmIK3KzwGXf/jlJxYD/2M30Cf3xP/QLCOGf//+MzACU9W/P/8Y" +
                "ksrOMMASGNBshgXdpgxPvv5C1fjvH6aBXtlH/4dlGANd/w9oIVAr0MTgNEOwIWBHQA2MKzrJsLTfnOH5998IzX/RvOyRcfh/cLIx" +
                "w+8f/1A040xzjIwMbExIKv79QRjonnrwv2uAGsPHVx+JSiJ/gT5gAlrHxsiEEilw3s7Z9ow71t1gYGZhJgr//fWHgRnoODYgAcMg" +
                "L6PklD1zHRm3LLtMnAuBEcAM1M0BNAiGMcIQnEMWOjM6xuz+HwQMSxhYN/csIkyhQbZ7oQuYxhmGyGD/EldGh+hd/4NTTKCp4R/D" +
                "vqVuWF3KxsyEP9nAQGeVEkNp8ykwuyRVGxhejITD4R+enLJu30mGQyuiSStq/v8DJlscBSy4tDm4j7TSxt4Jt4GDpoAFCDAA8g+i" +
                "2I58094AAAAASUVORK5CYII=";

        RETHROW_ICON = "data:image/png;base64," +
                "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAABGdBTUEAAK/INwWK6QAAABl0RVh0U29mdHdhcmUAQWRvYmUgSW1h" +
                "Z2VSZWFkeXHJZTwAAAGiSURBVHjaYvz//z8DOcBsTiRY46mU5YzI4kwMZIK/f/4yBPJ8ZjCeEYbiIkZiXLjm0qb/1189YLj2+iEQ" +
                "P4CL11pC6ObjDAwX89YyEm3gy7fb/3/98YThy8/HDF9/PMaqJm7dC4bbNdsZWZAFz2xp+X/37FowW9k4mMHEp4aR2CBYFCTBIF/r" +
                "8h8lDNfObWXwdgoE47W9rSSFacTyxwwPm/eguvDHD2Bg//wK4QggxPfe+cpw+fk7hsvPXgJphJd7/fjBdPGmj2DDQGwUAwU4gAb+" +
                "+Axlc8DFo8xDMLwuVWr///bdnwyTz35meNZ9EC6PaqCEAMOfbx/BbA4BDrxe/P3zN0PfsXcMrycdQ7GMBSN9fYcaSCDM0A3Cmg5f" +
                "Pzr3//uWfDCb02cig6icESNFOQVkQP/mMwwgjM+wgcspIC9vq3cGs70a98JdSUpOQTFwZqbg/1DvSDB79dblDOnT3xNtICxxo8Ty" +
                "gwc/4MkGxKY4pxTNPMowLd8aziYnpzCAvEwOliyx+9+23Ps/iEYWZyS3xBbNs/qPLYGTbSAuABBgAN3bN/tgtzFzAAAAAElFTkSu" +
                "QmCC";

        SCOPE_ICON = "data:image/png;base64," +
                "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAABGdBTUEAAK/INwWK6QAAABl0RVh0U29mdHdhcmUAQWRvYmUgSW1h" +
                "Z2VSZWFkeXHJZTwAAAFzSURBVHja5FS9SgRBDE7ERvQRlC1OEEtBQRAstBBBsPLgwM5GX8JXsLBQOC0s5NQHEBWut7MUCytf4ARF" +
                "2Ekmzt/e7t7OISzbOTDLZpL58iVfGBQRaHJNQMOrccDJ7Of1/UOeX95qgawuLcBiaw6dYXto98XNoxBTrd01dzOcIUNiDd3bJ0Cj" +
                "kZcJwaXEXDRxJ+YrCN5pg/1ZpWQigsP97Voln18/VEVRxLWFSBVXGaYjgGe9Pvx8f0UBpqZn4KizCcXqKoBKUenS52AAB+2NCBzC" +
                "5V1/hGEE0C6ttW+24J9lai1BqDFzaLMwZ4DGToM9ws5ABZ9vkc1t7WjJFkDCWCjTFyJdGpswK85nAcVNlY+NMGQDwI6eILoE5Fhg" +
                "AU47EOejwN4kVLEe2rEhzpVOklk4vbqP9m++lRRipTRyQ0A2h8WetbdWzHe5xNBL4G0usFekq4B7O2twfNID+zyO1RhzVZ12Ibaz" +
                "u56H/L8H9leAAQDk6AG97WYpAAAAAABJRU5ErkJggg==";

        SEQUENCE_ICON = "data:image/png;base64," +
                "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAABGdBTUEAAK/INwWK6QAAABl0RVh0U29mdHdhcmUAQWRvYmUgSW1h" +
                "Z2VSZWFkeXHJZTwAAACwSURBVHjaYvz//z8DNQETA5UBCzbBC4/f/t959TFBze7asgwGssKMKIIgL6Pjjs2n/hMDOjaf/o+uF6sL" +
                "GX7/Yvj+6y/DfwZGIAQqhAqDnAJm/4dy/vwkzssMXz8yfPr+B2wcTD/MQAaYBSDO108YWhmxxfKFWw//7zx+EREscMPQwtBSn8FA" +
                "TZ6RoIHoILNxJliRAAcHQ3t5PCM+tdhdOBrLo7E8GsuDIZZJAQABBgBQxF8YWzJVFAAAAABJRU5ErkJggg==";

        TERMINATIONHANDLER_ICON = "data:image/png;base64," +
                "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAIAAAAC64paAAAKO2lDQ1BQaG90b3Nob3AgSUNDIHByb2ZpbGUAAHicnZZ3VFPZFofP" +
                "vTe9UJIQipTQa2hSAkgNvUiRLioxCRBKwJAAIjZEVHBEUZGmCDIo4ICjQ5GxIoqFAVGx6wQZRNRxcBQblklkrRnfvHnvzZvfH/d+" +
                "a5+9z91n733WugCQ/IMFwkxYCYAMoVgU4efFiI2LZ2AHAQzwAANsAOBws7NCFvhGApkCfNiMbJkT+Be9ug4g+fsq0z+MwQD/n5S5" +
                "WSIxAFCYjOfy+NlcGRfJOD1XnCW3T8mYtjRNzjBKziJZgjJWk3PyLFt89pllDznzMoQ8GctzzuJl8OTcJ+ONORK+jJFgGRfnCPi5" +
                "Mr4mY4N0SYZAxm/ksRl8TjYAKJLcLuZzU2RsLWOSKDKCLeN5AOBIyV/w0i9YzM8Tyw/FzsxaLhIkp4gZJlxTho2TE4vhz89N54vF" +
                "zDAON40j4jHYmRlZHOFyAGbP/FkUeW0ZsiI72Dg5ODBtLW2+KNR/Xfybkvd2ll6Ef+4ZRB/4w/ZXfpkNALCmZbXZ+odtaRUAXesB" +
                "ULv9h81gLwCKsr51Dn1xHrp8XlLE4ixnK6vc3FxLAZ9rKS/o7/qfDn9DX3zPUr7d7+VhePOTOJJ0MUNeN25meqZExMjO4nD5DOaf" +
                "h/gfB/51HhYR/CS+iC+URUTLpkwgTJa1W8gTiAWZQoZA+J+a+A/D/qTZuZaJ2vgR0JZYAqUhGkB+HgAoKhEgCXtkK9DvfQvGRwP5" +
                "zYvRmZid+8+C/n1XuEz+yBYkf45jR0QyuBJRzuya/FoCNCAARUAD6kAb6AMTwAS2wBG4AA/gAwJBKIgEcWAx4IIUkAFEIBcUgLWg" +
                "GJSCrWAnqAZ1oBE0gzZwGHSBY+A0OAcugctgBNwBUjAOnoAp8ArMQBCEhcgQFVKHdCBDyByyhViQG+QDBUMRUByUCCVDQkgCFUDr" +
                "oFKoHKqG6qFm6FvoKHQaugANQ7egUWgS+hV6ByMwCabBWrARbAWzYE84CI6EF8HJ8DI4Hy6Ct8CVcAN8EO6ET8OX4BFYCj+BpxGA" +
                "EBE6ooswERbCRkKReCQJESGrkBKkAmlA2pAepB+5ikiRp8hbFAZFRTFQTJQLyh8VheKilqFWoTajqlEHUJ2oPtRV1ChqCvURTUZr" +
                "os3RzugAdCw6GZ2LLkZXoJvQHeiz6BH0OPoVBoOhY4wxjhh/TBwmFbMCsxmzG9OOOYUZxoxhprFYrDrWHOuKDcVysGJsMbYKexB7" +
                "EnsFO459gyPidHC2OF9cPE6IK8RV4FpwJ3BXcBO4GbwS3hDvjA/F8/DL8WX4RnwPfgg/jp8hKBOMCa6ESEIqYS2hktBGOEu4S3hB" +
                "JBL1iE7EcKKAuIZYSTxEPE8cJb4lUUhmJDYpgSQhbSHtJ50i3SK9IJPJRmQPcjxZTN5CbiafId8nv1GgKlgqBCjwFFYr1Ch0KlxR" +
                "eKaIVzRU9FRcrJivWKF4RHFI8akSXslIia3EUVqlVKN0VOmG0rQyVdlGOVQ5Q3mzcovyBeVHFCzFiOJD4VGKKPsoZyhjVISqT2VT" +
                "udR11EbqWeo4DUMzpgXQUmmltG9og7QpFYqKnUq0Sp5KjcpxFSkdoRvRA+jp9DL6Yfp1+jtVLVVPVb7qJtU21Suqr9XmqHmo8dVK" +
                "1NrVRtTeqTPUfdTT1Lepd6nf00BpmGmEa+Rq7NE4q/F0Dm2OyxzunJI5h+fc1oQ1zTQjNFdo7tMc0JzW0tby08rSqtI6o/VUm67t" +
                "oZ2qvUP7hPakDlXHTUegs0PnpM5jhgrDk5HOqGT0MaZ0NXX9dSW69bqDujN6xnpReoV67Xr39An6LP0k/R36vfpTBjoGIQYFBq0G" +
                "tw3xhizDFMNdhv2Gr42MjWKMNhh1GT0yVjMOMM43bjW+a0I2cTdZZtJgcs0UY8oyTTPdbXrZDDazN0sxqzEbMofNHcwF5rvNhy3Q" +
                "Fk4WQosGixtMEtOTmcNsZY5a0i2DLQstuyyfWRlYxVtts+q3+mhtb51u3Wh9x4ZiE2hTaNNj86utmS3Xtsb22lzyXN+5q+d2z31u" +
                "Z27Ht9tjd9Oeah9iv8G+1/6Dg6ODyKHNYdLRwDHRsdbxBovGCmNtZp13Qjt5Oa12Oub01tnBWex82PkXF6ZLmkuLy6N5xvP48xrn" +
                "jbnquXJc612lbgy3RLe9blJ3XXeOe4P7Aw99D55Hk8eEp6lnqudBz2de1l4irw6v12xn9kr2KW/E28+7xHvQh+IT5VPtc99XzzfZ" +
                "t9V3ys/eb4XfKX+0f5D/Nv8bAVoB3IDmgKlAx8CVgX1BpKAFQdVBD4LNgkXBPSFwSGDI9pC78w3nC+d3hYLQgNDtoffCjMOWhX0f" +
                "jgkPC68JfxhhE1EQ0b+AumDJgpYFryK9Issi70SZREmieqMVoxOim6Nfx3jHlMdIY61iV8ZeitOIE8R1x2Pjo+Ob4qcX+izcuXA8" +
                "wT6hOOH6IuNFeYsuLNZYnL74+BLFJZwlRxLRiTGJLYnvOaGcBs700oCltUunuGzuLu4TngdvB2+S78ov508kuSaVJz1Kdk3enjyZ" +
                "4p5SkfJUwBZUC56n+qfWpb5OC03bn/YpPSa9PQOXkZhxVEgRpgn7MrUz8zKHs8yzirOky5yX7Vw2JQoSNWVD2Yuyu8U02c/UgMRE" +
                "sl4ymuOWU5PzJjc690iecp4wb2C52fJNyyfyffO/XoFawV3RW6BbsLZgdKXnyvpV0Kqlq3pX668uWj2+xm/NgbWEtWlrfyi0Liwv" +
                "fLkuZl1PkVbRmqKx9X7rW4sVikXFNza4bKjbiNoo2Di4ae6mqk0fS3glF0utSytK32/mbr74lc1XlV992pK0ZbDMoWzPVsxW4dbr" +
                "29y3HShXLs8vH9sesr1zB2NHyY6XO5fsvFBhV1G3i7BLsktaGVzZXWVQtbXqfXVK9UiNV017rWbtptrXu3m7r+zx2NNWp1VXWvdu" +
                "r2DvzXq/+s4Go4aKfZh9OfseNkY39n/N+rq5SaOptOnDfuF+6YGIA33Njs3NLZotZa1wq6R18mDCwcvfeH/T3cZsq2+nt5ceAock" +
                "hx5/m/jt9cNBh3uPsI60fWf4XW0HtaOkE+pc3jnVldIl7Y7rHj4aeLS3x6Wn43vL7/cf0z1Wc1zleNkJwomiE59O5p+cPpV16unp" +
                "5NNjvUt675yJPXOtL7xv8GzQ2fPnfM+d6ffsP3ne9fyxC84Xjl5kXey65HCpc8B+oOMH+x86Bh0GO4cch7ovO13uGZ43fOKK+5XT" +
                "V72vnrsWcO3SyPyR4etR12/eSLghvcm7+ehW+q3nt3Nuz9xZcxd9t+Se0r2K+5r3G340/bFd6iA9Puo9OvBgwYM7Y9yxJz9l//R+" +
                "vOgh+WHFhM5E8yPbR8cmfScvP174ePxJ1pOZp8U/K/9c+8zk2Xe/ePwyMBU7Nf5c9PzTr5tfqL/Y/9LuZe902PT9VxmvZl6XvFF/" +
                "c+At623/u5h3EzO577HvKz+Yfuj5GPTx7qeMT59+A/eE8/uo9fd9AAAABGdBTUEAALGOfPtRkwAAACBjSFJNAAB6JQAAgIMAAPn/" +
                "AACA6QAAdTAAAOpgAAA6mAAAF2+SX8VGAAAC2ElEQVR4nGL8//8/A7kAIIBYgPj+3Q/Ht10jSZull5aisgBAADEAbV484cgvEsGS" +
                "CUeAGgECCGTzPwaQy18+/QHk//n778/PPz9///3969/f33///P3PxMDAyMzIxMTIwsLEzMrExsKsqM4P8SxAAIE0Q3z988cvdi5m" +
                "EJ+dmZGFkY31379/zEAZRgYGoE5GJkZmoBHMTL9//AUq+Qt2PEAAgRT//f/3/79/33/8ZWJhlH5x5m9TE1Z/MtfVvZA2Bbri/z+g" +
                "cpB2gAACOxvE+ff7x58/7ExAnRzd07Fq/lGayTRzC9ByoOK/YNcCBBDEZqBZ///8+fv37z+G379AJh07DrbrH9DJECcyWVkCpUCe" +
                "/8cAtAuiGSCAwDaDlP8DagSJ/P7N8OMHw5cvICOZGYEkA4hk+Pfty//fv4Hhxsz0H6wYaC4DQABBnA20+B8wVJgYGIHe///mw58b" +
                "jxhB9kHCC+wIHR2gQcxMjAwsQDtAHgUKAgQQSPPv/6CQZwJqZWL48+o1+5PHf25dRw+tp7pAKaAF4Nj5D3E2QABBbWZkZGRmArmJ" +
                "iYvrv4AAs6gYA6rNDAKCQClmFqAXgPH2H+JsgAACawamBCYmFjZmYEwyMrMwiYkyK8kDLQNr+gtiMDIABUFSTIz/GYBxzvT3H8hm" +
                "gACCxjNQMysLKA0w/PnOyMrIGuAB0vaHgYGFGebwvyApYDpjhIQZKA4AAggS2v+BzmZhZwbqZ50x61tKHNZ4Zp0ziwEYyf+BzgaF" +
                "MFAEIIBAmv/8/wd0MStQKzPTeyVrxgNXGEGAAUwyggMSCBh/M4G4wBQLVPwHHNoAAQRN20BJNjZmIANkJMhbDMBwBQY+KHiBdvxn" +
                "Agn/BwYeyFCQz8GaAQII4uy/QDFxKS6srsUKwIHNABBAQDP+AwuD+Qv2MgANZoSVKv8ZIZKgqPoPii6w5H8QEyyYmOAELAwAAoiR" +
                "kmIIIMAAg4ptX+LonLMAAAAASUVORK5CYII=";

        THROW_ICON = "data:image/png;base64," +
                "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAABGdBTUEAAK/INwWK6QAAABl0RVh0U29mdHdhcmUAQWRvYmUgSW1h" +
                "Z2VSZWFkeXHJZTwAAAF3SURBVHjaYvz//z8DNQETA5XB4DeQBZlzZkvL/7tn14LZysbBDCY+NYwkmwiKFBiuCOD4//lQIxhXOHD8" +
                "R5YjFqO48McPBoa/P79COAL4HWI2JxKcPE6lLGfEGYYCHEADf3wGYwEODrwG/v3zlyGQ5zOD8Yyw/zjDUEBCgOHPt49gNocAwsA1" +
                "lzb9v/7qAcO11w+B+AFcXFWZnaGS4S+D/qTg/xfz1jJiGAi2+TvUQCQxW2lWBiNhToYvStwMX3/wYbh2hgvQ8BbP/7drtjOieDmq" +
                "civD768fwTgIyCYFLAqSYJCvdfmP4kJROSPGws1nwGHSn2VEUpKJWP6Y4WHzHkZGYvLyspNr/l9+fpfh8rN7DCAaBnr9+MF08aaP" +
                "YMMw0uGrh2f/L0gQAGMQG196kyyx+9+23Ps/iEYWR1E0I0Pg/9vNmWAMYuMzUCTX8j8I403YDx78gCcbEBsfeD3pGCPBrAfyZgMw" +
                "+4EwIS/jwowjr4AFCDAAMzFYFvLEd+kAAAAASUVORK5CYII=";

        WAIT_ICON = "data:image/png;base64," +
                "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAABGdBTUEAAK/INwWK6QAAABl0RVh0U29mdHdhcmUAQWRvYmUgSW1h" +
                "Z2VSZWFkeXHJZTwAAAJ4SURBVHjatFRfSFNRGP+d3VlQutDYroVTJ8u2taKZilJiFFkQ9FAi5LMgRC9iIL30EP15iQYRgRDRQxhF" +
                "EEEFKgZSpGZtLda2Qp06Z5utpNVbm6fzbW1x7zZ7yD74cbnf9/v9znfv+c5hnHOsZWgLFV4HFvnouyBefVyAP/QZkeXvKC8tgdWo" +
                "R2NtBVp3mVBv2crUOpavw/4hFx8YdqPZXo0mmxG2ahlbNuvw6Uscvtkoxn0hjHln0XnIge62Oraq4eX7z3l0+Qc6D+6GXRhlgjMi" +
                "iyeJBLzCeGDkLeTSYpztaMmaahSdDbpSZhe6DmPYExRVlsX+3mtIajVgEoPz0TjsNeUpHvFJ92dl0SHBvRDjLWdu8rGZCI+LhBpl" +
                "Jy7yp97pnDzxSTfpD3PyyXY44ppCk/hnFpOMpHhXIxPqPPFJRxuo+OSJ9yHssRiREKx8oFgpUCMdTYNibPziJ5srZPxMFJ6x5Ary" +
                "1klHo6XoMPI1Dr2uBLcev0QywXOQNuRIqPLEJx3NqaJD/aZieIOLONK4A1Fhro4rp4+h69I9DDlP/R6cdBCfdDT0ig5rKw0Ix75h" +
                "44b1edG804zrvcfR1nMjp0Y6OkEKw7rtRnimwpAkTUE0WGvQ39eBvd1XFXnS0XFUGLY6zHB/CGEmHMM6rZSDoiIJWvGst5rgut2X" +
                "zROfdHS2lYbbDKz9gAMPRz1g4mTQyncGJ7JdaJmAlEamRjziky5zUeSc5XN3X/AlsWNEslTJBUcoMBfFg2duGMRmnD+5j6162zif" +
                "uDiRG2xVYmgrYREbJpfpUrsfmF/Cm8A8Jn1zqUV7jv7ltvkv9+G/hAZrHL8EGADyfG8UmGquvwAAAABJRU5ErkJggg==";

        WHILE_ICON = "data:image/png;base64," +
                "iVBORw0KGgoAAAANSUhEUgAAAG0AAABACAIAAACfqG7pAAAAhnpUWHRSYXcgcHJvZmlsZSB0eXBlIGV4aWYAAHjaVY7bCcRACEX/" +
                "rSIl+Bof5SzZBLaDLT/KJAw5H3q5yEE4/r8TtoaQQYeHpRkWmpr8qRA4EURipN41J/cWqsSrBuEZLMNR16He/cMQCztd3W3YbjuX" +
                "nQ8hiZotaiv2G7kk9n0+evf6diNcSXcsTvPFwaAAAAoEaVRYdFhNTDpjb20uYWRvYmUueG1wAAAAAAA8P3hwYWNrZXQgYmVnaW49" +
                "Iu+7vyIgaWQ9Ilc1TTBNcENlaGlIenJlU3pOVGN6a2M5ZCI/Pgo8eDp4bXBtZXRhIHhtbG5zOng9ImFkb2JlOm5zOm1ldGEvIiB4" +
                "OnhtcHRrPSJYTVAgQ29yZSA0LjQuMC1FeGl2MiI+CiA8cmRmOlJERiB4bWxuczpyZGY9Imh0dHA6Ly93d3cudzMub3JnLzE5OTkv" +
                "MDIvMjItcmRmLXN5bnRheC1ucyMiPgogIDxyZGY6RGVzY3JpcHRpb24gcmRmOmFib3V0PSIiCiAgICB4bWxuczpleGlmPSJodHRw" +
                "Oi8vbnMuYWRvYmUuY29tL2V4aWYvMS4wLyIKICAgIHhtbG5zOnRpZmY9Imh0dHA6Ly9ucy5hZG9iZS5jb20vdGlmZi8xLjAvIgog" +
                "ICBleGlmOlBpeGVsWERpbWVuc2lvbj0iMTA5IgogICBleGlmOlBpeGVsWURpbWVuc2lvbj0iNjQiCiAgIHRpZmY6SW1hZ2VXaWR0" +
                "aD0iMTA5IgogICB0aWZmOkltYWdlSGVpZ2h0PSI2NCIKICAgdGlmZjpPcmllbnRhdGlvbj0iMSIvPgogPC9yZGY6UkRGPgo8L3g6" +
                "eG1wbWV0YT4KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "CiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgIAo8P3hwYWNrZXQgZW5kPSJ3Ij8+utyfWQAAAANzQklUCAgI2+FP4AAABJtJ" +
                "REFUeNrtnMtPG1cUh38X22Cb8WAbaggPp7yxQRAIpDyMiBJI01CltEoqVay66C5Si1ClSJWyatWiLmj/gkrZtIuoi0ZpobSExuFp" +
                "UzAYbCiER40JhPIw2LwM00UdMBaISmApTs63u3PvPZr5dOecO+ORmSAIIE5MGCkgj+TxpUPs31heXq6rq3M4HOTlWPR6fUNDg1Qq" +
                "PcSj1Wo1m82NjY2k6Vhqa2vr6+u1Wu0hHgEolcrKykrSdCx7K5HyI9UZ8kgeCfJIHskjeSTII3kkjwR5JI/kkTwS5JE8vooe182f" +
                "JDLGmOL9llXfIe/Et3mMMcZY/K3udd9BT/tHGsYYi/nQuOCbEvXBwzW/SCsPrkoYY0z35cjWXljfmPXDpwScw2GEXf5pJRQ8yjKq" +
                "SzgAa+13f+xf2gEguCy/2P/rnP357gO7axeAd7an+xkASWra8sDUpgAAO/OWTtvyTmBEz4TJPO3xfawQMOaIKXvIo1QBxIvGOyz/" +
                "eE/do/i0A3K57xaE3Xu06+yzzyxu5KoiN0aa+7d8ndPdw46V6kxetjrUMgoAKeUZPB8lYUG51yIvffHDnUJeErBWxBFh7MX3GKa+" +
                "cF2HR0OYNI0+XatKDXe2G+eA6NJycYdx7q+uEedaRRLGWwc2ASQU56gU0fLgJWmRJucNnVIUinVGknjxSgIAt31g4qnHu9LfNAJE" +
                "ZFZe1ssBl7VvfM6ztdD7eAaA8lzRGV6tECP0CcJSkGZcK1MAcPbaZxcXbU0WL5BUlJV9SQfA0TPsWFwabrEDCM8qO6tQqmS+c3C3" +
                "1l3Rq8TPK4Ly7eYTpjF36+33ipNfU+8TX/61bStk9j1cbk2BCBAmTaOOMWPHAqDJz45OKK1KBoQnXfap4bY+N4DU8nQFHx0ZxK3X" +
                "5pprecmP2XGruf9vTxC+sAvGPcXUF97R4Q/rqs0y1DkxBkjTCxN4jb6qmP9qwjU+NNDW9wRAUkm2ko/hRPtlofHenXzZ3mrq+vT6" +
                "bdPOierMgYA+BK8gACwEPEKcUPFmIqyOmc6HTc+2gZTi5EiFSp1cncu+fzxjMjaPAVDln4/j1Xw4O1AW8gzPy8LKmvrkF+sfMASf" +
                "Z6Tp1ww8sGu7/9sCEFegUyrUkRJVwdV0YNtyv9sNROhLz/JKlZThpSBIyYnLqSkQAdgAIMs8f4ZXKiQsXFtRpgawCQBphjTuxFse" +
                "d+tnNw0Z8bH7JBk+H9w8sjc2Njat9lcXQiM/AmDqoho92gYBILn4dU6hkoUB8qy3zom/a/UC0JZkR/ExJ9/yeFYWPQfT36B5IG/7" +
                "qF5g3tZrshVd1KlEoeAR4pSPLes3ervH3ACgUEeKALCYm7975gc6h5cEAPJoXgIAssJvpl23ev6c2YT/tUVVN60vDHZYFwVIAsf4" +
                "NQ8hPOHGpKvOfEQvgK2d06/Ygh9Go9FgMAjE/0Cr1U5NTe016b0ZvX8kj+SRII/kkTySR4I8kkfySB4J8kgeySNBHskjeXwVOPD7" +
                "TFxcnMPhSE1NJS/HIpfLOY7ba7KA/0FyOp0bGxuk6Vg4jtNoNEd6JCg/kkfySJBH8vji8S+mTeWAX7e8DgAAAABJRU5ErkJggg==";

        ENDWHILE_ICON = "data:image/png;base64," +
                "iVBORw0KGgoAAAANSUhEUgAAAGwAAAA7CAIAAAARj/jVAAAAh3pUWHRSYXcgcHJvZmlsZSB0eXBlIGV4aWYAAHjaVY7bCcNADAT/" +
                "VUVK0Ov0KCc5bEgHKT8SZ3N4PqRlEYPg+H1PeDWEDDo8LM2w0NTkd4XAhSASI/Wuubi2UCXeNQivYBmOug/16m+GWNjp6m7Dpk0u" +
                "Ox9CEjVb1FbsN3JLbN4fPXv5POXwB0xeLH4CFFUMAAAKBGlUWHRYTUw6Y29tLmFkb2JlLnhtcAAAAAAAPD94cGFja2V0IGJlZ2lu" +
                "PSLvu78iIGlkPSJXNU0wTXBDZWhpSHpyZVN6TlRjemtjOWQiPz4KPHg6eG1wbWV0YSB4bWxuczp4PSJhZG9iZTpuczptZXRhLyIg" +
                "eDp4bXB0az0iWE1QIENvcmUgNC40LjAtRXhpdjIiPgogPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5" +
                "LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4KICA8cmRmOkRlc2NyaXB0aW9uIHJkZjphYm91dD0iIgogICAgeG1sbnM6ZXhpZj0iaHR0" +
                "cDovL25zLmFkb2JlLmNvbS9leGlmLzEuMC8iCiAgICB4bWxuczp0aWZmPSJodHRwOi8vbnMuYWRvYmUuY29tL3RpZmYvMS4wLyIK" +
                "ICAgZXhpZjpQaXhlbFhEaW1lbnNpb249IjEwOCIKICAgZXhpZjpQaXhlbFlEaW1lbnNpb249IjU5IgogICB0aWZmOkltYWdlV2lk" +
                "dGg9IjEwOCIKICAgdGlmZjpJbWFnZUhlaWdodD0iNTkiCiAgIHRpZmY6T3JpZW50YXRpb249IjEiLz4KIDwvcmRmOlJERj4KPC94" +
                "OnhtcG1ldGE+CiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAog" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "IAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAKPD94cGFja2V0IGVuZD0idyI/PpWOaIYAAAADc0JJVAgICNvhT+AAAAZ+" +
                "SURBVHja7Zx7TFNXHMe/t9wWWtpLe3mIggoiYIFQRFTUosYHU3BOp04N/+icyUx0PrZlLjrNlqibjwwTRWNYnC7L1JiZjeEwOgXr" +
                "AyhvEIqKoPIGgdZSLG3p/hCklD5ARuKS8/2rPT3nd36/T09/v9N7Ty5lNptBNDJxCAIC8Z0Q/ebVxYsXU1NTCZGh6OjRozKZ7M1b" +
                "6k1OjIuLW7VqVUREBGHkWGlpaQaDISUlxcZKBBATEyOXywkmx3r06FFpaSnJiaSwEIgEIhGBSCASiAQiEYFIIBKIBCIRgUggEogE" +
                "IhGB+A5B7Mrb4U/ZEWfhn2qLDuO2ZGne3HLtaTwXS1EUJT1U2W3DCN9nyty1X6XeaeiGg0lFH11/2dtkrD4u651ma05Xb6Pu7mYf" +
                "iqIor42K1t4hHutvaS0sqdOXcAe50duny/aQoQb+VitR4CGx0jiXqnvFbaa+Dg2nN+y62mQaOEpXrcx7pjNbGBF7CF3xqqVScenw" +
                "5riAmK3p1kMAfkjiLCEA7d3zvxe1mwCYNcV/q3qnuXo+XaXpAWBsyM1pAcANmtxR8lRvBgBTc/H9io5BFge4YdXHzhBngb8w2uxN" +
                "O0DovuDAhX0xDNcKNO1q2VBzbk/yRvmBOR4cJ0Z6up5nX0o+eCa7vfTkyvf9lFm7ZXzKoqMwcmU05/LtnvpCVV3bq0iJ+6vKa0V9" +
                "i/ZZTnmtOjGU4b98cP0hAEyKC2EYDy41Kj9P+4FTw4YIwMUnYqZU7DJo2b8eO8aXamo0FJ/YezYpfXuEwKkR+fz4mYJpc5OrDcrk" +
                "Q3+s/3ltgFu/Vxx2xnIpbj9AjfJho3ZxEK/+rqIJ8JwdR99TND3KrqzXzhuPqpslegB+sRESkadg9DK67cBHobDQQUmbZDSgzTz0" +
                "7ZWn3UNxTTJrxy4ZADTn3s572tlj+SHXf368H4BOVUl1o86oLsqoBFxDFy0MEwCassKqJl13a/6dOgDiqOljGVZE/w+qc+fN3R/G" +
                "Bnqz/RoXd6SiH5bB4LX0m8+CADRf3v/DzWbTUMB7SkNFAKCub2np0A84B+QWkjBHBKA+X9XQ1laRUWwExk+fEr5ACqA2t7y2rb38" +
                "ugoAb8qciSKxhM/p83NnfJiE7isB4mXXjCOD4izw4a1EvVbT0W6hhqqyvKLn/TWD4gZvPbXBG0DVT3tOKF8O4WyUucdoAgAOhzJ2" +
                "GQYOEEauiHYBzDXKh7WPFfdaAZ+p4Z5+sxcHAuYn2aqn5ZmFnQCC4oJFjKf7KG7PnAU+nMLy4+V9U/nWFIyW58goZu7B5CWXkjJ0" +
                "Bcf3X/zaaaI3NBSqdADABrBcyqo7xc74QIqsspcVxQ/uVz8G3IJj/BifsMWxzPfVmqoHJZmFTwCMnxUuZryELnb87Mz+cvlupWlE" +
                "hcV+4NTbFBaZ3EZheWHZZeya498dlX5RqLlxJNXXyTLUFpw5VQ4AAXOnsm4CnpVLtN+89/xRVlt3/1ZGiwGYFBvoLpKwgYmR1G93" +
                "6pSKa48BSKZO82VYZsBYSz/VWpb6DwqLrcBH8R8LN+TT09snANBUNToAqK/POrZu2bEaAML5nyT6e3iLrSHCLThBzgA9FWk3WgHf" +
                "aKlYxLpzJdFLggFDcVpOJ+AaNnsiI5a4UXhnRDvMr3vWyA+60gPcFS36pSCVtV7/MXtPrTubeKHDnhEep1vTqta/Zh6adODz2d5j" +
                "AscKBoMQRqyIdrmQaXoFgB86bSwjFnEp3oR5c1g8bNMDwGT5ZOGIdzeDQ+MFb7v61xIngf8azwx/n6hTt+msmpor8pWqmdYZlsPG" +
                "Hz4278qmLL0DIzx2kkyesC5pebSv58SIEC+ujRkpdvqKMGSWAkBgbIBQJOFzAMGUpVH02ZtGABNmhXswXiPf3QwKzVyaVyIzOA68" +
                "Yvp8qWRoEPkxyc80W3ML6vT2ckD4sWeabbkFdfr+74oe//EtdUJpbmV7DwCuHSMcnrvY09d/4jiJq52lRE/aXty1Oj/ncScAiFh3" +
                "FwCU15p/dM0l98vbzQAEngzXyk/L5OWRmNHVWnqvrM080A0Xp6Hx/FbXaHbm2Q+822S7Ppv7JJfLFQqFmciZUlJStmzZYtlCLoWR" +
                "64kEIoFIRCASiAQigUhEIBKIBCKBSEQgEogEIoFIRCASiP8v9d9jiYqKSkhIoGmaQHEsvV5/8uRJy5b+Z0AYDAatVksYDUVisZiy" +
                "OL5BkYcLkZz4TuhfM5P82+OquDwAAAAASUVORK5CYII=";

        START_ICON = "data:image/png;base64," +
                "iVBORw0KGgoAAAANSUhEUgAAAIAAAACACAYAAADDPmHLAAAACXBIWXMAAAsTAAALEwEAmpwYAAAAIGNIUk0AAHolAACAgwAA+f8A" +
                "AIDpAAB1MAAA6mAAADqYAAAXb5JfxUYAAEIbSURBVHja7L13mGTXWSf8O+GGyp1z9+QZTdIkSaNgy0K2bAzyGmNYDDYYE0z4CGZJ" +
                "y7Mf6wXMx8LuAx+P+RZ2ARvLmCwnLIMccFCWRtLk2D0902E6V1dXvOmc8/1xq6qrq+6tqh6NhcTDfXQ01VW3blXdN//e97wvUUrh" +
                "VhxSShBCAACWZUHXdbiuC8YohBAghIIQQAj/PEoppJRgjMF1HAgpwSgFACgAnHMQALbjgFIKpRSgJBRQvhaBEAK6rsOxbVDGQAjR" +
                "S8XiIGNst1Jqt+O621zX3UYJGfQ8r0sIkRJSGFCAUgoKAJRC5R4opQACj1GWZYxlFLBACKZi0diEEN4EQC5yTZvVdT3rOA50XYeU" +
                "EsLzwBiD4zj+zSAAZxy24yAWjYJrGkrFImzHgWmayGWz0DQN8XgcUvq/CQAcxwGjFIxzOLYN3TDgug7i8QTW1jLo6OhENBqB47jg" +
                "nINz/orpxvE6PAghFWZLeZ530HacO4UQ9wrPO+B53piUMiqV8okLhQrBQYBmxFdQgARc4fbajl09N5/LgRAKAIJzPss5v8Q4ez4W" +
                "iz9LKX2ZUjpLqc+Ut0qgXq2Dv56IDhBIqbY5jvOg57pvdVznXuGJkaoUlQmrysSXStYQ3CcuIaR6buW6ldeVUpBC+o+l/3dZqUFK" +
                "CQDMtu0xy7LGADy0lsmAMZbRdfNEJGJ8mXPty5zzk4QSVdGG/84At0DSlVIDnut+p+M673Yd941CiHhFsismQ0lZlnLfvHBGwbkG" +
                "xhi4xv3HlIJQCkopiP8B66ofClIqCCEghIDnunBcB67jwrZtOI4D13MhpajwDggBXNfrsO3sW3I59RZK2e8Yhv6SGYn8Yzye+Ayl" +
                "9ExFM/w7A9wE4V3Pe5Nt2T9kO9Y7hJC9FUJtkFBKoXEOXddhmCZ0XYemaWCcgeDW3XjP8+A4DkqlEgqFAoqFPEolq2z3FQihkFLS" +
                "YrF0Rz5fuCOzuvpfDMP8quzqeiSZSj1GGcv9OwO0peZhOq77XYV8/qcd132jqlXvypdyxhh004BpmoiYJjRd/5ZLWcXpikaj6O7u" +
                "BgDYto18Loe1tTXkcjmUSiVIKUEpgecJ3XFyb8/lcm+PRMwryWTyY4ZhPsIovfHvDBAs8brjOO9zHOcXHNc9oJQCKUu7lAqUEkTN" +
                "KCLRKEzTvCUe8Cs9DMOAYRjo7umBEAK5bBbpdBqrq6solUoAAEoJSoXSrkKh8Du6bvxCqiP557pu/H+U0lm8BkwDfw0QHq7nfb9l" +
                "Wb/iue7hih2HkpBKgWs6otEoYrEoONdes7aUMYaOzk50dHbCdV2srqaxuLCItUwGQgoQSuE4dt/C/MKvaZr+ox0dHf8rGo3+IWUs" +
                "I4T416PBvxYOUI6b77Ms6zcc235zJTRTvrcN3dARjycQiUQ2rd6F8JDN5rBalsa1TAbZbBYlqwSrWILtOJBKgpYjC0IASgg0w0Ak" +
                "EkEkEkEinkCyI4WOVAdSHR1IJpPQdX3T9yW7toYbc3NYWV6C47iglPoOp1CIxaLj3b09H+nq7H5EQinbsv7t4wBlYvYUC4UPl0ql" +
                "n5JKMT9k80M3wzSQSCRgmpG2r2lZFm7MzmJmZhrTU9NYXFxAPpdHySqVwzffSatGAYRUwSRC4TuLtUymfDCHEgrKKEzTRCwWR1dX" +
                "J/oHBjE8PIy+vn7EE/GW3y2ZSiGZSqFYHMX01DQWFhbguh4Yo8gX8jtzudxfZDoz7xscHPwVxvnLeJVxhFdVA1BKYNv2d5eKpd91" +
                "PW8nKYdgUinouo5Ewpf4do61TAaXL1/G5cuXMD01hczqKjxPgFICzjkoY2CMVYnNyUbiAxU8wNcCAPH/IwSEKv+3EAVCK5CB8plJ" +
                "AZRRxGJx9Pf3Y2zLFmzZuhW9vb1tfe98Po/r169hYX4eUiowSuEJD5xrxb6+vt/t7ev975xrjlUqvSoa4FVhACkllELStu3fLRWL" +
                "PymVAim/hzGGeCKBWCzWUtWXSiVcOH8ep0+dxOTkJHLZLAghftjHuK8+KQOjDJT6BHSkB9vzkHVLKHkObE9BEQ9UEzBNhUgEiEYY" +
                "4poBzjiI5CDCAPUioMoAkTooWJk5FEAlCAWkLEclUsGImOjv68OOnbuwY+dOJJPJlvcrvbKCqxMTSK+mQSkD4GMQHR0dT46Ojv00" +
                "Y+wM4/z1zwCcMziOe7xYLPxv13EPgZAqaBONxZBMJsEYa3rthYUFPP/sczhz+iSWlpZBCKDpug/yEApOOSglcJSLNa+AuWIGc/lV" +
                "LBRyKNIsiOYi2eWgu1ugo0OhswOIxwDTBDQN0HhZ8gFAMUBwQGggbgTMjYNaneClbmhWD5ibAhURUELLzOAjh1IISKUQi8UwtmUr" +
                "9u3fj5GRkaa/SymFqevXcfXqBGzbBmccnhDgnK+NjY3+ck9v3586jo1YLP76ZADP8yCE94FcLv9RKUXMh3IlOGNIpVIwW6j7qevX" +
                "8cQ3v4GzZ8+imC9AM3RwzsEIg844QBSyooDZ0jLG1xYwmVnBqp2HHnUxPAhsGyMYG2Ho7SKIRQg05qt5Kf1Vdjv8VesAEB9u8v8v" +
                "AaKgFAHxdDA3Aa3UB70wBqM0BM3pAlG8ygxSKQhfpWNoeAgHDhzEtu3bm2q3fD6PSxcvYHFx0dcGSkJIhdHR0f/V3dP9i6lUh7W6" +
                "mn79MIBtWdANg+Vy2f9ZKpY+VAvXRiIRJFOpplI/OzODf/mXr+L0yZOwbRuGYYJxBo34dr0gi5gszONCZhbj6SU4lgWeUNizg2H/" +
                "boYtoxSpBEApIESZ4O38TLXhH9SkDMrPVxhCQIGAehHopQGY+R2IFrdDd7t9X4IJALIa8QwODuLQ4cPYsnVrU20wefUqxq9cgSc8" +
                "UMrgeS6SqdQ3Dx86/IP5Qn4qmUy99hmAEAKrVEpatvUXpZL1LgKgYvOTqRRisVh4yJTN4qtf/jKefvopWKUSDNMEoww65QAD5q1l" +
                "nFmbwoX0DWSzeYARjG5hOHo7w64dBB0p351zPUDJyi/cSGESSveNUUAoE9QzBBFQUKBeDJHCVsTz+xEtbQNTBsA8gEh4ngAhBFu2" +
                "bsHRo8fQ08RhTKfTOH3qFHK5LDjn8DwPyWRyfHh49PtGRkde0nXttcsA5Rz/0Orq6qOO49xdIT5nDB0dHdANI/T9zz37DL742GNY" +
                "XlqCaUZ8uJdpAJG4Zs3jxZUJXEovQBU9kCjDwb0MdxwhGBsm4BrguRulnGygaQDhSZ2o15/VBhOsRxGAJBIKHoiiMKxhpHJHkCje" +
                "Bq6iUMwFoOB5HnRdx959+3Ho8OFQXMGyLJw+fQrzN+bAOIMQAhrX0kfvuOP7+/v7v2RZ1muPAZRSkFJuzWRWP+847sGKl6/rOjq7" +
                "ukJV/tLSIj776U/j1MmT4JxD03RolIMyguuleTy7fAnjmUWgpECjHIcPEhw/RjDYT8pJGp8gtSZ2nfg1JCWbMQGkgQlCtYAiDc9L" +
                "4kIRBdMaRGfuOFKl/WAwoKjPCK7roaenB3fceSeGQxxFpRTOnj2Dq+MTYIxBKglGWfHosWPv7evv/6xS6rXFAJ7n7cxkMl9wXXdP" +
                "hfiRSAQdHR0g5Uqf+uPE88/jM59+FJlMBmYkAkYYDM6x5Gbw1PJ5nE3PAiUFcI79+wjuu5tidAgQ0if8BqKTRsknFfKR1jygGiSf" +
                "hGqBUAaoZxp4kJCIOmPoyz2ApL3LdyipgPAEGKPYt/8ADh0+HCog41eu4Ny5syAgPnpJqXPkyNEfHB0b+7vXDBAkhBhbXV39Ui3x" +
                "o9EoOjo7A893HAef/fSj+OY3vgHGOTRNg041eMTFC5nLeGbhCuySA0iOgRGKB++n2LPDJ6fnqUaiBzJCE+ITNHp7DUzQnhaoNQOq" +
                "3pEsv1cSF1AUnaVDGCg8CFN0Q1KnLDguBgaHcM899yARgh9cv3YNL7/8kn9dqUAoce46fvd/HBoa+ty/OgMIIfozmczjjm0fIsQP" +
                "82KxGFIdHYHnLy8v41OPfAKXLl3ypZ4yGEzDjLOAL8+dxo1sGnA5eITgvuMU9x6nMA3AceAjc2hG9DrVX0980qgFVFAE0IYpCNIC" +
                "YQxQUemCuNBFB4YKb0G3fQyESCgq4XkeYrE4jh8/jqHh4cD7Nj09hRdfOFFOiQOMs+Lxu+/5rt7e3i+/kqKTV8QAUsrI6urqPzu2" +
                "fT8hBEJKxJsQf3JyEh//8z/DyvIyzEgEnDAQBryQuYQn5i/CdQTgcQyNErz9IYotIwTOBo++kQGCbD8px/KtiB+u/ltrgbYYoOF8" +
                "UjUL3fYxjJXeDl0mIKlbTk5RHD12DDt37QrGRaamcOL558tYhgRnfPWuu48/ODQ0fLJcsvbqMYBSimSz2b8qFovvaUftnz59Gp/8" +
                "xMdhlSzohgGdchSVhccXXsKllRuA5AAI7rqT4tvupzB1wHU3Eo6QFuq/CQNsEJAAExAuvaS1GWjCABvfS2q0gY2oGMK24ruREtsh" +
                "qO2nwKXCvgMHcPDgwVAhOvHC86CEQkiBZDJ59Q1veOObYvH4zM3Q8qYZIJfL/mY+l//1ito3TROdXV2BqujFEy/gk488AikFNE2H" +
                "yTTccFfwhZkTWM7nAMlhRIC3PcRw9CBpiOPbYYAG54+ESH+dJgi36zVmoKUfUHNOWwxQNp/EAZMRbLfeiX7vTkjqQikFT3jYs+c2" +
                "HD5yJPDeX7p4ESdffgmcc7iuh+7u7ifvf+CBh3RdtzarCcim30AISqXSd69lMo+iHOdrnKO7p6ec666L7597Fp965BG/UJNzGEzD" +
                "ldIsvjDzIkq2C3gMnT3Au97BMDZM4DjVbOxNMoCq0QRB19iY872VDNBwjRYM4FcwCygiMeZ8O8act0BRARAFz/Wwc9cuHDl6NJAO" +
                "L7/0Ii5euAiucXiuhx07d/7J0aNHf6paFd3mQTcr+Y7j7Mlms3+qysSnhKCzqyuQ+C+9+CI+9clPllO0Ggym4XThKj59/TmUbA9w" +
                "GUbGCN73fRwjQwSWjVuQDifthPptAgObNIs39W0ZiGK4pn0RE8ZnQQkHJX6R69WJCZw+dSrwfYeOHMXwyAhcxwVjDFeuXP7J8fHx" +
                "H2Vlh7Amyd100Qp8284CoOdzuT+XQnRVsigdnZ2BgMSF8+fxyUc+AQKUic/xUu4KHps6CeERwKXYvovg+97NkUr4kv+q3HwV/B51" +
                "i655cyxLwaBjhn0DV7S/AwEBoxyarmF8fBwXzp9vlFxCcOdddyGZTPq5A0Jx5vTpP1jLru3TNA2UsfaWKu+OabXKWatft2z7vgr0" +
                "m0gmYZpmw5ebmZ7GX3z8YxCeB03zJf/l3Dj+efo0lCSAS7DrNoLveSf3nT2vvXuqNkkI1QTWLVeYt2AE0sQJ3hxvtD6fgMPADH0K" +
                "l/g/AKC+JtA0XL58CdeuTTa8IxKJ4Pg9d4NRv17Btq3E8889/2elUkkXQsB13ZaLtkt827bvyefz/7ni8ZumiUQiEZjQ+dif/xkK" +
                "+Tw0XYfBNJwtTOLxmVOAooBDsGM3wXd9JwfngPcK6yGDbmwrZ6wZDnArJPrmr0PAEcEsfRJX6GfACAclvu907uxZLC4uNryjt7cP" +
                "B2+/HZ7rgjGO+fm5ey5eOP+r3N8r2doE0PJOmWYLgJHL5T6qpORKKVBKA2N9ISQ+9ZefxPzcHAzThEE1XLXm8E/TJyElARyC0W0E" +
                "73qYQ+M+nHsr7XvDeaqRIBXBV7WMEqgJmiOBt5pXai/EYeI6+RquksfBqQlKGKCAM6dOoVAoNLx17759GBoegeM6YIzh4oWLv5ZO" +
                "pw8ahgGuaU0XlVKi2VJKoVAo/IJj28cqqj+ZSgXa/X967As4ffIkIpEINMKxJFbxjzMvwvUU4FH0DPiSr+ubkHwVLKVtee91RAcQ" +
                "wAGtwKAWWkeRNpmCBLw37DwCBh1X8Bhm8Sw0YoIxBsu2cOb0adSXkZOyP2DoBpRSsG078vJLL33Utm3qeR6arZYaQAixNZ/L/VqF" +
                "+GYkEpjTv3jhAr78+D9X4V2HOPjC7IsolCxAUUTiwHd9J0MyHiz5QUS8Ga0apv6DaN/ABypc0yj1rVD5IW9TfoBGQHBe/h1WMQFO" +
                "TWicI51ewcT4eMM1kskkbj90CJ7rgXGO6enpN12/fv29hmFUU8dBi7aS/nw+/5tCiKTy7QU6UqmGD8/ncvi7v/lrKACMMnBK8eWl" +
                "U5jPrgKUgxDg7W9lGOgncNzmEtLyfgakX8MJRDZoglAnQNVqCtJU+jdDRLUZ5gmM0TlclHBK/iUc5ECJDo1ruH5tEisryw3n37Z3" +
                "L/oHBuC5LiglOHv69G/mcrmU6/obXINWqIoQQqBUKt1dLBTei0qSJx4H1xp353zhC/+I+fl56LoBk2k4lbuKs0tTANUAB7j3boa9" +
                "uylsGzctWW2dVq9FasyBqpaBN4r+OuFJaNTQqhagHfV/M1EOg441NY0L4tNghPnNMkBw6eIleHWqlFKKI0ePghC/iUZ6Nb31/Lmz" +
                "HyoUi8hms4GrKRBULBZ/XSpF/eIDFuj1X7p4Ac889RTMSAQa5VjyMvja/DkfY3KBsZ1+Vs91QjzumzIDpCUDBTl5qDLCxoU6LdFe" +
                "OViw2flWHBpMXBdPYUo+DU4j4Jwhn8/h2mRjaDg4OIht27f7JfmUYmJi4mcL+Vy/v8+hMQygYbZBCHG/bdlvJ/CLOROJRAPa57ku" +
                "Pv/Zz/k7dikDoQpfXTyDkuUAisKMEbz12zgo9QszX5GkqOZxfRhDNDh/LU1Ae9dt6vy15TwGMQ9p4KxKQoqA4bz7WRTVEhg0cK5h" +
                "ZmYa+VzjrvPbDx2GpmsACHK5XPf83PyHOjs70ZHqQGfdokHggOd5yOfyv6qUJFJJcE1DLNa4Deqpp57CtWuT0A3Dh3lzk5hYnQMY" +
                "BwTwhnsZ+rtJINAD9QpjeUWaEqteWqtLNZaDBxG+mWkKqgJq59x21b8K8QcKchnnnc/5cDGlEEJiMkALdHZ2YseOnXBdB5wxXLl8" +
                "+YML8/P9mdVVpNPpDYtqmob6pZQ6YtvW20D8CpREPN6Q5cvncvjqV74MTdfBCUNeFvHEwkUADHCA0a0ER2+nsB2E2tZXgrAFSWGD" +
                "9KvNXS/o/EZmIjcHOLUV/rUwBcTEdfcZzIsz0Ii/RX5lZRkrKysN5x48eBC6blQyt12LS0s/3NXdXd2rWFkNuQBKKYrF4s8IKZiU" +
                "fuFhNCDse+KJb2JleRka16AxhudWLyNbLACgYDpw/72sZl9da89ZBYRg7dxg1YIJECD1Ya+1YsbN2vpW0q+aib4KDk8VBM7Zn4cg" +
                "Dijx0b6Z6amGDGCqowNbtm6F67oghGL8ypWfAEEsEonANM3qopWeOEIISCnhuu5gqVj8HgJ/C1csHm+w/dlsFk8/+RQMw4RGOZa9" +
                "Nby0PAlQDrjA/v0UY8PUL+io/hjSFNhpR8U2YwLVJlE383rDd6op/Gid+9+M9LdvHhgMLLqXcN15HhrzAaK1tTWkA7TAvv37wZi/" +
                "bS69srJtYnz84VKphHw+X11USoHaVSwW/qPniaSCKu+CbZT+5559BquraXDOwBnF8+krcGwHUAR6DLjrKKuCPW0BN23H9U1UbR0T" +
                "bNYMhGqIEElurfrbSEsH1BO0wycUFBdLj8NVhbIWoJibmytvZ1s/+vv7MTg4CM/zoKBw5dLlHykWCigViygWCigWCqCUMlQWoYxb" +
                "JfuHVHkrdMSMNEC+lmXh+Wefha7r4NQv4T6zMl2V/oP7GXq6CYKaXqhNoWdtOFpBTNCCEcJWaLhZp42aIY2bCR1bMTeaXJtCx4o7" +
                "iev2C9CorwWya2vIrWUbrrlrz25I5W9DX1xceMC27b3RcpudDSZASgnHso84jn2kXFiDWLzR8z97+gwWFhbANd/2n8pMwnV86ddi" +
                "wOED1N+sERjq3DzQogIg3UCsfwMjkE2H5xWJ91V+gMSGSP5G1U+Cid8sZ7EJDanKNQSXC1+DBxuUUCj4u6jrj61btiKRSEBKBcuy" +
                "9OvXrr3bdhyULAslywKt/fqWXXqXlJIopaBpWmCu/4UXngelDJwwZEUBp9Nl6feAPbsoerqCpb9Zlq25KQjXBPUxfiCmXscMwYvU" +
                "EJ2EA0po5Ys0Uf2qTXPWIlKq1DEwaFhwxnHDPgdODTBGsba2BsfZCLcapomxsS3wPA+EEMzemH2X57pUSQklJei6GpSabdv/oZL0" +
                "iUSjDaHfjRuzmLx6FbqhQ2McV/KzKFhFH6jQgIP72AbAR20mz95UjbbBBGHaoI4Zgld7SKJCKweWtGcGwoCfFljJRlNFIJWHicJT" +
                "fvRGKDzPRTqdbrjM9u3b/VY4hCK9kj6UzWZvr/RfppwxcMYgpdrvOu7eymdEA/bunzl9BrbtNzISROD06rT/IzxgaJhgqJ/AFe1w" +
                "e2tT0FZkEEaYJiBPU9VfAxaFpotD1L4KS1K1Arc2oQnqgSxGDExbp7HmzYFRDYQQrKZXG+HhoSGkkh2QUsK2S2w1s/q2eDyOWCwG" +
                "6pXtv21ZbxZC+Li/pjXs5JVS4sL5c34hAeVYtDOYzacBwgEF7N1FwTiCy6Pb/dFozhCqiUMWiPaqcIYIJXgIgtg8V0DakPYAbdHM" +
                "2QuKZOquTUFRcDOYLp3yzQBlKBaL1R6FVQBJ0zA0NAjhuSCE4MbM7EO5fB75Qt7PBTDOYdn2Qyjj/oZhNMT+c3M3MDc3B41zcMow" +
                "nr8B6frenh4HtozSDXl+1dKzIW2Fd82YAIEJn1ZmAKFFIU3fH5oraI/4YY7fZirRqhtWapiagOJa4UVIuCCUQkqBtbW1hveOjI1V" +
                "W+0vryzfwRgbSKU6/HSw6zidruse8X+ECuzUNX5l3M8fMwYPLi6vzQOEAgIYHqRIJcOdv3At0Lx0qxkThGoD1Zze7fBEWKKokRGb" +
                "ZCVbEb+J5guy/TIkYcWIhgVrHBl3DpxqIIQiF5AgGhwcQiQSgVKAVSqlFubmjuZzOb8o1HXdfZ7n9VWaL5sBjRzGr1wuN2ViSLs5" +
                "LBbXfAYAsGXUb7bYgJtvJgxSwXa2FToXmPRpQvWmDKFa+xANzBeUeFKtk1rN8OGG+1Dr79R+v7IGKHo5zFmXwYgGSilKpSLcKgzr" +
                "H4lEAqmOTkgp4HkeFpcW7/GdQL8NyXEpBCpNB+qLPorF4gb1P1NYges6AAiY4WsAIUKcONUEQWuFpgVKXqMtrWeEBvut2ltNNYVq" +
                "nXdopvbr7X67ae4GZzPATCmlMJ0/CxBVblTtoVQsbrxLhKC/rw9C+O3uM6uZu6OxGGipVIJl20cBQCoJI6Dz9sL8PHK5XHksCzBV" +
                "WPLDJwl0dBB0lNW/CpGIIPVfDxCFAjsNXbzC0Lt1RlCqPdPf0jyoWsKT5tqoqdqv2z7WpgOsVKPEb9AI5ceUcCxaV2HJQrWCuFjH" +
                "AADQPzjgn08J1tYye65NTsYp55wIz7utMoAhqG/NzMy0X3dOGBzlYq6Yqdr/vh4CzsubOUPgVdXWjWqs32tHGwQxAmqAnZtJB6+r" +
                "+fYIH+Shb0ry28h9qDonsFbYKBjW7CVknUUwqoEx3wzUH91d3eDc70RSLBQHOecjVArR4wlvazVkCGCAubk5UOr32s16JaStgr/L" +
                "kgD9vb4fIOu/cJtOUWhyJSz+VptN+NQyQ/NVT/BW1w8NQ9uQ/FYgWKj0B3wGAYUtiliypsAoByEEjuOifuNvIpksO4IKruvyfD5/" +
                "G6WMjUghuyvzdOo1gFIKK0vLvgNIGdJODq4ob9znQGfHxqaLUOvdutoPizaGh001ARpNQrtp3k3lBFRzwqsm+wrW/yFNS9SbObsN" +
                "yaoWj6VSWLGmQAkFIX7/4XpHMBqNlvMCEq7nAlC7eC6bG5FSQkKCU97QrMgqlbC2lgFjHIQSrNhZv0sTYdBNIBEvh381P6S+Axup" +
                "oSdRG3JC1b+r763cIELWPQO1DtGToBsX1PrlW1CkqZoBNio8vG3XyQ3KUG54jMbHqqZucMWahYTwK4elgus6MOoiungiASEEGGO4" +
                "fu36Vqrp2piQvgdHKW1ggFw+D8u2UelDs2Jnq98gFiEwdLJB4hWap1tbecr1kHEQChcI0qhvTWGuCpH4cEg6nPjtZsIDvf6AUHBj" +
                "iphizVmCK63KiDu4jttw7UQi4Xco8ZlgK3UcewSq3Lk7YLtXPpeDY5eHN0Iia1tVkY1F1x3A4N45rSODllm+WpPQLiOo9nMAzQiu" +
                "WmAJjd+ZBH8/1Q7MXQf4BKj7ZuaBgKHgrsGRJb+RNUGDCQD8HUQVBLNYLPRz27J7Kl8uqFddLpeDUv4gJBcCWa/sAEogGgUoAYSq" +
                "YNO+KpaVx6Txh9aaA6gA81BzH0ntzSNko4KtPh+Uiwt3Km9a9Ydi9qT5Z7ah9iv/yhYED9IKtQ0vLK+EopdD0uiGVKJh4wgARCJR" +
                "AH4oWCgUU5xS2q38bE9gl49KOEEIhZAeLNer3mrDIFVpqbX7G5iglkhqIxNUfsQGJqh5UHu99Rf9UVIkyPaGMcMrJXqo7SdNCd+S" +
                "GdCc+KEEV42PoQhcacMWxWqxqAxwhEzTrN7vUqmY4iAkVRnNFqQBisWiP4yx3AbO8WT1d5vG+hchFW4mFZUU4BSS9pgAQc7ihhdJ" +
                "zbWDAQdFQnNwbdlhNI3LSXBo24zQTXAMFQA/BxFchoWFZX/Jky48afs+ACGQorH/UzXRpwAhBOdSCANVddx41xzH9aNjQuEoF65a" +
                "1wCcbbS5QSofIZFBIBPU39ua5zZEFRsITYIVstoUXVtoAtJW2jYUy2iCXciA/EMowev8Kj97W/ta+dtSAioJlGpkAMZ9nEBKCQKi" +
                "uFJKU0pBQgYygBQCKO8Z8Dtir1+U0nWnp0Hlq0aGqH0tkAnqJD/INwACTAMaNUOAhd6k6ifNgax2/I0W6GWD2m9C/GYmYUNfQ0LL" +
                "AhvcGZPSmuQbUeBSKa1SThzIAFKAlEerycp0rxrihfkAiqyr9s0yAYK0QStGaGCGBgPxCp2A8BqDljuBWhEfLYgfZBKahIakMgAr" +
                "YO8vpaw8QNu/CK/MUq/M4m3U3f5oNYrKxK11aoma0Ssk4P7LMtFq7TutMQeVvyuMsmlGCDAPQWbiFtG/rQijnc0o9SBPq0ig4bUa" +
                "1V95LGt9KQpQSYIHk9b0fiKEgIMQZ318eqPN4Jz7nET85g+0Rq8IodZ/SICRr5f2anSg1tE7CT+UVAHOYVC42CDoKtjDu+WgkLp5" +
                "TRCI6rVD/PprBBAf9dEASHmgFUEQBwgpqjkCpRQ4AbxqR7CAwTqc8WqOhBNeRZkAv5dvrUqrmpcglV8j0ZKsE66qKZo4iLXXJE0I" +
                "3SD15FtI8DZNQL19D9MGzUzCBnCrjviy7pz1LqkkxKeT6xoAACeUllT5ikEzbM2I6YcNBDC43+TZUrYPPNhqwwdXCEkDCFObUieq" +
                "LmZX69XZpMZvIKSJRmhH6tXm+GAzjSFb1e43lXqEO3+Bf4e8X9ad62/uZag0hwsK6x3XhfA8EEqgQAj3XHe1og6ECEaOKjN2OaXQ" +
                "OaveKsuq+YJlqVZ1al+VVTypcWJowOu12IFqRyNgo1ZpRml1i0xAyw0bCPDWN2ECwv6uxvoyHAhSSoERHQaPVu9XEAPYlgXP8/yd" +
                "XRq3OCFkpXLbvYDebdFYtDryRaccUc2oUqhYwvo2MLnRrhM0t/ukhuj1JiAoUghjhKCEC3mlJmATMHIzwodJ/ab8AawTPpD45dek" +
                "UjCoiShPQJXVaDCyW/KjOX/v5xqNRCML/hjU4ORBIpEE5xwKCjrT0aknfJJRoFhS67uAmyQwKjjBhtdrawjKj2WT56qvyTY6eCA8" +
                "K9nWaoP49d9F1djjajgnG5+rPy/onEBvv95HqCG+KifzTB5H3EhBQYIA0AKSe35uR0F4ArFEfJUTkKlK7Oi6bjU8qByxWAyGYfh9" +
                "ZQlBRyRSyT+iZCmULIVEnPjwUF1sv8EZVMHwbEXdk5r31D4Xdm49hBymHb4VdQBBoEx9u3ipGp/blD8Q5O2rxjqAymMhBRJmFwwt" +
                "6gNglIBpjQyQyWSqIBBj7AYVUk5XgAHXcxsySPF4HJFotJoQGoh2V9Wr5wDZ/LrxVqjj8Nq/6+DPWiAjVIJCqmLkJqT5pghefx3Z" +
                "+B1lQHGmbILiyRAChjp/snkdoKq7h0IJdEcGYfJIlVYaC9AAa2tVCdI07Rrt7OiYASAAwPMay4g0TUNXZxeUVJBQ6It3wC8C8NV0" +
                "Zk1VOV7VMYEM8FgbTIRsvOkVdY8mN732xsoAlVt/rXZW0ExhWX8tGfAd6j63GVOrWpUvm5vOQLVfyyw1GkJKhcHE1mrExhhrKO+X" +
                "UmJ1LQNCCIQnsGVsyyR1XHeWcz6nlIIUElbdvjIA6O7p8QdDKoUeswNR3UClDHR5RW20hfUElcF2v/JYIsC+o46QcuPNk/WfV3du" +
                "PcNUCNtq1TNbLcEDmaWewDLc9ksZrMlqBUeGCE6QxkPNPGR/cAfHUHJ71VByzvw28hvsfxbZtSwICHRDh4K6yJVSOUrpVaXUiFKy" +
                "YWMhAAwM9JcTLQodZhz9sU5Mlm4AhCKdUbAdgLGadLBczxPUe/C1Nh4IDv0q/wYCRHVFOiQobFQIbRi+GTwgqIoprHF1PSoX9nwr" +
                "f2BDlq/2O8mASqWqA6gQ01MYSm2DUL4DqGt6w81bTa+iVCyCUALOeYkyPk45Y4hEzHMVGLgQtKFgYBCmafiRANWwJdXrfyMK5PMK" +
                "a1lVhXNruRm1nN2GR1wvFUHnySDJl42SItXNRwH1WkbWaTI0MTGt/JcNJg4hKn+Dd18+HyF1AArwhIu+2Ai6ov2Q8AdVB81pnp+f" +
                "g+f55eLJZHKqs7Njjmq6BtOMvFDBkUvFQgMi2NnZiVQq5TeQBsGurpEquqNcYGFJbtgbWH/jgkIaFRJK1atS1cSGtjQFcqOqls0I" +
                "3kzty2C/IshMNZizILOAADMS4uyhGTBUYQDpYXv3fkS0GFCO4oI2+ExPT5dzOAKdnV1nurq6HWrbDhhjJyilAlBwbCdwf/ng0BCk" +
                "lBBKYmtqAFEz5v8CAswtqOrO4IYbhkaulXX2PlDCVfjzzZzAQKmWjZJbz3RQwfZehnyWaubpqxb4QNDzsjFaUnX4SfBSoOC4re9Y" +
                "lVt4eRzvhiSQ52F+bg60bKtTHamnc9ksqK7riCUSV3Rdv66UgpACuVxjt6mtW7f5A4wh0RXpwLbOfkAKgAHLKxJrWbUB7kXdj5BN" +
                "bkxD+NPCo5ZBxAshUr2HL1XACvH+pQoAYmS4JkAzwqPRUQVCwJ0aE4H6vxukX6Az2oftXfvgSbda9lWfCFpaWkI6nfZTwJxj+/Yd" +
                "zyWSSX97OAEs0zSf8WcEAGuZgAYDo6OIxqJ+CpEyHO7fUcVyhQPcWJAb/IBaW62a2TBZx/0IRgAh2zADdTcwTBs0rGbnhah+KcPV" +
                "vwxS30HqPsx3qTMR9fekdjmejT29R9Ad6/cRQEJgBDT3mpy8ilKp5Df+TiZn4vHYaeF5laFREolE4nGUU4j5fA5OHR6QTCYxNDwC" +
                "IQQ8KbGvdxvMSNT/FQSYmpFwvPUfFohZ10lWrZNYsdn1pqHeRoc5VbKFut6UA9jiObRpooK0S5C6r70/zRJDwU4jxdGR+6sSzxiD" +
                "EWD/J66MgxAC1/MwNDT8lKbpeU8If2QMIRSRaPQJzlkJULBtB9m1TMNFdu7a6dsTJdEf68L+vjFAeAAD0mmF5RUJysIRq1rGkAhw" +
                "EgO87gYHUIaDLwiQ4CCVH+oAqtaOaVOiB0hqMxQ0NOPXJEqoXa5wMZAcw76BOzaof1q/uyubxdTUVDU7ODI6+nilMbjfLt5xQCm5" +
                "FolEnhPC1wgry429Z7dv217eWiRBQHHP6IH1KhAJXJ2SgSZABgE8TTRCg3NX37EjxBQEOoF16h5hYVcTk9BuSBgEQjWDr4OiiVYq" +
                "v1YbOJ6NO0YfQGe0F1L5xbtmQHufy1cuYy27BiggHo/nu3t6vpLN5VAoFKrkAyEUiUTqUcDfIJLJrPkdQGuOWDyOrdu2QQgPrvSw" +
                "r2crhjr7qlpg9oZEZk2t1wA22ysYAHXKAI9d1t7YAH8BTSS6GdxbjwA2DQtbwcdheIAM+R6yzeeaMKIQEjE9iXu3vQ1CugABNI03" +
                "bAYFgHNnz4HA7xwyODT0RDKZnK5UCFNCaTXfn0jGH+Ocl5QCbKuE5ZXGpoN79/kdqBUUYnoUD2w95EcDBBA2MHHNdwalDLBdMjyW" +
                "rYeBpQqWWFXnL8iQ5AheARCEkGvIAM0lQ36PDArhZEBoLANQwDYKRSzXwuHh+7C9e29V/ZtmpMH7X1lZweTERLnns8L27Tv+gTMG" +
                "xpjfSZxzBs4ZKKOIRmOTiUTyq/5eAGBxcb6BAYaHhzE0PAzhCbjCw/HRA+hMdQFCABy4Pi2wmlWgNIBzEWDL0Yjb1yOKYcxT6zxu" +
                "0BDy1iCBDV6/bIJJ1Kp/hIBDCFD9IVFPkAlYP0eBMx1v2f3d1fw4oyywu9vpkyeRL+ShlEQymVrZuXPnP3KNI2KaiJgmqOu4cB0X" +
                "nuP7Ah0dHR9DeepUdi2DbDZbh+cTHDx4OxQUBBS6Iym8ZedRoKyGPBu4MiECkcFmDo8KSf7U3qh6zYCwDGQdY2xqyWCHTwUUtGxg" +
                "NLQRijYJOwG0ldpWCii5JRwZvg/7h+6EK/zewIZpNpSAeZ6HU6dOgjMG13Gxbfv2z1JGl3K5HAqFPAqFPHgl119J1uiG/viNG7PX" +
                "isXCViEkbszOIplMbnQGd+xA/8AAlpeW4VAPD2w9in+ZeBkr2TWAMUxNS2wZVejuJA3DokIzOyRkH5kKzghtqBKuyQxtqCQjr6Ag" +
                "sI1GVyqgECRoaET9a0FlZEGqP+g5KSV0ZuDhg+8DoxSeAiihiNbQsXKcP3cON2Zv+KigRnDHnXd+vLu7G6Km9I+yqj3wZwboulHs" +
                "6+//MyEkKGVYWlpqgIYZYzh8+IhfWgSJzkgK79x3H6A8gADSA85f8sJVmGqhGmvNAxpz8fVe+YbzgkCodmoC6iKBehtfj2ZKudHk" +
                "1EO3QaarGawbpPqDnis6Jdy3/dtxYPAY7LL0mwFzHZRSePbpp8uVwA62bdv21I6dO59mjEM3jOqilmVhwyqV0NHR+THDMDJKKTiO" +
                "g9mZ6Qbu2rFzJwaHhiA8AdtzcP/WI9gxsBVwHYADS4sK16ZFNU286RVEOAQXZ6gmUG7bgBBanC9DvheaMFKQaVCtTUCYOXCFh45I" +
                "N7778Acgy9nbMOkfv3IFE1cnwDXf+bv90KE/zGbX1Go6jdpFPddF7XIcB4zSue7unk9KIUApwezsjQYtQCnFnXfdCUoJZLlg9AcO" +
                "v9mvQyujgxcuCeTy2Jgq3kxhZgChm2XFWsbyTVYopCwbHdNQosrmoW+7IV4Yg9iuhXfe/n5s6doJT/m7tiORaIPtV0rh61/7F0gp" +
                "4bkuhodHLgwODX8+u5ZFySptWDQSjaJ+6YaBwaGh3+cazysFOLaNqevXGvMDI6PYsXMnPNeDIzwc7N+Nt+49Drg2QAG7BJy54LWM" +
                "eWU74EuQNy7bCL2aaIWmEl9OP0g0KRurizaCPlvK9v5tqgkVULSL2D94Jx4++B7Ywqpu9Y5EGz3/ixcv4srly9A4h5ASx+648/f6" +
                "+vrs7p4e9PT0blg8l80G+kCM82s9vX1/dmNm5kOMMczOzmJoeKRhfOyddx3H3NwcHMeBKzx878E348zcVcwszwNcw/wNifEugd3b" +
                "WPAAyZs5yEbni9Q9r3ALOoSo9raJKRW+RwAtHD606RB6wkNUT+AD9/4nGFoEjmeBgPj9/utq/4UQ+MqXvuT3AvQ8DA8Pnx0cHPyr" +
                "6ampwM2/gYMjNU0DoxRDAwP/UzeMtPQbC2Ji/ErAvoEEjh47BikEhBJI6HF88O53QtO4z94MuHhJYHFZgdH2kLWbrePfjMpvuQKu" +
                "L2/Fd20Th1j/DAXLtfD9d/4U9g0ehuNZVcw/CPV7/rlncW3yKrg/ABTHj9/9W8lk0olGo4jFYg2L/dZHPgJN1xuXPzAy5+eSFx9i" +
                "jCGXzSGZTDYMk+rp6UE6nUZmNQNQYCTVB6YxnJ66CDAOJYCVjMJgP4WmkdBwB21IV5gUhjZ0DOpaQoJ3ASkS3G5ONdkbEBa+taMR" +
                "ml2j8nkFu4AH9nwnPnDvh+AKpxK6IZlMNqB+2WwWf/NXfwXP8+A4Dvbu3fu173j44V81TBPRaBTRWKxhhU8PJwRCCAwODf5RIp64" +
                "WOk3c/HixYbScUII7r7nXsTiMUgpYAkH79r/AO7fewfglAAGFHMKL5/2AncSbVoLyNZ+gwpJOsn6fECTLGBYRHKrJD/oPtQmkUpO" +
                "CTv79uMn3vify4zo27t4wDBPAPjnLz5WLfrQNU3c/6YH/jMh/p7p+gmx1UmxlelRYUvjWnHnrl2/pODPEsjlsrhy5XLDh8fjcdxz" +
                "zz1+cyKpIKTCB+9+F/aM7vSdQg4sLymcvuBt2NlzKz3lb4k6fhU+UwZsNLFdG52xHnzozb+JjkiXj/eXh3kE1fudP38eL7zwAgxd" +
                "h2WVcPc99/7xjp07n/c8D1LK0FVNBoUtqRR6+/oeGxwc+qTnumCM4drkNSwuLjZ8idGxLTh06DA8z4WnBGJaFL/0wPsw0N1fxQdm" +
                "pyXOXRbBuYIQ7zkMHw8s5ZK3jnBS3vw5TbH9gALS2sSTI1xwZuJDb/4t7OrbV/b6/UrfoJg/n8vhc5/5NFD21Qb6BycfePDBX1dK" +
                "gVLafAnPQztr+/btvxKJROZkGYw/e+Y0bNtu+DIHb78d27bvKIeGLgYSPfi1h96PrlTK30vGgclJgQtXfCYAbr20vRImaJomlu0l" +
                "a26m6qhi913hAYri5x/8rzi+9X6U3GK10DMeMMUdAD772c9gcWEBjDEoAG9561s/FIvFMqVSCY7jNF00zDbULqUUYrHY/J7bbvvp" +
                "yo7MQr6AM6dPNYQWvj9wDwYGBuB5HizPwa7uMfzfb/sxpGIJnwkYMDHhM0Hl97wS4jd772a3hn2rTEGz7W2VCMb1PEip8H9923/B" +
                "m/e+A0W3UAXdwuz+U08+gZdOnIBhmrBsC/fcc++fHDp8+PNuWVu3XP/tN36jtZoof3AylbpYKpV60yvpu7jGsbq6Ckooenp6GnIF" +
                "A4ODWFiYh1UqQRCFoVQfDozswInp8ygVCwBnWE0ruALo6aIbdg21avKgNtFCPqwlS9Brm9kdHDYnqL67d/1uocBRs+XyLkoYfu7N" +
                "/xVvP/DdKLklv9sKpUgkEg1l3gAwMTGBv/6rT4FQCs91MTg4dO773/u+7zNN06k0/m61iG1Z7eMvhMDzvOiJEy98PZ1O38kog5AS" +
                "d951F4aGhgJt0ze/+Q3k83lomoaYHsH4yhR++5/+HDeW5wHdBDxgcIjiwG0cjPmSVtv+vV7jtfr7Vm4Tb9bxqyUYFLalPIApLNdG" +
                "RI/iQ2/+MB7c8x0ousVqZ6xEIhHo9K2uruKP/+ijvtdPKTjjhR/+kR9507bt218M6vMQSlNvEydXYtBsNrvruWeeebpkWT2+xFPc" +
                "e98b0NXV1XB+NpvFU08+iUI+D65piGomFgrL+O9f+jjOX7sE6FFAAJ3dBAf3csQiBJ6oISJp7PpB2iD2rWCCdhig1Z7BMKJXXis6" +
                "BfQlhvHLb/sIjo7dXVX7pAnxLcvCn/7Jn+Dq5FXomgbXdfG93/eeDxy/++6/8DwvuN1fGAOIZpOew5iAUkxdv/7wyZMnPwelqJAC" +
                "kUgEb3jj/YgHTBzP5/N45umnkF3LQtM1mNxA0Svhj77xN/jq6WcArgOKIhIl2L+Xrw+gDukMtoEJarqSBjLBZnBhtbHWoFkbmrCp" +
                "oO2o/8qO3oJdwMHhY/hPD/03bO3e4Tt8Zb8rjPhCCHzyE3+BkydPwjQMWJaFe+677/999/d87y8wxrBZerIPf/jDNyEZCh2dnZc5" +
                "56vzc3Nvp5TCtmwsLS5iaGiowV7puo6hoWFkMqvI5/JQlMDkOt60+xjMiInTM5cgPReeYJhf8AGnjiRd70Ta1CFo7Re06wMohE8c" +
                "C5wbEDJAoqn6B+B4LlzPwcO3fy9+6W2/hd5EPyyvtAHlCyK+Ugp//7d/gxMnXoBpmCiVLBw+euQz7/n+H/hxSomSTatvbiED1FQJ" +
                "P+86Tmx5Zfk+zjiKxSKWlpcwGMAEmqZheGQExWIRaxkfMgYIjo3tw8HRXbi8dB2rmSUowpBO+51HEnEC0yAbu2VUKoJCckSb7QBy" +
                "Uy1iENCvV7VW/6os9Z2RHvzsg7+GHzj+QTBKyzOYAK5poQ4fAHzm0X/AU08+CdM0YVkWtmzd8uw73/ld706lUtbNEN9v3fMKh+so" +
                "JXHy5Mn/c+Xy5R/nGofneOjs6sR9b3hjIGihlMLlS5dw5cplEELAKEPUMJG1C3jkmc/j0yf+BcJ1AWpAM4DtWxlGBhkY9VvTBlGb" +
                "NHvulfYGqntChWghFWAqahnH8RwIKfCGXQ/iA/f+HLb07IDlFavnGaaJeEB2r3LPHv37v8eTTz5RVfujY2Onf+yDP/HtqVRq7pXQ" +
                "8BUzQPkL0hdeeP6RyYmJ93JNh+c6SKU6cN8b39iQPq4cc3NzOHf2LGzLAtc4NKYhoht44fpZ/Ok3/wFnr13yJ5NTDR2dBDu2MnR2" +
                "0GpnrabOHtm8dlBtPqmaDI1qYI7y1m3LszDWtR0/cNeP4cG93+E35JL+5FVCCKKRCKKxWOD38jwPf/+3f4tnn30GEdOEZdkYHBq8" +
                "/OMf/Im3dnV3X3+l9LtVDADPdbWXX37pY1cnJt7HuQbXcxGLxXHvvfeip7c38H2lYhHnz53DwuIiOCuXpusmbOHin848gU898xhu" +
                "LN0AiA5oHAP9FFtGGBIxst6MqQWBbyYaUK06havwYVDVHL4UsNwSumI9+M6D34N3Hn4PeuK9KHml6gUq6F7QrEYAKBQK+OtP/SXO" +
                "njkD0/Rt/vDI8IX3/dD73zE4ODhxS0orbgUDCCEgyhsNT508+SdXLl/+ccoohCfANY7jd9+NsbEtoe+fun4dExPjfjlaeT5xVI9g" +
                "pbiKL5z6Bh498VUsLM0B4KARHYP9FCODDPGYT91a00BuhQ1oMgkk1CSgAuW6cMqJnAdv+w684/bvwVj3drjChlCiWsdnRiKIRdeb" +
                "cNYfi4uL+NQnH8H169dgGiYs28bWrVtP/uAPvf+d0Xh8igKBXUD+1RigAj1yzvHsM0//3rXJa79MKKm2oT9w8CBuv/32UJEsFosY" +
                "H7+CxYUFQPnlThrjiBgmlnIr+Mr5Z/CZl76Gq7PXAU8CUQMDvRyD/QwdCQLKNrZeuZVHq7BQKgnbs6GUwmBqFN+25214aO/DGO3e" +
                "Vh7j4lY5Utd1xGKxQC+/cpw7exb/8Hd/i7W1NeiaDsu2MTw68vX3//AH3tPb27uQzWZhlqt6X1MMUIEfZ2ZmkM/nfu7cmbO/7wmX" +
                "EULhOg62bNmKu44fRyTAOawcK8vLmJy8irW1NVDKwBgFZxxR3UTWyuPE5Fl88fQTeGb8LIprGYBRxLoMDPVxdHdRRIzycItXyAyt" +
                "pn9IKeAKF0IKxI0k9g4exJt2vxV3brkXPcl+eMKBK9dT35xzRGNRmJFoqHKSUuKrX/kyvvT44/6kNkLhuC7uOn78U7t37/7gtm3b" +
                "i8lUCsVi8bXLAJRSXL9+DcPDwxi/Mv4frly+9H8KhUI/ZQyu4yCRSODOu45jdGysqU+xuLCA6elp5PO58nX90bWmbgBQmFqdx/MT" +
                "p/D1Cydwamoc+bUMoEnEOzUM9OjoSnGYJgGjG/vtbmaA4zrooyCVhBAePOmBEIqk2YFtPTtxbMs9uGPsbox174DBdTjCgVCiCl5x" +
                "xv1i20gk0MOvHEuLi/jMpx/F+fPnYRgGhOeBcY4DBw/+5sMPv+PDk5OTGBkZQWdX1+uDAfr6+pHJZEAp2fPySy99Ip1OH6eEQkjf" +
                "Du7ZcxuOHD0aWNdWKxHLy0uYm5tDbi1bnmzmM4PONRiaDiEF5tcWcWZmHCeunsPp6QlcW55D0cmBax6SSYJknCIZ54gYFIwRsBpC" +
                "1Pba8Ydm+MSWSlYhVY3piBlxDCSHsb1nF/YOHMCegQMYTI3A1Ex40qtu0KwAj5quIRJpTXgAeObpp/H4P30Ra9ksDMOAY9uIJxLL" +
                "b3nrW3+yu7vn0e6uLszMzGBsbOz1xQDplRVEo1GUSqX45NWrvzM9M/0zUvh97BzXQUdHJ47dcQe2btvW8jMymQyWFheRyWTgug4I" +
                "oWVm8E2EwXVQSmG5Npbyq5havoHJpXlcmb+GlfwalnKLsGQelHogzAHTBBiX0DigawwRXYfOdUR4FDEjgc5oJ7rjfRhMjmC4YxT9" +
                "ySF0x3oQNWJ+Ukx6EMrbMMqOUgpD1xGJRsvz+Zp7ojdu3MAXH/sCzp89C8Y5KCFwXQ+jY2PfuOfee3+qt6/vQj6XQ19fH2ZnZ1+f" +
                "DOCHLyXoho7Maubd586e/YN8PjdKKYOQAkpKjG3ZgiNHjzWklYMOx7GxuppBZnUV+XwenueCkI2pa844dO5XNlPqD1AsOhZs14OQ" +
                "EjkrD8dzIZQHyggMzhGPRBEzYtCZAY1rMLgJVh6wJKEgpYCEqEtQ+UTXdB1mebctD0HxNuZGcvjG17+OZ55+CsVCEbphwHUd6Jru" +
                "HT12x/9z6PDh387nC04sFoVlWd9SBuB4FQ6lFDzPQ1dX16OHDh9+dmZ6+renpqfeD6XAGMfk5CRmZmawe/ceHDh4EKlUKvRaum6g" +
                "v78f/f39sG0b+VwO2WwWhUIBrutCCFEdj84oLQ+99J3TuKmBUIKeZKpc7EKrUqqwsU25LWwQsQ4kkMoYlgqD6TpMw4BhmqHQbVAW" +
                "74Xnn8OTTzyBxcVF6LoOrmlwbBtjY2PP3n74yC8NDAw8pZTPcK/G8aowQK2moJTO7rntth8eGBr8zIXz5z+SXkkfYJRCCokzp0/h" +
                "ypXL2L17D/bt34+Ojo6m16vUxnf39EBKCdu2UCyWUCoWyyVPNoTw7bmUEpJIEEkgiH9zCV2fsUfKW+JJeU4NJQSUMVDGoHEOTdeg" +
                "awZ0Q/d767Sw6/Uh7smXX8ZTTz6Bubk5cM5h6Docx0EsHk+/8Y33/49du3f/fqlUchzHgRnQ5evfBAPUaoOent7PHTl67CvplZWf" +
                "uXjxwi8WC4VezjW4jouTL7+ECxfOY9u2bdi3bz8GBgfbSlH7TlcU6O6uOpGe51WXEMLfwFIzNcunN6makUqlDOe8Ztc0vanfurKy" +
                "gpdOnMCLL76IpcUFMO7vzBWuC6pp8tgdd37itttu+0hHZ+dV1/Vb9berTV63DFCLcVNCCtu37/hdwzD+enZm+ufn5xd+1LJKKc41" +
                "eJ6H8+fO4fLFS+gfGMDuPXuwdds2xEIw8zCm0HW9Kehyqw/HcTAxMY6TL72Ey5cuI5vLQisT3vM8QCmMjo19fueuXb+3b9/+pxzH" +
                "hm3bLR3Gf3MMsK4NXDDGpvr6B36xf3Doj9PLyz87NT313mKh0E0pAwgwOzuDqakpxOMxDA0PY/v2HRgeGQksPvnXOGzbxsz0NM6f" +
                "P48rly5hcXEBQkhwjfsVO54HQ9PkttHRL9y2d+8fuK779YgZgeM4uJmCnH8zDFBvFpKp1PjI6OjPd/f2/I98Lvf+qampH86sru5U" +
                "5cRJqVTCpYsXceniRUSjMfT29WF0bBRDQ8Po6up61Wyn67pIp9OYnZnG+Pg4pq5fx+pK2nc8OS830fIghUA8kVjdsXPno8PDI/+b" +
                "EnKiq7sbk5NXYejGa4J5XxMMUAv8KCnBGJvZtWv3b2/fvuMPx8fH37a0uPCD6XT6LZZlxSqNLS2rhMmrE5gYHwfnDLFYDD09Pejp" +
                "7UVXdze6u3sQj8UQiUYDx6e1+30sy0I+l8PKygqWFhcxPz+P+fk5rKbTKFmW3zqXcRBGwcH9qdwax1Df0Au9/X1/eeDAwUd7e3tn" +
                "5+bmkMmsNozk+XcGCNEIrueBUZpPJJKP9vb2PVos5nek0+nvWFleeVdmLXPcKpWi/nBEv4gvl80hs5rBpUuX/EITxmGaJkzTQCwW" +
                "RzQahWEaMM0IDF0HZX6eAeXxKUL443KKxSKskoVCsYBcNotioYhiqQjHtuEJUS3VJmWHUUkfOdSpJrs6O8/09vd/sa+39/N9/f3P" +
                "Z7NZqRv6a0LVv64YoJYRpPQxeE3TJvp6+z7a3d3zUUDtKBaLb5i7ceOhQr5wdy6f2yochykof8RdeTRqsVhAoZDH0uKSDzrVbLst" +
                "z8wuP9f4GPDTtpUCVMoYOCGQUpSHXRKkkqmFRDLxcn9//5d7+/q/ppQ6TRkVkMrvqVzel/daPl7TDFDPDKIcwkUi5kQsFp+Ix+Of" +
                "6OzoNK9du7bTE96dhULh2ML8wgGp1LZiIT8kJeGe50Eq6W+CAIEn/I4ltLw5scoIZUmm3DcXwhO+ZHMNClCmaa5wzq91dXVdTHV0" +
                "vKRx7fmR0dGLgFpxHAeGEUE6vVLecEleL7f19cMAQczgh1WwdMM42xXvPqvp2sejkSgSyWQyFo0OLiwujGYymdFUqmN0/saN/mwu" +
                "293R2dkpPBHxPJd7nqcrpQhlzGWUubqu25nMapZStjI4NLhECJkpFgrTtx86NLW4sDCngEXOdXR0diC9vFzZKFOO38Xr8Vbi/x8A" +
                "3ycr1sCaN10AAAAASUVORK5CYII=";

        END_ICON = "data:image/png;base64," +
                "iVBORw0KGgoAAAANSUhEUgAAAIAAAACACAYAAADDPmHLAAAACXBIWXMAAAsTAAALEwEAmpwYAAAAIGNIUk0AAHolAACAgwAA+f8A" +
                "AIDpAAB1MAAA6mAAADqYAAAXb5JfxUYAAEyuSURBVHja7L13lFzXfSb43fBe5a7OjQ5oZBAAiUAwgCBFUSNLdBhFy3EtWbK9jiPv" +
                "yPbYHs8cH51xHGlm7fXK4yRZskRrZUumkkXJEiUrMoEUCYDI6EYDjQY6p8ov3Hv3jxfqvlANUNLIpI/78B02uquqq94vfb/vFy5R" +
                "SuE78SWlBCEEANBqtWCaJhzHAWMUQggQQkEIIIT3OEoppJRgjMGxbQgpwSgFACgAnHMQAJZtg1IKpRSgJBTgvxaBEAKmacK2LFDG" +
                "QAgxm43GMGNst1Jqt+042xzH2UYJGXZdt1cIURZSZKAApRQUACiF4B4opQACl1FWYYytKWCeEEwX8oVJIdxJgJzjhnHNNM2Kbdsw" +
                "TRNSSgjXBWMMtm17N4MAnHFYto1CPg9uGGg2GrBsG9lsFtVKBYZhoFgsQkrvMwGAbdtglIJxDtuyYGYycBwbxWIJ6+tr6O7uQT6f" +
                "g2074JyDc/5ty43jJfhFCAmUrey67n7Ltu8SQtwrXPc213XHpZR5qZQnXCgEAgcBNhK+ggIk4AhnwLKt8LG1ahWEUAAQnPNrnPPz" +
                "jLNjhULxSUrpc5TSa5R6SvmdMqjv1hd/KQkdIJBSbbNt+5Wu4zxoO/a9whVjoRX5glW+8KWSmsA94RJCwscGrxv8XikFKaT3vfT+" +
                "7Ts1SCkBgFmWNd5qtcYBvHp9bQ2MsTXTzD6Ty2Ue5dx4lHN+nFCiAm/4bwrwHbB0pdQm13H+ve3Yb3Js534hRDGw7CBkKCl9K/fC" +
                "C2cUnBtgjIEb3PueUhBKQSkF8f5A2/VDQUoFIQSEEHAdB7Zjw7EdWJYF27bhuA6kFIHugBDAcdxuy6q8qlpVr6KU/WEmYz6bzeX+" +
                "sVgsfYJS+nzgGf5NAb4FwTuu+4DVsn7SsluvFUIOBIKKWCilMDiHaZrIZLMwTROGYYBxBoLv3I13XRe2baPZbKJer6NRr6HZbPlx" +
                "X4EQCiklbTSad9Zq9TvXVlf/ayaT/ZLs7f1QV7n8CGWs+m8KcFNuHlnbcd5Qr9V+yXac+5Xu3pVn5YwxmNkMstksctksDNP8325l" +
                "AejK5/Po6+sDAFiWhVq1ivX1dVSrVTSbTUgpQSmB6wrTtqvfX61Wvz+Xy17s6up6fyaT/RCj9Pq/KUC6xZu2bb/Ztu1fsR3nNqUU" +
                "iG/tUipQSpDP5pHL55HNZr8jCPjb/cpkMshkMujr74cQAtVKBSsrK1hdXUWz2QQAUErQrDd31ev1PzTNzK+Uu7v+2jQz/4tSeg0v" +
                "gtDAXwSCh+O6P95qtX7DdZxDQRyHkpBKgRsm8vk8CoU8ODdetLGUMYbunh509/TAcRysrq5gYX4B62trEFKAUArbtgbn5+Z/yzDM" +
                "n+nu7v6zfD7/J5SxNSHEv5wM/qV4AD9vvq/Vav0327K+J0jNlIe2YWZMFIsl5HK5F+zehXBRqVSx6lvj+toaKpUKmq0mWo0mLNuG" +
                "VBLUzywIASghMDIZ5HI55HI5lIoldHWX0V3uRrm7G11dXTBN8wXfl8r6Oq7PzmJ5aRG27YBS6gFOoVAo5Cf6Bvp/r7en70MSSlmt" +
                "1r9+HsAXZn+jXn9ns9n8RakU81I2L3XLZDMolUrIZnM3/ZqtVgvXr13DzMxVXJ2+ioWFedSqNTRbTT9980BamAUQEpJJhMIDi7qS" +
                "KY/MoYSCMopsNotCoYje3h4MbRrG6OgoBgeHUCwVb/jeuspldJXLaDQ24+r0VczPz8NxXDBGUavXdlar1b9Z61l78/Dw8G8wzp/D" +
                "d5lH+K56AEoJLMv6wWaj+S7HdXcSPwWTSsE0TZRKnsXfzNf62houXLiACxfO4+r0NNZWV+G6ApQScM5BGQNjvrApBScIGUhCAKIC" +
                "OQefn3j/EQoQAkIYQAlAqC8T5SmTAiijKBSKGBoawviWLdiydSsGBgZu6n3XajVcuXIZ83NzkFKBUQpXuODcaAwODr5rYHDgv3Nu" +
                "2K1m87viAb4rCiClhFLosizrXc1G4xekUiD+cxhjKJZKKBQKN3T1zWYTZ8+cwckTxzE1NYVqpQJCiJf2MQ5GiXcx4rtaAksS2LZA" +
                "1QaatoTruhDgADfAc3kYuTyyuQzyWQ6DElDlgksXpmiBKxdcCTDi4xLCoAgFKIWUflYiFTK5LIYGB7Fj5y7s2LkTXV1dN7xfK8vL" +
                "uDQ5iZXVFVDKAHgcRHd39zc2bx7/JcbY84zzl74CcM5g286RRqP+l47tHAQhIWmTLxTQ1dUFxtiGrz0/P49jTz6F508ex+LiEggB" +
                "DNP0SB4KcOp5l5ZkqFjAfMXFXMXFYsVBVZpQZg5mzwDy/QMo9PQg392NbKkInsmCGQYo5z5DqECkBBEuqLTB3Raybh15u4KCvY4u" +
                "Zx0FWYcpHVACKMqhCIUCIIWAVAqFQgHjW7Zi3623YmxsbMPPpZTC9JUruHRpEpZlgTMOVwhwztfHxzf/ev/A4Htt20KhUHxpKoDr" +
                "uhDC/alqtfYeKUXBo3IlOGMol8vI3sDdT1+5gq9/7as4deoUGrU6jIwJzj1LNxmBIhTrNsX1KnBp0cb0cgvrDQmSL6E8MoKBLeMY" +
                "HB9Dub8P2ULeJ4d81tAnkiIXAOlBEUgAQgESBFIRDzQKG1mngbK9in57Ef3uMrpkHRwSknAoSiGlB0I5NzAyOoLbbtuPbdu3b+jd" +
                "arUazp87i4WFBc8bKAkhFTZv3vxnff19v1Yud7dWV1deOgpgtVowMxlWrVb+Z7PRfIdO1+ZyOXSVyxta/bWZGfzzP38JJ48fh2VZ" +
                "yGSy4NyzdsYoqg7F5TXg/IKLqYUmGg0XRqkLwzu3Y2zvbvSPjyNfLnl/Q0ooKaDztyRmhcH7gwKkj0mUCvAJwn8LBbgKECAQUoEL" +
                "C73OKkaceYy5iyirhpfpEA6A+NiHYHh4GAcPHcKWrVs39AZTly5h4uJFuMIFpQyu66CrXP7aoYOH3lKr16a7usovfgUghKDVbHa1" +
                "rNbfNJutNxJ4N5D4iLhQKHROmSoVfOnRR/H444+h1Wwim82CUgaTAaAM1+sEp+cVzs9ZWFtrApxhYOsWjB/cj6GdO1Ds7vZcuXBB" +
                "lPLSPO8/DwymKQDa1UH9/1Iq3xsoCNX+XioFIb3vXQU4ikAoAlO0MOIuYod7HZvlKkwiIAiHIgSu6ynClq1bcPjwHejfADCurKzg" +
                "5IkTqFYr4JzDdV10dXVNjI5u/tGxzWPPmqbx4lUAv8Y/srq6+rBt2/cEwueMobu7G2Ym0/H5Tz35BD77yCNYWlxENpsDZxQGp1CE" +
                "Ymqd4rlrApMLFpx6C7yQxci+vRi//Xb0bd4MbnBAuIBSoH5OTylpC97PAEgsHdU/u9KtP/i3lKEHENIPEdJTCCV9ZVCAkgqOAhxQ" +
                "KCkx4K7iVnUdO9UCcnDhUA6AwnUdmKaJvftuxcFDhzryCq1WCydPnsDc9VkwziCEgMGNlcN33vnjQ0NDX2i1Wi8+BfCsRm5dW1v9" +
                "tG07+wOUb5omenp7O7r8xcUFfPLjH8eJ48fBOYdhmDAYQCjD5QrFsasClxZtyGYLZj6Lkf23YfyOO1Ee3gRGACUECBSY730YJaHl" +
                "M0ISCuD/SrN+//+BB1CAUDIMAcL3BNIXuPQVQ0R+7/1c+ApjSwpXKQzICm7HDPaSJZhUwgEHoOA4Lvr7+3HnXXdhtANQVErh1Knn" +
                "cWliEowxSCXBKGscvuOOnxgcGvqkUurFpQCu6+5cW1v7jOM4twTCz+Vy6O7uBvE7feJfzxw7hk98/GGsra0hm8uBUwqTE8w3OR6f" +
                "ljg3Z0O0WuCcYXDvHmw5chQ9oyMgSgFS+EL3BU2JZ/2Utr0AabN8lIQkVPj/djMIQsuXvnULXxmkalu8UG1l0H8mZKA4KlQUIRVs" +
                "ReBIYAzruI9exU62BkUoBKEQrgBjFPtuvQ0HDx3qaCATFy/i9OlTIPCBKKX27bcffsvm8fGPvmiIICHE+Orq6hd04efzeXT39KQ+" +
                "3rZtfPLjD+NrX/0qGOcwDAMmAxzFcew6wdPTDpoNC1S56Bodw9b77sfArp2gAIhwQSgJrZtShNbPKQHxPQBLET4JvIHuAcJKI9oW" +
                "7gtS+O7BDaw8tHbdM3jeIFQK/3fCzzKEUrAkhVISt9JFvNK8hn5qwQaDAoHrOtg0PIKjR4+i1IE/uHL5Mp577lnvPUsFQol995F7" +
                "fmRkZORT/+IKIIQYWltb+7xtWQcJ8dK8QqGAcnd36uOXlpbw4Q99EOfPn0c2lwOjBBlOMV3j+NKkwPUVG1TYMDIZjN51D8buugtm" +
                "Ngvlerl34OKD/wdWzzTrDzwCIST8eRAGqA8EAxXQ0b7u0kOLDpRBs+y4tbtSxcKBghAq9B6BcjQlQxktvCozgzsyK543AIXruigU" +
                "ijhy5AhGRkdT79vVq9P45tPP+CVxgHHWOHLP0TcMDAw8+u00nXxbCiClzK2urv6TbVkvJ4RASIniBsKfmprCB/76fVheWkI2l4NB" +
                "AUU4js1SPD5lw7YdMOGgNDqGra98FbpHx6CEC6q8cnAgTEYB5heXQkWgbaUIQ0OoJIFCaNmAhgH01C+0aE1wSim4om3RUkLzAprg" +
                "tTAhPPYzxAXCf11HArYE7jCX8ZrCLMrMha2YX5yiOHzHHdi5a1c6LzI9jWeOHQMIfD6Fr959z5FXjoyMHvdb1r57xSClFKlWq++3" +
                "bfvloeXn8x2Ff/LkSTz0wQ+g1Wwhm8vBZEDN5fjCJHBhrgmmXBgEGLzzbowdfRnMXAbCtnwu34/dG3T4kLRScwD6/CvEBNqjpVYR" +
                "8HoACYiftgavq0BAiIJSflcpaReMiIq9CRV/L8EPCaAUuB94nmr14aqTw492zWCn2YQFBqUUvvnMM2i2Wti/f3/iM46Pj0MIgWee" +
                "PgZKKGzH7jl54sTDXaWuBwrF4sy3Ysz0W1WAWq363xr1+o8FMT+bzXYU/jefeRrvf997YVk2MpkMshy43jDwd89LXJi1wJUDnslg" +
                "/NU/gPEHvgeEMUjHSQo4jN8k8fPUf2vlXuKrD/OLUoF30HFB4EXb/ycd/n5c2W7S3WqPzxGB624Of7ayFU80e5Cjyu9j5Dhz+hSO" +
                "P/dc6mts27YNBw4chOs6YJRibXVt+xOPP/4R27azYYPrC7jYO9/5zhdczm02mz9YrVT/FyEEUikYhoHevj6/ABPL7596Eg998INh" +
                "0SbDCc6vGfj0aQeVhgOuHGR6+rD1B16H7u07oBwHhChNOG1BxX9GKcKfsdjjUp9PtVSRkpD9UxoXEKaFqu0VECOJgvghted7P/bu" +
                "R/Tx7deXKvp4CgVHEjzXLAEA9uabXrmacSwtLsK2bQwPDyfuaX9/PxzHxvz8ArjBUavVxm3b7t80NPSIn47ftAK8YA9g2/YtlUrl" +
                "vUHKRAlBT29vqvCf/eY38eGHHgIhnmZnOMFzixyfOuXAsl0wYSM/shlbX/uDyG8ahrCtEJjdXBiKONm2ILXv2z+JClNKlRA+Up6D" +
                "xGulvX4IKTd8TpqjYESBEYV/WB3Ah5cHwQgBJwSmaeLS5CROnjiR+tkP3n4Yo2NjcGwHjDFcvHjhFyYmJn6G+YCQ+H/vRhclmqXc" +
                "6AJg1qrVv5ZC9Abm0t3Tk0pInD1zBg996IMgAAyDw+QET89zfP6cDQgXRNgobt2Bse97HYxSCTKYqukk7Ii1qhiVu9GTAits5/nC" +
                "Z/BkaAnJx8e1QqGD1Df6+9j4felxOEsF/mmtG+9f6AchAKcEhmliYmICZ8+cScZuQnDX3Xejq6vLqx0QiudPnvzj9cr6PsMwQBm7" +
                "uetmXYVftfrtlmXdF1C/pa4uZLPZxJubuXoVf/OB90O4buj2vznP8cXzNqgSgOuguG03hl/1A2CmCem6iRujVFLACuqG91RXlGDY" +
                "I3DtUkvrPLSvIBGngFWbGWz7jrApRMX+mEpTuA5aqG6AD3JU4tHVEv56vg+MAJwCpmHgwoXzuHx5KvGcXC6HI0fvAaMMIIBltUrH" +
                "njr2vmazaQoh4DjODS96s8K3LOtorVb7zzroK5VKqQWd9//1+1Cv1WCYJjKc4PgSx5cu2GDwhF/YugODr3gQhDEo4UaF3dlQIsJQ" +
                "iMZaaBW89mQQtDJvkOYFZE871VMqyQekhQcV+z5V+TTNSMMNkc+oVSKDEJFnEo+uFPGBuW4YfirLOcfpU6ewsLCQuN8DA4PYf+AA" +
                "XMcBYxxzc7NHz50985vcm5W8cQig/qTMRheATLVafY+SkiulQClNRfxCSHz4bx/C3OwsMtksMgyYWGd49LwDIiXgOsiOjqP/FQ+C" +
                "cA4pRMrNS3e5bUEnOfy2dfnCDgFdm63zcvIgb5cRIkev9AWvKcM+RU3omieKC1cHe2mm3glHqBhG8TyBwqeXivjYQgk55mUuUMDz" +
                "J06gXq8nXnvvvn0YGR2D7dhgjOHc2XO/tbKysj+TyYAbxoYXlVJio0sphXq9/iu2Zd0RuP6ucjk17n/ukc/g5PHjyPl5/lyT45Fz" +
                "AtJ1QaQLs28QfS9/NSg3oWJuP2FtEZcctaDI/7UrBHY6sRNRAr2gk6R3Q48h28KXSkGGAo8h+7iFx4tLCY+hUodHlYrfB4UMkfjb" +
                "2SIeXc4hzxUY42hZLTx/8iTibeTExwMZMwOlFCzLyj337LPvsSyLuq6Lja4begAhxNZatfpbgfCzuVxqTf/c2bN49PP/5Bd1CJqS" +
                "4XMXJOpNB0xJ0FwePfe/CixfgBQpwlcqMqSp371oehZVDOWTOfHmDRlTAuF7AOHHf+9S7UtXFrQxhNQwRPt9KMTeYsIbdAawSHqM" +
                "FK9HCUAV8GfTRZyumshxBc4NrKwsY3JiItl93NWFAwcPwnVcMM5x9erVB65cufITmUwmLB2nXfRG1l+r1X5HCNGlvHiB7nI5SQpV" +
                "q/jo333Em+tnFIRRfPESwdyqDZMCoBRd9zwAo7cf0nEiXThKz6lTUjkVicVRoBbm1X5MD123hvgjBZ5AESJX2xMo6ZWCdbygYuEh" +
                "wBaRsBTpLFIa+FQdwpcGDFU69lEK4FSh5hL831MFrLoMJiMwuIErl6ewvLyUkMOevXsxtGkTXMcBpQSnTp78nWq1WnYcb8A17ero" +
                "IoQQaDab9zTq9Z9AQPUWi+BGcjrnM5/5R8zNzXkDmozg+ALD6Ws2TE6gXAf52w4jM74dQkv1oq5eRdy6DIkTbWzbf0LcTUfKuLp7" +
                "l5r1B9YePC9u+UFZV+sDCJpBAkHKgD9IKSBJPQzFMEMEpMY+t44nEMtAgudnqcJkjeEvL+dgEPgEFsH5c+fhBmE0SA0pxe2HD/vU" +
                "N8XK6srWM6dPvaPeaKBSqaReGxJBjUbjt6VS1Gs+YKmo//y5s3jisce84g4jmGtyfG3SAacSynFgjG5F7tbb29RuijtHmnuPuVOl" +
                "3/iId1AxpB+1eKWHgVDQ2hUAwtjjZKQnMOppoHuECE5RCe8WFXoaAEzhEmL8Q5Yp/NOcgc8uGCgYBIwz1GpVXJ5KpobDw8PYtn27" +
                "15JPKSYnJ3+5XqsOkZDvjl60U2wQQrzcalnfH3TRlkqlBNvnOg4+/clPeZwy9dq3vjwl0bT8Cl4uj9zho1B+IUSqJBiKA6qIW4yD" +
                "vFgDBzRhQ3uM8it2XsyHBvD0sKALHX5zh4x4jriHCZtEdOuOeCxEQkYEFCo9PMRAr8YxpPUnenUKhb+6ZOJakyJDAc4NzMxcRa2a" +
                "nDo/cPAQDNMAQFCtVvvmZufe0dPTg+5yN3piF00jB1zXRa1a+02lJJFKghsGCoXkGNRjjz2Gy5enYGYyXr6/QHFpwfZatqWAedsd" +
                "oOVeSNdtL2HQiRbdHao4b6401K7Cn8WtXPnWKjRr9SxYhuVbTxFkOxRol1DS/51f5pVaV4+WXUQ6hWM9A4i9T11h9fCmQk8S5R70" +
                "dvS0MCGVAicKy02Cv5zk4H4xSwiJqRQv0NPTgx07dsJxbHDGcPHChZ+bn5sbWltdxcrKSuSihmEgfimlbres1veCeJZRKhYTDQe1" +
                "ahVf+uKjMEwTnBCsOwxPXna9KRrXAdu0GXz7LVCOnUjfwlwbmptF1ILjyhDFAEhRgrbQA6uO5/p6JtC+kOgCCpQJWktYnCvQlVSP" +
                "/SqSNrYtX6YUh1RK9SCCH2KhgTDg0VmGx5co8twjiJaXl7C8vJxQgv3798M0vSbcarXSu7C4+Lbevr5wVjG4ErUASikajcbbhRRM" +
                "Sq/xMJ+S9n3961/D8tISDG6Ac4KnZoD1hgtGAHADxr7D/lydisX7KGMXj6MqDgI7IHLEWrhCT6A3b+oCDNO/aAjQaWERAXRtZdD/" +
                "rogpn/4+oZFQ0JpMgwsamFQatpDx1FZFU8JAEYj3YLxvgqIpPS9ACMHM1ekEv1Du7saWrVvhOA4IoZi4ePHnQVDI5XLIZrPhRYOd" +
                "OEIISCnhOM5ws9H4IQJvhKtQLCZif6VSwePfeAyZTBYGA+abDM9fd8ApAOGAbtkF0jcEFbj+CLiLDmOoBJKOxWeNfNF/JlL6+JRM" +
                "tnIFrxt26qioB2gTQzHBa5mE8lPDNgCMNpFGuAep/zxm/SnoP57uhjUHXRn07IABZ5eBL1wnHiBkDOvr61hJ8QL7br0VjHFQSrCy" +
                "vLxtcmLiNc1mE7VaLbyolAL61WjUf8R1RZeC8qdgC6k9/KurK+AGA2UUT89ItCwXFBLI5EF33BrO/+nuPd5+jVSXioiHkDLK50et" +
                "X2luui14JXXELyO9elFAGAV70ce200HhA8oo2aRCejkJ/NrKoqRm/TFlj+OBTsBRTw6Iv+7uI5MKFQe+F6CYnZ1NFMuGhoYwPDwM" +
                "13WhoHDx/IWfbtTraDYaaNTraNTroJQyBBehjLea1k8qfxQ6l80lKN9Wq4VjTz7pLWOiBAsNhnPzLgxGACFAtuwCSt1QKTx//MPG" +
                "vYKeEcgUQKijc8SQuVLREBByBT6wi8Z9CVe2wZ8btncj5AXClm/d2qXaICwhQRqFHixRaYx6Bd3f66lxvG8xXG1HgUurEl+6DuS5" +
                "5wUq6+uorlcSxrrrlt2Qfpa2sDD/Csuy9ub9NTuRECClhN2ybrdt63bi/+VCMYn8T518HvPz8+CGCc4oTsxJWJYLAglkclDju6Gk" +
                "iAk2CuxUrAQrdaIlFnN1tK1bnZDRvn0Zi9OBB/AInShxFGcHlWxXBQM84GojYCrW9h2SS7FMJTJGpmcOQLvqGGEw40RWMlXVFYL4" +
                "xkF8RXj4kkBTeFhA+VPU8a+tW7aiVCpBSoVWq2VeuXz5TZZto9lqodlqgeqURMtqvlFKSZTf5pVW63/66WOg1BvUXLUpzs654AyA" +
                "EFDDW6CKZd/647V4RMgaxJGzbFtNGN8lEkqQxvWrgNCRUUWIIH8truuAMEIOSRX2+Cu9x39DoiiWIag2pyFjXIFOckWFnsz94xhJ" +
                "xWIDZcCZJYFj8xJZTsAYxfr6Omzbisgrk81ifHwLXNcFIQTXrl97o+s41JuQlqDtmCsNy7JeFxR9cvl8IvW7fv0api5dgpkxYXCC" +
                "i0sK1aYLSgAwDjm2I4z9yQKOitTZlU6thl4BsRCRjLkyxRPEyZvQO0jdYjWF0K4wbGjVQd3i09hE1UH4EfZQJhnKeMVSJbxeivC1" +
                "YhkCDBC0lwmFRy67Yfbmug5WVlYSRrt9+3ZvFQ6hWFleOVipVA4E+5cpZwycMUipbnVsZ28Qd/Mps/vPn3weluUtMnIkxel5AQoF" +
                "CAHZOwhZ7oMSbmjtHTty9Fq7Zm2Jm6jH9hRPEM3TY+lakNppmUCcBErWDNLp4TaR1DnsyFh6GLh2IePKG/NeuuLr+wlUNBMgWj4Z" +
                "fE8o8MR1F1eqCibzegFXV1aT9PDICMpd3ZBSwrKabHVt9XuLxSIKhQKo68d/q9X6HiGEx/sbRmKSV0qJs2dOe40EDJivE8ytu2DU" +
                "p3k3bYGiLL2MqiHjaOrXnrtPtSrZ4caGFioj3sCVKShetVnARB3Av/Tnqdjfb6eLSVySHB3X0z+0wxqiKWL8HkTobUQ7mVSEDYr2" +
                "zDECVBsSj11zkOEElDI0Go1wR2HwZRgGRkaGIVwHhBBcn7n26mqthlq95tUCGOdoWdar4fP+mUwmkfvPzl7H7OwsDM7BKcXEsoTj" +
                "eJO5yOag+jYBwtVoXRVlwVQKotdDQcwl6jexDaoQS+mQcNNxpk/n++MeSMbcrz7l4+rPR0p46SD8CF2M2O9SwG+8shjpN1BR6yfx" +
                "732l+Oq0A1t4lUIpBdbX1xNeYGx8PFy1v7S8dCdjbFO53O2Vgx3b7nEc5/YgDUnb1DVxccKrHzMGWxFcWnK9gQgpILsHgFwRRIgE" +
                "YZOMixrql+kkitJr9LonkElgFhZ9VNsj6DP9QrSFKVLcvw4Q9SsaTqJeoB0i2ilrhHaWsSYVKRNeL6rwSGIcPfbDE3iszux5AQac" +
                "WnQwXZEwOQUhFNWUAtHw8AhyuRyUAlrNZnl+dvZwrVr1mkIdx9nnuu5gsHw5m7LIYeLihXAp03KDYLEmPOYPCqp3U6S8peKINwF6" +
                "OjNpGyHsuMXpRR/p5/yR1C9SPFIpfICKFH8ioFG1+wZ1jCK0pRBKex3oHgsyqtSpbWoqZB+l3t0UI8JIO4/U0sC2J2AAak2JE/MO" +
                "TH87WrPZgBObrCqVSih390BKAdd1sbC4cNQDgd4akiNSeLk75zzR9NFoNDT3TzCzrmDZ/hsyMkB5AESIEKBESqUq2l4lUypsaURP" +
                "mhK0gZzmDWJhQGqhQk/vErl1LAyEDaNhuphSJNL+tl6B1BWsXdRCm8UMQ1AM9SMZghSCVnWEVk9k0vK9yd620R27ZnkzjJTAdV00" +
                "G41E3+DQ4CCE76XXVtfuyRcK4M1mEy3LOuzFZ4lMyubt+bk5VKtVb4kBoZhZd3wmQ0LlykCuAEhvD46iBERKSEoB4o0eSek1uVEt" +
                "5SPaGBUFgUQwpkQgteWNJHyO9/xg/o34zyNKQfrzfdI/4CHYD6Tv9cdNDG+ooDk7tfVMaYukkKB9ZSy/11F+pNMo+LlUicpmpFYS" +
                "RdBa7E8qAyEKZxdtVCyFnEHhuAKNRgNdsfa9oeFN3mg5I1hfX7vl8tRUkXPOiXDdPUHdOm1vzczMVbiOA9MwYAmC+YoADSRb7AEo" +
                "A5GiHbP8Cdtwvo54H5CEwvUkQpX3bwJvHRt0JfCG5/xJ3rZleds/vMcIX+DUtwZCgGCuN3hdkjI9TDq1amsdyMnO33Y3ElLmDVSn" +
                "tBBI92xAwgvJhOtX2qRyWyGIlG3voBQMAlyvuLhWEdg7yCCVFwbiX329feDcm0Ju1BvDnPMxLoXod4W7NUwZUhRgdnbWO4WDEiw1" +
                "gfWmt54FhAClnjZQCawnVNK2wKkvROrbtPRMFpQSCE3wyv+e+oOnSnnolvqCEf7rUhIc59JWBOL/PJCyCsZ5tXHvmxk/0+cTddwS" +
                "H/qMADYVnyW4AVEUbzPXfhdx/RqVSnRMoHkCSoBGS2Ji2caB4SxsoWDbTri4K8QBXV3I5XKo1+twHIfXarU9nDI2JoXsC87TiXsA" +
                "pRSWF5fAGAOjBCtNhZajYBAFUOah/+C4FurHJgko6mmqotT/oG3X7mUP8JRAqTAE6OEAvuCDo1xUMOWLgD30Br6pL3ylhQsvPnoz" +
                "/YE+KJK+R0B1GOtSKU2cKrZPKLpWLta3eCPh6w2siIYa+GcdRV1/m0AgiBJCgYJMLDvhImxXuHAcBxkN0OfzeZRKJVSrVX+2QO3i" +
                "1Up1TEoJCQlOeWJZUavZxPr6GhjjIJRgueFxyGAADBMwcyFCDWKzCpSAaXgAgaA94dMQH3hbFijVlEABkgQj1MFsf+AN2l6EKE8x" +
                "iBYCSCzuk46LGzrMFsamkYAkW4c0LxBfNKURRaqT8GOcQChU6bt5/68HnkD3AJHCkF9dmlqx4fqGo6SC49gRBQCAYqkEIQQYY7hy" +
                "+cpWbpjGuJDCsyZKEwpQrdXQsiwEe2iW67J9p4wsCDO9bZyEtC0/uKHSC9pEqUiMlz4qlf58ehAOCCFQpP0cIj3LpWhjAeH/jGiK" +
                "QbRtHe15OI0zV+0tDp09QPrEcRj307aKbcDpS3TaOJoOGpXsAPRk3NqDn8u2MigFRoG5dQdNV8GgXlh0bCeh6KVSyVNMTwm2ctu2" +
                "xjxkK8FSxr1q1Spsy4ZhckgF1C1/LElJLwWkNIyxSrvZxOOPve3aAZ0c7ufxXCbxzZRKDxME1i6kv/BRB4TEX98S/tvf5q4pQigk" +
                "QtpMWSB4vYjSYWZPxXgMpBVmOlTwIsL2SS49PCSnl6LsYSj8mMD1VI+oFNcfZlLASt1Fw5boyRsQSiS4gGCCSPmG0mjUh7jVsvqD" +
                "D5m2q65arUL5p2vYkmKt5ckcCoCZ9W6/lJ5V+uatIL0HEQIiVfg7JQFB2rGdhBmBJkD/w0gNEIZ7eny3T2OKQIgWAvzUMIz5kVww" +
                "mQWQmLDj4SDao6fSR9QUkulfSgEo0lSSZvlx4Qf/jll7CGo1YMig0LAEVhsSAyUCIWlicMQbKc/7QyQE9XqjzCmlfcq31rQtH0E6" +
                "QQiFkoDjyrYVMcNP91SwAym0PiUlCKXtm6ztVpLSH0pIAX/hOihf0IKo0NJpsMwJAQ/gq05wQ/RdP5pChBZE0q0+Evc3SAPRQfDx" +
                "JtG02oBKifk3JfwYANTjvu4JqFJo2go1W4Z7E2XKIGo2mw3vQ7PZKHMQUg6OZkvzAI1GwzuMkQBCAq4QoQ0RbkTGXYKuVUWjglF6" +
                "ZhAoQSwDUL6AqR87gxM+A28QWHWw5UsF/1bt9a8kBIdtUyea2RN143UziUnkWDOn1HsaYu1fKoXVa7OAMlIZjJd427E/WfbV/534" +
                "Gdppriu8DI36JJwUydVxYaFPAUIIzqUQmVCAKeuubNvxFwkQtASBI7wpFU+qzNdUL90LXXmYBraFHvRM6+FAksDNk7DZUQWbPaFC" +
                "10+D9W3Bz8J1blqq5zOARAN7RLXTP6j0NDBBCOmt6indOZHWbkSbOGSc1UvrZ9Rcd3rM16w7BgxJu3jQJog0Ugj+rsBgW6pSSQVg" +
                "/uEYUnpmxZVShgdcZKoCSCH8M3QIhH94AonvwPPfnNJCCIkpQeQZ0scLSnmHLECFxA/RSGDqm5rwR6XbO/9ISIHSwMkHGYC2OYp0" +
                "2iG44VIKlTrHh1TXnxYSou3qen3/BQtfz/eDRZAx109UtFsokA4h6TvtKG3HQUUUuFTKUFCJvXjtRhAvRSSk3aMX2Y/nvyHlAy4d" +
                "WIWOQrd8TQk8kOilkJIijOsytF7iC7k9LEIDqyf+KjfSju16OAijfwf0j5vMBuJgECnxP7kzIN7x0+4VvCnhB2RP5N9I/F5nCtv8" +
                "gK8ExONfkwrAwuNxoAAeqHBQXUvbgugdrUaRUCopPZTvtydBeuhfFzIhnpUTBIuOqQbBlVc88h+r9JDgZwnEJwICly58q4ce++Pk" +
                "T4z+JR04gFThx04QUSkcwM0KPtXq42Aurgwyme6FP/dDAIl21EY9gr9ZlfpGmwZ2wnoDIeAgxG73niVjBufc0yRCYDAPkbshwSJD" +
                "NxWuUY2jfz9NhI8RlP89CehcHSdoIUHP75WEtuufhFlHGPtDNjCWjcQQXqeNnukbPeKxP+YFkMwCAH16OK2XL1rXTxU+NvAMwX2X" +
                "6UoTPoYEJEnyAwspEOwVVkqBE8BV2iBFQgEYD3NtgxEwqgVS123fXB8DeKe+RpUgInjl1+p8AekhAgRhSFAa6CNog71Ivh+CQm2V" +
                "qwoOgowVgDbIAlLrAfooe2RkKzrC1UnwEUYv7so7WTpSfp74twTRAa3uATxgFxJm6ZiuvUWUAOCE0mbQx5d2hm02lwWlFAoEOcNT" +
                "goYKFwS0s4Agngd/WAsHYaXQo//80BC1/ADheame8r/XFaFN8YZuP5YGhu7fQzjR/b431Q+gb3lLrnaLLrSITTJpWYJO5Ybcfjy9" +
                "k+24nsbydRS+VG2CS/s3pAzX6QdJcVpabzsOhOuduaBACHcdZzVwB0KkM0fUF453KCODFwQACDssRASuN4L6/YOkguwgKOiEYFDC" +
                "5/5pBEiGikDbyiFDKjiAdyoUflAcaWOAIPho6d9N1IOjhFDaeroUHNBR8LFYj2iJN2rtiAo5gRuitHAiLPjV2AwlKGRYWPpIUwCr" +
                "1YLruuCGAcPgLU4IWQ503HWTHiBfyIfAzWAEeZNiSXmHMijH8jqBAnaOJFF/oAigFNrB4G0P4J0nF4LBCEUrEd227r9+SP/qyqCn" +
                "eCplu7cCkrvGkxuB01bSbrgNLNGlg6S7jwFAEkPuqcIPcnrd7Sc8A0JgqKTXDVTOc/+jkg7MbtNvX5PIZXPrPJfPzXt8P1KLB6VS" +
                "Fzjn3sIig6A3T3FF+XvMXAtEulCUgQTC0v6m8jpAwsMaA3Yv7HCRvtsHPMsIXb4+DuurFyWh24dfBlY66g97AJC0/khZ8AUAwQAB" +
                "xJc1KM0N6/15qYJHQuCpQx66MoRoH0m3H8keZLsP01XoKjH0Fo2QqzFSinuerBWEK1AoFVc5AZkOsmbHX+Gmg4dCoYBMJuMvHSIo" +
                "ZgP6l0C5NuBYIGY+dI9x9i+wYkK9TCHwJkpj6vy1/uH3yvftAeGjCAERbQXR47ryNUAFTR+6QsQk2ulYlXj6m7qvSRdsyqhW4vc6" +
                "Qwd0tvoIiIvn/bLt5m+QIgopMdBlIp81/DBLwIykAqytrYUkEGPsOhdSXvUKBxKO6+0HMrSu4GKxiFw+D2d9HSAEw2XeDqrCgbKb" +
                "oEY+wqMnKGDlZ/dEtXsHgjgfPpO0Q4n/i1AR9NCgewW/GVTP/YP3FtnCRUjUcjuucNXrGrFWMm1mO9Klq6Hxdkakkl4itPygyUPe" +
                "OCvwP0TnbKD9cyEkNvdlUcgwNG0JQigMluIB1tfDG2AYxmXe0909s7y0JAAw1/XaiHQFMAwDvT29WF9dgwLQXzLAOA3NW7XqQKEP" +
                "xDd1FaeCgXaGQNqkTqQUG9YCVPJgP10RlIqiuYgyaEFftdmwVP+eQgtvUB2KlpV1wUbWfqbX7sMiWSer99uN9Pyf6I0gaYAwBSgq" +
                "KbFrU8HLAgjAKEu090spsbq+5tH6rsCW8S1T3Haca5zzWcuyxqSUaDWbyOfz0W7S/n5cmpyEVASDJQN5k6Fpu15VrlUNUSjRMICO" +
                "8gnRSIkAIJIIue8phxaqw+fpqBBtZQg9Q4gstQfG4P/NrvMnqamARn2r5D77eJPmjQTf0eo78vwaBtBifvyxBiHYPVIImRPOmbdG" +
                "PhL/K6isV0BAYGZMKKhzXClVpZReUkqNKSUTg4UAsGnTkHcjFVDOMwx0mZiad0EZgbSqgOsAjPnAJMb7B1asW74f10NvoNpKEhJC" +
                "mvUHnT2KaECQqBDtIoJbfNCmuf1v5UA1ktzeGEkREvG9Ay5IA28eem+XfkmHLCGS+nUQPlEKwpXozXPsHi3C8fkF0zAT7m11ZRXN" +
                "RgPEW0HfpIxPUM4Ycrns6YAGrjeS/eRDm4aRzWagAGQ4webejN96RaGcFpTdSBYmpPTftJb/SgWiZOjmiGzHsqDcScLnqZDyDB4b" +
                "dsb4YztEu7z9LlKjWLXXfaGX1F4reI/Cf00RfS+hYHy0Hgd07c/UHg/yUjwVuQ/t+9OmiMP7owmfpDCFriuxZTCH4d582GqXdk7z" +
                "3NwsXNdrF+/q6pru6emepYZpIJvNPR0cetRs1BOMYE9PD8rlclBDxq7BbPv4VSkgm2seU6e/2ZDhkpG0hYgks0VkrPkxuGlKRhsj" +
                "pXaj9f+rmJIEvxPSuwKBtue9Yldb8bzHp7yW0pWx/RlJrH+faO89vB/634gpWeS5UBGePw3tp3UFua7A4R09KOV42O+XNuBz9epV" +
                "j78TAj09vc/39vbZ3LJsMMaeoZQKKQWzLRvNZhNFbT+QYRgYHhnB8vIyhAK2DGRRzBlo2t5pnrKxBtI10h6u0rqEw/SOIgoQtRgf" +
                "CQv6z7XQEKSD0Ng/aGf5ERJD/H6WEEH1uFlOOBY2VDI7SGQGaTE+jSGEiiD7xHNjxJCXMaRwB0EZ3o//9+7pDT8Z94/jjRSBXBdz" +
                "s7OgjEEKgXJ3+fFqpQJqmiYKpdJF0zSveKPPAtVqctvU1q3bvI5dBfQVDWzpz8J1vTAgrSqk1fCImJTWJaJ8a1RpLk6Flk5UzFVq" +
                "Vq1bkL7njaikdRKZDCkRr6GS7jfyWBV1v0QLRZEwo2Tsb2jvTw9Z+t8TmscDvM8uY/cj+Lw+VtBDUhwDCEdipCeDg9t7YLsqbPuK" +
                "cx6Li4tYWVnxSsCcY/v2HU+Vurq88XACtLLZ7BPBHPv6WsqCgc2bkS/kPY1jBAc2F0O4JIUD0VzzsJn+4aWMvnmpEiNPYVzVxp7b" +
                "7liL9zpOUDIRAqIjuEmM0MYecYHJ9u90QejKqNIUSiZwAKRKhiUVVUy9pz8MN0qL8RpG0kNEcG/jGMCyXBzZ04eRvlwY/zMpy72m" +
                "pi6h2Wx6i7+7umaKxcJJ4brBoVESpVLp8/ArebVaFXaMFu7q6sLI6BiEEHAlsHc0j0LOCBs4RX0JSrgJijLezBjEZBJPefx4rYNC" +
                "KO0GSx38tYXpLQUQCeuNADmlOsT+2KViVh8HhfHX1wGpiFu71F4vpkgxIidi9TIKfhPETywMMAI8eHhT2OrFGEMmJf5PXpwAIQSO" +
                "62JkZPQxwzBrrhDekTGEUOTy+a9zzpqAgmXZqKyvJV5k566dPqGgsKlsYs9IoR0G7BqUVQ0HRKPWF0Wz8Q8V9QhxK4oh4EAZRPuG" +
                "JUNA1Dpf0JX6HBVz74HQo58xAu6E7k2SGUFy3EtT+sR9St4jSAXHEdi+qYB7bxuApbl/Gp/uqlQwPT0dVgfHNm/+fLAY3FsXb9ug" +
                "lFzO5XJPCeF5hOWl5O7Z7du2e0sH/dO8j+zo1oCchFNbTFq2t8Mlmglo2t52w8lhRxK5ybF0MZYV+LtiItbcUcCp7jmZgkazhGT6" +
                "p4eVSCoYd/Ux60YiE5Cx8KMini+Km9qfwWq5+L47hzHUm4PwayXZlPU+Fy5ewHplHVBAsVis9fX3f7FSraJer4O2wTNFqVR+GPAG" +
                "RNbW1uHETvMsFIvYum0bhHDhuAq3juUx2peD63rcs9tahbTrkRUmiIOteFiQiMbOeFxX0Z9D8wyRTpi4MEUUUGqHCSSvSI6uhykZ" +
                "4x46/G3/OfE0LZHq6kYRYplov18iRU5rCFEKUgh0Fzhe/7JxOI6XnhgGTwyDAsDpU6dB4G0OGR4Z+XpXV9fVoEOYEkrDCl2pq/gI" +
                "57ypFGC1mlhaTi4d3LvP20CtABSzHPfv6fU7lgmUcODUF71sQMhUQUYRr4zEyjjyjz8/oigiBfHHM5CNrH4jEiiO3OMKJnRFiSu6" +
                "TGQQSPN+/n686GeMYgAA6eyflGg2Hbzy9k04sL0Htiv9qZ9cAv0vLy9janLS3/mssH37jn/gjHnj/oyDcs7Aubf1O58vTJVKXV/y" +
                "ZgGAhYW5hAKMjo5iZHQUwhVwhMKRnWX0lbNwhdfx6zSXIJ2GPzOop0MyxfWluN84ohZJwieSBsaJHxnDBi8QB6QjfhV6FcgOXieS" +
                "gsoIs5caflK6geLeL+GFgvkLIZHlFG9+cEd49g+jLHW728njx1Gr16CURFdXeXnnzp3/yA2OXDaLXDYL6tgOHNuBa3tYoLu7+/3w" +
                "T52qrK+hUqkklg3t338ACt6Cpv6SiVfe2u+PIREo6cCuzUUIjcjNSChCLDSk5Psk4mZlFPTFGLKol4jG1pu6REr4UPHYrSKAMM5m" +
                "6ooJDStA9xpSxriEePzXwR8iILrRdPDKw8O4d/8QWo4MdwLHW8Bc18WJE8fBGYNjO9i2ffsnKaOL1WoV9XoN9XoNPKdV/ggBzIz5" +
                "+evXr11uNOpbhZC4fu0aurq6omBwxw4MbdqEpcUl2IzigX29+OrZZSxVmuCMwmktw8j2gZlFv2aAKCtINFYwYPSC2j5pN1oGJV2l" +
                "T/aqdskwKAHHewESc//kJstB+tBLWkVQr/AlWMLkAqewPS2+2CmN/QNSC0Px4pCSElmD4udedwsYI4BLQClJVHAB4Mzp07h+7brH" +
                "ChoEd9511wf6+vogtNY/ysJ44J0ZYJqZxuDQ0PuEkKCUYXFxMVEhZIzh0KHb/aWJQG/RwL8/PAQp/E4hJWDXr0eQMlQ6mtZHy6Ku" +
                "XCNFEghdxmoDMmrBCQ7/haSAcQ8SDVVJviAWPkJXjyRfkOr5EMMHbXyEOJGmFBp1B2+4fwuOHhhC0/Ko4mzKuQ5KKTz5+ON+J7CN" +
                "bdu2PbZj587HGeMwM5nwoq1WC5Gr2UR3d8/7M5nMmlIKtm3j2szVhHbt2LkTwyMjEELAciVevrcXu0bLsB0BQhgcuwKntQyqaIIW" +
                "RdzdxoFbhPxJSb+kBAnIn+hSvlgVUUZc+k25/7igO1b3dIZTRt22kJ3BZFo2lEb+RGhkT5lcW2CwO4u3//Ct3modAJTQVOufuHgR" +
                "k5cmwQ0P/B04ePBPKpV1tbqyAv2iruNAv2zbBqN0tq+v/yEpBCgluHbtesILUEpx1913ebt9FJAxGH703lEYjIXsYKt+HcJthR8g" +
                "DgiTN0CmpI8yQv4kYv9GgC6CDeSNL9WB9hVJSjv0FkImlDSevkJ7DGLeEGkGEJTRdW+gFFotB7/4g3uxZ2sPbNfrxs7l8onYr5TC" +
                "V778z5BSwnUcjI6OnR0eGf10Zb2CZqsZuSIYILZi/I/m5+d+ynVF0bYsTF+5jFv27I3WB8Y2Y8fOnZi4OAGbUhzYUsarDm7C5565" +
                "BjPDIKUDq34N+dIWqMi+Hu2KdnYhbeoj0guoYl0/egdo5Iw1smEr2LfWHYT23t54gyhSmkcj8R9hD0B021esCSSCBdqjZPWGg/v2" +
                "D+GnXrcXTcsNR71z+STyP3fuHC5euACDc1i2jTvuvOvdg4ODVjASFpn8qlYqqTeAcX65f2DwfddnZt7BGMO1a9cwMjqWOD72rruP" +
                "YHZ2FrZtwxESb7pnFKdnKpheqMI0GGx7DaxZQDY7AOVPECGt/KuIttZFK+dqHUGRTn3VHhPXha6QshziRg2A6gaAME2R0n4fFzrQ" +
                "uTkUHQZHUgZKXEegnOf47Z+9E7ksR8vyJrYLhUKi918IgS9+4QtQSsFxXYyOjp4aHh7+/65OT6cO/6YeHGkYBhilGNm06X+amcyK" +
                "9BZKY3LiYurWqcN33AEpBIQEygWOn3nlNpgGh/A3f7Wac3Cdqh8Kkh0ucVeexiC2Q4BMNGZgI0Yw7srTrg0pY5k4wzaCR4RWoEqh" +
                "wfX6RbRjSKbiojgDCCHRajr4tbccwl23DqHlL+nKZDKprN+xp57E5alL4N4BoDhy5J7f7erqsvP5PAqFQuLiaYdCBtrISqVrW7Zs" +
                "ede5c2ffxRjD/Nw8FubnMTg0FHnonj17cW1mBlenr8KiFAe2lvGjL9uCh/55EtRkgBJoNGZQLGwHpRxKyXYPYLjMgCASJ0g8BEQ2" +
                "OmnPVYnWb3Wzs2A3CgNpDaLanv4OywWjIQAdmkbjFq+HB7Tz/2rNxg9+z3b89Bv2odHy5rIpo6kHelUqFXzpi18ENwxYloV9+/Z9" +
                "+a4jRz6mH8ad8ACdPz2BEALDI8N/WiqWzgX7Zs6dO5eYICKE4J6j96JQLEBKgZYj8Ya7R/HA/mE4LReEUAhhodGYSaw0SfTgxVIl" +
                "xNm1RLYQzSg68vk3RQRFPUXytVSkJzEBCON/RweqYZUvCiQRyyDCKqMCGg0bB3f34Xfffk+4ggbEm9VIG/v6p88+EjZ9mIYhXv7A" +
                "K/4z8S0kfkJseFJscHpUp8vgRmPnrl3/ScGjeqvVCi5evJD448ViEUePHvUGOf1VaD/74A7s2dIL2/aUwHGraDSvRytf8XKpELGc" +
                "WSX76jqFANWp2+dmuQCV6BhC7G+k9hzIOEPoC1rvbZRpzZ4qWmjSMolWy8Gm3hz++NfvR393ziv4+Id5pPX7nTlzBk8//TQypolW" +
                "q4l7jt775zt27jzmuq5/Qlr6FRaDOl1SKQwMDj4yPDzykOs4YIzh8tRlLCwsJN7E5vEtOHjwEFzXgSsVChmGX339Xoz0FX1+gMJy" +
                "VtG05kH9Prc2b57mAVIIlg78f1zQiDWl3MxFYilj4m8KmexUjjeMxOoHweKAVB4gXhzz34Nju8gZFH/06/fjwO5+NC2vNmNmMqk5" +
                "f61axac+8XHAx2qbhoanXvHKV/62Ut7GsA0v4bq4mWv79u2/kcvlZqXfLnXq+ZOwLCvxZvYfOIBt23fAdVzYrsKmnix+8023oa+U" +
                "C5WgaS+hYS9E2LK4C0Wi5TpZLEoWUWIdNUJ24AlUx0JUgjQSyQ4hxAWuYmXjDUJDuAw6piTBa7uOAAXw7l97GV51dBy1phs2ehZT" +
                "TnEHgE9+8hNYmJ8HY95o+KsefPAdhUJhrdlswrbtDS/aKTbol1IKhUJh7pY9e34pSNrrtTqeP3kiAS48PHAUmzZtguu6sByJXSMl" +
                "/JcfOYDuQlZTgkU0rYXI/Fu8eyji/mOYIRIuQpebFiY2iPVpv0ubDYh7hZQG0CQjKGPkVnrTiP65XdsFhMQfvOM+/NCDu1FruCHp" +
                "1inuP/aNr+PZZ55BJptFy2rh6NF7/+LgoUOfdnxvfaOLpG0F6fRFKcXJEyf+9PLU1H/gBodt29i7dx9u2bMndcHkV7/yZVTW18EN" +
                "E4Usx9mrFfz+R09geb0J02RQkMixXuSNwbBQFEH0eu7vzwwGp40oEkvuSRLEqg0HQTdeHXej3D9ZHFIpxSKd6EFyqWbICSjYtgCj" +
                "BP/9V1+GH3vNHtSbrvd7SlEqlRJt3gAwOTmJ9/7lX3jH3bouhjZtOv1Lb//lewqFQu1m5UqsVuvm0yJC4Lpu/plnnv7KysrKXYwy" +
                "CClx1913Y2RkJDU2fe1rX0WtVoNhGChkOSau1/AHHz2Ba4tVmBkOBYksLaNgDHmbAf0tzyqy91W755HZ7+j3aiOy54WuCImkg/EF" +
                "QimTwanVwVjq2OF3rZaDYs7Au3/j5XjDq3ehVnfC91wqlVJB3+rqKv78T9/joX5KwRmvv+2nf/qBbdu3fzNtz0NHmbov4MF++RCV" +
                "SmXXU0888Xiz1er3qoMU9973MvT29qbmpo994xuo12rghoFChmN2rYV3f+wkTk8twsgaACRMUkCRD4FRo31mkGb1EQFq36u4YG/k" +
                "GW56UVCK0G8k8DQFSZ0TbD++VrexeVMRf/xf/x3uv3MMVd/tkw2E32q18N6/+AtcmroE0zDgOA5++Ed/7KeO3HPP37iu2zHnT1WA" +
                "FxIC9FAwfeXKa44fP/4pKEWFFMjlcnjZ/S+PTBSFnqBWwxOPP4bKegWGaSBrcNQtgT/7zBl88dlpMIOBUgVGTJTYEAzqLZzwBkNJ" +
                "Ys5fxfe/kA6SJjccAk9IX2GDncKpFG+SFCIdSCHdG0gpUa/buOfgMN71m6/ALdt7UW844WKMTsIXQuChD/4Njh8/jmwmg1arhaP3" +
                "3ff/vOmHfvhXGGN4ofL8lhQgUILJiYlfPnnixP/rHVzsoqurCy+7/36kFZiazSaePvYUlhaXYJgmTE7BGcNHvz6FDz56FpYrYBje" +
                "isgC7UWOdre5fYINLF73Bjdp+fEdAzdYGUY6rg1JHxWLCj3pDRzbhetK/B+v3Yv/8h+OolQwQ4qXMtYx5iul8NG/+wieeOJxZDNZ" +
                "NJstHDp8+yfe8pNv/WFKiZDyhRe/2Dvf+c5vmSotFIvHHNsuLC0v3ccZR6PRwOLSIoZHRhIfwDAMjI6NodFoYH1tLdwLc8eufty2" +
                "tR8Xr1WwtFIHYRQOGhDKAlcZMMISG7JIsqAYWiPRVqyHR8kkLhV93IaPie4aiv99XbiJ1e764/1solazMNibw+/+6v345bfdAUYp" +
                "bL+tixtGR+EDwCce/gc89o1vIJvNotVqYcvWLU++/vVveFO5XG6lVfpuku7+1kum3ueXOH78+F9dvHDhZ7nB4douenp7cN/L7k8l" +
                "LZRSuHD+PC5evABCCBhlyGc51hsuPvToeXzyiUvemhqTgoKjQHqQJV3t/bYpAFB1Kv/F28NupgTcySvED2vs2Bqm7QTUrN6yXAih" +
                "8H0PbMNv/MIR7N7Wh3rTCV8mk82imFLdC+7Zwx/7GL7xja+Hbn/z+PjJ//Pnfv77yuXy7Lcjw29bAfw3SJ9++tiHpiYnf4IbJlzH" +
                "Rrncjfvuvz9RPg6+ZmdncfrUKVitFrjBYXCGrMnxzPlFvPdzp3Hq0iLACQxOYCKHAumBiZy/Kk6lx/4OAPCFfMJOvYDJRRHpq2Pi" +
                "KaTrCDRbLnZt7cbb33oH3vC9uwEQ2I5AMIqXz+XQqSjnui4+9vd/jyeffAK5bBatloXhkeELP/tzP/9gb1/flW9Xft8pBYDrOMZz" +
                "zz37/kuTk2/m3IDjOigUirj33nvRPzCQ+rxmo4Ezp09jfmEBnPmt6RkOy1H43LEr+PCXz+H6fAUwCEzOkUUBeZTBYfqW395D+m2l" +
                "fzeTDupVwJRmEBJTGuF6vfuDfXn8+Ov34a0/vB9DfQXUWyJUooDdSzurEQDq9To+8uG/xannn0c268X80bHRs2/+ybe+dnh4eBLf" +
                "ga/viAIIISD8QcMTx4//xcULF36WMgrhCnCD48g992B8fEvH509fuYLJyQmvHY1zcEaRzxhYqrTwmScu45OPTWB2sQJwb/VJThWQ" +
                "RxEcGT9buHmhq42s/gaKQNK3Ske+dxwBq+VisC+P1z24C29+423Yua0HLVtAuF4MoYQim8uhkG8v4Yx/LSws4MMPfQhXrlxGNpNF" +
                "y7KwdevW42/5ybe+Pl8sTlMgdQvIv5gCBNQj5xxPPvH4uy9PXf514p9fp5TCbfv348CBAx2F02g0MDFxEQvz84Dy2p0MzpDLcCys" +
                "NvHFZ6fxqScmMXltBVAS3DCQJwXkVBEmMghOHe0kTXWzrv9mPEJKSmdZAlIqbBntwmtfvQtv/P7d2LGlB44r4bgy/AumaaJQKKSm" +
                "eO1RrlP4h4/+PdbX12EaJlqWhdHNY19569t+6scGBgbmK5UKsn5X74tKAYJzB2dmZlCrVf+v08+f+iNXOIwQCse2sWXLVtx95Ag6" +
                "9SECwPLSEqamLmF9fR2UMjBGQ0VYrzt45tw8Pvf0FI6dn0W1UgcoRYbnUUQBGeTAwCM5/c1JOM4DI0HnkhgxJISE4wi4rkK5y8Tt" +
                "tw7hB75nJx64ZxybBouwHRk5YItzjnwhj2wu3/GtSCnxpS8+ii98/vP+SW0UtuPg7iNHPrx79+6f27Zte6OrXEaj0XjxKgClFFeu" +
                "XMbo6CgmLk687uKF839Vr9eHKGNwbBulUgl33X0Em8fHN8QUC/PzuHr1Kmq1qv+6DJxRZE1vLvHqQg1PnZ3F105exekrC1hdbwCS" +
                "wiA5FFkeWWTBwdvrJ8lN0n8q/f1IfxmT63pbuXvKWdyyow/3HxnDy+7ejJ3bepExGWxHemNyviJxxpHL55HL5VIRfvC1uLCAT3z8" +
                "YZw5cwaZTAbCdcE4x2379//Oa17z2ndOTU1hbGwMPb29Lw0FGBwcwtraGigltzz37LMfXFlZOUIJhZAe4XHLLXtw++HDqX1tukUs" +
                "LS1idnYW1fWKf7KZpwymwZAxGFyhMLdSx6mpJXzzwhzOTC/iyvw6KjUH0iYwZAYZZSBLM+CEgYL62891Q1eRY2Cl9M8r9vP5jMlQ" +
                "KmaweaSEW3b24tCtQzi4bxBjo2XkshyO4ylHoD8EgGEayOVuLHgAeOLxx/H5z30W65UKMpkMbMtCsVRaetWDD/5CX1//w329vZiZ" +
                "mcH4+PhLSwFWlpeRz+fRbDaLU5cu/eHVmatvl8Jzi7Zjo7u7B3fceSe2btt2w7+xtraGxYUFrK2twXFsEEJ9ZaAwOIXJOSilaNou" +
                "ltabmJ6rYHpxHRdnVrBabeH6Qh31mgvhAm5LQbrUG6tSFJx6ypQ1OfJ5jlIxg/6eHIYGCtg8WsLWsW6MjpQw2JdHsWAChMBxpTeT" +
                "r9rAk1KKjGkil8/75/NtHHeuX7+Ozz7yGZw5dQqMc1BC4DguNo+Pf/Xovff+4sDg4NlatYrBwUFcu3btpakAXvrShJkxsba69qbT" +
                "p079ca1W3Uwpg5ACSkqMb9mC2w/fgf7+/hv+Ldu2sLq6hrXVVdRqNbiuA0KiXS6cUZgGA6PUG1yRQMNyYAsJIRWqDctz1a5XazA5" +
                "RzFnolAwkTE975LLGmD+8SgSBNKVEAqRraXBecuGaSLrT9vyDixetDZSxVe/8hU88fhjaNQbMDMZOI4N0zDdw3fc+QcHDx36/Vqt" +
                "bhcKebRarf+tCsDxXfhSSsF1XfT29j588NChJ2euXv396avTb4VSYIxjamoKMzMz2L37Fty2fz/K5XLH1zLNDIaGhjA0NATLslCr" +
                "VlGpVFCv1+E4DoTwzsRxXAHmH1/rgVOKguGdmddXyoNQEh6wREL2tm2xTUuAQLTPHg6OqfFKrzBNE9lMBplstiN1m1bFe/rYU/jG" +
                "17+OhYUFmKYJbhiwLQvj4+NPHjh0+3/atGnTY14YEt8N0Xx3FED3FJTSa7fs2fO2TSPDnzh75szvrSyv3MYohRQSz588gYsXL2D3" +
                "7luw79Zb0d3dveHrBb3xff39firWQqPRRLPR8FueLAghIZX0G3S8gQpBggMWqd9yQPz1wzRsUafEK8xQxmBwDsM0YBoZmBnT261z" +
                "g7geT3GPP/ccHvvG1zE7OwvOOTKmCdu2USgWV+6//+X/Y9fu3X/UbDZt27aRTdny9a9CAXRv0N8/8KnbD9/xxZXl5befO3f21xr1" +
                "+gDnBhzbwfHnnsXZs2ewbds27Nt3KzYND99UddIDXXmgry8Eka7rhpcQwh9gaZ+aFRxEGYSRoFWKc65NTdNv6bMuLy/j2WeewTe/" +
                "+U0sLsyDcW8yVzgOqGHIO+6864N79uz5ve6enkuOk1zV/69SAXSOmxJS3759x7symcxHrs1c/Y9zc/M/02o1y5wbcF0XZ06fxoVz" +
                "5zG0aRN233ILtm7bhkKnQZYOSmGa5oaky3f6y7ZtTE5O4Pizz+LC+QuoVCswfMG7/ilrm8fHP71z165379t362O2bcGyrBsCxn91" +
                "CtD2Bg4YY9ODQ5t+bWh45M9XlpZ+efrq9E806vU+ShlAgGvXZjA9PY1isYCR0VFs374Do2Njqc0n/xJflmVh5upVnDlzBhfPn8fC" +
                "wjyEkOAG9zp2XBcZw5DbNm/+zJ69e//YcZyv5LI52LaNb7Uf41+FAsTDQle5PDG2efN/7Bvo/x+1avWt09PTb1tbXd2p/MJJs9nE" +
                "+XPncP7cOeTzBQwMDmLz+GaMjIyit7f3uxY7HcfBysoKrs1cxcTEBKavXMHq8goc1wHj3F+i5UIKgWKptLpj586HR0fH/pIS8kxv" +
                "Xx+mpi4hY2ZeFMr7olAAnfhRUoIxNrNr1+7f3759x59MTEx87+LC/FtWVlZe1Wq1CsFiy1arialLk5icmADnDIVCAf39/egfGEBv" +
                "Xx/6+vpRLBSQy+dTj0+72ffTarVQq1axvLyMxYUFzM3NYW5uFqsrK2i2Wt7pm4yDMAoO7m1UNzhGBkeeHhga/Nvbbtv/8MDAwLXZ" +
                "2Vmsra16YeBF9PWiUgDdIziuC0ZprVTqenhgYPDhRqO2Y2Vl5QeWl5bfuLa+dqTVbOa9wxG9EfFqpYq11TWcP3/eazRhHNlsFtls" +
                "BoVCEfl8HplsBtlsDhnTBGVenQH+8SlCeMflNBoNtJot1Bt1VCsVNOoNNJoN2JYFV3hLGag/NcUYg5IKUkmY1JC9PT3PDwwNfXZw" +
                "YODTg0NDxyqVijQz5ovC1b+kFCDKwUsI4cIwjMnBgcH39PX1vwdQOxqNxstmr19/db1Wv6daq24Vts0UlH/EnUfpNhp11Os1LC4s" +
                "eqST1pypH/uOlO8Bb/1KQBlTxsAJgZQC3ql5BOWu8nypq/Tc0NDQowODQ19WSp2kjApI5e1U9ufyXsxfL2oFiCuD8FO4XC47WSgU" +
                "J4vF4gd7unuyly9f3ukK9656vX7H/Nz8bVKpbY16bURKwl3XhVTSm4IBgStcKOVZcXiSNhBaMuVeuBCu8CybG1CAymazy5zzy729" +
                "vefK3d3PGtw4NrZ58zlALdu2jUwmh5WVZX/gkrxUbutLRwHSlMFLq9AyM5lTvcW+U4ZpfCCfy6PU1dVVyOeH5xfmN6+trW0ul7s3" +
                "z12/PlSpVvq6e3p6hCtyrutw13VNpRShjDmMMsc0TWttbbVCKVseHhleJITMNOr1qwcOHpxemJ+fVcAC5ya6e7qxsrQUDMr4+bt4" +
                "Kd5K/P8DAODnts7wGH1qAAAAAElFTkSuQmCC";

        EMPTY_ICON = "data:image/png;base64," +
                "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAABGdBTUEAAK/INwWK6QAAABl0RVh0U29mdHdhcmUAQWRvYmUgSW1h" +
                "Z2VSZWFkeXHJZTwAAADZSURBVHjatFVBDoIwENwBnuBVvZmYmMiVF+iT5QV6JHow8Ua8+gV3bAGx1GMXDtDdbqc702YASbF8Cv9q" +
                "ni/Wt9aN0GcRVXHIxXuDY/Fht5JyuUAHWF9bOVbbpM5O57sHlKwP1YCs/iiLUoosFY8BIFUKIA2QOgXMTQHVdZglAqrOTDm5wz/K" +
                "EGPKphr6e5isIefUkAYdTjV8S257yhQkm0OooQu+Pjs6HLwSbqPuCxdymMeQ62sQmQP8oubRsr40gYtigu4XIzReRmNvsNVeys0a" +
                "sP4FfAQYAFsPaftsOAb3AAAAAElFTkSuQmCC";
    }

    private BPEL2SVGIcons() {
    }
}




