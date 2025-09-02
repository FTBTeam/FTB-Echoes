# FTB Echoes

Use https://github.com/FTBTeam/FTB-Mods-Issues for any mod issues

## Echo Datapack Format

Each Echo is loaded a datapack json file, located in `data/<modid>/echo_definitions/<echo_id>`. E.g. the Echo `ftbechoes:test1` should be defined in `data/ftbechoes/echo_definitions/test1.json`.

### Top Level

Note that whenever "a serialized component" is mentioned below, this is the raw json for a Minecraft text component, as documented [here](https://minecraft.wiki/w/Text_component_format).

See example echo definition [here](https://github.com/FTBTeam/FTB-Echoes/blob/main/src/main/resources/data/ftbechoes/echo_definitions/test_echo1.json).

An Echo json has the following top-level fields, all required:

* `id` - must be unique to the Echo; match the path & filename
* `title` - displayed in the Echo GUI, a serialized component
* `stages` - a list of Echo stages for this echo, see below
* `all_complete` - a message displayed at the end of the Echo GUI lore panel. Optional, defaults to "All Stages Complete!"

### Echo Stages

Echo stages define the progression for an Echo. Fields:

* `title` - optional, displayed in the Lore page as the first text line of a stage if present
* `lore` - a list of lore entry components, see below
* `not_ready` - serialized component, text displayed after the lore if the player isn't ready to proceed
* `ready` - serialized component, text displayed after the lore if the player _is_ ready to proceed
* `required_stage` - a string; the game stage the player must have to complete the stage
* `shop_unlock` - required, but may be empty; a list of shop entries (see below) to unlock once the stage is completed

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

* `location` - the resource location for an audio clip; a sound event name

### Shop Entries

Shop entries define items that may be bought, and/or commands that may be run (with permission level 2) on behalf of the player, along with an associated cost in the currency system currently in use.

Fields:

* `name` - a string to identify this entry, must be unique within this particular Echo
* `item` - a serialized itemstack, possibly including component data. If omitted, then `description` and `command` must be specified.
  * This is exclusive with the `command` field.
* `cost` - an integer cost, must be > 0
* `description` - a serialized component; may be omitted if `item` is specified but overrides the item description if present.
* `icon` - the resource location for an icon image; may be omitted if `item` is specified
* `command` - a command to run on behalf of the player, with the permission level specified in `permission_level`. 
  * This is exclusive with the `item` field.
* `permission_level` - option integer permission level in range 1-4. Defaults to 1.

## Support

- For **Modpack** issues, please go here: https://go.ftb.team/support-modpack
- For **Mod** issues, please go here: https://go.ftb.team/support-mod-issues
- Just got a question? Check out our Discord: https://go.ftb.team/discord

## Licence

All Rights Reserved to Feed The Beast Ltd. Source code is `visible source`, please see our [LICENSE.md](/LICENSE.md) for more information. Any Pull Requests made to this mod must have the CLA (Contributor Licence Agreement) signed and agreed to before the request will be considered.

## Keep up to date

[![](https://cdn.feed-the-beast.com/assets/socials/icons/social-discord.webp)](https://go.ftb.team/discord) [![](https://cdn.feed-the-beast.com/assets/socials/icons/social-github.webp)](https://go.ftb.team/github) [![](https://cdn.feed-the-beast.com/assets/socials/icons/social-twitter-x.webp)](https://go.ftb.team/twitter) [![](https://cdn.feed-the-beast.com/assets/socials/icons/social-youtube.webp)](https://go.ftb.team/youtube) [![](https://cdn.feed-the-beast.com/assets/socials/icons/social-twitch.webp)](https://go.ftb.team/twitch) [![](https://cdn.feed-the-beast.com/assets/socials/icons/social-instagram.webp)](https://go.ftb.team/instagram) [![](https://cdn.feed-the-beast.com/assets/socials/icons/social-facebook.webp)](https://go.ftb.team/facebook) [![](https://cdn.feed-the-beast.com/assets/socials/icons/social-tiktok.webp)](https://go.ftb.team/tiktok)
