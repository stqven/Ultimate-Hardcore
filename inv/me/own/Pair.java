package inv.me.own;

public class Pair<DT1, DT2> {
    DT1 data1;
    DT2 data2;

    public Pair(DT1 data1, DT2 data2) {
        this.data1 = data1;
        this.data2 = data2;
    }

    public DT1 getKey() {
        return data1;
    }

    public DT2 getValue() {
        return data2;
    }
}