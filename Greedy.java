import java.util.*;

public class Greedy {

    private static final int MEDIUM_DEPTH = 3;
    private static final int HARD_DEPTH   = 4;

    // Loop prevention (store recent CPU positions)
    private static final Deque<String> hardHistory = new ArrayDeque<>();
    private static final int HISTORY_LIMIT = 6;

    /* =========================================================
       DIVIDE AND CONQUER REGION ANALYSIS (used in Easy mode)
       ========================================================= */
    private static class Region {
        int rowStart, rowEnd, colStart, colEnd, index;
        int gemCount, shieldCount, mineCount, wallCount, deadEndCount;
        double score;
        List<Region> subregions = new ArrayList<>();
        
        Region(int rs, int re, int cs, int ce, int idx) {
            rowStart = rs; rowEnd = re; colStart = cs; colEnd = ce; index = idx;
        }
        
        int area() { return (rowEnd - rowStart) * (colEnd - colStart); }
        
        double computeScore() {
            double score = gemCount * 10.0 + shieldCount * 5.0 - 
                          mineCount * 15.0 - deadEndCount * 8.0 - 
                          wallCount * 0.5;
            int area = area();
            return area > 0 ? score / Math.sqrt(area) : score;
        }
    }
    
    private static class RegionPolicy {
        Region bestRegion, worstRegion;
        RegionPolicy(Region best, Region worst) {
            this.bestRegion = best; this.worstRegion = worst;
        }
    }
    
    private static RegionPolicy computeRegionsDivideConquer(BoardModel m) {
        Region root = new Region(0, m.rows, 0, m.cols, 0);
        root = divideAndConquerAnalyze(m, root, 3);
        Region best = findExtremeRegion(root, true);
        Region worst = findExtremeRegion(root, false);
        return new RegionPolicy(best, worst);
    }
    
    private static Region divideAndConquerAnalyze(BoardModel m, Region region, int depth) {
        if (depth == 0 || region.area() <= 4) {
            analyzeRegionDirectly(m, region);
            return region;
        }
        
        int midRow = (region.rowStart + region.rowEnd) / 2;
        int midCol = (region.colStart + region.colEnd) / 2;
        
        Region[] quadrants = new Region[4];
        quadrants[0] = divideAndConquerAnalyze(m, 
            new Region(region.rowStart, midRow, region.colStart, midCol, region.index * 4 + 1), depth - 1);
        quadrants[1] = divideAndConquerAnalyze(m, 
            new Region(region.rowStart, midRow, midCol, region.colEnd, region.index * 4 + 2), depth - 1);
        quadrants[2] = divideAndConquerAnalyze(m, 
            new Region(midRow, region.rowEnd, region.colStart, midCol, region.index * 4 + 3), depth - 1);
        quadrants[3] = divideAndConquerAnalyze(m, 
            new Region(midRow, region.rowEnd, midCol, region.colEnd, region.index * 4 + 4), depth - 1);
        
        region.gemCount = 0; region.shieldCount = 0; region.mineCount = 0;
        region.wallCount = 0; region.deadEndCount = 0;
        
        for (Region quad : quadrants) {
            region.gemCount += quad.gemCount;
            region.shieldCount += quad.shieldCount;
            region.mineCount += quad.mineCount;
            region.wallCount += quad.wallCount;
            region.deadEndCount += quad.deadEndCount;
            region.subregions.add(quad);
        }
        
        region.score = region.computeScore();
        return region;
    }
    
    private static Region findExtremeRegion(Region region, boolean findMax) {
        if (region.subregions.isEmpty()) return region;
        
        Region extreme = findExtremeRegion(region.subregions.get(0), findMax);
        for (int i = 1; i < region.subregions.size(); i++) {
            Region current = findExtremeRegion(region.subregions.get(i), findMax);
            if (findMax) {
                if (current.score > extreme.score) extreme = current;
            } else {
                if (current.score < extreme.score) extreme = current;
            }
        }
        return extreme;
    }
    
