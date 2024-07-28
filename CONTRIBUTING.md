# Contributing to Bell Claims

So you'd like to contribute to the Bell Claims project? Well you're in luck because we're a little short on developers... or generally just anyone ranging from designers, testers, QA, translaters, you name it. If you've got any experience in Kotlin and a passion to make cool Minecraft server stuff, feel free to join the club so long as you're willing to accept the few and loose guidelines.

## Want to talk?
There's no official team based communication right now, but you can contact me through either:
- Mastodon [@mizarc@mastodon.gamedev.place](https://mastodon.gamedev.place/@mizarc)
- Twitter (X) [@MizarcGames](https://twitter.com/MizarcGames)

If you need any help with Bukkit/Spigot/PaperMC development, try out the forums:
https://forums.papermc.io/


## How Do I Contribute?

### Bug Reporting
The easiest way to contribute to the project without programming experience is to simply report bugs on the [issue page](https://gitlab.com/Mizarc/BellClaims/-/issues). Be sure to follow the issue reporting guidelines, providing as much information as possible so as to reduce confusion and speed up the bug squashing process.

### Feature Requesting
As with bug reporting, you may request features on the [issue page](https://gitlab.com/Mizarc/BellClaims/-/issues). Your requested features must be something that can be managable to implement in a reasonable amount of time, while also being something that people will regularly use. Please describe the necessary use cases and be as detailed as possible.

### Code Contribution
If you've had a look at the feature request or bug report page and want to contribute before I get the time to, you may fork the project and send a merge request to potentially have your changes added. If you're new to git, here's a general rundown:

1. Fork the repo from this project
2. Clone your forked project onto your own device..
3. Work on changes on your copied repo as you see fit.
4. Submit a merge request back to this project.

As for being on the team to be able to contribute directly, contact me first and we'll discuss appropriate arrangements.


## Project Structure

### What do all the branches mean?
Branches are laid out in a pretty straightforward way.
- `main` holds the lastest development release. Ensure that it is able to be run at all costs. Do not commit to this directly.
- `feature/` prefix is for adding new or improving upon existing features.
- `tweak/` prefix is for tweaking existing functionality values.
- `fix/` prefix is for non-urgent bugs or bugs that may take an extended time to repair.
- `hotfix/` prefix is for urgent bugs that should be worked on ASAP and sent to the next patch release.
- `release/` prefix is for maintaining an older feature release, usually to send out bug fixes.

### Why are the folders organised the way they are?
This is just how I've laid out the project based on my own expertise. If you know of a better and more intuitive project structure, feel free to teach me all about it.
- `api` is where all interactions should go through. This contains all the interfaces for communicating with the application.
- `domain` holds the data and defines how objects are to be persisted.
- `infrastructure` is the bread and butter. All implementations of services are repositories go here.
- `interaction` is the basis of user interaction. Listens for player actions, creates menus, etc.
- `utils` is for miscellaneous floating functions.


## Style Guidelines

### Git Messages
- Use an imperative, present tense style (Say "Fix bug" not "Fixed bug").
- Be descriptive, explain why the changes were made and what was implemented to do so.
- Limit the subject line to 50 characters.
- Add a blank line between the subject line and the commit message body.
- Limit the message body to 72 characters per line.

### Kotlin
This project simply follows the [Official Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html) page. If you want to know what best to do when unsure, just give that a good read. Anything else is free reign aside from these few project specific guidelines:
- A hard limit of 120 lines is in place. Avoid exceeding this and attempt to wrap elegantly.
