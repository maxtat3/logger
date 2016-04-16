package domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Contained results in measure process.
 */
public class Result {

    /**
     * Maximum used channels in one measure cycle
     */
    private int usedChannel = 4;

    /**
     * Results container for channel 1
     */
    private ArrayList<Integer> values1 = new ArrayList<>();

    /**
     * Results container for channel 2
     */
    private ArrayList<Integer> values2 = new ArrayList<>();

    /**
     * Results container for channel 3
     */
    private ArrayList<Integer> values3 = new ArrayList<>();

    /**
     * Results container for channel 4
     */
    private ArrayList<Integer> values4 = new ArrayList<>();


    public int getUsedChannel() {
        return usedChannel;
    }

    public void setUsedChannel(int usedChannel) {
        this.usedChannel = usedChannel;
    }

    /**
     * Add one value to container stored channel 1 values.
     * Call this method add one atomic point in measure process.
     * @param value value channel 1
     */
    public void addChannel1Val(Integer value) {
        values1.add(value);
    }

    /**
     * Returned all values stored for channel 1
     * @return all values stored for channel 1
     */
    public List<Integer> getChannel1AllValues() {
        return values1;
    }

    public void addChannel2Val(Integer value) {
        values2.add(value);
    }

    public List<Integer> getChannel2AllValues() {
        return values2;
    }

    public void addChannel3Val(Integer value) {
        values3.add(value);
    }

    public List<Integer> getChannel3AllValues() {
        return values3;
    }

    public void addChannel4Val(Integer value) {
        values4.add(value);
    }

    public List<Integer> getChannel4AllValues() {
        return values4;
    }
}
