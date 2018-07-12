package com.blockchain.store.playmarket.utilities.crypto;

import android.util.Log;

import com.blockchain.store.playmarket.Application;
import com.blockchain.store.playmarket.data.entities.App;
import com.blockchain.store.playmarket.utilities.Constants;

import org.ethereum.geth.Account;
import org.ethereum.geth.Address;
import org.ethereum.geth.BigInt;
import org.ethereum.geth.Transaction;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Bytes;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Uint;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.AbiTypes;
import org.web3j.abi.datatypes.generated.Bytes23;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.crypto.ContractUtils;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.crypto.TransactionUtils;
import org.web3j.crypto.Wallet;
import org.web3j.crypto.WalletUtils;
import org.web3j.rlp.RlpDecoder;
import org.web3j.rlp.RlpEncoder;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.ethmobile.ethdroid.EthDroid;
import io.ethmobile.ethdroid.KeyManager;

import static com.blockchain.store.playmarket.utilities.Constants.GAS_LIMIT;
import static com.blockchain.store.playmarket.utilities.Constants.NODE_ADDRESS;
import static com.blockchain.store.playmarket.utilities.Constants.NON_LOCAL_NODE_ADDRESS;
import static com.blockchain.store.playmarket.utilities.Constants.RINKEBY_ID;
import static org.web3j.crypto.Hash.sha3;

/**
 * Created by samsheff on 24/08/2017.
 */

public class CryptoUtils {

    public static KeyManager keyManager;
    public static EthDroid ethdroid;

    public static String CONTRACT_ADDRESS = "0xf18418d6dc1a2278c69968b8b8a2d84b553fba51";
    public static final String TEST_ADDRESS = "0x5e5c1c8e03472666e0b9e218153869dcbc9c1e65";
    public static final String ICO_CONTRACT_ADDRESS = "0xEDC64A365e12054928dAC9bF32F1C1552EE9679F";


    public static KeyManager setupKeyManager(String dataDir) {
        return KeyManager.newKeyManager(dataDir);
    }

