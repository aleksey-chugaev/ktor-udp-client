import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.NetworkInterface
import kotlin.text.toByteArray

fun main(args: Array<String>) {
    println("Start")

    broadcastKtor()
//    broadcastClassic()

    println("Exit")
}

fun broadcastKtor() {
    runBlocking {
        println("runBlocking")
        val selectorManager = SelectorManager(Dispatchers.IO)
        val broadcastAddress = getBroadcastAddress()
        val socketAddress = InetSocketAddress(broadcastAddress.hostName, 9002)
        val clientSocket = aSocket(selectorManager).udp().bind {
            broadcast = true
        }
        println("bind")

        val packet = BytePacketBuilder().apply {
            writeFully("test broadcastKtor".toByteArray())
        }.build()

        println("before send")
        clientSocket.send(Datagram(address = socketAddress, packet = packet))
        println("after send")

        println("receiving")
        val datagram = clientSocket.receive()
        println("received datagram from ${datagram.address}")
        val responseText = datagram.packet.readText()
        println("received text '$responseText'")

        clientSocket.close()
    }
}

fun broadcastClassic() {
    println("broadcastClassic")
    val broadcastAddress = getBroadcastAddress()
    val datagramSocket = DatagramSocket()
    val data = "test broadcastClassic".toByteArray()
    val packet = DatagramPacket(data, data.size, broadcastAddress, 9002)
    println("before send")
    datagramSocket.send(packet)
    println("after send")
    datagramSocket.close()
}

fun getBroadcastAddress(): InetAddress {
    val broadcastAddresses = mutableSetOf<InetAddress>()
    NetworkInterface.getNetworkInterfaces().toList().forEach { networkInterface ->
        if (networkInterface.isUp && !networkInterface.isLoopback) {
            broadcastAddresses.addAll(networkInterface.interfaceAddresses.mapNotNull { it.broadcast })
        }
    }
    println("broadcastAddresses: ${broadcastAddresses.size}")
    broadcastAddresses.forEach {
        println(it)
    }
    return broadcastAddresses.first()
}