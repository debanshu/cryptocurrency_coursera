/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Debanshu
 */
public class MaxFeeTxHandlerTest {
    
    public MaxFeeTxHandlerTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Test of isValidTx method, of class MaxFeeTxHandler.
     */
    @org.junit.Test
    public void testIsValidTx() {
        System.out.println("isValidTx");
        Transaction tx = null;
        UTXOPool ledger = null;
        MaxFeeTxHandler instance = null;
        boolean expResult = false;
        boolean result = instance.isValidTx(tx, ledger);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of handleTxs method, of class MaxFeeTxHandler.
     */
    @org.junit.Test
    public void testHandleTxs() {
        System.out.println("handleTxs");
        Transaction[] possibleTxs = null;
        MaxFeeTxHandler instance = null;
        Transaction[] expResult = null;
        Transaction[] result = instance.handleTxs(possibleTxs);
        assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
