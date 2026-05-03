# Install Maven 3.9.6
$ProgressPreference = 'SilentlyContinue'

Write-Host "Downloading Maven 3.9.6..."
$MavenUrl = "https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip"
$ZipFile = "$PSScriptRoot\maven.zip"
$ExtractPath = "C:\Apache"
$MavenPath = "$ExtractPath\Maven"

try {
    # Create Apache directory if it doesn't exist
    if (-not (Test-Path $ExtractPath)) {
        New-Item -ItemType Directory -Path $ExtractPath -Force | Out-Null
    }

    # Download Maven
    Invoke-WebRequest -Uri $MavenUrl -OutFile $ZipFile
    Write-Host "Maven downloaded successfully"

    # Extract
    Write-Host "Extracting Maven..."
    Expand-Archive -Path $ZipFile -DestinationPath $ExtractPath -Force
    
    # Rename folder
    $TempPath = "$ExtractPath\apache-maven-3.9.6"
    if (Test-Path $TempPath) {
        if (Test-Path $MavenPath) {
            Remove-Item $MavenPath -Recurse -Force
        }
        Move-Item $TempPath $MavenPath
    }

    # Set environment variables
    Write-Host "Setting environment variables..."
    [Environment]::SetEnvironmentVariable('MAVEN_HOME', $MavenPath, 'User')
    
    # Update PATH
    $CurrentPath = [Environment]::GetEnvironmentVariable('Path', 'User')
    if ($CurrentPath -notlike "*Maven\bin*") {
        $NewPath = "$CurrentPath;$MavenPath\bin"
        [Environment]::SetEnvironmentVariable('Path', $NewPath, 'User')
    }

    # Clean up
    Remove-Item $ZipFile -Force -ErrorAction SilentlyContinue

    Write-Host "Maven installed successfully at $MavenPath"
    Write-Host "Please restart your terminal for PATH changes to take effect"
}
catch {
    Write-Error "Failed to install Maven: $_"
    exit 1
}