    private static void analyzeRegionDirectly(BoardModel m, Region region) {
        for (int r = region.rowStart; r < region.rowEnd; r++) {
            for (int c = region.colStart; c < region.colEnd; c++) {
                Cell cell = m.grid[r][c];
                
                if (cell.wall) region.wallCount++;
                else if (cell.mine) region.mineCount++;
                else if (cell.gem) region.gemCount++;
                else if (cell.shield) region.shieldCount++;
                
                if (!cell.wall && isDeadEnd(m, r, c)) {
                    region.deadEndCount++;
                }
            }
        }
        region.score = region.computeScore();
    }
    
    private static boolean isDeadEnd(BoardModel m, int r, int c) {
        int blocked = 0;
        for (Direction d : Direction.values()) {
            int nr = r + d.dx;
            int nc = c + d.dy;
            if (!m.inBounds(nr, nc) || m.grid[nr][nc].wall || m.grid[nr][nc].mine) {
                blocked++;
            }
        }
        return blocked >= 6;
    }
    
    private static int getRegionIndex(BoardModel m, int r, int c) {
        int midRow = m.rows / 2;
        int midCol = m.cols / 2;
        if (r < midRow) {
            return (c < midCol) ? 1 : 2;
        } else {
            return (c < midCol) ? 3 : 4;
        }
    }
    
    private static int getRegionBonus(BoardModel m, RegionPolicy policy, int r, int c) {
        int regionIdx = getRegionIndex(m, r, c);
        int bonus = 0;
        if (policy.bestRegion != null && regionIdx == policy.bestRegion.index) bonus += 30;
        if (policy.worstRegion != null && regionIdx == policy.worstRegion.index) bonus -= 40;
        return bonus;
    }

    /* =========================================================
       ENTRY POINT
       ========================================================= */
    public static Direction choose(BoardModel m, Difficulty level) {
        switch (level) {
            case EASY:   return playEasyMode(m);
            case MEDIUM: return playMediumMode(m);
            case HARD:   return playHardMode(m);
            default:     return playMediumMode(m);
        }
    }

    /* =========================================================
       EASY MODE (Greedy + Divide & Conquer) – no recursion, so no loops
       ========================================================= */
    private static Direction playEasyMode(BoardModel m) {
        // Compute region policy once
        RegionPolicy policy = computeRegionsDivideConquer(m);
        
        Direction best = null;
        int bestScore = Integer.MIN_VALUE;

        for (Direction d : Direction.values()) {
            BoardModel.SlideResult res = m.slide(m.cpuRow, m.cpuCol, d, false);
            if (res.hitMine && m.cpuShields == 0) continue;
            if (res.r == m.cpuRow && res.c == m.cpuCol) continue;

            // Base score: gems and shields
            int score = res.gems * 10 + res.shields * 5;
            // Add region bonus
            score += getRegionBonus(m, policy, res.r, res.c);
            
            if (score > bestScore) {
                bestScore = score;
                best = d;
            }
        }
        return best != null ? best : getAnySafeMove(m);
    }

    /* =========================================================
       MEDIUM MODE
       Uses CONTRIBUTION 1 & 2 (now with loop prevention via visited set)
       ========================================================= */
    private static Direction playMediumMode(BoardModel m) {
        Direction bestDir = null;
        int bestScore = Integer.MIN_VALUE;

        for (Direction d : Direction.values()) {
            BoardModel.SlideResult res = m.slide(m.cpuRow, m.cpuCol, d, false);
            if (!isValid(res, m)) continue;

            int ns = adjustShields(res, m.cpuShields);

            // Initialize visited set with starting position
            Set<String> visited = new HashSet<>();
            visited.add(m.cpuRow + "," + m.cpuCol);

            // CONTRIBUTION 1: Pure Backtracking (with visited)
            int v1 = res.gems + backtrackingSearch(m, res.r, res.c, ns, 1, visited);

            // CONTRIBUTION 2: Backtracking + Pruning (with visited)
            int v2 = res.gems + backtrackingWithPruning(
                    m, res.r, res.c, ns, 1, 0, Integer.MIN_VALUE, visited);

            int value = Math.max(v1, v2);

            if (value > bestScore) {
                bestScore = value;
                bestDir = d;
            }
        }
        return bestDir != null ? bestDir : getAnySafeMove(m);
    }

