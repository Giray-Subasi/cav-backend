# C.A.V demo profile seeder
# Target: local development backend at localhost:8080
# Existing profiles are never modified or deleted.

$ErrorActionPreference = 'Stop'
$apiBase = 'http://localhost:8080'

$securePassword = $null
$password = $null
$loginBody = $null
$loginResult = $null
$headers = $null

try {
    Write-Host 'C.A.V - Demo Profile Setup'
    Write-Host 'Target: localhost:8080 (development environment)'
    Write-Host ''

    $username = Read-Host '5173 development ADMIN username'
    $securePassword = Read-Host 'ADMIN password' -AsSecureString

    if ([string]::IsNullOrWhiteSpace($username)) {
        throw 'Username cannot be empty.'
    }

    $password = [System.Net.NetworkCredential]::new(
        '',
        $securePassword
    ).Password

    $loginBody = @{
        username = $username
        password = $password
    } | ConvertTo-Json -Compress

    try {
        $loginResult = Invoke-RestMethod `
            -Uri "$apiBase/auth/login" `
            -Method Post `
            -ContentType 'application/json' `
            -Body $loginBody `
            -ErrorAction Stop
    }
    catch {
        $status = if ($_.Exception.Response) {
            [int]$_.Exception.Response.StatusCode
        }
        else {
            'connection error'
        }

        throw "Login failed: $status"
    }

    if (-not $loginResult.token) {
        throw 'Login response did not contain a token.'
    }

    $headers = @{
        Authorization = "Bearer $($loginResult.token)"
    }

    # Do not retain the plaintext password after login.
    Remove-Variable password, loginBody -ErrorAction SilentlyContinue

    $operators = @(
        'TURKCELL',
        'VODAFONE',
        'TURK_TELEKOM',
        'VODAFONE',
        'TURKCELL',
        'TURK_TELEKOM',
        'VODAFONE',
        'TURKCELL',
        'TURK_TELEKOM',
        'VODAFONE',
        'TURKCELL',
        'TURK_TELEKOM',
        'VODAFONE',
        'TURKCELL'
    )

    $created = 0
    $skipped = 0

    for ($i = 1; $i -le 14; $i++) {
        $suffix = '{0:D2}' -f $i

        $iccid = "894900000000000000$suffix"
        $eid = "890490320000000000000000000000$suffix"
        $operator = $operators[$i - 1]

        # Check whether this ICCID already exists.
        try {
            $existing = Invoke-RestMethod `
                -Uri "$apiBase/profiles/$iccid" `
                -Method Get `
                -Headers $headers `
                -ErrorAction Stop

            if (
                $existing.eid -ne $eid -or
                $existing.operator -ne $operator
            ) {
                Write-Host "SKIP $suffix - ICCID exists with different data."
            }
            else {
                Write-Host "SKIP $suffix - Already exists."
            }

            $skipped++
            continue
        }
        catch {
            $status = if ($_.Exception.Response) {
                [int]$_.Exception.Response.StatusCode
            }
            else {
                0
            }

            # Only HTTP 404 means the profile is missing.
            if ($status -ne 404) {
                throw "Could not check profile $suffix. HTTP status: $status"
            }
        }

        $profileBody = @{
            iccid = $iccid
            eid = $eid
            operator = $operator
        } | ConvertTo-Json -Compress

        try {
            $null = Invoke-RestMethod `
                -Uri "$apiBase/profiles" `
                -Method Post `
                -Headers $headers `
                -ContentType 'application/json' `
                -Body $profileBody `
                -ErrorAction Stop

            Write-Host "CREATED $suffix - $operator"
            $created++
        }
        catch {
            $status = if ($_.Exception.Response) {
                [int]$_.Exception.Response.StatusCode
            }
            else {
                0
            }

            throw "Could not create profile $suffix. HTTP status: $status"
        }
    }

    Write-Host ''
    Write-Host "Finished. Created: $created | Skipped: $skipped"
}
catch {
    Write-Host "Setup stopped: $($_.Exception.Message)"
}
finally {
    Remove-Variable `
        securePassword, password, loginBody, loginResult, headers, profileBody `
        -ErrorAction SilentlyContinue
}