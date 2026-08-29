$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
$certDir = Join-Path $root 'backend\certs'
$rawDir = Join-Path $root 'android\app\src\main\res\raw'
New-Item -ItemType Directory -Force -Path $certDir | Out-Null
New-Item -ItemType Directory -Force -Path $rawDir | Out-Null

$ip = (Get-NetIPAddress -AddressFamily IPv4 | Where-Object { $_.IPAddress -notlike '127.*' -and $_.PrefixOrigin -ne 'WellKnown' } | Select-Object -First 1 -ExpandProperty IPAddress)
if (-not $ip) { throw 'Could not determine laptop IPv4 address.' }

$caKey = Join-Path $certDir 'ca.key'
$caCrt = Join-Path $certDir 'ca.crt'
$serverKey = Join-Path $certDir 'server.key'
$serverCsr = Join-Path $certDir 'server.csr'
$serverCrt = Join-Path $certDir 'server.crt'
$ext = Join-Path $certDir 'server.ext'

if (-not (Test-Path $caKey)) {
    openssl genrsa -out $caKey 2048
    openssl req -x509 -new -nodes -key $caKey -sha256 -days 825 -subj '/CN=AgentGuard Local CA' -out $caCrt
}

@"
subjectAltName=IP:$ip,IP:127.0.0.1
extendedKeyUsage=serverAuth
"@ | Set-Content -NoNewline $ext

openssl genrsa -out $serverKey 2048
openssl req -new -key $serverKey -subj "/CN=$ip" -out $serverCsr
openssl x509 -req -in $serverCsr -CA $caCrt -CAkey $caKey -CAcreateserial -out $serverCrt -days 825 -sha256 -extfile $ext

Copy-Item $caCrt (Join-Path $rawDir 'agentguard_ca.pem') -Force
Remove-Item $serverCsr,$ext -Force -ErrorAction SilentlyContinue
Write-Host "TLS ready for https://$ip`:3000"
Write-Host "CA copied to Android resources. Keep backend\certs\ca.key private."
