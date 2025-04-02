// TODO Make the ouput colorfull
// TODO MAKE PROPER TABLE OUTPUT


import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

public class TransactionManager {
    private static final String DATA_FILE = "transactions.dat";
    private static final Scanner scanner = new Scanner(System.in);
    private static List<Transaction> transactions = new ArrayList<>();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static boolean headerPrinted = false;
    public static void main(String[] args) {
        loadTransactions();
        boolean running = true;

        while (running) {
            System.out.println("\n\033[0;32m===== Transaction Manager =====\033[0m");
            System.out.println("1. Add Transaction");
            System.out.println("2. Delete Transaction");
            System.out.println("3. Edit Transaction");
            System.out.println("4. Query Transactions");
            System.out.println("5. Info (Spending/Credit Reports)");
            System.out.println("6. Exit");
            System.out.print("\033[93mEnter your choice: \033[0m ");

            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                continue;
            }

            clearScreen();

            switch (choice) {
                case 1:
                    addTransaction();
                    break;
                case 2:
                    deleteTransaction();
                    break;
                case 3:
                    editTransaction();
                    break;
                case 4:
                    queryTransactions();
                    break;
                case 5:
                    showInfo();
                    break;
                case 6:
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }

        saveTransactions();
        System.out.println("\033[0;32mThank you for using Transaction Manager. Goodbye!\033[0m");
    }

