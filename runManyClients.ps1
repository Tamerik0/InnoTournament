param(
    [Parameter(Mandatory=$true)]
    [int]$NumberOfFabricClients,
    [int]$NumberOfNeoforgeClients = 0,
    [string]$GradleProjectPath = (Get-Location).Path,
    [string]$BaseUsername = "Xyecoc",
    [string]$GradlewCommand = "./gradlew"
)

function Start-GradleClient {
    param(
        [string]$Path,
        [string]$Username,
        [string]$GradleCommand,
        [string]$Task
    )

    $env:ORG_GRADLE_PROJECT_username = $Username
    $arguments = @("-Dorg.gradle.java.home=C:\Users\Tamerlan\.jdks\jbrsdk_jcef-21.0.7", "-Dusername=$Username", "$task")
    Write-Host "Launching client with username '$Username' in $Path..."
    try {
        $process = Start-Process -FilePath $GradleCommand -ArgumentList $arguments -WorkingDirectory $Path -PassThru
        if ($process) {
            Write-Host "Client '$Username' launched. PID: $($process.Id)"
            return $process.Id
        } else {
            Write-Warning "Failed to launch process for client '$Username'."
            return $null
        }
    }
    catch {
        Write-Error "Error launching client '$Username': $($_.Exception.Message)"
        return $null
    }
}

$runningPids = @()
Write-Host "Launching $NumberOfFabricClients Minecraft Fabric clients..."
$task = ":fabric:runClient"
for ($i = 0; $i -lt $NumberOfFabricClients; $i++) {
    $currentUsername = "$($BaseUsername)$i"
    $id = Start-GradleClient -Path $GradleProjectPath -Username $currentUsername -GradleCommand $GradlewCommand -Task $task
    if ($id) {
        $runningPids += $id
    }
}

Write-Host "Launching $NumberOfNeoforgeClients Minecraft Neoforge clients..."
$task = ":neoforge:runClient"
for ($i = $NumberOfFabricClients; $i -lt $NumberOfFabricClients + $NumberOfNeoforgeClients; $i++) {
    $currentUsername = "$($BaseUsername)$i"
    $id = Start-GradleClient -Path $GradleProjectPath -Username $currentUsername -GradleCommand $GradlewCommand -Task $task
    if ($id) {
        $runningPids += $id
    }
}


Write-Host ""
Write-Host "All $($NumberOfClients + $NumberOfNeoforgeClients) clients have been launched."
Write-Host "PIDs of launched processes: $($runningPids -join ', ')"
Write-Host "You can close these processes manually or via Task Manager (or 'kill' command on Linux/macOS)."
Write-Host "Script finished."