    /* =========================================================
       CONTRIBUTION 1: PURE BACKTRACKING (with loop prevention)
       ========================================================= */
    private static int backtrackingSearch(BoardModel m,
                                          int r, int c,
                                          int shields,
                                          int depth,
                                          Set<String> visited) {
        if (depth == MEDIUM_DEPTH) return 0;

        int best = 0;
        for (Direction d : Direction.values()) {
            BoardModel.SlideResult res = m.slide(r, c, d, false);
            if (res.hitMine && shields == 0) continue;
            if (res.r == r && res.c == c) continue;

            String posKey = res.r + "," + res.c;
            if (visited.contains(posKey)) continue; // prevent loops

            int ns = adjustShields(res, shields);
            Set<String> newVisited = new HashSet<>(visited);
            newVisited.add(posKey);
            best = Math.max(best,
                    res.gems + backtrackingSearch(m, res.r, res.c, ns, depth + 1, newVisited));
        }
        return best;
    }

    /* =========================================================
       CONTRIBUTION 2: BACKTRACKING WITH PRUNING (with loop prevention)
       ========================================================= */
    private static int backtrackingWithPruning(BoardModel m,
                                               int r, int c,
                                               int shields,
                                               int depth,
                                               int current,
                                               int bestSoFar,
                                               Set<String> visited) {
        if (depth == MEDIUM_DEPTH) return current;

        int best = bestSoFar;
        for (Direction d : Direction.values()) {
            BoardModel.SlideResult res = m.slide(r, c, d, false);
            if (res.hitMine && shields == 0) continue;
            if (res.r == r && res.c == c) continue;

            String posKey = res.r + "," + res.c;
            if (visited.contains(posKey)) continue; // prevent loops

            int optimistic = current + res.gems + 100 * (MEDIUM_DEPTH - depth);
            if (optimistic <= best) continue; // PRUNE

            int ns = adjustShields(res, shields);
            Set<String> newVisited = new HashSet<>(visited);
            newVisited.add(posKey);
            best = Math.max(best,
                    backtrackingWithPruning(
                            m, res.r, res.c, ns,
                            depth + 1,
                            current + res.gems,
                            best,
                            newVisited));
        }
        return best;
    }

    /* =========================================================
       HARD MODE (STRONG + LOOP SAFE)
       Uses CONTRIBUTION 3 & 4
       ========================================================= */
    private static Direction playHardMode(BoardModel m) {

        // === ABSOLUTE RULE: TAKE GEM IF REACHABLE ===
        for (Direction d : Direction.values()) {
            BoardModel.SlideResult res = m.slide(m.cpuRow, m.cpuCol, d, false);
            if (res.gems > 0 && isValid(res, m)) {
                recordHistory(m);
                return d;
            }
        }

        Direction bestDir = null;
        int bestScore = Integer.MIN_VALUE;

        Map<String, Integer> memo = new HashMap<>();

        for (Direction d : Direction.values()) {
            BoardModel.SlideResult res = m.slide(m.cpuRow, m.cpuCol, d, false);
            if (!isValid(res, m)) continue;

            int ns = adjustShields(res, m.cpuShields);

            // CONTRIBUTION 3: Top-down DP
            int dpValue = res.gems +
                    dpMemoizedSearch(m, res.r, res.c, ns, 1, memo);

            // CONTRIBUTION 4: Bottom-up DP
            int buValue = dpBottomUp(m, res.r, res.c, ns);

            // EXTRA HEURISTIC: move closer to gems
            int heuristic = estimateFutureGemPotential(m, res.r, res.c);

            int total = Math.max(dpValue, buValue) + heuristic;

            if (total > bestScore) {
                bestScore = total;
                bestDir = d;
            }
        }

        if (bestDir == null) return getAnySafeMove(m);

        // LOOP PREVENTION
        BoardModel.SlideResult finalRes =
                m.slide(m.cpuRow, m.cpuCol, bestDir, false);

        String key = finalRes.r + "," + finalRes.c;
        if (hardHistory.contains(key)) {
            return getAnySafeMove(m);
        }

        recordHistory(m);
        return bestDir;
    }

