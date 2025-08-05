package tv.gen;

import java.io.File;
import js.data.AbstractData;
import js.file.Files;
import js.json.JSMap;

public class TvConfig implements AbstractData {

  public File textFile() {
    return mTextFile;
  }

  public File tokenFile() {
    return mTokenFile;
  }

  public boolean testing() {
    return mTesting;
  }

  public File logFile() {
    return mLogFile;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String _0 = "text_file";
  protected static final String _1 = "token_file";
  protected static final String _2 = "testing";
  protected static final String _3 = "log_file";

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    m.putUnsafe(_0, mTextFile.toString());
    m.putUnsafe(_1, mTokenFile.toString());
    m.putUnsafe(_2, mTesting);
    m.putUnsafe(_3, mLogFile.toString());
    return m;
  }

  @Override
  public TvConfig build() {
    return this;
  }

  @Override
  public TvConfig parse(Object obj) {
    return new TvConfig((JSMap) obj);
  }

  private TvConfig(JSMap m) {
    {
      mTextFile = Files.DEFAULT;
      String x = m.opt(_0, (String) null);
      if (x != null) {
        mTextFile = new File(x);
      }
    }
    {
      mTokenFile = Files.DEFAULT;
      String x = m.opt(_1, (String) null);
      if (x != null) {
        mTokenFile = new File(x);
      }
    }
    mTesting = m.opt(_2, false);
    {
      mLogFile = Files.DEFAULT;
      String x = m.opt(_3, (String) null);
      if (x != null) {
        mLogFile = new File(x);
      }
    }
  }

  public static Builder newBuilder() {
    return new Builder(DEFAULT_INSTANCE);
  }

  @Override
  public boolean equals(Object object) {
    if (this == object)
      return true;
    if (object == null || !(object instanceof TvConfig))
      return false;
    TvConfig other = (TvConfig) object;
    if (other.hashCode() != hashCode())
      return false;
    if (!(mTextFile.equals(other.mTextFile)))
      return false;
    if (!(mTokenFile.equals(other.mTokenFile)))
      return false;
    if (!(mTesting == other.mTesting))
      return false;
    if (!(mLogFile.equals(other.mLogFile)))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    int r = m__hashcode;
    if (r == 0) {
      r = 1;
      r = r * 37 + mTextFile.hashCode();
      r = r * 37 + mTokenFile.hashCode();
      r = r * 37 + (mTesting ? 1 : 0);
      r = r * 37 + mLogFile.hashCode();
      m__hashcode = r;
    }
    return r;
  }

  protected File mTextFile;
  protected File mTokenFile;
  protected boolean mTesting;
  protected File mLogFile;
  protected int m__hashcode;

  public static final class Builder extends TvConfig {

    private Builder(TvConfig m) {
      mTextFile = m.mTextFile;
      mTokenFile = m.mTokenFile;
      mTesting = m.mTesting;
      mLogFile = m.mLogFile;
    }

    @Override
    public Builder toBuilder() {
      return this;
    }

    @Override
    public int hashCode() {
      m__hashcode = 0;
      return super.hashCode();
    }

    @Override
    public TvConfig build() {
      TvConfig r = new TvConfig();
      r.mTextFile = mTextFile;
      r.mTokenFile = mTokenFile;
      r.mTesting = mTesting;
      r.mLogFile = mLogFile;
      return r;
    }

    public Builder textFile(File x) {
      mTextFile = (x == null) ? Files.DEFAULT : x;
      return this;
    }

    public Builder tokenFile(File x) {
      mTokenFile = (x == null) ? Files.DEFAULT : x;
      return this;
    }

    public Builder testing(boolean x) {
      mTesting = x;
      return this;
    }

    public Builder logFile(File x) {
      mLogFile = (x == null) ? Files.DEFAULT : x;
      return this;
    }

  }

  public static final TvConfig DEFAULT_INSTANCE = new TvConfig();

  private TvConfig() {
    mTextFile = Files.DEFAULT;
    mTokenFile = Files.DEFAULT;
    mLogFile = Files.DEFAULT;
  }

}
