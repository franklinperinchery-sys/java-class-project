package movieticket.model;

/** Small, focused interface (Interface Segregation Principle): anything that can earn a discount. */
public interface Discountable {
    double getDiscountRate();
}
