package com.gantenx.constant;

public enum Proportion {
    PROPORTION_OF_100(100),
    PROPORTION_OF_95(95),
    PROPORTION_OF_90(90),
    PROPORTION_OF_85(85),
    PROPORTION_OF_80(80),
    PROPORTION_OF_75(75),
    PROPORTION_OF_70(70),
    PROPORTION_OF_65(65),
    PROPORTION_OF_60(60),
    PROPORTION_OF_55(55),
    PROPORTION_OF_50(50),
    PROPORTION_OF_45(45),
    PROPORTION_OF_40(40),
    PROPORTION_OF_35(35),
    PROPORTION_OF_30(30),
    PROPORTION_OF_25(25),
    PROPORTION_OF_20(20),
    PROPORTION_OF_15(15),
    PROPORTION_OF_10(10), PROPORTION_OF_5(5), PROPORTION_OF_0(0);

    private final int value;

    Proportion(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static Proportion fromValue(int value) {
        for (Proportion proportion : Proportion.values()) {
            if (proportion.value == value) {
                return proportion;
            }
        }
        throw new IllegalArgumentException("No proportion found for value: " + value);
    }

    public static Proportion getProportion(double v) {
        for (Proportion p : Proportion.values()) {
            if (v >= p.getValue()) {
                return p;
            }
        }
        return Proportion.PROPORTION_OF_0;  // 如果 v 小于所有比例，返回 PROPORTION_OF_0
    }
}

