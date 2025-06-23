package src;
import java.util.*;
import java.time.*;
import java.time.format.*;

class Item {
    String name, category;
    double price;
    int quantity;

    public Item(String name, String category, double price, int quantity) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.quantity = quantity;
    }
}

class Voucher {
    String name;
    double discount;
    String category;
    int minQty;
    double minAmount;
    boolean exclusive;
    Set<String> conflicts;

    public Voucher(String name, double discount, String category, int minQty, double minAmount, boolean exclusive, Set<String> conflicts) {
        this.name = name;
        this.discount = discount;
        this.category = category;
        this.minQty = minQty;
        this.minAmount = minAmount;
        this.exclusive = exclusive;
        this.conflicts = conflicts;
    }

    public boolean isApplicable(List<Item> cart, double subtotal) {
        if (minAmount > 0 && subtotal < minAmount) return false;
        if (category == null) return true;
        int count = 0;
        for (Item item : cart) {
            if (item.category.equals(category)) count += item.quantity;
        }
        return count >= minQty;
    }

    public boolean isCompatible(Set<String> selected) {
        for (String s : selected) {
            if (conflicts.contains(s)) return false;
        }
        return true;
    }
}

public class VoucherOptimiser {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        List<Item> catalog = List.of(
                new Item("Red Apple", "Fruit", 2.00, 0),
                new Item("Whole Bread", "Bakery", 1.50, 0),
                new Item("Fresh Milk", "Dairy", 3.00, 0),
                new Item("Baby Spinach", "Vegetable", 2.50, 0),
                new Item("Chicken Breast", "Meat", 8.00, 0),
                new Item("Greek Yogurt", "Dairy", 4.50, 0),
                new Item("Banana", "Fruit", 1.80, 0),
                new Item("Croissant", "Bakery", 2.20, 0),
                new Item("Cabbage", "Vegetable", 2.00, 0),
                new Item("Cheddar Cheese", "Dairy", 5.00, 0),
                new Item("Orange", "Fruit", 2.20, 0),
                new Item("Carrot", "Vegetable", 1.80, 0),
                new Item("Pasta", "Grain", 3.50, 0),
                new Item("White Rice", "Grain", 2.90, 0),
                new Item("Beef Steak", "Meat", 10.50, 0),
                new Item("Eggs", "Dairy", 0.50, 0),
                new Item("Muffin", "Bakery", 2.10, 0),
                new Item("Tomato", "Vegetable", 2.30, 0),
                new Item("Fish Fillet", "Meat", 9.00, 0),
                new Item("Cereal", "Grain", 4.00, 0),
                new Item("Iced Tea", "Beverage", 3.00, 0),
                new Item("Orange Juice", "Beverage", 4.20, 0),
                new Item("Bottled Water", "Beverage", 1.00, 0),
                new Item("Coffee", "Beverage", 4.00, 0)
        );

        List<Item> cart = new ArrayList<>();
        System.out.println("===============================================");
        System.out.println("\t\t\tAVAILABLE ITEMS ");
        System.out.println("===============================================");
        System.out.println("ID | Item Name         | Price   | Category    | Stock");
        System.out.println("---|--------------------|---------|-------------|-------");
        for (int i = 0; i < catalog.size(); i++) {
            Item item = catalog.get(i);
            System.out.printf("%-3d| %-18s RM%-6.2f %-13s ✓\n", i + 1, item.name, item.price, item.category);
        }

        List<Voucher> vouchers = List.of(
                new Voucher("Dairy Saver", 2.00, "Dairy", 2, 0, false, Set.of()),
                new Voucher("Bakery Combo", 1.00, "Bakery", 2, 0, false, Set.of()),
                new Voucher("Vege Value Pack", 2.00, "Vegetable", 2, 0, false, Set.of()),
                new Voucher("Fruit Bundle", 1.50, "Fruit", 3, 0, false, Set.of()),
                new Voucher("Meat Feast", 3.00, "Meat", 2, 0, false, Set.of()),
                new Voucher("Grain Boost", 2.50, "Grain", 2, 0, false, Set.of()),
                new Voucher("Beverage Bonus", 2.00, "Beverage", 2, 0, false, Set.of()),
                new Voucher("Super Saver Combo", 10.00, null, 0, 50.00, true,
                        Set.of("Dairy Saver", "Bakery Combo", "Vege Value Pack", "Fruit Bundle", "Meat Feast", "Grain Boost", "Beverage Bonus"))
        );

        while (true) {
            System.out.println("===============================================");
            System.out.println("\t\t\t  YOUR CART");
            System.out.println("===============================================");
            if (cart.isEmpty()) {
                System.out.println("Current Cart: [Empty]\nSubtotal: RM 0.00\n");
            } else {
                double sub = 0;
                for (Item item : cart) {
                    double total = item.price * item.quantity;
                    System.out.printf("- %s x%d @ RM %.2f = RM %.2f\n", item.name, item.quantity, item.price, total);
                    sub += total;
                }
                System.out.printf("Subtotal: RM %.2f\n\n", sub);
            }

            System.out.print("Enter item ID (1-24) or 0 to finish: ");
            int id = sc.nextInt();
            if (id == 0) break;
            if (id < 1 || id > catalog.size()) continue;
            Item selected = catalog.get(id - 1);
            System.out.printf("Item: %s (RM %.2f)\n", selected.name, selected.price);
            System.out.print("Enter quantity: ");
            int qty = sc.nextInt();
            cart.add(new Item(selected.name, selected.category, selected.price, qty));
            System.out.printf("✓ Added: %s x%d = RM %.2f\n\n", selected.name, qty, selected.price * qty);
        }

