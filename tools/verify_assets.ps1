param(
    [string]$WorkspaceRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
)

$ErrorActionPreference = 'Stop'

$assetRoot = Join-Path $WorkspaceRoot 'src/main/resources/assets/occultism'
$modelRoot = Join-Path $assetRoot 'models'
$textureRoot = Join-Path $assetRoot 'textures'
$blockstateRoot = Join-Path $assetRoot 'blockstates'
$recipeRoot = Join-Path $assetRoot 'recipes'
$ritualRoot = Join-Path $assetRoot 'rituals'
$dictionaryIndexPath = Join-Path $assetRoot 'dictionary/modonomicon_index.json'
$enLangPath = Join-Path $assetRoot 'lang/en_us.lang'
$zhLangPath = Join-Path $assetRoot 'lang/zh_cn.lang'
$modBlocksPath = Join-Path $WorkspaceRoot 'src/main/java/com/occultism/occultism/registry/ModBlocks.java'
$modItemsPath = Join-Path $WorkspaceRoot 'src/main/java/com/occultism/occultism/registry/ModItems.java'
$dictionaryGuiPath = Join-Path $WorkspaceRoot 'src/main/java/com/occultism/occultism/client/gui/GuiDictionaryOfSpirits.java'
$pentacleDetectorPath = Join-Path $WorkspaceRoot 'src/main/java/com/occultism/occultism/ritual/PentacleDetector.java'

$jsonErrors = New-Object System.Collections.Generic.List[string]
$missingParents = New-Object System.Collections.Generic.List[string]
$missingTextures = New-Object System.Collections.Generic.List[string]
$missingBlockstateModels = New-Object System.Collections.Generic.List[string]
$missingBlockstates = New-Object System.Collections.Generic.List[string]
$missingItemBlockModels = New-Object System.Collections.Generic.List[string]
$missingItemModels = New-Object System.Collections.Generic.List[string]
$missingDictionaryImages = New-Object System.Collections.Generic.List[string]
$missingLangEn = New-Object System.Collections.Generic.List[string]
$missingLangZh = New-Object System.Collections.Generic.List[string]
$recipeSemanticErrors = New-Object System.Collections.Generic.List[string]
$blockstateSemanticErrors = New-Object System.Collections.Generic.List[string]
$dictionarySemanticErrors = New-Object System.Collections.Generic.List[string]

function Read-JsonFile {
    param([string]$Path)

    try {
        return Get-Content -LiteralPath $Path -Raw | ConvertFrom-Json
    } catch {
        $script:jsonErrors.Add("$Path :: $($_.Exception.Message)")
        return $null
    }
}

function Test-LocalModelRef {
    param([string]$Ref)

    if ([string]::IsNullOrWhiteSpace($Ref)) {
        return $true
    }
    if ($Ref -like 'builtin/*' -or $Ref -like 'block/*' -or $Ref -like 'item/*') {
        return $true
    }
    if ($Ref -like 'minecraft:*') {
        return $true
    }

    $pathPart = $Ref
    if ($Ref -like 'occultism:*') {
        $pathPart = $Ref.Substring('occultism:'.Length)
    }

    return Test-Path -LiteralPath (Join-Path $script:modelRoot ($pathPart + '.json'))
}

function Test-BlockstateModelRef {
    param([string]$Ref)

    if ([string]::IsNullOrWhiteSpace($Ref)) {
        return $true
    }
    if ($Ref -like 'minecraft:*') {
        return $true
    }

    $pathPart = $Ref
    if ($Ref -like 'occultism:*') {
        $pathPart = $Ref.Substring('occultism:'.Length)
    }

    return Test-Path -LiteralPath (Join-Path (Join-Path $script:modelRoot 'block') ($pathPart + '.json'))
}

function Test-TextureRef {
    param([string]$Ref)

    if ([string]::IsNullOrWhiteSpace($Ref)) {
        return $true
    }
    if ($Ref.StartsWith('#')) {
        return $true
    }
    if ($Ref -like 'minecraft:*') {
        return $true
    }

    $pathPart = $Ref
    if ($Ref -like 'occultism:*') {
        $pathPart = $Ref.Substring('occultism:'.Length)
    }

    return Test-Path -LiteralPath (Join-Path $script:textureRoot ($pathPart + '.png'))
}

