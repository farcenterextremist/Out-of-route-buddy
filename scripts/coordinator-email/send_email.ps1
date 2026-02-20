# Send email using .NET SMTP (no Python required). Uses same .env as send_email.py.
# Usage: .\send_email.ps1 -Subject "Subject" -Body "Body text"
#        .\send_email.ps1 -Subject "Subject" -BodyFile "phase_abc_completion_body.txt"
param(
    [Parameter(Mandatory = $true)][string]$Subject,
    [string]$Body,
    [string]$BodyFile
)
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$envPath = Join-Path $ScriptDir ".env"
if (-not (Test-Path $envPath)) {
    Write-Error "Missing .env. Copy .env.example to .env and set COORDINATOR_EMAIL_* and COORDINATOR_SMTP_*."
    exit 1
}
$envVars = @{}
Get-Content $envPath -Encoding UTF8 | ForEach-Object {
    $line = $_.Trim()
    if ($line -and -not $line.StartsWith("#") -and $line -match "^(.+?)=(.*)$") {
        $key = $Matches[1].Trim()
        $val = $Matches[2].Trim().Trim('"').Trim("'")
        $envVars[$key] = $val
    }
}
$to = $envVars["COORDINATOR_EMAIL_TO"]
$from = $envVars["COORDINATOR_EMAIL_FROM"]; if (-not $from) { $from = $to }
$host_smtp = $envVars["COORDINATOR_SMTP_HOST"]
$portStr = $envVars["COORDINATOR_SMTP_PORT"]; if (-not $portStr) { $portStr = "587" }; $port = [int]$portStr
$user = $envVars["COORDINATOR_SMTP_USER"]
$pass = $envVars["COORDINATOR_SMTP_PASSWORD"]
foreach ($k in @("COORDINATOR_EMAIL_TO", "COORDINATOR_SMTP_HOST", "COORDINATOR_SMTP_USER", "COORDINATOR_SMTP_PASSWORD")) {
    if (-not $envVars[$k]) {
        Write-Error "Missing in .env: $k"
        exit 1
    }
}
if ($BodyFile) {
    $bodyPath = if ([System.IO.Path]::IsPathRooted($BodyFile)) { $BodyFile } else { Join-Path $ScriptDir $BodyFile }
    $Body = [System.IO.File]::ReadAllText($bodyPath, [System.Text.Encoding]::UTF8)
}
if (-not $Body) {
    Write-Error "Provide -Body or -BodyFile"
    exit 1
}
try {
    [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
    $secpass = ConvertTo-SecureString $pass -AsPlainText -Force
    $cred = New-Object System.Management.Automation.PSCredential($user, $secpass)
    $msg = New-Object System.Net.Mail.MailMessage
    $msg.From = $from
    $msg.To.Add($to)
    $msg.Subject = $Subject
    $msg.Body = $Body
    $msg.IsBodyHtml = $false
    $smtp = New-Object System.Net.Mail.SmtpClient($host_smtp, $port)
    $smtp.EnableSSL = $true
    $smtp.Credentials = $cred
    $smtp.Send($msg)
    Write-Host "Email sent successfully."
} catch {
    Write-Error "Failed to send email: $_"
    exit 1
}
