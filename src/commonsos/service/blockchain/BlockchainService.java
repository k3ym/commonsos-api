package commonsos.service.blockchain;

import static commonsos.service.UserService.WALLET_PASSWORD;
import static java.lang.String.format;
import static java.math.BigInteger.ONE;
import static java.math.BigInteger.TEN;
import static org.web3j.tx.TransactionManager.DEFAULT_POLLING_ATTEMPTS_PER_TX_HASH;
import static org.web3j.tx.TransactionManager.DEFAULT_POLLING_FREQUENCY;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Wallet;
import org.web3j.crypto.WalletFile;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.ChainId;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.ReadonlyTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.Transfer;
import org.web3j.tx.gas.StaticGasProvider;
import org.web3j.tx.response.NoOpProcessor;
import org.web3j.tx.response.PollingTransactionReceiptProcessor;
import org.web3j.utils.Convert.Unit;
import org.web3j.utils.Files;

import com.fasterxml.jackson.databind.ObjectMapper;

import commonsos.Cache;
import commonsos.Configuration;
import commonsos.exception.DisplayableException;
import commonsos.exception.ServerErrorException;
import commonsos.repository.CommunityRepository;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.User;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class BlockchainService {

  public static final BigInteger ETHER_TRANSFER_GAS_LIMIT = new BigInteger("21000");
  public static final BigInteger TOKEN_TRANSFER_GAS_LIMIT = new BigInteger("90000");
  public static final BigInteger TOKEN_TRANSFER_FROM_GAS_LIMIT = new BigInteger("90000");
  public static final BigInteger TOKEN_APPROVE_GAS_LIMIT = new BigInteger("90000");
  public static final BigInteger TOKEN_DEPLOYMENT_GAS_LIMIT = new BigInteger("4700000");
  public static final BigInteger GAS_PRICE = new BigInteger("18000000000");

  private static final int NUMBER_OF_DECIMALS = 18;
  private static final BigInteger MAX_UINT_256 = new BigInteger("2").pow(256);
  public static final BigInteger INITIAL_TOKEN_AMOUNT = MAX_UINT_256.divide(TEN.pow(NUMBER_OF_DECIMALS)).subtract(ONE);

  @Inject CommunityRepository communityRepository;
  @Inject ObjectMapper objectMapper;
  @Inject Web3j web3j;
  @Inject Cache cache;
  @Inject Configuration config;

  public boolean isConnected() {
    try {
      web3j.ethBlockNumber().send();
      return true;
    }
    catch (Exception e) {
      log.warn("Blockchain error "+ e.getMessage());
      return false;
    }
  }

  public boolean isAllowed(User user, Community community, BigInteger floor) {
    try {
      Token token = loadTokenReadOnly(community.getTokenContractAddress());
      BigInteger allowance = token.allowance(user.getWalletAddress(), community.getAdminUser().getWalletAddress()).send();
      log.info(String.format("Token allowance info. communityId=%d, userId=%d, spenderId=%d, allowance=%d", community.getId(), user.getId(), community.getAdminUser().getId(), allowance));
      return allowance.compareTo(floor) >= 0;
    } catch (Exception e) {
      throw new ServerErrorException(e);
    }
  }

  public String createWallet(String password) {
    File filePath = null;
    try {
      File tmp = tempDir();
      String fileName = WalletUtils.generateFullNewWalletFile(password, tmp);

      filePath = Paths.get(tmp.getAbsolutePath(), fileName).toFile();
      return Files.readString(filePath);
    } catch (Exception e) {
      throw new ServerErrorException(e);
    } finally {
      if (filePath != null) filePath.delete();
    }
  }

  File tempDir() {
    return new File(System.getProperty("java.io.tmpdir"));
  }

  public Credentials credentials(String wallet, String password) {
    try {
      WalletFile walletFile = objectMapper.readValue(wallet, WalletFile.class);
      ECKeyPair keyPair = Wallet.decrypt(password, walletFile);
      return Credentials.create(keyPair);
    } catch (Exception e) {
      throw new ServerErrorException(e);
    }
  }

  public Credentials systemCredentials() {
    try {
      return WalletUtils.loadCredentials(config.systemWalletPassword(), new File(config.systemWallet()));
    } catch (Exception e) {
      throw new ServerErrorException(e);
    }
  }

  public String transferTokens(User remitter, User beneficiary, Long communityId, BigDecimal amount) {
    return communityRepository.isAdmin(remitter.getId(), communityId) ?
      transferTokensAdmin(remitter, beneficiary, communityId, amount) :
      transferTokensRegular(remitter, beneficiary, communityId, amount);
  }

  private String transferTokensRegular(User remitter, User beneficiary, Long communityId, BigDecimal amount) {
    Community community = communityRepository.findPublicStrictById(communityId);
    User walletUser = community.getAdminUser();
    
    if (!isAllowed(remitter, community, toTokensWithoutDecimals(amount))) {
      String message = String.format(
          "community admin is not allowed to send token. [community=%s, user=%s]",
          community.getName(),
          remitter.getUsername());
      throw new ServerErrorException(message);
    }
    
    log.info(format("Creating token transaction from %s to %s amount %.0f contract %s", remitter.getWalletAddress(), beneficiary.getWalletAddress(), amount, community.getTokenContractAddress()));
    
    Credentials credentials = credentials(walletUser.getWallet(), WALLET_PASSWORD);
    Token token = loadToken(
        credentials,
        community.getTokenContractAddress(),
        GAS_PRICE,
        TOKEN_TRANSFER_FROM_GAS_LIMIT);
    
    TransactionReceipt receipt = handleBlockchainException(() -> {
      return token.transferFrom(remitter.getWalletAddress(), beneficiary.getWalletAddress(), toTokensWithoutDecimals(amount)).send();
    });
    
    log.info(format("Token transaction sent, hash %s", receipt.getTransactionHash()));
    return receipt.getTransactionHash();
  }

  private String transferTokensAdmin(User remitter, User beneficiary, Long communityId, BigDecimal amount) {
    Community community = communityRepository.findPublicStrictById(communityId);

    log.info(format("Creating token transaction from %s to %s amount %.0f contract %s", remitter.getWalletAddress(), beneficiary.getWalletAddress(), amount, community.getTokenContractAddress()));

    Credentials credentials = credentials(remitter.getWallet(), WALLET_PASSWORD);
    Token token = loadToken(
        credentials,
        community.getTokenContractAddress(),
        GAS_PRICE,
        TOKEN_TRANSFER_GAS_LIMIT);

    TransactionReceipt receipt = handleBlockchainException(() -> {
      return token.transfer(beneficiary.getWalletAddress(), toTokensWithoutDecimals(amount)).send();
    });
    
    log.info(format("Token transaction sent, hash %s", receipt.getTransactionHash()));
    return receipt.getTransactionHash();
  }

  public String transferTokensFromMainWallet(Community community, User beneficiary, BigDecimal amount) {
    log.info(format("Creating token transaction from %s to %s amount %.0f contract %s", community.getMainWalletAddress(), beneficiary.getWalletAddress(), amount, community.getTokenContractAddress()));

    Credentials credentials = credentials(community.getMainWallet(), WALLET_PASSWORD);
    Token token = loadToken(
        credentials,
        community.getTokenContractAddress(),
        GAS_PRICE,
        TOKEN_TRANSFER_GAS_LIMIT);

    TransactionReceipt receipt = handleBlockchainException(() -> {
      return token.transfer(beneficiary.getWalletAddress(), toTokensWithoutDecimals(amount)).send();
    });
    
    log.info(format("Token transaction sent, hash %s", receipt.getTransactionHash()));
    return receipt.getTransactionHash();
  }

  private BigInteger toTokensWithoutDecimals(BigDecimal amount) {
    return amount.multiply(BigDecimal.TEN.pow(NUMBER_OF_DECIMALS)).toBigIntegerExact();
  }

  private BigDecimal toTokensWithDecimals(BigInteger amount) {
    return new BigDecimal(amount).divide(BigDecimal.TEN.pow(NUMBER_OF_DECIMALS));
  }

  Token loadToken(Credentials remitterCredentials, String tokenContractAddress, BigInteger gasPrice, BigInteger gasLimit) {
    TransactionManager transactionManager = new RawTransactionManager(web3j, remitterCredentials, ChainId.NONE, new NoOpProcessor(web3j));
    return Token.load(tokenContractAddress, web3j, transactionManager, new StaticGasProvider(GAS_PRICE, TOKEN_TRANSFER_GAS_LIMIT));
  }

  Token loadTokenReadOnly(String tokenContractAddress) {
    String walletAddress = systemCredentials().getAddress();
    return Token.load(tokenContractAddress, web3j, new ReadonlyTransactionManager(web3j, walletAddress), new StaticGasProvider(GAS_PRICE, TOKEN_TRANSFER_GAS_LIMIT));
  }

  public void transferEther(User remitter, String beneficiaryAddress, BigInteger amount) {
    Credentials credentials = credentials(remitter.getWallet(), WALLET_PASSWORD);
    transferEther(credentials, beneficiaryAddress, amount);
  }
  
  public void transferEther(Credentials credentials, String beneficiaryAddress, BigInteger amount) {
    log.info(String.format("transferEther %d to %s", amount, beneficiaryAddress));
    TransactionReceipt receipt = handleBlockchainException(() -> {
      TransactionManager tm = new RawTransactionManager(web3j, credentials, ChainId.NONE, new PollingTransactionReceiptProcessor(web3j, DEFAULT_POLLING_FREQUENCY, DEFAULT_POLLING_ATTEMPTS_PER_TX_HASH ));
      Transfer transfer = new Transfer(web3j, tm);
      return transfer.sendFunds(beneficiaryAddress, new BigDecimal(amount), Unit.WEI).send();
    });
    log.info(String.format("Ether transaction receipt received for %s", receipt.getTransactionHash()));
  }

  private <T> T handleBlockchainException(Callable<T> callable) {
    try {
      return callable.call();
    }
    catch (Exception e) {
      if (e.getMessage().contains("insufficient funds for gas"))
        throw new DisplayableException("error.outOfEther");
      throw new RuntimeException(e);
    }
  }

  public String createToken(User owner, String symbol, String name) {
    Credentials credentials = credentials(owner.getWallet(), WALLET_PASSWORD);
    return createToken(credentials, symbol, name);
  }

  public String createToken(Credentials credentials, String symbol, String name) {
    return handleBlockchainException(() -> {
      log.info("Deploying token contract: " + name + " (" + symbol + "), owner: " + credentials.getAddress());
      Token token = handleBlockchainException(() -> {
        return Token.deploy(
            web3j,
            credentials,
            new StaticGasProvider(GAS_PRICE, TOKEN_DEPLOYMENT_GAS_LIMIT),
            name,
            symbol,
            INITIAL_TOKEN_AMOUNT).send();
      });
      if (!token.isValid()) {
        if (token.getTransactionReceipt().isPresent()) {
          throw new RuntimeException("Deploying token contract " + token.getTransactionReceipt().get().getTransactionHash() + " failed");
        } else {
          throw new RuntimeException("Deploying token contract " + token.getContractAddress() + " failed");
        }
      }
      
      log.info("Deploy successful, contract address: " + token.getContractAddress());
      return token.getContractAddress();
    });
  }

  public BigDecimal tokenBalance(User user, Long communityId) {
    try {
      log.info("Token balance request for: " + user.getWalletAddress());
      Community community = communityRepository.findPublicById(communityId).orElseThrow(RuntimeException::new);
      Token token = loadTokenReadOnly(community.getTokenContractAddress());
      BigInteger balance = token.balanceOf(user.getWalletAddress()).send();
      log.info("Token balance request complete, balance " + balance.toString());
      return toTokensWithDecimals(balance);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public BigDecimal getBalance(String address) {
    try {
      log.info("balance request for " + address);
      BigInteger balance = web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).send().getBalance();
      log.info("balance request for " + address + " complete, balance=" + balance.toString());
      return toTokensWithDecimals(balance);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  
  public String tokenName(String tokenAddress) {
    String tokenName = cache.getTokenName(tokenAddress);
    
    if (tokenName == null) {
      tokenName = getTokenNameFromBlockchain(tokenAddress);
    }
    
    return tokenName;
  }
  
  private synchronized String getTokenNameFromBlockchain(String tokenAddress) {
    String tokenName = cache.getTokenName(tokenAddress);
    if (tokenName != null) return tokenName;

    try {
      log.info(String.format("Token name request for: tokenAddress=%s", tokenAddress));
      Token token = loadTokenReadOnly(tokenAddress);
      tokenName = token.name().send();
      log.info(String.format("Token name request complete: name=%s, tokenAddress=%s", tokenName, tokenAddress));
      
      cache.setTokenName(tokenAddress, tokenName == null ? "" : tokenName);
      return tokenName;
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public String tokenSymbol(String tokenAddress) {
    String tokenSymbol = cache.getTokenSymbol(tokenAddress);
    
    if (tokenSymbol == null) {
      tokenSymbol = getTokenSymbolFromBlockchain(tokenAddress);
    }
    
    return tokenSymbol;
  }
  
  private synchronized String getTokenSymbolFromBlockchain(String tokenAddress) {
    String tokenSymbol = cache.getTokenSymbol(tokenAddress);
    if (tokenSymbol != null) return tokenSymbol;

    try {
      log.info(String.format("Token symbol request for: tokenAddress=%s", tokenAddress));
      Token token = loadTokenReadOnly(tokenAddress);
      tokenSymbol = token.symbol().send();
      log.info(String.format("Token symbol request complete: symbol=%s, tokenAddress=%s", tokenSymbol, tokenAddress));
      
      cache.setTokenSymbol(tokenAddress, tokenSymbol == null ? "" : tokenSymbol);
      return tokenSymbol;
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public BigDecimal totalSupply(String tokenAddress) {
    BigDecimal totalSupply = cache.getTotalSupply(tokenAddress);
    
    if (totalSupply == null) {
      totalSupply = getTotalSupplyFromBlockchain(tokenAddress);
    }
    
    return totalSupply;
  }
  
  private synchronized BigDecimal getTotalSupplyFromBlockchain(String tokenAddress) {
    BigDecimal totalSupply = cache.getTotalSupply(tokenAddress);
    if (totalSupply != null) return totalSupply;

    try {
      log.info(String.format("Token total supply request for: tokenAddress=%s", tokenAddress));
      Token token = loadTokenReadOnly(tokenAddress);
      BigInteger totalSupplyInWei = token.totalSupply().send();
      log.info(String.format("Token total supply complete: totalSupply=%d, tokenAddress=%s", totalSupplyInWei, tokenAddress));
      
      totalSupply = totalSupplyInWei == null ? BigDecimal.ZERO : toTokensWithDecimals(totalSupplyInWei);
      cache.setTotalSupply(tokenAddress, totalSupply);
      return totalSupply;
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  
  public void delegateTokenTransferRight(User walletOwner, Community community) {
    log.info(format("Approving token transfer. [owner=%s, spender=%s, amount=%d]", walletOwner.getUsername(), community.getAdminUser().getUsername(), INITIAL_TOKEN_AMOUNT));

    Credentials credentials = credentials(walletOwner.getWallet(), WALLET_PASSWORD);
    Token token = loadToken(
        credentials,
        community.getTokenContractAddress(),
        GAS_PRICE,
        TOKEN_APPROVE_GAS_LIMIT);
    
    TransactionReceipt receipt = handleBlockchainException(() -> {
      return token.approve(community.getAdminUser().getWalletAddress(), INITIAL_TOKEN_AMOUNT).send();
    });

    log.info(format("Approving token transfer has sent. [hash=%s]", receipt.getTransactionHash()));
  }
  
  public CommunityToken getCommunityToken(String tokenAddress) {
    CommunityToken communityToken = new CommunityToken();
    communityToken.setTokenName(tokenName(tokenAddress));
    communityToken.setTokenSymbol(tokenSymbol(tokenAddress));
    communityToken.setTotalSupply(totalSupply(tokenAddress));
    
    return communityToken;
  }
}
