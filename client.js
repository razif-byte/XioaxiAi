/**
 * Xiaoxi Portable AI - Companion Client Script for USB Flashdrive (Windows)
 * 
 * CARA PENGGUNAAN:
 * 1. Simpan fail ini ('client.js') ke dalam USB flashdrive anda bersama Node.js portable.
 * 2. Lihat IP Server yang terpapar di atas skrin aplikasi Android "Xiaoxi AI" (contoh: http://192.168.1.15:8080).
 * 3. Jalankan skrip ini dari terminal USB anda:
 *    node client.js --server 192.168.1.15
 * 
 * Skrip ini akan mendaftarkan komputer Windows anda ke peranti Android,
 * dan bersedia menerima arahan suara seperti "buka VLC" atau "buka WhatsApp" untuk diluncurkan secara automatik!
 */

const http = require('http');
const { exec } = require('child_process');
const os = require('os');

// Parse command line arguments for server IP
const args = process.argv.slice(2);
const serverIndex = args.indexOf('--server');
const SERVER_IP = (serverIndex !== -1 && args[serverIndex + 1]) ? args[serverIndex + 1] : '192.168.1.15'; // Ganti dengan IP Android anda
const SERVER_PORT = 8080;
const CLIENT_PORT = 8080;

const clientIp = getLocalIpAddress();
const pcName = os.hostname();

console.log(`====================================================`);
console.log(`🤖 XIAOXI PORTABLE CLIENT (WINDOWS CONSOLE) RUNNING`);
console.log(`====================================================`);
console.log(`📍 IP Tempatan Windows: ${clientIp}`);
console.log(`🔗 Menyambung ke Xiaoxi Server di: http://${SERVER_IP}:${SERVER_PORT}`);

// Register client to Android Server
function registerToAndroid() {
    const payload = JSON.stringify({
        name: pcName,
        type: 'Windows'
    });

    const options = {
        hostname: SERVER_IP,
        port: SERVER_PORT,
        path: '/register',
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Content-Length': ByteLength(payload)
        },
        timeout: 4000
    };

    const req = http.request(options, (res) => {
        let data = '';
        res.on('data', (chunk) => data += chunk);
        res.on('end', () => {
            if (res.statusCode === 200) {
                console.log(`✅ Terhubung dengan sukses! Xiaoxi AI telah mendaftarkan ${pcName}.`);
                startHeartbeat();
            } else {
                console.log(`❌ Pendaftaran ditolak oleh server: ${data}`);
            }
        });
    });

    req.on('error', (err) => {
        console.log(`⚠️ Gagal menyambung ke Android Xiaoxi Server (${err.message}). Cuba lagi dalam 5 saat...`);
        setTimeout(registerToAndroid, 5000);
    });

    req.write(payload);
    req.end();
}

// Start periodic ping/heartbeat (every 10 seconds)
function startHeartbeat() {
    setInterval(() => {
        const options = {
            hostname: SERVER_IP,
            port: SERVER_PORT,
            path: '/ping',
            method: 'POST',
            timeout: 3000
        };

        const req = http.request(options, (res) => {
            res.on('data', () => {});
        });
        req.on('error', (err) => {
            console.log(`⚠️ Android Server Offline atau tidak bertindak balas.`);
        });
        req.end();
    }, 10000);
}

// Start Local Server on Windows to listen to Broadcasted Action Commands from Android
const server = http.createServer((req, res) => {
    if (req.url === '/command' && req.method === 'POST') {
        let body = '';
        req.on('data', chunk => body += chunk);
        req.on('end', () => {
            try {
                const payload = JSON.parse(body);
                const action = payload.action;
                const command = payload.command;

                console.log(`🎤 Suara dikesan di Android: "${command}" [Aksi: ${action}]`);

                if (action === 'LAUNCH_VLC') {
                    console.log(`🎬 Menjalankan VLC Media Player di Windows...`);
                    // Command to open VLC on Windows
                    exec('start vlc', (err) => {
                        if (err) exec('\"C:\\Program Files\\VideoLAN\\VLC\\vlc.exe\"', (e2) => {
                            if (e2) console.log(`❌ Gagal membuka VLC: Sila pastikan VLC dipasang atau berada dalam PATH.`);
                        });
                    });
                } else if (action === 'LAUNCH_WHATSAPP') {
                    console.log(`💬 Membuka WhatsApp Web/Desktop di Windows...`);
                    exec('start whatsapp://', (err) => {
                        if (err) exec('start https://web.whatsapp.com', (e2) => {
                            if (e2) console.log(`❌ Gagal membuka WhatsApp.`);
                        });
                    });
                } else {
                    console.log(`❓ Aksi "${action}" tidak disokong secara lokal oleh PC.`);
                }

                res.writeHead(200, { 'Content-Type': 'application/json' });
                res.end(JSON.stringify({ status: 'executed' }));
            } catch (err) {
                res.writeHead(400);
                res.end(`Bad Request: ${err.message}`);
            }
        });
    } else {
        res.writeHead(404);
        res.end();
    }
});

server.listen(CLIENT_PORT, () => {
    console.log(`🎧 Windows Client sedia mendengar arahan di port ${CLIENT_PORT}...`);
    registerToAndroid();
});

// Helper functions
function getLocalIpAddress() {
    const interfaces = os.networkInterfaces();
    for (const devName in interfaces) {
        const iface = interfaces[devName];
        for (let i = 0; i < iface.length; i++) {
            const alias = iface[i];
            if (alias.family === 'IPv4' && alias.address !== '127.0.0.1' && !alias.internal) {
                return alias.address;
            }
        }
    }
    return '127.0.0.1';
}

function ByteLength(str) {
    return Buffer.byteLength(str, 'utf8');
}
