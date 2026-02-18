import java.util.*;

// =====================================================================
//  PROJECT: Social Network Backend System
//  Language: Java (Console-based)
//  Data Structures: Graph (Adjacency List), HashMap, BFS, DFS,
//                   Stack, Queue, ArrayList, Sorting
// =====================================================================


// ---------------------------------------------------------------------
//  User — stores profile data
// ---------------------------------------------------------------------
class User {
    private String userId;
    private String name;
    private int    age;
    private String email;

    public User(String userId, String name, int age, String email) {
        this.userId = userId;
        this.name   = name;
        this.age    = age;
        this.email  = email;
    }

    public String getUserId() { return userId; }
    public String getName()   { return name;   }
    public int    getAge()    { return age;     }
    public String getEmail()  { return email;   }

    @Override
    public String toString() {
        return "[ID: " + userId + " | Name: " + name
             + " | Age: " + age + " | Email: " + email + "]";
    }
}


// ---------------------------------------------------------------------
//  Message — stored inside the message Queue
// ---------------------------------------------------------------------
class Message {
    private String fromId;
    private String toId;
    private String content;

    public Message(String fromId, String toId, String content) {
        this.fromId  = fromId;
        this.toId    = toId;
        this.content = content;
    }

    @Override
    public String toString() {
        return "From: " + fromId + "  ->  To: " + toId + "  |  \"" + content + "\"";
    }
}


// ---------------------------------------------------------------------
//  SocialGraph — Adjacency List + all graph algorithms
// ---------------------------------------------------------------------
class SocialGraph {

    // userId -> list of connected friend userIds
    private HashMap<String, ArrayList<String>> adjList;

    public SocialGraph() {
        adjList = new HashMap<>();
    }

    // Add a user node — O(1)
    public void addVertex(String userId) {
        adjList.putIfAbsent(userId, new ArrayList<>());
    }

    // Remove a user node and all its edges — O(V + E)
    public void removeVertex(String userId) {
        adjList.remove(userId);
        for (ArrayList<String> neighbors : adjList.values()) {
            neighbors.remove(userId);
        }
    }

    // Add undirected friendship edge — O(1)
    public void addEdge(String u, String v) {
        adjList.get(u).add(v);
        adjList.get(v).add(u);
    }

    // Remove undirected friendship edge — O(degree)
    public void removeEdge(String u, String v) {
        adjList.get(u).remove(v);
        adjList.get(v).remove(u);
    }

    // Return friend list of a user — O(1)
    public ArrayList<String> getFriends(String userId) {
        return adjList.getOrDefault(userId, new ArrayList<>());
    }

    public boolean hasVertex(String userId) {
        return adjList.containsKey(userId);
    }

    public boolean hasEdge(String u, String v) {
        return adjList.containsKey(u) && adjList.get(u).contains(v);
    }

    public Set<String> getAllUsers() {
        return adjList.keySet();
    }

    // BFS — mutual friends: intersection of both friend lists — O(dA + dB)
    public ArrayList<String> getMutualFriends(String userA, String userB) {
        ArrayList<String> mutual = new ArrayList<>();
        HashSet<String>   setB   = new HashSet<>(getFriends(userB));
        for (String f : getFriends(userA)) {
            if (setB.contains(f)) mutual.add(f);
        }
        return mutual;
    }

    // BFS Level-2 — friends-of-friends not yet connected to userId — O(V + E)
    public ArrayList<String> suggestFriends(String userId) {
        ArrayList<String>        suggestions = new ArrayList<>();
        HashMap<String, Integer> visited     = new HashMap<>();
        Queue<String>            queue       = new LinkedList<>();

        visited.put(userId, 0);
        queue.add(userId);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            int    depth   = visited.get(current);
            if (depth >= 2) continue;

            for (String neighbor : getFriends(current)) {
                if (!visited.containsKey(neighbor)) {
                    visited.put(neighbor, depth + 1);
                    queue.add(neighbor);
                    // Level-2 node not already a direct friend → suggestion
                    if (depth + 1 == 2 && !getFriends(userId).contains(neighbor)) {
                        suggestions.add(neighbor);
                    }
                }
            }
        }
        return suggestions;
    }

    // DFS — find all connected components (user groups) — O(V + E)
    public ArrayList<ArrayList<String>> getConnectedComponents() {
        ArrayList<ArrayList<String>> components = new ArrayList<>();
        HashSet<String>              visited    = new HashSet<>();

        for (String userId : adjList.keySet()) {
            if (!visited.contains(userId)) {
                ArrayList<String> component = new ArrayList<>();
                dfs(userId, visited, component);
                components.add(component);
            }
        }
        return components;
    }

    // Recursive DFS helper
    private void dfs(String userId, HashSet<String> visited, ArrayList<String> component) {
        visited.add(userId);
        component.add(userId);
        for (String neighbor : getFriends(userId)) {
            if (!visited.contains(neighbor)) dfs(neighbor, visited, component);
        }
    }

    // Sorting — users ranked by friend count descending — O(V log V)
    public ArrayList<Map.Entry<String, Integer>> getSortedByConnections() {
        ArrayList<Map.Entry<String, Integer>> list = new ArrayList<>();
        for (Map.Entry<String, ArrayList<String>> e : adjList.entrySet()) {
            list.add(new AbstractMap.SimpleEntry<>(e.getKey(), e.getValue().size()));
        }
        list.sort((a, b) -> b.getValue() - a.getValue());
        return list;
    }
}


