# Inertia 🧩

A strategic, grid-based sliding puzzle game built entirely in Java Swing. **Inertia** challenges players to navigate a frictionless board, collect gems, and survive deadly mines while competing against a highly optimized, custom-built AI opponent.

## 🚀 Features

* **Continuous Sliding Mechanics:** Move in 8 different directions. Once you move, you slide until you hit a wall or a stop marker.
* **Algorithmic CPU Opponent:** Play against an AI with three distinct difficulty levels, powered by advanced state-space search algorithms.
* **Dynamic Board Generation:** Randomized board layouts with walls, stop markers, gems, mines, and shields, ensuring a unique puzzle every match.
* **Custom Java Swing UI:** Features smooth frame-by-frame particle animations, gradient rendering, and custom 2D graphics without relying on external game engines.

## 🧠 AI & Algorithm Design

The CPU opponent (`Greedy.java`) evaluates millions of potential board states using different algorithmic strategies depending on the difficulty:

* **Easy (Divide & Conquer):** Partitions the board into regions and evaluates the safety and gem density of each quadrant, moving toward mathematically optimal zones.
* **Medium (Backtracking & Pruning):** Utilizes pure backtracking search combined with alpha-beta style pruning. It calculates an optimistic heuristic to aggressively prune unpromising branches, drastically reducing the time complexity of the decision tree.
* **Hard (Dynamic Programming):** Implements both Top-Down DP (Memoization) and Bottom-Up DP. It maps out a comprehensive state space (tracking row, column, shields, and depth) to guarantee mathematically optimal moves while avoiding infinite loops.

## 🎮 How to Play

### The Rules
* **Move:** Click anywhere on the grid relative to your player icon (🟢) to slide in that direction (N, NE, E, SE, S, SW, W, NW).
* **Collect:** Gather cyan gems (◆) to score points. The game ends when all gems are cleared from the board.
* **Survive:** Avoid black bomb mines (●). Hitting one instantly ends the game unless you have a shield.
* **Defend:** Collect blue shields (🛡️). If you hit a mine while holding a shield, the shield breaks, but you survive the blast.
* **Win:** Have a higher score than the CPU when all gems are collected, or force the CPU into a mine.

### Board Elements
* 🟢 **Green Circle:** Human Player
* 🔴 **Red Circle:** CPU Player
* ⬛ **Dark Gray:** Walls (Stops movement)
* ⭕ **Hollow Circle:** Stop Markers (Stops movement safely)
* ◆ **Cyan Diamond:** Gems (+1 Point)
* ● **Black Bomb:** Mine (Instant Death)
* 🛡️ **Blue Circle 'S':** Shield (Absorbs 1 Mine explosion)

## 💻 Installation & Execution

Ensure you have the [Java Development Kit (JDK)](https://www.oracle.com/java/technologies/downloads/) installed on your machine.

1. **Clone the repository:**
   ```bash
   git clone [https://github.com/yourusername/Inertia-Game.git](https://github.com/yourusername/Inertia-Game.git)
   cd Inertia-Game
