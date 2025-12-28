# release.ps1

# Recuperation des variables d'environnement GitLab
$Tag = $env:CI_COMMIT_TAG
$ProjectId = $env:CI_PROJECT_ID
$ProjectUrl = $env:CI_PROJECT_URL
$ApiUrl = $env:CI_API_V4_URL
$JobToken = $env:CI_JOB_TOKEN

Write-Host "Demarrage du script de release pour $Tag"

# --- 1. Extraction du Changelog ---
Write-Host "Extraction des notes de version..."

# CORRECTION ICI : On cherche 'CHANGELOG' sans extension .md
if (Test-Path "CHANGELOG.md") {
    $Content = Get-Content "CHANGELOG.md" -Raw
} elseif (Test-Path "CHANGELOG") {
    $Content = Get-Content "CHANGELOG" -Raw
} else {
    Write-Error "Fichier CHANGELOG introuvable."
    exit 1
}

# Regex pour capturer le texte entre le tag actuel et le suivant
$Pattern = '(?ms)^##\s+' + [regex]::Escape($Tag) + '[^\r\n]*[\r\n]+([\s\S]*?)(?=^##\s+|\Z)'

if ($Content -match $Pattern) {
    $Description = $Matches[0].Trim()
    Write-Host "Notes extraites avec succes."
} else {
    $Description = "Voir le fichier CHANGELOG pour les details."
    Write-Warning "Notes de version non trouvees pour $Tag. Utilisation de la description par defaut."
}

# --- 2. Creation du Zip Windows ---
Write-Host "Compression de la version Windows..."
$ZipName = "JeuWorms-Windows.zip"
Compress-Archive -Path "build/jpackage/JeuWorms" -DestinationPath $ZipName -Force

# --- 3. Identification du Jar ---
$JarFileObject = Get-ChildItem "build/libs/*.jar" | Select-Object -First 1
if (-not $JarFileObject) {
    Write-Error "Aucun fichier .jar trouve dans build/libs/"
    exit 1
}
$JarName = $JarFileObject.Name
Write-Host "Fichier JAR trouve : $JarName"

# --- 4. Construction des Liens d'Assets ---
# On utilise l'URL raw des artefacts
$BaseUrl = "$ProjectUrl/-/jobs/artifacts/$Tag/raw"

$Assets = @{
    links = @(
        @{
            name = "Jeu Windows (Zip)"
            url  = "$BaseUrl/$ZipName`?job=release-job"
            filepath = "/$ZipName"
        },
        @{
            name = "Jeu Java (Jar)"
            url  = "$BaseUrl/build/libs/$JarName`?job=build-job"
        }
    )
}

# --- 5. Creation de la Release via API GitLab ---
Write-Host "Envoi a l'API GitLab..."

$Body = @{
    name = "Version $Tag"
    tag_name = $Tag
    description = $Description
    assets = $Assets
}

# Conversion en JSON
$JsonPayload = $Body | ConvertTo-Json -Depth 10

# Appel REST
try {
    $Response = Invoke-RestMethod -Uri "$ApiUrl/projects/$ProjectId/releases" `
        -Method Post `
        -Headers @{ "JOB-TOKEN" = $JobToken } `
        -ContentType "application/json" `
        -Body $JsonPayload
    
    Write-Host "Release creee avec succes : $($Response._links.self)"
} catch {
    Write-Error "Erreur lors de la creation de la release : $_"
    try { Write-Host $_.Exception.Response.GetResponseStream() } catch {}
    exit 1
}