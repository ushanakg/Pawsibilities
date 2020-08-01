Original App Design Project - README
===

# Pawsibilities

## Table of Contents
1. [Overview](#Overview)
1. [Product Spec](#Product-Spec)
1. [Wireframes](#Wireframes)
2. [Schema](#Schema)
2. [Developer](#Developer)
2. [Tester](#Tester)

## Overview
### Description
This app allows dog-lovers to find dogs to pet near them, and also allows users to tag any dogs that they have spotted in the vicinity so that other users in the area can also meet those furry friends.

### App Evaluation
- **Category:** Social Networking, Animal, Lifestyle
- **Mobile:** Mobile only.
 - **Story:** Users can crowdsource information about where and when in a neighborhood dogs have been spotted so that users can use this information to go find dogs near them to pet/play with. Tags will include location and timestamp, but can also include breed of dog, a photo, and other information to help inform users' choices to go meet a given dog.
 - **Market:** Anybody who loves dogs and would love to meet dogs around their neighborhood or anybody who meets a dog and wants other to be able to pet it as well.
 - **Habit:** People who are walking/cycling to their next destination will check this app to see if there are any furry friends on the way that they can stop to say hi to. Users with dogs who need a buddy for their pet to play with may also check this app. 
 - **Scope:** V1 would include the ability to create a tag with a dog's info, location, time, and the expected time for a dog to remain there. Could also include the ability for another user to update the tag if the dog has moved. V2 would allow users to connect to create a social network, so now any tags created will be actively broadcast to your social network. In addition, the app will use your location and a radius that you set in order to notify you when a dog in that area has been tagged. 

## Product Spec

### 1. User Stories (Required and Optional)

**Required Must-have Stories**
* Configure a Parse database to store the app's data
* Users can log in
* User can log out
* User can sign up to create a new account
* User's session persists across app restarts
* App integrates with Google Maps SDK to show the user all tags on a map 
* Users can create a tag (through a separate view) at their current location that includes the dog's name, photo, and a direction of travel
  * When the user adds a tag, the tag "drops" to the map (animation)
* Clicking on a tag allows the user to view details such as name of the dog, photo, when the tag was last updated, the direction of travel, and how far away the tag is from the user
* Users can update tags (through a separate view) dropped by others: e.g. marking tags as invalid and changing the tag's location
* Tags on the map in the visible map view are loaded when map is opened/moved
* Users can view a list view of tags, sorted in ascending order by the amount of time it would take to walk to the tag
  * This list can be refreshed by swiping, and infinitely scrolls
* Users can double tap to launch the camera and take a picture for their profile.
* Incorporates an external library to add visual polish

**Optional Nice-to-have Stories**
* When signing in, the user's password is hidden
* Floating action button which allows users to create tags is revealed and hidden with a circular reveal animation
* Creation/edit/detailed tag views are modal overlays instead of separate activities
  * These modal overlays open with a circular reveal animation
* Users can see the username and profile picture of the user that has dropped the tag
* Users can edit their profile
    * Users can select/take their profile photo
    * User can change their notification radius
* In addition to the bottom navigation view, users can swipe between the three main screens
* Users can see the app's launcher icon on their device's home screen
* Users can see the app's logo on the landing page of the app
* User profile's can show how many tags a user has dropped as well as the distance of the nearest tag from them
* Users are notified when a tag within a given radius of their current location is dropped
    * Notification radius is made visible on the map 

### 2. Screen Archetypes

* Log In
   * Users can log in
* Sign Up
   * User can sign up to create a new account
* Map View
    * Pinch to zoom in/out on the map gesture implemented
    * Tags within the visible area on the map view are loaded and placed on the map
    * Users are notified when a tag within 2 miles of their current location is posted
* Profile
    * User can edit details such as their profile photo and notification radius
    * User can log out
* Drop/Compose Tag
    * Users can drop a tag at their current location that includes a timestamp, photo, and a direction of travel
* Detailed View/Edit Tag
    * Users can update tags dropped by others
* List View
    * Users can view a list of tags sorted by least to greatest distance from the user's current location

### 3. Navigation

**Tab Navigation** (Tab to Screen)

* Map View
* List View
* Profile

**Flow Navigation** (Screen to Screen)

* Log In
   * => Map View
   * => Sign Up
* Sign Up
   * => Map View
* Map View
    * => Drop/Compose Tag 
    * => Edit Tag 
* Drop/Compose Tag
    * Map View (going back after creating tag)
* Detailed View/Edit Tag
    * Map View (going back after editing tag)
    * List View (going back after viewing tag)
* List View
    * => Detailed/Edit Tag

## Wireframes
[Add picture of your hand sketched wireframes in this section]
<img src="https://github.com/ushanakg/Pawsibilities/blob/master/wireframe.jpg" width=600>

## Schema 
### Models
**User**
| Property | Type | Description |
|----------|------|-------------|
|username |String|username for user to login|
|password|String|password for user to login|
|createdAt|DateTime|date when post is created (default field)|
|updatedAt|DateTime|date when post is last updated (default field)|
|Radius|Number|radius within which user gets notified for tags dropped (stretch goal)|

**Tag**
| Property | Type | Description |
|----------|------|-------------|
|objectId|String|unique id for the user that dropped the tag (default field)|
|location|Location|current location of the user|
|createdAt|DateTime|date when post is created (default field)|
|updatedAt|DateTime|date when post is last updated (default field)|
|image|File|image of the dog that is being tagged|
|name|String|name of the dog being tagged (optional)|
|type|String|type/breed of the dog being tagged (optional)|
|direction|String|the direction of travel of the dog being tagged|
|expired|Boolean|true if the tag no longer accurately describes the dog's location (as determined by other users)|


### Networking
* Log In
   * (Get/REQUEST) authenticate username and password
* Sign Up
   * (Create/POST) new user object
* Map View
    * (Get/REQUEST) query tags to display them on the map
* List View
    * (GET/REQUEST) query tags to sort and display them in a RecyclerView
* Profile
    * (Update/POST) update radius preference
* Drop/Compose Tag
    * (Create/POST) create a new tag
    * (Get/REQUEST) user's current location 
* Detailed View/Edit Tag
    * (Update/POST) update location or expiration status


### Developer (using Android Studio)
Pawsibilities is built in Android Studio using Gradle 6.1.1 with Java 1.8 or Java 11.
- To copy the project into desired directory, enter desired directory in terminal and run: git clone https://github.com/ushanakg/Pawsibilities.git
- To invoke a build and run tests: ./gradlew build
- To skip tests: ./gradlew build -x test
Pawsibilities is contained in a singular module:
- pawsibilities-app contains the utility classes needed for the app's functionality

### Tester
In order to download and install Pawsibilities from Github you must first enable Developer Options on your phone.
- Go to Settings > System > About phone.
- Tap Software info > Build number.
- Tap Build number seven times. You may also have to tap in your PIN for verification.
- Go back to the Settings pane, where you will now find Developer options as an entry.
- Tap it and toggle the switch on if it is not already.

To install Pawsibilities, view this Github repository from your device. Under "Releases", click the link of the latest release to install that compiled APK. Navigate through any prompts asking you to confirm download and installation. 
