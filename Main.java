import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Product cheese = new ExpirableProduct("Cheese", 100, 5, LocalDate.of(2025, 12, 31));
        Product biscuits = new ExpirableProduct("Biscuits", 150, 2, LocalDate.of(2025, 8, 1));
        Product tv = new ShippableProduct("TV", 300, 3, 0.5);
        Product scratchCard = new Product("Scratch Card", 50, 10);

        Customer customer = new Customer("Amr", 1000);
        Cart cart = new Cart();
        cart.add(cheese, 2);
        cart.add(biscuits, 1);
        cart.add(tv, 1);
        cart.add(scratchCard, 1);

        CheckoutService.checkout(customer, cart);
    }
}

// ---------------------- Product & Variants ----------------------

class Product {
    protected String name;
    protected double price;
    protected int quantity;

    public Product(String name, double price, int quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void reduceQuantity(int amount) {
        if (quantity >= amount) {
            quantity -= amount;
        }
    }

    public boolean isExpired() {
        return false;
    }

    public boolean isShippable() {
        return false;
    }
}

class ExpirableProduct extends Product {
    private LocalDate expiryDate;

    public ExpirableProduct(String name, double price, int quantity, LocalDate expiryDate) {
        super(name, price, quantity);
        this.expiryDate = expiryDate;
    }

    @Override
    public boolean isExpired() {
        return LocalDate.now().isAfter(expiryDate);
    }
}

interface Shippable {
    String getName();
    double getWeight();
}

class ShippableProduct extends Product implements Shippable {
    private double weight;

    public ShippableProduct(String name, double price, int quantity, double weight) {
        super(name, price, quantity);
        this.weight = weight;
    }

    @Override
    public boolean isShippable() {
        return true;
    }

    @Override
    public double getWeight() {
        return weight;
    }
}

// ---------------------- Customer & Cart ----------------------

class Customer {
    private String name;
    private double balance;

    public Customer(String name, double balance) {
        this.name = name;
        this.balance = balance;
    }

    public double getBalance() {
        return balance;
    }

    public void deduct(double amount) {
        this.balance -= amount;
    }

    public String getName() {
        return name;
    }
}

class CartItem {
    public Product product;
    public int quantity;

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }
}

class Cart {
    private List<CartItem> items = new ArrayList<>();

    public void add(Product product, int quantity) {
        if (quantity > product.getQuantity()) {
            throw new IllegalArgumentException("Not enough stock for product: " + product.getName());
        }
        items.add(new CartItem(product, quantity));
    }

    public List<CartItem> getItems() {
        return items;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}

// ---------------------- Checkout & Shipping ----------------------

class ShippingService {
    public static void ship(List<Shippable> items) {
        System.out.println(" Shipment notice ");
        double totalWeight = 0.0;

        for (Shippable item : items) {
            System.out.println(item.getName() + " " + (item.getWeight() * 1000) + "g");
            totalWeight += item.getWeight();
        }

        System.out.println("Total package weight: " + totalWeight + "kg\n");
    }
}
class CheckoutService {
    public static void checkout(Customer customer, Cart cart) {
        if (cart.isEmpty()) {
            System.out.println("Error: Cart is empty.");
            return;
        }

        double subtotal = 0;
        double shippingFee = 0;
        List<Shippable> shippables = new ArrayList<>();

        for (CartItem item : cart.getItems()) {
            Product product = item.product;

            if (product.isExpired()) {
                System.out.println("Error: Product " + product.getName() + " is expired.");
                return;
            }

            if (item.quantity > product.getQuantity()) {
                System.out.println("Error: Not enough stock for product " + product.getName());
                return;
            }

            double itemTotal = product.getPrice() * item.quantity;
            subtotal += itemTotal;

            if (product.isShippable()) {
                for (int i = 0; i < item.quantity; i++) {
                    shippables.add((Shippable) product);
                }
                shippingFee += 10 * item.quantity;
            }
        }

        double total = subtotal + shippingFee;

        if (customer.getBalance() < total) {
            System.out.println("Error: Insufficient balance.");
            return;
        }

        for (CartItem item : cart.getItems()) {
            item.product.reduceQuantity(item.quantity);
        }

        customer.deduct(total);

        if (!shippables.isEmpty()) {
            ShippingService.ship(shippables);
        }

        System.out.println(" Checkout receipt ");
        for (CartItem item : cart.getItems()) {
            System.out.println(item.quantity + "x " + item.product.getName() + " " + (item.product.getPrice() * item.quantity));
        }
        System.out.println("----------------------");
        System.out.println("Subtotal: " + subtotal);
        System.out.println("Shipping: " + shippingFee);
        System.out.println("Amount: " + total);
        System.out.println("Customer balance: " + customer.getBalance());
    }
}
