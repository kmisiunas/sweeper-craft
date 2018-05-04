# Minecraft sweeper game

## Compiling

 - `compile` compiles the document 
 - `android:package-debug` 
 - `android:test`   run instrumented android unit tests
 - `android:run` install and run the app
 - `android:package-release` package and sign the apk
 
 
## Setup
  
This sequence describes the necessary steps to get the program to compile
 
 - Install SBT 
 - Install [Scala for android](https://github.com/scala-android/sbt-android)
 - Might want to add GPS via [sbt-android](https://github.com/scala-android/sbt-android-gms)
 
## Principles
 
 - Isolate different parts. In order of priority: game, drawing, main menu, new game, settings, high score, badges, tutorial
 - Break in one of the sub-systems must not fail the main app! 
 - Simplicity! The previous code was very convoluted... 
 
## ToDo

 - [x] Game engine in scala
 - [x] Update drawing engine for larger map and new engine
 - [x] Update GameActivity for the new game engine and communication via Actions and action strings
 - [x] Implement settings
 - [x] Redesign main page graphics and integrate with new game graphics 
 - [x] Ads outside game activity and only after 60 min of usage
 - (canceled) Premium version: new textures, no ads, support developers <- maybe replace with 'Disable ads for a day (free)'
 - [x] Highscore integration - use external libraries to reduce coding here 
 - [x] Consider including Badges - they look nice in v3
 - [x] Balance the game and fix bugs
 - [x] Release testing version to everyone who sent emails to me 
 - Translate app to all specified languages 
 - Update marketing descriptions 
 - Bug fixes, balance the game, and release to the public 

### Need Implementation

 - [x] Action checker unrelieble at the moment. maybe move to click based cheker in a new thread?  
 - [x] Maybe optimise drawing or at least sequence of drawing since upon click the screen freezes until redrawn
 - [x] Move zoom:Int into Activity constant list - decide how def centerViewOn( Pos ) works in operator
 - [x] Add extensive user monitoring via Google analytics:
    - [x] Add Texture tracking
    - [x] Add zoom level tracking
 - [x] Decide what action happens SartingTile is clicked => Tutorial! 
 - [x] hash code checking to prevent tampering with the map
 - [x] position in GameState encodes Float relative positions
 - [ ] SaveState seems slow for infinite maps
    - Consider adding loading screen while saving the state
    - maybe Java tries Saving Object variables too?
 - [x] Share on social media - find a library or a common method for doing this.
 - [x] Google Login/Logout via Settings (need to play with Game helper to understand when it does what)
    - [ ] Make sure it does not crash the game at any point. 
    - [x] Test that the behaviour is expectable when user is online and offline
 - [x] Badges synchronise with online (one way for now)
 - [ ] Rate me needs better UI 
 - [ ] Better UI for tutorial
 - [ ] make sure google play descriptions are up to date
 - [x] Settings version injection code for #version and #versionId
 - [ ] consider adding advanced UI: https://github.com/wasabeef/awesome-android-ui
 - [x] consider the minSDKverion 
 - [ ] Classic theme chest needs better graphics 
 - [ ] Rare exception: java.lang.IllegalStateException: Fragment null must be a public static class to be properly recreated from instance state.
 - [ ] Modernise underlying Themes 
 - [ ] Replace banner ads with occasional full screen ad
 
## Tile Mechanism

Encode tiles as Char arrays for efficiency and use maps to move between them. 
Each tile has 1 Byte giving 256 valeus. 

    hidableTiles = 
       1(*empty*) + 8(*numbers*) + 1(*goal*) + 3(*evils*) + 1(*gold*) + 1(*treasure*) + 1(*diamond*) + 1(*steak*)+ 1(*pie*) 
    => 18
    
    combinations = hidableTiles * ( 1(*normal*) + 3(*hiden textures*) * 3(* markings*))
    => 180
    
    unhidableTiles = 
      1(*base*) + 4(*sides*) + 4(*corrners*) + 8(*beyond*) + 1(*tnt*) +  1(*diamond sword*) + 1(*plce holder*) + 1(*error*)
    => 21
     
    total = combinations + unhidableTiles
    => 201
    
 There is enough space. The mapping is specified in GameState object.
 
## Game Modes

 1. **Classic**: one life, no treasures, no extra lives
 2. **Adventure**: 4 lives, with treasures and lives
 3. **Rescue**: find a special tile until score runs negative. 4 lives, with treasures and lives
 
## Identity

**Name** of the game should be restored to **"Minecraft Sweeper"** since Mojang permits some usage of term Minecraft
as described on [Mojang webpage](https://account.mojang.com/documents/brand_guidelines). 
In addition, Minesweeper is trademark as well, thus limiting our name exploration in that direction. 
Regarding the names, we should also appeal ban in UK because it is annoying. 

Should have some sort of forum for users to discuss and propose new features. I would not like to manage it. 

Should I get www.minecraft-sweeper.net and point it to minimal github like page? 

# Productivity

Hours taken so far: 24 + 2 + 5


# Google Play services

## OAuth

Signing certificate fingerprint (SHA1):
AC:49:EB:9B:40:4C:60:09:2D:05:33:64:CD:DA:4B:D0:F7:2F:C4:C5

Application id
181348035390

Debug SHA1:


## Sign In design

Create new activity that asks to sign in. Two options presented: 
play anonymously or Sign in with google. 
Also suggest that you can sign in later via Settings page. 

 
 