function ForEach-ObjectNode {
    param(
        [object]$Value,
        [scriptblock]$Action
    )

    if ($null -eq $Value) {
        return
    }

    & $Action $Value

    if ($Value -is [System.Collections.IEnumerable] -and -not ($Value -is [string])) {
        foreach ($entry in $Value) {
            ForEach-ObjectNode $entry $Action
        }
        return
    }

    if ($Value.PSObject -and $Value.PSObject.Properties) {
        foreach ($prop in $Value.PSObject.Properties) {
            ForEach-ObjectNode $prop.Value $Action
        }
    }
}

function Check-ModelJson {
    param(
        [object]$Json,
        [string]$Path
    )

    if ($null -eq $Json) {
        return
    }

    if ($Json.PSObject.Properties.Name -contains 'parent') {
        $parent = [string]$Json.parent
        if (-not (Test-LocalModelRef $parent)) {
            $script:missingParents.Add("$Path -> parent $parent")
        }
    }

    if ($Json.PSObject.Properties.Name -contains 'textures') {
        foreach ($prop in $Json.textures.PSObject.Properties) {
            $texture = [string]$prop.Value
            if (-not (Test-TextureRef $texture)) {
                $script:missingTextures.Add("$Path -> texture $($prop.Name)=$texture")
            }
        }
    }

    if ($Json.PSObject.Properties.Name -contains 'overrides') {
        foreach ($override in @($Json.overrides)) {
            if ($override.PSObject.Properties.Name -contains 'model') {
                $model = [string]$override.model
                if (-not (Test-LocalModelRef $model)) {
                    $script:missingParents.Add("$Path -> override model $model")
                }
            }
        }
    }
}

function Check-BlockstateJson {
    param(
        [object]$Json,
        [string]$Path
    )

    if ($null -eq $Json) {
        return
    }

    ForEach-ObjectNode $Json {
        param([object]$Node)

        if ($Node.PSObject -and $Node.PSObject.Properties.Name -contains 'model') {
            $model = [string]$Node.model
            if (-not (Test-BlockstateModelRef $model)) {
                $script:missingBlockstateModels.Add("$Path -> model $model")
            }
        }
    }
}

function Check-RecipeSemantics {
    param(
        [object]$Json,
        [string]$Path
    )

    if ($null -eq $Json) {
        return
    }

    $recipeName = [System.IO.Path]::GetFileNameWithoutExtension($Path)
    $slabResults = @{
        'otherplanks_slab' = 'occultism:otherplanks_slab'
        'otherstone_slab' = 'occultism:otherstone_slab'
        'otherrock_slab' = 'occultism:otherrock_slab'
        'polished_otherstone_slab' = 'occultism:polished_otherstone_slab'
        'polished_otherrock_slab' = 'occultism:polished_otherrock_slab'
        'otherstone_bricks_slab' = 'occultism:otherstone_bricks_slab'
        'otherrock_bricks_slab' = 'occultism:otherrock_bricks_slab'
    }

    if ($slabResults.ContainsKey($recipeName)) {
        if (-not ($Json.PSObject.Properties.Name -contains 'result')) {
            $script:recipeSemanticErrors.Add("$Path -> missing result")
        } else {
            $result = $Json.result
            $expectedItem = $slabResults[$recipeName]
            if ([string]$result.item -ne $expectedItem) {
                $script:recipeSemanticErrors.Add("$Path -> expected result item $expectedItem")
            }
            if (-not ($result.PSObject.Properties.Name -contains 'data') -or [int]$result.data -ne 0) {
                $script:recipeSemanticErrors.Add("$Path -> subtype result must declare data 0")
            }
        }
    }

    $chiseledInputs = @{
        'chiseled_otherstone_bricks' = 'occultism:otherstone_bricks_slab'
        'chiseled_otherrock_bricks' = 'occultism:otherrock_bricks_slab'
    }
    if ($chiseledInputs.ContainsKey($recipeName)) {
        $ingredient = $null
        if ($Json.PSObject.Properties.Name -contains 'key' -and
                $Json.key.PSObject.Properties.Name -contains 'O') {
            $ingredient = $Json.key.O
        }
        $expectedItem = $chiseledInputs[$recipeName]
        if ($null -eq $ingredient -or [string]$ingredient.item -ne $expectedItem) {
            $script:recipeSemanticErrors.Add("$Path -> expected O ingredient $expectedItem")
        } elseif (-not ($ingredient.PSObject.Properties.Name -contains 'data') -or [int]$ingredient.data -ne 0) {
            $script:recipeSemanticErrors.Add("$Path -> subtype ingredient must declare data 0")
        }
    }
}

