$path = 'C:\Users\diamo\.cursor\plans\quest_system_architecture_b9749397.plan.md'
$c = [System.IO.File]::ReadAllText($path)

$c = $c.Replace('`**QuestDefinition`**', '**`QuestDefinition`**')
$c = $c.Replace('`**QuestPhaseDefinition**`', '**`QuestPhaseDefinition`**')
$c = $c.Replace('`**QuestStageDefinition**`', '**`QuestStageDefinition`**')
$c = $c.Replace('`**QuestObjectiveDefinition**`', '**`QuestObjectiveDefinition`**')
$c = $c.Replace('`**QuestInstance`**', '**`QuestInstance`**')
$c = $c.Replace('`**QuestStageInstance**`', '**`QuestStageInstance`**')
$c = $c.Replace('`**QuestObjectiveInstance**`', '**`QuestObjectiveInstance`**')
$c = $c.Replace('`**quests/`**', '**`quests/`**')
$c = $c.Replace('`**QuestScopeProvider` additions for rescoping:**', '**`QuestScopeProvider` additions for rescoping:**')
$c = $c.Replace('`**/quest start`**', '**`/quest start`**')
$c = $c.Replace('`**/quest info**`', '**`/quest info`**')
$c = $c.Replace('`**/quest history**`', '**`/quest history`**')
$c = $c.Replace('`**/quest admin reload**`', '**`/quest admin reload`**')
$c = $c.Replace('`**/quest admin setstate**`', '**`/quest admin setstate`**')
$c = $c.Replace('`**/quest admin setprogress**`', '**`/quest admin setprogress`**')

[System.IO.File]::WriteAllText($path, $c)
Write-Host "Done - replacements applied"
