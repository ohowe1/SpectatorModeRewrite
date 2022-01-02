[![Maintainability](https://api.codeclimate.com/v1/badges/b886095a96a861fe1a35/maintainability)](https://codeclimate.com/github/carelesshippo/SpectatorModeRewrite/maintainability)
[![Test Coverage](https://api.codeclimate.com/v1/badges/b886095a96a861fe1a35/test_coverage)](https://codeclimate.com/github/carelesshippo/SpectatorModeRewrite/test_coverage)
# SMP Spectator Mode

---

### This plugin is not being actively developed. New features are however welcome. If you would like a new feature open a issue [here](https://github.com/carelesshippo/SpectatorModeRewrite/issues).

Download on [SpigotMC](https://www.spigotmc.org/resources/smp-spectator-mode.77267/)

<details><summary>Default config file</summary>
<p>
## Default `config.yml`

```yml
#   _____ __  __ _____     _____                 _        _               __  __           _
#  / ____|  \/  |  __ \   / ____|               | |      | |             |  \/  |         | |
# | (___ | \  / | |__) | | (___  _ __   ___  ___| |_ __ _| |_ ___  _ __  | \  / | ___   __| | ___
#  \___ \| |\/| |  ___/   \___ \| '_ \ / _ \/ __| __/ _` | __/ _ \| '__| | |\/| |/ _ \ / _` |/ _ \
#  ____) | |  | | |       ____) | |_) |  __/ (__| || (_| | || (_) | |    | |  | | (_) | (_| |  __/
# |_____/|_|  |_|_|      |_____/| .__/ \___|\___|\__\__,_|\__\___/|_|    |_|  |_|\___/ \__,_|\___|
#                               | |
#                               |_|

# If the command /s is enabled (/s enable overrules this)
enabled: true

# If a player receives the night vision effect while in spectator mode
night-vision: true

# If a player receives the conduit effect while in spectator mode
conduit: true

# If when a player logs on in spectator mode they will be teleported back
teleport-back: false

# Whether to enforce the worlds
enforce-worlds: false
# The names of the worlds spectator mode is allowed in
worlds-allowed: [ world, world_nether, world_the_end ]

# If the y level is limited to the number in y-level in spectator mode
enforce-y: false

# see above (players can not go below this level) double
y-level: 0.0

# If a player is not allowed to go through non-see-through able blocks in spectator mode
disallow-non-transparent-blocks: false

# If a player is not allowed to go through blocks in spectator mode
disallow-all-blocks: false

# Specific blocks that a player cannot go through. the id
disallowed-blocks: [ ]

# How close a player can get to a block, to be used with disallow-all-blocks (percentage of block), adjust according to ping. integer
bubble-size: 35

# Whether to make it so players can not go past a certain distance in spectator mode. The permission smpspectator.bypass bypasses this
enforce-distance: false
# See above (blocks) integer
distance: 64

# The minimum health a player can have to activate /s
minimum-health: 0

# Prevents players from using the spectator teleport hot bar
prevent-teleport: false
# Prevents these commands from being executed unless you have the smpspectator.bypass permission. Example list: [back, return, home, homes, tpaccept, tpyes, warp, warps]
bad-commands: [ ]

# If this is above 0, the player has to be still for the next X seconds after preforming the command to enter spectator mode. int
stand-still-ticks: 0

# Prevents players from going past the world border in spectator mode
enforce-world-border: true

# If this is true, players will not see the setting gamemode messages
disable-switching-message: false

# If this is true, you won't get the survival-mode-message on join, if the server sent you back into survival
silence-survival-mode-message-on-join: true

# If this is true the message with a new version, or up to date message will appear
update-checker: true

# If a hostile mob is within this distance, the player will not be allowed into spectator mode. 0 is off.
closest-hostile: 0

# This will detach leads when a player enters spectator mode with /s
detach-leads: true

# If your server is having lag issues it is advised to turn this off
mobs: true

# If another plugin or a command changes a player's gamemode while they are in spectator mode, the effects will be removed;
watch-gamemode: true

# If they are falling, disallow spectator mode
prevent-falling: true

# Only allow spectating entities and no free movement
only-spectating-no-free-movement: false

# Only allow players to stay in spectator mode for this many ticks. -1 for no limit
spectator-ticks: -1

# vanish upon entering spectator mode. This will make em un-vanish once they exit spectator mode.
# the player will be vanished regardless if they have supervanish permissions if you enable this option.
# if you don't want players to enter vanish when using spectator mode with /gamemode, disable watch-gamemode
supervanish-hook: false

### Message section ###
#Adding /actionbar/ in front of a message, will make it appear in the actionbar instead of the chat

# Message when gamemode set to spectator mode
spectator-mode-message: '&9Setting gamemode to &b&lSPECTATOR MODE'

# Message when gamemode set to survival mode
survival-mode-message: '&9Setting gamemode to &b&lSURVIVAL MODE'

# Message when user preforms the command while falling (error message)
falling-message: '&cHey you &lcan not &r&cdo that while falling!'

# Message when user preforms command in world it is not allowed in
world-message: '&cHey you&l can not &r&cdo that in that world!'

# Message sent when a player tries to execute /s but is below the minimum health
health-message: '&cYou are below the minimum required health to preform this command!'

# Message when spectator mode is disabled and the user runs the command
disabled-message: '&cSpectator Mode is &lnot &r&cenabled by the server!'

# Message when spectator mode has been disabled
disable-message: '&dSpectator mode has been &ldisabled'

# Message when spectator mode has been enabled
enable-message: '&dSpectator mode has been &lenabled'

# Message when the config.yml is reloaded
reload-message: '&bThe config file has been reloaded!'

# Message sent when an invalid player is forced into spectator mode
invalid-player-message: '&cThat is not a valid player'

# Message when forcing a player was successful. /target/ is the player
force-success: '&bSuccessfully forced /target/'

# Message sent when a player tries to use the /s effect when not in spectator mode
no-spectator-message: '&cYou did not preform the /s command'

# Message sent when a player tries to execute a command not allowed in spectator mode
bad-command-message: '&cYou can not execute that command while in spectator mode'

# Message sent when a player executes /s while in spectator mode but did not use it to get into spectator mode
not-in-state-message: '&cYou did not use this command to get into spectator mode!'

# Message sent when a player tries to use /s when they are to close to a hostile mob (See closest-hostile)
mob-too-close-message: '&cYou are too close to a hostile mob to enter spectator mode'

# Message sent when a player teleports when not allowed to
unallowed-teleport-message: '&cYou are not allowed to teleport in spectator mode'

# Message sent when a player is below the enforced y-level limit
y-level-limit-message: '&cYou are below the enforced y-level limit'

# Message sent when a player is told to stand still
stand-still-message: '&bStand still to be put into spectator mode!'

# Message sent when player does not stand still
moved-message: '&cYou moved! Spectator mode has been cancelled'

# Message sent when player has reached there time limit in spectator mode
times-up-message: '&cTime limit reached! Toggling gamemode to &b&lSURVIVAL MODE'

# Get debug logs
debug: false
```
</p>
</details>

<details><summary>Permissions</summary>
<p>
  
## Permissions for SMP Spectator Mode
`spectator.*`: Give access to the entire permission node

`smpspectator.use`: Be able to use the /s command

`smpspectator.enable`: Be able to enable and disable spectator mode from the /s command

`smpspectator.speed`: Be able to change fly speed in spectator mode

`smpspectator.bypass`: Be able to bypass the y-level and block restrictions

`smpspectator.force`: Be able to force other players into and out of spectator mode

`smpspectator.toggle`: Be able to use the /seffect command

`smpspectator.reload`: Be able to reload the config

</p>
</details>

