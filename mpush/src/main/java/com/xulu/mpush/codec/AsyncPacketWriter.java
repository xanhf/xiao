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
package com.xulu.mpush.codec;

import com.xulu.mpush.api.Constants;
import com.xulu.mpush.api.Logger;
import com.xulu.mpush.api.PacketWriter;
import com.xulu.mpush.api.connection.Connection;
import com.xulu.mpush.api.protocol.Packet;
import com.xulu.mpush.client.ClientConfig;
import com.xulu.mpush.util.ByteBuf;
import com.xulu.mpush.util.thread.EventLock;
import com.xulu.mpush.util.thread.ExecutorManager;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.Executor;


/**
 * Created by ohun on 2016/1/17.
 *
 * @author ohun@live.cn (夜色)
 */
public final class AsyncPacketWriter implements PacketWriter {
    private final Executor executor = ExecutorManager.INSTANCE.getWriteThread();
    private final Logger logger;
    private final Connection connection;
    private final EventLock connLock;
    private final ByteBuf buffer;

    public AsyncPacketWriter(Connection connection, EventLock connLock) {
        this.connection = connection;
        this.connLock = connLock;
        this.buffer = ByteBuf.allocateDirect(1024);//默认写buffer为1k
        this.logger = ClientConfig.I.getLogger();
    }

    public void write(Packet packet) {
        executor.execute(new WriteTask(packet));
    }

    private class WriteTask implements Runnable {
        private final long sendTime = System.currentTimeMillis();
        private final Packet packet;

        private WriteTask(Packet packet) {
            this.packet = packet;
        }

        @Override
        public void run() {
            buffer.clear();
            PacketEncoder.encode(packet, buffer);
            buffer.flip();
            ByteBuffer out = buffer.nioBuffer();
            buffer.toString();
            logger.d("write packet end, packet=%s ", packet, (System.currentTimeMillis() - sendTime));
         //   Packet decode = PacketDecoder.decode(out);
            while (out.hasRemaining()) {
                if (connection.isConnected()) {
                    try {
                        connection.getChannel().write(out);
                        connection.setLastWriteTime();
                    } catch (IOException e) {
                        logger.e(e, "write packet ex, do reconnect, packet=%s", packet);
                        if (isTimeout()) {
                            logger.w("ignored timeout packet=%s, sendTime=%d", packet, sendTime);
                            return;
                        }
                        connection.reconnect();
                    }
                } else if (isTimeout()) {
                    logger.w("ignored timeout packet=%s, sendTime=%d", packet, sendTime);
                    return;
                } else {
                    connLock.await(Constants.DEFAULT_WRITE_TIMEOUT);
                }
            }
            logger.d("write packet end, packet=%s, costTime=%d", packet.cmd, (System.currentTimeMillis() - sendTime));
        }

        public boolean isTimeout() {
            return System.currentTimeMillis() - sendTime > Constants.DEFAULT_WRITE_TIMEOUT;
        }
    }
}
