/*
 * Copyright (c) 2020
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.acra.security

import android.os.Build
import java.io.IOException
import java.net.InetAddress
import java.net.Socket
import java.net.UnknownHostException
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

class ProtocolSocketFactoryWrapper(private val delegate: SSLSocketFactory, protocols: List<TLS>) : SSLSocketFactory() {
    private val protocols: List<String>

    init {
        val list = protocols.toMutableList()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            list.remove(TLS.V1_3)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                list.remove(TLS.V1_2)
                list.remove(TLS.V1_1)
            }
        }
        this.protocols = list.map { it.id }
    }

    private fun setProtocols(socket: Socket): Socket {
        if (socket is SSLSocket && isTLSServerEnabled(socket)) {
            socket.enabledProtocols = protocols.toTypedArray()
        }
        return socket
    }

    private fun isTLSServerEnabled(sslSocket: SSLSocket): Boolean {
        for (protocol in sslSocket.supportedProtocols) {
            if (protocols.contains(protocol)) {
                return true
            }
        }
        return false
    }

    override fun getDefaultCipherSuites(): Array<String> = delegate.defaultCipherSuites

    override fun getSupportedCipherSuites(): Array<String> = delegate.supportedCipherSuites

    @Throws(IOException::class)
    override fun createSocket(socket: Socket, s: String, i: Int, b: Boolean): Socket = setProtocols(delegate.createSocket(socket, s, i, b))

    @Throws(IOException::class, UnknownHostException::class)
    override fun createSocket(s: String, i: Int): Socket = setProtocols(delegate.createSocket(s, i))

    @Throws(IOException::class, UnknownHostException::class)
    override fun createSocket(s: String, i: Int, inetAddress: InetAddress, i1: Int): Socket = setProtocols(delegate.createSocket(s, i, inetAddress, i1))

    @Throws(IOException::class)
    override fun createSocket(inetAddress: InetAddress, i: Int): Socket = setProtocols(delegate.createSocket(inetAddress, i))

    @Throws(IOException::class)
    override fun createSocket(inetAddress: InetAddress, i: Int, inetAddress1: InetAddress, i1: Int): Socket = setProtocols(delegate.createSocket(inetAddress, i, inetAddress1, i1))
}