param(
    [string]$IpAddress
)

$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
$certDir = Join-Path $root 'backend\certs'
$rawDir = Join-Path $root 'android\app\src\main\res\raw'

New-Item -ItemType Directory -Force -Path $certDir | Out-Null
New-Item -ItemType Directory -Force -Path $rawDir | Out-Null

if (-not $IpAddress) {
    $IpAddress = (
        Get-NetIPAddress -AddressFamily IPv4 |
        Where-Object {
            $_.IPAddress -notlike '127.*' -and
            $_.PrefixOrigin -ne 'WellKnown' -and
            $_.InterfaceAlias -notlike '*WSL*' -and
            $_.InterfaceAlias -notlike '*Loopback*'
        } |
        Select-Object -First 1 -ExpandProperty IPAddress
    )
}

if (-not $IpAddress) {
    throw 'Could not determine laptop IPv4 address. Pass -IpAddress explicitly.'
}

$caKey = Join-Path $certDir 'ca.key'
$caCrt = Join-Path $certDir 'ca.crt'
$serverKey = Join-Path $certDir 'server.key'
$serverCsr = Join-Path $certDir 'server.csr'
$serverCrt = Join-Path $certDir 'server.crt'
$caExt = Join-Path $certDir 'ca.ext'
$serverExt = Join-Path $certDir 'server.ext'

# Generate a fresh development CA.
openssl genrsa -out $caKey 2048

@"
basicConstraints=critical,CA:TRUE
keyUsage=critical,keyCertSign,cRLSign
subjectKeyIdentifier=hash
authorityKeyIdentifier=keyid:always
"@ | Set-Content $caExt

# Create self-signed CA with proper CA extensions.
openssl req -new -key $caKey `
    -subj '/CN=AgentGuard Local CA' `
    -out "$certDir\ca.csr"

openssl x509 -req `
    -in "$certDir\ca.csr" `
    -signkey $caKey `
    -out $caCrt `
    -days 825 `
    -sha256 `
    -extfile $caExt

@"
subjectAltName=IP:$IpAddress,IP:127.0.0.1
extendedKeyUsage=serverAuth
keyUsage=digitalSignature,keyEncipherment
subjectKeyIdentifier=hash
authorityKeyIdentifier=keyid,issuer
"@ | Set-Content $serverExt

openssl genrsa -out $serverKey 2048

openssl req -new `
    -key $serverKey `
    -subj "/CN=$IpAddress" `
    -out $serverCsr

openssl x509 -req `
    -in $serverCsr `
    -CA $caCrt `
    -CAkey $caKey `
    -CAcreateserial `
    -out $serverCrt `
    -days 825 `
    -sha256 `
    -extfile $serverExt

Copy-Item $caCrt (Join-Path $rawDir 'agentguard_ca.pem') -Force

Remove-Item `
    "$certDir\ca.csr",
    "$certDir\ca.ext",
    "$certDir\server.csr",
    "$certDir\server.ext",
    "$certDir\ca.srl" `
    -Force `
    -ErrorAction SilentlyContinue

Write-Host "TLS ready for https://$IpAddress`:3000"
Write-Host "CA copied to Android resources. Keep backend\certs\ca.key private."
