
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import static org.junit.Assert.*;

/**
 *
 * @author Debanshu
 */
public class MaxFeeTxHandlerTest {

    public static MaxFeeTxHandler txHandler;
    public static Transaction[] txs;
    public static Signature signature;

    private static Transaction createTransaction(
            int[] inpIndexes,
            Transaction[] inpTransactions,
            PrivateKey[] privateKeys,
            double[] outValues,
            PublicKey[] publicKeys) {
        //init new transaction
        Transaction tx = new Transaction();
        
        //add the transaction inputs
        IntStream.range(0, inpIndexes.length)
                .forEach(idx -> {
                    tx.addInput(inpTransactions[idx].getHash(), inpIndexes[idx]);
                });

        // add the transaction outputs
        IntStream.range(0, outValues.length)
                .forEach(idx -> {
                    tx.addOutput(outValues[idx], publicKeys[idx]);
                });
        
        //sign the input indexes with private keys
        IntStream.range(0, inpIndexes.length)
                .forEach(idx -> {
                    try {
                        signature.initSign(privateKeys[idx]);
                        signature.update(tx.getRawDataToSign(idx));
                        tx.addSignature(signature.sign(), idx);
                    } catch (InvalidKeyException ex) {
                        Logger.getLogger(MaxFeeTxHandlerTest.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (SignatureException ex) {
                        Logger.getLogger(MaxFeeTxHandlerTest.class.getName()).log(Level.SEVERE, null, ex);
                    }
                });

        //finalize and return transaction
        tx.finalize();
        return tx;
    }

    public MaxFeeTxHandlerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws InvalidKeyException, SignatureException, NoSuchAlgorithmException, NoSuchProviderException {
        // Crypto setup
        // You need the following JAR for RSA http://www.bouncycastle.org/download/bcprov-jdk15on-156.jar
        // More information https://en.wikipedia.org/wiki/Bouncy_Castle_(cryptography)
        Security.addProvider(new BouncyCastleProvider());
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
        keyGen.initialize(1024, random);

        // Generating two key pairs, one for Scrooge and one for Alice
        KeyPair pair = keyGen.generateKeyPair();
        PrivateKey private_key_scrooge = pair.getPrivate();
        PublicKey public_key_scrooge = pair.getPublic();

        pair = keyGen.generateKeyPair();
        PrivateKey private_key_alice = pair.getPrivate();
        PublicKey public_key_alice = pair.getPublic();

        // START - ROOT TRANSACTION
        // Generating a root transaction tx out of thin air, so that Scrooge owns a coin of value 10
        // By thin air I mean that this tx will not be validated, I just need it to get a proper Transaction.Output
        // which I then can put in the UTXOPool, which will be passed to the TXHandler
        Transaction rootTransaction = new Transaction();
        rootTransaction.addOutput(10, public_key_scrooge);

        // that value has no meaning, but tx.getRawDataToSign(0) will access in.prevTxHash;
        byte[] initialHash = BigInteger.valueOf(1695609641).toByteArray();
        rootTransaction.addInput(initialHash, 0);

        signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(private_key_scrooge);
        signature.update(rootTransaction.getRawDataToSign(0));
        byte[] sig = signature.sign();

        rootTransaction.addSignature(sig, 0);
        rootTransaction.finalize();
        // END - ROOT TRANSACTION

        // The transaction output of the root transaction is unspent output
        UTXOPool utxoPool = new UTXOPool();
        UTXO utxo = new UTXO(rootTransaction.getHash(), 0);
        utxoPool.addUTXO(utxo, rootTransaction.getOutput(0));

        // START - PROPER TRANSACTION
        Transaction tx1 = createTransaction(new int[]{0},
                new Transaction[]{rootTransaction},
                new PrivateKey[]{private_key_scrooge},
                new double[]{5, 3, 1},
                new PublicKey[]{public_key_alice, public_key_alice, public_key_alice});
        
        Transaction tx2 = createTransaction(new int[]{0},
                new Transaction[]{rootTransaction},
                new PrivateKey[]{private_key_scrooge},
                new double[]{4, 3, 1},
                new PublicKey[]{public_key_alice, public_key_alice, public_key_alice});

        // remember that the utxoPool contains a single unspent Transaction.Output which is the coin from Scrooge
        txHandler = new MaxFeeTxHandler(utxoPool);
        txs = new Transaction[]{tx1,tx2};
    }

    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Test of isValidTx method, of class MaxFeeTxHandler.
     */
    @org.junit.Test
    public void testIsValidTx() {
        assertEquals(true, txHandler.isValidTx(txs[0]));
    }

    /**
     * Test of handleTxs method, of class MaxFeeTxHandler.
     */
    @org.junit.Test
    public void testHandleTxs() {
        Transaction[] expResult = new Transaction[]{txs[1]};
        Transaction[] result = txHandler.handleTxs(txs);
        assertArrayEquals(expResult, result);
    }

}
