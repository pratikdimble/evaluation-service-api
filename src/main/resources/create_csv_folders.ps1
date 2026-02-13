# Base folder where folders will be created
$baseFolder = "D:\POCs\CC\evaluation-service-api\src\main\resources\Batch"
# $baseFolder = "D:\POCs\CC\evaluation-service-api\src\main\resources\Online"

# Make sure base folder exists
if (-not (Test-Path $baseFolder)) {
    New-Item -Path $baseFolder -ItemType Directory
}

# Number of folders to create
$numberOfFolders = 40
$startIndex = 26  # Start from model16

# Loop to create folders and CSV files
for ($i = 0; $i -lt $numberOfFolders; $i++) {
    $folderNumber = $startIndex + $i
    $folderName = "model$folderNumber"
    $folderPath = Join-Path $baseFolder $folderName

    # Create folder
    if (-not (Test-Path $folderPath)) {
        New-Item -Path $folderPath -ItemType Directory
    }
	
	$nestedPath = Join-Path $baseFolder "$folderName\GCP_Online\testplan_dev\report"
# 	$nestedPath = Join-Path $baseFolder "$folderName\GCP_vsOnPrem\testplan_dev\report"
	if (-not (Test-Path $nestedPath)) {
        New-Item -Path $nestedPath -ItemType Directory -Force
    }

    # CSV file path
    $csvPath = Join-Path $nestedPath  "comparison_summary.csv"

    # Generate CSV content with 10 rows
    $csvContent = @()
    for ($row = 1; $row -le 10; $row++) {
        $scenario = "Scenario$row"
		$inputA = "${folderName}_IN$row.dat"
        $layoutA = "N"

        # Random record count less than 10000
        $recordCount = Get-Random -Minimum 1 -Maximum 10000

        # InputB is InputA appended with _out
        $inputB = [System.IO.Path]::GetFileNameWithoutExtension($inputA) + "_out.dat"
        $layoutB = "N"

        $comparedRecords = $recordCount
        $comparedAttributes = Get-Random -Minimum 30 -Maximum 100
        $attributesDifferences = 0

        $csvContent += "$scenario,$inputA,$layoutA,$recordCount,$inputB,$layoutB,$recordCount,$comparedRecords,$comparedAttributes,$attributesDifferences"
    }

    # Write CSV file with header
    "Scenario,InputA,LayoutA Modified,InputA Records,InputB,LayoutB Modified,InputB Records,Compared Records,Compared Attributes,Attributes with Differences" | Out-File -FilePath $csvPath -Encoding UTF8
    $csvContent | Out-File -FilePath $csvPath -Encoding UTF8 -Append
}

Write-Host "Created $numberOfFolders folders starting from model$startIndex with CSV files (random records <10000) and InputB appended with _out."
