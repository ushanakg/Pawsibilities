Original App Design Project - README
===

# Pawsibilities

## Table of Contents
1. [Overview](#Overview)
1. [Product Spec](#Product-Spec)
1. [Wireframes](#Wireframes)
2. [Schema](#Schema)

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

* Users can log in
* User can log out
* User can sign up to create a new account
* Users can drop a tag at their current location that includes a timestamp, photo, and a direction of travel
* Users can update tags dropped by others
* Pinch to zoom in/out on the map gesture implemented
    * Tags within a 2 mile radius are present/visible on the map view
*

**Optional Nice-to-have Stories**
* Users are notified when a tag within 2 miles of their current location is posted
    * User can change their notification radius
    * Notification radius is made visible on the map 
* All tags that fit in the map view are visible
* Users can connect with each other receive a notification every time a user in your network drops a tag
* Users can add a photo/more details to their profile
    * Users can edit their profile

### 2. Screen Archetypes

* Log In
   * Users can log in
* Sign Up
   * User can sign up to create a new account
* Map View
    * Pinch to zoom in/out on the map gesture implemented
    * Tags within a 2 mile radius are present/visible on the map view
    * Users are notified when a tag within 2 miles of their current location is posted
* Profile
    * User can log out
* Drop/Compose Tag
    * Users can drop a tag at their current location that includes a timestamp, photo, and a direction of travel
    * Users can update tags dropped by others

### 3. Navigation

**Tab Navigation** (Tab to Screen)

* Profile
* Map View

**Flow Navigation** (Screen to Screen)

* Log In
   * => Map View
   * => Sign Up
* Sign Up
   * => Map View
* Map View
    * => Drop/Compose Tag (modal overlay?)
* Drop/Compose Tag
    * Map View (going back after creating tag)

## Wireframes
[Add picture of your hand sketched wireframes in this section]
<img src="https://github.com/ushanakg/Pawsibilities/blob/master/wireframe.jpg" width=600>

### [BONUS] Digital Wireframes & Mockups

### [BONUS] Interactive Prototype

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
    * (Update/POST) update location or expiration status on a tag
* Profile
    * (Update/POST) update radius preference
* Drop/Compose Tag
    * (Create/POST) create a new tag
    * (Get/REQUEST) user's current location
    * (Delete) delete a tag
    
- [Create basic snippets for each Parse network request]
- [OPTIONAL: List endpoints if using existing API such as Yelp]
