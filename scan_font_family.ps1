$uiPath = "d:\UrduFonts\app\src\main\java\com\webscare\urdufonts\ui"
$files = Get-ChildItem -Path $uiPath -Filter *.kt -Recurse

$results = @()

foreach ($file in $files) {
    $content = [System.IO.File]::ReadAllText($file.FullName)
    
    # Match Text( (allowing whitespace before parenthesis)
    $matches = [regex]::Matches($content, '(?<![a-zA-Z0-9_])Text\s*\(')
    
    foreach ($match in $matches) {
        $startIndex = $match.Index
        
        # Find where the opening parenthesis is
        $parenIndex = $content.IndexOf('(', $startIndex)
        if ($parenIndex -eq -1) { continue }
        
        $openCount = 1
        $endIndex = -1
        
        # Scan forward to find the matching closing parenthesis
        for ($i = $parenIndex + 1; $i -lt $content.Length; $i++) {
            $char = $content[$i]
            if ($char -eq '(') {
                $openCount++
            } elseif ($char -eq ')') {
                $openCount--
                if ($openCount -eq 0) {
                    $endIndex = $i
                    break
                }
            }
        }
        
        if ($endIndex -ne -1) {
            $textBlock = $content.Substring($startIndex, $endIndex - $startIndex + 1)
            
            # Check if this Text block does NOT contain fontFamily
            if ($textBlock -notlike "*fontFamily*") {
                # Get line number of the match
                $lineNum = ($content.Substring(0, $startIndex) -split "`n").Length
                
                # Get clean single-line snippet of the text block for display
                $snippet = ($textBlock -replace "\s+", " ").Trim()
                if ($snippet.Length -gt 120) {
                    $snippet = $snippet.Substring(0, 117) + "..."
                }
                
                $results += [PSCustomObject]@{
                    File = $file.FullName.Replace($uiPath, "")
                    Line = $lineNum
                    Snippet = $snippet
                }
            }
        }
    }
}

$results | Format-Table -AutoSize -Wrap