// ---------------------------------------------------------------------
//  SocialNetwork — service layer that wires everything together
// ---------------------------------------------------------------------
class SocialNetwork {

    private HashMap<String, User> userProfiles;   // userId → User
    private SocialGraph           graph;
    private Stack<String>         recentlyViewed; // LIFO: last viewed on top
    private Queue<Message>        messageQueue;   // FIFO: oldest message first

    public SocialNetwork() {
        userProfiles   = new HashMap<>();
        graph          = new SocialGraph();
        recentlyViewed = new Stack<>();
        messageQueue   = new LinkedList<>();
    }

    // 1. Add user — O(1)
    public void addUser(String userId, String name, int age, String email) {
        if (userProfiles.containsKey(userId)) {
            System.out.println("  [ERROR] User ID '" + userId + "' already exists.");
            return;
        }
        User u = new User(userId, name, age, email);
        userProfiles.put(userId, u);
        graph.addVertex(userId);
        System.out.println("  [OK] User added: " + u);
    }

    // 2. Delete user — O(V + E)
    public void deleteUser(String userId) {
        if (!check(userId)) return;
        userProfiles.remove(userId);
        graph.removeVertex(userId);
        System.out.println("  [OK] User '" + userId + "' deleted.");
    }

    // 3. Add friendship — O(1)
    public void addFriend(String a, String b) {
        if (!checkBoth(a, b)) return;
        if (graph.hasEdge(a, b)) { System.out.println("  [INFO] Already friends."); return; }
        graph.addEdge(a, b);
        System.out.println("  [OK] Friendship added: " + a + " <---> " + b);
    }

    // 4. Remove friendship — O(degree)
    public void removeFriend(String a, String b) {
        if (!checkBoth(a, b)) return;
        if (!graph.hasEdge(a, b)) { System.out.println("  [INFO] Not friends."); return; }
        graph.removeEdge(a, b);
        System.out.println("  [OK] Friendship removed: " + a + " and " + b);
    }

    // 5. Display all friends — O(degree)
    public void displayFriends(String userId) {
        if (!check(userId)) return;
        ArrayList<String> friends = graph.getFriends(userId);
        System.out.println("  Friends of " + userId + " [" + friends.size() + " total]:");
        if (friends.isEmpty()) { System.out.println("    (None)"); return; }
        for (String fid : friends) System.out.println("    -> " + userProfiles.get(fid));
    }

    // 6. Mutual friends via BFS — O(dA + dB)
    public void showMutualFriends(String a, String b) {
        if (!checkBoth(a, b)) return;
        ArrayList<String> mutual = graph.getMutualFriends(a, b);
        System.out.println("  Mutual friends [" + mutual.size() + "] between " + a + " and " + b + ":");
        if (mutual.isEmpty()) { System.out.println("    (None)"); return; }
        for (String mid : mutual) System.out.println("    -> " + userProfiles.get(mid));
    }

    // 7. Suggest friends via BFS level-2 — O(V + E)
    public void suggestFriends(String userId) {
        if (!check(userId)) return;
        ArrayList<String> sugg = graph.suggestFriends(userId);
        System.out.println("  Suggestions for " + userId + " [" + sugg.size() + "]:");
        if (sugg.isEmpty()) { System.out.println("    (No suggestions)"); return; }
        for (String sid : sugg) System.out.println("    -> " + userProfiles.get(sid));
    }

