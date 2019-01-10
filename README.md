# Burning Ember (BEATs)
*Burning Ember* is a CMS-like application meant as a playlist creation system backed by an archive of previously played tracks. Opposed to a 'real' Content Management System, Burning Ember is **offline** and **fairly simple**. Making it easy for podcasters, dj's, radio show hosts and the like to have a **highly reliable** way to create playlists and keep track of them during a show. 

**More importantly, it's entirely free**. And i do really mean free. For those of you that have heard of FOSS: *Burning Ember* is both **Free-as-in-beer AND Free-as-in-speech**.

## License/FOSS
This project is licensed under the Apache version 2.0 License (See LICENSE file).


## The idea behind this thing:
*Burning Ember* makes keeping an archive and creating playlists as easy and misery free as possible. Making your life just that bit less bad. Keep in mind the [most important natural law of the universe](https://en.wikipedia.org/wiki/The_law_of_conservation_of_misery) though.
* **Crash Resilient**, using the project on hardware that regularly crashes is perfectly doable. The application just continues where you left off.
* **Lightweight**, no more swearing because the application is too slow. For use on old or slow laptops.
* **Portable**, just use it on whatever platform you like (supports most OSes).
* **Ease of Use**, even your grandpa should be able to get the hang of it.

## The History (because appearantly, there is some of that too):
This software is based upon the original implementations of the *WWDB project*.
The *WWDB project* set out with the same goal as *Burning Ember*,
but was less resilient and less extensive.
When the need for a V2.0 of the *WWDB project* arose, it seemed better to start a build-from-scratch.
This rebuild eventually evolved into *Burning Ember*.

The original *WWDB project* ran for about 3 years (The v1.xx versions).
It was specifically designed for a local radio station who needed such an application.
*Burning Ember* as a successor is build with the same idea in mind.  
As a considerable amount of users ran the program on old and crash prone hardware,
the goals and underlying structure changed when work on *Burning Ember* began.

## A long text, because sometimes I like to write weird things:
The archive ensures that your next playlist has some **consistency**. Whether you want to make sure you're not always playing that same band/song or even the opposite, making sure that a featured artist doesn't get forgotten.  

*Burning Ember* is **portable**: any platform that supports executable jars (Java) can make use of it. So whether you're favorite OS is Windows, Mac or a flavor of Linux or Unix, you can have the same experience across all your machines (Does not support Wi-Fi enabled crock pots or Tamagotchi's). 

On top of being portable, the system is also **lightweight**. Meaning that even old or slow computers have no problems using it. 

The interface and tools are fairly **simplistic** by design. Making it easy for non-technical users to use, reducing or eliminating user error. More knowledgeable users aren't left out though. A set of advanced tools are just a few clicks away, separated from the more standard functions.

The back-end is also engineered in such a way that **prevents loss of data**. Even frequently occurring power outages or crash-prone systems can safely use *Burning Ember*. The database ensures data always reaches the disk first, before continuing with other operations. This means that it doesn't matter what you were doing when a sudden crash or power outage occurred. Only if you were making a change at the exact moment of failure will you lose that particular (part of a) change. On restart, you'll just continue where you left off. 

The previous item essentially removes the need for backups. Yet *Burning Ember* still contains **a backup system in case of hardware faults**. The backups happen on a regular schedule, while keeping the user unaware of them. The system incorporates a little intelligence that decides whether a backup is necessary and acts accordingly. The online backup system doesn't hinder most users, even on very slow system or with large file sizes.

## If you've read everything up until this point, please send me a message. I don't care how much you like to read, nobody makes it this far. I like you, you lunatic!