    public static void buildEtherNodeTestnet(String datadir) {
        try {
            keyManager = KeyManager.newKeyManager(datadir);

            ethdroid = new EthDroid.Builder(datadir)
                    .onTestnet()
                    .withDatadirPath(datadir)
                    .withKeyManager(KeyManager.newKeyManager(datadir))
                    .withDefaultContext()
                    .build();
            ethdroid.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getRawTransaction(Transaction transaction) {
        String transactionInfo = transaction.toString();
        Pattern pattern = Pattern.compile("Hex:.*");
        Matcher matcher = pattern.matcher(transactionInfo);
        if (matcher.find()) {
            return matcher.group(0).replaceAll("Hex:\\s*", "");
        } else {
            return "";
        }
    }

    public static byte[] getDataForBuyApp(String appId, String address) {
        byte[] hash = sha3("buyApp(uint256,address)".getBytes());
        appId = Integer.toHexString(Integer.valueOf(appId));
        String hashString = bytesToHexString(hash);
        String functionHash = hashString.substring(0, 8);

        Log.d("Ether", functionHash);
        Log.d("Ether", hashString);

        String appIdEnc = String.format("%64s", appId).replace(' ', '0');
        String catIdEnc = String.format("%64s", address).replace(' ', '0');

        Log.d("Ether", appIdEnc);
        Log.d("Ether", catIdEnc);

        String data = functionHash + appIdEnc + catIdEnc;
        Log.d("oldMethod", "result = " + data);

        return hexStringToByteArray(data);
    }

    public static byte[] getDataForBuyAppWithWeb3(String appId, String address) {
        String originalAddress = address;
        if (address.startsWith("0x")) {
            address = address.replaceFirst("0x", "");
        }
        ArrayList<Type> valueList = new ArrayList<>();
        valueList.add(new Uint(new BigInteger(appId)));
        valueList.add(new org.web3j.abi.datatypes.Address(address));

        List<TypeReference<?>> typeReferences = Arrays.asList(
                new TypeReference<org.web3j.abi.datatypes.Uint>() {
                },
                new TypeReference<org.web3j.abi.datatypes.Address>() {
                });
        Function function = new Function("buyApp",
                valueList, typeReferences);

        String encode = FunctionEncoder.encode(function);

        if (encode.startsWith("0x")) {
            encode = encode.replaceFirst("0x", "");
        }
        Log.d("newMethod", "result = " + encode);
        return hexStringToByteArray(encode);
    }

    public static byte[] getDataForReviewAnApp(String appId, String address, String vote, String description, int txIndex) {
        // function pushFeedbackRating(uint idApp, uint vote, string description, bytes32 txIndex)
        ArrayList<Type> arrayList = new ArrayList<>();
        arrayList.add(new Uint(new BigInteger(appId)));
        arrayList.add(new Uint(new BigInteger(vote)));
        arrayList.add(new Utf8String(description));
        if (txIndex != 0) {
            String s = Integer.toHexString(txIndex);
            byte[] bytes1 = hexStringToByteArray(s);
            arrayList.add(new Bytes32(new byte[1]));
        } else {
            arrayList.add(Bytes32.DEFAULT);
        }

        List<TypeReference<?>> typeReferences = Arrays.asList(
                new TypeReference<Uint>() {
                },
                new TypeReference<Uint>() {
                },
                new TypeReference<Utf8String>() {
                },
                new TypeReference<Bytes32>() {
                }
        );

        Function function = new Function("pushFeedbackRating",
                arrayList, typeReferences);

        String encode = FunctionEncoder.encode(function);
        Log.d("test", "generateAppBuyTransaction: " + encode);
        if (encode.startsWith("0x")) {
            encode = encode.replaceFirst("0x", "");
        }
        return hexStringToByteArray(encode);
    }

    public static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b & 0xff));
        }

        return sb.toString();
    }

    public static byte[] hexStringToByteArray(String s) {
        byte[] b = new byte[s.length() / 2];
        for (int i = 0; i < b.length; i++) {
            int index = i * 2;
            int v = Integer.parseInt(s.substring(index, index + 2), 16);
            b[i] = (byte) v;
        }
        Log.d("Ether", "hexStringToByteArray: " + b);
        return b;
    }

    public static String generateAppBuyTransaction(int nonce, BigInt gasPrice, App app) throws Exception {
        KeyManager keyManager = Application.keyManager;
        Account account = keyManager.getAccounts().get(0);

        BigInt price = new BigInt(0);
        price.setString(app.price, 10);

//        getDataForBuyAppWithWeb3(app.appId, NON_LOCAL_NODE_ADDRESS);
//        getDataForBuyApp(app.appId, NON_LOCAL_NODE_ADDRESS);
        /*

        * */
        Transaction transaction = new Transaction(nonce, new Address(Constants.REVIEW_ADDRESS),
                price, GAS_LIMIT, gasPrice,
                getDataForBuyAppWithWeb3(app.appId, NON_LOCAL_NODE_ADDRESS));

        Transaction signedTransaction = keyManager.getKeystore().signTx(account, transaction, new BigInt(RINKEBY_ID));
        return getRawTransaction(signedTransaction);
    }

    public static String generateSendReviewTransaction(int nonce, BigInt gasPrice, App app, String vote, String description, int txIndex) throws Exception {
        KeyManager keyManager = Application.keyManager;
        Account account = keyManager.getAccounts().get(0);
        BigInt price = new BigInt(0);

        Transaction transaction = new Transaction(nonce, new Address(Constants.REVIEW_ADDRESS),
                price, GAS_LIMIT, gasPrice,
                getDataForReviewAnApp(app.appId, account.getAddress().getHex(), vote, description, txIndex));
        Transaction signedTransaction = keyManager.getKeystore().signTx(account, transaction, new BigInt(RINKEBY_ID));
        return getRawTransaction(signedTransaction);
    }

    public static String generateInvestTransaction(int nonce, BigInt gasPrice, String investPrice) throws Exception {
        KeyManager keyManager = Application.keyManager;
        Account account = keyManager.getAccounts().get(0);

        BigInt price = new BigInt(0);
        price.setString(investPrice, 10);

        Transaction transaction = new Transaction(nonce, new Address(Constants.INVEST_ADDRESS),
                price, GAS_LIMIT, gasPrice, null);
        Transaction signedTransaction = keyManager.getKeystore().signTx(account, transaction, new BigInt(RINKEBY_ID));

        return getRawTransaction(signedTransaction);
    }

    public static String generateInvestTransactionWithAddress(int nonce, BigInt gasPrice, String investPrice, String address) throws Exception {
        BigInt price = new BigInt(0);
        price.setString(investPrice, 10);

        KeyManager keyManager = Application.keyManager;
        Account account = keyManager.getAccounts().get(0);
        Transaction transaction = new Transaction(nonce, new Address(Constants.INVEST_ADDRESS),
                price, GAS_LIMIT, gasPrice, null);
        Transaction signedTransaction = keyManager.getKeystore().signTx(account, transaction, new BigInt(RINKEBY_ID));
        return getRawTransaction(signedTransaction);
    }

    public static String generateTransferTransaction(int nonce, String gasPrice, String transferAmount, String recipientAddress) throws Exception {
        BigInt price = new BigInt(0);
        price.setString(transferAmount, 10);

        KeyManager keyManager = Application.keyManager;
        Account account = keyManager.getAccounts().get(0);

        Transaction transaction = new Transaction(nonce, new Address(recipientAddress),
                price, GAS_LIMIT, new BigInt(Long.parseLong(gasPrice)), null);
        Transaction signedTransaction = keyManager.getKeystore().signTx(account, transaction, new BigInt(RINKEBY_ID));
        return getRawTransaction(signedTransaction);
    }

}

