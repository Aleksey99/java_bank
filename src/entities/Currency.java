package entities;

public enum Currency {
    RUB(1.0),
    EUR(80.0),
    USD(70.0);

    private double RUBperUnit;

    Currency(double koef) {
        this.RUBperUnit = koef;
    }

    public double getRUBperUnit() {
        return RUBperUnit;
    }
}