    /* =========================================================
       CONTRIBUTION 3: DP MEMOIZATION (TOP-DOWN)
       ========================================================= */
    private static int dpMemoizedSearch(BoardModel m,
                                        int r, int c,
                                        int shields,
                                        int depth,
                                        Map<String, Integer> memo) {
        if (depth == HARD_DEPTH) return 0;

        String key = r + "," + c + "," + shields + "," + depth;
        if (memo.containsKey(key)) return memo.get(key);

        int best = 0;
        for (Direction d : Direction.values()) {
            BoardModel.SlideResult res = m.slide(r, c, d, false);
            if (res.hitMine && shields == 0) continue;
            if (res.r == r && res.c == c) continue;

            int ns = adjustShields(res, shields);
            best = Math.max(best,
                    res.gems + dpMemoizedSearch(m, res.r, res.c, ns, depth + 1, memo));
        }

        memo.put(key, best);
        return best;
    }

    /* =========================================================
       CONTRIBUTION 4: DP BOTTOM-UP
       ========================================================= */
    private static int dpBottomUp(BoardModel m,
                                  int r, int c,
                                  int shields) {
        Map<String, Integer> current = new HashMap<>();
        current.put(r + "," + c + "," + shields, 0);

        for (int depth = 0; depth < HARD_DEPTH; depth++) {
            Map<String, Integer> next = new HashMap<>();
            for (String state : current.keySet()) {
                String[] p = state.split(",");
                int cr = Integer.parseInt(p[0]);
                int cc = Integer.parseInt(p[1]);
                int cs = Integer.parseInt(p[2]);
                int score = current.get(state);

                for (Direction d : Direction.values()) {
                    BoardModel.SlideResult res = m.slide(cr, cc, d, false);
                    if (res.hitMine && cs == 0) continue;
                    if (res.r == cr && res.c == cc) continue;

                    int ns = adjustShields(res, cs);
                    String nk = res.r + "," + res.c + "," + ns;
                    next.put(nk,
                            Math.max(next.getOrDefault(nk, 0),
                                    score + res.gems));
                }
            }
            current = next;
        }
        return Collections.max(current.values());
    }

    /* =========================================================
       HELPERS
       ========================================================= */
    private static boolean isValid(BoardModel.SlideResult res, BoardModel m) {
        if (res.hitMine && m.cpuShields == 0) return false;
        return !(res.r == m.cpuRow && res.c == m.cpuCol);
    }

    private static int adjustShields(BoardModel.SlideResult res, int shields) {
        int s = shields + res.shields;
        if (res.hitMine && s > 0) s--;
        return s;
    }

    private static Direction getAnySafeMove(BoardModel m) {
        for (Direction d : Direction.values()) {
            BoardModel.SlideResult res = m.slide(m.cpuRow, m.cpuCol, d, false);
            if (!(res.hitMine && m.cpuShields == 0)
                    && (res.r != m.cpuRow || res.c != m.cpuCol)) {
                return d;
            }
        }
        return Direction.N;
    }

    private static int estimateFutureGemPotential(BoardModel m, int r, int c) {
        int score = 0;
        for (Direction d : Direction.values()) {
            int nr = r + d.dx;
            int nc = c + d.dy;
            if (m.inBounds(nr, nc) && m.grid[nr][nc].gem) {
                score += 30;
            }
        }
        return score;
    }

    private static void recordHistory(BoardModel m) {
        String key = m.cpuRow + "," + m.cpuCol;
        hardHistory.addLast(key);
        if (hardHistory.size() > HISTORY_LIMIT) {
            hardHistory.removeFirst();
        }
    }
}