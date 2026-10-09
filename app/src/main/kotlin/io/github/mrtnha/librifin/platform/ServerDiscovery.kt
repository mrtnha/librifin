package io.github.mrtnha.librifin.platform

import android.content.Context
import android.net.wifi.WifiManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketTimeoutException

/**
 * Jellyfin UDP server discovery (https://jellyfin.org/docs/general/networking/).
 * [replies] broadcasts [MESSAGE] to [PORT] every [RESEND_INTERVAL_MS] (UDP is unreliable and servers may
 * come online later) and emits the raw text of every reply, until the collecting coroutine is cancelled.
 */
class ServerDiscovery(private val context: Context) {

    fun replies(): Flow<String> = flow {
        val wifi = context.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        // To save power, many devices drop incoming broadcast and multicast packets unless an app holds this lock.
        val lock = wifi?.createMulticastLock("librifin-discovery")?.apply {
            setReferenceCounted(false)
            acquire()
        }
        val socket = DatagramSocket().apply {
            broadcast = true
            soTimeout = RECEIVE_TIMEOUT_MS // lets us check for cancellation and resend regularly
        }
        try {
            val message = MESSAGE.encodeToByteArray()
            val buffer = ByteArray(4096)
            var nextSendAt = 0L
            while (true) {
                currentCoroutineContext().ensureActive()
                val now = System.currentTimeMillis()
                if (now >= nextSendAt) {
                    for (address in broadcastAddresses()) {
                        try {
                            socket.send(DatagramPacket(message, message.size, address, PORT))
                        } catch (_: IOException) {
                            // Interface went away or isn't routable; try the others.
                        }
                    }
                    nextSendAt = now + RESEND_INTERVAL_MS
                }
                val packet = DatagramPacket(buffer, buffer.size)
                try {
                    socket.receive(packet)
                } catch (_: SocketTimeoutException) {
                    continue
                }
                emit(String(packet.data, packet.offset, packet.length, Charsets.UTF_8))
            }
        } finally {
            socket.close()
            lock?.release()
        }
    }.flowOn(Dispatchers.IO)

    /** The global broadcast address plus the broadcast address of every active IPv4 interface. */
    private fun broadcastAddresses(): Set<InetAddress> {
        val result = linkedSetOf(InetAddress.getByName("255.255.255.255"))
        try {
            NetworkInterface.getNetworkInterfaces()?.toList().orEmpty()
                .filter { it.isUp && !it.isLoopback }
                .flatMap { it.interfaceAddresses }
                .mapNotNullTo(result) { it.broadcast }
        } catch (_: IOException) {
            // Fall back to the global broadcast address only.
        }
        return result
    }

    private companion object {
        const val MESSAGE = "Who is JellyfinServer?"
        const val PORT = 7359
        const val RESEND_INTERVAL_MS = 1_500L
        const val RECEIVE_TIMEOUT_MS = 250
    }
}
