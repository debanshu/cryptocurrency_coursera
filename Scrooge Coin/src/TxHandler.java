
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

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
        Set<UTXO> UTXOSet = new HashSet<>();
        double inputSum = 0; 
        
        // (1) all outputs claimed by {@code tx} are in the current UTXO pool        
        // basically check all inputs must be in the UTXOPool
        // so that they can be claimed in this tx
        for(int idx= 0; idx < tx.numInputs(); idx++) { //get input indices
            //get input at idx
            Transaction.Input inp = tx.getInput(idx);
            // create UTXO temporaily with tx hash and index
            UTXO temp = new UTXO(inp.prevTxHash,inp.outputIndex);
            
            //check if UTXO exist in pool, else return false
            if(!ledger.contains(temp))
                return false;
            
            // (2) the signatures on each input of {@code tx} are valid
            //since it exists check if the signature is valid
            //get raw data
            byte[] msg = tx.getRawDataToSign(idx);
            //get signature
            byte[] sig = inp.signature;
            //get public key
            //go into UTXOPool and get the output
            PublicKey pk = ledger.getTxOutput(temp).address;
            //verify signatures, if not verified return false
            if(!Crypto.verifySignature(pk, msg, sig))
                return false;  
            
            // (3) no UTXO is claimed multiple times by {@code tx}
            // store the utxo in a set and check if it exists
            if(UTXOSet.contains(temp))
                return false;
            UTXOSet.add(temp);
            
            // (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
            // sum all the inputSum
            inputSum += ledger.getTxOutput(temp).value;
        }        
       
        double outputSum = 0;
        // (4) all of {@code tx}s output values are non-negative
        for(Transaction.Output o:tx.getOutputs()) {
            if(o.value < 0)
                return false;
            outputSum += o.value;
        }       
        
        // (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output               
        if(!(inputSum >= outputSum))
            return false;
        
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
        return Arrays.stream(possibleTxs)
                .filter( tx -> isValidTx(tx))
                .map( tx -> {
                    //update UTXOPool ledger
                    
                    //remove all inputs from the ledger, since they are not 'spent'
                    for(Transaction.Input inp:tx.getInputs()) {
                        ledger.removeUTXO(new UTXO(inp.prevTxHash,inp.outputIndex));
                    }
                    
                    //add all outputs to the ledger, since they are 'unspent'
                    for(int idx= 0; idx < tx.numOutputs(); idx++) { //get output indexes
                        Transaction.Output o = tx.getOutput(idx);
                        ledger.addUTXO(new UTXO(tx.getHash(), idx), o);
                    }
                    return tx;
                })
                .toArray(Transaction[]::new);
                       
                
    }

}
