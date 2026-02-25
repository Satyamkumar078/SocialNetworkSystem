# Social Network Backend System (DSA Project)

## Overview
The Social Network Backend System is a console-based Java application that simulates the core backend of a social media platform. It demonstrates how real-world social networks manage users, friendships, messaging, and community detection using fundamental Data Structures and Algorithms.
Every feature in this project is backed by a specific data structure chosen for a deliberate reason — not randomly, but because it is the most efficient tool for that job.

## How to Run
```bash
javac SocialNetworkSystem.java
java SocialNetworkSystem

## Project Structure
All code lives in a single file: SocialNetworkSystem.java
It contains 5 classes, each with a clear, focused responsibility:
SocialNetworkSystem.java
│
├── User                  — Stores profile data (ID, name, age, email)
├── Message               — Represents a message stored in the Queue
├── SocialGraph           — Graph engine: Adjacency List + BFS + DFS + Sorting
├── SocialNetwork         — Service layer: all 11 features, input validation
└── SocialNetworkSystem   — Main class: menu UI, input handling, sample data

## Tech Stack
- Java
- HashMap
- Graph (Adjacency List)
- BFS
- DFS
- Stack
- Queue

## Features
## 1. Add User
Registers a new user with a unique ID, name, age, and email. Stored in a HashMap for O(1) retrieval and added as a node in the friendship graph.
## 2. Delete User
Removes the user's profile from the HashMap and deletes their node from the graph along with all friendship edges connected to them.
## 3. Add Friend Connection
Creates an undirected edge between two users in the adjacency list. Both users appear in each other's friend lists.
## 4. Remove Friend Connection
Deletes the edge between two users. Validates that both users exist and are actually friends before removing.
## 5. Display All Friends
Retrieves and prints the complete friend list of a given user directly from the adjacency list.
## 6. Mutual Friends — BFS
Finds friends that two users have in common. Loads the friend list of User A into a HashSet, then checks each friend of User B against it. HashSet lookup is O(1), making the overall operation O(dA + dB).
## 7. Friend Suggestions — BFS Level-2
Runs BFS from the given user up to depth 2. Any node found at exactly level 2 that is not already a direct friend is returned as a suggestion. This is exactly how platforms like LinkedIn and Facebook generate "People You May Know."
## 8. Connected Groups — DFS
Detects all isolated communities in the network. Iterates over every unvisited user and runs a recursive DFS from each, collecting all reachable users into one component. Each DFS call produces one community group.
## 9. View Profile + Recently Viewed Stack
Displays a user's full profile and pushes their ID onto a Stack. Because the Stack is LIFO, calling "Recently Viewed" always shows the most recently browsed profile at the top — the same behavior as a browser's back-history.
## 10. Send Message — Queue
Adds a message object to a LinkedList-based Queue. Messages wait in FIFO order. The "Process Next Message" option dequeues and delivers the oldest message first, just like a real messaging pipeline.
## 11. Most Connected User — Sorting
Extracts all (userId, friendCount) pairs from the adjacency list, sorts them in descending order using a lambda comparator, and displays the full ranked list. The user at the top is the most connected.


## Menu options
 1.  Add User                  2.  Delete User
 3.  Add Friend                4.  Remove Friend
 5.  Display Friends           6.  Mutual Friends (BFS)
 7.  Suggest Friends (BFS L2)  8.  Connected Groups (DFS)
 9.  View Profile (Stack)      10. Recently Viewed
 11. Send Message (Queue)      12. Process Next Message
 13. Show Message Queue        14. Most Connected User
 15. List All Users             0. Exit

## Edge Cases Handled

Adding a user with a duplicate ID is rejected with an error message
Adding a friendship between users who are already friends is caught
Removing a friendship that does not exist is caught
All operations that reference a user first validate that the user exists
Viewing an empty Stack or Queue produces a clean informational message
BFS and DFS handle disconnected graphs correctly (isolated nodes like Frank)


## Possible Future Improvements
Weighted friendships — use edge weights to represent friendship strength, enabling Dijkstra's algorithm for "closest" friend paths
Shortest path between users — BFS from user A to user B to find degrees of separation (Six Degrees of Kevin Bacon problem)
Post and feed system — each user maintains a Deque of posts; followers see a merged feed
Trending topics — HashMap of keyword frequencies, sorted in real time
File persistence — save and reload the graph from a .txt or .json file between sessions
Notification system — Observer design pattern to alert users when they receive a message or friend request
Group/Page feature — model groups as sub-graphs with their own adjacency lists


## Requirements

Java JDK 8 or higher
No external libraries — uses only java.util.*
Single-file compilation — everything is in SocialNetworkSystem.java