    private static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    private static void addTransaction() {
        System.out.println("\n\033[0;34m===== Add Transaction =====\033[0m");

        System.out.print("To (person): ");
        String person = scanner.nextLine().trim();

        System.out.print("Description: ");
        String description = scanner.nextLine().trim();

        BigDecimal amount = null;
        while (amount == null) {
            System.out.print("Amount: ");
            try {
                amount = new BigDecimal(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid amount. Please enter a valid number.");
            }
        }

        String type = "";
        while (!type.equalsIgnoreCase("debit") && !type.equalsIgnoreCase("credit")) {
            System.out.print("Type (debit/credit): ");
            type = scanner.nextLine().trim();
            if (!type.equalsIgnoreCase("debit") && !type.equalsIgnoreCase("credit")) {
                System.out.println("Invalid type. Please enter 'debit' or 'credit'.");
            }
        }

        LocalDate date = null;
        System.out.print("Date (YYYY-MM-DD, leave empty for current date): ");
        String dateInput = scanner.nextLine().trim();
        if (dateInput.isEmpty()) {
            date = LocalDate.now();
        } else {
            try {
                date = LocalDate.parse(dateInput, DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Using current date.");
                date = LocalDate.now();
            }
        }

        LocalTime time = null;
        System.out.print("Time (HH:MM:SS, leave empty for current time): ");
        String timeInput = scanner.nextLine().trim();
        if (timeInput.isEmpty()) {
            time = LocalTime.now();
        } else {
            try {
                time = LocalTime.parse(timeInput, TIME_FORMATTER);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid time format. Using current time.");
                time = LocalTime.now();
            }
        }

        Transaction transaction = new Transaction(
                transactions.size() + 1,
                person,
                description,
                amount,
                type.equalsIgnoreCase("debit") ? TransactionType.DEBIT : TransactionType.CREDIT,
                date,
                time);

        transactions.add(transaction);
        System.out.println("\033[0;32mTransaction added successfully.\033[0m");
        saveTransactions();
    }

    private static void deleteTransaction() {
        System.out.println("\n\033[0;34m===== Delete Transaction =====\033[0m");
        displayAllTransactions();

        System.out.print("Enter ID of transaction to delete: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());

            Optional<Transaction> transactionToRemove = transactions.stream()
                    .filter(t -> t.getId() == id)
                    .findFirst();

            if (transactionToRemove.isPresent()) {
                transactions.remove(transactionToRemove.get());
                System.out.println("\033[0;32mTransaction deleted successfully.\033[0m");
                saveTransactions();
            } else {
                System.out.println("Transaction not found.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a valid ID.");
        }
    }

    private static void editTransaction() {
        System.out.println("\n\033[0;34m===== Edit Transaction =====\033[0m");
        displayAllTransactions();

        System.out.print("Enter ID of transaction to edit: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());

            Optional<Transaction> transactionToEdit = transactions.stream()
                    .filter(t -> t.getId() == id)
                    .findFirst();

            if (transactionToEdit.isPresent()) {
                Transaction transaction = transactionToEdit.get();

                System.out.println("Editing transaction: " + transaction);
                System.out.println("Leave field empty to keep current value.");

                System.out.print("To/From [" + transaction.getPerson() + "]: ");
                String person = scanner.nextLine().trim();
                if (!person.isEmpty()) {
                    transaction.setPerson(person);
                }

                System.out.print("2. Description: ");
                System.out.print("Description [" + transaction.getDescription() + "]: ");
                String description = scanner.nextLine().trim();
                if (!description.isEmpty()) {
                    transaction.setDescription(description);
                }

                System.out.print("3. Amount: ");
                System.out.print("Amount [" + transaction.getAmount() + "]: ");
                String amountStr = scanner.nextLine().trim();
                if (!amountStr.isEmpty()) {
                    try {
                        transaction.setAmount(new BigDecimal(amountStr));
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid amount. Keeping current value.");
                    }
                }

                System.out.print("4. Type (debit/credit): ");
                System.out.print("Type (debit/credit) [" + transaction.getType() + "]: ");
                String type = scanner.nextLine().trim();
                if (!type.isEmpty()) {
                    if (type.equalsIgnoreCase("debit")) {
                        transaction.setType(TransactionType.DEBIT);
                    } else if (type.equalsIgnoreCase("credit")) {
                        transaction.setType(TransactionType.CREDIT);
                        if (type.equalsIgnoreCase("debit") || type.equalsIgnoreCase("credit")) {
                            transaction.setType(
                                    type.equalsIgnoreCase("debit") ? TransactionType.DEBIT : TransactionType.CREDIT);
                        } else {
                            System.out.println("Invalid type. Keeping current value.");
                        }
                    }

                    System.out.print("5. Date (YYYY-MM-DD): ");
                    System.out.print("Date (YYYY-MM-DD) [" + transaction.getDate().format(DATE_FORMATTER) + "]: ");
                    String dateStr = scanner.nextLine().trim();
                    if (!dateStr.isEmpty()) {
                        try {
                            transaction.setDate(LocalDate.parse(dateStr, DATE_FORMATTER));
                        } catch (DateTimeParseException e) {
                            System.out.println("Invalid date format. Keeping current value.");
                        }
                    }

                    System.out.print("6. Time (HH:MM:SS): ");
                    System.out.print("Time (HH:MM:SS) [" + transaction.getTime().format(TIME_FORMATTER) + "]: ");
                    String timeStr = scanner.nextLine().trim();
                    if (!timeStr.isEmpty()) {
                        try {
                            transaction.setTime(LocalTime.parse(timeStr, TIME_FORMATTER));
                        } catch (DateTimeParseException e) {
                            System.out.println("Invalid time format. Keeping current value.");
                        }
                    }

                    System.out.println("\033[0;32mTransaction updated successfully.\033[0m");
                    saveTransactions();
                } else {
                    System.out.println("Transaction not found.");
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a valid ID.");
        }
    }

    private static void queryTransactions() {
        System.out.println("\n\033[0;34m===== Query Transactions =====\033[0m");
        System.out.println("1. All Transactions");
        System.out.println("2. Custom Query");
        System.out.print("\033[93mEnter your choice: \033[0m ");

        try {
            int choice = Integer.parseInt(scanner.nextLine().trim());
            
            switch (choice) {
                case 1:
                    displayAllTransactions();
                    break;
                case 2:
                    customQuery();
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a number.");
        }
        clearScreen();
    }

    private static void customQuery() {
        System.out.println("\n\033[0;34m===== Custom Query =====\033[0m");

        // Initialize filters
        String personFilter = null;
        String descriptionFilter = null;
        BigDecimal minAmount = null;
        BigDecimal maxAmount = null;
        TransactionType typeFilter = null;
        LocalDate startDate = null;
        LocalDate endDate = null;
        LocalTime startTime = null;
        LocalTime endTime = null;

        // Gather filter parameters
        System.out.println("Enter filter criteria (leave empty to skip):");

        System.out.print("1. By Person: ");
        String person = scanner.nextLine().trim();
        if (!person.isEmpty()) {
            personFilter = person;
        }

        System.out.print("2. Description (exact match): ");
        String description = scanner.nextLine().trim();
        if (!description.isEmpty()) {
            descriptionFilter = description;
        }

        System.out.print("3. Amount Range - Minimum: ");
        String minAmountStr = scanner.nextLine().trim();
        if (!minAmountStr.isEmpty()) {
            try {
                minAmount = new BigDecimal(minAmountStr);
            } catch (NumberFormatException e) {
                System.out.println("Invalid amount. Ignoring this filter.");
            }
        }

        System.out.print("   Amount Range - Maximum: ");
        String maxAmountStr = scanner.nextLine().trim();
        if (!maxAmountStr.isEmpty()) {
            try {
                maxAmount = new BigDecimal(maxAmountStr);
            } catch (NumberFormatException e) {
                System.out.println("Invalid amount. Ignoring this filter.");
            }
        }

        System.out.print("4. Type (debit/credit): ");
        String type = scanner.nextLine().trim();
        if (!type.isEmpty()) {
            if (type.equalsIgnoreCase("debit")) {
                typeFilter = TransactionType.DEBIT;
            } else if (type.equalsIgnoreCase("credit")) {
                typeFilter = TransactionType.CREDIT;
            } else {
                System.out.println("Invalid type. Ignoring this filter.");
            }
        }

        System.out.print("5. Date Range - Start (YYYY-MM-DD): ");
        String startDateStr = scanner.nextLine().trim();
        if (!startDateStr.isEmpty()) {
            try {
                startDate = LocalDate.parse(startDateStr, DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Ignoring this filter.");
            }
        }

        System.out.print("   Date Range - End (YYYY-MM-DD): ");
        String endDateStr = scanner.nextLine().trim();
        if (!endDateStr.isEmpty()) {
            try {
                endDate = LocalDate.parse(endDateStr, DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Ignoring this filter.");
            }
        }

        System.out.print("6. Time Range - Start (HH:MM:SS): ");
        String startTimeStr = scanner.nextLine().trim();
        if (!startTimeStr.isEmpty()) {
            try {
                startTime = LocalTime.parse(startTimeStr, TIME_FORMATTER);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid time format. Ignoring this filter.");
            }
        }

        System.out.print("   Time Range - End (HH:MM:SS): ");
        String endTimeStr = scanner.nextLine().trim();
        if (!endTimeStr.isEmpty()) {
            try {
                endTime = LocalTime.parse(endTimeStr, TIME_FORMATTER);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid time format. Ignoring this filter.");
            }
        }

        // Apply filters - corrected code with final variables for lambda expressions
        final String finalPersonFilter = personFilter;
        final String finalDescriptionFilter = descriptionFilter;
        final BigDecimal finalMinAmount = minAmount;
        final BigDecimal finalMaxAmount = maxAmount;
        final TransactionType finalTypeFilter = typeFilter;
        final LocalDate finalStartDate = startDate;
        final LocalDate finalEndDate = endDate;
        final LocalTime finalStartTime = startTime;
        final LocalTime finalEndTime = endTime;

        List<Transaction> filteredTransactions = transactions.stream()
                .filter(t -> finalPersonFilter == null
                        || t.getPerson().toLowerCase().contains(finalPersonFilter.toLowerCase()))
                .filter(t -> finalDescriptionFilter == null || t.getDescription().equals(finalDescriptionFilter))
                .filter(t -> finalMinAmount == null || t.getAmount().compareTo(finalMinAmount) >= 0)
                .filter(t -> finalMaxAmount == null || t.getAmount().compareTo(finalMaxAmount) <= 0)
                .filter(t -> finalTypeFilter == null || t.getType() == finalTypeFilter)
                .filter(t -> finalStartDate == null || !t.getDate().isBefore(finalStartDate))
                .filter(t -> finalEndDate == null || !t.getDate().isAfter(finalEndDate))
                .filter(t -> finalStartTime == null || !t.getTime().isBefore(finalStartTime))
                .filter(t -> finalEndTime == null || !t.getTime().isAfter(finalEndTime))
                .collect(Collectors.toList());

        // Display results
        System.out.println("\n===== Query Results =====");
        if (filteredTransactions.isEmpty()) {
            System.out.println("No transactions found matching the criteria.");
        } else {
            filteredTransactions.forEach(System.out::println);
            System.out.println("\nTotal transactions: " + filteredTransactions.size());

            // Calculate totals
            BigDecimal totalDebit = filteredTransactions.stream()
                    .filter(t -> t.getType() == TransactionType.DEBIT)
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalCredit = filteredTransactions.stream()
                    .filter(t -> t.getType() == TransactionType.CREDIT)
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            System.out.println("Total Debit: " + totalDebit);
            System.out.println("Total Credit: " + totalCredit);
            System.out.println("Net Balance: " + totalCredit.subtract(totalDebit));
        }
    }

    private static void showInfo() {
        System.out.println("\n===== Information =====");
        System.out.println("1. Today's Spending/Credit");
        System.out.println("2. Weekly Spending/Credit");
        System.out.println("3. Monthly Spending/Credit");
        System.out.print("\033[93mEnter your choice: \033[0m ");

        try {
            int choice = Integer.parseInt(scanner.nextLine().trim());
            LocalDate today = LocalDate.now();

            List<Transaction> filteredTransactions;

            switch (choice) {
                case 1:
                    // Today's transactions
                    filteredTransactions = transactions.stream()
                            .filter(t -> t.getDate().equals(today))
                            .collect(Collectors.toList());
                    displaySummary("Today's", filteredTransactions);
                    break;
                case 2:
                    // This week's transactions (last 7 days)
                    LocalDate weekStart = today.minusDays(6);
                    filteredTransactions = transactions.stream()
                            .filter(t -> !t.getDate().isBefore(weekStart) && !t.getDate().isAfter(today))
                            .collect(Collectors.toList());
                    displaySummary("This Week's", filteredTransactions);
                    break;
                case 3:
                    // This month's transactions
                    LocalDate monthStart = today.withDayOfMonth(1);
                    filteredTransactions = transactions.stream()
                            .filter(t -> !t.getDate().isBefore(monthStart) && !t.getDate().isAfter(today))
                            .collect(Collectors.toList());
                    displaySummary("This Month's", filteredTransactions);
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a number.");
        }
        clearScreen();
    }

    private static void displaySummary(String period, List<Transaction> filteredTransactions) {
        System.out.println("\n===== " + period + " Summary =====");

        if (filteredTransactions.isEmpty()) {
            System.out.println("No transactions found for this period.");
            return;
        }

        // Calculate totals
        BigDecimal totalDebit = filteredTransactions.stream()
                .filter(t -> t.getType() == TransactionType.DEBIT)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCredit = filteredTransactions.stream()
                .filter(t -> t.getType() == TransactionType.CREDIT)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Count transactions
        long debitCount = filteredTransactions.stream()
                .filter(t -> t.getType() == TransactionType.DEBIT)
                .count();

        long creditCount = filteredTransactions.stream()
                .filter(t -> t.getType() == TransactionType.CREDIT)
                .count();

        // Display summary
        System.out.println("Total Transactions: " + filteredTransactions.size());
        System.out.println("Total Debit Transactions: " + debitCount);
        System.out.println("Total Credit Transactions: " + creditCount);
        System.out.println("Total Debit Amount: " + totalDebit);
        System.out.println("Total Credit Amount: " + totalCredit);
        System.out.println("Net Balance: " + totalCredit.subtract(totalDebit));

        // Display individual transactions
        System.out.println("\nTransactions:");
        filteredTransactions.forEach(System.out::println);
    }

    private static void displayAllTransactions() {
        System.out.println("\n===== All Transactions =====");
        if (transactions.isEmpty()) {
            System.out.println("No transactions found.");
        } else {
            transactions.forEach(System.out::println);
            System.out.println("\nTotal transactions: " + transactions.size());
        }
    }

    @SuppressWarnings("unchecked")
    private static void loadTransactions() {
        File file = new File(DATA_FILE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                transactions = (List<Transaction>) ois.readObject();
                System.out.println("Loaded " + transactions.size() + " transactions.");
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Error loading transactions: " + e.getMessage());
                transactions = new ArrayList<>();
            }
        } else {
            System.out.println("No saved transactions found. Starting with empty database.");
            transactions = new ArrayList<>();
        }
    }

    private static void saveTransactions() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(transactions);
        } catch (IOException e) {
            System.out.println("Error saving transactions: " + e.getMessage());
        }
    }
}

enum TransactionType {
    DEBIT, CREDIT
}

class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String person;
    private String description;
    private BigDecimal amount;
    private TransactionType type;
    private LocalDate date;
    private LocalTime time;
    private static boolean headerPrinted = false;

    public Transaction(int id, String person, String description, BigDecimal amount, TransactionType type,
            LocalDate date, LocalTime time) {
        this.id = id;
        this.person = person;
        this.description = description;
        this.amount = amount;
        this.type = type;
        this.date = date;
        this.time = time;
    }

    public int getId() {
        return id;
    }

    public String getPerson() {
        return person;
    }

    public void setPerson(String person) {
        this.person = person;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
    
        // Print header only once
        if (!headerPrinted) {
            sb.append(String.format("%-5s | %-12s | %-20s | %-10s | %-10s | %-12s | %-8s\n",
                    "ID", "Person", "Description", "Amount", "Type", "Date", "Time"));
            sb.append("-".repeat(90)).append("\n");
            headerPrinted = true;
        }
    
        // Print the actual row data with proper spacing
        sb.append(String.format("%-5d | %-12s | %-20s | %-10s | %-10s | %-12s | %-8s",
                id, person, description.length() > 20 ? description.substring(0, 17) + "..." : description,
                amount.toString(), type,
                date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                time.format(DateTimeFormatter.ofPattern("HH:mm:ss"))));
    
        return sb.toString();
    }
}