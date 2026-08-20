package com.example.util

import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.ServerSocket
import java.net.Socket
import java.net.URL
import java.util.concurrent.Executors

data class NetworkDevice(
    val ip: String,
    val name: String,
    val deviceType: String, // "Windows", "Android", "Unknown"
    val isOnline: Boolean,
    val lastSeen: Long
)

class WifiControllerServer(
    private val port: Int = 8080,
    private val onCommandReceivedFromClient: (String) -> Unit
) {

    private var serverSocket: ServerSocket? = null
    private val scope = CoroutineScope(Dispatchers.IO)
    private val executor = Executors.newCachedThreadPool()

    // Reactive list of discovered/registered wifi devices
    private val _devices = MutableStateFlow<List<NetworkDevice>>(emptyList())
    val devices = _devices.asStateFlow()

    private val _serverIpAddress = MutableStateFlow("127.0.0.1")
    val serverIpAddress = _serverIpAddress.asStateFlow()

    private val _isServerRunning = MutableStateFlow(false)
    val isServerRunning = _isServerRunning.asStateFlow()

    companion object {
        private const val TAG = "WifiControllerServer"
    }

    fun startServer(localIp: String) {
        if (_isServerRunning.value) return
        _serverIpAddress.value = localIp

        try {
            serverSocket = ServerSocket(port)
            _isServerRunning.value = true
            Log.d(TAG, "Server started on http://$localIp:$port")

            // Start socket accepting thread
            scope.launch(Dispatchers.IO) {
                while (_isServerRunning.value) {
                    try {
                        val clientSocket = serverSocket?.accept() ?: break
                        executor.execute {
                            handleClientSocket(clientSocket)
                        }
                    } catch (e: Exception) {
                        if (_isServerRunning.value) {
                            Log.e(TAG, "Error accepting client connection: ${e.message}")
                        }
                    }
                }
            }

            // Periodically check for offline devices (every 10 seconds)
            scope.launch {
                while (_isServerRunning.value) {
                    kotlinx.coroutines.delay(10000)
                    pruneOfflineDevices()
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Failed to start server: ${e.message}")
        }
    }

    fun stopServer() {
        try {
            _isServerRunning.value = false
            serverSocket?.close()
            serverSocket = null
            Log.d(TAG, "Server stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop server: ${e.message}")
        }
    }

    private fun handleClientSocket(socket: Socket) {
        try {
            val reader = BufferedReader(InputStreamReader(socket.getInputStream(), Charsets.UTF_8))
            val outputStream = socket.getOutputStream()

            // Read request line
            val requestLine = reader.readLine() ?: return
            val parts = requestLine.split(" ")
            if (parts.size < 2) {
                sendErrorResponse(outputStream, 400, "Bad Request")
                socket.close()
                return
            }

            val method = parts[0]
            val path = parts[1]

            // Read headers to find content-length
            var contentLength = 0
            var line: String? = reader.readLine()
            while (line != null && line.isNotEmpty()) {
                if (line.lowercase().startsWith("content-length:")) {
                    contentLength = line.substring(15).trim().toIntOrNull() ?: 0
                }
                line = reader.readLine()
            }

            // Read request body
            val body = if (contentLength > 0) {
                val buffer = CharArray(contentLength)
                var totalRead = 0
                while (totalRead < contentLength) {
                    val read = reader.read(buffer, totalRead, contentLength - totalRead)
                    if (read == -1) break
                    totalRead += read
                }
                String(buffer, 0, totalRead)
            } else {
                ""
            }

            val clientIp = socket.inetAddress?.hostAddress ?: ""

            when {
                (path == "/" || path == "/index.html") && method == "GET" -> {
                    handleServeWebDashboard(outputStream)
                }
                path == "/register" && method == "POST" -> {
                    handleRegister(body, clientIp, outputStream)
                }
                path == "/command" && method == "POST" -> {
                    handleCommand(body, outputStream)
                }
                (path == "/ping" || path == "/ping/") && (method == "POST" || method == "GET") -> {
                    handlePing(clientIp, outputStream)
                }
                else -> {
                    sendErrorResponse(outputStream, 404, "Not Found")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling client connection: ${e.message}")
        } finally {
            try {
                socket.close()
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    private fun handleRegister(body: String, clientIp: String, out: OutputStream) {
        try {
            val json = JSONObject(body)
            val name = json.optString("name", "Unknown PC")
            val type = json.optString("type", "Windows")

            registerDevice(clientIp, name, type)

            val response = JSONObject()
                .put("status", "registered")
                .put("message", "Terhubung ke Xiaoxi AI!")
                .toString()
            
            sendJsonResponse(out, 200, response)
        } catch (e: Exception) {
            sendErrorResponse(out, 400, "Error: ${e.message}")
        }
    }

    private fun handleCommand(body: String, out: OutputStream) {
        try {
            val json = JSONObject(body)
            val command = json.optString("command", "")

            Handler(Looper.getMainLooper()).post {
                onCommandReceivedFromClient(command)
            }

            val response = JSONObject()
                .put("status", "received")
                .put("message", "Arahan sedang diproses oleh Xiaoxi")
                .toString()

            sendJsonResponse(out, 200, response)
        } catch (e: Exception) {
            sendErrorResponse(out, 400, "Error: ${e.message}")
        }
    }

    private fun handlePing(clientIp: String, out: OutputStream) {
        try {
            val currentList = _devices.value
            val existing = currentList.find { it.ip == clientIp }
            if (existing != null) {
                registerDevice(clientIp, existing.name, existing.deviceType)
            }

            val response = JSONObject()
                .put("status", "pong")
                .put("timestamp", System.currentTimeMillis())
                .toString()

            sendJsonResponse(out, 200, response)
        } catch (e: Exception) {
            sendErrorResponse(out, 400, "Error: ${e.message}")
        }
    }

    private fun sendJsonResponse(out: OutputStream, statusCode: Int, json: String) {
        val statusMessage = when (statusCode) {
            200 -> "OK"
            else -> "OK"
        }
        val responseBytes = json.toByteArray(Charsets.UTF_8)
        val responseHeader = "HTTP/1.1 $statusCode $statusMessage\r\n" +
                "Content-Type: application/json; charset=utf-8\r\n" +
                "Content-Length: ${responseBytes.size}\r\n" +
                "Connection: close\r\n\r\n"
        out.write(responseHeader.toByteArray(Charsets.UTF_8))
        out.write(responseBytes)
        out.flush()
    }

    private fun sendErrorResponse(out: OutputStream, statusCode: Int, message: String) {
        val statusMessage = when (statusCode) {
            400 -> "Bad Request"
            404 -> "Not Found"
            405 -> "Method Not Allowed"
            else -> "Internal Server Error"
        }
        val responseBytes = message.toByteArray(Charsets.UTF_8)
        val responseHeader = "HTTP/1.1 $statusCode $statusMessage\r\n" +
                "Content-Type: text/plain; charset=utf-8\r\n" +
                "Content-Length: ${responseBytes.size}\r\n" +
                "Connection: close\r\n\r\n"
        out.write(responseHeader.toByteArray(Charsets.UTF_8))
        out.write(responseBytes)
        out.flush()
    }

    private fun handleServeWebDashboard(out: OutputStream) {
        val html = """
<!DOCTYPE html>
<html lang="ms">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Xiaoxi AI - Portal Pengawasan WiFi</title>
    <link href="https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@400;500;700&family=JetBrains+Mono:wght@400;700&display=swap" rel="stylesheet">
    <script src="https://cdn.tailwindcss.com"></script>
    <script>
        tailwind.config = {
            theme: {
                extend: {
                    fontFamily: {
                        sans: ['Space Grotesk', 'sans-serif'],
                        mono: ['JetBrains Mono', 'monospace'],
                    }
                }
            }
        }
    </script>
    <style>
        body {
            background-color: #0f172a;
            color: #f1f5f9;
        }
        .glow {
            box-shadow: 0 0 15px rgba(99, 102, 241, 0.3);
        }
    </style>
</head>
<body class="min-h-screen p-4 md:p-8 flex flex-col justify-between">
    <div class="max-w-4xl mx-auto w-full space-y-6">
        <!-- Header -->
        <header class="flex flex-col md:flex-row items-center justify-between bg-slate-800/50 p-6 rounded-2xl border border-slate-700/50 glow">
            <div class="flex items-center space-x-4">
                <div class="w-12 h-12 rounded-xl bg-indigo-600 flex items-center justify-center font-bold text-xl text-white">
                    肖
                </div>
                <div>
                    <h1 class="text-2xl font-bold tracking-tight text-white">Xiaoxi AI Portal</h1>
                    <p class="text-xs text-indigo-400">Asisten Pintar Bahasa Melayu & Kawalan WiFi</p>
                </div>
            </div>
            <div class="mt-4 md:mt-0 flex items-center space-x-2 bg-slate-900/80 px-4 py-2 rounded-lg border border-slate-800">
                <span class="w-2.5 h-2.5 rounded-full bg-emerald-500 animate-pulse"></span>
                <span class="text-xs font-mono text-emerald-400">SERVER AKTIF (PORT 8080)</span>
            </div>
        </header>

        <!-- Main Grid -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <!-- Left Panel: Remote Terminal Command -->
            <section class="bg-slate-800/30 p-6 rounded-2xl border border-slate-700/30 flex flex-col justify-between space-y-4">
                <div>
                    <h2 class="text-lg font-bold text-white mb-2">Hantar Arahan Suara (Teks)</h2>
                    <p class="text-sm text-slate-400">Hantar arahan terus kepada pembantu suara Xiaoxi di telefon anda.</p>
                </div>
                
                <div class="space-y-3">
                    <input type="text" id="cmdInput" placeholder="Sebut sesuatu... (cth: buka vlc, buka whatsapp)" class="w-full bg-slate-900/90 border border-slate-700 rounded-xl px-4 py-3 text-slate-200 placeholder-slate-500 focus:outline-none focus:border-indigo-500 font-sans transition-all">
                    <button onclick="sendCommand()" class="w-full bg-indigo-600 hover:bg-indigo-500 text-white font-bold py-3 px-4 rounded-xl transition-all shadow-lg shadow-indigo-600/20 active:scale-[0.98]">
                        Kirim Arahan
                    </button>
                </div>

                <div id="responseBox" class="hidden mt-4 p-4 rounded-xl bg-slate-900/60 border border-slate-800 text-sm font-mono text-slate-300">
                    <span class="text-indigo-400">Sistem:</span> <span id="responseText">...</span>
                </div>
            </section>

            <!-- Right Panel: Remote Device Registration -->
            <section class="bg-slate-800/30 p-6 rounded-2xl border border-slate-700/30 flex flex-col justify-between space-y-4">
                <div>
                    <h2 class="text-lg font-bold text-white mb-2">Daftar Komputer Ini</h2>
                    <p class="text-sm text-slate-400">Sertakan komputer Windows ini sebagai peranti kawalan dalam sistem Xiaoxi.</p>
                </div>

                <div class="space-y-3">
                    <div class="grid grid-cols-2 gap-2">
                        <input type="text" id="devName" placeholder="Nama Komputer" class="bg-slate-900/90 border border-slate-700 rounded-xl px-3 py-2 text-slate-200 placeholder-slate-500 focus:outline-none focus:border-indigo-500 text-sm">
                        <select id="devType" class="bg-slate-900/90 border border-slate-700 rounded-xl px-3 py-2 text-slate-200 focus:outline-none focus:border-indigo-500 text-sm">
                            <option value="Windows">Windows</option>
                            <option value="Android">Android</option>
                            <option value="Linux">Linux</option>
                        </select>
                    </div>
                    <button onclick="registerBrowser()" class="w-full bg-emerald-600 hover:bg-emerald-500 text-white font-bold py-3 px-4 rounded-xl transition-all shadow-lg shadow-emerald-600/20 active:scale-[0.98]">
                        Hubungkan Komputer Ini
                    </button>
                </div>

                <div id="regBox" class="hidden mt-4 p-4 rounded-xl bg-slate-900/60 border border-slate-800 text-sm font-mono text-emerald-400">
                    <span id="regText">...</span>
                </div>
            </section>
        </div>

        <!-- Documentation & Information -->
        <section class="bg-slate-800/20 p-6 rounded-2xl border border-slate-700/20">
            <h2 class="text-lg font-bold text-white mb-4">Panduan Integrasi Xiaoxi</h2>
            <div class="space-y-4 text-sm text-slate-300">
                <div class="p-4 bg-slate-900/40 rounded-xl border border-slate-800/50">
                    <h3 class="font-bold text-indigo-400 mb-1">Hubungan Automatik Windows</h3>
                    <p class="text-xs text-slate-400 leading-relaxed font-sans">Telefon Android anda bertindak sebagai hub pusat. Semua komputer di bawah rangkaian WiFi yang sama boleh menghantar dan menerima arahan dari telefon melalui skrip permulaan auto.</p>
                </div>
                <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div class="p-4 bg-slate-900/40 rounded-xl border border-slate-800/50">
                        <h4 class="font-semibold text-slate-200 mb-1">Arahan Tempatan Tersedia</h4>
                        <ul class="list-disc pl-5 space-y-1 text-xs text-slate-400">
                            <li><strong class="text-slate-300">buka vlc</strong> - Lancarkan VLC Player</li>
                            <li><strong class="text-slate-300">buka whatsapp</strong> - Akses permesejan</li>
                            <li><strong class="text-slate-300">buka kamera</strong> - Aktifkan kamera Android</li>
                            <li><strong class="text-slate-300">mainkan lagu</strong> - Putar muzik YouTube</li>
                            <li><strong class="text-slate-300">menyanyi</strong> - Xiaoxi menyanyikan lagu</li>
                        </ul>
                    </div>
                    <div class="p-4 bg-slate-900/40 rounded-xl border border-slate-800/50">
                        <h4 class="font-semibold text-slate-200 mb-1">Kecerdasan Buatan (AI) Fallback</h4>
                        <p class="text-xs text-slate-400">Sebarang arahan selain di atas akan diproses secara dinamik oleh model Gemini 3.5 Flash untuk jawapan mesra suara.</p>
                    </div>
                </div>
            </div>
        </section>
    </div>

    <!-- Footer -->
    <footer class="text-center text-xs text-slate-600 mt-8">
        Xiaoxi Mobile Voice Assistant Hub © 2026. Hak Cipta Terpelihara.
    </footer>

    <script>
        function sendCommand() {
            const cmd = document.getElementById('cmdInput').value.trim();
            if(!cmd) return;
            
            const responseBox = document.getElementById('responseBox');
            const responseText = document.getElementById('responseText');
            responseBox.classList.remove('hidden');
            responseText.innerText = "Mengirim...";

            fetch('/command', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ command: cmd })
            })
            .then(function(res) { return res.json(); })
            .then(function(data) {
                responseText.innerText = data.message || "Arahan diterima!";
            })
            .catch(function(err) {
                responseText.innerText = "Ralat semasa menghubungi pelayan.";
            });
        }

        function registerBrowser() {
            const name = document.getElementById('devName').value.trim() || "Web Companion Client";
            const type = document.getElementById('devType').value;
            
            const regBox = document.getElementById('regBox');
            const regText = document.getElementById('regText');
            regBox.classList.remove('hidden');
            regText.innerText = "Mendaftarkan...";

            fetch('/register', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ name: name, type: type })
            })
            .then(function(res) { return res.json(); })
            .then(function(data) {
                regText.innerText = "Sukses: " + data.message;
            })
            .catch(function(err) {
                regText.innerText = "Gagal menyambung. Sila pastikan pelayan aktif.";
            });
        }
    </script>
</body>
</html>
        """.trimIndent()
        
        val responseBytes = html.toByteArray(Charsets.UTF_8)
        val responseHeader = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: text/html; charset=utf-8\r\n" +
                "Content-Length: " + responseBytes.size + "\r\n" +
                "Connection: close\r\n\r\n"
        out.write(responseHeader.toByteArray(Charsets.UTF_8))
        out.write(responseBytes)
        out.flush()
    }

    /**
     * Broadcast an execution trigger to all registered clients.
     */
    fun broadcastCommand(command: String, parsedAction: String) {
        val currentDevices = _devices.value
        Log.d(TAG, "Broadcasting command '$command' / '$parsedAction' to ${currentDevices.size} devices")
        
        for (device in currentDevices) {
            if (device.isOnline) {
                scope.launch {
                    sendHttpRequestToDevice(device.ip, "command", mapOf(
                        "command" to command,
                        "action" to parsedAction
                    ))
                }
            }
        }
    }

    /**
     * Send specific action directly to a targeted device IP
     */
    fun triggerActionOnDevice(deviceIp: String, action: String) {
        scope.launch {
            sendHttpRequestToDevice(deviceIp, "command", mapOf(
                "command" to "Manual UI Action",
                "action" to action
            ))
        }
    }

    private suspend fun sendHttpRequestToDevice(
        deviceIp: String,
        endpoint: String,
        payload: Map<String, String>
    ) = withContext(Dispatchers.IO) {
        try {
            val urlSpec = "http://$deviceIp:8080/$endpoint"
            val url = URL(urlSpec)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.connectTimeout = 3000
            connection.readTimeout = 3000
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")

            val json = JSONObject(payload).toString()
            connection.outputStream.use { os ->
                val input = json.toByteArray(Charsets.UTF_8)
                os.write(input, 0, input.size)
            }

            val responseCode = connection.responseCode
            Log.d(TAG, "Sent trigger to $urlSpec, Response: $responseCode")
            connection.disconnect()
        } catch (e: Exception) {
            Log.e(TAG, "Error sending HTTP request to device $deviceIp: ${e.message}")
            // Mark device as offline on error
            markDeviceOffline(deviceIp)
        }
    }

    @Synchronized
    fun registerDevice(ip: String, name: String, type: String) {
        val list = _devices.value.toMutableList()
        val index = list.indexOfFirst { it.ip == ip }
        val newDevice = NetworkDevice(
            ip = ip,
            name = name,
            deviceType = type,
            isOnline = true,
            lastSeen = System.currentTimeMillis()
        )

        if (index >= 0) {
            list[index] = newDevice
        } else {
            list.add(newDevice)
        }
        _devices.value = list
        Log.d(TAG, "Device registered: $name ($ip) [$type]")
    }

    @Synchronized
    private fun markDeviceOffline(ip: String) {
        val list = _devices.value.toMutableList()
        val index = list.indexOfFirst { it.ip == ip }
        if (index >= 0) {
            val d = list[index]
            list[index] = d.copy(isOnline = false)
            _devices.value = list
            Log.d(TAG, "Device marked offline: ${d.name} ($ip)")
        }
    }

    @Synchronized
    private fun pruneOfflineDevices() {
        val currentTime = System.currentTimeMillis()
        val list = _devices.value.toMutableList()
        var updated = false

        for (i in list.indices) {
            val device = list[i]
            // If device hasn't checked in within 20 seconds, mark it offline
            if (device.isOnline && (currentTime - device.lastSeen > 20000)) {
                list[i] = device.copy(isOnline = false)
                updated = true
                Log.d(TAG, "Pruning device to offline: ${device.name}")
            }
        }
        if (updated) {
            _devices.value = list
        }
    }
}