function Check-BlockstateSemantics {
    param(
        [object]$Json,
        [string]$Path
    )

    if ($null -eq $Json -or [System.IO.Path]::GetFileName($Path) -ne 'otherplanks_fence_gate.json') {
        return
    }
    if (-not ($Json.PSObject.Properties.Name -contains 'variants')) {
        $script:blockstateSemanticErrors.Add("$Path -> missing variants")
        return
    }

    $expected = New-Object System.Collections.Generic.HashSet[string]
    foreach ($facing in @('south', 'west', 'north', 'east')) {
        foreach ($inWall in @('false', 'true')) {
            foreach ($open in @('false', 'true')) {
                foreach ($powered in @('false', 'true')) {
                    [void]$expected.Add("facing=$facing,in_wall=$inWall,open=$open,powered=$powered")
                }
            }
        }
    }

    $actual = New-Object System.Collections.Generic.HashSet[string]
    foreach ($variant in $Json.variants.PSObject.Properties.Name) {
        [void]$actual.Add([string]$variant)
    }
    foreach ($variant in $expected) {
        if (-not $actual.Contains($variant)) {
            $script:blockstateSemanticErrors.Add("$Path -> missing variant $variant")
        }
    }
    foreach ($variant in $actual) {
        if (-not $expected.Contains($variant)) {
            $script:blockstateSemanticErrors.Add("$Path -> unexpected variant $variant")
        }
    }
}

