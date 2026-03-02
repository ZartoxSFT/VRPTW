package vrptw;

import java.util.ArrayList;
import java.util.List;

public class Solution {
    public final List<List<Integer>> routes;

    public Solution(List<List<Integer>> routes) {
        this.routes = routes;
    }

    public Solution deepCopy() {
        List<List<Integer>> copy = new ArrayList<>();
        for (List<Integer> r : routes) {
            copy.add(new ArrayList<>(r));
        }
        return new Solution(copy);
    }
}
