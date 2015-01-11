package nars.jwam.datastructures;

import java.util.Arrays;

public class Numbers {

    double[] nums, temps;
    int[] free_indices;
    int nr_free_indices = 0, nr_nums = 0, nr_temps = 0;

    public Numbers() {
        reset();
    }

    public void reset() {
        nums = new double[32];
        temps = new double[32];
        free_indices = new int[32];
        nr_free_indices = nr_nums = nr_temps = 0;
    }

    public void reset_temps() {
        nr_temps = 0;
    }

    public int new_number(String str) {
        if (!str.endsWith("s") || str.contains(".")) {
            nums[nr_nums] = Double.parseDouble(str);
            nr_nums++;
            if (nums.length == nr_nums) {
                nums = Arrays.copyOf(nums, nums.length * 2);  // Expand space
            }
            return ((nr_nums - 1) << 2) | 1;
        } else {
            return simple_int(Integer.parseInt(str.substring(0, str.length() - 1))); // Make simple int 
        }
    }

    public void remove_num(int num) {
        if ((num & 3) == 1) {
            int index = num >> 2;
            if (index == nr_nums - 1) {
                nr_nums--;
            } else {
                free_indices[nr_free_indices] = index;
                nr_free_indices++;
                if (nr_free_indices == free_indices.length) // If no more room for indices
                {
                    free_indices = Arrays.copyOf(free_indices, free_indices.length * 2); // Expand space
                }
            }
        }
    }

    public int add_num(int num) {		// Can only add nums with assert so must be on heap already, not possible to add double
        double v = 0;
        if ((num & 3) == 0) {
            return num; 		// simple num
        } else if ((num & 3) == 1) {
            v = nums[num >> 2];
        } else if ((num & 3) == 2) {
            v = temps[num >> 2];
        }
        int index = nr_nums;
        if (nr_free_indices > 0) {
            nr_free_indices--;
            index = free_indices[nr_free_indices];
        } else {
            nr_nums++;
            if (nums.length == nr_nums) {
                nums = Arrays.copyOf(nums, nums.length * 2);  // Expand space
            }
        }
        nums[index] = v;
        return (index << 2) | 1;
    }

    public int store_temp(double num) {
        temps[nr_temps] = num;
        nr_temps++;
        if (temps.length == nr_temps) {
            temps = Arrays.copyOf(temps, temps.length * 2);  // Expand space
        }
        return ((nr_temps - 1) << 2) | 2;
    }

    public boolean are_equal(int n1, int n2) {
        return getDouble(n1) == getDouble(n2);
    }

    public static int simple_int(int value) {
        return (value < 0) ? (((-value) << 3) | 4) : (value << 3);
    }

    public double getDouble(int n) {
        double v = Double.NaN;
        if ((n & 3) == 0) {
            v = (double) (n >> 3);
            if ((n & 4) > 0) {
                v *= -1;
            }
        } else if ((n & 3) == 1) {
            v = nums[n >> 2];
        } else if ((n & 3) == 2) {
            v = temps[n >> 2];
        }
        return v;
    }

    public String numToString(int num) {
        return "" + getDouble(num);
    }
}