function Check-DictionaryIndexSemantics {
    param([object]$Json)

    if ($null -eq $Json -or -not ($Json.PSObject.Properties.Name -contains 'categories')) {
        return
    }

    $entryIds = New-Object System.Collections.Generic.HashSet[string]
    foreach ($category in @($Json.categories)) {
        foreach ($entry in @($category.entries)) {
            [void]$entryIds.Add("occultism:$($category.id)/$($entry.id)")
        }
    }

    $pentacleIds = New-Object System.Collections.Generic.HashSet[string]
    $pentacleSource = Get-Content -LiteralPath $script:pentacleDetectorPath -Raw
    foreach ($match in [regex]::Matches($pentacleSource, 'new PentacleDefinition\("([^"]+)"')) {
        [void]$pentacleIds.Add($match.Groups[1].Value)
    }

    $bookBindingRecipes = @{
        'occultism:book_of_binding_foliot' = 'occultism:crafting/book_of_binding_bound_foliot_from_unbound'
        'occultism:book_of_binding_djinni' = 'occultism:crafting/book_of_binding_bound_djinni_from_unbound'
        'occultism:book_of_binding_afrit' = 'occultism:crafting/book_of_binding_bound_afrit_from_unbound'
        'occultism:book_of_binding_marid' = 'occultism:crafting/book_of_binding_bound_marid_from_unbound'
    }

    foreach ($category in @($Json.categories)) {
        foreach ($entry in @($category.entries)) {
            $entryContext = "$($category.id)/$($entry.id)"
            foreach ($parent in @($entry.parents)) {
                if ($null -eq $parent) {
                    continue
                }
                $parentId = [string]$parent.entry
                if ([string]::IsNullOrWhiteSpace($parentId) -or -not $entryIds.Contains($parentId)) {
                    $script:dictionarySemanticErrors.Add("$entryContext -> unresolved parent $parentId")
                }
            }

            foreach ($page in @($entry.pages)) {
                $pageContext = "$entryContext/$($page.id)"
                $pageType = [string]$page.type

                if ($pageType -eq 'modonomicon:spotlight') {
                    if (-not ($page.PSObject.Properties.Name -contains 'item') -or
                            -not ($page.item -is [string]) -or
                            [string]::IsNullOrWhiteSpace([string]$page.item)) {
                        $script:dictionarySemanticErrors.Add("$pageContext -> spotlight item must be a string id")
                    } elseif ([string]$page.detail -ne "Item: $($page.item)") {
                        $script:dictionarySemanticErrors.Add("$pageContext -> spotlight detail does not match item id")
                    }
                }

                if ($pageType -eq 'occultism:book_binding_recipe') {
                    $unboundItem = ''
                    if ($page.PSObject.Properties.Name -contains 'unbound_book' -and
                            $page.unbound_book.PSObject.Properties.Name -contains 'id') {
                        $unboundItem = [string]$page.unbound_book.id
                    }
                    if (-not $bookBindingRecipes.ContainsKey($unboundItem)) {
                        $script:dictionarySemanticErrors.Add("$pageContext -> unknown unbound book $unboundItem")
                    } else {
                        $expectedRecipe = $bookBindingRecipes[$unboundItem]
                        $actualRecipe = [string]$page.recipe_id_1
                        if ($actualRecipe -ne $expectedRecipe) {
                            $script:dictionarySemanticErrors.Add("$pageContext -> expected recipe $expectedRecipe")
                        }
                        if ([string]$page.detail -ne "Recipe: $expectedRecipe") {
                            $script:dictionarySemanticErrors.Add("$pageContext -> book-binding detail does not match recipe")
                        }
                        $recipeId = $expectedRecipe.Substring('occultism:crafting/'.Length)
                        if (-not (Test-Path -LiteralPath (Join-Path $script:recipeRoot ($recipeId + '.json')))) {
                            $script:dictionarySemanticErrors.Add("$pageContext -> missing local recipe $recipeId")
                        }
                    }
                }

                if ($pageType -eq 'modonomicon:multiblock') {
                    $multiblockId = [string]$page.multiblock_id
                    $pentacleId = $multiblockId
                    if ($pentacleId.StartsWith('occultism:')) {
                        $pentacleId = $pentacleId.Substring('occultism:'.Length)
                    }
                    if ([string]::IsNullOrWhiteSpace($pentacleId) -or -not $pentacleIds.Contains($pentacleId)) {
                        $script:dictionarySemanticErrors.Add("$pageContext -> unresolved multiblock $multiblockId")
                    } elseif ([string]$page.detail -ne "Multiblock: $multiblockId") {
                        $script:dictionarySemanticErrors.Add("$pageContext -> multiblock detail does not match id")
                    }
                } elseif ($page.PSObject.Properties.Name -contains 'multiblock_id') {
                    $script:dictionarySemanticErrors.Add("$pageContext -> non-multiblock page retains multiblock_id")
                }
            }
        }
    }

    ForEach-ObjectNode $Json {
        param([object]$Node)

        if ($Node.PSObject -and
                $Node.PSObject.Properties.Name -contains 'type' -and
                [string]$Node.type -eq 'modonomicon:entry_read') {
            $entryId = [string]$Node.entry_id
            if ([string]::IsNullOrWhiteSpace($entryId) -or -not $entryIds.Contains($entryId)) {
                $script:dictionarySemanticErrors.Add("unresolved entry_read condition $entryId")
            }
        }
    }
}

function Get-LangKeys {
    param([string]$LangText)

    $keys = New-Object System.Collections.Generic.HashSet[string]
    foreach ($line in ($LangText -split "`r?`n")) {
        if ([string]::IsNullOrWhiteSpace($line) -or $line.TrimStart().StartsWith('#')) {
            continue
        }
        $separator = $line.IndexOf('=')
        if ($separator -gt 0) {
            [void]$keys.Add($line.Substring(0, $separator))
        }
    }
    return $keys
}

