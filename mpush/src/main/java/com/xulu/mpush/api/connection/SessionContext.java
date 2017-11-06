/*
 * (C) Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     ohun@live.cn (夜色)
 */

package com.xulu.mpush.api.connection;


/**
 * Created by ohun on 2015/12/22.
 *
 * @author ohun@live.cn (夜色)
 */
public final class SessionContext {
    public int heartbeat;
    public Cipher cipher;
    public String bindUser;
    public String tags;

    public void changeCipher(Cipher cipher) {
        this.cipher = cipher;
    }

    public void setHeartbeat(int heartbeat) {
        this.heartbeat = heartbeat;
    }

    public SessionContext setBindUser(String bindUser) {
        this.bindUser = bindUser;
        return this;
    }

    public SessionContext setTags(String tags) {
        this.tags = tags;
        return this;
    }

    public boolean handshakeOk() {
        return heartbeat > 0;
    }

    @Override
    public String toString() {
        return "SessionContext{" +
                "heartbeat=" + heartbeat +
                ", cipher=" + cipher +
                ", bindUser='" + bindUser + '\'' +
                '}';
    }
}