    // 8. Connected groups via DFS — O(V + E)
    public void showConnectedGroups() {
        ArrayList<ArrayList<String>> groups = graph.getConnectedComponents();
        System.out.println("  Connected Groups [" + groups.size() + " found]:");
        int i = 1;
        for (ArrayList<String> group : groups) {
            System.out.print("    Group " + i++ + ": ");
            for (String uid : group) {
                User u = userProfiles.get(uid);
                System.out.print((u != null ? u.getName() : uid) + "  ");
            }
            System.out.println();
        }
    }

    // 9. View profile — pushes to Stack — O(1)
    public void viewProfile(String userId) {
        if (!check(userId)) return;
        User u = userProfiles.get(userId);
        recentlyViewed.push(userId);
        System.out.println("  " + u);
        System.out.println("  Friends: " + graph.getFriends(userId).size());
    }

    // Show recently viewed Stack — O(n)
    public void showRecentlyViewed() {
        if (recentlyViewed.isEmpty()) { System.out.println("  (None)"); return; }
        System.out.println("  Recently Viewed [most recent first]:");
        Stack<String> copy = new Stack<>();
        copy.addAll(recentlyViewed);
        int rank = 1;
        while (!copy.isEmpty()) {
            String uid = copy.pop();
            User u = userProfiles.get(uid);
            System.out.println("    " + rank++ + ". " + (u != null ? u.getName() : uid));
        }
    }

    // 10. Send message — enqueue — O(1)
    public void sendMessage(String fromId, String toId, String content) {
        if (!checkBoth(fromId, toId)) return;
        messageQueue.add(new Message(fromId, toId, content));
        System.out.println("  [OK] Message queued.");
    }

    // Deliver (dequeue) next message — O(1)
    public void processNextMessage() {
        if (messageQueue.isEmpty()) { System.out.println("  [INFO] Queue is empty."); return; }
        System.out.println("  [DELIVERED] " + messageQueue.poll());
    }

    // Show all messages in queue
    public void showMessageQueue() {
        if (messageQueue.isEmpty()) { System.out.println("  (Queue is empty)"); return; }
        System.out.println("  Pending Messages [" + messageQueue.size() + "]:");
        int i = 1;
        for (Message m : messageQueue) System.out.println("    " + i++ + ". " + m);
    }

    // 11. Most connected user — sorting — O(V log V)
    public void showMostConnectedUser() {
        if (userProfiles.isEmpty()) { System.out.println("  (No users)"); return; }
        ArrayList<Map.Entry<String, Integer>> sorted = graph.getSortedByConnections();
        System.out.println("  Users by connection count:");
        for (Map.Entry<String, Integer> e : sorted) {
            User u = userProfiles.get(e.getKey());
            System.out.println("    " + (u != null ? u.getName() : e.getKey())
                    + "  ->  " + e.getValue() + " friend(s)");
        }
        Map.Entry<String, Integer> top = sorted.get(0);
        System.out.println("  >>> Most Connected: "
                + userProfiles.get(top.getKey()).getName()
                + " (" + top.getValue() + " friends)");
    }

    // List all users
    public void listAllUsers() {
        if (userProfiles.isEmpty()) { System.out.println("  (No users)"); return; }
        System.out.println("  All Users [" + userProfiles.size() + "]:");
        for (User u : userProfiles.values()) System.out.println("    " + u);
    }

    // Validators
    private boolean check(String userId) {
        if (!userProfiles.containsKey(userId)) {
            System.out.println("  [ERROR] User '" + userId + "' not found.");
            return false;
        }
        return true;
    }

    private boolean checkBoth(String a, String b) {
        return check(a) && check(b);
    }
}


// ---------------------------------------------------------------------
//  SocialNetworkSystem — main class, menu UI
// ---------------------------------------------------------------------
public class SocialNetworkSystem {

    static Scanner       sc = new Scanner(System.in);
    static SocialNetwork sn = new SocialNetwork();