function Add-DictionaryIndexLangKeys {
    param(
        [object]$Json,
        [System.Collections.Generic.List[string]]$Keys
    )

    if ($null -eq $Json -or -not ($Json.PSObject.Properties.Name -contains 'categories')) {
        return
    }

    foreach ($category in @($Json.categories)) {
        foreach ($property in @('name', 'description')) {
            if ($category.PSObject.Properties.Name -contains $property) {
                $value = [string]$category.$property
                if ($value.StartsWith('book.')) {
                    $Keys.Add($value)
                }
            }
        }

        foreach ($entry in @($category.entries)) {
            foreach ($property in @('name', 'description')) {
                if ($entry.PSObject.Properties.Name -contains $property) {
                    $value = [string]$entry.$property
                    if ($value.StartsWith('book.')) {
                        $Keys.Add($value)
                    }
                }
            }

            foreach ($page in @($entry.pages)) {
                foreach ($property in @('title', 'text')) {
                    if ($page.PSObject.Properties.Name -contains $property) {
                        $value = [string]$page.$property
                        if ($value.StartsWith('book.')) {
                            $Keys.Add($value)
                        }
                    }
                }
            }
        }
    }
}

function Test-DictionaryImageRef {
    param([string]$Ref)

    if ([string]::IsNullOrWhiteSpace($Ref)) {
        return $true
    }
    if ($Ref -like 'minecraft:*') {
        return $true
    }

    $pathPart = $Ref
    if ($Ref -like 'occultism:*') {
        $pathPart = $Ref.Substring('occultism:'.Length)
    }

    return Test-Path -LiteralPath (Join-Path $script:assetRoot $pathPart)
}

function Check-DictionaryIndexImages {
    param([object]$Json)

    if ($null -eq $Json -or -not ($Json.PSObject.Properties.Name -contains 'categories')) {
        return
    }

    foreach ($category in @($Json.categories)) {
        foreach ($entry in @($category.entries)) {
            foreach ($page in @($entry.pages)) {
                if (-not ($page.PSObject.Properties.Name -contains 'detail')) {
                    continue
                }
                $detail = [string]$page.detail
                if (-not $detail.StartsWith('Image: ')) {
                    continue
                }
                foreach ($image in @($detail.Substring('Image: '.Length).Split(','))) {
                    $imageRef = $image.Trim()
                    if (-not (Test-DictionaryImageRef $imageRef)) {
                        $script:missingDictionaryImages.Add("$($category.id)/$($entry.id)/$($page.id) -> $imageRef")
                    }
                }
            }
        }
    }
}

Get-ChildItem -LiteralPath $modelRoot -Recurse -File -Filter '*.json' | ForEach-Object {
    Check-ModelJson (Read-JsonFile $_.FullName) $_.FullName
}

Get-ChildItem -LiteralPath $blockstateRoot -File -Filter '*.json' | ForEach-Object {
    $blockstateJson = Read-JsonFile $_.FullName
    Check-BlockstateJson $blockstateJson $_.FullName
    Check-BlockstateSemantics $blockstateJson $_.FullName
}

Get-ChildItem -LiteralPath $recipeRoot -File -Filter '*.json' | ForEach-Object {
    $recipeJson = Read-JsonFile $_.FullName
    Check-RecipeSemantics $recipeJson $_.FullName
}

Get-ChildItem -LiteralPath $ritualRoot -File -Filter '*.json' | ForEach-Object {
    [void](Read-JsonFile $_.FullName)
}

$modBlocks = Get-Content -LiteralPath $modBlocksPath -Raw
$modItems = Get-Content -LiteralPath $modItemsPath -Raw
$dictionaryGui = Get-Content -LiteralPath $dictionaryGuiPath -Raw
$enLang = Get-Content -LiteralPath $enLangPath -Raw
$zhLang = Get-Content -LiteralPath $zhLangPath -Raw
$enLangKeys = Get-LangKeys $enLang
$zhLangKeys = Get-LangKeys $zhLang

$blocks = @([regex]::Matches($modBlocks, 'block\(\"([^\"]+)\"') | ForEach-Object { $_.Groups[1].Value })
$glyphs = @([regex]::Matches($modBlocks, 'glyph\(\"([^\"]+)\"') | ForEach-Object { $_.Groups[1].Value })
$blocks = @($blocks + $glyphs) | Sort-Object -Unique

