## Data Structures Explanation

The application utilizes standard Java Collection Framework structures to manage game state efficiently, prioritizing performance and logical representation of game entities.

### A. Deque (`java.util.ArrayDeque`)
* **Usage:** Used for both the **Draw Pile** (`drawPile`) and the **Discard Pile** (`discard`) in `UnoModel`.
* **Reasoning:**
    * A `Deque` (Double Ended Queue) provides efficient **O(1)** performance for adding and removing elements from the start or end.
    * This perfectly models a stack of cards where players draw from the top (`pop`) or play onto the top (`push`).
    * `ArrayDeque` is faster and more memory-efficient than the legacy `Stack` class.

### B. List (`java.util.ArrayList`)
* **Usage:**
    * **Player Hands:** stored in `UnoPlayer.hand`.
    * **Player List:** stored in `UnoModel.players`.
    * **Views:** stored in `UnoModel.views` (Observer pattern).
* **Reasoning:**
    * `ArrayList` allows for dynamic resizing, which is essential for player hands that grow and shrink throughout the game.
    * It provides **O(1) random access** (by index), which is critical for the Controller. When a player clicks the 3rd card in their GUI hand, the Controller can immediately retrieve `hand.get(2)` without iterating.

### C. Enums (`java.lang.Enum`)
* **Usage:** `UnoColor` and `UnoRank`.
* **Reasoning:**
    * Enums provide type safety, preventing invalid values (e.g., a card cannot have a color "PURPLE_GREEN").
    * They allow for easy grouping of "Light Side" vs. "Dark Side" colors and ranks, simplifying the flipping logic.

### D. Custom Objects (State Management)
* **`UnoCard`:** Designed as a dual-state object. Instead of flipping physically swapping objects, the class holds properties for both sides (`lightRank`, `darkRank`, etc.) and returns the correct one based on the boolean flag `isDark`. This simplifies state management significantly.
* **`UnoEvent`:** An immutable data transfer object used to pass a snapshot of the game state (current player, top card, hand) to the View. This decouples the Model from the View, ensuring the UI cannot accidentally modify the game state.