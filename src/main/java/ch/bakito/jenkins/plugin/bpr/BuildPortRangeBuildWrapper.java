package ch.bakito.jenkins.plugin.bpr;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.tasks.BuildWrapper;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;
import java.util.Map;

@SuppressWarnings("rawtypes")
public class BuildPortRangeBuildWrapper extends BuildWrapper {

  private final Integer portPoolSize;

  private Range range = null;

  /**
     *
     */
  @DataBoundConstructor
  public BuildPortRangeBuildWrapper(Integer portPoolSize) {
    super();
    this.portPoolSize = portPoolSize;
  }

  public Integer getPortPoolSize() {
    return portPoolSize;
  }

  @Override
  public Environment setUp(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {

    if (portPoolSize != null) {
      DescriptorImpl descriptor = getDescriptor();
      range = descriptor.getRange(build.getId(), portPoolSize);
    }

    return new PoolConfigEnvironment();
  }

  public class PoolConfigEnvironment extends Environment {

    @Override
    public boolean tearDown(AbstractBuild build, BuildListener listener) throws IOException, InterruptedException {
      System.err.println("tearDown");

      if (portPoolSize != null) {
        DescriptorImpl descriptor = getDescriptor();
        descriptor.releaseRange(build.getId());
      }
      return true;
    }
  }

  @Override
  public void makeBuildVariables(AbstractBuild build, Map<String, String> variables) {
    super.makeBuildVariables(build, variables);

    if (range != null) {
      DescriptorImpl descriptor = getDescriptor();
      for (int i = range.getFrom(); i <= range.getTo(); i++) {
        variables.put(descriptor.ENV_VAR_PREFIX + "_" + (i - range.getFrom()), String.valueOf(range.getFrom() + i));
      }
    }

  }

  @Override
  public DescriptorImpl getDescriptor() {
    return (DescriptorImpl) super.getDescriptor();
  }

  @Extension
  public static class DescriptorImpl extends Descriptor<BuildWrapper> {

    private static final String ENV_VAR_PREFIX = "PORT_POOL";
    private int startPort = 50000;
    private int poolSize = 1000;

    private String[] pool = new String[0];

    public DescriptorImpl() {
      init();
    }

    public Range getRange(String jobId, int ports) {
      synchronized (pool) {

        int startIndex = 0;
        int cnt = 0;
        for (int i = 0; i < pool.length; i++) {
          if (pool[i] == null) {
            if (startIndex < 0) {
              startIndex = i;
            }

            cnt++;
            if (cnt == ports) {
              break;
            }
          } else {
            if (cnt < ports) {
              startIndex = -1;
              cnt = 0;
            }
          }

        }
        for (int i = startIndex; i < startIndex + ports; i++) {
          pool[i] = jobId;
        }
        return new Range(startPort + startIndex, startPort + startIndex + ports - 1);
      }
    }

    public void releaseRange(String jobName) {
      synchronized (pool) {
        for (int i = 0; i < pool.length; i++) {
          if (jobName.equals(pool[i])) {
            pool[i] = null;
          }
        }
      }
    }

    private synchronized void init() {
      pool = new String[poolSize];
    }

    @Override
    public String getDisplayName() {
      return "Build Port Range Configuration";
    }

    /**
     * @return the startPort
     */
    public int getStartPort() {
      return startPort;
    }

    /**
     * @param startPort the startPort to set
     */
    public void setStartPort(int startPort) {
      this.startPort = startPort;
    }

    /**
     * @return the poolSize
     */
    public int getPoolSize() {
      return poolSize;
    }

    /**
     * @param poolSize the poolSize to set
     */
    public void setPoolSize(int poolSize) {
      this.poolSize = poolSize;
      init();
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
      req.bindJSON(this, formData);
      save();
      return super.configure(req, formData);
    }
  }

  private static final class Range {

    private final int from;
    private final int to;

    public Range(int from, int to) {
      this.from = from;
      this.to = to;
    }

    public int getFrom() {
      return from;
    }

    public int getTo() {
      return to;
    }
  }
}
