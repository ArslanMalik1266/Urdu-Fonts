$oldPkg = "com.webscare.urdufonts"
$newPkg = "com.urdufonts.app"

Write-Host "Starting package rename from $oldPkg to $newPkg..."

# 1. Update text content in all files under app/
$files = Get-ChildItem -Path "d:\UrduFonts\app" -Recurse -Include *.kt, *.xml, *.kts, *.pro -File
foreach ($file in $files) {
    $content = Get-Content $file.FullName -Raw -Encoding UTF8
    if ($content -match [regex]::Escape($oldPkg)) {
        $newContent = $content -replace [regex]::Escape($oldPkg), $newPkg
        [System.IO.File]::WriteAllText($file.FullName, $newContent, [System.Text.Encoding]::UTF8)
        Write-Host "Updated content in: $($file.Name)"
    }
}

# 2. Move source directories
$srcMainOld = "d:\UrduFonts\app\src\main\java\com\webscare\urdufonts"
$srcMainNew = "d:\UrduFonts\app\src\main\java\com\urdufonts\app"

if (Test-Path $srcMainOld) {
    New-Item -ItemType Directory -Force -Path "d:\UrduFonts\app\src\main\java\com\urdufonts" | Out-Null
    Move-Item -Path $srcMainOld -Destination $srcMainNew -Force
    Remove-Item -Path "d:\UrduFonts\app\src\main\java\com\webscare" -Recurse -Force
    Write-Host "Moved main source directory successfully."
}

$srcTestOld = "d:\UrduFonts\app\src\test\java\com\webscare\urdufonts"
$srcTestNew = "d:\UrduFonts\app\src\test\java\com\urdufonts\app"

if (Test-Path $srcTestOld) {
    New-Item -ItemType Directory -Force -Path "d:\UrduFonts\app\src\test\java\com\urdufonts" | Out-Null
    Move-Item -Path $srcTestOld -Destination $srcTestNew -Force
    Remove-Item -Path "d:\UrduFonts\app\src\test\java\com\webscare" -Recurse -Force
    Write-Host "Moved test source directory successfully."
}

$srcAndroidTestOld = "d:\UrduFonts\app\src\androidTest\java\com\webscare\urdufonts"
$srcAndroidTestNew = "d:\UrduFonts\app\src\androidTest\java\com\urdufonts\app"

if (Test-Path $srcAndroidTestOld) {
    New-Item -ItemType Directory -Force -Path "d:\UrduFonts\app\src\androidTest\java\com\urdufonts" | Out-Null
    Move-Item -Path $srcAndroidTestOld -Destination $srcAndroidTestNew -Force
    Remove-Item -Path "d:\UrduFonts\app\src\androidTest\java\com\webscare" -Recurse -Force
    Write-Host "Moved androidTest source directory successfully."
}

Write-Host "Package rename completed successfully!"
