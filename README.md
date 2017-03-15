# Twitch-Bot-V2
An improved version of my original attempt at a Twitch IRC bot. This bot is being built to join a streamers channel chat on Twitch.tv, providing basic moderation, information, and a little bit of fun to the viewers. Eventually it will have a full GUI, just like the last version, making it easy for the streamer to control exactly what they want the bot to do.

The original version was started back when I was still taking my introductory object-oriented classes. With no dependency management and lack of planning, it quickly became difficult to add new features. This version, although still in progress, now has dependency management through Maven, and I have a much better plan of what I want it to do. I also have begun to incorporate an embedded database to better manage the data the bot gathers about the viewers.

### Some Primary Features
* Basic spam protection, with a strike system and the ability to pardon specific users (eg. so that they can post a one time link)
* Storage of viewing data for users, such as how much total time they've spent watching
* Ability to create custom commands for members of the chat to call and get information
* Viewer currency that they can use for gambling/games/calling commands
* Fun tools for the streamer such as raffles, polls, auctions, and queues.