        long startDP = System.nanoTime();
        optimize(cart, vouchers);
        long endDP = System.nanoTime();
        System.out.printf("DP Time: %.3f ms\n", (endDP - startDP) / 1e6);

        long startGreedy = System.nanoTime();
        greedyOptimize(cart, vouchers);
        long endGreedy = System.nanoTime();
        System.out.printf("Greedy Time: %.3f ms\n", (endGreedy - startGreedy) / 1e6);
    }

    public static void optimize(List<Item> cart, List<Voucher> vouchers) {
        int n = vouchers.size();
        double subtotal = cart.stream().mapToDouble(i -> i.price * i.quantity).sum();
        double bestDiscount = 0;
        List<String> bestCombo = new ArrayList<>();

        for (int mask = 0; mask < (1 << n); mask++) {
            Set<String> selected = new HashSet<>();
            double totalDiscount = 0;
            boolean valid = true;
            boolean hasExclusive = false;

            for (int i = 0; i < n; i++) {
                if ((mask & (1 << i)) != 0) {
                    Voucher v = vouchers.get(i);
                    if (!v.isApplicable(cart, subtotal) || !v.isCompatible(selected)) {
                        valid = false;
                        break;
                    }
                    if (v.exclusive && !selected.isEmpty()) {
                        valid = false;
                        break;
                    }
                    if (!v.exclusive && hasExclusive) {
                        valid = false;
                        break;
                    }
                    if (v.exclusive) hasExclusive = true;
                    selected.add(v.name);
                    totalDiscount += v.discount;
                }
            }

            if (valid && totalDiscount > bestDiscount) {
                bestDiscount = totalDiscount;
                bestCombo = new ArrayList<>(selected);
            }
        }

        System.out.println("\n===============================================");
        System.out.println("\t\t\t    DP RECEIPT ");
        System.out.println("===============================================");
        String txnID = "TXN" + LocalDate.now().toString().replace("-", "") + "001";
        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"));

        System.out.printf("Transaction ID: %s\nDate: %s\nTime: %s\n\n", txnID, date, time);
        System.out.println("ITEMS PURCHASED:");
        for (Item i : cart) {
            System.out.printf("- %s x%d @ RM %.2f = RM %.2f\n", i.name, i.quantity, i.price, i.price * i.quantity);
        }

        System.out.println("\nVOUCHERS APPLIED:");
        for (String v : bestCombo) {
            System.out.printf("✓ %s\n", v);
        }
        System.out.printf("Total Vouchers Used: %d\n", bestCombo.size());

        System.out.println("\n-----------------------------------------------");
        System.out.printf("SUBTOTAL:        RM %.2f\n", subtotal);
        System.out.printf("DISCOUNT:       -RM %.2f\n", bestDiscount);
        System.out.printf("TOTAL TO PAY:    RM %.2f\n", subtotal - bestDiscount);
        System.out.printf("YOU SAVED:       RM %.2f (%.2f%%)\n", bestDiscount, (bestDiscount / subtotal) * 100);
        System.out.println("===============================================");
    }

    public static void greedyOptimize(List<Item> cart, List<Voucher> vouchers) {
        double subtotal = cart.stream().mapToDouble(i -> i.price * i.quantity).sum();
        Set<String> selected = new HashSet<>();
        double totalDiscount = 0;
        List<String> appliedVouchers = new ArrayList<>();

        List<Voucher> sorted = new ArrayList<>(vouchers);
        sorted.sort((a, b) -> Double.compare(b.discount, a.discount)); // descending

        for (Voucher v : sorted) {
            if (!v.isApplicable(cart, subtotal)) continue;
            if (!v.isCompatible(selected)) continue;
            if (v.exclusive && !selected.isEmpty()) continue;

            boolean exclusiveInCart = selected.stream().anyMatch(name -> {
                for (Voucher existing : vouchers) {
                    if (existing.name.equals(name) && existing.exclusive) return true;
                }
                return false;
            });

            if (!v.exclusive && exclusiveInCart) continue;

            selected.add(v.name);
            appliedVouchers.add(v.name);
            totalDiscount += v.discount;
        }

        // RECEIPT (formatted like DP)
        System.out.println("\n===============================================");
        System.out.println("\t\t\tGREEDY RECEIPT ");
        System.out.println("===============================================");
        String txnID = "TXN" + LocalDate.now().toString().replace("-", "") + "002";
        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"));

        System.out.printf("Transaction ID: %s\nDate: %s\nTime: %s\n\n", txnID, date, time);
        System.out.println("ITEMS PURCHASED:");
        for (Item i : cart) {
            System.out.printf("- %s x%d @ RM %.2f = RM %.2f\n", i.name, i.quantity, i.price, i.price * i.quantity);
        }

        System.out.println("\nVOUCHERS APPLIED:");
        for (String v : appliedVouchers) {
            System.out.printf("✓ %s\n", v);
        }
        System.out.printf("Total Vouchers Used: %d\n", appliedVouchers.size());

        System.out.println("\n-----------------------------------------------");
        System.out.printf("SUBTOTAL:        RM %.2f\n", subtotal);
        System.out.printf("DISCOUNT:       -RM %.2f\n", totalDiscount);
        System.out.printf("TOTAL TO PAY:    RM %.2f\n", subtotal - totalDiscount);
        System.out.printf("YOU SAVED:       RM %.2f (%.2f%%)\n", totalDiscount, (totalDiscount / subtotal) * 100);
        System.out.println("===============================================");
    }

}
