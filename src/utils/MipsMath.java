package utils;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @Description TODO
 * @Author
 * @Date 2023/11/20
 **/
public class MipsMath {
    /**
     * 计算一个给定二进制数末尾0的个数
     */
    public static int countTailZeroNumber(int n) {
        int cnt = 0;
        n = n >>> 1;
        while (n != 0) {
            n = n >>> 1;
            cnt++;
        }
        return cnt;
    }

    /**
     * 是否能够编码为16位整数
     * @param signExtend 是否拓展至负数
     */
    public static boolean is16BitImm(int imm, boolean signExtend) {
        if (signExtend) {
            return Short.MIN_VALUE <= imm && imm <= Short.MAX_VALUE;
        }
        else {
            return 0 <= imm && imm <= (Short.MAX_VALUE - Short.MIN_VALUE);
        }
    }

    /**
     * 判断是否为2的幂次，要求x为非负整数
     */
    public static boolean isPow2(int x){
        return (x & (x - 1)) == 0;
    }

    private static class MulOptimizer {
        private int steps;
        // source 是乘常数
        private int multiplier;
        // bool 负责指示加减，integer 负责指示位移
        private final ArrayList<Pair<Boolean, Integer>> items;

        private MulOptimizer(int... shifts) {
            multiplier = 0;
            items = new ArrayList<>();
            for (int shift : shifts) {
                if (shift >= 0) {
                    multiplier += 1 << shift;
                    items.add(new Pair<>(true, shift & Integer.MAX_VALUE));
                } else {
                    // i & Integer.MAX_VALUE 是 i + (1 << 31) 的意思
                    // 不过这里只是故弄玄虚罢了，我们只是需要一个 +shift，一个 -shift 而已
                    multiplier -= 1 << (shift & Integer.MAX_VALUE);
                    items.add(new Pair<>(false, shift & Integer.MAX_VALUE));
                }
            }

            // 计算一下开销
            steps = items.get(0).getFirst() || items.get(0).getSecond() == 0 ? 1 : 2;
            for (int i = 1; i < items.size(); i++) {
                steps += items.get(i).getSecond() == 0 ? 1 : 2;
            }
        }

        private int getSteps() {
            return steps;
        }

        public int getMultiplier() {
            return multiplier;
        }

        public ArrayList<Pair<Boolean, Integer>> getItems() {
            return items;
        }

        /**
         * 这个是基础步骤的步数的优化步数的比较
         * 此时的 base == 4
         *
         * @return true 则采取优化
         */
        private boolean isBetter() {
            int base = 4;
            // 这里有个 base-- 我没有看懂
            if ((multiplier & 0xffff) == 0 ||
                    (Short.MIN_VALUE <= multiplier && multiplier <= Short.MAX_VALUE - Short.MIN_VALUE)) {
                base--;
            }
            return steps < base;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder("x * " + multiplier + " = ");
            for (int i = 0; i < items.size(); i++) {
                if (i != 0 || !items.get(i).getFirst()) {
                    builder.append(items.get(i).getFirst() ? "+ " : "- ");
                }
                if (items.get(i).getSecond() == 0) {
                    builder.append("x ");
                } else {
                    builder.append("(x << ").append(items.get(i).getSecond()).append(") ");
                }
            }
            return builder.append("-> ").append(steps).toString();
        }
    }

    /**
     * 可以根据乘常数查询对应的优化序列
     */
    private static final HashMap<Integer, MulOptimizer> mulOptimizers = new HashMap<>();


    // 生成 mulOptimizers
    static {
        ArrayList<MulOptimizer> tmpLists = new ArrayList<>();
        // 只是一个标签而已，没有实际意义，为了让一个 shift 发挥两次作用
        int NEGATIVE_TAG = 0x80000000;
        // 因为基准是 4，所以最多可以采用 3 个正向 shift，所以有 i, j, k 三个
        for (int i = 0; i < 32; i++) {
            tmpLists.add(new MulOptimizer(i));
            tmpLists.add(new MulOptimizer(i | NEGATIVE_TAG));
            for (int j = 0; j < 32; j++) {
                tmpLists.add(new MulOptimizer(i, j));
                tmpLists.add(new MulOptimizer(i, j | NEGATIVE_TAG));
                tmpLists.add(new MulOptimizer(i | NEGATIVE_TAG, j));
                tmpLists.add(new MulOptimizer(i | NEGATIVE_TAG, j | NEGATIVE_TAG));
                for (int k = 0; k < 32; k++) {
                    tmpLists.add(new MulOptimizer(i, j, k));
                    tmpLists.add(new MulOptimizer(i, j, k | NEGATIVE_TAG));
                    tmpLists.add(new MulOptimizer(i, j | NEGATIVE_TAG, k));
                    tmpLists.add(new MulOptimizer(i, j | NEGATIVE_TAG, k | NEGATIVE_TAG));
                    tmpLists.add(new MulOptimizer(i | NEGATIVE_TAG, j, k));
                    tmpLists.add(new MulOptimizer(i | NEGATIVE_TAG, j, k | NEGATIVE_TAG));
                    tmpLists.add(new MulOptimizer(i | NEGATIVE_TAG, j | NEGATIVE_TAG, k));
                    tmpLists.add(new MulOptimizer(i | NEGATIVE_TAG, j | NEGATIVE_TAG, k | NEGATIVE_TAG));
                }
            }
        }
        // 通过这个筛选，获得比基准情况和其他优化情况更优的优化
        for (MulOptimizer tmp : tmpLists) {
            if (tmp.isBetter()) {
                if (!mulOptimizers.containsKey(tmp.getMultiplier()) ||
                        tmp.getSteps() < mulOptimizers.get(tmp.getMultiplier()).getSteps()) {
                    mulOptimizers.put(tmp.getMultiplier(), tmp);
                }
            }
        }
    }

    public static ArrayList<Pair<Boolean, Integer>> getMulOptItems(int multiplier) {
        if (mulOptimizers.containsKey(multiplier)) {
            return mulOptimizers.get(multiplier).getItems();
        } else {
            return null;
        }
    }
}