    public static void main(String[] args) {
        System.out.println("============================================================");
        System.out.println("        SOCIAL NETWORK BACKEND SYSTEM  —  DSA Project      ");
        System.out.println("============================================================");
        loadSampleData();

        boolean running = true;
        while (running) {
            printMenu();
            String choice = sc.nextLine().trim();
            switch (choice) {
                case "1":  addUser();                   break;
                case "2":  deleteUser();                break;
                case "3":  addFriend();                 break;
                case "4":  removeFriend();              break;
                case "5":  displayFriends();            break;
                case "6":  mutualFriends();             break;
                case "7":  suggestFriends();            break;
                case "8":  sn.showConnectedGroups();    break;
                case "9":  viewProfile();               break;
                case "10": sn.showRecentlyViewed();     break;
                case "11": sendMessage();               break;
                case "12": sn.processNextMessage();     break;
                case "13": sn.showMessageQueue();       break;
                case "14": sn.showMostConnectedUser();  break;
                case "15": sn.listAllUsers();           break;
                case "0":
                    System.out.println("  Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("  [ERROR] Invalid option.");
            }
            System.out.println();
        }
    }

    static void printMenu() {
        System.out.println("------------------------------------------------------------");
        System.out.println("   1.  Add User                  2.  Delete User");
        System.out.println("   3.  Add Friend                4.  Remove Friend");
        System.out.println("   5.  Display Friends           6.  Mutual Friends (BFS)");
        System.out.println("   7.  Suggest Friends (BFS L2)  8.  Connected Groups (DFS)");
        System.out.println("   9.  View Profile (Stack)      10. Recently Viewed");
        System.out.println("   11. Send Message (Queue)      12. Process Next Message");
        System.out.println("   13. Show Message Queue        14. Most Connected User");
        System.out.println("   15. List All Users             0. Exit");
        System.out.println("------------------------------------------------------------");
        System.out.print("   Choice: ");
    }

    static void addUser() {
        System.out.print("  User ID : "); String uid = sc.nextLine().trim();
        System.out.print("  Name    : "); String nm  = sc.nextLine().trim();
        System.out.print("  Age     : ");
        int age = 0;
        try { age = Integer.parseInt(sc.nextLine().trim()); }
        catch (NumberFormatException e) { sc.nextLine(); }
        System.out.print("  Email   : "); String em = sc.nextLine().trim();
        sn.addUser(uid, nm, age, em);
    }

    static void deleteUser() {
        System.out.print("  User ID: ");
        sn.deleteUser(sc.nextLine().trim());
    }

    static void addFriend() {
        System.out.print("  User A ID: "); String a = sc.nextLine().trim();
        System.out.print("  User B ID: "); String b = sc.nextLine().trim();
        sn.addFriend(a, b);
    }

    static void removeFriend() {
        System.out.print("  User A ID: "); String a = sc.nextLine().trim();
        System.out.print("  User B ID: "); String b = sc.nextLine().trim();
        sn.removeFriend(a, b);
    }

    static void displayFriends() {
        System.out.print("  User ID: ");
        sn.displayFriends(sc.nextLine().trim());
    }

    static void mutualFriends() {
        System.out.print("  User A ID: "); String a = sc.nextLine().trim();
        System.out.print("  User B ID: "); String b = sc.nextLine().trim();
        sn.showMutualFriends(a, b);
    }

    static void suggestFriends() {
        System.out.print("  User ID: ");
        sn.suggestFriends(sc.nextLine().trim());
    }

    static void viewProfile() {
        System.out.print("  User ID: ");
        sn.viewProfile(sc.nextLine().trim());
    }

    static void sendMessage() {
        System.out.print("  From ID  : "); String from = sc.nextLine().trim();
        System.out.print("  To ID    : "); String to   = sc.nextLine().trim();
        System.out.print("  Message  : "); String msg  = sc.nextLine().trim();
        sn.sendMessage(from, to, msg);
    }

    // Pre-loaded sample data
    static void loadSampleData() {
        System.out.println("  Loading sample data...\n");
        sn.addUser("u1", "Alice",   22, "alice@mail.com");
        sn.addUser("u2", "Bob",     24, "bob@mail.com");
        sn.addUser("u3", "Charlie", 21, "charlie@mail.com");
        sn.addUser("u4", "Diana",   23, "diana@mail.com");
        sn.addUser("u5", "Eve",     25, "eve@mail.com");
        sn.addUser("u6", "Frank",   27, "frank@mail.com"); // isolated node

        sn.addFriend("u1", "u2"); // Alice   — Bob
        sn.addFriend("u1", "u3"); // Alice   — Charlie
        sn.addFriend("u2", "u4"); // Bob     — Diana
        sn.addFriend("u3", "u4"); // Charlie — Diana
        sn.addFriend("u4", "u5"); // Diana   — Eve

        sn.sendMessage("u1", "u2", "Hey Bob!");
        sn.sendMessage("u3", "u1", "Let's connect!");

        System.out.println("\n  Done. Try options 6, 7, 8, 14 for a quick demo.\n");
    }
}
