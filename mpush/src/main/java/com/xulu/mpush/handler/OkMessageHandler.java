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

package com.xulu.mpush.handler;


import com.xulu.mpush.api.Logger;
import com.xulu.mpush.api.connection.Connection;
import com.xulu.mpush.api.protocol.Command;
import com.xulu.mpush.api.protocol.Packet;
import com.xulu.mpush.client.ClientConfig;
import com.xulu.mpush.message.OkMessage;

/**
 * Created by ohun on 2015/12/30.
 *
 * @author ohun@live.cn (夜色)
 */
public final class OkMessageHandler extends BaseMessageHandler<OkMessage> {
    private final Logger logger = ClientConfig.I.getLogger();

    @Override
    public OkMessage decode(Packet packet, Connection connection) {
        return new OkMessage(packet, connection);
    }

    @Override
    public void handle(OkMessage message) {
        if (message.cmd == Command.BIND.cmd) {
            ClientConfig.I.getClientListener().onBind(message.getConnection().getClient(),true, message.getConnection().getSessionContext().bindUser);
        } else if (message.cmd == Command.UNBIND.cmd) {
            ClientConfig.I.getClientListener().onUnbind(true, null);
        }

        logger.w(">>> receive ok message=%s", message);
    }
}
