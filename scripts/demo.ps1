param(
    [string]$BaseUrl = "http://localhost:8080",
    [switch]$PauseForCaptures
)

$ErrorActionPreference = "Stop"
$BaseUrl = $BaseUrl.TrimEnd('/')
$createdIds = [System.Collections.Generic.List[int]]::new()

function Invoke-Step {
    param([string]$Method, [string]$Path, [int]$ExpectedStatus, [string]$Body)

    $parameters = @{
        Uri = "$BaseUrl$Path"
        Method = $Method
        UseBasicParsing = $true
    }
    if ($Body) {
        $parameters.ContentType = "application/json; charset=utf-8"
        $parameters.Body = [System.Text.Encoding]::UTF8.GetBytes($Body)
    }
    try {
        $response = Invoke-WebRequest @parameters
        $code = [int]$response.StatusCode
        $content = $response.Content
    }
    catch {
        if (-not $_.Exception.Response) { throw }
        $code = [int]$_.Exception.Response.StatusCode
        $content = $_.ErrorDetails.Message
    }
    Write-Host "`n$Method $Path"
    Write-Host "$code $([System.Net.HttpStatusCode]$code)"
    if ($content -and $Path -notlike '/swagger-*') { Write-Host $content }
    if ($code -ne $ExpectedStatus) {
        throw "Statut attendu : $ExpectedStatus ; obtenu : $code"
    }
    return [pscustomobject]@{ StatusCode = $code; Content = $content }
}

function Pause-Capture([string]$Label) {
    if ($PauseForCaptures) { Read-Host "$Label - Entree pour continuer" | Out-Null }
}

function Assert-Result([bool]$Condition, [string]$Message) {
    if (-not $Condition) { throw $Message }
}

# Insertion explicite de trois exemples, puis suppression de ces seuls exemples.
# Aucune initialisation de donnees n'est executee par le backend au demarrage.
$initialCount = [long](Invoke-RestMethod "$BaseUrl/students/count")
$initialYears = @{}
foreach ($row in (Invoke-RestMethod "$BaseUrl/students/byYear")) {
    $initialYears[[int]$row[0]] = [long]$row[1]
}

try {
    foreach ($json in @(
        '{"nom":"LACHGAR","prenom":"Mohamed","dateNaissance":"1985-09-01"}',
        '{"nom":"HARIB","prenom":"Sara","dateNaissance":"2002-05-15"}',
        '{"nom":"AMRANI","prenom":"Yassine","dateNaissance":"1985-12-10"}'
    )) {
        $response = Invoke-Step POST /students/save 201 $json
        $student = $response.Content | ConvertFrom-Json
        $createdIds.Add([int]$student.id)
        Assert-Result ($student.dateNaissance -eq (($json | ConvertFrom-Json).dateNaissance)) "Date modifiee lors de la sauvegarde"
    }
    Pause-Capture "Capture 3 : POST /students/save - 201 Created"

    $response = Invoke-Step GET /students/all 200
    $all = @($response.Content | ConvertFrom-Json)
    foreach ($id in $createdIds) {
        Assert-Result ($id -in $all.id) "Etudiant $id absent de la liste"
    }
    Pause-Capture "Capture 4 : GET /students/all - 200 OK"

    $id = $createdIds[0]
    $response = Invoke-Step GET "/students/$id" 200
    Assert-Result (($response.Content | ConvertFrom-Json).id -eq $id) "Identifiant incorrect"

    $response = Invoke-Step PUT "/students/$id" 200 '{"nom":"LACHGAR","prenom":"Mohamed modifie","dateNaissance":"1985-09-01"}'
    Assert-Result (($response.Content | ConvertFrom-Json).prenom -eq "Mohamed modifie") "Modification non appliquee"
    $persisted = Invoke-RestMethod "$BaseUrl/students/$id"
    Assert-Result ($persisted.prenom -eq "Mohamed modifie") "Modification non persistee"

    $response = Invoke-Step GET /students/count 200
    Assert-Result ([long]$response.Content -eq ($initialCount + 3)) "Comptage incorrect"
    $response = Invoke-Step GET /students/byYear 200
    $years = @{}
    $previousYear = 0
    foreach ($row in ($response.Content | ConvertFrom-Json)) {
        $year = [int]$row[0]
        Assert-Result ($year -gt $previousYear) "Annees non triees"
        $years[$year] = [long]$row[1]
        $previousYear = $year
    }
    Assert-Result ($years[1985] -eq ([long]$initialYears[1985] + 2)) "Statistique 1985 incorrecte"
    Assert-Result ($years[2002] -eq ([long]$initialYears[2002] + 1)) "Statistique 2002 incorrecte"
    Pause-Capture "Capture 5 : count et byYear"

    $response = Invoke-Step POST /students/save 400 '{"nom":" ","prenom":"","dateNaissance":null}'
    $problem = $response.Content | ConvertFrom-Json
    Assert-Result ($problem.errors.nom -and $problem.errors.prenom -and $problem.errors.dateNaissance) "Details de validation absents"
    $null = Invoke-Step POST /students/save 400 '{"nom":"A","prenom":"B","dateNaissance":"1985-02-30"}'
    Assert-Result ([long](Invoke-RestMethod "$BaseUrl/students/count") -eq ($initialCount + 3)) "Une requete invalide a insere des donnees"

    $response = Invoke-Step DELETE "/students/delete/$id" 204
    Assert-Result ([string]::IsNullOrEmpty($response.Content)) "DELETE 204 doit avoir un corps vide"
    Pause-Capture "Capture 6 : DELETE - 204 No Content"
    $null = Invoke-Step GET "/students/$id" 404
    $null = Invoke-Step DELETE "/students/delete/$id" 404
    $null = Invoke-Step PUT "/students/$id" 404 '{"nom":"A","prenom":"B","dateNaissance":"1985-09-01"}'

    $null = Invoke-Step GET /swagger-ui/index.html 200
    $null = Invoke-Step GET /swagger-ui.html 200
    $doc = Invoke-RestMethod "$BaseUrl/v3/api-docs"
    Assert-Result ($doc.info.title -eq 'Student Management API') "Titre OpenAPI incorrect"
    foreach ($path in @('/students/save', '/students/all', '/students/count', '/students/byYear', '/students/delete/{id}', '/students/{id}')) {
        Assert-Result ($null -ne $doc.paths.PSObject.Properties[$path]) "Endpoint absent d'OpenAPI : $path"
    }
    Write-Host "`nGET /v3/api-docs - 200 OK"
    $doc.info | Format-List | Out-Host
    Write-Host "Capture 7 : ouvrir $BaseUrl/swagger-ui/index.html"
    Pause-Capture "Swagger UI : groupe StudentController"
}
finally {
    foreach ($id in $createdIds) {
        try {
            Invoke-WebRequest -UseBasicParsing -Method Delete "$BaseUrl/students/delete/$id" | Out-Null
        }
        catch {
            if (-not $_.Exception.Response -or [int]$_.Exception.Response.StatusCode -ne 404) { throw }
        }
    }
}

Assert-Result ([long](Invoke-RestMethod "$BaseUrl/students/count") -eq $initialCount) "Comptage final different du comptage initial"
Write-Host "`nDEMONSTRATION VALIDEE : CRUD, statistiques, validation et Swagger."
Write-Host "Les trois exemples crees par ce script ont ete supprimes."