$itemFactories = 'item|legacyRitualDummy|food|miner|magicLamp|familiarRing|chalk|brush|infusedPickaxe|iesniumPickaxe|butcherKnife|horseArmor|vitalityCompass|dimensionalMatrix|divinationRod|otherworldGoggles|jobToken|dictionary|satchel|enderSatchel|soulGem|soulShard|boundBook|callingBook|debugSummon|knowledgeTablet|naturePaste|damageInCrafting|demonDreamFood|storageRemote|seeds'
$itemPattern = '(?:' + $itemFactories + ')\("([^"]+)"'
$items = @([regex]::Matches($modItems, $itemPattern) | ForEach-Object { $_.Groups[1].Value }) | Sort-Object -Unique

$noItemBlock = @('datura', 'lighted_air') + $glyphs + @(
    'otherplanks_double_slab',
    'otherstone_double_slab',
    'otherrock_double_slab',
    'polished_otherstone_double_slab',
    'polished_otherrock_double_slab',
    'otherstone_bricks_double_slab',
    'otherrock_bricks_double_slab',
    'otherstone_natural',
    'otherrock_natural',
    'otherworld_log_natural',
    'stripped_otherworld_log_natural',
    'otherworld_leaves_natural',
    'otherflower_natural',
    'iesnium_ore_natural',
    'otherstone_wall',
    'otherrock_wall',
    'polished_otherstone_wall',
    'polished_otherrock_wall',
    'otherstone_bricks_wall',
    'otherrock_bricks_wall'
)

foreach ($block in $blocks) {
    if (-not (Test-Path -LiteralPath (Join-Path $blockstateRoot ($block + '.json')))) {
        $missingBlockstates.Add($block)
    }
    if ($block -notin $noItemBlock -and -not (Test-Path -LiteralPath (Join-Path (Join-Path $modelRoot 'item') ($block + '.json')))) {
        $missingItemBlockModels.Add($block)
    }
    if ($enLang -notmatch [regex]::Escape("tile.occultism.$block.name=")) {
        $missingLangEn.Add("tile.occultism.$block.name")
    }
    if ($zhLang -notmatch [regex]::Escape("tile.occultism.$block.name=")) {
        $missingLangZh.Add("tile.occultism.$block.name")
    }
}

foreach ($item in $items) {
    if (-not (Test-Path -LiteralPath (Join-Path (Join-Path $modelRoot 'item') ($item + '.json')))) {
        $missingItemModels.Add($item)
    }

    $translationPath = $item -replace '/', '.'
    if ($enLang -notmatch [regex]::Escape("item.occultism.$translationPath.name=")) {
        $missingLangEn.Add("item.occultism.$translationPath.name")
    }
    if ($zhLang -notmatch [regex]::Escape("item.occultism.$translationPath.name=")) {
        $missingLangZh.Add("item.occultism.$translationPath.name")
    }
}

$dictionaryGuiKeys = @([regex]::Matches($dictionaryGui, 'I18n\.format\("([^"]+)"') |
    ForEach-Object { $_.Groups[1].Value }) | Sort-Object -Unique
$dictionaryCategories = @([regex]::Matches($dictionaryGui, 'category\("([^"]+)"') |
    ForEach-Object { $_.Groups[1].Value }) | Sort-Object -Unique
$dictionaryEntries = @([regex]::Matches($dictionaryGui, 'entry\("([^"]+)"') |
    ForEach-Object { $_.Groups[1].Value }) | Sort-Object -Unique

foreach ($key in $dictionaryGuiKeys) {
    if ($enLang -notmatch [regex]::Escape("$key=")) {
        $missingLangEn.Add($key)
    }
    if ($zhLang -notmatch [regex]::Escape("$key=")) {
        $missingLangZh.Add($key)
    }
}

foreach ($category in $dictionaryCategories) {
    $key = "guide.occultism.dictionary.category.$category.title"
    if ($enLang -notmatch [regex]::Escape("$key=")) {
        $missingLangEn.Add($key)
    }
    if ($zhLang -notmatch [regex]::Escape("$key=")) {
        $missingLangZh.Add($key)
    }
}

