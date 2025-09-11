# FTB Echoes

Use https://github.com/FTBTeam/FTB-Mods-Issues for any mod issues

## Echo Datapack Format

Each Echo is loaded a datapack json file, located in `data/<modid>/echo_definitions/<echo_id>`. E.g. the Echo `ftbechoes:test1` should be defined in `data/ftbechoes/echo_definitions/test1.json`.

### Top Level

Note that whenever "a serialized component" is mentioned below, this is the raw json for a Minecraft text component, as documented [here](https://minecraft.wiki/w/Text_component_format).

See example echo definitions [here](https://github.com/FTBTeam/FTB-Echoes/blob/main/src/main/resources/data/ftbechoes/echo_definitions/). These test definitions are loaded by the mod only when in a development environment.

An Echo json has the following top-level fields, all required:

* `id` - must be unique to the Echo; match the path & filename
* `title` - displayed in the Echo GUI, a serialized component
* `stages` - a list of Echo stages for this echo, see below
* `all_complete` - a message displayed at the end of the Echo GUI lore panel when the team has completed all stages. Optional, defaults to "All Stages Complete!"

### Echo Stages

Echo stages define the progression for an Echo. Fields:

* `title` - optional, displayed in the Lore page as the first text line of a stage if present
* `lore` - a list of lore entry components, see [Lore Entry Components](#lore-entry-components) below
* `not_ready` - serialized component, text displayed after the lore or in the task panel if the player isn't ready to complete the stage
* `ready` - serialized component, text displayed after the lore or in the task panel if the player _is_ ready to complete the stage
* `completed` - serialized component, text displayed in the task panel if the player has completed the stage. Optional; defaults to "Stage Completed!"
* `required_stage` - a string; the game stage the player must have to complete this echo stage
* `shop_unlock` - required, but may be empty; a list of shop entries to unlock once the stage is completed; see [Shop Entries](#shop-entries) below
* `completion_reward` - optional one-time reward(s) granted to players when they complete the stage; see [Completion Rewards](#completion-rewards) below 

### Lore Entry Components

Defines the text, image and/or audio clips that make up the lore for one stage.

* `type` - required, must be one of `ftbechoes:text`, `ftbechoes:image`, or `ftbechoes:audio`

For `ftbechoes:text`:

* `text` - a serialized component

For `ftbechoes:image`:

* `location` - the resource location for an image, via the resource manager
* `width` - optional image width, in pixels. Defaults to 32.
* `height` - optional image height, in pixels. Defaults to 32.
* `align` - optional image alignment (`left`, `center` or `right`). Defaults to `left`.

For `ftbechoes:audio`:

* `location` - the resource location for an audio clip, which is a Minecraft sound event name

### Shop Entries

Shop entries define items that may be bought, and/or commands that may be run (with a configurable permission level) on behalf of the player, along with an associated cost in the currency system currently in use. The player must have completed the associated echo stage to be able to buy these items. 

Fields:

* `name` - a string to identify this entry, must be unique within this particular Echo
* `item` - a serialized itemstack, or list of itemstacks, possibly including component data. If omitted, then `description` and `command` must be specified.
  * This is exclusive with the `command` field.
* `cost` - an integer cost, must be > 0
* `description` - a serialized component; may be omitted if `item` is specified but overrides the item description if present.
* `icon` - the resource location for an icon image; may be omitted if `item` is specified
* `command` - a json object describing a command to run on behalf of the player; see [Command Entries](#command-entries) below
  * This is exclusive with the `item` field.

### Completion Rewards

Completion rewards define a set of rewards granted to a player when they complete a stage. This includes one or more of an item, some experience, some currency, or a command to be run on behalf of the player (e.g. grant them a game stage)

All fields below are optional, but at least of one `item`, `experience`, `currency` or `command` must be specified.

Fields:

* `item` - a serialized itemstack, or list of itemstacks, to give to the player
* `experience` - an integer quantity of experience for the player
* `currency` - an integer quantity of currency for the player
* `command` - a json object describing a command to run on behalf of the player; see [Command Entries](#command-entries) below
* `description` - an optional serialized component, or list of serialized components, used for tooltip purposes on the "Claim Reward" button
  * This is recommended if there is a command component to this reward

### Command Entries

Command entries define a string command, with an optional permission level and silent field (to suppress normal command output)

Fields:

* `run` - the command string, mandatory
* `permission` - the integer permission level in range 0..4, optional (default: 0)
* `silent` - a boolean to suppress normal command output, optional (default: true)

## Support

- For **Modpack** issues, please go here: https://go.ftb.team/support-modpack
- For **Mod** issues, please go here: https://go.ftb.team/support-mod-issues
- Just got a question? Check out our Discord: https://go.ftb.team/discord

## Licence

All Rights Reserved to Feed The Beast Ltd. Source code is `visible source`, please see our [LICENSE.md](/LICENSE.md) for more information. Any Pull Requests made to this mod must have the CLA (Contributor Licence Agreement) signed and agreed to before the request will be considered.

## Keep up to date

[![](https://cdn.feed-the-beast.com/assets/socials/icons/social-discord.webp)](https://go.ftb.team/discord) [![](https://cdn.feed-the-beast.com/assets/socials/icons/social-github.webp)](https://go.ftb.team/github) [![](https://cdn.feed-the-beast.com/assets/socials/icons/social-twitter-x.webp)](https://go.ftb.team/twitter) [![](https://cdn.feed-the-beast.com/assets/socials/icons/social-youtube.webp)](https://go.ftb.team/youtube) [![](https://cdn.feed-the-beast.com/assets/socials/icons/social-twitch.webp)](https://go.ftb.team/twitch) [![](https://cdn.feed-the-beast.com/assets/socials/icons/social-instagram.webp)](https://go.ftb.team/instagram) [![](https://cdn.feed-the-beast.com/assets/socials/icons/social-facebook.webp)](https://go.ftb.team/facebook) [![](https://cdn.feed-the-beast.com/assets/socials/icons/social-tiktok.webp)](https://go.ftb.team/tiktok)
