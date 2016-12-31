
import java.security.PublicKey;

public class TxHandler {
    
    public UTXOPool ledger;

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
        // IMPLEMENT THIS
        this.ledger = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        // IMPLEMENT THIS
        // (1) all outputs claimed by {@code tx} are in the current UTXO pool
        
        for(int idx= 0; idx < tx.numOutputs(); idx++) { //get output indexes
            // create UTXO temporaily with tx hash and index
            UTXO temp = new UTXO(tx.getHash(),idx);
            //check if UTXO exist in pool, else return false
            if(!ledger.contains(temp))
                return false;
        }
        
        // (2) the signatures on each input of {@code tx} are valid
        for(int idx= 0; idx < tx.numInputs(); idx++) { //get input indexes
            //get raw data
            byte[] msg = tx.getRawDataToSign(idx);
            //get signature
            byte[] sig = tx.getInput(idx).signature;
            //get public key
            PublicKey pk = tx.getOutput(tx.getInput(idx).outputIndex).address;
            //verify signatures, if not verified return false
            if(!Crypto.verifySignature(pk, msg, sig))
                return false;            
        }
        
        // (3) no UTXO is claimed multiple times by {@code tx}
        
        
        
        //all checks passed
        return true;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        // IMPLEMENT THIS
    }

}