foreach ($entry in $dictionaryEntries) {
    foreach ($suffix in @('title', 'body')) {
        $key = "guide.occultism.dictionary.$entry.$suffix"
        if ($enLang -notmatch [regex]::Escape("$key=")) {
            $missingLangEn.Add($key)
        }
        if ($zhLang -notmatch [regex]::Escape("$key=")) {
            $missingLangZh.Add($key)
        }
    }
}

if (Test-Path -LiteralPath $dictionaryIndexPath) {
    $dictionaryIndex = Read-JsonFile $dictionaryIndexPath
    $dictionaryIndexKeys = New-Object System.Collections.Generic.List[string]
    Add-DictionaryIndexLangKeys $dictionaryIndex $dictionaryIndexKeys
    Check-DictionaryIndexImages $dictionaryIndex
    Check-DictionaryIndexSemantics $dictionaryIndex

    foreach ($key in @($dictionaryIndexKeys | Sort-Object -Unique)) {
        if (-not $enLangKeys.Contains($key)) {
            $missingLangEn.Add($key)
        }
        if (-not $zhLangKeys.Contains($key)) {
            $missingLangZh.Add($key)
        }
    }
}

$summary = [ordered]@{
    ModelJsonFiles = @(Get-ChildItem -LiteralPath $modelRoot -Recurse -File -Filter '*.json').Count
    BlockstateJsonFiles = @(Get-ChildItem -LiteralPath $blockstateRoot -File -Filter '*.json').Count
    RecipeJsonFiles = @(Get-ChildItem -LiteralPath $recipeRoot -File -Filter '*.json').Count
    RitualJsonFiles = @(Get-ChildItem -LiteralPath $ritualRoot -File -Filter '*.json').Count
    RegisteredBlocks = @($blocks).Count
    RegisteredItems = @($items).Count
    JsonErrors = $jsonErrors.Count
    MissingParents = $missingParents.Count
    MissingTextures = $missingTextures.Count
    MissingBlockstateModels = $missingBlockstateModels.Count
    MissingBlockstates = $missingBlockstates.Count
    MissingItemBlockModels = $missingItemBlockModels.Count
    MissingItemModels = $missingItemModels.Count
    MissingDictionaryImages = $missingDictionaryImages.Count
    MissingLangEn = $missingLangEn.Count
    MissingLangZh = $missingLangZh.Count
    RecipeSemanticErrors = $recipeSemanticErrors.Count
    BlockstateSemanticErrors = $blockstateSemanticErrors.Count
    DictionarySemanticErrors = $dictionarySemanticErrors.Count
}

[pscustomobject]$summary | Format-List

$sections = [ordered]@{
    'JSON errors' = $jsonErrors
    'Missing model parents or overrides' = $missingParents
    'Missing textures' = $missingTextures
    'Missing blockstate models' = $missingBlockstateModels
    'Missing blockstates for registered blocks' = $missingBlockstates
    'Missing item models for registered item-blocks' = $missingItemBlockModels
    'Missing item models for registered items' = $missingItemModels
    'Missing Dictionary of Spirits image refs' = $missingDictionaryImages
    'Missing English language keys' = $missingLangEn
    'Missing Chinese language keys' = $missingLangZh
    'Recipe semantic errors' = $recipeSemanticErrors
    'Blockstate semantic errors' = $blockstateSemanticErrors
    'Dictionary semantic errors' = $dictionarySemanticErrors
}

$hasFailures = $false
foreach ($section in $sections.GetEnumerator()) {
    if ($section.Value.Count -gt 0) {
        $hasFailures = $true
        Write-Host "--- $($section.Key) ---"
        $section.Value | Select-Object -First 200 | ForEach-Object { Write-Host $_ }
        if ($section.Value.Count -gt 200) {
            Write-Host "... $($section.Value.Count - 200) more"
        }
    }
}

if ($hasFailures) {
    exit 1
}

Write-Host 'Asset verification passed.'
