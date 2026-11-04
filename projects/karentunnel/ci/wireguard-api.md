# Real WireGuard tunnel API

Version: `1.0.20260102`

## Public classes
```
com.wireguard.android.backend.Backend
com.wireguard.android.backend.BackendException
com.wireguard.android.backend.GoBackend
com.wireguard.android.backend.Statistics
com.wireguard.android.backend.Tunnel
com.wireguard.android.backend.WgQuickBackend
com.wireguard.android.util.RootShell
com.wireguard.android.util.SharedLibraryLoader
com.wireguard.android.util.ToolsInstaller
com.wireguard.config.Attribute
com.wireguard.config.BadConfigException
com.wireguard.config.Config
com.wireguard.config.InetAddresses
com.wireguard.config.InetEndpoint
com.wireguard.config.InetNetwork
com.wireguard.config.Interface
com.wireguard.config.ParseException
com.wireguard.config.Peer
com.wireguard.crypto.Curve25519
com.wireguard.crypto.Key
com.wireguard.crypto.KeyFormatException
com.wireguard.crypto.KeyPair
com.wireguard.util.NonNullForAll
```

## `com.wireguard.android.backend.Backend`
```
Compiled from "Backend.java"
public interface com.wireguard.android.backend.Backend {
  public abstract java.util.Set<java.lang.String> getRunningTunnelNames();
  public abstract com.wireguard.android.backend.Tunnel$State getState(com.wireguard.android.backend.Tunnel) throws java.lang.Exception;
  public abstract com.wireguard.android.backend.Statistics getStatistics(com.wireguard.android.backend.Tunnel) throws java.lang.Exception;
  public abstract java.lang.String getVersion() throws java.lang.Exception;
  public abstract boolean isAlwaysOn() throws java.lang.Exception;
  public abstract boolean isLockdownEnabled() throws java.lang.Exception;
  public abstract com.wireguard.android.backend.Tunnel$State setState(com.wireguard.android.backend.Tunnel, com.wireguard.android.backend.Tunnel$State, com.wireguard.config.Config) throws java.lang.Exception;
}
```

## `com.wireguard.android.backend.Tunnel`
```
Compiled from "Tunnel.java"
public interface com.wireguard.android.backend.Tunnel {
  public static final int NAME_MAX_LENGTH;
  public static final java.util.regex.Pattern NAME_PATTERN;
  public static boolean isNameInvalid(java.lang.CharSequence);
  public abstract java.lang.String getName();
  public abstract void onStateChange(com.wireguard.android.backend.Tunnel$State);
  static {};
}
```

## `com.wireguard.android.backend.Statistics`
```
Compiled from "Statistics.java"
public class com.wireguard.android.backend.Statistics {
  com.wireguard.android.backend.Statistics();
  void add(com.wireguard.crypto.Key, long, long, long);
  public boolean isStale();
  public com.wireguard.android.backend.Statistics$PeerStats peer(com.wireguard.crypto.Key);
  public com.wireguard.crypto.Key[] peers();
  public long totalRx();
  public long totalTx();
}
```

## `com.wireguard.android.backend.GoBackend`
```
Compiled from "GoBackend.java"
public final class com.wireguard.android.backend.GoBackend implements com.wireguard.android.backend.Backend {
  public com.wireguard.android.backend.GoBackend(android.content.Context);
  public static void setAlwaysOnCallback(com.wireguard.android.backend.GoBackend$AlwaysOnCallback);
  public java.util.Set<java.lang.String> getRunningTunnelNames();
  public com.wireguard.android.backend.Tunnel$State getState(com.wireguard.android.backend.Tunnel);
  public com.wireguard.android.backend.Statistics getStatistics(com.wireguard.android.backend.Tunnel);
  public java.lang.String getVersion();
  public boolean isAlwaysOn() throws java.util.concurrent.ExecutionException, java.lang.InterruptedException, java.util.concurrent.TimeoutException;
  public boolean isLockdownEnabled() throws java.util.concurrent.ExecutionException, java.lang.InterruptedException, java.util.concurrent.TimeoutException;
  public com.wireguard.android.backend.Tunnel$State setState(com.wireguard.android.backend.Tunnel, com.wireguard.android.backend.Tunnel$State, com.wireguard.config.Config) throws java.lang.Exception;
  static {};
}
```

## `com.wireguard.config.Config`
```
Compiled from "Config.java"
public final class com.wireguard.config.Config {
  public static com.wireguard.config.Config parse(java.io.InputStream) throws java.io.IOException, com.wireguard.config.BadConfigException;
  public static com.wireguard.config.Config parse(java.io.BufferedReader) throws java.io.IOException, com.wireguard.config.BadConfigException;
  public boolean equals(java.lang.Object);
  public com.wireguard.config.Interface getInterface();
  public java.util.List<com.wireguard.config.Peer> getPeers();
  public int hashCode();
  public java.lang.String toString();
  public java.lang.String toWgQuickString();
  public java.lang.String toWgUserspaceString();
}
```

## `com.wireguard.crypto.Key`
```
Compiled from "Key.java"
public final class com.wireguard.crypto.Key {
  public static com.wireguard.crypto.Key fromBase64(java.lang.String) throws com.wireguard.crypto.KeyFormatException;
  public static com.wireguard.crypto.Key fromBytes(byte[]) throws com.wireguard.crypto.KeyFormatException;
  public static com.wireguard.crypto.Key fromHex(java.lang.String) throws com.wireguard.crypto.KeyFormatException;
  static com.wireguard.crypto.Key generatePrivateKey();
  static com.wireguard.crypto.Key generatePublicKey(com.wireguard.crypto.Key);
  public boolean equals(java.lang.Object);
  public byte[] getBytes();
  public int hashCode();
  public java.lang.String toBase64();
  public java.lang.String toHex();
}
```
