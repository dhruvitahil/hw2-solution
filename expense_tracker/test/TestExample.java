import static org.junit.Assert.*;

import model.Filter.AmountFilter;
import model.Filter.CategoryFilter;
import org.junit.Before;
import org.junit.Test;
import model.Transaction;
import controller.ExpenseTrackerController;
import model.ExpenseTrackerModel;
import view.ExpenseTrackerView;

import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import java.text.ParseException;
import java.util.*;
import java.util.List;

public class TestExample{

    private ExpenseTrackerModel model;
    private ExpenseTrackerView view;
    private ExpenseTrackerController controller;

    @Before
    public void setup() {
        model = new ExpenseTrackerModel();
        view = new ExpenseTrackerView();
        controller = new ExpenseTrackerController(model, view);
    }

    @Test
    public void testAddTransaction() {
        assertEquals(0, model.getTransactions().size());

        double amount = 50.00;
        String category = "food";

        assertTrue(controller.addTransaction(amount, category));
        assertEquals(1, model.getTransactions().size());

        Transaction firstTransaction = model.getTransactions().get(0);
        checkTransaction(amount, category, firstTransaction);
        assertEquals(amount, getTotalCost(), 0.01);
    }

    @Test
    public void testRemoveTransaction() {
        assertEquals(0, model.getTransactions().size());

        double amount = 50.0;
        String category = "food";
        Transaction addedTransaction = new Transaction(amount, category);
        model.addTransaction(addedTransaction);

        assertEquals(1, model.getTransactions().size());
        Transaction firstTransaction = model.getTransactions().get(0);
        checkTransaction(amount, category, firstTransaction);

        assertEquals(amount, getTotalCost(), 0.01);

        model.removeTransaction(addedTransaction);

        List<Transaction> transactions = model.getTransactions();
        assertEquals(0, transactions.size());

        double totalCost = getTotalCost();
        assertEquals(0.00, totalCost, 0.01);
    }
    @Test
    public void testInvalidInputHandling() {
        assertEquals(0, model.getTransactions().size());

        double invalidAmt = -50.0;
        String invalidCtgry = "xyz";


        try {
            Transaction addedTransaction = new Transaction(invalidAmt, invalidCtgry);
            model.addTransaction(addedTransaction);
        }
        catch (IllegalArgumentException e) {
            assertEquals("The amount is not valid.", e.getMessage());
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }
    

@Test
    public void testFilterByAmount() {
        assertEquals(0, model.getTransactions().size());

        double amt1 = 30.00;
        double amt2 = 60.00;
        double filterValue = 30.00;

        controller.addTransaction(amt1, "food");
        controller.addTransaction(amt2, "bills");

        controller.setFilter(new AmountFilter(filterValue));
        controller.applyFilter();

         List<Transaction> transactions = model.getTransactions();
        for (int i = 0; i < transactions.size(); i++) {
            boolean exHighlight = (i == 0); 
            Component c = view.transactionsTable.getDefaultRenderer(Object.class)
                    .getTableCellRendererComponent(view.transactionsTable, null, false, false, i, 0);
            Color bgColor = c.getBackground();
            Color exColor = exHighlight ? new Color(173, 255, 168) : view.transactionsTable.getBackground();
            assertEquals(exColor, bgColor);
        }
    }

    @Test
    public void testFilterByCategory() {
        assertEquals(0, model.getTransactions().size());

        String ctgry1 = "food";
        String ctgry2 = "bills";
        String filterCtgry = "food";

        controller.addTransaction(50.00, ctgry1);
        controller.addTransaction(60.00, ctgry2);

        controller.setFilter(new CategoryFilter(filterCtgry));
        controller.applyFilter();

        List<Transaction> transactions = model.getTransactions();
        for (int i = 0; i < transactions.size(); i++) {
            boolean exHighlight = (i == 0); 
            Component c = view.transactionsTable.getDefaultRenderer(Object.class)
                    .getTableCellRendererComponent(view.transactionsTable, null, false, false, i, 0);
            Color bgColor = c.getBackground();
            Color exColor = exHighlight ? new Color(173, 255, 168) : view.transactionsTable.getBackground();
            assertEquals(exColor, bgColor);
        }
    }

    @Test
    public void testUndoDisallowed() {
        assertEquals(0, model.getTransactions().size());
        int selectedRow = 0;
        assertFalse(controller.removeSelectedTransaction(selectedRow));
        assertEquals(0, model.getTransactions().size());
    }

    @Test
    public void testUndoAllowed() {
        assertEquals(0, model.getTransactions().size());

        double amount = 50.00;
        String category = "food";

        assertTrue(controller.addTransaction(amount, category));
        assertEquals(1, model.getTransactions().size());
        assertEquals(amount, getTotalCost(), 0.01);

        int selectedRow = 0;
        assertTrue(controller.removeSelectedTransaction(selectedRow));
        assertEquals(0, model.getTransactions().size());
        assertEquals(0.00, getTotalCost(), 0.01);
    }

    private double getTotalCost() {
        double totalCost = 0.0;
        List<Transaction> allTransactions = model.getTransactions();
        for (Transaction transaction : allTransactions) {
            totalCost += transaction.getAmount();
        }
        return totalCost;
    }

    private void checkTransaction(double amount, String category, Transaction transaction) {
        assertEquals(amount, transaction.getAmount(), 0.01);
        assertEquals(category, transaction.getCategory());
        String transactionDateString = transaction.getTimestamp();
        Date transactionDate = null;
        try {
            transactionDate = Transaction.dateFormatter.parse(transactionDateString);
        }
        catch (ParseException pe) {
            pe.printStackTrace();
            transactionDate = null;
        }
        Date nowDate = new Date();
        assertNotNull(transactionDate);
        assertNotNull(nowDate);
        assertTrue(nowDate.getTime() - transactionDate.getTime() < 60000);
    }
}